/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 20174 Fankes Studio(qzmmcn@163.com)
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
 * This file is created by fankes on 2022/3/25.
 */
@file:Suppress("StaticFieldLeak", "ConstPropertyName")

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
import com.fankes.coloros.notify.utils.factory.appIconOf
import com.fankes.coloros.notify.utils.factory.appNameOf
import com.fankes.coloros.notify.utils.factory.colorAlphaOf
import com.fankes.coloros.notify.utils.factory.delayedRun
import com.fankes.coloros.notify.utils.factory.dp
import com.fankes.coloros.notify.utils.factory.dpFloat
import com.fankes.coloros.notify.utils.factory.drawableOf
import com.fankes.coloros.notify.utils.factory.isSystemInDarkMode
import com.fankes.coloros.notify.utils.factory.isUpperOfAndroidS
import com.fankes.coloros.notify.utils.factory.runInSafe
import com.fankes.coloros.notify.utils.factory.safeOf
import com.fankes.coloros.notify.utils.factory.safeOfFalse
import com.fankes.coloros.notify.utils.factory.systemAccentColor
import com.fankes.coloros.notify.utils.tool.ActivationPromptTool
import com.fankes.coloros.notify.utils.tool.BitmapCompatTool
import com.fankes.coloros.notify.utils.tool.IconAdaptationTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.DrawableClass
import com.highcapable.yukihookapi.hook.type.android.IconClass
import com.highcapable.yukihookapi.hook.type.android.ImageViewClass
import com.highcapable.yukihookapi.hook.type.android.StatusBarNotificationClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import top.defaults.drawabletoolbox.DrawableBuilder
import androidx.core.graphics.drawable.toDrawable
import com.highcapable.yukihookapi.hook.type.android.NotificationClass

/**
 * 系统界面核心 Hook 类
 */
object SystemUIHooker : YukiBaseHooker() {

    /** 原生存在的类 */
    private val ContrastColorUtilClass by lazyClass("com.android.internal.util.ContrastColorUtil")

    /** 原生存在的类 */
    private val NotificationUtilsClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.notification.NotificationUtils")

    /** 原生存在的类 */
    private val NotificationIconAreaControllerClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.phone.NotificationIconAreaController")

    /** 原生存在的类 */
    private val NotificationEntryClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.notification.collection.NotificationEntry")

    /** 原生存在的类 */
    private val StatusBarIconClass by lazyClass("com.android.internal.statusbar.StatusBarIcon")

    /** 原生存在的类 */
    private val StatusBarIconViewClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.StatusBarIconView")

    /** 原生存在的类 */
    private val IconBuilderClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.notification.icon.IconBuilder")

    /** 原生存在的类 */
    private val IconManagerClass by lazyClass("${PackageName.SYSTEMUI}.statusbar.notification.icon.IconManager")

    /** 原生存在的类 */
    private val NotificationBackgroundViewClass by lazyClassOrNull("${PackageName.SYSTEMUI}.statusbar.notification.row.NotificationBackgroundView")

    /** 原生存在的类 */
    private val PlayerViewHolderClass by lazyClassOrNull("${PackageName.SYSTEMUI}.media.PlayerViewHolder")

    /** 原生存在的类 */
    private val MediaDataClass by lazyClassOrNull("${PackageName.SYSTEMUI}.media.MediaData")

    /** 原生存在的类 - 旧版本不存在 */
    private val LegacyNotificationIconAreaControllerImpl by lazyClassOrNull("${PackageName.SYSTEMUI}.statusbar.phone.LegacyNotificationIconAreaControllerImpl")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusContrastColorUtilClass by lazyClassOrNull("com.oplusos.util.OplusContrastColorUtil")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationBackgroundViewClass by lazyClassOrNull(
        "com.oplusos.systemui.statusbar.notification.row.OplusNotificationBackgroundView")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusMediaControlPanelClass by lazyClassOrNull("com.oplusos.systemui.media.OplusMediaControlPanel")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusMediaViewControllerClass by lazyClassOrNull("com.oplusos.systemui.media.OplusMediaViewController")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val BasePlayViewHolderClass by lazyClassOrNull("com.oplusos.systemui.media.base.BasePlayViewHolder")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationSmallIconUtilClass by lazyClassOrNull("com.oplus.systemui.statusbar.notification.util.OplusNotificationSmallIconUtil")

    /** 根据多个版本存在不同的包名相同的类 */
    private val OplusNotificationIconAreaControllerClass by lazyClass(
        VariousClass(
            "com.oplus.systemui.statusbar.phone.OplusNotificationIconAreaController",
            "com.oplusos.systemui.statusbar.phone.OplusNotificationIconAreaController",
            "com.oplusos.systemui.statusbar.policy.OplusNotificationIconAreaController",
            "com.coloros.systemui.statusbar.policy.ColorNotificationIconAreaController"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val SystemPromptControllerClass by lazyClass(
        VariousClass(
            "com.oplus.systemui.statusbar.controller.SystemPromptController",
            "com.oplusos.systemui.statusbar.policy.SystemPromptController",
            "com.coloros.systemui.statusbar.policy.ColorSystemPromptController"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val RoundRectDrawableUtilClass by lazyClass(
        VariousClass(
            "com.oplusos.systemui.notification.util.RoundRectDrawableUtil",
            "com.coloros.systemui.notification.util.RoundRectDrawableUtil"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val RoundRectDrawableUtil_CompanionClass by lazyClass(
        VariousClass(
            "com.oplusos.systemui.notification.util.RoundRectDrawableUtil\$Companion",
            "com.coloros.systemui.notification.util.RoundRectDrawableUtil\$Companion"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val DndAlertHelperClass by lazyClass(
        VariousClass(
            "com.oplus.systemui.statusbar.notification.helper.DndAlertHelper",
            "com.oplusos.systemui.notification.helper.DndAlertHelper",
            "com.coloros.systemui.notification.helper.DndAlertHelper"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val OplusPowerNotificationWarningsClass by lazyClass(
        VariousClass(
            "com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings",
            "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings",
            "com.coloros.systemui.notification.power.ColorosPowerNotificationWarnings"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val StatusBarNotificationPresenterClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.phone.StatusBarNotificationPresenter",
            "${PackageName.SYSTEMUI}.statusbar.phone.StatusBar"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val ExpandableNotificationRowClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.notification.row.ExpandableNotificationRow",
            "${PackageName.SYSTEMUI}.statusbar.ExpandableNotificationRow"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationViewWrapperClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.notification.row.wrapper.NotificationViewWrapper",
            "${PackageName.SYSTEMUI}.statusbar.notification.NotificationViewWrapper"
        )
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationHeaderViewWrapperClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper",
            "${PackageName.SYSTEMUI}.statusbar.notification.NotificationHeaderViewWrapper"
        )
    )

    /** 根据多个版本存在不同的方法相同的类 */
    private val StatusBarIconControllerClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.StatusBarIconView",
            "com.oplus.systemui.statusbar.phone.StatusBarIconControllerExImpl"
        )
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

    /** 通知面板默认背景着色 [ColorStateList] */
    private var defaultNotifyPanelTintList: ColorStateList? = null

    /** 通知面板默认背景阴影效果强度 */
    private var defaultNotifyPanelElevation = -1f

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
        get() = NotificationBackgroundViewClass?.hasMethod {
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
        if (ConfigData.isEnableModuleLog) YLog.debug(
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
        val nfField = StatusBarIconViewClass.field { name = "mNotification" }
        val sRadiusField = StatusBarIconViewClass.field {
            name = "sIconRadiusFraction"
        }.remedys {
            StatusBarIconControllerClass.field { name = "sIconRadiusFraction" }
        }
        val sNfSizeField = StatusBarIconViewClass.field {
            name = "sNotificationRoundIconSize"
        }.remedys {
            StatusBarIconControllerClass.field { name = "sNotificationRoundIconSize" }
        }
        val roundUtil = RoundRectDrawableUtil_CompanionClass.method {
            name = "getRoundRectDrawable"
            param(DrawableClass, FloatType, IntType, IntType, ContextClass)
        }.onNoSuchMethod { YLog.error("Your system not support \"getRoundRectDrawable\"!", it) }
            .get(RoundRectDrawableUtilClass.field { name = "Companion" }.get().any())
        /** 启动一个线程防止卡顿 */
        Thread {
            (notificationIconContainer?.children?.toList() ?: notificationIconInstances.takeIf { it.isNotEmpty() })?.forEach {
                runInSafe {
                    /** 得到通知实例 */
                    val nf = nfField.get(it).cast<StatusBarNotification>() ?: return@Thread

                    /** 得到原始通知图标 */
                    val iconDrawable = nf.notification.smallIcon.loadDrawable(it.context)
                        ?: return@Thread YLog.warn("refreshStatusBarIcons got null smallIcon")
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
            ContrastColorUtilClass.let {
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
                            customPair = Triple(it.iconBitmap.toDrawable(context.resources), it.iconColor, false)
                        return@run
                    }
                }
                if (isGrayscaleIcon.not() && ConfigData.isEnableNotifyIconFixPlaceholder)
                    customPair = Triple(context.resources.drawableOf(R.drawable.ic_message), 0, true)
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
                    val oldStyle = if (context.isSystemInDarkMode) 0xFFDCDCDC.toInt() else 0xFF707173.toInt()

                    /** 新版风格 */
                    val newStyle = if (context.isSystemInDarkMode) 0xFFDCDCDC.toInt() else Color.WHITE

                    /** 原生着色 */
                    val md3Style = if (isUpperOfAndroidS) context.systemAccentColor else
                        (if (context.isSystemInDarkMode) 0xFF707173.toInt() else oldStyle)

                    /** 原生通知图标颜色 */
                    val nativeIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor) 0 else iconColor

                    /** 自定义通知图标颜色 */
                    val customIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor) 0 else customTriple.second

                    /** 旧版图标着色 */
                    val oldApplyColor = customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: oldStyle

                    /** 新版图标着色 */
                    val newApplyColor = customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: md3Style

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
        if (view == null) return
        if (defaultNotifyPanelTintList == null) defaultNotifyPanelTintList = view.backgroundTintList
        if (defaultNotifyPanelElevation < 0f) defaultNotifyPanelElevation = view.elevation
        val isEnable = ConfigData.isEnableNotifyPanelAlpha
        val currentAlpha = ConfigData.notifyPanelAlphaLevel
        val currentColor = if (view.context?.isSystemInDarkMode == true) 0xFF404040.toInt() else 0xFFFAFAFA.toInt()
        /** 设置通知面板背景透明度 */
        when {
            isEnable.not() -> {
                if (isTint) view.backgroundTintList = defaultNotifyPanelTintList
                else drawable?.setTintList(defaultNotifyPanelTintList)
            }
            isTint.not() && view.parent?.parent?.javaClass?.name?.contains("ChildrenContainer") == true -> drawable?.alpha = 0
            else -> {
                currentColor.colorAlphaOf(currentAlpha / 100f).also {
                    if (isTint) view.backgroundTintList = ColorStateList.valueOf(it)
                    else drawable?.setTint(it)
                }
            }
        }
        /** 移除阴影效果 */
        view.elevation = if (isEnable) 0f else defaultNotifyPanelElevation
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
                intent.data?.schemeSpecificPart?.also { packageName ->
                    if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED)) ActivationPromptTool.prompt(context, packageName)
                    if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED).not() &&
                        intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    ) return@registerReceiver
                    if (ConfigData.isEnableNotifyIconFix && ConfigData.isEnableNotifyIconFixNotify)
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
        SystemPromptControllerClass.method {
            name = "updateDeveloperMode"
        }.hook().before {
            /** 是否移除 */
            if (ConfigData.isEnableRemoveDevNotify) resultNull()
        }
        /** 移除充电完成通知 */
        OplusPowerNotificationWarningsClass.method {
            name = "showChargeErrorDialog"
            param(IntType)
        }.hook().before {
            /** 是否移除 */
            if (args().first().int() == 7 && ConfigData.isEnableRemoveChangeCompleteNotify) resultNull()
        }
        /** 移除免打扰通知 */
        DndAlertHelperClass.method {
            name { it.lowercase() == "sendnotificationwithendtime" }
            param(LongType)
        }.remedys {
            method {
                name = "operateNotification"
                param(LongType, IntType, BooleanType)
            }
        }.hook().before {
            /** 是否移除 */
            if (ConfigData.isEnableRemoveDndAlertNotify) resultNull()
        }
        /** 拦截ColorOS使用应用图标判断 */
        OplusNotificationSmallIconUtilClass?.method {
            name = "useAppIconForSmallIcon"
            param(NotificationClass)
        }?.hook()?.before {
            resultFalse()
        }
        /** 修复并替换 ColorOS 以及原生灰度图标色彩判断 */
        NotificationUtilsClass.apply {
            method {
                name = "isGrayscale"
                param(ImageViewClass, ContrastColorUtilClass)
            }.hook().replaceAny { args().first().cast<ImageView>()?.let { isGrayscaleIcon(it.context, it.drawable) } ?: callOriginal() }
            method {
                name = "isGrayscaleOplus"
                param(ImageViewClass, OplusContrastColorUtilClass ?: VagueType)
            }.ignored().hook().replaceAny { args().first().cast<ImageView>()?.let { isGrayscaleIcon(it.context, it.drawable) } ?: callOriginal() }
        }
        /** 替换状态栏图标 */
        IconManagerClass.method {
            name = "getIconDescriptor"
            param(NotificationEntryClass, BooleanType)
        }.hook().after {
            IconBuilderClass.field { name = "context" }
                .get(IconManagerClass.field { name = "iconBuilder" }.get(instance).cast()).cast<Context>()?.also { context ->
                    NotificationEntryClass.method {
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
                                if (pair.second) StatusBarIconClass.field {
                                    name = "icon"
                                    type = IconClass
                                }.get(result).set(Icon.createWithBitmap(pair.first.toBitmap()))
                            }
                        }
                    }
                }
        }
        /** 得到状态栏图标实例 */
        StatusBarIconViewClass.method {
            name = "setNotification"
            param(StatusBarNotificationClass)
        }.hook().after {
            /** 注册壁纸颜色监听 */
            if (args().first().any() != null) instance<ImageView>().also { registerWallpaperColorChanged(it) }
        }
        /** 注入通知控制器实例 */
        StatusBarNotificationPresenterClass.constructor().hookAll().after { notificationPresenter = instance }
        /** 注入状态栏通知图标容器实例 */
        OplusNotificationIconAreaControllerClass.apply {
            var way = 0
            method {
                name = "updateIconsForLayout"
                paramCount = 10
            }.remedys {
                /** ColorOS 14 */
                method {
                    name = "updateIconsForLayout"
                    paramCount = 5
                }
                method {
                    name = "updateIconsForLayout"
                    paramCount = 1
                }.onFind { way = 1 }
                method {
                    name = "updateStatusBarIcons"
                }.onFind { way = 2 }
            }.hook().after {
                when (way) {
                    2 -> notificationIconContainer = OplusNotificationIconAreaControllerClass.method { name = "getNotificationIcons" }.get(instance).invoke()
                    1 -> {
                        notificationIconInstances.clear()
                        field { name = "mLastToShow" }.get(instance).list<View>()
                            .takeIf { it.isNotEmpty() }?.forEach { notificationIconInstances.add(it) }
                    }
                    else -> notificationIconContainer = args(index = 1).cast()
                }
            }
        }
        
        /** 注入状态栏通知图标容器实例 */
        if (LegacyNotificationIconAreaControllerImpl != null) {
            /** ColorOS 15.0.1 */
            LegacyNotificationIconAreaControllerImpl?.apply {
                method {
                    name = "updateIconsForLayout"
                    paramCount = 8
                }.hook().after {
                    notificationIconContainer = args(index = 1).cast()
                }
            }
        } else {
            /** ColorOS 15 */
            NotificationIconAreaControllerClass.apply {
                method {
                    name = "updateIconsForLayout"
                    paramCount = 8
                }.hook().after {
                    notificationIconContainer = args(index = 1).cast()
                }
            }
        }
        
        /** 替换通知面板背景 - 新版本 */
        if (isOldNotificationBackground.not())
            OplusNotificationBackgroundViewClass?.apply {
                method {
                    name = "drawRegionBlur"
                    paramCount = 2
                }.remedys {
                    method {
                        name = "draw"
                        paramCount = 2
                    }
                }.hook().before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
                method {
                    name = "draw"
                    paramCount = 2
                    superClass(isOnlySuperClass = true)
                }.hook().before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }
        /** 替换通知面板背景 - 旧版本 */
        if (isOldNotificationBackground)
            NotificationBackgroundViewClass?.apply {
                method {
                    name = "draw"
                    paramCount = 2
                }.hook().before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
                method {
                    name = "drawCustom"
                    paramCount = 2
                }.ignored().hook().before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
            }
        /** 替换通知面板背景 - 避免折叠展开通知二次修改通知面板背景 */
        ExpandableNotificationRowClass.apply {
            method {
                name = "updateBackgroundForGroupState"
                emptyParam()
            }.hook().before {
                if (ConfigData.isEnableNotifyPanelAlpha) field { name = "mShowGroupBackgroundWhenExpanded" }.get(instance).setTrue()
            }
        }
        /** 替换媒体通知面板背景 - 设置媒体通知自动展开 */
        OplusMediaControlPanelClass?.apply {
            method {
                name = "bind"
                paramCount = 2
            }.hook().after {
                /** 得到当前实例 */
                val holder = OplusMediaControlPanelClass?.field {
                    name = "mViewHolder"
                    superClass(isOnlySuperClass = true)
                }?.get(instance)?.any()
                /** 记录媒体通知 [View] */
                notificationPlayerView = PlayerViewHolderClass?.method {
                    name = "getPlayer"
                    emptyParam()
                }?.get(holder)?.invoke()
                /** 设置背景着色 */
                modifyNotifyPanelAlpha(notificationPlayerView, isTint = true)
                /** 当前是否正在播放 */
                val isPlaying = MediaDataClass?.method {
                    name = "isPlaying"
                    emptyParam()
                }?.get(args().first().any())?.boolean() ?: false

                /** 当前通知是否展开 */
                val isExpanded = OplusMediaViewControllerClass?.method {
                    name = "getExpanded"
                    emptyParam()
                }?.get(field { name = "mOplusMediaViewController" }.get(instance).any())?.boolean() ?: false
                /** 符合条件后执行 */
                if (ConfigData.isEnableNotifyMediaPanelAutoExp.not() || isExpanded || isPlaying.not()) return@after
                /** 模拟手动展开通知 */
                BasePlayViewHolderClass?.method {
                    name = "getExpandButton"
                    emptyParam()
                }?.get(holder)?.invoke<View>()?.performClick()
            }
        }
        /** 替换通知图标和样式 */
        NotificationHeaderViewWrapperClass.apply {
            method {
                name { it == "resolveHeaderViews" || it == "onContentUpdated" }
            }.hookAll().after {
                field { name = "mIcon" }.get(instance).cast<ImageView>()?.apply {
                    ExpandableNotificationRowClass
                        .method { name = "getEntry" }
                        .get(NotificationViewWrapperClass.field {
                            name = "mRow"
                        }.get(instance).any()).call()?.let {
                            it.javaClass.method {
                                name = "getSbn"
                            }.get(it).invoke<StatusBarNotification>()
                        }.also { nf ->
                            nf?.notification?.also {
                                it.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                                    /** 执行替换 */
                                    fun doParse() {
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
                                    doParse()
                                    /** 延迟重新设置防止部分机型的系统重新设置图标出现图标着色后黑白块问题 */
                                    delayedRun(ms = 1500) { doParse() }
                                }
                            }
                        }
                }
            }
        }
    }
}
