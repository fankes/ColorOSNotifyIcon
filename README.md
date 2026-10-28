# ColorOS Notification Icon Enhancer

[![GitHub license](https://img.shields.io/github/license/fankes/ColorOSNotifyIcon?color=blue&style=flat-square)](https://github.com/fankes/ColorOSNotifyIcon/blob/master/LICENSE)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/fankes/ColorOSNotifyIcon/commit_ci.yml?label=CI%20builds&style=flat-square)](https://github.com/fankes/ColorOSNotifyIcon/actions/workflows/commit_ci.yml)
[![GitHub release](https://img.shields.io/github/v/release/fankes/ColorOSNotifyIcon?display_name=release&logo=github&color=green&style=flat-square)](https://github.com/fankes/ColorOSNotifyIcon/releases)
![GitHub all releases](https://img.shields.io/github/downloads/fankes/ColorOSNotifyIcon/total?label=downloads&style=flat-square)
![GitHub all releases](https://img.shields.io/github/downloads/Xposed-Modules-Repo/com.fankes.coloros.notify/total?label=LSPosed%20downloads&labelColor=F48FB1&style=flat-square)

[![Telegram CI](https://img.shields.io/badge/CI%20builds-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/ColorOSNotifyIcon_CI)
[![Telegram Channel](https://img.shields.io/badge/channel-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/XiaofangDaily)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/XiaofangInternet)
[![QQ](https://img.shields.io/badge/discussion-QQ-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://qm.qq.com/cgi-bin/qm/qr?k=dp2h5YhWiga9WWb_Oh7kSHmx01X8I8ii&jump_from=webapi&authKey=Za5CaFP0lk7+Zgsk2KpoBD7sSaYbeXbsDgFjiWelOeH4VSionpxFJ7V0qQBSqvFM)
[![QQ Channel](https://img.shields.io/badge/discussion-QQ%20频道-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://pd.qq.com/s/44gcy28h)

<img src="img-src/icon.png" width = "100" height = "100" alt="LOGO"/>

English | [简体中文](README-zh-CN.md)

Optimize notification icons for ColorOS and adapt to stock notification icon specifications.

## Project Migration Notice

Maintaining two modules for both **HyperOS/MIUI** and **ColorOS** at the same time is very inconvenient, so I plan to gradually merge and decouple the
two
projects into a new one, with plans to support more systems and devices, such as AOSP and AOSP-like systems.

The new project is [Sumi](https://github.com/fankes/Sumi), which is currently in development. After the new project publishes its official
release, I will stop maintaining this project and recommend that everyone move to the new project.

Features of this module that are unrelated to notification icons (for example, removing the developer options warning notification and the
charging complete notification) will not be migrated to the new project.

## Compatibility Notes

- **LSPosed** (scope: "System UI") is recommended
- It has only been tested on ColorOS 11, 12, 12.1, 13 and 17. If you have problems,
  please [submit an issue](https://github.com/fankes/ColorOSNotifyIcon/issues)
- It is recommended to use ColorOS 11 or above

### Compatibility Changes

Because ColorOS 15 changed the system notification icon behavior and forcibly replaces icons with the colored app icon during icon initialization,
the "System Framework" scope has been added. If this scope is not checked automatically after the module is installed, please check it manually and
restart the system to fix this breakage. Systems older than ColorOS 15 do not need to check it.

Thanks to [Nep-Timeline](https://github.com/Nep-Timeline) for providing the solution.

## Background

ColorOS, the second-largest system after MIUI, supports native notification icons, but it has not been adapted for the colorful icons pushed by third
parties, and even the system's own icons are colored, which is extremely unfriendly.

Moreover, starting from ColorOS 12 the native icons lost their tint attribute, which is also a kind of damage to the native Android ecosystem. In
ColorOS 15 and later, the system even forcibly replaces icons with the colored app icon during icon initialization.

## Contribute to the ANIP

The notification icons of this project come from the ANIP project. [Click here](https://github.com/BetterAndroid/android-notification-icon-project)
for details.

## Release Channels

| <img src="https://avatars.githubusercontent.com/in/15368?s=64&v=4" width = "30" height = "30" alt="LOGO"/> | [GitHub CI](https://github.com/fankes/ColorOSNotifyIcon/actions/workflows/commit_ci.yml) | CI automatic builds (beta) |
|------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|----------------------------|

| <img src="https://github.com/peter-iakovlev/Telegram/blob/public/Icon.png?raw=true" width = "30" height = "30" alt="LOGO"/> | [Telegram CI Channel](https://t.me/ColorOSNotifyIcon_CI) | CI automatic builds (beta) |
|-----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|----------------------------|

| <img src="https://avatars.githubusercontent.com/in/15368?s=64&v=4" width = "30" height = "30" alt="LOGO"/> | [GitHub Releases](https://github.com/fankes/ColorOSNotifyIcon/releases) | Official releases (stable) |
|------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------|----------------------------|

| <img src="https://avatars.githubusercontent.com/u/78217009?s=200&v=4?raw=true" width = "30" height = "30" alt="LOGO"/> | [Xposed-Modules-Repo](https://github.com/Xposed-Modules-Repo/com.fankes.coloros.notify/releases) | Official releases (stable) |
|------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------|

The release addresses of this module are limited to those listed above. We are not responsible for any impact caused by versions downloaded from other
unofficial channels.

### Choose a Module Version by Scenario

The installation packages of this project come in two tracks: **modern** and **legacy**.

- Versions marked **modern** are for Xposed frameworks that support the libxposed API, such as LSPosed
- Versions marked **legacy** are for Xposed frameworks that support the Xposed Framework API, such as old LSPosed (≤ 1.9.0), EdXposed, etc.

The **modern** version supports automatic hot reload on LSPosed 2.1.0+. After the first installation, later module updates take effect without
restarting System UI.

## Notes

1. This software is free and developed out of interest, for learning and communication only. If you obtained it from an unofficial channel by paying
   for it, you may have been defrauded, and you are welcome to report suspicious behavior to us.
2. This software is licensed under the **GNU Affero General Public License (AGPL 3.0)**. According to the license:
    1. Any derivative work must use the same AGPLv3 license
    2. The complete source code must be provided when distributing this software or its modified versions
    3. The original copyright notice and license information must be retained
    4. No additional restrictions may be imposed to limit others' free use of this software
3. We encourage free distribution and improvement under the terms of AGPLv3, but please respect the author's attribution and do not impersonate the
   original author

## More Projects

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>Hey, please stay a moment! 👋</h2>
    <h3>If you find this project helpful, take a look at my other projects below!</h3>
    <h3>If these projects help you, consider giving me a follow or a star ⭐️!</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README.md">→ Click here to see more of my projects ←</a></h1>
</div>

## Star History

[![Star History Chart](https://api.star-history.com/chart?repos=fankes/ColorOSNotifyIcon&type=date&legend=top-left&sealed_token=6F-xnv_jLhf5xImaia70rtxXh8PS_8jLSvUa6ssR0Sn1KJjeF0zpbVsnuwCp8jXijJ7cA7r_s9uLWb7AoID_ZizuPnhEjtGe2LnLHoucO8QtlQrJbg_t0Q)](https://www.star-history.com/?repos=fankes%2FColorOSNotifyIcon&type=date&legend=top-left)

## License

- [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html)

```
Copyright (C) 2017 Fankes Studio(qzmmcn@163.com)

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

Powered by [YukiHook](https://github.com/HighCapable/YukiHook)

Copyright © 2017 Fankes Studio(qzmmcn@163.com)