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
 * This file is Created by fankes on 2022/1/24.
 */
package com.fankes.coloros.notify.param

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.fankes.coloros.notify.bean.IconDataBean
import com.fankes.coloros.notify.hook.HookConst.NOTIFY_ICON_DATAS
import com.fankes.coloros.notify.utils.bitmap
import com.fankes.coloros.notify.utils.safeOf
import com.fankes.coloros.notify.utils.safeOfNan
import com.highcapable.yukihookapi.hook.factory.modulePrefs
import com.highcapable.yukihookapi.hook.param.PackageParam
import org.json.JSONArray
import org.json.JSONObject

/**
 * 通知栏小图标适配类
 *
 * 国内 APP 不规范的图标将由这里完成其自定义单色小图标绘制
 * @param context 实例 - 二选一
 * @param param 实例 - 二选一
 */
class IconPackParams(private val context: Context? = null, private val param: PackageParam? = null) {

    companion object {

        /**
         * Android 11 系统默认图标
         * @return [Bitmap]
         */
        val android11IconBitmap by lazy {
            ("iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAAAXNSR0IArs4c6QAAAARzQklUCAgI\n" +
                    "CHwIZIgAAAPkSURBVHic7ZvfVdswFMY/9fS93qDZoNmg6QS4E8AGdAMYgQ1gg4QJkk5AOkGcCUgm\n" +
                    "+Pog27EdCa7+WDIn/J4gx77W/SzpyvdKwCefdFE5H06yAPCj/vefUuqQqy1JhCA5B3AFYFH/NAdQ\n" +
                    "WC4/ANjWf28APCultpZrozGaECQbx0sAs0BzFYAVgI1S6jm0baNDsiB5R/KV4/FK8i63r1ZIXpPc\n" +
                    "jSjAkB3J69x+t5AsEwtgEqTMLcJjRgGGPOYQoCC5zu25gTV1SE4iwpx5h8J77KjDtRNO4ZPkAsAS\n" +
                    "9jXAVDgA+K2U2khvEAtRq7zG9EVoOAD4JV2MfZFcRD3uXHvCEcBfh+vf429tU0oBYMmYcwbJF+H4\n" +
                    "3FKH09ng/jnJG5IHh7F+qO+ZD2zN6mdshXbWsUSQhsh7ga2C5Epga0XBmyR5L2xbWGilVl6C04KG\n" +
                    "5MMbth6m0MbhQyRhcuVhtyBZGWxV9BjTlPWynavdxviNwLhXw2v7C4M95/hf27IJO+TGx7CkNzh1\n" +
                    "Y8Nzuo0PyjmQfBK019orbOHzD2Q5hMqr1Se6zse0ZWNGy6RuE+I24sOl98e09RZG386EoJ5dU60e\n" +
                    "xUvgiBQ0RBBTj1gYfkuB10Tpef+ZjyYhrvzbEsT3wPtdhDjzsSdEHb5CE62+zEPCJ4CfDrfMhs8a\n" +
                    "9oi8KS//j6RHuL/A3vAYCpFrfmiYAXj3m6VLPfH5vMDePaLP8MTckhT1DOq0/jJ6C+hejwjqQTQv\n" +
                    "sxteqUsEps/wK8pTA1b7XbtfB22bUvapAPAEACQBveZ4q1ToY7+lHRpMlf31Z4HIL6rrc3eOCF3Q\n" +
                    "fERan6c4WWahK8TopfcJ0vrcCpFzk0Yuuj5f8tDolQaGQsSsQ0yd3lRwyT2ix1CIHImSXPSy70Mh\n" +
                    "nFPzH5jeS+8JURdM90mbk4f9sDhsmiMuoVec+WgS4hLmiTMfz4RQSq3gVn4PIUci6Fj72MMWPqUV\n" +
                    "rJSZ51jIq3OU1xJDS36p92K5V9MoKwLvGLcIPDZuReBOYyW9wjlnSHmROSb+tVXm2SgyFmGlCsrK\n" +
                    "7aRgozh1T1iO56uVpyAROg5shA98oc4wmzaTXXPcXfs2ROsi0T5L6glxC7f6ZHMAJWfRaA9gLkk6\n" +
                    "uW443QD4FtCwlBwBLKJuOAXaD7IS6VadIRwBlKMegarHuySs5qKiZ1XdR4yC8gk0JRvmKFRRHlpT\n" +
                    "ECdEBohRMu9QqZj7KFMX6m+TlIJU9P12SAH1RnGXXfiuHCjYAO/KmAdgS5wOwIZuFNvjdAB2lFRi\n" +
                    "yiPRjTCATsjYFmZHnIovjfMf90i0BOpQ18T87SXWXz+ZKv8BVnFXPlKejoIAAAAASUVORK5CYII=").bitmap
        }

        /**
         * Android 12 系统默认图标
         * @return [Bitmap]
         */
        val android12IconBitmap by lazy {
            ("iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAAAXNSR0IArs4c6QAAAARzQklUCAgI\n" +
                    "CHwIZIgAAANkSURBVHic7ZvNkRoxEIWfXL4vIbBVvpsMTAhksIRgRwAZsBnAxWfjCMARGM4+MBkw\n" +
                    "RPB8kKh1zagljUYzwlv6qrZ2a0fo50lq9agboFAoFApeVI5GSX4xf07NDwDUAE4AbkqpU45+9Ybk\n" +
                    "hOTK8/yF5JbklX6uJH+QfPG0uyI5ST+iCMwgf5sBbC3PVoGDd4nSGrARlabtvGI0RLizNc9eegrQ\n" +
                    "5EJy0RDhTj4xBBHu/EkoQGjdvcT40EOLKYBn4dmnHvX6kOp+xpvh7UyvU4PkDMARwFOHj50B7KBP\n" +
                    "iEopVZmZnOHtFFkA+NyhzhuAedbThuSMZB2wpHckg2eM5JzkMaDem5mQ/JD86ehoTXLeo+6lR+jv\n" +
                    "KccSjZk5iRMTWHP6V1200MmgPtYGE+GfdlxiXFK1E9u5pdCxmh3sQYf2Jg4xlqnb69IxaTUsBmxz\n" +
                    "LbR5GKpNX4dmQoeOI7RdCW1Hnx59HCrJQL02/0FySnJD8kDtHovbJrDsumOfhoN297oWyjZnUDRu\n" +
                    "oWVptxX7VOMLRliaO0s56XhtLeOOZfe2grHj6bM1bFSJ63OR//KGsqG0nhZsL3dRsNCyJBehqyeE\n" +
                    "jzEfAiA5SlYbAW3EvkK/WFWQjV2XshWAX96eDoljL+d3dSNJbSP+W4oQhiKEoQhhiD01JDYUvMsR\n" +
                    "+RZzZZdaiEe4Mou6Aylbw1CEMBQhDLE2okZu91bbgi6xj/dJaje/bA1DEcJQhDAUIQxRp4a5BdpY\n" +
                    "HkW5t49AHxfbZp2nGO8uUQoJRN2bRm0Nx6yP+a5hFUIpNZ4QDrILMTq0xxWuI7ZvS1SL3pZ9VoQt\n" +
                    "xjnhCFFp6rCB7XW7FWAaHMqxjcFzFajjosliGik6dBI65Ipb9G1TCuyMGWVrdUpKFLlygARQ6ki5\n" +
                    "lMSaL1HEdE7KVbikXKp0J7jmd+LoTia7phDDIwL5KBE2kq8eMZxZ9p6653TndI9/UkiYGZMM551D\n" +
                    "l5mjtgfN5PMmydKUkn1xhdo4HuG/PjsB2EO/E1QAzkqpmnoLPUF7p0v4vdQzdNpx7jhKm8CVkYKk\n" +
                    "OZyDYMTYDSjC49iEEKgdH+lojaFmbl8hFurVsWZY9r5LgDUffSuEQu2F7gNFqam313IsAXJ93XEG\n" +
                    "/fZ4/w3o06QGAKXU4Nm7hUKhUIjgL/9/6dhvvYPfAAAAAElFTkSuQmCC").bitmap
        }
    }

    /**
     * 已存储的 JSON 数据
     * @return [String]
     */
    private val storageDataJson get() = (context?.modulePrefs ?: param?.prefs)?.getString(NOTIFY_ICON_DATAS)

    /**
     * 获取图标数据
     * @return [Array] 通知栏小图标数组
     */
    val iconDatas
        get() = ArrayList<IconDataBean>().apply {
            storageDataJson?.also {
                if (it.isNotBlank()) runCatching {
                    JSONArray(it).also { array ->
                        for (i in 0 until array.length()) runCatching {
                            (array.get(i) as JSONObject).apply {
                                add(
                                    IconDataBean(
                                        appName = getString("appName"),
                                        packageName = getString("packageName"),
                                        isEnabled = getBoolean("isEnabled"),
                                        isEnabledAll = getBoolean("isEnabledAll"),
                                        iconBitmap = getString("iconBitmap").bitmap,
                                        iconColor = safeOfNan { Color.parseColor(getString("iconColor")) },
                                        contributorName = getString("contributorName")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

    /**
     * 拼接图标数组数据
     * @param dataJson1 图标数据 JSON
     * @param dataJson2 图标数据 JSON
     * @return [String] 拼接后的数据
     */
    fun splicingJsonArray(dataJson1: String, dataJson2: String) = safeOf(default = "[]") {
        dataJson1.replace(oldValue = "]", newValue = "") + "," + dataJson2.replace(oldValue = "[", newValue = "")
    }

    /**
     * 是否不为合法 JSON
     * @param json 数据
     * @return [Boolean]
     */
    fun isNotVaildJson(json: String) = json.trim().let { !it.startsWith("[") || !it.endsWith("]") }

    /**
     * 是否为异常地址
     * @param json 数据
     * @return [Boolean]
     */
    fun isHackString(json: String) = json.contains(other = "Checking your browser before accessing")

    /**
     * 比较图标数据不相等
     * @param dataJson 图标数据 JSON
     * @return [Boolean] 是否不相等
     */
    fun isCompareDifferent(dataJson: String) = storageDataJson?.trim() != dataJson.trim()

    /**
     * 保存图标数据
     * @param dataJson 图标数据 JSON
     */
    fun save(dataJson: String) = context?.modulePrefs?.putString(NOTIFY_ICON_DATAS, dataJson)
}
