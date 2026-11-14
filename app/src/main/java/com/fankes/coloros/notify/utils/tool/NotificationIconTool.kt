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
 * This file is created by fankes on 2026/11/14.
 */
@file:Suppress("DiscouragedApi")

package com.fankes.coloros.notify.utils.tool

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.const.PackageName
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.param.factory.isAppNotifyEnabledOf
import com.fankes.coloros.notify.param.factory.isAppNotifyOverlayOf
import com.fankes.coloros.notify.utils.factory.isSystemInDarkMode
import com.fankes.coloros.notify.utils.factory.isUpperOfColorOS17
import com.fankes.coloros.notify.utils.factory.launcherIconOf
import com.fankes.coloros.notify.utils.factory.safeOfFalse
import com.fankes.coloros.notify.utils.factory.safeOfNull
import com.fankes.coloros.notify.utils.factory.systemAccentColor
import com.fankes.coloros.notify.utils.tool.NotificationIconTool.NOTIFY_ICON_PADDING_RATIO_MD3
import com.fankes.coloros.notify.utils.tool.NotificationIconTool.resolveCustomIcon
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.betterandroid.ui.extension.component.base.getDrawableCompat
import com.highcapable.betterandroid.ui.extension.component.base.toPx
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass

/**
 * 通知图标的规则选择、灰度检测与共用绘制
 */
object NotificationIconTool {

    /**
     * 通知面板图标内边距比例 (MD3 风格) - 内边距占图标视图尺寸的比例 (0f~0.5f)
     *
     * 按视图尺寸而非绝对 dp 计算，折叠与展开的图标视图大小不同，等比内边距才能保证缩小比例一致；
     * 数值越大图标越小，想进一步缩小图标改这里即可
     */
    private const val NOTIFY_ICON_PADDING_RATIO_MD3 = 0.17f

    /** 通知面板图标内边距比例 (经典风格) - 含义同 [NOTIFY_ICON_PADDING_RATIO_MD3] */
    private const val NOTIFY_ICON_PADDING_RATIO_CLASSIC = 0.04f

    /**
     * - 这个是修复彩色图标的关键核心代码判断
     *
     * 判断是否为灰度图标，默认使用模块算法，可切换到系统方案
     * @param context 实例
     * @param drawable 要判断的图标
     * @param contrastColorUtilClass 宿主的系统灰度检测类
     * @return [Boolean]
     */
    fun <T : Any> isGrayscaleIcon(context: Context, drawable: Drawable, contrastColorUtilClass: () -> Class<T>) =
        if (ConfigData.isUseSystemIconInversion) safeOfFalse {
            contrastColorUtilClass().resolve()
                .optional(silent = true)
                .let {
                    it.firstMethodOrNull {
                        name = "isGrayscaleIcon"
                        parameters(Drawable::class)
                    }?.of(
                        it.firstMethodOrNull {
                            name = "getInstance"
                            parameters(Context::class)
                        }?.invoke<T>(context)
                    )?.invokeQuietly<Boolean>(drawable) == true
                }
        } else ContrastBitmapTool.isGrayscale(drawable)

    /**
     * 自动适配状态栏、通知栏自定义小图标
     * @param context 实例
     * @param isGrayscaleIcon 是否为灰度图标
     * @param packageName APP 包名
     * @param moduleResources 模块资源
     * @return [Triple] - ([Drawable] 位图,[Int] 颜色,[Boolean] 是否为占位符图标)
     */
    fun resolveCustomIcon(context: Context, isGrayscaleIcon: Boolean, packageName: String, moduleResources: () -> Resources): Triple<Drawable?, Int, Boolean> {
        var customPair: Triple<Drawable?, Int, Boolean>? = null
        val statSysAdbIcon = runCatching {
            val resId = $$"com.android.internal.R$drawable".toClass()
                .resolve()
                .firstField { name = "stat_sys_adb" }
                .get<Int>() ?: error("Resource not found")
            context.resources.getDrawableCompat(resId)
        }.getOrNull() ?: moduleResources().getDrawableCompat(R.drawable.ic_unsupported)
        when {
            // 替换系统图标为 Android 默认
            (packageName == PackageName.SYSTEM_FRAMEWORK || packageName == PackageName.SYSTEMUI) && isGrayscaleIcon.not() ->
                customPair = Triple(statSysAdbIcon, 0, false)
            // 替换自定义通知图标
            ConfigData.isEnableNotifyIconFix -> run {
                val icon = IconRuleManagerTool.getIcon(packageName)
                if (icon != null) {
                    if (context.isAppNotifyEnabledOf(icon))
                        customPair = Triple(
                            if (isGrayscaleIcon.not() || context.isAppNotifyOverlayOf(icon))
                                IconRuleManagerTool.getBitmap(icon)?.toDrawable(context.resources) else null,
                            icon.color ?: 0,
                            false
                        )
                    return@run
                }
                if (isGrayscaleIcon.not() && ConfigData.isEnableNotifyIconFixPlaceholder)
                    customPair = Triple(moduleResources().getDrawableCompat(R.drawable.ic_message), 0, true)
            }
        }
        return customPair ?: Triple(null, 0, false)
    }

    /**
     * 创建通知栏自绘图标 - 背景与着色图标都由自绘 [Drawable] 承载
     * @param context 实例
     * @param customTriple [resolveCustomIcon] 的结果
     * @param drawable 原始图标
     * @param iconColor 原生图标颜色
     * @param viewSizePx 图标视图尺寸 (px) - 未知时为 0
     * @return [Drawable]
     */
    fun createNotificationIcon(
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
            (d as? BitmapDrawable)?.takeIf { ConfigData.isEnableNotifyIconResampling }?.bitmap
                ?.let { ContrastBitmapTool.resampling(it, contentPx).toDrawable(context.resources) } ?: d
        }

        // 旧版风格 - ColorOS 17+ 通知面板为液态玻璃背景，浅色模式下的深色图标看不清，始终使用夜间模式颜色
        val oldStyle = if (isUpperOfColorOS17 || context.isSystemInDarkMode) 0xFFDCDCDC.toInt() else 0xFF707173.toInt()

        // 原生着色
        val md3Style = if (AndroidVersion.isGreaterThan(AndroidVersion.R)) context.systemAccentColor else
            (if (context.isSystemInDarkMode) 0xFF707173.toInt() else oldStyle)

        // 原生通知图标颜色
        val nativeIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor) 0 else iconColor

        // 保留原生单色图标时，规则颜色只补充通知未提供的颜色
        val customIconColor = if (ConfigData.isEnableNotifyIconForceSystemColor || (customTriple.first == null && iconColor != 0))
            0 else customTriple.second

        // 旧版图标着色
        val oldApplyColor = customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: oldStyle

        // 新版图标 (背景) 着色
        val newApplyColor = customIconColor.takeIf { it != 0 } ?: nativeIconColor.takeIf { it != 0 } ?: md3Style

        // 用自绘 [CustomIconDrawable] 承载 “背景 + 着色图标”，其内部把 setColorFilter/setTint 覆盖为重申自身颜色，
        // 背景也画在图标自身而非视图 background
        return if (isMd3) CustomIconDrawable(
            glyph = glyph,
            badgeColor = newApplyColor,
            glyphColor = if (newApplyColor == -1) Color.BLACK else Color.WHITE,
            cornerRadiusPx = ConfigData.notifyIconCornerSize.toPx(context).toFloat(),
            paddingRatio = padRatio,
            referenceSidePx = if (viewSizePx > 0) viewSizePx.toFloat() else headerIconSizeOf(context).toFloat()
        ) else CustomIconDrawable(
            glyph = glyph,
            badgeColor = Color.TRANSPARENT,
            glyphColor = oldApplyColor,
            cornerRadiusPx = 0f,
            paddingRatio = padRatio
        )
    }

    /**
     * 取通知面板头部图标尺寸 - 圆角设置针对的图标大小
     * @param context 实例
     * @return [Int] 图标尺寸 (px)
     */
    fun headerIconSizeOf(context: Context) =
        context.resources.getIdentifier("notification_header_icon_size", "dimen", context.packageName)
            .takeIf { it != 0 }?.let { context.resources.getDimensionPixelSize(it) } ?: 40.toPx(context)

    /**
     * 为 MCS 生成与通知和媒体图标一致的位图。
     *
     * MCS 不在 SystemUI 进程中，不能直接复用通知视图的 Drawable；这里仍使用同一套规则、Material 3 和尺寸计算。
     * @param context 当前宿主上下文
     * @param packageName 应用包名
     * @param sizePx 输出边长
     * @param moduleResources 模块资源
     * @param contrastColorUtilClass 宿主的系统灰度检测类
     * @return 适配后的位图，无法适配时返回 null
     */
    fun <T : Any> renderMcsIcon(
        context: Context,
        packageName: String,
        @Px sizePx: Int,
        moduleResources: () -> Resources,
        contrastColorUtilClass: () -> Class<T>
    ): Bitmap? = safeOfNull {
        val source = context.packageManager.getApplicationIcon(packageName)
        val isGrayscale = isGrayscaleIcon(context, source, contrastColorUtilClass)
        val customTriple = resolveCustomIcon(context, isGrayscale, packageName, moduleResources)
        val drawable = when {
            ConfigData.isEnableNotifyIconForceAppIcon -> context.launcherIconOf(packageName)
            (customTriple.first != null && customTriple.third.not()) || isGrayscale ->
                createNotificationIcon(context, customTriple, source, 0, sizePx)
            else -> null
        } ?: return@safeOfNull null
        drawable.toBitmap(sizePx, sizePx)
    }

    /**
     * 锁屏岛使用适配图标，同时保留系统构造时传入的原始图标，供息屏关闭适配时使用
     * @param original 系统原始图标，可能为空
     * @param icon 锁屏岛的适配图标
     */
    class LockScreenIconDrawable(val original: Drawable?, icon: Drawable) : DrawableWrapper(icon)

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
        @Px private val cornerRadiusPx: Float,
        private val paddingRatio: Float,
        @Px private val referenceSidePx: Float = 0f
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