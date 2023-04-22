/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 2017-2023 Fankes Studio(qzmmcn@163.com)
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

import com.fankes.coloros.notify.const.PackageName
import com.fankes.coloros.notify.data.ConfigData
import com.fankes.coloros.notify.hook.entity.SystemUIHooker
import com.fankes.coloros.notify.utils.factory.isNotColorOS
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.loggerW
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            tag = "ColorOSNotifyIcon"
            isRecord = true
            elements(PRIORITY)
        }
        isDebug = false
        isEnablePrefsBridgeCache = false
    }

    override fun onHook() = encase {
        loadApp(PackageName.SYSTEMUI) {
            ConfigData.init(instance = this)
            when {
                isNotColorOS -> loggerW(msg = "Aborted Hook -> This System is not ColorOS")
                ConfigData.isEnableModule.not() -> loggerW(msg = "Aborted Hook -> Hook Closed")
                else -> loadHooker(SystemUIHooker)
            }
        }
    }
}