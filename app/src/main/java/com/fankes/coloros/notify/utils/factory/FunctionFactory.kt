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
 *
 * This file is created by fankes on 2022/1/7.
 */
@file:Suppress("unused", "ObsoleteSdkInt", "DEPRECATION")

package com.fankes.coloros.notify.utils.factory

import android.app.Activity
import android.app.Notification
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Base64
import android.view.View
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.wrapper.BuildConfigWrapper
import com.google.android.material.snackbar.Snackbar
import com.highcapable.betterandroid.system.extension.component.clipboardManager
import com.highcapable.betterandroid.system.extension.component.copy
import com.highcapable.betterandroid.system.extension.component.getPackageInfoOrNull
import com.highcapable.betterandroid.system.extension.component.hasPackage
import com.highcapable.betterandroid.system.extension.component.isComponentEnabled
import com.highcapable.betterandroid.system.extension.component.versionCodeCompat
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.betterandroid.ui.component.notification.factory.notificationManager
import com.highcapable.betterandroid.ui.extension.component.base.getColorCompat
import com.highcapable.betterandroid.ui.extension.view.toast
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.hasClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication.Companion.appContext
import com.topjohnwu.superuser.Shell
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 系统深色模式是否开启
 * @return [Boolean] 是否开启
 */
val isSystemInDarkMode get() = appContext.isSystemInDarkMode

/**
 * 系统深色模式是否没开启
 * @return [Boolean] 是否开启
 */
inline val isNotSystemInDarkMode get() = !isSystemInDarkMode

/**
 * 系统深色模式是否开启
 * @return [Boolean] 是否开启
 */
val Context.isSystemInDarkMode get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * 系统深色模式是否没开启
 * @return [Boolean] 是否开启
 */
inline val Context.isNotSystemInDarkMode get() = !isSystemInDarkMode

/**
 * 系统版本是否高于或等于 ColorOS 17
 * @return [Boolean] 是否符合条件
 */
val isUpperOfColorOS17 by lazy {
    (colorOSNumberVersion.removePrefix("V").substringBefore(".").toIntOrNull() ?: 0) >= 17
}

/**
 * 当前设备是否是 ColorOS 定制 Android 系统
 * @return [Boolean] 是否符合条件
 */
val isColorOS by lazy {
    val loader = object {}.javaClass.classLoader ?: ClassLoader.getSystemClassLoader()
    loader.hasClass("oppo.R") || loader.hasClass("com.color.os.ColorBuild") || loader.hasClass("oplus.R")
}

/**
 * 当前设备是否不是 ColorOS 定制 Android 系统
 * @return [Boolean] 是否符合条件
 */
inline val isNotColorOS get() = !isColorOS

/**
 * 当前设备是否为 RealmeUI
 * @return [Boolean] 是否符合条件
 */
val isRealmeUI
    get() = safeOfFalse {
        val query = appContext.contentResolver.query(
            "content://com.oplus.customize.coreapp.configmanager.configprovider.AppFeatureProvider".toUri()
                .buildUpon()
                .appendPath("app_feature")
                .build(), null, "featurename=?", arrayOf("com.android.launcher.device_rm"), null
        )
        val isExist = query != null && query.count > 0
        query?.close()
        isExist
    }

/**
 * 获取 Android 版本代号
 * @return [String]
 */
val androidVersionCodeName
    get() = when (AndroidVersion.code) {
        37 -> "C"
        36 -> "W"
        35 -> "V"
        34 -> "U"
        33 -> "T"
        32 -> "S_V2"
        31 -> "S"
        30 -> "R"
        29 -> "Q"
        28 -> "P"
        27 -> "O_MR1"
        26 -> "O"
        25 -> "N_MR1"
        24 -> "N"
        23 -> "M"
        22 -> "L_MR1"
        21 -> "L"
        else -> "?"
    }

/**
 * 获取 ColorOS 完整版本
 * @return [String]
 */
val colorOSFullVersion get() = "${if (isRealmeUI) "RealmeUI " else ""}$colorOSVersion"

/**
 * 获取 ColorOS 版本
 * @return [String]
 */
val colorOSVersion get() = "$colorOSNumberVersion ${Build.DISPLAY}"

/**
 * 获取 ColorOS 数字版本
 *
 * 新版 ColorOS (15.0.1+) 的 `getOplusOSVERSION` 直接返回 "ro.build.version.oplus.api" 的值 (大于 33)
 * 它不再是 VERSIONS 数组的下标，且 VERSIONS 数组只到 V15.0.0，所以这里直接读取 "ro.build.version.oplusrom"
 * @return [String]
 */
val colorOSNumberVersion
    get() = safeOf(default = "Unavailable") {
        "com.oplus.os.OplusBuild".toClassOrNull()?.let {
            val scope = it.resolve().optional(silent = true)
            val versionIndex = scope.firstMethodOrNull { name = "getOplusOSVERSION" }?.invokeQuietly<Int>() ?: -1
            if (versionIndex > 33) findPropString("ro.build.version.oplusrom").takeIf { e -> e.isNotBlank() }
            else scope.firstFieldOrNull {
                name = "VERSIONS"
            }?.getQuietly<Array<String>>()
                ?.takeIf { e -> e.isNotEmpty() }
                ?.getOrNull(versionIndex)
        } ?: findPropString("ro.system.build.fingerprint", "Unavailable").split("ssi:")[1].split("/")[0].trim()
    }

/**
 * 判断 APP 是否安装
 * @param packageName APP 包名
 * @return [Boolean]
 */
fun Context.isInstall(packageName: String) = packageManager.hasPackage(packageName)

/**
 * 得到 APP 版本信息
 * @return [String]
 */
val Context.appVersionName get() = packageManager.getPackageInfoOrNull(packageName)?.versionName ?: ""

/**
 * 得到 APP 版本号
 * @return [Int]
 */
val Context.appVersionCode get() = packageManager.getPackageInfoOrNull(packageName)?.versionCodeCompat

/**
 * 得到 APP 最后更新时间
 * @param packageName APP 包名
 * @return [Long] or null
 */
fun Context.appLastUpdateTimeOf(packageName: String) = packageManager.getPackageInfoOrNull(packageName)?.lastUpdateTime

/**
 * 得到 APP 名称
 * @param packageName APP 包名 - 默认为当前 APP
 * @return [String]
 */
fun Context.appNameOf(packageName: String = getPackageName()) =
    packageManager.getPackageInfoOrNull(packageName)?.applicationInfo?.loadLabel(packageManager)?.toString() ?: ""

/**
 * 得到 APP 图标
 * @param packageName APP 包名 - 默认为当前 APP
 * @return [Drawable] or null
 */
fun Context.appIconOf(packageName: String = getPackageName()) = packageManager.getPackageInfoOrNull(packageName)?.applicationInfo?.loadIcon(packageManager)

/**
 * 得到 APP 在桌面上的启动 Activity
 * @param packageName APP 包名
 * @return [List]<[ActivityInfo]>
 */
fun Context.launcherActivitiesOf(packageName: String) = runCatching {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(packageName)
    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress", "QueryPermissionsNeeded")
    if (AndroidVersion.isAtLeast(AndroidVersion.T))
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    else packageManager.queryIntentActivities(intent, 0)
}.getOrNull()?.mapNotNull { it.activityInfo } ?: emptyList()

/**
 * 得到 APP 在桌面上显示的图标
 *
 * ColorOS 会在 [ActivityInfo.loadIcon] 中返回当前主题的图标，没有启动 Activity 时回退到 [appIconOf]
 * @param packageName APP 包名
 * @return [Drawable] or null
 */
fun Context.launcherIconOf(packageName: String) =
    runCatching { launcherActivitiesOf(packageName).firstOrNull()?.loadIcon(packageManager) }.getOrNull() ?: appIconOf(packageName)

/**
 * 获取 APP 是否为 DEBUG 版本
 * @param packageName APP 包名
 * @return [Boolean]
 */
fun Context.isDebugApp(packageName: String) =
    safeOfFalse { (packageManager.getPackageInfoOrNull(packageName)?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE) ?: 0) != 0 }

/**
 * 获取 APP 是否为系统 APP
 * @param packageName APP 包名
 * @return [Boolean]
 */
fun Context.isSystemApp(packageName: String) =
    safeOfFalse { (packageManager.getPackageInfoOrNull(packageName)?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 0 }

/**
 * 对数值自动补零
 * @return [String]
 */
val Int.autoZero: String get() = if (this < 10) "0$this" else toString()

/**
 * 从字符串获取小时
 * @return [Int]
 */
val String.hour
    get() = safeOfNan {
        Calendar.getInstance().also {
            it.time = SimpleDateFormat("HH:mm", Locale.CHINA).parse(this) as Date
        }.get(Calendar.HOUR_OF_DAY)
    }

/**
 * 从字符串获取分钟
 * @return [Int]
 */
val String.minute
    get() = safeOfNan {
        Calendar.getInstance().also {
            it.time = SimpleDateFormat("HH:mm", Locale.CHINA).parse(this) as Date
        }.get(Calendar.MINUTE)
    }

/**
 * 是否关闭了通知权限
 * @return [Boolean]
 */
val isNotNoificationEnabled get() = !appContext.notificationManager.areNotificationsEnabled()

/**
 * 网络连接是否正常
 * @return [Boolean] 网络是否连接
 */
val Context.isNetWorkSuccess
    get() = safeOfFalse {
        @Suppress("DEPRECATION")
        getSystemService<ConnectivityManager>()?.activeNetworkInfo != null
    }

/**
 * dp 转换为 pxInt
 * @param context 使用的实例
 * @return [Int]
 */
fun Number.dp(context: Context) = dpFloat(context).toInt()

/**
 * dp 转换为 pxFloat
 * @param context 使用的实例
 * @return [Float]
 */
fun Number.dpFloat(context: Context) = toFloat() * context.resources.displayMetrics.density

/**
 * 获取系统主题色
 *
 * 由于 ColorOS 阉割了 [android.R.color.system_accent1_600] 这里取系统壁纸颜色做补偿
 *
 * 从 ColorOS 13 (Android 13) 开始使用系统取色方案
 * @return [Int] Android < 13 返回 [wallpaperColor]
 */
val Context.systemAccentColor
    get() = safeOf(wallpaperColor) {
        if (AndroidVersion.isGreaterThan(AndroidVersion.S)) resources.getColorCompat(android.R.color.system_accent1_600) else wallpaperColor
    }

/**
 * 获取系统壁纸颜色
 * @return [Int] 无法获取时返回默认颜色
 */
val Context.wallpaperColor
    get() = runCatching {
        WallpaperManager.getInstance(this).getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()
    }.getOrNull() ?: (if (isSystemInDarkMode) 0xFFD8D8D8.toInt() else 0xFF707173.toInt())

/**
 * 是否为白色
 * @return [Boolean]
 */
val Int.isWhiteColor
    get() = safeOfTrue {
        val r = this and 0xff0000 shr 16
        val g = this and 0x00ff00 shr 8
        val b = this and 0x0000ff
        (0.2126 * r + 0.7152 * g + 0.0722 * b) >= 128
    }

/**
 * 调整颜色透明度
 * @param value 透明度
 * @return [Int] 调整后的颜色
 */
fun Int.colorAlphaOf(value: Float) = safeOfNan { (255.coerceAtMost(0.coerceAtLeast((value * 255).toInt())) shl 24) + (0x00ffffff and this) }

/**
 * Base64 加密
 * @return [String]
 */
val String.base64: String get() = Base64.encodeToString(toByteArray(), Base64.DEFAULT)

/**
 * 获取系统 Prop 值
 * @param key Key
 * @param default 默认值
 * @return [String]
 */
fun findPropString(key: String, default: String = "") = safeOf(default) {
    "android.os.SystemProperties".toClassOrNull()
        ?.resolve()
        ?.optional(silent = true)
        ?.firstMethodOrNull {
            name = "get"
            parameters(String::class, String::class)
        }?.invokeQuietly<String>(key, default) ?: default
}

/**
 * 是否有 Root 权限
 * @return [Boolean]
 */
val isRootAccess get() = safeOfFalse {
    @Suppress("DEPRECATION")
    Shell.rootAccess()
}

/**
 * 执行命令
 * @param cmd 命令
 * @param isSu 是否使用 Root 权限执行 - 默认：是
 * @return [String] 执行结果
 */
fun execShell(cmd: String, isSu: Boolean = true) = safeOfNothing {
    @Suppress("DEPRECATION")
    (if (isSu) Shell.su(cmd) else Shell.sh(cmd)).exec().out.let {
        if (it.isNotEmpty()) it[0].trim() else ""
    }
}

/**
 * 弹出 [Snackbar]
 * @param msg 提示内容
 * @param actionText 按钮文本 - 不写默认取消按钮
 * @param callback 按钮事件回调
 */
fun Context.snake(msg: String, actionText: String = "", callback: () -> Unit = {}) =
    Snackbar.make((this as Activity).findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).apply {
        // 有底栏时显示在底栏上方，避免挡住底栏
        (findViewById(R.id.main_bottom_nav) ?: findViewById<View>(R.id.config_bottom_bar))?.let { anchorView = it }
        if (actionText.isBlank()) return@apply
        setActionTextColor(if (isSystemInDarkMode) Color.BLACK else Color.WHITE)
        setAction(actionText) { callback() }
    }.show()

/**
 * 启动系统浏览器
 * @param url 网址
 * @param packageName 指定包名 - 可不填
 */
fun Context.openBrowser(url: String, packageName: String = "") = runCatching {
    startActivity(Intent().apply {
        if (packageName.isNotBlank()) setPackage(packageName)
        action = Intent.ACTION_VIEW
        data = url.toUri()
        // 防止顶栈一样重叠在自己的 APP 中
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}.onFailure {
    if (packageName.isNotBlank()) snake(msg = getString(R.string.common_launch_failed, packageName))
    else snake(msg = getString(R.string.common_launch_browser_failed))
}

/**
 * 跳转 APP 自身设置界面
 * @param packageName 包名
 */
fun Context.openSelfSetting(packageName: String = appContext.packageName) = runCatching {
    if (isInstall(packageName))
        startActivity(Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    else toast(getString(R.string.common_app_not_installed))
}.onFailure { toast(getString(R.string.common_launch_app_info_failed, packageName)) }

/** 跳转通知设置界面 */
fun Context.openNotifySetting() = runCatching {
    Intent().also { intent ->
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        intent.putExtra(Notification.EXTRA_CHANNEL_ID, applicationInfo.uid)
        startActivity(intent)
    }
}.onFailure { snake(msg = getString(R.string.common_open_notify_settings_failed)) }

/**
 * 复制到剪贴板
 * @param content 要复制的文本
 */
fun Context.copyToClipboard(content: String) = runInSafe {
    clipboardManager.apply {
        copy(content)
        (primaryClip?.getItemAt(0)?.text ?: "").also {
            if (it != content) snake(msg = getString(R.string.common_copy_failed)) else snake(msg = getString(R.string.common_copied))
        }
    }
}

/**
 * 时间戳 -> 时间
 * @param format 格式化方法 - 默认：yyyy-MM-dd HH:mm:ss
 * @return [String] 目标字符串时间
 */
fun Long.stampToDate(format: String = "yyyy-MM-dd HH:mm:ss") =
    safeOfNothing { SimpleDateFormat(format, Locale.CHINA).format(Date(this)) ?: "" }

/**
 * 延迟执行
 * @param ms 毫秒 - 默认：150
 * @param it 方法体
 */
fun Any?.delayedRun(ms: Long = 150, it: () -> Unit) = runInSafe {
    @Suppress("DEPRECATION", "ReplaceWithCoroutinesExtension")
    Handler().postDelayed({ it() }, ms)
}

/**
 * 隐藏或显示启动器图标
 *
 * - 你可能需要 LSPosed 的最新版本以开启高版本系统中隐藏 APP 桌面图标功能
 * @param isShow 是否显示
 */
fun Context.hideOrShowLauncherIcon(isShow: Boolean) {
    packageManager?.setComponentEnabledSetting(
        ComponentName(packageName, "${BuildConfigWrapper.APPLICATION_ID}.Home"),
        if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
}

/**
 * 获取启动器图标状态
 * @return [Boolean] 是否显示
 */
val Context.isLauncherIconShowing
    get() = packageManager.isComponentEnabled(ComponentName(packageName, "${BuildConfigWrapper.APPLICATION_ID}.Home"))