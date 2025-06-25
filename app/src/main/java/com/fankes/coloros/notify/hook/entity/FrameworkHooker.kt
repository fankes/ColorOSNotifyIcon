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
 * This file is created by Nep-Timeline on 2025/5/27.
 */
package com.fankes.coloros.notify.hook.entity

import android.app.Notification
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

/**
 * 系统框架核心 Hook 类
 */
object FrameworkHooker : YukiBaseHooker() {

    /** ColorOS 存在的类 - 旧版本不存在 */
    private val OplusNotificationFixHelperClass by lazyClassOrNull("com.android.server.notification.OplusNotificationFixHelper")

    override fun onHook() {
        /** 拦截 ColorOS 覆盖应用通知图标 */
        OplusNotificationFixHelperClass?.resolve()?.optional()?.firstMethodOrNull {
            name = "fixSmallIcon"
            parameters(Notification::class, String::class, String::class, Boolean::class)
        }?.hook()?.intercept()
    }
}