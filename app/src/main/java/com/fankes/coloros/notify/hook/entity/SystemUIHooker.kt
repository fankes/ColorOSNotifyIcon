/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 2017-2023 Fankes Studio(qzmmcn@163.com)
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
 * This file is Created by fankes on 2022/3/25.
 */
@file:Suppress("StaticFieldLeak")

package com.fankes.coloros.notify.hook.entity

import android.app.Notification
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import android.util.ArrayMap
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.bean.IconDataBean
import com.fankes.coloros.notify.const.PackageName
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.param.IconPackParams
import com.fankes.coloros.notify.param.factory.isAppNotifyHookAllOf
import com.fankes.coloros.notify.param.factory.isAppNotifyHookOf
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.BitmapCompatTool
import com.fankes.coloros.notify.utils.tool.IconAdaptationTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.log.loggerW
import com.highcapable.yukihookapi.hook.type.android.*
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import top.defaults.drawabletoolbox.DrawableBuilder

/**
 * 系统界面核心 Hook 类
 */
object SystemUIHooker : YukiBaseHooker() {

    /** 原生存在的类 */
    private const val ContrastColorUtilClass = "com.android.internal.util.ContrastColorUtil"

    /** 原生存在的类 */
    private const val NotificationUtilsClass = "${PackageName.SYSTEMUI}.statusbar.notification.NotificationUtils"

    /** 原生存在的类 */
    private const val NotificationEntryClass = "${PackageName.SYSTEMUI}.statusbar.notification.collection.NotificationEntry"

    /** 原生存在的类 */
    private const val StatusBarIconClass = "com.android.internal.statusbar.StatusBarIcon"

    /** 原生存在的类 */
    private const val StatusBarIconViewClass = "${PackageName.SYSTEMUI}.statusbar.StatusBarIconView"

    /** 原生存在的类 */
    private const val IconBuilderClass = "${PackageName.SYSTEMUI}.statusbar.notification.icon.IconBuilder"

    /** 原生存在的类 */
    private const val IconManagerClass = "${PackageName.SYSTEMUI}.statusbar.notification.icon.IconManager"

    /** 原生存在的类 */
    private const val NotificationBackgroundViewClass = "${PackageName.SYSTEMUI}.statusbar.notification.row.NotificationBackgroundView"

    /** 原生存在的类 */
    private const val PlayerViewHolderClass = "${PackageName.SYSTEMUI}.media.PlayerViewHolder"

    /** 原生存在的类 */
    private const val MediaDataClass = "${PackageName.SYSTEMUI}.media.MediaData"

    /** ColorOS 存在的类 - 旧版本不存在 */
    private const val OplusContrastColorUtilClass = "com.oplusos.util.OplusContrastColorUtil"

    /** ColorOS 存在的类 - 旧版本不存在 */
    private const val OplusNotificationBackgroundViewClass =
        "com.oplusos.systemui.statusbar.notification.row.OplusNotificationBackgroundView"

    /** ColorOS 存在的类 - 旧版本不存在 */
    private const val OplusMediaControlPanelClass = "com.oplusos.systemui.media.OplusMediaControlPanel"

    /** ColorOS 存在的类 - 旧版本不存在 */
    private const val OplusMediaViewControllerClass = "com.oplusos.systemui.media.OplusMediaViewController"

    /** ColorOS 存在的类 - 旧版本不存在 */
    private const val BasePlayViewHolderClass = "com.oplusos.systemui.media.base.BasePlayViewHolder"

    /** 根据多个版本存在不同的包名相同的类 */
    private val OplusNotificationIconAreaControllerClass = VariousClass(
        "com.oplusos.systemui.statusbar.phone.OplusNotificationIconAreaController",
        "com.oplusos.systemui.statusbar.policy.OplusNotificationIconAreaController",
        "com.coloros.systemui.statusbar.policy.ColorNotificationIconAreaController"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val SystemPromptControllerClass = VariousClass(
        "com.oplusos.systemui.statusbar.policy.SystemPromptController",
        "com.coloros.systemui.statusbar.policy.ColorSystemPromptController"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val RoundRectDrawableUtilClass = VariousClass(
        "com.oplusos.systemui.notification.util.RoundRectDrawableUtil",
        "com.coloros.systemui.notification.util.RoundRectDrawableUtil"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val RoundRectDrawableUtil_CompanionClass = VariousClass(
        "com.oplusos.systemui.notification.util.RoundRectDrawableUtil\$Companion",
        "com.coloros.systemui.notification.util.RoundRectDrawableUtil\$Companion"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val DndAlertHelperClass = VariousClass(
        "com.oplusos.systemui.notification.helper.DndAlertHelper",
        "com.coloros.systemui.notification.helper.DndAlertHelper"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val OplusPowerNotificationWarningsClass = VariousClass(
        "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings",
        "com.coloros.systemui.notification.power.ColorosPowerNotificationWarnings"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val StatusBarNotificationPresenterClass = VariousClass(
        "${PackageName.SYSTEMUI}.statusbar.phone.StatusBarNotificationPresenter",
        "${PackageName.SYSTEMUI}.statusbar.phone.StatusBar"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val ExpandableNotificationRowClass = VariousClass(
        "${PackageName.SYSTEMUI}.statusbar.notification.row.ExpandableNotificationRow",
        "${PackageName.SYSTEMUI}.statusbar.ExpandableNotificationRow"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationViewWrapperClass = VariousClass(
        "${PackageName.SYSTEMUI}.statusbar.notification.row.wrapper.NotificationViewWrapper",
        "${PackageName.SYSTEMUI}.statusbar.notification.NotificationViewWrapper"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationHeaderViewWrapperClass = VariousClass(
        "${PackageName.SYSTEMUI}.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper",
        "${PackageName.SYSTEMUI}.statusbar.notification.NotificationHeaderViewWrapper"
    )

    /** 缓存的彩色 APP 图标 */
    private var appIcons = ArrayMap<String, Drawable>()

    /** 缓存的通知优化图标数组 */
    private var iconDatas = ArrayList<IconDataBean>()

    /** 状态栏通知图标容器 */
    private var notificationIconContainer: ViewGroup? = null

    /** 状态栏通知图标数组 */
    private var notificationIconInstances = ArrayList<View>()

    /** 媒体通知 [View] */
    private var notificationPlayerView: View? = null

    /** 通知栏通知控制器 */
    private var notificationPresenter: Any? = null

    /** 仅监听一次主题壁纸颜色变化 */
    private var isWallpaperColorListenerSetUp = false

    /** 是否已经使用过缓存刷新功能 */
    private var isUsingCachingMethod = false

    /**
     * 判断通知是否来自系统推送
     * @return [Boolean]
     */
    private val StatusBarNotification.isOplusPush get() = opPkg == PackageName.SYSTEM_FRAMEWORK && opPkg != packageName

    /**
     * 判断通知背景是否为旧版本
     * @return [Boolean]
     */
    private val isOldNotificationBackground
        get() = NotificationBackgroundViewClass.toClassOrNull()?.hasMethod {
            name = "drawCustom"
            paramCount = 2
        } ?: false

    /**
     * 打印日志
     * @param tag 标识
     * @param context 实例
     * @param nf 通知实例
     * @param isCustom 是否为通知优化生效图标
     * @param isGrayscale 是否为灰度图标
     */
    private fun loggerDebug(tag: String, context: Context, nf: StatusBarNotification?, isCustom: Boolean, isGrayscale: Boolean) {
        if (ConfigData.isEnableModuleLog) loggerD(
            msg = "(Processing $tag) ↓\n" +
                    "[Title]: ${nf?.notification?.extras?.getString(Notification.EXTRA_TITLE)}\n" +
                    "[Content]: ${nf?.notification?.extras?.getString(Notification.EXTRA_TEXT)}\n" +
                    "[App Name]: ${context.appNameOf(packageName = nf?.packageName ?: "")}\n" +
                    "[Package Name]: ${nf?.packageName}\n" +
                    "[Sender Package Name]: ${nf?.opPkg}\n" +
                    "[Custom Icon]: $isCustom\n" +
                    "[Grayscale Icon]: $isGrayscale\n" +
                    "[From System Push]: ${nf?.isOplusPush}\n" +
                    "[String]: ${nf?.notification}"
        )
    }

    /**
     * 注册主题壁纸改变颜色监听
     *
     *  - 仅限在 Android 12 上注册
     * @param view 实例
     */
    private fun registerWallpaperColorChanged(view: View) = runInSafe {
        if (isWallpaperColorListenerSetUp.not() && isUpperOfAndroidS) view.apply {
            WallpaperManager.getInstance(context).addOnColorsChangedListener({ _, _ -> refreshNotificationIcons() }, handler)
        }
        isWallpaperColorListenerSetUp = true
    }

    /** 刷新状态栏小图标 */
    private fun refreshStatusBarIcons() = runInSafe {
        val nfField = StatusBarIconViewClass.toClass().field { name = "mNotification" }
        val sRadiusField = StatusBarIconViewClass.toClass().field { name = "sIconRadiusFraction" }
        val sNfSizeField = StatusBarIconViewClass.toClass().field { name = "sNotificationRoundIconSize" }
        val roundUtil = RoundRectDrawableUtil_CompanionClass.toClass().method {
            name = "getRoundRectDrawable"
            param(DrawableClass, FloatType, IntType, IntType, ContextClass)
        }.onNoSuchMethod { loggerE(msg = "Your system not support \"getRoundRectDrawable\"!", e = it) }
            .get(RoundRectDrawableUtilClass.toClass().field { name = "Companion" }.get().any())
        /** 启动一个线程防止卡顿 */
        Thread {
            (notificationIconContainer?.children?.toList() ?: notificationIconInstances.takeIf { it.isNotEmpty() })?.forEach {
                runInSafe {
                    /** 得到通知实例 */
                    val nf = nfField.get(it).cast<StatusBarNotification>() ?: return@Thread

                    /** 得到原始通知图标 */
                    val iconDrawable = nf.notification.smallIcon.loadDrawable(it.context)
                        ?: return@Thread loggerW(msg = "refreshStatusBarIcons got null smallIcon")
                    /** 获取优化后的状态栏通知图标 */
                    compatStatusIcon(
                        context = it.context,
                        nf = nf,
                        isGrayscaleIcon = isGrayscaleIcon(it.context, iconDrawable),
                        packageName = nf.packageName,
                        drawable = iconDrawable
                    ).also { pair ->
                        /** 得到图标圆角 */
                        val sRadius = sRadiusField.get(it).float()

                        /** 得到缩放大小 */
                        val sNfSize = sNfSizeField.get(it).int()
                        /** 在主线程设置图标 */
                        it.post { (it as? ImageView?)?.setImageDrawable(roundUtil.invoke(pair.first, sRadius, sNfSize, sNfSize, it.context)) }
                    }
                }
            }
        }.start()
    }

    /** 刷新通知小图标 */
    private fun refreshNotificationIcons() = runInSafe {
        notificationPresenter?.current()?.method {
            name = "updateNotificationsOnDensityOrFontScaleChanged"
            emptyParam()
        }?.call()
        modifyNotifyPanelAlpha(notificationPlayerView, isTint = true)
    }

    /**
     * - 这个是修复彩色图标的关键核心代码判断
     *
     * 判断是否为灰度图标 - 反射执行系统方法
     * @param context 实例
     * @param drawable 要判断的图标
     * @return [Boolean]
     */
    private fun isGrayscaleIcon(context: Context, drawable: Drawable) =
        if (ConfigData.isEnableColorIconCompat.not()) safeOfFalse {
            ContrastColorUtilClass.toClass().let {
                it.method {
                    name = "isGrayscaleIcon"
                    param(DrawableClass)
                }.get(it.method {
                    name = "getInstance"
                    param(ContextClass)
                }.get().invoke(context)).boolean(drawable)
            }
        } else BitmapCompatTool.isGrayscaleDrawable(drawable)

    /**
     * 适配通知栏、状态栏来自系统推送的彩色 APP 图标
     *
     * 适配第三方图标包对系统包管理器更换图标后的彩色图标
     * @param iconDrawable 原始图标
     * @return [Drawable] 适配的图标
     */
    private fun StatusBarNotification.compatPushingIcon(iconDrawable: Drawable) = safeOf(iconDrawable) {
        /** 给系统推送设置 APP 自己的图标 */
        if (isOplusPush && opPkg.isNotBlank())
            appIcons[packageName] ?: iconDrawable
        else iconDrawable
    }

    /**
     * 自动适配状态栏、通知栏自定义小图标
     * @param context 实例
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @return [Triple] - ([Drawable] 位图,[Int] 颜色,[Boolean] 是否为占位符图标)
     */
    private fun compatCustomIcon(context: Context, isGrayscaleIcon: Boolean, packageName: String): Triple<Drawable?, Int, Boolean> {
        /** 防止模块资源注入失败重新注入 */
        context.injectModuleAppResources()
        var customPair: Triple<Drawable?, Int, Boolean>? = null
        val statSysAdbIcon = runCatching {
            context.resources.drawableOf("com.android.internal.R\$drawable".toClass().field { name = "stat_sys_adb" }.get().int())
        }.getOrNull() ?: context.resources.drawableOf(R.drawable.ic_unsupported)
        when {
            /** 替换系统图标为 Android 默认 */
            (packageName == PackageName.SYSTEM_FRAMEWORK || packageName == PackageName.SYSTEMUI) && isGrayscaleIcon.not() ->
                customPair = Triple(statSysAdbIcon, 0, false)
            /** 替换自定义通知图标 */
            ConfigData.isEnableNotifyIconFix -> run {
                iconDatas.takeIf { it.isNotEmpty() }?.forEach {
                    if (packageName == it.packageName && isAppNotifyHookOf(it)) {
                        if (isGrayscaleIcon.not() || isAppNotifyHookAllOf(it))
                            customPair = Triple(BitmapDrawable(context.resources, it.iconBitmap), it.iconColor, false)
                        return@run
                    }
                }
                if (isGrayscaleIcon.not() && ConfigData.isEnableNotifyIconFixPlaceholder)
                    customPair = Triple(context.resources.drawableOf(R.drawable.ic_unsupported), 0, true)
            }
        }
        return customPair ?: Triple(null, 0, false)
    }

    /**
     * 自动适配状态栏小图标
     * @param context 实例
     * @param nf 通知实例
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @param drawable 原始图标
     * @return [Pair] - ([Drawable] 图标,[Boolean] 是否替换)
     */
    private fun compatStatusIcon(
        context: Context,
        nf: StatusBarNotification,
        isGrayscaleIcon: Boolean,
        packageName: String,
        drawable: Drawable
    ) = compatCustomIcon(context, isGrayscaleIcon, packageName).let {
        /** 打印日志 */
        loggerDebug(tag = "Status Bar Icon", context, nf, isCustom = it.first != null && it.third.not(), isGrayscaleIcon)
        it.first?.let { e -> Pair(e, true) } ?: Pair(if (isGrayscaleIcon) drawable else nf.compatPushingIcon(drawable), isGrayscaleIcon.not())
    }

    /**
     * 自动适配通知栏小图标
     * @param context 实例
     * @param nf 通知实例
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @param drawable 原始图标
     * @param iconColor 原生图标颜色
     * @param iconView 图标 [ImageView]
     */
    private fun compatNotifyIcon(
        context: Context,
        nf: StatusBarNotification,
        isGrayscaleIcon: Boolean,
        packageName: String,
        drawable: Drawable,
        iconColor: Int,
        iconView: ImageView
    ) = runInSafe {
        compatCustomIcon(context, isGrayscaleIcon, packageName).also { customTriple ->
            when {
                ConfigData.isEnableNotifyIconForceAppIcon -> iconView.apply {
                    /** 重新设置图标 */
                    setImageDrawable(appIcons[packageName] ?: context.appIconOf(packageName))
                    /** 设置默认样式 */
                    setDefaultNotifyIconViewStyle()
                }
                (customTriple.first != null && customTriple.third.not()) || isGrayscaleIcon -> iconView.apply {
                    /** 设置不要裁切到边界 */
                    clipToOutline = false
                    /** 重新设置图标 */
                    setImageDrawable(customTriple.first ?: drawable)

                    /** 旧版风格 */
                    val oldStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else 0xff707173).toInt()

                    /** 新版风格 */
                    val newStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else Color.WHITE).toInt()

                    /** 原生着色 */
                    val md3Style = if (isUpperOfAndroidS) context.systemAccentColor else
                        (if (context.isSystemInDarkMode) 0xff707173 else oldStyle).toInt()

                    /** 旧版图标着色 */
                    val oldApplyColor = customTriple.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: oldStyle

                    /** 新版图标着色 */
                    val newApplyColor = customTriple.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: md3Style

                    /** 判断风格并开始 Hook */
                    if (ConfigData.isEnableMd3NotifyIconStyle) {
                        /** 通知图标边框圆角大小 */
                        background = DrawableBuilder()
                            .rectangle()
                            .cornerRadius(ConfigData.notifyIconCornerSize.dp(context))
                            .solidColor(newApplyColor)
                            .build()
                        setColorFilter(newStyle)
                        setPadding(2.dp(context), 2.dp(context), 2.dp(context), 2.dp(context))
                    } else {
                        background = null
                        setColorFilter(oldApplyColor)
                        setPadding(0, 0, 0, 0)
                    }
                }
                else -> iconView.apply {
                    /** 重新设置图标 */
                    setImageDrawable(nf.compatPushingIcon(drawable))
                    /** 设置默认样式 */
                    setDefaultNotifyIconViewStyle()
                }
            }
            /** 是否为通知优化生效图标 */
            val isCustom = customTriple.first != null && customTriple.third.not()
            /** 打印日志 */
            loggerDebug(tag = "Notification Panel Icon", iconView.context, nf, isCustom = isCustom, isGrayscaleIcon)
        }
    }

    /**
     * 设置通知面板背景透明度
     * @param view 背景 View 实例
     * @param drawable 背景实例
     * @param isTint 是否着色 [view]
     */
    private fun modifyNotifyPanelAlpha(view: View?, drawable: Drawable? = null, isTint: Boolean = false) {
        val isEnable = ConfigData.isEnableNotifyPanelAlpha
        val currentAlpha = ConfigData.notifyPanelAlphaLevel
        val currendColor = if (view?.context?.isSystemInDarkMode == true) 0xFF404040.toInt() else 0xFFFAFAFA.toInt()
        /** 设置通知面板背景透明度 */
        when {
            isEnable.not() -> {
                if (isTint) view?.backgroundTintList = ColorStateList.valueOf(currendColor)
                else drawable?.setTint(currendColor)
            }
            isTint.not() && view?.parent?.parent?.javaClass?.name?.contains("ChildrenContainer") == true -> drawable?.alpha = 0
            else -> {
                currendColor.colorAlphaOf(currentAlpha / 100f).also {
                    if (isTint) view?.backgroundTintList = ColorStateList.valueOf(it)
                    else drawable?.setTint(it)
                }
            }
        }
        /** 移除阴影效果 */
        if (isEnable) view?.elevation = 0f
    }

    /** 设置默认通知栏通知图标样式 */
    private fun ImageView.setDefaultNotifyIconViewStyle() {
        /** 设置裁切到边界 */
        clipToOutline = true
        /** 设置一个圆角轮廓裁切 */
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, out: Outline) {
                out.setRoundRect(
                    0, 0,
                    view.width, view.height, 3.dpFloat(context)
                )
            }
        }
        /** 清除图标间距 */
        setPadding(0, 0, 0, 0)
        /** 清除背景 */
        background = null
        /** 清除着色 */
        colorFilter = null
    }

    /** 注册生命周期 */
    private fun registerLifecycle() {
        onAppLifecycle {
            /** 解锁后重新刷新状态栏图标防止系统重新设置它 */
            registerReceiver(Intent.ACTION_USER_PRESENT) { _, _ -> if (isUsingCachingMethod) refreshStatusBarIcons() }
            /** 注册定时监听 */
            registerReceiver(Intent.ACTION_TIME_TICK) { context, _ ->
                if (ConfigData.isEnableNotifyIconFix && ConfigData.isEnableNotifyIconFixNotify && ConfigData.isEnableNotifyIconFixAuto)
                    IconAdaptationTool.prepareAutoUpdateIconRule(context, ConfigData.notifyIconFixAutoTime)
            }
            /** 注册发送适配新的 APP 图标通知监听 */
            registerReceiver(IntentFilter().apply {
                addDataScheme("package")
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
            }) { context, intent ->
                if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED).not() &&
                    intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                ) return@registerReceiver
                if (ConfigData.isEnableNotifyIconFix && ConfigData.isEnableNotifyIconFixNotify)
                    intent.data?.schemeSpecificPart?.also { packageName ->
                        when (intent.action) {
                            Intent.ACTION_PACKAGE_ADDED -> {
                                if (iconDatas.takeIf { e -> e.isNotEmpty() }
                                        ?.filter { e -> e.packageName == packageName }
                                        .isNullOrEmpty()
                                ) IconAdaptationTool.pushNewAppSupportNotify(context, packageName)
                            }
                            Intent.ACTION_PACKAGE_REMOVED -> IconAdaptationTool.removeNewAppSupportNotify(context, packageName)
                        }
                    }
            }
            /** 注入模块资源 */
            onCreate { injectModuleAppResources() }
        }
        /** 刷新图标缓存 */
        SystemUITool.Host.onRefreshSystemUI(param = this) { recachingPrefs(it) }
    }

    /** 缓存图标数据 */
    private fun cachingIconDatas() {
        iconDatas.clear()
        IconPackParams(param = this).iconDatas.apply { if (isNotEmpty()) forEach { iconDatas.add(it) } }
    }

    /**
     * 刷新缓存数据
     * @param isRefreshCacheOnly 仅刷新缓存不刷新图标和通知改变 - 默认：否
     * @return [Boolean] 是否成功
     */
    private fun recachingPrefs(isRefreshCacheOnly: Boolean = false): Boolean {
        /** 必要的延迟防止 Sp 存储不刷新 */
        SystemClock.sleep(300)
        /** 获取可读写状态 */
        return prefs.isPreferencesAvailable.also {
            isUsingCachingMethod = true
            prefs.clearCache()
            cachingIconDatas()
            if (isRefreshCacheOnly) return@also
            refreshStatusBarIcons()
            refreshNotificationIcons()
        }
    }

    override fun onHook() {
        /** 注册生命周期 */
        registerLifecycle()
        /** 缓存图标数据 */
        cachingIconDatas()
        /** 移除开发者警告通知 */
        SystemPromptControllerClass.hook {
            injectMember {
                method { name = "updateDeveloperMode" }
                beforeHook {
                    /** 是否移除 */
                    if (ConfigData.isEnableRemoveDevNotify) resultNull()
                }
            }
        }
        /** 移除充电完成通知 */
        OplusPowerNotificationWarningsClass.hook {
            injectMember {
                method {
                    name = "showChargeErrorDialog"
                    param(IntType)
                }
                beforeHook {
                    /** 是否移除 */
                    if (args().first().int() == 7 && ConfigData.isEnableRemoveChangeCompleteNotify) resultNull()
                }
            }
        }
        /** 移除免打扰通知 */
        DndAlertHelperClass.hook {
            injectMember {
                method {
                    name { it.lowercase() == "sendnotificationwithendtime" }
                    param(LongType)
                }
                beforeHook {
                    /** 是否移除 */
                    if (ConfigData.isEnableRemoveDndAlertNotify) resultNull()
                }
            }
        }
        /** 修复并替换新版本 ColorOS 原生灰度图标色彩判断 */
        NotificationUtilsClass.hook {
            injectMember {
                method {
                    name = "isGrayscaleOplus"
                    param(ImageViewClass, OplusContrastColorUtilClass)
                }
                replaceAny { args().first().cast<ImageView>()?.let { isGrayscaleIcon(it.context, it.drawable) } ?: callOriginal() }
            }.ignoredHookingFailure()
        }
        /** 替换状态栏图标 */
        IconManagerClass.hook {
            injectMember {
                method {
                    name = "getIconDescriptor"
                    param(NotificationEntryClass, BooleanType)
                }
                afterHook {
                    IconBuilderClass.toClass().field { name = "context" }
                        .get(field { name = "iconBuilder" }.get(instance).cast()).cast<Context>()?.also { context ->
                            NotificationEntryClass.toClass().method {
                                name = "getSbn"
                            }.get(args().first().any()).invoke<StatusBarNotification>()?.also { nf ->
                                nf.notification.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                                    compatStatusIcon(
                                        context = context,
                                        nf = nf,
                                        isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable).also {
                                            /** 缓存第一次的 APP 小图标 */
                                            if (it.not()) context.appIconOf(nf.packageName)?.also { e -> appIcons[nf.packageName] = e }
                                        },
                                        packageName = nf.packageName,
                                        drawable = iconDrawable
                                    ).also { pair ->
                                        if (pair.second) StatusBarIconClass.toClass().field {
                                            name = "icon"
                                            type = IconClass
                                        }.get(result).set(Icon.createWithBitmap(pair.first.toBitmap()))
                                    }
                                }
                            }
                        }
                }
            }
        }
        /** 得到状态栏图标实例 */
        StatusBarIconViewClass.hook {
            injectMember {
                method {
                    name = "setNotification"
                    param(StatusBarNotificationClass)
                }
                afterHook {
                    /** 注册壁纸颜色监听 */
                    if (args().first().any() != null) instance<ImageView>().also { registerWallpaperColorChanged(it) }
                }
            }
        }
        /** 注入通知控制器实例 */
        StatusBarNotificationPresenterClass.hook {
            injectMember {
                allMembers(MembersType.CONSTRUCTOR)
                afterHook { notificationPresenter = instance }
            }
        }
        /** 注入状态栏通知图标容器实例 */
        OplusNotificationIconAreaControllerClass.hook {
            injectMember {
                var isOldWay = false
                method {
                    name = "updateIconsForLayout"
                    paramCount = 10
                }.remedys {
                    method {
                        name = "updateIconsForLayout"
                        paramCount = 1
                    }.onFind { isOldWay = true }
                }
                afterHook {
                    if (isOldWay) {
                        notificationIconInstances.clear()
                        field { name = "mLastToShow" }.get(instance).list<View>()
                            .takeIf { it.isNotEmpty() }?.forEach { notificationIconInstances.add(it) }
                    } else notificationIconContainer = args(index = 1).cast()
                }
            }
        }
        /** 替换通知面板背景 - 新版本 */
        OplusNotificationBackgroundViewClass.hook {
            injectMember {
                method {
                    name = "drawRegionBlur"
                    paramCount = 2
                }.remedys {
                    method {
                        name = "draw"
                        paramCount = 2
                    }
                }
                beforeHook { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }
            injectMember {
                method {
                    name = "draw"
                    paramCount = 2
                    superClass(isOnlySuperClass = true)
                }
                beforeHook { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }
        }.ignoredHookClassNotFoundFailure().by { isOldNotificationBackground.not() }
        /** 替换通知面板背景 - 旧版本 */
        NotificationBackgroundViewClass.hook {
            injectMember {
                method {
                    name = "draw"
                    paramCount = 2
                }
                beforeHook { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }
            injectMember {
                method {
                    name = "drawCustom"
                    paramCount = 2
                }
                beforeHook { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }.ignoredNoSuchMemberFailure()
        }.by { isOldNotificationBackground }
        /** 替换通知面板背景 - 避免折叠展开通知二次修改通知面板背景 */
        ExpandableNotificationRowClass.hook {
            injectMember {
                method {
                    name = "updateBackgroundForGroupState"
                    emptyParam()
                }
                beforeHook {
                    if (ConfigData.isEnableNotifyPanelAlpha) field { name = "mShowGroupBackgroundWhenExpanded" }.get(instance).setTrue()
                }
            }
        }
        /** 替换媒体通知面板背景 - 设置媒体通知自动展开 */
        OplusMediaControlPanelClass.hook {
            injectMember {
                method {
                    name = "bind"
                    paramCount = 2
                }
                afterHook {
                    /** 得到当前实例 */
                    val holder = field {
                        name = "mViewHolder"
                        superClass(isOnlySuperClass = true)
                    }.get(instance).any()
                    /** 记录媒体通知 [View] */
                    notificationPlayerView = PlayerViewHolderClass.toClassOrNull()?.method {
                        name = "getPlayer"
                        emptyParam()
                    }?.get(holder)?.invoke()
                    /** 设置背景着色 */
                    modifyNotifyPanelAlpha(notificationPlayerView, isTint = true)
                    /** 当前是否正在播放 */
                    val isPlaying = MediaDataClass.toClassOrNull()?.method {
                        name = "isPlaying"
                        emptyParam()
                    }?.get(args().first().any())?.boolean() ?: false

                    /** 当前通知是否展开 */
                    val isExpanded = OplusMediaViewControllerClass.toClassOrNull()?.method {
                        name = "getExpanded"
                        emptyParam()
                    }?.get(field { name = "mOplusMediaViewController" }.get(instance).any())?.boolean() ?: false
                    /** 符合条件后执行 */
                    if (ConfigData.isEnableNotifyMediaPanelAutoExp.not() || isExpanded || isPlaying.not()) return@afterHook
                    /** 模拟手动展开通知 */
                    BasePlayViewHolderClass.toClassOrNull()?.method {
                        name = "getExpandButton"
                        emptyParam()
                    }?.get(holder)?.invoke<View>()?.performClick()
                }
            }
        }.ignoredHookClassNotFoundFailure()
        /** 替换通知图标和样式 */
        NotificationHeaderViewWrapperClass.hook {
            injectMember {
                method { name { it == "resolveHeaderViews" || it == "onContentUpdated" } }.all()
                afterHook {
                    NotificationHeaderViewWrapperClass.toClass()
                        .field { name = "mIcon" }.get(instance).cast<ImageView>()?.apply {
                            ExpandableNotificationRowClass.toClass()
                                .method { name = "getEntry" }
                                .get(NotificationViewWrapperClass.toClass().field {
                                    name = "mRow"
                                }.get(instance).any()).call()?.let {
                                    it.javaClass.method {
                                        name = "getSbn"
                                    }.get(it).invoke<StatusBarNotification>()
                                }.also { nf ->
                                    nf?.notification?.also {
                                        it.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                                            compatNotifyIcon(
                                                context = context,
                                                nf = nf,
                                                isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable),
                                                packageName = context.packageName,
                                                drawable = iconDrawable,
                                                iconColor = it.color,
                                                iconView = this
                                            )
                                        }
                                    }
                                }
                        }
                }
            }
        }
    }
}