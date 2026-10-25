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
 * This file is created by fankes on 2022/2/26.
 */
@file:Suppress("SetTextI18n")

package com.fankes.coloros.notify.ui.activity

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.fankes.coloros.notify.R
import com.fankes.coloros.notify.const.ModuleVersion
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.data.factory.bind
import com.fankes.coloros.notify.databinding.ActivityMainBinding
import com.fankes.coloros.notify.databinding.PageAboutBinding
import com.fankes.coloros.notify.databinding.PageConfigBinding
import com.fankes.coloros.notify.databinding.PageHomeBinding
import com.fankes.coloros.notify.ui.activity.base.BaseActivity
import com.fankes.coloros.notify.utils.factory.androidVersionCodeName
import com.fankes.coloros.notify.utils.factory.colorOSFullVersion
import com.fankes.coloros.notify.utils.factory.colorOSNumberVersion
import com.fankes.coloros.notify.utils.factory.hideOrShowLauncherIcon
import com.fankes.coloros.notify.utils.factory.isLauncherIconShowing
import com.fankes.coloros.notify.utils.factory.isNotColorOS
import com.fankes.coloros.notify.utils.factory.isNotNoificationEnabled
import com.fankes.coloros.notify.utils.factory.navigate
import com.fankes.coloros.notify.utils.factory.openBrowser
import com.fankes.coloros.notify.utils.factory.openNotifySetting
import com.fankes.coloros.notify.utils.factory.showDialog
import com.fankes.coloros.notify.utils.factory.showTimePicker
import com.fankes.coloros.notify.utils.tool.GithubReleaseTool
import com.fankes.coloros.notify.utils.tool.IconRuleManagerTool
import com.fankes.coloros.notify.utils.tool.SystemUITool
import com.fankes.projectpromote.ProjectPromote
import com.highcapable.betterandroid.ui.extension.component.base.getColorCompat
import com.highcapable.betterandroid.ui.extension.component.runDelayed
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.runtime.YukiHookRuntime
import kotlinx.coroutines.Job

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {

        /** 窗口是否启动 */
        internal var isActivityLive = false

        /** 模块是否可用 */
        internal var isModuleRegular = false

        /** 模块是否有效 */
        internal var isModuleValied = false

        private const val GITHUB_PROFILE_URL = "https://github.com/fankes"
        private const val GITHUB_REPO_URL = "https://github.com/fankes/ColorOSNotifyIcon"
        private const val TELEGRAM_CI_URL = "https://t.me/ColorOSNotifyIcon_CI"
        private const val TELEGRAM_CHANNEL_URL = "https://t.me/XiaofangDaily"
        private const val TELEGRAM_GROUP_URL = "https://t.me/XiaofangInternet"
        private const val QQ_GROUP_URL = "https://qm.qq.com/cgi-bin/qm/qr?k=dp2h5YhWiga9WWb_Oh7kSHmx01X8I8ii&jump_from=webapi&authKey=Za5CaFP0lk7+Zgsk2KpoBD7sSaYbeXbsDgFjiWelOeH4VSionpxFJ7V0qQBSqvFM"
        private const val QQ_CHANNEL_URL = "https://pd.qq.com/s/44gcy28h"
        private const val COOLAPK_PROFILE_URL = "https://www.coolapk.com/u/876977"
        private const val COOLAPK_PACKAGE_NAME = "com.coolapk.market"
        private const val AFDIAN_URL = "https://afdian.com/a/fankes"
        private const val YUKIHOOK_URL = "https://github.com/HighCapable/YukiHook"
        private const val KAVAREF_URL = "https://github.com/HighCapable/KavaRef"
    }

    private val homeBinding by lazy { PageHomeBinding.inflate(layoutInflater, binding.mainViewPager, false) }
    private val configBinding by lazy { PageConfigBinding.inflate(layoutInflater, binding.mainViewPager, false) }
    private val aboutBinding by lazy { PageAboutBinding.inflate(layoutInflater, binding.mainViewPager, false) }

    override fun onCreate() {
        // 注册导出调试日志启动器，必须在 STARTED 之前，不能放进框架服务回调里
        SystemUITool.registerExportDebugLogsLauncher(activity = this)
        initPager()
        initHomeInfo()
        initListeners()
        if (YukiHook.Status.runtime == YukiHookRuntime.LIBXPOSED) {
            var unavailableJob: Job? = null
            YukiHook.Status.observeFrameworkService(lifecycle) { available ->
                // 第一次回调可能是 false，稍后连上会再回调 true，所以 false 先等一会
                unavailableJob?.cancel()
                if (available) onCreate(available = true)
                else unavailableJob = runDelayed(500) { onCreate(available = false) }
            }
        } else onCreate(available = true)
    }

    private fun onCreate(available: Boolean) {
        initConfigBindings()
        // 设置可用性
        isActivityLive = true
        when {
            // 判断是否为 ColorOS 系统
            isNotColorOS ->
                showDialog {
                    title = getString(R.string.common_not_coloros)
                    msg = getString(R.string.main_not_coloros_msg)
                    confirmButton(text = getString(R.string.main_view_supported_modules)) {
                        openBrowser(url = IconRuleManagerTool.ANIP_REPOSITORY_URL)
                        finish()
                    }
                    cancelButton(text = getString(R.string.common_exit)) { finish() }
                    noCancelable()
                }
            else -> {
                // 判断是否 Hook
                if (available && YukiHook.Status.isModuleActive) {
                    if (IconRuleManagerTool.hasCachedResources(this).not() && ConfigData.isEnableNotifyIconFix)
                        showDialog {
                            title = getString(R.string.main_config_optimize_list_title)
                            msg = getString(R.string.main_config_optimize_list_first_msg)
                            confirmButton(text = getString(R.string.common_go)) { navigate<ConfigureActivity>() }
                            cancelButton()
                            noCancelable()
                        }
                    if (isNotNoificationEnabled && ConfigData.isEnableNotifyIconFix)
                        showDialog {
                            title = getString(R.string.main_notify_permission_off)
                            msg = getString(R.string.main_notify_permission_off_msg)
                            confirmButton { openNotifySetting() }
                            cancelButton()
                            noCancelable()
                        }
                    // 推广、恰饭
                    ProjectPromote.show(activity = this, ModuleVersion.toString())
                } else
                    showDialog {
                        title = getString(R.string.main_module_not_active)
                        msg = getString(R.string.main_module_not_active_msg)
                        confirmButton(text = getString(R.string.common_got_it))
                        noCancelable()
                    }
            }
        }
        // 框架服务状态确定后同步刷新模块状态，否则需要切换后台触发 onResume 才会更新
        checkModuleStatus()
    }

    /** 初始化主页的版本文本、CI 自动构建标识、检查更新和各项设置的初始状态，它们不依赖模块是否激活 */
    private fun initHomeInfo() {
        // 检查更新
        GithubReleaseTool.checkingForUpdate(context = this, ModuleVersion.NAME) { version, function ->
            homeBinding.mainTextReleaseVersion.apply {
                text = getString(R.string.main_click_to_update, version)
                isVisible = true
                setOnClickListener { function() }
            }
        }
        homeBinding.mainTextVersion.text = getString(R.string.main_module_version, ModuleVersion.NAME, ModuleVersion.CODE)
        // 设置 CI 自动构建标识
        if (ModuleVersion.isCiMode)
            homeBinding.mainTextReleaseVersion.apply {
                text = "CI ${ModuleVersion.GITHUB_COMMIT_ID}"
                isVisible = true
                setOnClickListener {
                    showDialog {
                        title = getString(R.string.main_ci_title)
                        msg = getString(R.string.main_ci_msg, ModuleVersion.GITHUB_COMMIT_ID)
                        confirmButton(text = getString(R.string.common_got_it))
                        noCancelable()
                    }
                }
            }
        homeBinding.mainTextColorOsVersion.text = getString(R.string.main_system_version_detail, androidVersionCodeName, colorOSFullVersion)
        // 媒体通知自动展开仅支持 12.1 - 旧版本适配过于复杂已放弃
        if (colorOSNumberVersion != "V12.1") {
            configBinding.notifyMediaPanelAutoExpSwitch.isVisible = false
            configBinding.notifyMediaPanelAutoExpText.isVisible = false
        }
        // 通知面板背景透明度功能仅支持 ColorOS 12、12.1、13、13.1
        configBinding.notifyPanelConfigItem.isVisible = colorOSNumberVersion.let { it == "V12" || it == "V12.1" || it == "V13" || it == "V13.1" }
    }

    /** 初始化读写偏好设置的状态文本和开关绑定 */
    private fun initConfigBindings() {
        configBinding.notifyIconAutoSyncText.text = ConfigData.notifyIconFixAutoTime
        homeBinding.moduleEnableSwitch.bind(ConfigData.ENABLE_MODULE) {
            onInitialize {
                homeBinding.moduleEnableLogItem.isVisible = it
                homeBinding.expAllDebugLogButton.isVisible = it && ConfigData.isEnableModuleLog
                configBinding.notifyIconConfigItem.isVisible = it
                configBinding.devNotifyConfigItem.isVisible = it
                configBinding.notifyStyleConfigItem.isVisible = it
            }
            onChanged {
                reinitialize()
                refreshModuleStatus()
                SystemUITool.showNeedRestartSnake(context = this@MainActivity)
            }
        }
        homeBinding.moduleEnableLogSwitch.bind(ConfigData.ENABLE_MODULE_LOG) {
            onInitialize { homeBinding.expAllDebugLogButton.isVisible = it && ConfigData.isEnableModule }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true)
            }
        }
        configBinding.devNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_DEV_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        configBinding.crcpNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_CHANGE_COMPLETE_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        configBinding.dndNotifyConfigSwitch.bind(ConfigData.ENABLE_REMOVE_DND_ALERT_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        configBinding.colorIconCompatSwitch.bind(ConfigData.ENABLE_COLOR_ICON_COMPAT) {
            isAutoApplyChanges = false
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChanges()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = getString(R.string.main_compat_mode_title)
                    msg = getString(R.string.main_compat_mode_msg)
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        configBinding.md3StyleConfigSwitch.bind(ConfigData.ENABLE_MD3_NOTIFY_ICON_STYLE) {
            onInitialize { configBinding.notifyIconCustomCornerItem.isVisible = it && ConfigData.isEnableNotifyIconForceAppIcon.not() }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        configBinding.notifyIconForceSystemColorSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FORCE_SYSTEM_COLOR) {
            isAutoApplyChanges = false
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChangesAndReinitialize()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = getString(R.string.main_destructive_warn)
                    msg = getString(R.string.main_system_default_tint_msg)
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        configBinding.notifyIconForceAppIconSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FORCE_APP_ICON) {
            isAutoApplyChanges = false
            onInitialize {
                configBinding.notifyIconForceSystemColorItem.isVisible = it.not()
                configBinding.notifyIconCustomCornerItem.isVisible = it.not() && ConfigData.isEnableMd3NotifyIconStyle
            }
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChangesAndReinitialize()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = getString(R.string.main_destructive_warn)
                    msg = getString(R.string.main_force_app_icon_msg)
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        configBinding.notifyPanelConfigSwitch.bind(ConfigData.ENABLE_NOTIFY_PANEL_ALPHA) {
            onInitialize {
                configBinding.notifyPanelConfigTextPanel.isVisible = it
                configBinding.notifyPanelConfigWarnPanel.isVisible = it
                configBinding.notifyPanelConfigSeekbar.isVisible = it
            }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        configBinding.notifyMediaPanelAutoExpSwitch.bind(ConfigData.ENABLE_NOTIFY_MEDIA_PANEL_AUTO_EXP) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        configBinding.notifyIconFixSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX) {
            onInitialize {
                configBinding.notifyIconFixButton.isVisible = it
                configBinding.notifyIconFixPlaceholderItem.isVisible = it
                configBinding.notifyIconFixNotifyItem.isVisible = it
                configBinding.notifyIconAutoSyncItem.isVisible = it
            }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity)
            }
        }
        configBinding.notifyIconFixPlaceholderSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_PLACEHOLDER) {
            isAutoApplyChanges = false
            onChanged {
                /** 应用更改并刷新系统界面 */
                fun applyChangesAndRefresh() {
                    applyChanges()
                    SystemUITool.refreshSystemUI(context = this@MainActivity)
                }
                if (it) showDialog {
                    title = getString(R.string.common_notice)
                    msg = getString(R.string.main_placeholder_icon_msg)
                    confirmButton { applyChangesAndRefresh() }
                    cancelButton { cancelChanges() }
                    noCancelable()
                } else applyChangesAndRefresh()
            }
        }
        configBinding.notifyIconFixNotifySwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_NOTIFY) {
            onChanged { SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true) }
        }
        configBinding.notifyIconAutoSyncSwitch.bind(ConfigData.ENABLE_NOTIFY_ICON_FIX_AUTO) {
            onInitialize { configBinding.notifyIconAutoSyncChildItem.isVisible = it }
            onChanged {
                reinitialize()
                SystemUITool.refreshSystemUI(context = this@MainActivity, isRefreshCacheOnly = true)
            }
        }
        configBinding.notifyPanelConfigSeekbar.bind(ConfigData.NOTIFY_PANEL_ALPHA_LEVEL, configBinding.notifyPanelConfigText, suffix = "%") {
            SystemUITool.refreshSystemUI(context = this)
        }
        configBinding.notifyIconCustomCornerSeekbar.bind(ConfigData.NOTIFY_ICON_CORNER_SIZE, configBinding.notifyIconCustomCornerText, suffix = " dp") {
            SystemUITool.refreshSystemUI(context = this)
        }
    }

    /** 初始化按钮的点击事件，它们不依赖模块是否激活 */
    private fun initListeners() {
        // 导出全部日志按钮点击事件
        homeBinding.expAllDebugLogButton.setOnClickListener { SystemUITool.obtainAndExportDebugLogs(context = this) }
        // 通知图标优化名单按钮点击事件
        configBinding.notifyIconFixButton.setOnClickListener { navigate<ConfigureActivity>() }
        // 自动更新在线规则修改时间按钮点击事件
        configBinding.notifyIconAutoSyncButton.setOnClickListener {
            showTimePicker(ConfigData.notifyIconFixAutoTime) {
                showDialog {
                    title = getString(R.string.main_auto_update_title, it)
                    msg = getString(R.string.main_auto_update_msg, it)
                    confirmButton(text = getString(R.string.main_save_settings)) {
                        ConfigData.notifyIconFixAutoTime = it
                        this@MainActivity.configBinding.notifyIconAutoSyncText.text = it
                        SystemUITool.refreshSystemUI(context, isRefreshCacheOnly = true)
                    }
                    cancelButton()
                    noCancelable()
                }
            }
        }
        // 重启按钮点击事件
        binding.titleRestartIcon.setOnClickListener { SystemUITool.restartSystemUI(context = this) }
        // 开发者名片与项目地址
        aboutBinding.aboutProfileItem.setOnClickListener { openBrowser(url = GITHUB_PROFILE_URL) }
        aboutBinding.aboutRepoItem.setOnClickListener { openBrowser(url = GITHUB_REPO_URL) }
        // 联系方式
        aboutBinding.linkTelegramCi.setOnClickListener { openBrowser(url = TELEGRAM_CI_URL) }
        aboutBinding.linkTelegramChannel.setOnClickListener { openBrowser(url = TELEGRAM_CHANNEL_URL) }
        aboutBinding.linkTelegramGroup.setOnClickListener { openBrowser(url = TELEGRAM_GROUP_URL) }
        aboutBinding.linkQqGroup.setOnClickListener { openBrowser(url = QQ_GROUP_URL) }
        aboutBinding.linkQqChannel.setOnClickListener { openBrowser(url = QQ_CHANNEL_URL) }
        aboutBinding.linkFollowMe.setOnClickListener {
            openBrowser(url = COOLAPK_PROFILE_URL, packageName = COOLAPK_PACKAGE_NAME)
        }
        // 恰饭！
        aboutBinding.linkSupportMe.setOnClickListener { openBrowser(url = AFDIAN_URL) }
        // 基于项目构建
        aboutBinding.linkAnip.setOnClickListener { openBrowser(url = IconRuleManagerTool.ANIP_REPOSITORY_URL) }
        aboutBinding.linkYukihook.setOnClickListener { openBrowser(url = YUKIHOOK_URL) }
        aboutBinding.linkKavaref.setOnClickListener { openBrowser(url = KAVAREF_URL) }
        // 设置桌面图标显示隐藏
        homeBinding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
        homeBinding.hideIconInLauncherSwitch.setOnCheckedChangeListener { btn, b ->
            if (btn.isPressed.not()) return@setOnCheckedChangeListener
            hideOrShowLauncherIcon(b)
        }
    }

    /** 初始化底栏和页面 (主页 | 配置 | 关于) */
    private fun initPager() {
        val pages = listOf(homeBinding.root, configBinding.root, aboutBinding.root)
        val navIds = listOf(R.id.nav_home, R.id.nav_config, R.id.nav_about)
        binding.mainViewPager.adapter = object : PagerAdapter() {
            override fun getCount() = pages.size
            override fun isViewFromObject(view: View, `object`: Any) = view === `object`
            override fun instantiateItem(container: ViewGroup, position: Int) = pages[position].also { container.addView(it) }
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)
        }
        binding.mainViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                binding.mainBottomNav.selectedItemId = navIds[position]
            }
        })
        binding.mainBottomNav.setOnItemSelectedListener {
            binding.mainViewPager.currentItem = navIds.indexOf(it.itemId)
            true
        }
        // 重复点击当前页面的底栏图标时平滑回到顶部
        binding.mainBottomNav.setOnItemReselectedListener { pages[navIds.indexOf(it.itemId)].smoothScrollTo(0, 0) }
    }

    /** 刷新模块状态 */
    private fun refreshModuleStatus() {
        val isActive = YukiHook.Status.isModuleActive
        val isWarning = isActive && (isModuleRegular.not() || isModuleValied.not() || ConfigData.isEnableModule.not())
        homeBinding.mainLinStatus.setCardBackgroundColor(
            resources.getColorCompat(when {
                isWarning -> R.color.colorStatusWarningContainer
                isActive -> R.color.colorStatusActiveContainer
                else -> R.color.colorStatusInactiveContainer
            }, null)
        )
        homeBinding.mainImgStatus.setImageResource(
            when {
                isWarning -> R.drawable.ic_status_warn
                isActive -> R.drawable.ic_status_success
                else -> R.drawable.ic_status_error
            }
        )
        homeBinding.mainTextStatus.text = when {
            YukiHook.Status.isModuleActive && ConfigData.isEnableModule.not() -> getString(R.string.main_module_disabled)
            YukiHook.Status.isModuleActive && (isModuleRegular.not() || isModuleValied.not()) -> getString(R.string.main_module_not_fully_active)
            YukiHook.Status.isModuleActive -> getString(R.string.main_module_active)
            else -> getString(R.string.main_module_inactive)
        }
        homeBinding.mainTextApiWay.isVisible = YukiHook.Status.isModuleActive
        homeBinding.mainTextApiWay.text = "Activated by ${YukiHook.Status.Executor.name} API ${YukiHook.Status.Executor.apiVersion}"
    }

    /** 刷新并检查模块状态 */
    private fun checkModuleStatus() {
        // 刷新模块状态
        refreshModuleStatus()
        // 检查模块激活状态
        SystemUITool.checkingActivated(context = this) { isValied ->
            isModuleRegular = true
            isModuleValied = isValied
            refreshModuleStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        checkModuleStatus()
    }
}