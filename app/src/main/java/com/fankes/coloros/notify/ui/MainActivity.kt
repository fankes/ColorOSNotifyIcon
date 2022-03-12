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

package com.fankes.coloros.notify.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.view.isVisible
import com.fankes.coloros.notify.BuildConfig
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.hook.HookConst.ENABLE_ANDROID12_STYLE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_HIDE_ICON
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE
import com.fankes.coloros.notify.hook.HookConst.ENABLE_MODULE_LOG
import com.fankes.coloros.notify.hook.HookConst.ENABLE_NOTIFY_ICON_FIX
import com.fankes.coloros.notify.hook.HookConst.REMOVE_CHANGECP_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DEV_NOTIFY
import com.fankes.coloros.notify.hook.HookConst.REMOVE_DNDALERT_NOTIFY
import com.fankes.coloros.notify.ui.base.BaseActivity
import com.fankes.coloros.notify.utils.factory.*
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.highcapable.yukihookapi.hook.factory.modulePrefs
import com.highcapable.yukihookapi.hook.xposed.YukiHookModuleStatus

class MainActivity : BaseActivity() {

    companion object {

        /** 模块版本 */
        private const val moduleVersion = BuildConfig.VERSION_NAME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /** 设置文本 */
        findViewById<TextView>(R.id.main_text_version).text = "模块版本：$moduleVersion"
        findViewById<TextView>(R.id.main_text_coloros_version).text = "系统版本：$colorOSVersion"
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
            YukiHookModuleStatus.isActive() -> {
                findViewById<LinearLayout>(R.id.main_lin_status).setBackgroundResource(R.drawable.bg_green_round)
                findViewById<ImageFilterView>(R.id.main_img_status).setImageResource(R.mipmap.ic_success)
                findViewById<TextView>(R.id.main_text_status).text = "模块已激活"
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
        /** 初始化 View */
        val moduleEnableSwitch = findViewById<SwitchCompat>(R.id.module_enable_switch)
        val moduleEnableLogSwitch = findViewById<SwitchCompat>(R.id.module_enable_log_switch)
        val devNotifyConfigItem = findViewById<View>(R.id.config_item_dev)
        val a12StyleConfigItem = findViewById<View>(R.id.config_item_a12)
        val notifyIconConfigItem = findViewById<View>(R.id.config_item_notify)
        val devNotifyConfigSwitch = findViewById<SwitchCompat>(R.id.remove_dev_n_enable_switch)
        val crcpNotifyConfigSwitch = findViewById<SwitchCompat>(R.id.remove_chargecp_n_enable_switch)
        val dndNotifyConfigSwitch = findViewById<SwitchCompat>(R.id.remove_dndalert_n_enable_switch)
        val a12StyleConfigSwitch = findViewById<SwitchCompat>(R.id.a12_style_enable_switch)
        val hideIconInLauncherSwitch = findViewById<SwitchCompat>(R.id.hide_icon_in_launcher_switch)
        val notifyIconFixSwitch = findViewById<SwitchCompat>(R.id.notify_icon_fix_switch)
        val notifyIconFixButton = findViewById<View>(R.id.config_notify_app_button)
        /** 获取 Sp 存储的信息 */
        devNotifyConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        a12StyleConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        notifyIconConfigItem.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        moduleEnableLogSwitch.isVisible = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        notifyIconFixButton.isVisible = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
        devNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_DEV_NOTIFY, default = true)
        crcpNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_CHANGECP_NOTIFY, default = false)
        dndNotifyConfigSwitch.isChecked = modulePrefs.getBoolean(REMOVE_DNDALERT_NOTIFY, default = false)
        a12StyleConfigSwitch.isChecked = modulePrefs.getBoolean(ENABLE_ANDROID12_STYLE, isUpperOfAndroidS)
        moduleEnableSwitch.isChecked = modulePrefs.getBoolean(ENABLE_MODULE, default = true)
        moduleEnableLogSwitch.isChecked = modulePrefs.getBoolean(ENABLE_MODULE_LOG, default = false)
        hideIconInLauncherSwitch.isChecked = modulePrefs.getBoolean(ENABLE_HIDE_ICON)
        notifyIconFixSwitch.isChecked = modulePrefs.getBoolean(ENABLE_NOTIFY_ICON_FIX, default = true)
        moduleEnableSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_MODULE, b)
            moduleEnableLogSwitch.isVisible = b
            notifyIconConfigItem.isVisible = b
            devNotifyConfigItem.isVisible = b
            a12StyleConfigItem.isVisible = b
            SystemUITool.showNeedRestartSnake(context = this)
        }
        moduleEnableLogSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_MODULE_LOG, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        hideIconInLauncherSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_HIDE_ICON, b)
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, "com.fankes.coloros.notify.Home"),
                if (b) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        notifyIconFixSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_NOTIFY_ICON_FIX, b)
            notifyIconFixButton.isVisible = b
            SystemUITool.showNeedRestartSnake(context = this)
        }
        devNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_DEV_NOTIFY, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        crcpNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_CHANGECP_NOTIFY, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        dndNotifyConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(REMOVE_DNDALERT_NOTIFY, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        a12StyleConfigSwitch.setOnCheckedChangeListener { btn, b ->
            if (!btn.isPressed) return@setOnCheckedChangeListener
            modulePrefs.putBoolean(ENABLE_ANDROID12_STYLE, b)
            SystemUITool.showNeedRestartSnake(context = this)
        }
        /** 通知图标优化名单按钮点击事件 */
        notifyIconFixButton.setOnClickListener { startActivity(Intent(this, ConfigureActivity::class.java)) }
        /** 重启按钮点击事件 */
        findViewById<View>(R.id.title_restart_icon).setOnClickListener { SystemUITool.restartSystemUI(context = this) }
        /** 恰饭！ */
        findViewById<View>(R.id.link_with_follow_me).setOnClickListener {
            openBrowser(url = "https://www.coolapk.com/u/876977", packageName = "com.coolapk.market")
        }
        /** 项目地址点击事件 */
        findViewById<View>(R.id.link_with_project_address).setOnClickListener {
            openBrowser(url = "https://github.com/fankes/ColorOSNotifyIcon")
        }
    }
}