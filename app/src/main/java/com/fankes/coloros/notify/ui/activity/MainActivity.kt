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
@file:Suppress("SetTextI18n")

package com.fankes.coloros.notify.ui.activity

import androidx.core.view.isVisible
import com.fankes.coloros.notify.BuildConfig
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.data.factory.bind
import com.fankes.coloros.notify.databinding.ActivityMainBinding
import com.fankes.coloros.notify.param.IconPackParams
import com.fankes.coloros.notify.ui.activity.base.BaseActivity
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.GithubReleaseTool
import com.fankes.coloros.notify.utils.tool.I18nWarnTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.fankes.coloros.notify.utils.tool.YukiPromoteTool
import com.highcapable.yukihookapi.YukiHookAPI

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {

        /** 窗口是否启动 */
        internal var isActivityLive = false

        /** 模块是否可用 */
        internal var isModuleRegular = false

        /** 模块是否有效 */
        internal var isModuleValied = false

        /** 模块版本 */
        private const val moduleVersion = BuildConfig.VERSION_NAME

        /** 预发布的版本标识 */
        private const val pendingFlag = ""
    }

    override fun onCreate() {
        /** 设置可用性 */
        isActivityLive = true
        /** 检查更新 */
        GithubReleaseTool.checkingForUpdate(context = this, moduleVersion) { version, function ->
            binding.mainTextReleaseVersion.apply {
                text = "点击更新 $version"
                isVisible = true
                setOnClickListener { function() }
            }
        }
        when {
            /** 判断是否为 ColorOS 系统 */
            isNotColorOS ->
                showDialog {
                    title = "不是 ColorOS 系统"
                    msg = "此模块专为 ColorOS 系统打造，当前无法识别你的系统为 ColorOS，所以模块无法工作。"
                    confirmButton(text = "查看支持的模块") {
                        openBrowser(url = "https://github.com/fankes/AndroidNotifyIconAdapt")
                        finish()
                    }
                    cancelButton(text = "退出") { finish() }
                    noCancelable()
                }
            /** 判断是否 Hook */
            YukiHookAPI.Status.isXposedModuleActive -> {
                if (IconPackParams(context = this).iconDatas.isEmpty() && ConfigData.isEnableNotifyIconFix)
                    showDialog {
                        title = "配置通知图标优化名单"
                        msg = "模块需要获取在线规则以更新“通知图标优化名单”，它现在是空的，这看起来是你第一次使用模块，请首先进行配置才可以使用相关功能。\n" +
                                "你可以随时在本页面下方找到“配置通知图标优化名单”手动前往。"
                        confirmButton(text = "前往") { navigate<ConfigureActivity>() }
                        cancelButton()
                        noCancelable()
                    }
                if (isNotNoificationEnabled && ConfigData.isEnableNotifyIconFix)
                    showDialog {
                        title = "模块的通知权限已关闭"
                        msg = "请开启通知权限，以确保你能收到通知优化图标在线规则的更新。"
                        confirmButton { openNotifySetting() }
                        cancelButton()
                        noCancelable()
                    }
                /** 推广、恰饭 */
                YukiPromoteTool.promote(context = this)
            }
            else ->
                showDialog {
                    title = "模块没有激活"
                    msg = "检测到模块没有激活，模块需要 Xposed 环境依赖，" +
                            "同时需要系统拥有 Root 权限，" +
                            "请自行查看本页面使用帮助与说明第二条。\n" +
                            "由于需要修改系统应用达到效果，模块不支持太极阴、应用转生。"
                    confirmButton(text = "我知道了")
                    noCancelable()
                }
        }
        I18nWarnTool.checkingOrShowing(context = this)
        binding.mainTextVersion.text = "模块版本：$moduleVersion $pendingFlag"
        binding.mainTextColorOsVersion.text = "系统版本：[$androidVersionCodeName] $colorOSFullVersion"
        /** 媒体通知自动展开仅支持 12.1 - 旧版本适配过于复杂已放弃 */
        if (colorOSNumberVersion != "V12.1") {
            binding.notifyMediaPanelAutoExpSwitch.isVisible = false
            binding.notifyMediaPanelAutoExpText.isVisible = false
        }
        binding.notifyIconAutoSyncText.text = ConfigData.notifyIconFixAutoTime
        binding.moduleEnableSwitch.bind(ConfigData.ENABLE_MODULE) {
            onInitialize {
                binding.moduleEnableLogSwitch.isVisible = it
                binding.notifyIconConfigItem.isVisible = it
                binding.devNotifyConfigItem.isVisible = it
                binding.notifyStyleConfigItem.isVisible = it
            }
            onChanged {
                reinitialize()
                refreshModuleStatus()
                SystemUITool.showNeedRestartSnake(context = this@MainActivity)
            }
        }
        binding.moduleEnableLogSwitch.bind(ConfigData.ENABLE_MODULE_LOG) {
            onChanged { SystemUITool.showNeedRestartSnake(context = this@MainActivity) }
        }
        binding.devNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_DEV_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        binding.crcpNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_CHANGE_COMPLETE_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        binding.dndNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_DND_ALERT_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        binding.colorIconCompatSwitch.bind(ConfigData.ENABLE_COLOR_ICON_COMPAT) {
            isAutoApplyChanges = false
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChanges()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = "启用兼容模式"
                    msg = "启用兼容模式可修复部分系统版本可能出现无法判定通知图标反色的问题，" +
                            "但是这也可能会导致新的问题，一般情况下不建议开启，确定要继续吗？\n\n" +
                            "如果系统界面刷新后通知图标颜色发生错误，请尝试重启一次系统界面。"
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        binding.md3StyleConfigSwitch.bind(ConfigData.ENABLE_MD3_NOTIFY_ICON_STYLE) {
            onInitialize { binding.notifyIconCustomCornerItem.isVisible = it && ConfigData.isEnableNotifyIconForceAppIcon.not() }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        binding.notifyIconForceAppIconSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FORCE_APP_ICON) {
            isAutoApplyChanges = false
            onInitialize { binding.notifyIconCustomCornerItem.isVisible = it.not() && ConfigData.isEnableMd3NotifyIconStyle }
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChangesAndReinitialize()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = "破坏性功能警告"
                    msg = "开启这个功能后，任何通知栏中的通知图标都会被强制替换为当前推送通知的 APP 的图标，" +
                            "某些系统级别的 APP 通知图标可能会显示异常或发生图标丢失。\n\n" +
                            "此功能仅面向一些追求图标美观度的用户，我们不推荐开启这个功能，且发生任何 BUG 都不会去修复，仍然继续开启吗？"
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        binding.notifyPanelConfigSwitch.bind(ConfigData.ENABLE_NOTIFY_PANEL_ALPHA) {
            onInitialize {
                binding.notifyPanelConfigTextPanel.isVisible = it
                binding.notifyPanelConfigWarnPanel.isVisible = it
                binding.notifyPanelConfigSeekbar.isVisible = it
            }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        binding.notifyMediaPanelAutoExpSwitch.bind(ConfigData.ENABLE_NOTIFY_MEDIA_PANEL_AUTO_EXP) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        binding.notifyIconFixSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX) {
            onInitialize {
                binding.notifyIconFixButton.isVisible = it
                binding.notifyIconFixPlaceholderItem.isVisible = it
                binding.notifyIconFixNotifyItem.isVisible = it
                binding.notifyIconAutoSyncItem.isVisible = it
            }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        binding.notifyIconFixPlaceholderSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_PLACEHOLDER) {
            isAutoApplyChanges = false
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChanges()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = "注意"
                    msg = "开启这个功能后，当发现未适配的彩色通知图标时，" +
                            "状态栏中显示的通知图标将会使用预置的占位符图标进行修补，" +
                            "通知栏中显示的通知图标保持原始图标不变。\n\n" +
                            "此功能的作用仅为临时修复破坏规范的通知图标，仍然继续开启吗？"
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        binding.notifyIconFixNotifySwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        binding.notifyIconAutoSyncSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_AUTO) {
            onInitialize { binding.notifyIconAutoSyncChildItem.isVisible = it }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true)
            }
        }
        binding.notifyPanelConfigSeekbar.bind(ConfigData.NOTIFY_PANEL_ALPHA_LEVEL, binding.notifyPanelConfigText, suffix = "%") {
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.notifyIconCustomCornerSeekbar.bind(ConfigData.NOTIFY_ICON_CORNER_SIZE, binding.notifyIconCustomCornerText, suffix = " dp") {
            SystemUITool.refreshSystemUI(context = this)
        }
        /** 通知图标优化名单按钮点击事件 */
        binding.notifyIconFixButton.setOnClickListener { navigate<ConfigureActivity>() }
        /** 自动更新在线规则修改时间按钮点击事件 */
        binding.notifyIconAutoSyncButton.setOnClickListener {
            showTimePicker(ConfigData.notifyIconFixAutoTime) {
                showDialog {
                    title = "每天 $it 自动更新"
                    msg = "设置保存后将在每天 $it 自动同步名单到最新云端数据，若数据已是最新则不会显示任何提示，否则会发送一条通知。\n\n" +
                            "请确保：\n\n" +
                            "1.模块没有被禁止前台以及后台联网权限\n" +
                            "2.模块没有被禁止被其它 APP 关联唤醒\n" +
                            "3.模块的系统通知权限已开启\n\n" +
                            "模块无需保持在后台运行，到达同步时间后会自动启动，如果到达时间后模块正在运行则会自动取消本次计划任务。"
                    confirmButton(text = "保存设置") {
                        ConfigData.notifyIconFixAutoTime = it
                        this@MainActivity.binding.notifyIconAutoSyncText.text = it
                        SystemUITool.refreshSystemUI(context, isRefreshCacheOnly = true)
                    }
                    cancelButton()
                    noCancelable()
                }
            }
        }
        /** 重启按钮点击事件 */
        binding.titleRestartIcon.setOnClickListener { SystemUITool.restartSystemUI(context = this) }
        /** 项目地址按钮点击事件 */
        binding.titleGithubIcon.setOnClickListener { openBrowser(url = "https://github.com/fankes/ColorOSNotifyIcon") }
        /** 恰饭！ */
        binding.linkWithFollowMe.setOnClickListener {
            openBrowser(url = "https://www.coolapk.com/u/876977", packageName = "com.coolapk.market")
        }
        /** 设置桌面图标显示隐藏 */
        binding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            hideOrShowLauncherIcon(b)
        }
    }

    /** 刷新模块状态 */
    private fun refreshModuleStatus() {
        binding.mainLinStatus.setBackgroundResource(
            when {
                YukiHookAPI.Status.isXposedModuleActive &&
                        (isModuleRegular.not() || isModuleValied.not() || ConfigData.isEnableModule.not()) -> R.drawable.bg_yellow_round
                YukiHookAPI.Status.isXposedModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isXposedModuleActive && ConfigData.isEnableModule -> R.drawable.ic_success
                else -> R.drawable.ic_warn
            }
        )
        binding.mainTextStatus.text = when {
            YukiHookAPI.Status.isXposedModuleActive && ConfigData.isEnableModule.not() -> "模块已停用"
            YukiHookAPI.Status.isXposedModuleActive && isModuleRegular.not() -> "模块已激活，请重启系统界面"
            YukiHookAPI.Status.isXposedModuleActive && isModuleValied.not() -> "模块已更新，请重启系统界面"
            YukiHookAPI.Status.isXposedModuleActive -> "模块已激活"
            else -> "模块未激活"
        }
        binding.mainTextApiWay.isVisible = YukiHookAPI.Status.isXposedModuleActive
        binding.mainTextApiWay.text = "Activated by ${YukiHookAPI.Status.Executor.name} API ${YukiHookAPI.Status.Executor.apiLevel}"
    }

    override fun onResume() {
        super.onResume()
        /** 刷新模块状态 */
        refreshModuleStatus()
        /** 检查模块激活状态 */
        SystemUITool.checkingActivated(context = this) { isValied ->
            isModuleRegular = true
            isModuleValied = isValied
            refreshModuleStatus()
        }
    }
}