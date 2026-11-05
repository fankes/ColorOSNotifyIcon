/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 2017 Fankes Studio(qzmmcn@163.com)
 * https://github.com/fankes/ColorOSNotifyIcon
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 * <p>
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 */
package com.fankes.coloros.notify.hook.entity

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Process
import androidx.core.net.toUri
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.param.factory.isAppNotifyEnabledOf
import com.fankes.coloros.notify.utils.factory.runInSafe
import com.fankes.coloros.notify.utils.tool.IconRuleManagerTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.extension.kavaref.hook
import com.highcapable.yukihookapi.extension.kavaref.lazyClassOrNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.runtime.YukiHookRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

/**
 * 流体云媒体内容服务 (MCS) 核心 Hook 类
 *
 * MCS 独立生成普通流体云的应用图标文件，宿主只消费它产出的 URI
 * 在下载卡片保存图标 URI 前重写图标文件，让后续显示走模块的适配结果
 */
object McsHooker : YukiBaseHooker() {

    /** 下载流体云卡片信息类 */
    private val DownloadLiquidCardDisplayInfoClass by lazyClassOrNull("com.heytap.mcs.liquid.domain.download.cardinfo.DownloadLiquidCardDisplayInfo")

    /** 流体云图标文件提供器 */
    private val LiquidIconFileProviderClass by lazyClassOrNull("com.heytap.mcs.liquid.image.FileProvider")

    /** 用于恢复规则快照的私有协程作用域 */
    private var iconScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 当前代规则恢复任务，低频 MCS 回调等待它完成 */
    private var iconReloadJob: Job? = null

    /** 开关切换清理缓存期间停止模块写入 */
    private val iconFileLock = Any()
    private var isRestarting = false

    /** 已通过 MCS 回调见过的应用图标 URI */
    private val seenIconUris = linkedSetOf<String>()

    /** 当前运行时是否提供模块共享文件 */
    private val isSharedCacheAvailable get() = YukiHook.Status.runtime == YukiHookRuntime.LIBXPOSED

    override fun onHook() {
        ConfigData.init(instance = this)
        if (isSharedCacheAvailable) IconRuleManagerTool.attachHost(sharedFiles)
        withApplicationReady { application ->
            iconReloadJob = iconScope.launch { IconRuleManagerTool.reload(application) }
            // 下载卡片在图标文件生成完成后回调，重写文件后它保存的 URI 就会指向适配结果
            DownloadLiquidCardDisplayInfoClass?.resolve()
                ?.optional()
                ?.firstMethodOrNull { name = "c"; parameters(Uri::class) }
                ?.hook()
                ?.before { compatAppIconUri(application, firstArg().get<Uri>()) }
            LiquidIconFileProviderClass?.resolve()
                ?.optional()
                ?.firstMethodOrNull {
                    name = "openFile"
                    parameters(Uri::class, String::class)
                }
                ?.hook()
                ?.before { compatAppIconUri(application, firstArg().get<Uri>()) }
            SystemUITool.Host.onMcsToggle(param = this, callback = { _ ->
                synchronized(iconFileLock) {
                    isRestarting = true
                    val cacheDir = File(application.filesDir, "cache_images")
                    val files = if (cacheDir.exists()) cacheDir.listFiles() else emptyArray()
                    val cleared = files?.filter { it.name.matches("app_icon_.+_v[0-9]+\\.png".toRegex()) }
                        ?.map { it.delete() || it.exists().not() }?.all { it } == true
                    if (cleared.not()) isRestarting = false
                    cleared
                }
            }, onCleared = {
                // 先发送清理结果，再退出；任务属于本代作用域，热重载时会取消。
                iconScope.launch {
                    delay(100.milliseconds)
                    Process.killProcess(Process.myPid())
                }
            })
            SystemUITool.Host.onRefreshMcs(param = this) {
                val enabled = ConfigData.isEnableMcsNotifyIcon
                runBlocking {
                    IconRuleManagerTool.reload(application)
                    if (enabled) {
                        val uris = synchronized(seenIconUris) { seenIconUris.toList() }
                        uris.forEach { compatAppIconUri(application, it.toUri()) }
                    }
                }
                true
            }
        }
    }

    /** 释放当前 MCS 代实例，避免 modern 热重载后保留旧规则、上下文和协程 */
    fun release() {
        iconReloadJob?.cancel()
        iconReloadJob = null
        iconScope.cancel()
        iconScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        synchronized(seenIconUris) { seenIconUris.clear() }
        IconRuleManagerTool.release()
    }

    /**
     * 将 MCS 生成的应用图标文件重写为模块适配后的图标
     *
     * 文件已缓存时 MCS 不会重复生成，这里只在回调到达时覆盖一次，保持低频
     * @param uri MCS 生成的应用图标 URI
     */
    private fun compatAppIconUri(context: Context, uri: Uri?) = runInSafe {
        iconReloadJob?.let { runBlocking { it.join() } }
        uri?.toString()?.let { synchronized(seenIconUris) { seenIconUris += it } }
        val name = uri?.lastPathSegment
        val packageName = packageNameOf(context, uri)
        val enabled = ConfigData.isEnableMcsNotifyIcon
        if (enabled.not()) return@runInSafe
        packageName ?: return@runInSafe
        val icon = IconRuleManagerTool.getIcon(packageName) ?: return@runInSafe
        if (context.isAppNotifyEnabledOf(icon).not()) return@runInSafe
        val sizePx = (context.resources.displayMetrics.density * 40).toInt().coerceAtLeast(1)
        val bitmap = SystemUIHooker.renderMcsIcon(context, packageName, sizePx)
            ?: IconRuleManagerTool.getBitmap(icon)
            ?: return@runInSafe
        name ?: return@runInSafe
        val file = File(File(context.filesDir, "cache_images"), name)
        if (file.exists().not()) {
            return@runInSafe
        }
        synchronized(iconFileLock) {
            if (isRestarting) return@runInSafe
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    /**
     * 从 MCS 应用图标 URI 解析包名，文件名为 app_icon_{包名}_v{版本}.png
     * @param uri 应用图标 URI
     * @return 包名 or null
     */
    private fun packageNameOf(context: Context, uri: Uri?): String? {
        val name = uri?.lastPathSegment ?: return null
        val match = "app_icon_(.+)_v[0-9]+\\.png".toRegex().matchEntire(name) ?: return null
        val encodedPackageName = match.groupValues[1]
        @Suppress("QueryPermissionsNeeded")
        return context.packageManager.getInstalledPackages(0).firstOrNull {
            it.packageName.replace('.', '_') == encodedPackageName
        }?.packageName
    }
}