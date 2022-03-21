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
 * This file is Created by fankes on 2022/2/26.
 */
package com.fankes.coloros.notify.hook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.VectorDrawable
import android.service.notification.StatusBarNotification
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.fankes.coloros.notify.bean.IconDataBean
import com.fankes.coloros.notify.hook.HookConst.ENABLE_ANDROID12_STYLE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE_LOG
import com.fankes.coloros.notify.hook.HookConst.ENABLE_NOTIFY_ICON_FIX
import com.fankes.coloros.notify.hook.HookConst.ENABLE_NOTIFY_ICON_FIX_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_CHANGECP_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DEV_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DNDALERT_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.SYSTEMUI_PACKAGE_NAME
import com.fankes.coloros.notify.hook.factory.isAppNotifyHookAllOf
import com.fankes.coloros.notify.hook.factory.isAppNotifyHookOf
import com.fankes.coloros.notify.param.IconPackParams
import com.fankes.coloros.notify.utils.drawable.drawabletoolbox.DrawableBuilder
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.IconAdaptationTool
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerW
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.*
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.xposed.proxy.YukiHookXposedInitProxy

@InjectYukiHookWithXposed
class HookEntry : YukiHookXposedInitProxy {

    companion object {

        /** 原生存在的类 */
        private const val ContrastColorUtilClass = "com.android.internal.util.ContrastColorUtil"

        /** 原生存在的类 */
        private const val NotificationUtilsClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.NotificationUtils"

        /** 原生存在的类 */
        private const val NotificationEntryClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.collection.NotificationEntry"

        /** 原生存在的类 */
        private const val StatusBarIconClass = "com.android.internal.statusbar.StatusBarIcon"

        /** 原生存在的类 */
        private const val IconBuilderClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.icon.IconBuilder"

        /** 原生存在的类 */
        private const val IconManagerClass = "$SYSTEMUI_PACKAGE_NAME.statusbar.notification.icon.IconManager"

        /** ColorOS 存在的类 - 旧版本不存在 */
        private const val OplusContrastColorUtilClass = "com.oplusos.util.OplusContrastColorUtil"

        /** 原生存在的类 */
        private const val PluginManagerImplClass = "$SYSTEMUI_PACKAGE_NAME.shared.plugins.PluginManagerImpl"

        /** 根据多个版本存在不同的包名相同的类 */
        private val SystemPromptControllerClass = VariousClass(
            "com.oplusos.systemui.statusbar.policy.SystemPromptController",
            "com.coloros.systemui.statusbar.policy.SystemPromptController"
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
        private val AbstractReceiverClass = VariousClass(
            "com.oplusos.systemui.common.receiver.AbstractReceiver",
            "com.coloros.systemui.common.receiver.AbstractReceiver"
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
    }

    /** 缓存的通知优化图标数组 */
    private var iconDatas = ArrayList<IconDataBean>()

    /**
     * 打印日志
     * @param tag 标识
     * @param context 实例
     * @param packageName APP 包名
     * @param isCustom 是否为通知优化生效图标
     * @param isGrayscale 是否为灰度图标
     */
    private fun PackageParam.printLogcat(
        tag: String,
        context: Context,
        packageName: String,
        isCustom: Boolean,
        isGrayscale: Boolean
    ) {
        if (prefs.getBoolean(ENABLE_MODULE_LOG)) loggerD(
            msg = "$tag --> [${context.findAppName(packageName)}][$packageName] " +
                    "custom [$isCustom] " +
                    "grayscale [$isGrayscale]"
        )
    }

    /**
     * - 这个是修复彩色图标的关键核心代码判断
     *
     * 判断是否为灰度图标 - 反射执行系统方法
     * @param context 实例
     * @param drawable 要判断的图标
     * @return [Boolean]
     */
    private fun PackageParam.isGrayscaleIcon(context: Context?, drawable: Drawable?) =
        ContrastColorUtilClass.clazz.let {
            drawable is VectorDrawable || it.method {
                name = "isGrayscaleIcon"
                param(DrawableClass)
            }.get(it.method {
                name = "getInstance"
                param(ContextClass)
            }.get().invoke(context)).invoke<Boolean>(drawable) ?: false
        }

    /**
     * 自动适配状态栏、通知栏自定义小图标
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @return [Pair] - ([Bitmap] 位图,[Int] 颜色)
     */
    private fun PackageParam.compatCustomIcon(isGrayscaleIcon: Boolean, packageName: String): Pair<Bitmap?, Int> {
        var customPair: Pair<Bitmap?, Int>? = null
        when {
            /** 替换系统图标为 Android 默认 */
            (packageName == "android" || packageName == "com.android.systemui") && !isGrayscaleIcon -> customPair =
                Pair(if (isUpperOfAndroidS) IconPackParams.android12IconBitmap else IconPackParams.android11IconBitmap, 0)
            /** 替换自定义通知图标 */
            prefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true) -> run {
                if (iconDatas.isNotEmpty())
                    iconDatas.forEach {
                        if (packageName == it.packageName && isAppNotifyHookOf(it)) {
                            if (!isGrayscaleIcon || isAppNotifyHookAllOf(it))
                                customPair = Pair(it.iconBitmap, it.iconColor)
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
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @param drawable 原始图标
     * @return [Bitmap]
     */
    private fun PackageParam.compatStatusIcon(context: Context, isGrayscaleIcon: Boolean, packageName: String, drawable: Drawable) =
        compatCustomIcon(isGrayscaleIcon, packageName).first.also {
            /** 打印日志 */
            printLogcat(tag = "StatusIcon", context, packageName, isCustom = it != null, isGrayscaleIcon)
        } ?: drawable.toBitmap()

    /**
     * 自动适配通知栏小图标
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @param drawable 原始图标
     * @param iconColor 原生图标颜色
     * @param iconView 图标 [ImageView]
     */
    private fun PackageParam.compatNotifyIcon(
        isGrayscaleIcon: Boolean,
        packageName: String,
        drawable: Drawable,
        iconColor: Int,
        iconView: ImageView
    ) {
        compatCustomIcon(isGrayscaleIcon, packageName).also { customPair ->
            when {
                customPair.first != null || isGrayscaleIcon -> iconView.apply {
                    setImageBitmap(customPair.first ?: drawable.toBitmap())
                    /** 是否开启 Android 12 风格 */
                    val isA12Style = prefs.getBoolean(ENABLE_ANDROID12_STYLE, isUpperOfAndroidS)

                    /** 旧版风格 */
                    val oldStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else 0xff707173).toInt()

                    /** 新版风格 */
                    val newStyle = (if (context.isSystemInDarkMode) 0xffdcdcdc else Color.WHITE).toInt()

                    /** 优化风格 */
                    val fixStyle = (if (context.isSystemInDarkMode) 0xff707173 else oldStyle).toInt()

                    /** 旧版图标着色 */
                    val oldApplyColor = customPair.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: oldStyle

                    /** 新版图标着色 */
                    val newApplyColor = customPair.second.takeIf { it != 0 } ?: iconColor.takeIf { it != 0 } ?: fixStyle
                    /** 判断风格并开始 Hook */
                    if (isA12Style) {
                        background = DrawableBuilder().rounded().solidColor(newApplyColor).build()
                        setColorFilter(newStyle)
                        setPadding(2.dp(context), 2.dp(context), 2.dp(context), 2.dp(context))
                    } else {
                        background = null
                        setColorFilter(oldApplyColor)
                        setPadding(0, 0, 0, 0)
                    }
                }
                else -> iconView.apply {
                    setPadding(0, 0, 0, 0)
                    background = null
                    colorFilter = null
                }
            }
            /** 打印日志 */
            printLogcat(tag = "NotifyIcon", iconView.context, packageName, isCustom = customPair.first != null, isGrayscaleIcon)
        }
    }

    override fun onInit() = configs {
        debugTag = "ColorOSNotify"
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp(SYSTEMUI_PACKAGE_NAME) {
            when {
                /** 不是 ColorOS 系统停止 Hook */
                isNotColorOS -> loggerW(msg = "Aborted Hook -> This System is not ColorOS")
                /** Hook 被手动关闭停止 Hook */
                !prefs.getBoolean(ENABLE_MODULE, default = true) -> loggerW(msg = "Aborted Hook -> Hook Closed")
                /** 开始 Hook */
                else -> {
                    /** 缓存图标数据 */
                    iconDatas = IconPackParams(param = this).iconDatas
                    /** 移除开发者警告通知 */
                    SystemPromptControllerClass.hook {
                        injectMember {
                            method { name = "updateDeveloperMode" }
                            beforeHook {
                                /** 是否移除 */
                                if (prefs.getBoolean(REMOVE_DEV_NOTIFY, default = true)) resultNull()
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
                                if (firstArgs as Int == 7 && prefs.getBoolean(REMOVE_CHANGECP_NOTIFY)) resultNull()
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
                                if (prefs.getBoolean(REMOVE_DNDALERT_NOTIFY)) resultNull()
                            }
                        }
                    }
                    /** 修复并替换新版本 ColorOS 原生灰度图标色彩判断*/
                    NotificationUtilsClass.hook {
                        injectMember {
                            method {
                                name = "isGrayscaleOplus"
                                param(ImageViewClass, OplusContrastColorUtilClass.clazz)
                            }
                            replaceAny { (firstArgs as? ImageView?)?.let { isGrayscaleIcon(it.context, it.drawable) } }
                        }.ignoredHookingFailure()
                    }
                    /** 替换状态栏图标 */
                    IconManagerClass.hook {
                        injectMember {
                            method {
                                name = "getIconDescriptor"
                                param(NotificationEntryClass.clazz, BooleanType)
                            }
                            afterHook {
                                IconBuilderClass.clazz.field { name = "context" }
                                    .of<Context>(field { name = "iconBuilder" }.of(instance))?.also { context ->
                                        NotificationEntryClass.clazz.method {
                                            name = "getSbn"
                                        }.get(firstArgs).invoke<StatusBarNotification>()?.also { nf ->
                                            nf.notification.smallIcon.loadDrawable(context).also { iconDrawable ->
                                                StatusBarIconClass.clazz.field {
                                                    name = "icon"
                                                    type = IconClass
                                                }.get(result).set(
                                                    Icon.createWithBitmap(
                                                        compatStatusIcon(
                                                            context = context,
                                                            isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable),
                                                            packageName = nf.packageName,
                                                            drawable = iconDrawable
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                    /** 替换通知图标和样式 */
                    NotificationHeaderViewWrapperClass.hook {
                        injectMember {
                            method { name = "resolveHeaderViews" }
                            afterHook {
                                NotificationHeaderViewWrapperClass.clazz
                                    .field { name = "mIcon" }.of<ImageView>(instance)?.apply {
                                        ExpandableNotificationRowClass.clazz
                                            .method { name = "getEntry" }
                                            .get(NotificationViewWrapperClass.clazz.field {
                                                name = "mRow"
                                            }.get(instance).self).call()?.let {
                                                it.javaClass.method {
                                                    name = "getSbn"
                                                }.get(it).invoke<StatusBarNotification>()
                                            }?.notification?.also { nf ->
                                                nf.smallIcon.loadDrawable(context).also { iconDrawable ->
                                                    compatNotifyIcon(
                                                        isGrayscaleIcon = isGrayscaleIcon(context, iconDrawable),
                                                        packageName = context.packageName,
                                                        drawable = iconDrawable,
                                                        iconColor = nf.color,
                                                        iconView = this
                                                    )
                                                }
                                            }
                                    }
                            }
                        }
                    }
                    /** 发送适配新的 APP 图标通知 */
                    PluginManagerImplClass.hook {
                        injectMember {
                            method {
                                name = "onReceive"
                                param(ContextClass, IntentClass)
                            }
                            afterHook {
                                if (prefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true) &&
                                    prefs.getBoolean(ENABLE_NOTIFY_ICON_FIX_NOTIFY, default = true)
                                ) (lastArgs as? Intent)?.also {
                                    if (!it.action.equals(Intent.ACTION_PACKAGE_REPLACED) &&
                                        it.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                                    ) return@also
                                    when (it.action) {
                                        Intent.ACTION_PACKAGE_ADDED ->
                                            it.data?.schemeSpecificPart?.also { newPkgName ->
                                                if (iconDatas.takeIf { e -> e.isNotEmpty() }
                                                        ?.filter { e -> e.packageName == newPkgName }
                                                        .isNullOrEmpty()
                                                ) IconAdaptationTool.pushNewAppSupportNotify(firstArgs as Context, newPkgName)
                                            }
                                        Intent.ACTION_PACKAGE_REMOVED ->
                                            IconAdaptationTool.removeNewAppSupportNotify(
                                                firstArgs as Context,
                                                packageName = it.data?.schemeSpecificPart ?: ""
                                            )
                                    }
                                }
                            }
                        }
                    }
                    /** 自动检查通知优化图标更新的注入监听 */
                    AbstractReceiverClass.hook {
                        injectMember {
                            method {
                                name = "onReceive"
                                param(ContextClass, IntentClass)
                            }
                            afterHook {
                                // TODO 待实现
                                loggerD(msg = "当前时间：${System.currentTimeMillis()}")
                            }
                        }
                    }
                }
            }
        }
    }
}