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
 * This file is created by fankes on 2022/3/26.
 */
package com.fankes.coloros.notify.service

import android.service.quicksettings.TileService
import com.fankes.coloros.notify.ui.activity.ConfigureActivity
import com.fankes.coloros.notify.utils.factory.navigate

class QuickStartTileService : TileService() {

    override fun onClick() {
        super.onClick()
        /** 启动通知图标优化列表窗口 */
        navigate<ConfigureActivity>()
    }
}