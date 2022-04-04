# ColorOS 通知图标增强

[![Blank](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/fankes/ColorOSNotifyIcon)
[![Blank](https://img.shields.io/badge/license-AGPL3.0-blue)](https://github.com/fankes/ColorOSNotifyIcon/blob/master/LICENSE)
[![Blank](https://img.shields.io/badge/version-v1.52-green)](https://github.com/fankes/ColorOSNotifyIcon/releases)
[![Blank](https://img.shields.io/github/downloads/fankes/ColorOSNotifyIcon/total?label=Release)](https://github.com/fankes/ColorOSNotifyIcon/releases)
[![Blank](https://img.shields.io/github/downloads/Xposed-Modules-Repo/com.fankes.coloros.notify/total?label=LSPosed%20Repo&logo=Android&style=flat&labelColor=F48FB1&logoColor=ffffff)](https://github.com/Xposed-Modules-Repo/com.fankes.coloros.notify/releases)
[![Telegram](https://img.shields.io/static/v1?label=Telegram&message=交流讨论&color=0088cc)](https://t.me/XiaofangInternet)
<br/><br/>
<img src="https://github.com/fankes/ColorOSNotifyIcon/blob/master/app/src/main/ic_launcher-playstore.png" width = "100" height = "100"/>
<br/>
Optimize notification icons for ColorOS and adapt to native notification icon specifications.<br/>
为 ColorOS 优化通知图标以及适配原生通知图标规范，理论支持 OxygenOS 和 RealmeUI。

# Developer

[酷安 @星夜不荟](http://www.coolapk.com/u/876977)

# 适配说明

- 此模块仅支持 <b>LSPosed</b>(作用域“系统界面”)、<b>~~EdXposed(随时停止支持)~~</b>、不支持<b>太极、无极</b>
- 目前仅在 ColorOS 12 for OnePlus 上测试通过，如有问题请提交 `issues`
- 建议在不低于 ColorOS 11 的版本上使用

# 请勿用于非法用途

- 本模块完全开源免费，如果好用你可以打赏支持开发，但是请不要用于非法用途。
- 本模块发布地址仅有 [Xposed-Modules-Repo](https://github.com/Xposed-Modules-Repo/com.fankes.coloros.notify/releases)、
  [Release](https://github.com/fankes/ColorOSNotifyIcon/releases)
  及 [蓝奏云](https://fankes.lanzouy.com/b030rvjyf)，从其他非正规渠道下载到的版本或对您造成任何影响均与我们无关。

# 贡献通知图标优化名单

此项目是 `AndroidNotifyIconAdapt` 项目的一部分，详情请参考下方。<br/>

- [Android 通知图标规范适配计划](https://github.com/fankes/AndroidNotifyIconAdapt)

# 历史背景

继 MIUI 之后的第二大系统 ColorOS 虽然支持原生通知图标，但是第三方推送五颜六色的图标系统并没有做适配，甚至系统自己的图标都是彩色的，极其不友好。<br/>
而且从 ColorOS 12 开始，原生图标丢失了着色属性，这也是一种对原生 Android 生态的破坏。

# 捐赠支持

- 工作不易，无意外情况此项目将继续维护下去，提供更多可能，欢迎打赏。<br/><br/>
  <img src="https://github.com/fankes/YuKiHookAPI/blob/master/img-src/wechat_code.jpg" width = "200" height = "200"/>

# 许可证

- [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html)

```
Copyright (C) 2019-2022 Fankes Studio(qzmmcn@163.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

Powered by [YukiHookAPI](https://github.com/fankes/YukiHookAPI)<br/><br/>
版权所有 © 2019-2022 Fankes Studio(qzmmcn@163.com)