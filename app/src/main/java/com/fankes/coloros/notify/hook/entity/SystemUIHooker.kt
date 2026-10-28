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
 * This file is created by fankes on 2022/3/25.
 */
@file:Suppress("StaticFieldLeak", "ConstPropertyName")

package com.fankes.coloros.notify.hook.entity

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.session.MediaSessionManager
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.core.view.setPadding
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.const.PackageName
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.param.factory.isAppNotifyEnabledOf
import com.fankes.coloros.notify.param.factory.isAppNotifyOverlayOf
import com.fankes.coloros.notify.service.IconRuleUpdateService
import com.fankes.coloros.notify.utils.factory.appLastUpdateTimeOf
import com.fankes.coloros.notify.utils.factory.appNameOf
import com.fankes.coloros.notify.utils.factory.colorAlphaOf
import com.fankes.coloros.notify.utils.factory.delayedRun
import com.fankes.coloros.notify.utils.factory.isSystemInDarkMode
import com.fankes.coloros.notify.utils.factory.isUpperOfColorOS17
import com.fankes.coloros.notify.utils.factory.isWhiteColor
import com.fankes.coloros.notify.utils.factory.launcherActivitiesOf
import com.fankes.coloros.notify.utils.factory.launcherIconOf
import com.fankes.coloros.notify.utils.factory.runInSafe
import com.fankes.coloros.notify.utils.factory.safeOf
import com.fankes.coloros.notify.utils.factory.safeOfFalse
import com.fankes.coloros.notify.utils.factory.safeOfNull
import com.fankes.coloros.notify.utils.factory.stampToDate
import com.fankes.coloros.notify.utils.factory.systemAccentColor
import com.fankes.coloros.notify.utils.tool.ActivationPromptTool
import com.fankes.coloros.notify.utils.tool.BitmapCompatTool
import com.fankes.coloros.notify.utils.tool.IconAdaptationTool
import com.fankes.coloros.notify.utils.tool.IconRuleManagerTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.fankes.coloros.notify.wrapper.BuildConfigWrapper
import com.highcapable.anip.sdk.Anip
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.betterandroid.ui.extension.component.base.getDrawableCompat
import com.highcapable.betterandroid.ui.extension.component.base.toPx
import com.highcapable.betterandroid.ui.extension.view.outlineProvider
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.classOf
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.extension.kavaref.hook
import com.highcapable.yukihookapi.extension.kavaref.hookAll
import com.highcapable.yukihookapi.extension.kavaref.lazyClass
import com.highcapable.yukihookapi.extension.kavaref.lazyClassOrNull
import com.highcapable.yukihookapi.extension.kavaref.toClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.runtime.YukiHookRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.WeakHashMap
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

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
    private val ImageViewClass by lazyClass("android.widget.ImageView")

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

    /** 原生存在的类 */
    private val ViewConfigCoordinatorClass by lazyClassOrNull("${PackageName.SYSTEMUI}.statusbar.notification.collection.coordinator.ViewConfigCoordinator")

    /** 原生存在的类 - 旧版本不存在 */
    private val LegacyNotificationIconAreaControllerImpl by lazyClassOrNull("${PackageName.SYSTEMUI}.statusbar.phone.LegacyNotificationIconAreaControllerImpl")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusContrastColorUtilClass by lazyClassOrNull("com.oplusos.util.OplusContrastColorUtil")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationBackgroundViewClass by lazyClassOrNull("com.oplusos.systemui.statusbar.notification.row.OplusNotificationBackgroundView")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusMediaControlPanelClass by lazyClassOrNull("com.oplusos.systemui.media.OplusMediaControlPanel")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusMediaViewControllerClass by lazyClassOrNull("com.oplusos.systemui.media.OplusMediaViewController")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val BasePlayViewHolderClass by lazyClassOrNull("com.oplusos.systemui.media.base.BasePlayViewHolder")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationSmallIconUtilClass by lazyClassOrNull("com.oplus.systemui.statusbar.notification.util.OplusNotificationSmallIconUtil")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationHeaderViewWrapperExImpClass by lazyClassOrNull("com.oplus.systemui.statusbar.notification.row.wrapper.OplusNotificationHeaderViewWrapperExImp")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationGroupTemplateWrapperClass by lazyClassOrNull("com.oplus.systemui.notification.row.oplusgroup.OplusNotificationGroupTemplateWrapper")

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationGroupExtImplClass by lazyClassOrNull("com.oplus.systemui.notification.row.oplusgroup.OplusNotificationGroupExtImpl")

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
            $$"com.oplusos.systemui.notification.util.RoundRectDrawableUtil$Companion",
            $$"com.coloros.systemui.notification.util.RoundRectDrawableUtil$Companion"
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

    /** ColorOS 16+ 存在的类 - 息屏时钟的通知图标布局 */
    private val AodNotificationLayoutClass by lazyClassOrNull("com.oplus.systemui.aod.aodclock.off.notification.NotificationLayout")

    /** ColorOS 16+ 存在的类 - 息屏插件 */
    private val AodPluginCallImplClass by lazyClassOrNull("com.oplus.systemui.aod.plugin.AodPluginCallImpl")

    /** ColorOS 16+ 存在的类 - 锁屏通知分发器 */
    private val LockScreenNotificationDispatcherClass by lazyClassOrNull("com.android.systemui.statusbar.LockScreenNotificationDispatcher")

    /** ColorOS 16+ 存在的类 - 系统依赖注入入口 */
    private val DependencyExClass by lazyClassOrNull("com.android.systemui.DependencyEx")

    /** ColorOS 16+ 存在的类 - 全屏应用通知轻反馈 */
    private val FullScreenBannerHelperClass by lazyClassOrNull("com.oplus.systemui.notification.interruption.fullscreenbanner.FullScreenBannerHelper")

    /** ColorOS 16+ 存在的类 - 需要合成头像与应用图标角标的应用名单 */
    private val NotificationIconBadgePackagesClass by lazyClassOrNull("com.oplus.systemui.notification.NotificationIconBadgePackages")

    /** ColorOS 16+ 存在的类 - 头像与应用图标角标的合成图标 */
    private val DrawableWithBadgeClass by lazyClassOrNull("com.oplus.systemui.notification.utlils.DrawableWithBadge")

    /** ColorOS 16+ 存在的类 - 锁屏岛 (锁屏胶囊) 图标数据 */
    private val CapsuleNotificationIconDataClass by lazyClassOrNull(
        $$"com.oplus.systemui.notification.lockscreen.notification.CapsuleNotificationDataController$NotificationIconData"
    )

    /** 系统配置变化分发器 */
    private val ConfigurationControllerClass by lazyClassOrNull("${PackageName.SYSTEMUI}.statusbar.policy.ConfigurationController")

    /** ColorOS 进程应用新系统配置时的通用注入类 */
    private val OplusCommonInjectorClass by lazyClassOrNull("oplus.android.OplusCommonInjector")

    /** 根据多个版本存在不同的方法相同的类 */
    private val StatusBarIconControllerClass by lazyClass(
        VariousClass(
            "${PackageName.SYSTEMUI}.statusbar.StatusBarIconView",
            "com.oplus.systemui.statusbar.phone.StatusBarIconControllerExImpl"
        )
    )

    /** SystemUI 进程上下文 */
    @SuppressLint("StaticFieldLeak")
    private var globalContext: Context? = null

    /** SystemUI 进程内用于恢复和更新 ANIP 快照的协程作用域。 */
    private val iconRuleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** 当前运行时是否提供模块共享文件，不提供时回退为 SystemUI 自行获取资源 */
    private val isSharedCacheAvailable get() = YukiHook.Status.runtime == YukiHookRuntime.LIBXPOSED

    /** 状态栏通知图标容器 */
    @SuppressLint("StaticFieldLeak")
    private var notificationIconContainer: ViewGroup? = null

    /** 状态栏通知图标数组 */
    private var notificationIconInstances = ArrayList<View>()

    /** 媒体会话管理器 */
    private var mediaSessionManager: MediaSessionManager? = null

    /** 媒体通知 [View] */
    @SuppressLint("StaticFieldLeak")
    private var notificationPlayerView: View? = null

    /** 通知栏通知控制器 */
    private var notificationPresenter: Any? = null

    /** 通知图标管理器 - 状态栏、息屏等位置的通知图标由它创建 */
    private var iconManager: Any? = null

    /** 通知面板默认背景着色 [ColorStateList] */
    private var defaultNotifyPanelTintList: ColorStateList? = null

    /** 通知面板默认背景阴影效果强度 */
    private var defaultNotifyPanelElevation = -1f

    /** 仅监听一次主题壁纸颜色变化 */
    private var isWallpaperColorListenerSetUp = false

    /** 已注册的主题壁纸颜色监听，模块实例释放时注销 */
    private var wallpaperColorsListener: Pair<WallpaperManager, WallpaperManager.OnColorsChangedListener>? = null

    /** 当前进程是否已加载此 Hooker */
    private var isHooked = false

    /** 当前模块实例加载时模块 APK 的最后更新时间 */
    private var moduleLastUpdateTime: Long? = null

    /** 是否已经使用过缓存刷新功能 */
    private var isUsingCachingMethod = false

    /** 上次自动更新 ANIP 资源的日期 */
    private var lastAutoUpdateIconRuleDate = ""

    /** 模块定时更新服务未响应时的兜底提醒 */
    private var iconRuleUpdateFallbackJob: Job? = null

    /** 记录已处理过的图标 [ImageView] - 值 true 表示本模块接管，false 表示交还系统；用于拦截判断与避免重复处理 */
    private val moduleStyledIcons = WeakHashMap<ImageView, Boolean>()

    /** 模块接管后的图标 [Drawable] - 系统再次设置图标时强制换回 */
    private val moduleStyledDrawables = WeakHashMap<ImageView, Drawable>()

    /**
     * 通知面板图标内边距比例 (MD3 风格) - 内边距占图标视图尺寸的比例 (0f~0.5f)
     *
     * 按视图尺寸而非绝对 dp 计算，折叠与展开的图标视图大小不同，等比内边距才能保证缩小比例一致；
     * 数值越大图标越小，想进一步缩小图标改这里即可
     */
    private const val NOTIFY_ICON_PADDING_RATIO_MD3 = 0.17f

    /** 通知面板图标内边距比例 (经典风格) - 含义同 [NOTIFY_ICON_PADDING_RATIO_MD3] */
    private const val NOTIFY_ICON_PADDING_RATIO_CLASSIC = 0.04f

    /** 等待模块定时更新服务响应的最长时间 */
    private const val ICON_RULE_UPDATE_TIMEOUT = 60_000L

    /** 系统主题变化的配置标志位 - 同 `OplusExtraConfiguration.THEME_OLD_SKIN_CHANGED` */
    private const val THEME_CHANGED = 0x8000000

    /** 桌面图标样式或图标包变化的配置标志位 - 同 `OplusExtraConfiguration.UX_ICON_CONFIG_CHANGED` */
    private const val UX_ICON_CONFIG_CHANGED = 0x800000

    /**
     * 判断通知是否来自系统推送
     * @return [Boolean]
     */
    private val StatusBarNotification.isOplusPush get() = opPkg == PackageName.SYSTEM_FRAMEWORK && opPkg != packageName

    /**
     * 判断通知是否由系统生成 (ColorOS 16.1折叠通知)
     * @return [Boolean]
     */
    private fun isCollapseNotification(nfPackageName: String, contextPackageName: String): Boolean =
        contextPackageName == PackageName.SYSTEMUI && nfPackageName != contextPackageName

    /**
     * 判断通知背景是否为旧版本
     * @return [Boolean]
     */
    private val isOldNotificationBackground
        get() = NotificationBackgroundViewClass?.resolve()?.optional(silent = true)
            ?.firstMethodOrNull {
                name = "drawCustom"
                parameterCount = 2
            } != null

    /**
     * 判断通知是否为新版本
     * @return [Boolean]
     */
    private val isNewNotification
        get() = OplusNotificationHeaderViewWrapperExImpClass?.resolve()?.optional(silent = true)
            ?.firstMethodOrNull {
                name = "proxyOnContentUpdated"
                parameterCount = 1
            } != null

    private val isNotificationPresenter
        get() = StatusBarNotificationPresenterClass.resolve().optional(silent = true)
            .firstMethodOrNull {
                name = "updateNotificationsOnDensityOrFontScaleChanged"
                emptyParameters()
            } != null

    /**
     * 打印日志
     * @param tag 标识
     * @param context 实例
     * @param nf 通知实例
     * @param isCustom 是否为通知优化生效图标
     * @param isGrayscale 是否为灰度图标
     */
    private fun loggerDebug(
        tag: String,
        context: Context,
        nf: StatusBarNotification?,
        isCustom: Boolean,
        isGrayscale: Boolean,
        contextPackageName: String,
        iconColor: Int
    ) {
        if (ConfigData.isEnableModuleLog) YLog.debug(
            msg = "(Processing $tag) ↓\n" +
                "[Title]: ${nf?.notification?.extras?.getString(Notification.EXTRA_TITLE)}\n" +
                "[Content]: ${nf?.notification?.extras?.getString(Notification.EXTRA_TEXT)}\n" +
                "[App Name]: ${context.appNameOf(packageName = nf?.packageName ?: "")}\n" +
                "[Package Name]: ${nf?.packageName}\n" +
                "[Sender Package Name]: ${nf?.opPkg}\n" +
                "[Context Package Name]: $contextPackageName\n" +
                "[Custom Icon]: $isCustom\n" +
                "[Grayscale Icon]: $isGrayscale\n" +
                "[From System Push]: ${nf?.isOplusPush}\n" +
                "[String]: ${nf?.notification}\n" +
                "[Color]: $iconColor"
        )
    }

    /**
     * 注册主题壁纸改变颜色监听
     *
     *  - 仅限在 Android 12 上注册
     * @param view 实例
     */
    private fun registerWallpaperColorChanged(view: View) = runInSafe {
        if (isWallpaperColorListenerSetUp.not() && AndroidVersion.isGreaterThan(AndroidVersion.R)) view.apply {
            val manager = WallpaperManager.getInstance(context)
            val listener = WallpaperManager.OnColorsChangedListener { _, _ -> refreshNotificationIcons() }
            manager.addOnColorsChangedListener(listener, handler)
            wallpaperColorsListener = manager to listener
        }
        isWallpaperColorListenerSetUp = true
    }

    /** 刷新状态栏小图标 */
    private fun refreshStatusBarIcons() = runInSafe {
        // 新版本的状态栏图标由 IconManager 创建，交给它重新创建
        if (isNewNotification) {
            refreshEntryIcons()
            return@runInSafe
        }
        val nfField = StatusBarIconViewClass.resolve().optional().firstFieldOrNull { name = "mNotification" }
        val sRadiusField = StatusBarIconViewClass.resolve().optional(silent = true).firstFieldOrNull {
            name = "sIconRadiusFraction"
        } ?: StatusBarIconControllerClass.resolve().optional(silent = true).firstFieldOrNull { name = "sIconRadiusFraction" }
        val sNfSizeField = StatusBarIconViewClass.resolve().optional(silent = true).firstFieldOrNull {
            name = "sNotificationRoundIconSize"
        } ?: StatusBarIconControllerClass.resolve().optional(silent = true).firstFieldOrNull { name = "sNotificationRoundIconSize" }
        val roundUtil = RoundRectDrawableUtil_CompanionClass.resolve().optional(silent = true).firstMethodOrNull {
            name = "getRoundRectDrawable"
            parameters(Drawable::class, Float::class, Int::class, Int::class, Context::class)
        }.apply {
            if (this == null) YLog.error("Your system not support \"getRoundRectDrawable\"!")
        }?.of(RoundRectDrawableUtilClass.resolve().optional().firstFieldOrNull { name = "Companion" }?.get())
        // 启动一个线程防止卡顿
        thread {
            (notificationIconContainer?.children?.toList() ?: notificationIconInstances.takeIf { it.isNotEmpty() })?.forEach {
                runInSafe {
                    // 得到通知实例
                    val nf = nfField?.of(it)?.get<StatusBarNotification>() ?: return@thread

                    // 得到原始通知图标
                    val iconDrawable = nf.notification.smallIcon.loadDrawable(it.context)
                        ?: return@thread YLog.warn("refreshStatusBarIcons got null smallIcon")
                    // 获取优化后的状态栏通知图标
                    compatStatusIcon(
                        context = it.context,
                        nf = nf,
                        isGrayscaleIcon = isGrayscaleIcon(it.context, iconDrawable),
                        packageName = nf.packageName,
                        drawable = iconDrawable
                    ).also { pair ->
                        // 得到图标圆角
                        val sRadius = sRadiusField?.of(it)?.get<Float>()

                        // 得到缩放大小
                        val sNfSize = sNfSizeField?.of(it)?.get<Int>()

                        // 在主线程设置图标
                        it.post {
                            val drawable = roundUtil?.invokeQuietly<Drawable>(pair.first, sRadius, sNfSize, sNfSize, it.context)
                            (it as? ImageView?)?.setImageDrawable(drawable)
                        }
                    }
                }
            }
        }
    }

    /** 刷新通知小图标 */
    private fun refreshNotificationIcons() = runInSafe {
        notificationPresenter?.asResolver()?.optional()?.firstMethodOrNull {
            name = "updateNotificationsOnDensityOrFontScaleChanged"
            emptyParameters()
        }?.invoke()
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
            ContrastColorUtilClass.resolve()
                .optional(silent = true)
                .let {
                    it.firstMethodOrNull {
                        name = "isGrayscaleIcon"
                        parameters(Drawable::class)
                    }?.of(
                        it.firstMethodOrNull {
                            name = "getInstance"
                            parameters(Context::class)
                        }?.invoke(context)
                    )?.invokeQuietly<Boolean>(drawable) == true
                }
        } else BitmapCompatTool.isGrayscaleDrawable(drawable)

    /**
     * 取 APP 当前在桌面上显示的图标 - 系统主题生效时为主题图标
     *
     * 统一使用 SystemUI 自身的上下文获取，ColorOS 只给白名单内的调用方返回主题图标，
     * 通知视图的上下文可能是通知所属 APP 的上下文
     * @param context 实例
     * @param packageName APP 包名
     * @return [Drawable] or null
     */
    private fun hostLauncherIconOf(context: Context, packageName: String) = (globalContext ?: context).launcherIconOf(packageName)

    /**
     * 判断通知小图标是否就是 APP 的桌面图标
     *
     * 只有资源图标能确认来源：对比资源 ID 与桌面启动 Activity、APP 声明的图标 (含圆形图标)，
     * 位图图标无法确认，一律视为不是
     * @param context 实例
     * @return [Boolean]
     */
    private fun StatusBarNotification.isLauncherSmallIcon(context: Context) = safeOfFalse {
        val icon = notification.smallIcon ?: return@safeOfFalse false
        if (icon.type != Icon.TYPE_RESOURCE || icon.resPackage != packageName) return@safeOfFalse false
        val activities = (globalContext ?: context).launcherActivitiesOf(packageName)
        val appInfo = activities.firstOrNull()?.applicationInfo ?: return@safeOfFalse false
        // iconRes、roundIconRes 为隐藏字段，分别是 Manifest 中声明的 icon 与 roundIcon
        val hiddenResIds = listOf("iconRes", "roundIconRes").mapNotNull { name ->
            appInfo.asResolver().optional(silent = true).firstFieldOrNull { this.name = name }?.getQuietly<Int>()
        }
        val resIds = activities.map { it.iconResource } + appInfo.icon + hiddenResIds
        resIds.any { it != 0 && it == icon.resId }
    }

    /**
     * 将系统推送和以桌面图标作为小图标的通知替换为 APP 当前的桌面图标，使其跟随系统主题
     *
     * 系统推送本身没有图标，其余无法确认来源的彩色图标保持原样
     * @param context 实例
     * @param iconDrawable 原始图标
     * @return [Drawable] 适配的图标
     */
    private fun StatusBarNotification.compatLauncherIcon(context: Context, iconDrawable: Drawable) = safeOf(iconDrawable) {
        if (isOplusPush || isLauncherSmallIcon(context))
            hostLauncherIconOf(context, packageName) ?: iconDrawable
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
        var customPair: Triple<Drawable?, Int, Boolean>? = null
        val statSysAdbIcon = runCatching {
            val resId = $$"com.android.internal.R$drawable".toClass()
                .resolve()
                .firstField { name = "stat_sys_adb" }
                .get<Int>() ?: error("Resource not found")
            context.resources.getDrawableCompat(resId)
        }.getOrNull() ?: module.resources.getDrawableCompat(R.drawable.ic_unsupported)
        when {
            // 替换系统图标为 Android 默认
            (packageName == PackageName.SYSTEM_FRAMEWORK || packageName == PackageName.SYSTEMUI) && isGrayscaleIcon.not() ->
                customPair = Triple(statSysAdbIcon, 0, false)
            // 替换自定义通知图标
            ConfigData.isEnableNotifyIconFix -> run {
                val snapshot = IconRuleManagerTool.snapshot
                val icon = snapshot?.getIcon(packageName)
                if (icon != null) {
                    if (isAppNotifyEnabledOf(icon) && (isGrayscaleIcon.not() || isAppNotifyOverlayOf(icon)))
                        customPair = Triple(
                            snapshot.getBitmap(icon)?.toDrawable(context.resources),
                            icon.color ?: 0,
                            false
                        )
                    return@run
                }
                if (isGrayscaleIcon.not() && ConfigData.isEnableNotifyIconFixPlaceholder)
                    customPair = Triple(module.resources.getDrawableCompat(R.drawable.ic_message), 0, true)
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
        // 打印日志
        loggerDebug(tag = "Status Bar Icon",
            context,
            nf,
            isCustom = it.first != null && it.third.not(),
            isGrayscaleIcon,
            context.packageName,
            nf.notification.color)
        it.first?.let { e -> Pair(e, true) } ?: Pair(if (isGrayscaleIcon) drawable else nf.compatLauncherIcon(context, drawable), isGrayscaleIcon.not())
    }

    /**
     * 创建通知栏自绘图标 - 背景与着色图标都由自绘 [Drawable] 承载
     * @param context 实例
     * @param customTriple [compatCustomIcon] 的结果
     * @param drawable 原始图标
     * @param iconColor 原生图标颜色
     * @param viewSizePx 图标视图尺寸 (px) - 未知时为 0
     * @return [Drawable]
     */
    private fun customNotifyIconOf(
        context: Context,
        customTriple: Triple<Drawable?, Int, Boolean>,
        drawable: Drawable,
        iconColor: Int,
        viewSizePx: Int
    ): Drawable {
        val isMd3 = ConfigData.isEnableMd3NotifyIconStyle

        // 图标相对背景的留白比例，折叠/展开等比一致
        val padRatio = if (isMd3) NOTIFY_ICON_PADDING_RATIO_MD3 else NOTIFY_ICON_PADDING_RATIO_CLASSIC

        // 图标实际显示尺寸 - 位图直接优化到该尺寸，避免二次缩放产生毛边
        val contentPx = if (viewSizePx > 0) (viewSizePx * (1f - padRatio * 2)).toInt().coerceAtLeast(1) else 0

        // 优化 (锐化) 后的单色图标
        val glyph = (customTriple.first ?: drawable).let { d ->
            (d as? BitmapDrawable)?.bitmap
                ?.let { BitmapCompatTool.optimizeForSize(it, contentPx).toDrawable(context.resources) } ?: d
        }

        // 旧版风格 - ColorOS 17+ 通知面板为液态玻璃背景，浅色模式下的深色图标看不清，始终使用夜间模式颜色
        val oldStyle = if (isUpperOfColorOS17 || context.isSystemInDarkMode) 0xFFDCDCDC.toInt() else 0xFF707173.toInt()

        // 原生着色
        val md3Style = if (AndroidVersion.isGreaterThan(AndroidVersion.R)) context.systemAccentColor else
            (if (context.isSystemInDarkMode) 0xFF707173.toInt() else oldStyle)

        // 原生通知图标颜色
        val nativeIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor) 0 else iconColor

        // 自定义通知图标颜色
        val customIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor) 0 else customTriple.second

        // 旧版图标着色 - 浅色面板上白色 / 浅色字会看不见，按面板明暗回退
        val oldApplyColor = customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: oldStyle
        val oldReadableColor = if (oldApplyColor.isWhiteColor && context.isSystemInDarkMode.not()) oldStyle else oldApplyColor

        // 新版图标 (背景) 着色 - 颜色透明时兜底，避免只有白色图标没有背景
        val newApplyColor = (customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: md3Style)
            .takeIf { Color.alpha(it) != 0 } ?: md3Style.takeIf { Color.alpha(it) != 0 } ?: 0xFF707173.toInt()

        // 用自绘 [CustomIconDrawable] 承载 “背景 + 着色图标”，其内部把 setColorFilter/setTint 覆盖为重申自身颜色，
        // 背景也画在图标自身而非视图 background
        return if (isMd3) CustomIconDrawable(
            glyph = glyph,
            badgeColor = newApplyColor,
            glyphColor = if (newApplyColor.isWhiteColor) Color.BLACK else Color.WHITE,
            cornerRadiusPx = ConfigData.notifyIconCornerSize.toPx(context).toFloat(),
            paddingRatio = padRatio,
            referenceSidePx = if (viewSizePx > 0) viewSizePx.toFloat() else headerIconSizeOf(context).toFloat()
        ) else CustomIconDrawable(
            glyph = glyph,
            badgeColor = Color.TRANSPARENT,
            glyphColor = oldReadableColor,
            cornerRadiusPx = 0f,
            paddingRatio = padRatio
        )
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
        // 系统已将头像和应用图标合成，此处只替换角标，不覆盖整个图标
        if (DrawableWithBadgeClass?.isInstance(iconView.drawable) == true) {
            moduleStyledIcons[iconView] = false
            return@runInSafe
        }
        val realPackageName = if (nf.isOplusPush || isCollapseNotification(nf.packageName, packageName)) nf.packageName else packageName
        compatCustomIcon(context, isGrayscaleIcon, realPackageName).also { customTriple ->
            when {
                ConfigData.isEnableNotifyIconForceAppIcon -> iconView.apply {
                    // 标记为已处理但非本模块接管
                    moduleStyledIcons[this] = false
                    // 重新设置图标
                    setImageDrawable(hostLauncherIconOf(context, realPackageName))
                    // 设置默认样式
                    setDefaultNotifyIconViewStyle()
                }
                (customTriple.first != null && customTriple.third.not()) || isGrayscaleIcon -> iconView.apply {
                    // 标记为本模块接管（兜底判断/防止重复处理用）
                    moduleStyledIcons[this] = true
                    // 背景与图标都交由自绘 Drawable 处理：清空视图层裁切/背景/内边距/着色，并用 FIT_XY 让 Drawable 充满视图
                    clipToOutline = false
                    scaleType = ImageView.ScaleType.FIT_XY
                    background = null
                    setPadding(0)
                    clearColorFilter()
                    // 图标视图尺寸
                    val viewSizePx = layoutParams?.width?.takeIf { it > 0 } ?: width.takeIf { it > 0 } ?: 0
                    val styled = customNotifyIconOf(context, customTriple, drawable, iconColor, viewSizePx)
                    moduleStyledDrawables[this] = styled
                    setImageDrawable(styled)
                    // ColorOS 16 会在布局/颜色刷新后把系统图标重新设回来，下一帧检查并补一次
                    post {
                        if (drawable !== styled) {
                            YLog.debug("Icon drawable was replaced by system, re-applying: $realPackageName")
                            setImageDrawable(styled)
                        }
                    }
                }
                else -> iconView.apply {
                    // 标记为已处理但非本模块接管
                    moduleStyledIcons[this] = false
                    // 重新设置图标
                    setImageDrawable(nf.compatLauncherIcon(context, drawable))
                    // 设置默认样式
                    setDefaultNotifyIconViewStyle()
                }
            }
            // 是否为通知优化生效图标
            val isCustom = customTriple.first != null && customTriple.third.not()
            // 打印日志
            loggerDebug(tag = "Notification Panel Icon",
                iconView.context,
                nf,
                isCustom = isCustom,
                isGrayscaleIcon,
                realPackageName,
                customTriple.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: 0)
        }
    }

    /**
     * 自动适配锁屏岛 (锁屏胶囊) 图标
     *
     * 处理方式与 [compatNotifyIcon] 保持一致
     * @param args 锁屏岛图标数据的构造方法参数
     * @return [Drawable] or null - 为 null 时交还系统处理
     */
    private fun compatCapsuleIcon(args: Array<Any?>) = safeOfNull {
        val context = globalContext ?: return@safeOfNull null
        val smallIcon = (args[3] as? Icon)?.loadDrawable(context) ?: return@safeOfNull null
        val nf = statusBarNotificationOfEntry(args[6]) ?: return@safeOfNull null
        compatSmallIconDrawable(context, nf, smallIcon, capsuleIconSizeOf(context))
    }

    /**
     * 自动适配全屏应用通知轻反馈图标
     *
     * 只替换应用图标，头像保持不变：有头像时 [builder] 的图标是头像的角标，没有头像时就是主图标
     * @param context 实例
     * @param entry 通知实体
     * @param builder 系统生成的 `FullScreenBanner.Builder`
     */
    private fun compatBannerBuilder(context: Context?, entry: Any?, builder: Any?) = runInSafe {
        val nf = statusBarNotificationOfEntry(entry) ?: return@runInSafe
        val realContext = globalContext ?: context ?: return@runInSafe
        val smallIcon = nf.notification.smallIcon?.loadDrawable(realContext) ?: return@runInSafe
        val icon = compatSmallIconDrawable(realContext, nf, smallIcon, sizePx = 0) ?: return@runInSafe
        val resolver = builder?.asResolver()?.optional(silent = true) ?: return@runInSafe
        resolver.firstFieldOrNull { name = "mIcon" }?.setQuietly(icon)
        resolver.firstFieldOrNull { name = "mLargeIconWithBadge" }?.getQuietly<Drawable>()
            ?.takeIf { DrawableWithBadgeClass?.isInstance(it) == true }
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                name = "setBadge"
                parameterCount = 2
            }?.invokeQuietly(icon, null)
    }

    /**
     * 自动适配息屏通知图标
     *
     * 系统在把图标交给息屏插件之前，会从锁屏通知分发器取出图标数据列表，此处直接替换其中的图标
     */
    private fun compatAodIconData() = runInSafe {
        val context = globalContext ?: return@runInSafe
        val dispatcher = DependencyExClass?.resolve()?.optional(silent = true)
            ?.firstFieldOrNull { name = "sDependency" }?.getQuietly<Any>()
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                name = "getDependency"
                parameters(Class::class)
            }?.invokeQuietly<Any>(LockScreenNotificationDispatcherClass)?.asResolver()?.optional(silent = true)
            ?: return@runInSafe
        val iconDataList = dispatcher.firstMethodOrNull { name = "getLockScreenNotificationIconData" }?.invokeQuietly<Any>()
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull { name = "getValue" }?.invokeQuietly<List<*>>()
            ?: return@runInSafe
        val entries = dispatcher.firstMethodOrNull { name = "getNotificationListOnKg" }?.invokeQuietly<List<*>>()
            ?: return@runInSafe
        iconDataList.forEach { iconData ->
            val data = iconData?.asResolver()?.optional(silent = true) ?: return@forEach
            val key = data.firstMethodOrNull { name = "getKey" }?.invokeQuietly<String>() ?: return@forEach
            val nf = statusBarNotificationOfKey(entries, key) ?: return@forEach
            val bitmap = compatAodBitmap(context, nf) ?: return@forEach
            data.firstMethodOrNull { name = "setIcon" }?.invokeQuietly(bitmap.toDrawable(context.resources))
        }
    }

    /**
     * 自动适配息屏通知图标并转换为位图，息屏的各个位置共用
     * @param context 实例
     * @param nf 通知实例
     * @return [Bitmap] or null - 为 null 时交还系统处理
     */
    private fun compatAodBitmap(context: Context, nf: StatusBarNotification) = safeOfNull {
        val smallIcon = nf.notification.smallIcon?.loadDrawable(context) ?: return@safeOfNull null
        compatSmallIconDrawable(context, nf, smallIcon, sizePx = 0)?.toBitmap()
    }

    /**
     * 按息屏图标数据的 key 从锁屏通知列表里取通知实例
     *
     * key 由 “包名 + uid + 通知 ID” 拼接，被折叠成组的通知取不到同 ID 的实例时按 “包名 + uid” 匹配
     * @param entries 锁屏通知列表 (`PipelineEntry`)
     * @param key 息屏图标数据的 key
     * @return [StatusBarNotification] or null
     */
    private fun statusBarNotificationOfKey(entries: List<*>, key: String): StatusBarNotification? {
        val notifications = entries.mapNotNull { entry ->
            statusBarNotificationOfEntry(
                entry?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                    name = "getRepresentativeEntry"
                }?.invokeQuietly<Any>()
            )
        }
        return notifications.firstOrNull { "${it.packageName}${it.uid}${it.id}" == key }
            ?: notifications.firstOrNull { key.startsWith("${it.packageName}${it.uid}") }
    }

    /**
     * 取通知实体的通知实例
     * @param entry `NotificationEntry` 实例
     * @return [StatusBarNotification] or null
     */
    private fun statusBarNotificationOfEntry(entry: Any?) =
        NotificationEntryClass.resolve().optional(silent = true).firstMethodOrNull {
            name = "getSbn"
        }?.of(entry)?.invokeQuietly<StatusBarNotification>()

    /**
     * 自动适配通知头像右下角的应用图标角标
     *
     * 系统会把头像和应用图标合成为一个 [DrawableWithBadgeClass]，此处只替换其中的角标，头像保持不变
     * @param headerWrapperExImp [OplusNotificationHeaderViewWrapperExImpClass] 实例
     * @param drawable 系统合成后的图标
     */
    private fun compatHeaderBadge(headerWrapperExImp: Any?, drawable: Drawable?) = runInSafe {
        if (DrawableWithBadgeClass?.isInstance(drawable) != true) return@runInSafe
        val (base, iconView) = headerIconOfEx(headerWrapperExImp)
        val nf = statusBarNotificationOf(base) ?: return@runInSafe
        val context = iconView?.context ?: return@runInSafe
        val smallIcon = nf.notification.smallIcon?.loadDrawable(context) ?: return@runInSafe
        val badge = compatSmallIconDrawable(context, nf, smallIcon, sizePx = 0) ?: return@runInSafe
        drawable?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
            name = "setBadge"
            parameterCount = 2
        }?.invokeQuietly(badge, null)
    }

    /**
     * 自动适配通知小图标 - 通知栏、锁屏岛、头像角标等需要小图标的位置共用
     *
     * 处理方式与 [compatNotifyIcon] 保持一致
     * @param context 实例
     * @param nf 通知实例
     * @param smallIcon 原始小图标
     * @param sizePx 图标视图尺寸 (px) - 未知时为 0
     * @return [Drawable] or null - 为 null 时交还系统处理
     */
    private fun compatSmallIconDrawable(context: Context, nf: StatusBarNotification, smallIcon: Drawable, sizePx: Int): Drawable? {
        if (isMediaNotification(context, nf.notification, context.packageName)) return null
        val isGrayscaleIcon = isGrayscaleIcon(context, smallIcon)
        val customTriple = compatCustomIcon(context, isGrayscaleIcon, nf.packageName)
        return when {
            ConfigData.isEnableNotifyIconForceAppIcon -> hostLauncherIconOf(context, nf.packageName)
            (customTriple.first != null && customTriple.third.not()) || isGrayscaleIcon ->
                customNotifyIconOf(context, customTriple, smallIcon, nf.notification.color, sizePx)
            nf.isOplusPush || nf.isLauncherSmallIcon(context) -> hostLauncherIconOf(context, nf.packageName)
            else -> null
        }
    }

    /**
     * 把替换用的图标缩放到状态栏图标尺寸
     *
     * ColorOS 16 起状态栏图标由 IconManager 创建，旧的重绘路径不再执行，
     * 位图的固有尺寸（ANIP 72px / 桌面图标 108dp）会直接参与系统缩放
     * @param context 实例
     * @return [Bitmap]
     */
    @Suppress("DiscouragedApi")
    private fun Drawable.toStatusBarBitmap(context: Context): Bitmap {
        val res = context.resources
        fun dimenOf(name: String, pkg: String) =
            res.getIdentifier(name, "dimen", pkg).takeIf { it != 0 }?.let { res.getDimensionPixelSize(it) } ?: 0
        // ColorOS 16 的状态栏槽位是 20dp（不是 AOSP 的 22dp），且系统按 0.75 缩放应用图标，
        // 蒙版直接铺满槽位会比系统图标大一圈，这里按槽位的 0.8 倍出图
        val slot = dimenOf("status_bar_icon_size", "com.android.systemui").takeIf { it > 0 }
            ?: dimenOf("status_bar_icon_size", "android").takeIf { it > 0 }
            ?: (20 * res.displayMetrics.density).toInt()
        val target = (slot * 0.8f).toInt().coerceAtLeast(1)
        val bitmap = toBitmap()
        return if (bitmap.width != target || bitmap.height != target)
            Bitmap.createScaledBitmap(bitmap, target, target, true) else bitmap
    }

    /**
     * 取通知面板头部图标尺寸 - 圆角设置针对的图标大小
     * @param context 实例
     * @return [Int] 图标尺寸 (px)
     */
    @Suppress("DiscouragedApi")
    private fun headerIconSizeOf(context: Context) =
        context.resources.getIdentifier("notification_header_icon_size", "dimen", context.packageName)
            .takeIf { it != 0 }?.let { context.resources.getDimensionPixelSize(it) } ?: 40.toPx(context)

    /**
     * 取锁屏岛图标尺寸
     * @param context 实例
     * @return [Int] 图标尺寸 (px) - 获取失败为 0
     */
    @Suppress("DiscouragedApi")
    private fun capsuleIconSizeOf(context: Context) =
        context.resources.getIdentifier("capsule_icon_size", "dimen", context.packageName)
            .takeIf { it != 0 }?.let { context.resources.getDimensionPixelSize(it) } ?: 0

    /**
     * 取通知头部包装器的图标 EX [ImageView]
     * @param headerWrapperExImp [OplusNotificationHeaderViewWrapperExImpClass] 实例
     * @return [Pair]<[Any] or null, [ImageView] or null>
     */
    private fun headerIconOfEx(headerWrapperExImp: Any?): Pair<Any?, ImageView?> {
        val base = headerWrapperExImp?.asResolver()
            ?.optional(silent = true)
            ?.firstMethodOrNull {
                name = "getBase"
                superclass()
            }?.invokeQuietly()
        val iconView = base?.asResolver()
            ?.optional(silent = true)
            ?.firstFieldOrNull {
                name = "mIcon"
                superclass()
            }?.getQuietly<ImageView>()
        return base to iconView
    }

    /**
     * 判断通知头部是否由系统合成头像与应用图标角标
     *
     * 名单内的应用带有头像大图标时，系统会把头像和应用图标合成为头部图标
     * @param headerWrapperExImp [OplusNotificationHeaderViewWrapperExImpClass] 实例
     * @return [Boolean]
     */
    private fun isHeaderBadgeComposite(headerWrapperExImp: Any?) = safeOfFalse {
        val hasAvatar = headerWrapperExImp?.asResolver()?.optional(silent = true)?.firstFieldOrNull {
            name = "mRightIcon"
        }?.getQuietly<ImageView>()?.drawable != null
        val packageName = statusBarNotificationOf(headerIconOfEx(headerWrapperExImp).first)?.packageName
        if (hasAvatar.not() || packageName == null) return@safeOfFalse false
        NotificationIconBadgePackagesClass?.resolve()?.optional(silent = true)
            ?.firstMethodOrNull { name = "getInstance" }?.invokeQuietly<Any>()
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull { name = "contains" }
            ?.invokeQuietly<Boolean>(packageName) == true
    }

    /**
     * 取通知头部包装器所属的通知实例
     * @param base [NotificationHeaderViewWrapperClass] 实例
     * @return [StatusBarNotification] or null
     */
    private fun statusBarNotificationOf(base: Any?): StatusBarNotification? {
        val row = NotificationViewWrapperClass.resolve().optional().firstFieldOrNull { name = "mRow" }?.of(base)?.get()
        return ExpandableNotificationRowClass.resolve().optional().firstMethodOrNull { name = "getEntry" }
            ?.of(row)?.invokeQuietly()?.let {
                it.asResolver().optional().firstMethodOrNull { name = "getSbn" }?.invoke<StatusBarNotification>()
            }
    }

    /**
     * 取通知头部包装器的图标 [ImageView]
     * @param headerWrapperExImp [OplusNotificationHeaderViewWrapperExImpClass] 实例
     * @return [ImageView] or null
     */
    private fun headerIconOf(headerWrapperExImp: Any?) = headerIconOfEx(headerWrapperExImp).second

    /**
     * 对通知头部图标应用本模块样式
     * @param headerWrapperExImp [OplusNotificationHeaderViewWrapperExImpClass] 实例
     */
    private fun styleHeaderIcon(headerWrapperExImp: Any?) = runInSafe {
        val (base, iconView) = headerIconOfEx(headerWrapperExImp)
        if (base == null || iconView == null) return@runInSafe
        if (isHeaderBadgeComposite(headerWrapperExImp)) {
            moduleStyledIcons[iconView] = false
            return@runInSafe
        }
        val nf = statusBarNotificationOf(base) ?: return@runInSafe
        val context = iconView.context
        nf.notification?.also {
            it.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                compatNotifyIcon(
                    context = context,
                    nf = nf,
                    isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable),
                    packageName = context.packageName,
                    drawable = iconDrawable,
                    iconColor = it.color,
                    iconView = iconView
                )
            }
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
        // 设置通知面板背景透明度
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
        // 移除阴影效果
        view.elevation = if (isEnable) 0f else defaultNotifyPanelElevation
    }

    /** 设置默认通知栏通知图标样式 */
    @SuppressLint("ReplaceWithViewOutlineProviderExtension")
    private fun ImageView.setDefaultNotifyIconViewStyle() {
        // 设置裁切到边界
        clipToOutline = true
        // 设置一个圆角轮廓裁切
        outlineProvider { view, outline ->
            outline.setRoundRect(
                0, 0,
                view.width, view.height, 3.toPx(context).toFloat()
            )
        }
        // 清除图标间距
        setPadding(0)
        // 清除背景
        background = null
        // 清除着色
        colorFilter = null
    }

    /** 注册生命周期 */
    private fun registerLifecycle() {
        registerAppLifecycle {
            // 解锁后重新刷新状态栏图标防止系统重新设置它
            registerReceiver(Intent.ACTION_USER_PRESENT) { _, _ -> if (isUsingCachingMethod) refreshStatusBarIcons() }
            // 注册定时监听
            registerReceiver(Intent.ACTION_TIME_TICK) { context, _ ->
                if (ConfigData.isEnableNotifyIconFix && ConfigData.isEnableNotifyIconFixAuto) {
                    val timestamp = System.currentTimeMillis()
                    val nowTime = timestamp.stampToDate(format = "HH:mm")
                    val today = timestamp.stampToDate(format = "yyyy-MM-dd")
                    // 息屏 Doze 时 TIME_TICK 会被合并为几分钟一次，可能错过设定的那一分钟，
                    // 所以过了设定时间后当天的任意一次 TIME_TICK 都可以补触发，两者均为补零的 "HH:mm" 可直接比较
                    if (nowTime >= ConfigData.notifyIconFixAutoTime && today != lastAutoUpdateIconRuleDate) {
                        lastAutoUpdateIconRuleDate = today
                        requestIconRuleUpdate(context)
                    }
                }
            }
            // 注册发送适配新的 APP 图标通知监听
            registerReceiver(IntentFilter().apply {
                addDataScheme("package")
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
            }) { context, intent ->
                intent.data?.schemeSpecificPart?.also { packageName ->
                    if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED)) ActivationPromptTool.prompt(
                        context, packageName,
                        isReloaded = moduleLastUpdateTime != null && context.appLastUpdateTimeOf(packageName) == moduleLastUpdateTime,
                        resources = module.resources
                    )
                    if (intent.action.equals(Intent.ACTION_PACKAGE_REPLACED).not() &&
                        intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    ) return@registerReceiver
                    if (ConfigData.isEnableNotifyIconFix && ConfigData.isEnableNotifyIconFixNotify)
                        when (intent.action) {
                            Intent.ACTION_PACKAGE_ADDED -> {
                                val snapshot = IconRuleManagerTool.snapshot ?: return@registerReceiver
                                if (snapshot.getIcon(packageName) == null)
                                    IconAdaptationTool.pushNewAppSupportNotify(context, packageName, module.resources)
                            }
                            Intent.ACTION_PACKAGE_REMOVED -> IconAdaptationTool.removeNewAppSupportNotify(context, packageName)
                        }
                }
            }
            onCreate { onHostCreated() }
        }
        // 刷新图标缓存
        SystemUITool.Host.onRefreshSystemUI(param = this) { recachingPrefs(it) }
        // 模块获取资源并写入共享文件后发送刷新命令，SystemUI 从共享文件重新装载
        SystemUITool.Host.onRefreshIconData(param = this) { version, result ->
            recachingPrefs(isRefreshCacheOnly = true)
            globalContext?.let { refreshIconData(it, requestedVersion = version, result = result) } ?: result(false)
        }
        // 模块获取已安装的应用，用于展示通知图标优化名单的适配情况
        SystemUITool.Host.onPushInstalledApps(param = this) {
            globalContext?.let { IconAdaptationTool.obtainInstalledApps(it) } ?: arrayListOf()
        }
        // 模块获取未收录应用的图标
        SystemUITool.Host.onPushAppIcons(param = this) { packageName ->
            globalContext?.let { IconAdaptationTool.obtainAppIconData(it, packageName) }
        }
        // 模块的定时更新服务已启动，后续结果由模块自行通知
        SystemUITool.Host.onIconRuleUpdateStarted(param = this) {
            iconRuleUpdateFallbackJob?.cancel()
            iconRuleUpdateFallbackJob = null
        }
    }

    /** 宿主 [Application] 创建后的初始化，模块热重载后同样需要执行 */
    private fun Application.onHostCreated() {
        globalContext = this
        moduleLastUpdateTime = appLastUpdateTimeOf(BuildConfigWrapper.APPLICATION_ID)
        // 从模块共享文件恢复 ANIP 快照
        globalContext?.let { refreshIconData(it) }
    }

    /**
     * 系统主题或桌面图标样式变化后刷新通知图标
     *
     * 切换主题或图标样式只改变 Oplus 扩展配置 (`OplusExtraConfiguration`)，公开配置没有变化时
     * `android.app.ConfigurationController` 不会回调 SystemUI 的 onConfigurationChanged，已显示的通知不会重新获取桌面图标，
     * 所以在 `OplusCommonInjector.applyConfigurationToResourcesForResourcesManager` 收到的配置变化中判断
     * @param changes 本次配置变化的标志位
     */
    private fun refreshIconsIfThemeChanged(changes: Int) {
        if (changes and (THEME_CHANGED or UX_ICON_CONFIG_CHANGED) == 0) return
        if (ConfigData.isEnableModuleLog) YLog.debug("Theme or icon style changed, refreshing notification icons")
        // 此时进程资源还没有全部应用新配置，延迟到本次配置处理完成后再刷新
        delayedRun {
            refreshStatusBarIcons()
            refreshNotificationIcons()
        }
    }

    /**
     * 重新创建全部通知的状态栏、息屏等位置的图标
     *
     * 与系统 `IconManager.recalculateForImportantConversationChange` 的做法一致，逐个调用 `updateIcons`，
     * 它会清除已缓存的图标并重新调用 `getIconDescriptor`
     */
    private fun refreshEntryIcons() = runInSafe {
        val manager = iconManager?.asResolver()?.optional(silent = true) ?: return@runInSafe
        val updateIcons = manager.firstMethodOrNull {
            name = "updateIcons"
            parameters(NotificationEntryClass, Boolean::class)
        } ?: return@runInSafe
        manager.firstFieldOrNull { name = "notifCollection" }?.getQuietly<Any>()
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                name = "getAllNotifs"
                emptyParameters()
            }?.invokeQuietly<Collection<*>>()?.forEach { entry -> runInSafe { updateIcons.invoke(entry, false) } }
    }

    /** 模块热重载完成后重新执行宿主初始化，热重载不会重放 [Application.onCreate] */
    fun onReloaded() {
        if (isHooked.not()) return
        iconRuleScope.launch {
            restoreNotificationPresenter()
            hostApplication?.apply {
                onHostCreated()
                if (ActivationPromptTool.isPrompting(this))
                    ActivationPromptTool.prompt(this, BuildConfigWrapper.APPLICATION_ID, isReloaded = true, resources = module.resources)
            }
        }
    }

    /**
     * 热重载后通知控制器的构造方法不会再次执行，[notificationPresenter] 会一直为空直到下一个实例被创建
     *
     * 通知控制器在 attach 时会把自己注册到 ConfigurationController，这里从它的监听列表中找回已存在的实例
     */
    private fun restoreNotificationPresenter() = runInSafe {
        if (notificationPresenter != null) return@runInSafe
        val presenterClass = (if (isNotificationPresenter) StatusBarNotificationPresenterClass else ViewConfigCoordinatorClass)
            ?: return@runInSafe
        val listeners = DependencyExClass?.resolve()?.optional(silent = true)
            ?.firstFieldOrNull { name = "sDependency" }?.getQuietly<Any>()
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                name = "getDependency"
                parameters(Class::class)
            }?.invokeQuietly<Any>(ConfigurationControllerClass)
            ?.asResolver()?.optional(silent = true)?.firstMethodOrNull { name = "getListeners" }?.invokeQuietly<List<*>>()
        notificationPresenter = listeners?.let { synchronized(it) { it.firstOrNull { e -> presenterClass.isInstance(e) } } }
        if (notificationPresenter == null) YLog.warn("Failed to restore notification presenter after reloaded")
    }

    /** 释放当前模块实例在宿主中启动的协程和监听 */
    fun release() {
        iconRuleScope.cancel()
        wallpaperColorsListener?.let { (manager, listener) -> manager.removeOnColorsChangedListener(listener) }
        wallpaperColorsListener = null
    }

    /**
     * 启动模块的定时更新服务，SystemUI 只能读取共享文件，获取资源需要交给模块进程
     *
     * 无法启动或超时未响应时提醒用户手动同步，legacy 运行时回退到 [legacyRefreshIconData]
     * @param context SystemUI 上下文
     */
    private fun requestIconRuleUpdate(context: Context) {
        if (isSharedCacheAvailable.not()) return legacyRefreshIconData(context, isNotifyResult = true)
        /** 提醒用户手动同步 */
        fun notifyManualUpdate() = runCatching {
            IconRuleManagerTool.notifyFetchResult(
                context, Anip.FetchResult(module.resources.getString(R.string.sync_cannot_start_module), Anip.FetchResult.Status.FAILED),
                module.resources
            )
        }.onFailure { YLog.error("Failed to notify icon resource update", it) }
        iconRuleUpdateFallbackJob?.cancel()
        runCatching {
            context.startForegroundService(Intent().apply {
                component = ComponentName(BuildConfigWrapper.APPLICATION_ID, classOf<IconRuleUpdateService>().name)
            })
        }.onFailure {
            YLog.error("Failed to start icon rule update service", it)
            notifyManualUpdate()
            return
        }
        iconRuleUpdateFallbackJob = iconRuleScope.launch {
            delay(ICON_RULE_UPDATE_TIMEOUT.milliseconds)
            YLog.warn("Icon rule update service did not respond")
            notifyManualUpdate()
        }
    }

    /**
     * 从模块共享文件重新装载 SystemUI 进程的 ANIP 快照，legacy 运行时回退到 [legacyRefreshIconData]
     * @param context SystemUI 上下文
     * @param requestedVersion 模块已获取的仓库版本，已生效时直接确认
     * @param result 装载完成回调
     */
    private fun refreshIconData(
        context: Context,
        requestedVersion: Pair<String, Long>? = null,
        result: (Boolean) -> Unit = {}
    ) {
        if (isSharedCacheAvailable.not()) return legacyRefreshIconData(context, requestedVersion, result = result)
        iconRuleScope.launch {
            if (requestedVersion != null && IconRuleManagerTool.hasSnapshotVersion(requestedVersion)) {
                result(true)
                return@launch
            }
            val previousVersion = IconRuleManagerTool.snapshotVersion
            runCatching {
                IconRuleManagerTool.reload(context)
                val isAvailable = IconRuleManagerTool.snapshot?.icons.isNullOrEmpty().not()
                if (isAvailable && previousVersion != IconRuleManagerTool.snapshotVersion) {
                    refreshStatusBarIcons()
                    refreshNotificationIcons()
                }
                result(isAvailable && (requestedVersion == null || IconRuleManagerTool.hasSnapshotVersion(requestedVersion)))
            }.onFailure {
                YLog.error("Failed to reload ANIP icon resources", it)
                result(false)
            }
        }
    }

    /**
     * 回退：legacy 运行时没有模块共享文件，SystemUI 恢复自己的私有缓存并自行获取资源
     * @param context SystemUI 上下文
     * @param requestedVersion 模块已获取的仓库版本，已生效时直接确认
     * @param isNotifyResult 是否发送定时同步结果通知
     * @param result 更新完成回调
     */
    private fun legacyRefreshIconData(
        context: Context,
        requestedVersion: Pair<String, Long>? = null,
        isNotifyResult: Boolean = false,
        result: (Boolean) -> Unit = {}
    ) {
        iconRuleScope.launch {
            if (requestedVersion != null && IconRuleManagerTool.hasSnapshotVersion(requestedVersion)) {
                result(true)
                return@launch
            }
            val previousVersion = IconRuleManagerTool.snapshotVersion
            val fetchResult = runCatching {
                IconRuleManagerTool.reload(context)
                IconRuleManagerTool.fetch(context, module.resources).also {
                    val isAvailable = IconRuleManagerTool.snapshot?.icons.isNullOrEmpty().not()
                    if (isAvailable && previousVersion != IconRuleManagerTool.snapshotVersion) {
                        refreshStatusBarIcons()
                        refreshNotificationIcons()
                    }
                    result(it.isOk && isAvailable &&
                        (requestedVersion == null || IconRuleManagerTool.hasSnapshotVersion(requestedVersion)))
                }
            }.getOrElse {
                YLog.error("Failed to refresh ANIP icon resources", it)
                result(false)
                Anip.FetchResult(it.message ?: "Failed to get ANIP icon resources", Anip.FetchResult.Status.FAILED)
            }
            if (isNotifyResult) runCatching { IconRuleManagerTool.notifyFetchResult(context, fetchResult, module.resources) }
                .onFailure { YLog.error("Failed to notify icon resource update", it) }
        }
    }

    /** 获取媒体会话管理器 */
    fun getMediaSessionManager(context: Context): MediaSessionManager {
        if (mediaSessionManager == null)
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        return mediaSessionManager!!
    }

    /** 判断是否为媒体通知 */
    fun isMediaNotificationAOSP(notification: Notification?): Boolean {
        if (notification == null) return false

        return notification.javaClass.resolve().firstMethod { name = "isMediaNotification" }.of(notification).invoke<Boolean>() == true
    }

    /** 通过媒体会话和 AOSP 方法判断是否为媒体通知 */
    fun isMediaNotification(context: Context, notification: Notification, packageName: String): Boolean {
        if (isMediaNotificationAOSP(notification)) return true

        val mediaSessionManager: MediaSessionManager = getMediaSessionManager(context)

        @Suppress("DEPRECATION")
        for (mediaController in mediaSessionManager.getActiveSessions(null))
            if (packageName == mediaController.getPackageName() && notification.contentView != null)
                return true

        return false
    }

    /**
     * 刷新缓存数据
     * @param isRefreshCacheOnly 仅刷新缓存不刷新图标和通知改变 - 默认：否
     * @return [Boolean] 是否成功
     */
    private fun recachingPrefs(isRefreshCacheOnly: Boolean = false): Boolean {
        // 必要的延迟防止 Sp 存储不刷新
        SystemClock.sleep(300)
        // 获取可读写状态
        return preferences.isAvailable.also {
            isUsingCachingMethod = true
            if (isRefreshCacheOnly) return@also
            refreshStatusBarIcons()
            refreshNotificationIcons()
        }
    }

    override fun onHook() {
        isHooked = true
        // SystemUI 只读取模块写入的 ANIP 共享文件
        if (isSharedCacheAvailable) IconRuleManagerTool.attachHost(sharedFiles)
        // 注册生命周期
        registerLifecycle()
        // 系统主题或桌面图标样式变化后刷新通知图标
        OplusCommonInjectorClass?.resolve()?.optional()?.firstMethodOrNull {
            name = "applyConfigurationToResourcesForResourcesManager"
            parameters(Configuration::class, Int::class)
        }?.hook()?.after { refreshIconsIfThemeChanged(lastArg().get<Int>() ?: 0) }
        // 移除开发者警告通知
        SystemPromptControllerClass.resolve().optional().firstMethodOrNull {
            name = "updateDeveloperMode"
        }?.hook()?.before {
            // 是否移除
            if (ConfigData.isEnableRemoveDevNotify) result = null
        }
        // 移除充电完成通知
        OplusPowerNotificationWarningsClass.resolve().optional().firstMethodOrNull {
            name = "showChargeErrorDialog"
            parameters(Int::class)
        }?.hook()?.before {
            // 是否移除
            if (firstArg().get<Int>() == 7 && ConfigData.isEnableRemoveChangeCompleteNotify) result = null
        }
        // 移除免打扰通知
        DndAlertHelperClass.resolve().optional(silent = true).apply {
            firstMethodOrNull {
                name { it.lowercase() == "sendnotificationwithendtime" }
                parameters(Long::class)
            }?.hook()?.before {
                // 是否移除
                if (ConfigData.isEnableRemoveDndAlertNotify) result = null
            }
            firstMethodOrNull {
                name = "operateNotification"
                parameters(Long::class, Int::class, Boolean::class)
            }?.hook()?.before {
                // 是否移除
                if (ConfigData.isEnableRemoveDndAlertNotify) result = null
            }
        }
        // 拦截 ColorOS 使用应用图标判断
        OplusNotificationSmallIconUtilClass?.resolve()?.optional()?.apply {
            firstMethodOrNull {
                name = "useAppIconForSmallIcon"
                parameters(Notification::class)
            }?.hook()?.intercept(false)
        }
        // 修复并替换 ColorOS 以及原生灰度图标色彩判断
        NotificationUtilsClass.resolve().optional(silent = true).apply {
            firstMethodOrNull {
                name = "isGrayscale"
                parameters(ImageView::class, ContrastColorUtilClass)
            }?.hook()?.intercept { firstArg().get<ImageView>()?.let { isGrayscaleIcon(it.context, it.drawable) } ?: callOriginal() }
            firstMethodOrNull {
                name = "isGrayscaleOplus"
                parameters(ImageView::class, OplusContrastColorUtilClass ?: VagueType)
            }?.hook()?.intercept { firstArg().get<ImageView>()?.let { isGrayscaleIcon(it.context, it.drawable) } ?: callOriginal() }
        }
        // 替换状态栏图标
        IconManagerClass.resolve().optional().firstMethodOrNull {
            name = "getIconDescriptor"
            parameters(NotificationEntryClass, Boolean::class)
        }?.hook()?.after {
            iconManager = instance
            IconBuilderClass.resolve().optional().firstFieldOrNull { name = "context" }
                ?.of(IconManagerClass.resolve().optional().firstFieldOrNull { name = "iconBuilder" }?.of(instance)?.get())
                ?.getQuietly<Context>()?.also { context ->
                    NotificationEntryClass.resolve().optional().firstMethodOrNull {
                        name = "getSbn"
                    }?.of(firstArg().get())?.invokeQuietly<StatusBarNotification>()?.also { nf ->
                        nf.notification.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                            compatStatusIcon(
                                context = context,
                                nf = nf,
                                isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable),
                                packageName = nf.packageName,
                                drawable = iconDrawable
                            ).also { pair ->
                                if (pair.second) {
                                    val bitmap = pair.first.toStatusBarBitmap(context)
                                    StatusBarIconClass.resolve().optional().firstFieldOrNull {
                                        name = "icon"
                                        type = Icon::class
                                    }?.of(result)?.set(Icon.createWithBitmap(bitmap))
                                }
                            }
                        }
                    }
                }
        }
        // 得到状态栏图标实例
        StatusBarIconViewClass.resolve().optional().firstMethodOrNull {
            name = "setNotification"
            parameters(StatusBarNotification::class)
        }?.hook()?.after {
            // 注册壁纸颜色监听
            if (firstArg().get() != null) instance<ImageView>().also { registerWallpaperColorChanged(it) }
        }
        // 注入通知控制器实例
        if (isNotificationPresenter)
            StatusBarNotificationPresenterClass.resolve().optional().constructor {}.hookAll().after { notificationPresenter = instance }
        else
            ViewConfigCoordinatorClass?.resolve()?.optional()?.constructor {}?.hookAll()?.after { notificationPresenter = instance }
        // 替换通知面板背景 - 新版本
        if (!isOldNotificationBackground)
            OplusNotificationBackgroundViewClass?.resolve()?.optional()?.apply {
                firstMethodOrNull {
                    name { it == "drawRegionBlur" || it == "draw" }
                    parameterCount = 2
                    superclass()
                }?.hook()?.before { modifyNotifyPanelAlpha(instance(), lastArg().get<Drawable>()) }
            }
        // 替换通知面板背景 - 旧版本
        if (isOldNotificationBackground)
            NotificationBackgroundViewClass?.resolve()?.optional(silent = true)?.apply {
                firstMethodOrNull {
                    name = "draw"
                    parameterCount = 2
                }?.hook()?.before { modifyNotifyPanelAlpha(instance(), lastArg().get<Drawable>()) }
                firstMethodOrNull {
                    name = "drawCustom"
                    parameterCount = 2
                }?.hook()?.before { modifyNotifyPanelAlpha(instance(), lastArg().get<Drawable>()) }
            }
        // 替换通知面板背景 - 避免折叠展开通知二次修改通知面板背景
        ExpandableNotificationRowClass.resolve().optional().apply {
            firstMethodOrNull {
                name = "updateBackgroundForGroupState"
                emptyParameters()
            }?.hook()?.before {
                if (ConfigData.isEnableNotifyPanelAlpha)
                    firstFieldOrNull { name = "mShowGroupBackgroundWhenExpanded" }?.of(instance)?.set(true)
            }
        }
        // 替换媒体通知面板背景 - 设置媒体通知自动展开
        OplusMediaControlPanelClass?.resolve()?.optional()?.apply {
            firstMethodOrNull {
                name = "bind"
                parameterCount = 2
            }?.hook()?.after {
                // 得到当前实例
                val holder = OplusMediaControlPanelClass?.resolve()?.optional()?.firstFieldOrNull {
                    name = "mViewHolder"
                    superclass()
                }?.of(instance)?.get()
                // 记录媒体通知 [View]
                notificationPlayerView = PlayerViewHolderClass?.resolve()?.optional()?.firstMethodOrNull {
                    name = "getPlayer"
                    emptyParameters()
                }?.of(holder)?.invokeQuietly<View>()
                // 设置背景着色
                modifyNotifyPanelAlpha(notificationPlayerView, isTint = true)
                // 当前是否正在播放
                val isPlaying = MediaDataClass?.resolve()?.optional()?.firstMethodOrNull {
                    name = "isPlaying"
                    emptyParameters()
                }?.of(firstArg().get())?.invokeQuietly<Boolean>() ?: false

                // 当前通知是否展开
                val isExpanded = OplusMediaViewControllerClass?.resolve()?.optional()?.firstMethodOrNull {
                    name = "getExpanded"
                    emptyParameters()
                }?.of(firstFieldOrNull { name = "mOplusMediaViewController" }?.of(instance)?.get())?.invokeQuietly<Boolean>() ?: false
                // 符合条件后执行
                if (ConfigData.isEnableNotifyMediaPanelAutoExp.not() || isExpanded || isPlaying.not()) return@after
                // 模拟手动展开通知
                BasePlayViewHolderClass?.resolve()?.optional()?.firstMethodOrNull {
                    name = "getExpandButton"
                    emptyParameters()
                }?.of(holder)?.invokeQuietly<View>()?.performClick()
            }
        }

        // 替换通知头像右下角的应用图标角标 - ColorOS 16+
        OplusNotificationHeaderViewWrapperExImpClass?.resolve()?.optional(silent = true)?.firstMethodOrNull {
            name = "applyExpandedCanvasIconWithBadge"
            parameterCount = 4
        }?.hook()?.before { compatHeaderBadge(instance, firstArg().get<Drawable>()) }
        // 替换息屏时钟布局的通知图标 - ColorOS 16+
        AodNotificationLayoutClass?.resolve()?.optional(silent = true)?.firstMethodOrNull {
            name = "updateNotificationView"
            parameters(StatusBarNotification::class)
        }?.hook()?.intercept {
            val nf = firstArg().get<StatusBarNotification>()
            val bitmap = nf?.let { compatAodBitmap(globalContext ?: instance<View>().context, it) }
            // 系统直接读取通知的小图标，这里只在方法执行期间临时替换，执行完立刻还原
            val field = nf?.notification?.asResolver()?.optional(silent = true)?.firstFieldOrNull { name = "mSmallIcon" }
            if (nf == null || bitmap == null || field == null) return@intercept callOriginal()
            val original = nf.notification.smallIcon
            field.set(Icon.createWithBitmap(bitmap))
            try {
                callOriginal()
            } finally {
                field.set(original)
            }
        }
        // 替换息屏通知图标 - ColorOS 16+
        AodPluginCallImplClass?.resolve()?.optional(silent = true)?.firstMethodOrNull {
            name = "updateNotificationIconData"
            emptyParameters()
        }?.hook()?.before { compatAodIconData() }
        // 替换全屏应用通知轻反馈图标 - ColorOS 16+
        FullScreenBannerHelperClass?.resolve()?.optional(silent = true)?.firstMethodOrNull {
            name = "buildNotification"
            parameterCount = 2
        }?.hook()?.after { compatBannerBuilder(firstArg().get<Context>(), lastArg().get(), result) }
        // 替换锁屏岛 (锁屏胶囊) 图标 - ColorOS 16+
        CapsuleNotificationIconDataClass?.resolve()?.optional(silent = true)?.constructor {
            parameterCount = 8
        }?.hookAll()?.before {
            // 排除带默认参数的合成构造方法
            if (args[7] !is Boolean) return@before
            compatCapsuleIcon(args)?.also { arg(2).set(it) }
        }

        if (isNewNotification) {
            // 阻止颜色覆盖
            OplusNotificationGroupExtImplClass?.resolve()?.optional()?.firstMethodOrNull {
                name = "updateExpandIconColorFilter"
                parameterCount = 1
            }?.hook()?.intercept()
            // 动态处理通知展开
            ExpandableNotificationRowClass.resolve().optional().apply {
                firstMethodOrNull {
                    name = "onExpansionChanged"
                    parameterCount = 2
                }?.hook()?.before {
                    ExpandableNotificationRowClass.resolve().optional()
                        .firstFieldOrNull { name = "mChildrenContainer" }
                        ?.of(instance)?.getQuietly()?.let {
                            it.asResolver().optional().firstFieldOrNull {
                                name = "mCurrentHeader"
                                superclass()
                            }?.get()
                        }.also { header ->
                            header?.asResolver()?.optional()?.firstFieldOrNull {
                                name = "mIcon"
                            }?.get<ImageView>()?.apply {
                                ExpandableNotificationRowClass.resolve().optional()
                                    .firstMethodOrNull { name = "getEntry" }
                                    ?.of(instance)?.invokeQuietly()?.let {
                                        it.asResolver().optional().firstMethodOrNull {
                                            name = "getSbn"
                                        }?.invoke<StatusBarNotification>()
                                    }.also { nf ->
                                        nf?.notification?.also {
                                            if (!isMediaNotification(context, it, context.packageName))
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
            // 替换通知图标和样式
            OplusNotificationHeaderViewWrapperExImpClass?.resolve()?.optional()?.apply {
                firstMethodOrNull {
                    name = "proxyOnContentUpdated"
                    parameterCount = 1
                }?.hook()?.after {
                    headerIconOf(instance)?.apply {
                        // 系统会合成头像与应用图标角标，此处不覆盖
                        if (isHeaderBadgeComposite(instance)) {
                            moduleStyledIcons[this] = false
                            return@apply
                        }
                        ExpandableNotificationRowClass.resolve().optional()
                            .firstMethodOrNull { name = "getEntry" }
                            ?.of(args[0])?.invokeQuietly()?.let {
                                it.asResolver().optional().firstMethodOrNull {
                                    name = "getSbn"
                                }?.invoke<StatusBarNotification>()
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
                firstMethodOrNull {
                    name = "updateIconColor"
                    emptyParameters()
                }?.hook()?.apply {
                    before { if (moduleStyledIcons[headerIconOf(instance)] == true && isHeaderBadgeComposite(instance).not()) result = false }
                    after { headerIconOf(instance)?.let { styleHeaderIcon(instance) } }
                }
                firstMethodOrNull {
                    name = "updateIconRoundness"
                    parameterCount = 1
                }?.hook()?.apply {
                    before { if (moduleStyledIcons[headerIconOf(instance)] == true && isHeaderBadgeComposite(instance).not()) result = null }
                    after { headerIconOf(instance)?.let { styleHeaderIcon(instance) } }
                    // after { headerIconOf(instance)?.let { if (!moduleStyledIcons.containsKey(it)) styleHeaderIcon(instance) } }
                }
                // ColorOS 16 对带会话头像的通知（如 QQ）会单独走这个方法把图标换回去，处理完再套一次模块样式
                firstMethodOrNull {
                    name = "updateIconWithAppIconIfNeeded"
                }?.hook()?.after {
                    runInSafe { headerIconOf(instance)?.let { styleHeaderIcon(instance) } }
                }
            }

            // 系统会在任意时机把通知图标重新设成自己的 drawable，这里在框架层兜住
            ImageViewClass.resolve().optional().firstMethodOrNull {
                name = "setImageDrawable"
                parameters(Drawable::class)
            }?.hook()?.before {
                val iconView = instance<ImageView>()
                if (moduleStyledIcons[iconView] == true) {
                    moduleStyledDrawables[iconView]?.let { styled ->
                        if (arg(0).get<Drawable>() !== styled) arg(0).set(styled)
                    }
                }
            }

            OplusNotificationGroupTemplateWrapperClass?.resolve()?.optional()?.apply {
                firstMethodOrNull {
                    name = "initIcon"
                }?.hook()?.before {
                    val instanceContext = firstFieldOrNull {
                        name = "context"
                    }?.of(instance)?.get() as Context?
                    if (instanceContext == null)
                        return@before
                    result = null
                    NotificationHeaderViewWrapperClass.resolve().optional().firstFieldOrNull { name = "mIcon" }?.of(instance)?.get<ImageView>()
                        ?.apply {
                            ExpandableNotificationRowClass.resolve().optional()
                                .firstMethodOrNull { name = "getEntry" }
                                ?.of(NotificationViewWrapperClass.resolve().optional().firstFieldOrNull {
                                    name = "mRow"
                                }?.of(instance)?.get())?.invokeQuietly()?.let {
                                    it.asResolver().optional().firstMethodOrNull {
                                        name = "getSbn"
                                    }?.invoke<StatusBarNotification>()
                                }.also { nf ->
                                    val context = StatusBarNotification::class.resolve().firstMethod {
                                        name = "getPackageContext"
                                    }.of(nf).invoke<Context>(instanceContext)
                                    if (context == null) return@also

                                    nf?.notification?.also {
                                        if (!isMediaNotification(context, it, context.packageName))
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
        } else {
            // 注入状态栏通知图标容器实例
            OplusNotificationIconAreaControllerClass.resolve().optional().apply {
                var way = 0
                (firstMethodOrNull {
                    name = "updateIconsForLayout"
                    parameterCount = 10
                } ?: firstMethodOrNull {
                    // ColorOS 14
                    name = "updateIconsForLayout"
                    parameterCount = 5
                } ?: firstMethodOrNull {
                    name = "updateIconsForLayout"
                    parameterCount = 1
                }?.apply { way = 1 }
                    ?: firstMethodOrNull {
                        name = "updateIconsForLayout"
                    }?.apply { way = 2 })?.hook()?.after {
                    when (way) {
                        2 -> notificationIconContainer = OplusNotificationIconAreaControllerClass.resolve().optional()
                            .firstMethodOrNull { name = "getNotificationIcons" }
                            ?.of(instance)?.invoke<ViewGroup>()
                        1 -> {
                            notificationIconInstances.clear()
                            firstFieldOrNull { name = "mLastToShow" }?.of(instance)?.get<List<View>>()
                                ?.takeIf { it.isNotEmpty() }?.forEach { notificationIconInstances.add(it) }
                        }
                        else -> notificationIconContainer = arg(index = 1).get<ViewGroup>()
                    }
                }
            }
            // 注入状态栏通知图标容器实例
            (LegacyNotificationIconAreaControllerImpl ?: NotificationIconAreaControllerClass)
                .resolve().optional().apply {
                    firstMethodOrNull {
                        name = "updateIconsForLayout"
                        parameterCount = 8
                    }?.hook()?.after {
                        notificationIconContainer = arg(index = 1).get<ViewGroup>()
                    }
                }

            // 替换通知图标和样式
            NotificationHeaderViewWrapperClass.resolve().optional().apply {
                method {
                    name { it == "updateExpandability" || it == "setExpanded" }
                }.hookAll().before {
                    firstFieldOrNull { name = "mIcon" }?.of(instance)?.get<ImageView>()?.apply {
                        ExpandableNotificationRowClass.resolve().optional()
                            .firstMethodOrNull { name = "getEntry" }
                            ?.of(NotificationViewWrapperClass.resolve().optional().firstFieldOrNull {
                                name = "mRow"
                            }?.of(instance)?.get())?.invokeQuietly()?.let {
                                it.asResolver().optional().firstMethodOrNull {
                                    name = "getSbn"
                                }?.invoke<StatusBarNotification>()
                            }.also { nf ->
                                nf?.notification?.also {
                                    if (!isMediaNotification(context, it, context.packageName))
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

                method {
                    name { it == "resolveHeaderViews" || it == "onContentUpdated" }
                }.hookAll().after {
                    firstFieldOrNull { name = "mIcon" }?.of(instance)?.get<ImageView>()?.apply {
                        ExpandableNotificationRowClass.resolve().optional()
                            .firstMethodOrNull { name = "getEntry" }
                            ?.of(NotificationViewWrapperClass.resolve().optional().firstFieldOrNull {
                                name = "mRow"
                            }?.of(instance)?.get())?.invokeQuietly()?.let {
                                it.asResolver().optional().firstMethodOrNull {
                                    name = "getSbn"
                                }?.invoke<StatusBarNotification>()
                            }.also { nf ->
                                nf?.notification?.also {
                                    it.smallIcon.loadDrawable(context)?.also { iconDrawable ->
                                        // 执行替换
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
                                        // 延迟重新设置防止部分机型的系统重新设置图标出现图标着色后黑白块问题
                                        delayedRun(ms = 1500) { doParse() }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    /**
     * 通知图标自绘 [Drawable] 灵感来源: @1Dot
     * @param glyph 单色图标(alpha 蒙版)
     * @param badgeColor 背景徽章颜色 - 透明则不画背景 (经典风格)
     * @param glyphColor 图标着色
     * @param cornerRadiusPx 背景圆角 (px)
     * @param referenceSidePx 背景圆角对应的图标边长 (px) - 实际绘制边长不同时圆角按比例缩放，为 0 则不缩放
     * @param paddingRatio 图标相对背景的内边距比例
     */
    private class CustomIconDrawable(
        glyph: Drawable,
        private val badgeColor: Int,
        private val glyphColor: Int,
        private val cornerRadiusPx: Float,
        private val paddingRatio: Float,
        private val referenceSidePx: Float = 0f
    ) : Drawable() {

        private val glyph: Drawable = glyph.mutate()

        private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = badgeColor
            style = Paint.Style.FILL
        }

        private var drawableAlpha = 255

        init {
            this.glyph.setTint(glyphColor)
        }

        override fun draw(canvas: Canvas) {
            val b = bounds
            val w = b.width()
            val h = b.height()
            if (w <= 0 || h <= 0) return
            val side = minOf(w, h).toFloat()
            val left = b.left + (w - side) / 2f
            val top = b.top + (h - side) / 2f
            val right = left + side
            val bottom = top + side
            if (Color.alpha(badgeColor) != 0) {
                backgroundPaint.alpha = (Color.alpha(badgeColor) * drawableAlpha / 255).coerceIn(0, 255)
                val radius = if (referenceSidePx > 0f) cornerRadiusPx * side / referenceSidePx else cornerRadiusPx
                canvas.drawRoundRect(left, top, right, bottom, radius, radius, backgroundPaint)
            }
            val pad = side * paddingRatio
            glyph.setTint(glyphColor)
            glyph.alpha = drawableAlpha
            glyph.setBounds((left + pad).toInt(), (top + pad).toInt(), (right - pad).toInt(), (bottom - pad).toInt())
            glyph.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {
            drawableAlpha = alpha.coerceIn(0, 255)
            invalidateSelf()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            glyph.setTint(glyphColor)
            invalidateSelf()
        }

        override fun setTint(tintColor: Int) {
            glyph.setTint(glyphColor)
            invalidateSelf()
        }

        override fun setTintList(tint: ColorStateList?) {
            glyph.setTint(glyphColor)
            invalidateSelf()
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity() = PixelFormat.TRANSLUCENT

        override fun getIntrinsicWidth() = glyph.intrinsicWidth
        override fun getIntrinsicHeight() = glyph.intrinsicHeight
    }
}