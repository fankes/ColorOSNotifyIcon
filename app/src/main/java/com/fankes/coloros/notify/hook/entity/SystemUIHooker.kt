/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 2019-2022 Fankes Studio(qzmmcn@163.com)
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
import com.fankes.coloros.notify.data.DataConst
import com.fankes.coloros.notify.hook.HookConst.ANDROID_PACKAGE_NAME
import com.fankes.coloros.notify.hook.HookConst.SYSTEMUI_PACKAGE_NAME
import com.fankes.coloros.notify.hook.factory.isAppNotifyHookAllOf
import com.fankes.coloros.notify.hook.factory.isAppNotifyHookOf
import com.fankes.coloros.notify.param.IconPackParams
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
    private const val NotificationUtilsClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.NotificationUtils"

    /** 原生存在的类 */
    private const val NotificationEntryClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.collection.NotificationEntry"

    /** 原生存在的类 */
    private const val StatusBarIconClass = "com.android.internal.statusbar.StatusBarIcon"

    /** 原生存在的类 */
    private const val StatusBarIconViewClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.StatusBarIconView"

    /** 原生存在的类 */
    private const val IconBuilderClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.icon.IconBuilder"

    /** 原生存在的类 */
    private const val IconManagerClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.icon.IconManager"

    /** 原生存在的类 */
    private const val NotificationBackgroundViewClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.row.NotificationBackgroundView"

    /** 原生存在的类 */
    private const val PlayerViewHolderClass = "$SYSTEMUI_PACKAGE_NAME.media.PlayerViewHolder"

    /** 原生存在的类 */
    private const val MediaDataClass = "$SYSTEMUI_PACKAGE_NAME.media.MediaData"

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
        "com.oplusos.systemui.notification.util.RoundRectDrawableUtil\$Companion"
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
        "$SYSTEMUI_PACKAGE_NAME.statusbar.phone.StatusBarNotificationPresenter",
        "$SYSTEMUI_PACKAGE_NAME.statusbar.phone.StatusBar"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val ExpandableNotificationRowClass = VariousClass(
        "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.row.ExpandableNotificationRow",
        "$SYSTEMUI_PACKAGE_NAME.statusbar.ExpandableNotificationRow"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationViewWrapperClass = VariousClass(
        "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.row.wrapper.NotificationViewWrapper",
        "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.NotificationViewWrapper"
    )

    /** 根据多个版本存在不同的包名相同的类 */
    private val NotificationHeaderViewWrapperClass = VariousClass(
        "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper",
        "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.NotificationHeaderViewWrapper"
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
     * 是否启用通知图标优化功能
     * @param isHooking 是否判断启用通知功能 - 默认：是
     * @return [Boolean]
     */
    private fun isEnableHookColorNotifyIcon(isHooking: Boolean = true) =
        prefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX) && (if (isHooking) prefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX_NOTIFY) else true)

    /**
     * 判断通知是否来自系统推送
     * @return [Boolean]
     */
    private val StatusBarNotification.isOplusPush get() = opPkg == ANDROID_PACKAGE_NAME && opPkg != packageName

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
     * @param packageName APP 包名
     * @param isCustom 是否为通知优化生效图标
     * @param isGrayscale 是否为灰度图标
     */
    private fun printLogcat(
        tag: String,
        context: Context,
        packageName: String,
        isCustom: Boolean,
        isGrayscale: Boolean
    ) {
        if (prefs.get(DataConst.ENABLE_MODULE_LOG)) loggerD(
            msg = "$tag --> [${context.appNameOf(packageName)}][$packageName] " +
                    "custom [$isCustom] " +
                    "grayscale [$isGrayscale]"
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
        if (prefs.get(DataConst.ENABLE_COLOR_ICON_COMPAT).not()) safeOfFalse {
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
     * @return [Pair] - ([Drawable] 位图 or null,[Int] 颜色)
     */
    private fun compatCustomIcon(context: Context, isGrayscaleIcon: Boolean, packageName: String): Pair<Drawable?, Int> {
        var customPair: Pair<Drawable?, Int>? = null
        val statSysAdbIcon = runCatching {
            context.resources.drawableOf("com.android.internal.R\$drawable".toClass().field { name = "stat_sys_adb" }.get().int())
        }.getOrNull() ?: context.resources.drawableOf(R.drawable.ic_unsupported)
        when {
            /** 替换系统图标为 Android 默认 */
            (packageName == ANDROID_PACKAGE_NAME || packageName == SYSTEMUI_PACKAGE_NAME) && isGrayscaleIcon.not() ->
                customPair = Pair(statSysAdbIcon, 0)
            /** 替换自定义通知图标 */
            prefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX) -> run {
                iconDatas.takeIf { it.isNotEmpty() }?.forEach {
                    if (packageName == it.packageName && isAppNotifyHookOf(it)) {
                        if (isGrayscaleIcon.not() || isAppNotifyHookAllOf(it))
                            customPair = Pair(BitmapDrawable(context.resources, it.iconBitmap), it.iconColor)
                        return@run
                    }
                }
            }
        }
        return customPair ?: Pair(null, 0)
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
    ) = compatCustomIcon(context, isGrayscaleIcon, packageName).first.also {
        /** 打印日志 */
        printLogcat(tag = "StatusIcon", context, packageName, isCustom = it != null, isGrayscaleIcon)
    }?.let { Pair(it, true) } ?: Pair(if (isGrayscaleIcon) drawable else nf.compatPushingIcon(drawable), isGrayscaleIcon.not())

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
        compatCustomIcon(context, isGrayscaleIcon, packageName).also { customPair ->
            when {
                prefs.get(DataConst.ENABLE_NOTIFY_ICON_FORCE_APP_ICON) && isEnableHookColorNotifyIcon(isHooking = false) ->
                    iconView.apply {
                        /** 重新设置图标 */
                        setImageDrawable(appIcons[packageName] ?: context.appIconOf(packageName))
                        /** 设置默认样式 */
                        setDefaultNotifyIconViewStyle()
                    }
                customPair.first != null || isGrayscaleIcon -> iconView.apply {
                    /** 设置不要裁切到边界 */
                    clipToOutline = false
                    /** 重新设置图标 */
                    setImageDrawable(customPair.first ?: drawable)

                    /** 是否开启 Android 12 风格 */
                    val isA12Style = prefs.get(DataConst.ENABLE_ANDROID12_STYLE)

                    /** 旧版风格 */
                    val oldStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else 0xff707173).toInt()

                    /** 新版风格 */
                    val newStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else Color.WHITE).toInt()

                    /** 原生着色 */
                    val a12Style = if (isUpperOfAndroidS) context.systemAccentColor else
                        (if (context.isSystemInDarkMode) 0xff707173 else oldStyle).toInt()

                    /** 旧版图标着色 */
                    val oldApplyColor = customPair.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: oldStyle

                    /** 新版图标着色 */
                    val newApplyColor = customPair.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: a12Style

                    /** 判断风格并开始 Hook */
                    if (isA12Style) {
                        /** 通知图标边框圆角大小 */
                        background = DrawableBuilder()
                            .rectangle()
                            .cornerRadius(prefs.get(DataConst.NOTIFY_ICON_CORNER).dp(context))
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
            /** 打印日志 */
            printLogcat(tag = "NotifyIcon", iconView.context, packageName, isCustom = customPair.first != null, isGrayscaleIcon)
        }
    }

    /**
     * 设置通知面板背景透明度
     * @param view 背景 View 实例
     * @param drawable 背景实例
     * @param isTint 是否着色 [view]
     */
    private fun modifyNotifyPanelAlpha(view: View?, drawable: Drawable? = null, isTint: Boolean = false) {
        prefs.get(DataConst.ENABLE_NOTIFY_PANEL_ALPHA).also { isEnabled ->
            val currentAlpha = prefs.get(DataConst.NOTIFY_PANEL_ALPHA)
            val currendColor = if (view?.context?.isSystemInDarkMode == true) 0xFF404040.toInt() else 0xFFFAFAFA.toInt()
            /** 设置通知面板背景透明度 */
            when {
                isEnabled.not() -> {
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
            if (isEnabled) view?.elevation = 0f
        }
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
                if (isEnableHookColorNotifyIcon() && prefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX_AUTO))
                    IconAdaptationTool.prepareAutoUpdateIconRule(context, prefs.get(DataConst.NOTIFY_ICON_FIX_AUTO_TIME))
            }
            /** 注册发送适配新的 APP 图标通知监听 */
            registerReceiver(IntentFilter().apply {
                addDataScheme("package")
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
            }) { context, intent ->
                if (isEnableHookColorNotifyIcon().not()) return@registerReceiver
                if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED).not() &&
                    intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                ) return@registerReceiver
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
        IconPackParams(param = this).iconDatas.apply {
            when {
                isNotEmpty() -> forEach { iconDatas.add(it) }
                isEmpty() && isEnableHookColorNotifyIcon(isHooking = false) -> loggerW(msg = "NotifyIconSupportData is empty!")
            }
        }
    }

    /**
     * 刷新缓存数据
     * @param isRefreshCacheOnly 仅刷新缓存不刷新图标和通知改变 - 默认：否
     * @return [Boolean] 是否成功
     */
    private fun recachingPrefs(isRefreshCacheOnly: Boolean = false): Boolean {
        /** 必要的延迟防止 Sp 存储不刷新 */
        SystemClock.sleep(100)
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
                    if (prefs.get(DataConst.REMOVE_DEV_NOTIFY)) resultNull()
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
                    if (args().first().int() == 7 && prefs.get(DataConst.REMOVE_CHANGECP_NOTIFY)) resultNull()
                }
            }
        }
        /** 移除免打扰通知 */
        DndAlertHelperClass.hook {
            injectMember {
                method {
                    name = "sendNotificationWithEndtime"
                    param(LongType)
                }
                beforeHook {
                    /** 是否移除 */
                    if (prefs.get(DataConst.REMOVE_DNDALERT_NOTIFY)) resultNull()
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
                                            if (it.not()) context.appIconOf(nf.packageName)
                                                ?.also { e -> appIcons[nf.packageName] = e }
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
        }.by { isOldNotificationBackground.not() }
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
            }
        }.by { isOldNotificationBackground }
        /** 替换通知面板背景 - 避免折叠展开通知二次修改通知面板背景 */
        ExpandableNotificationRowClass.hook {
            injectMember {
                method {
                    name = "updateBackgroundForGroupState"
                    emptyParam()
                }
                beforeHook {
                    if (prefs.get(DataConst.ENABLE_NOTIFY_PANEL_ALPHA))
                        field { name = "mShowGroupBackgroundWhenExpanded" }.get(instance).setTrue()
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
                    if (prefs.get(DataConst.ENABLE_NOTIFY_MEDIA_PANEL_AUTO_EXP).not() || isExpanded || isPlaying.not()) return@afterHook
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
                method { name = "resolveHeaderViews" }
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