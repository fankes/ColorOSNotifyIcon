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

import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import com.fankes.coloros.notify.BuildConfig
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.data.DataConst
import com.fankes.coloros.notify.databinding.ActivityMainBinding
import com.fankes.coloros.notify.param.IconPackParams
import com.fankes.coloros.notify.ui.activity.base.BaseActivity
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.GithubReleaseTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.yukihookapi.hook.factory.isXposedModuleActive
import com.highcapable.yukihookapi.hook.factory.modulePrefs
import com.highcapable.yukihookapi.hook.xposed.YukiHookModuleStatus

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {

        /** 窗口是否启动 */
        internal var isActivityLive = false

        /** 模块版本 */
        private const val moduleVersion = BuildConfig.VERSION_NAME

        /** 预发布的版本标识 */
        private const val pendingFlag = ""
    }

    /** 模块是否可用 */
    private var isModuleRegular = false

    /** 模块是否有效 */
    private var isModuleValied = false

    override fun onCreate() {
        /** 设置可用性 */
        isActivityLive = true
        /** 设置文本 */
        binding.mainTextVersion.text = "模块版本：$moduleVersion $pendingFlag"
        binding.mainTextColorOsVersion.text = "系统版本：$colorOSFullVersion"
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
                    msg = "此模块专为 ColorOS 系统打造，当前无法识别你的系统为 ColorOS，所以模块无法工作。\n" +
                            "如有问题请联系 酷安 @星夜不荟"
                    confirmButton(text = "退出") { finish() }
                    noCancelable()
                }
            /** 判断是否 Hook */
            isXposedModuleActive -> {
                if (IconPackParams(context = this).iconDatas.isEmpty() && modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX))
                    showDialog {
                        title = "配置通知图标优化名单"
                        msg = "模块需要获取在线规则以更新“通知图标优化名单”，它现在是空的，这看起来是你第一次使用模块，请首先进行配置才可以使用相关功能。\n" +
                                "你可以随时在本页面下方找到“配置通知图标优化名单”手动前往。"
                        confirmButton(text = "前往") { navigate<ConfigureActivity>() }
                        cancelButton()
                        noCancelable()
                    }
                if (isNotNoificationEnabled && modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX))
                    showDialog {
                        title = "模块的通知权限已关闭"
                        msg = "请开启通知权限，以确保你能收到通知优化图标在线规则的更新。"
                        confirmButton { openNotifySetting() }
                        cancelButton()
                        noCancelable()
                    }
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
        var notifyIconAutoSyncTime = modulePrefs.get(DataConst.NOTIFY_ICON_FIX_AUTO_TIME)
        binding.devNotifyConfigItem.isVisible = modulePrefs.get(DataConst.ENABLE_MODULE)
        binding.a12StyleConfigItem.isVisible = modulePrefs.get(DataConst.ENABLE_MODULE)
        binding.notifyIconConfigItem.isVisible = modulePrefs.get(DataConst.ENABLE_MODULE)
        binding.notifyIconFixButton.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX)
        binding.notifyIconFixNotifyItem.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX)
        binding.notifyIconAutoSyncItem.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX)
        binding.notifyIconAutoSyncChildItem.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX_AUTO)
        binding.notifyPanelConfigSeekbar.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_PANEL_ALPHA)
        binding.notifyPanelConfigText.isVisible = modulePrefs.get(DataConst.ENABLE_NOTIFY_PANEL_ALPHA)
        binding.devNotifyConfigSwitch.isChecked = modulePrefs.get(DataConst.REMOVE_DEV_NOTIFY)
        binding.crcpNotifyConfigSwitch.isChecked = modulePrefs.get(DataConst.REMOVE_CHANGECP_NOTIFY)
        binding.dndNotifyConfigSwitch.isChecked = modulePrefs.get(DataConst.REMOVE_DNDALERT_NOTIFY)
        binding.a12StyleConfigSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_ANDROID12_STYLE)
        binding.moduleEnableSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_MODULE)
        binding.moduleEnableLogSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_MODULE_LOG)
        binding.hideIconInLauncherSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_HIDE_ICON)
        binding.notifyIconFixSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX)
        binding.notifyIconFixNotifySwitch.isChecked = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX_NOTIFY)
        binding.notifyIconAutoSyncSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_NOTIFY_ICON_FIX_AUTO)
        binding.notifyPanelConfigSwitch.isChecked = modulePrefs.get(DataConst.ENABLE_NOTIFY_PANEL_ALPHA)
        binding.notifyPanelConfigSeekbar.progress = modulePrefs.get(DataConst.NOTIFY_PANEL_ALPHA)
        binding.notifyPanelConfigText.text = "当前 - ${modulePrefs.get(DataConst.NOTIFY_PANEL_ALPHA)}"
        binding.notifyIconAutoSyncText.text = notifyIconAutoSyncTime
        binding.moduleEnableSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_MODULE, b)
            binding.moduleEnableLogSwitch.isVisible = b
            binding.notifyIconConfigItem.isVisible = b
            binding.devNotifyConfigItem.isVisible = b
            binding.a12StyleConfigItem.isVisible = b
            SystemUITool.showNeedRestartSnake(context = this)
        }
        binding.moduleEnableLogSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_MODULE_LOG, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_HIDE_ICON, b)
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, "com.fankes.coloros.notify.Home"),
                if (b) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        binding.notifyIconFixSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_NOTIFY_ICON_FIX, b)
            binding.notifyIconFixButton.isVisible = b
            binding.notifyIconFixNotifyItem.isVisible = b
            binding.notifyIconAutoSyncItem.isVisible = b
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.notifyIconFixNotifySwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_NOTIFY_ICON_FIX_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this, isRefreshCacheOnly = true)
        }
        binding.devNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.REMOVE_DEV_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this, isRefreshCacheOnly = true)
        }
        binding.crcpNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.REMOVE_CHANGECP_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this, isRefreshCacheOnly = true)
        }
        binding.dndNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.REMOVE_DNDALERT_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this, isRefreshCacheOnly = true)
        }
        binding.a12StyleConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_ANDROID12_STYLE, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.notifyPanelConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_NOTIFY_PANEL_ALPHA, b)
            binding.notifyPanelConfigText.isVisible = b
            binding.notifyPanelConfigSeekbar.isVisible = b
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.notifyIconAutoSyncSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            modulePrefs.put(DataConst.ENABLE_NOTIFY_ICON_FIX_AUTO, b)
            binding.notifyIconAutoSyncChildItem.isVisible = b
            SystemUITool.refreshSystemUI(context = this, isRefreshCacheOnly = true)
        }
        binding.notifyPanelConfigSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.notifyPanelConfigText.text = "当前 - $progress"
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                modulePrefs.put(DataConst.NOTIFY_PANEL_ALPHA, seekBar.progress)
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        })
        /** 通知图标优化名单按钮点击事件 */
        binding.notifyIconFixButton.setOnClickListener { navigate<ConfigureActivity>() }
        /** 自动更新在线规则修改时间按钮点击事件 */
        binding.notifyIconAutoSyncButton.setOnClickListener {
            showTimePicker(notifyIconAutoSyncTime) {
                showDialog {
                    title = "每天 $it 自动更新"
                    msg = "设置保存后将在每天 $it 自动同步名单到最新云端数据，若数据已是最新则不会显示任何提示，否则会发送一条通知。\n\n" +
                            "请确保：\n\n" +
                            "1.模块没有被禁止前台以及后台联网权限\n" +
                            "2.模块没有被禁止被其它 APP 关联唤醒\n" +
                            "3.模块的系统通知权限已开启\n\n" +
                            "模块无需保持在后台运行，到达同步时间后会自动启动，如果到达时间后模块正在运行则会自动取消本次计划任务。"
                    confirmButton(text = "保存设置") {
                        notifyIconAutoSyncTime = it
                        binding.notifyIconAutoSyncText.text = it
                        modulePrefs.put(DataConst.NOTIFY_ICON_FIX_AUTO_TIME, it)
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
    }

    /** 刷新模块状态 */
    private fun refreshModuleStatus() {
        binding.mainLinStatus.setBackgroundResource(
            when {
                isXposedModuleActive && (isModuleRegular.not() || isModuleValied.not()) -> R.drawable.bg_yellow_round
                isXposedModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                isXposedModuleActive -> R.mipmap.ic_success
                else -> R.mipmap.ic_warn
            }
        )
        binding.mainTextStatus.text =
            when {
                isXposedModuleActive && isModuleRegular.not() && modulePrefs.get(DataConst.ENABLE_MODULE).not() -> "模块已停用"
                isXposedModuleActive && isModuleRegular.not() -> "模块已激活，请重启系统界面"
                isXposedModuleActive && isModuleValied.not() -> "模块已更新，请重启系统界面"
                isXposedModuleActive -> "模块已激活"
                else -> "模块未激活"
            }
        binding.mainTextApiWay.isVisible = isXposedModuleActive
        binding.mainTextApiWay.text = "Activated by ${YukiHookModuleStatus.executorName} API ${YukiHookModuleStatus.executorVersion}"
    }

    override fun onResume() {
        super.onResume()
        /** 刷新模块状态 */
        refreshModuleStatus()
        /** 发送广播检查模块激活状态 */
        SystemUITool.checkingActivated(context = this) { isRegular, isValied ->
            isModuleRegular = isRegular
            isModuleValied = isValied
            refreshModuleStatus()
        }
    }
}