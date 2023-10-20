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

## 发行渠道说明

- [Automatic Build on Commit](https://github.com/fankes/ColorOSNotifyIcon/actions/workflows/commit_ci.yml)

上述更新为代码 `commit` 后自动触发，具体更新内容可点击上方的文字前往 **GitHub Actions** 进行查看，本更新由开源的流程自动编译发布，
**不保证其稳定性**， 所发布的版本**仅供测试**，且不会特殊说明甚至可能会变更版本号或保持与当前稳定版相同的版本号。

如果你需要直接下载 CI 自动构建打包的安装包，请点击顶部的 `CI builds | Telegram` 标签加入 Telegram CI 自动构建频道。

- [Release](https://github.com/fankes/ColorOSNotifyIcon/releases)
- [Xposed-Modules-Repo](https://github.com/Xposed-Modules-Repo/com.fankes.coloros.notify/releases)
- [蓝奏云 **密码：al5u**](https://fankes.lanzouy.com/b030rvjyf)
- [酷安应用市场](https://www.coolapk.com/apk/com.fankes.coloros.notify)

上述更新为手动发布的稳定版，具体更新内容可点击上方的文字前往指定的发布页面查看，稳定版的更新将会同时发布到上述地址中，同步更新。

本模块发布地址仅限于上述所列出的地址，从其他非正规渠道下载到的版本或对您造成任何影响均与我们无关。

## 请勿用于非法用途

- 本模块完全开源免费，如果好用你可以打赏支持开发，但是请不要用于非法用途。

## 项目推广

如果你正在寻找一个可以自动管理 Gradle 项目依赖的 Gradle 插件，你可以了解一下 [SweetDependency](https://github.com/HighCapable/SweetDependency) 项目。

如果你正在寻找一个可以自动生成属性键值的 Gradle 插件，你可以了解一下 [SweetProperty](https://github.com/HighCapable/SweetProperty) 项目。

本项目同样使用了 **SweetDependency** 和 **SweetProperty**。

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