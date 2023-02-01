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
 * This file is Created by fankes on 2022/3/28.
 */
package com.fankes.coloros.notify.data

import com.fankes.coloros.notify.hook.HookConst
import com.fankes.coloros.notify.utils.factory.isUpperOfAndroidS
import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData

object DataConst {

    val ENABLE_MODULE = PrefsData("_enable_module", true)
    val ENABLE_MODULE_LOG = PrefsData("_enable_module_log", false)
    val ENABLE_COLOR_ICON_COMPAT = PrefsData("_color_icon_compat", false)
    val ENABLE_MD3_NOTIFY_ICON_STYLE = PrefsData("_notify_icon_md3_style", isUpperOfAndroidS)
    val ENABLE_NOTIFY_ICON_FIX = PrefsData("_notify_icon_fix", true)
    val ENABLE_NOTIFY_ICON_FORCE_APP_ICON = PrefsData("_notify_icon_force_app_icon", false)
    val ENABLE_NOTIFY_ICON_FIX_NOTIFY = PrefsData("_notify_icon_fix_notify", true)
    val REMOVE_DEV_NOTIFY = PrefsData("_remove_dev_notify", true)
    val REMOVE_CHANGECP_NOTIFY = PrefsData("_remove_charge_complete_notify", false)
    val REMOVE_DNDALERT_NOTIFY = PrefsData("_remove_dndalert_notify", false)
    val ENABLE_NOTIFY_ICON_FIX_AUTO = PrefsData("_enable_notify_icon_fix_auto", true)
    val ENABLE_NOTIFY_PANEL_ALPHA = PrefsData("_enable_notify_panel_alpha", false)
    val ENABLE_NOTIFY_MEDIA_PANEL_AUTO_EXP = PrefsData("_enable_notify_media_panel_auto_exp", false)
    val NOTIFY_ICON_CORNER = PrefsData("_notify_icon_corner", 10)
    val NOTIFY_PANEL_ALPHA = PrefsData("_notify_panel_alpha_pst", 75)
    val NOTIFY_ICON_DATAS = PrefsData("_notify_icon_datas", "")
    val NOTIFY_ICON_FIX_AUTO_TIME = PrefsData("_notify_icon_fix_auto_time", "07:00")

    val SOURCE_SYNC_WAY = PrefsData("_rule_source_sync_way", HookConst.TYPE_SOURCE_SYNC_WAY_1)
    val SOURCE_SYNC_WAY_CUSTOM_URL = PrefsData("_rule_source_sync_way_custom_url", "")
}