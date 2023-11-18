# ColorOS 通知图标增强

[![GitHub license](https://img.shields.io/github/license/fankes/ColorOSNotifyIcon?color=blue)](https://github.com/fankes/ColorOSNotifyIcon/blob/master/LICENSE)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/fankes/ColorOSNotifyIcon/commit_ci.yml?label=CI%20builds)](https://github.com/fankes/ColorOSNotifyIcon/actions/workflows/commit_ci.yml)
[![GitHub release](https://img.shields.io/github/v/release/fankes/ColorOSNotifyIcon?display_name=release&logo=github&color=green)](https://github.com/fankes/ColorOSNotifyIcon/releases)
![GitHub all releases](https://img.shields.io/github/downloads/fankes/ColorOSNotifyIcon/total?label=downloads)
![GitHub all releases](https://img.shields.io/github/downloads/Xposed-Modules-Repo/com.fankes.coloros.notify/total?label=LSPosed%20downloads&labelColor=F48FB1)

[![Telegram CI](https://img.shields.io/badge/CI%20builds-Telegram-blue.svg?logo=telegram)](https://t.me/ColorOSNotifyIcon_CI)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram)](https://t.me/XiaofangInternet)
[![QQ](https://img.shields.io/badge/discussion-QQ-blue.svg?logo=tencent-qq&logoColor=red)](https://qm.qq.com/cgi-bin/qm/qr?k=dp2h5YhWiga9WWb_Oh7kSHmx01X8I8ii&jump_from=webapi&authKey=Za5CaFP0lk7+Zgsk2KpoBD7sSaYbeXbsDgFjiWelOeH4VSionpxFJ7V0qQBSqvFM)
[![QQ 频道](https://img.shields.io/badge/discussion-QQ%20频道-blue.svg?logo=tencent-qq&logoColor=red)](https://pd.qq.com/s/44gcy28h)

<img src="https://github.com/fankes/ColorOSNotifyIcon/blob/master/img-src/icon.png?raw=true" width = "100" height = "100" alt="LOGO"/>

Optimize notification icons for ColorOS and adapt to native notification icon specifications.

为 ColorOS 优化通知图标以及适配原生通知图标规范，理论支持 OxygenOS 和 RealmeUI。

## For Non-Chinese Users

This project will not be adapted i18n, please stay tuned for my new projects in the future.

## 项目迁移公告

由于本人同时维护 **MIUI** 与 **ColorOS** 两个系统需要同时维护两个模块，十分不方便，所以我决定在后期逐渐合并两个项目并解耦合为一个新项目并计划适配更多系统与设备，例如原生与类原生系统。

在新的项目确定后，会在这里添加新项目的链接，届时我会终止维护这个项目并建议大家转移到新项目。

## 适配说明

- 此模块仅支持 **LSPosed** (作用域“系统界面”)、**~~EdXposed(随时停止支持)~~**、不支持**太极、无极**

- 目前仅在 ColorOS 12、12.1、13 for OnePlus 上测试通过，如有问题请提交 `issues`

- 建议在不低于 ColorOS 11 的版本上使用

## 历史背景

继 MIUI 之后的第二大系统 ColorOS 虽然支持原生通知图标，但是第三方推送五颜六色的图标系统并没有做适配，甚至系统自己的图标都是彩色的，极其不友好。

而且从 ColorOS 12 开始，原生图标丢失了着色属性，这也是一种对原生 Android 生态的破坏。

## 贡献通知图标优化名单

此项目是 `AndroidNotifyIconAdapt` 项目的一部分，详情请参考下方。

- [Android 通知图标规范适配计划](https://github.com/fankes/AndroidNotifyIconAdapt)

## 发行渠道

| <img src="https://avatars.githubusercontent.com/in/15368?s=64&v=4" width = "30" height = "30" alt="LOGO"/> | [GitHub CI](https://github.com/fankes/ColorOSNotifyIcon/actions/workflows/commit_ci.yml) | CI 自动构建 (测试版) |
|------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|---------------|

| <img src="https://github.com/peter-iakovlev/Telegram/blob/public/Icon.png?raw=true" width = "30" height = "30" alt="LOGO"/> | [Telegram CI 频道](https://t.me/ColorOSNotifyIcon_CI) | CI 自动构建 (测试版) |
|-----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|---------------|

| <img src="https://avatars.githubusercontent.com/in/15368?s=64&v=4" width = "30" height = "30" alt="LOGO"/> | [GitHub Releases](https://github.com/fankes/ColorOSNotifyIcon/releases) | 正式版 (稳定版) |
|------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------|-----------|

| <img src="https://avatars.githubusercontent.com/u/78217009?s=200&v=4?raw=true" width = "30" height = "30" alt="LOGO"/> | [Xposed-Modules-Repo](https://github.com/Xposed-Modules-Repo/com.fankes.coloros.notify/releases) | 正式版 (稳定版) |
|------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|-----------|

| <img src="https://github.com/fankes/fankes/assets/37344460/82113d3c-aa7b-4dd1-95c7-cda650065c12" width = "30" height = "30" alt="LOGO"/> | [123 云盘 **(密码：al5u)**](https://www.123pan.com/s/5SlUVv-C8DBh.html) | 正式版 (稳定版) |
|------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|-----------|

| <img src="https://github.com/fankes/fankes/assets/37344460/3cd43efd-785e-411d-a5c3-a8c9dc02308a" width = "30" height = "30" alt="LOGO"/> | [酷安应用市场](https://www.coolapk.com/apk/com.fankes.coloros.notify) | 正式版 (稳定版) |
|------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|-----------|

本模块发布地址仅限于上述所列出的地址，从其他非正规渠道下载到的版本或对您造成任何影响均与我们无关。

## 请勿用于非法用途

- 本模块完全开源免费，如果好用你可以打赏支持开发，但是请不要用于非法用途。

## 项目推广

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>嘿，还请君留步！👋</h2>
    <h3>这里有 Android 开发工具、UI 设计、Gradle 插件、Xposed 模块和实用软件等相关项目。</h3>
    <h3>如果下方的项目能为你提供帮助，不妨为我点个 star 吧！</h3>
    <h3>所有项目免费、开源，遵循对应开源许可协议。</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README-zh-CN.md">→ 查看更多关于我的项目，请点击这里 ←</a></h1>
</div>

## 捐赠支持

工作不易，无意外情况此项目将继续维护下去，提供更多可能，欢迎打赏。

<img src="https://github.com/fankes/fankes/blob/main/img-src/payment_code.jpg?raw=true" width = "500" alt="Payment Code"/>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=fankes/ColorOSNotifyIcon&type=Date)

## 隐私政策

- [PRIVACY](https://github.com/fankes/ColorOSNotifyIcon/blob/master/PRIVACY.md)

## 许可证

- [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html)

```
Copyright (C) 2017-2023 Fankes Studio(qzmmcn@163.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

Powered by [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI)

版权所有 © 2017-2023 Fankes Studio(qzmmcn@163.com)