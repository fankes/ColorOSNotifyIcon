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
import androidx.core.view.isVisible
import com.fankes.coloros.notify.BuildConfig
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.databinding.ActivityMainBinding
import com.fankes.coloros.notify.hook.HookConst.ENABLE_ANDROID12_STYLE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_HIDE_ICON
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE_LOG
import com.fankes.coloros.notify.hook.HookConst.ENABLE_NOTIFY_ICON_FIX
import com.fankes.coloros.notify.hook.HookConst.ENABLE_NOTIFY_ICON_FIX_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_CHANGECP_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DEV_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DNDALERT_NOTIFY
import com.fankes.coloros.notify.param.IconPackParams
import com.fankes.coloros.notify.ui.activity.base.BaseActivity
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.GithubReleaseTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.yukihookapi.hook.factory.isXposedModuleActive
import com.highcapable.yukihookapi.hook.factory.modulePrefs

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {

        /** 模块版本 */
        private const val moduleVersion = BuildConfig.VERSION_NAME

        /** 预发布的版本标识 */
        private const val pendingFlag = "[pending]"
    }

    override fun onCreate() {
        /** 检查更新 */
        GithubReleaseTool.checkingForUpdate(context = this, moduleVersion) { version, function ->
            binding.mainTextReleaseVersion.apply {
                text = "点击更新 $version"
                isVisible = true
                setOnClickListener { function() }
            }
        }
        /** 设置文本 */
        binding.mainTextVersion.text = "模块版本：$moduleVersion $pendingFlag"
        binding.mainTextColorOsVersion.text = "系统版本：$colorOSFullVersion"
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
                binding.mainLinStatus.setBackgroundResource(R.drawable.bg_green_round)
                binding.mainImgStatus.setImageResource(R.mipmap.ic_success)
                binding.mainTextStatus.text = "模块已激活"
                if (IconPackParams(context = this).iconDatas.isEmpty()
                    && modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
                ) showDialog {
                    title = "配置通知图标优化名单"
                    msg = "模块需要获取在线规则以更新“通知图标优化名单”，它现在是空的，这看起来是你第一次使用模块，请首先进行配置才可以使用相关功能。\n" +
                            "你可以随时在本页面下方找到“配置通知图标优化名单”手动前往。"
                    confirmButton(text = "前往") { navigate<ConfigureActivity>() }
                    cancelButton()
                    noCancelable()
                }
                if (isNotNoificationEnabled && modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true))
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
        /** 获取 Sp 存储的信息 */
        binding.devNotifyConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        binding.a12StyleConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        binding.notifyIconConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        binding.notifyIconFixButton.isVisible = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
        binding.notifyIconFixNotifyItem.isVisible = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
        binding.devNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_DEV_NOTIFY, default = true)
        binding.crcpNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_CHANGECP_NOTIFY)
        binding.dndNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_DNDALERT_NOTIFY)
        binding.a12StyleConfigSwitch.isChecked = modulePrefs.getBoolean(ENABLE_ANDROID12_STYLE, isUpperOfAndroidS)
        binding.moduleEnableSwitch.isChecked = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        binding.moduleEnableLogSwitch.isChecked = modulePrefs.getBoolean(ENABLE_MODULE_LOG)
        binding.hideIconInLauncherSwitch.isChecked = modulePrefs.getBoolean(ENABLE_HIDE_ICON)
        binding.notifyIconFixSwitch.isChecked = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
        binding.notifyIconFixNotifySwitch.isChecked = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX_NOTIFY, default = true)
        binding.moduleEnableSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_MODULE, b)
            binding.moduleEnableLogSwitch.isVisible = b
            binding.notifyIconConfigItem.isVisible = b
            binding.devNotifyConfigItem.isVisible = b
            binding.a12StyleConfigItem.isVisible = b
            SystemUITool.showNeedRestartSnake(context = this)
        }
        binding.moduleEnableLogSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_MODULE_LOG, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_HIDE_ICON, b)
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, "com.fankes.coloros.notify.Home"),
                if (b) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        binding.notifyIconFixSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_NOTIFY_ICON_FIX, b)
            binding.notifyIconFixButton.isVisible = b
            binding.notifyIconFixNotifyItem.isVisible = b
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.notifyIconFixNotifySwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_NOTIFY_ICON_FIX_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.devNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_DEV_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.crcpNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_CHANGECP_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.dndNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_DNDALERT_NOTIFY, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        binding.a12StyleConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_ANDROID12_STYLE, b)
            SystemUITool.refreshSystemUI(context = this)
        }
        /** 通知图标优化名单按钮点击事件 */
        binding.notifyIconFixButton.setOnClickListener { navigate<ConfigureActivity>() }
        /** 重启按钮点击事件 */
        binding.titleRestartIcon.setOnClickListener { SystemUITool.restartSystemUI(context = this) }
        /** 项目地址按钮点击事件 */
        binding.titleGithubIcon.setOnClickListener { openBrowser(url = "https://github.com/fankes/ColorOSNotifyIcon") }
        /** 恰饭！ */
        binding.linkWithFollowMe.setOnClickListener {
            openBrowser(url = "https://www.coolapk.com/u/876977", packageName = "com.coolapk.market")
        }
    }
}