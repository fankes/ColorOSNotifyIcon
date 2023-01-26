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
@file:Suppress("MemberVisibilityCanBePrivate")

package com.fankes.coloros.notify.param

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.fankes.coloros.notify.bean.IconDataBean
import com.fankes.coloros.notify.data.DataConst
import com.fankes.coloros.notify.utils.factory.*
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

        /**
         * Android 13 系统默认图标
         * @return [Bitmap]
         */
        val android13IconBitmap by lazy {
            ("iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAACXBIWXMAAAsTAAALEwEAmpwYAAAFG2lUWHRYTU" +
                    "w6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZ" +
                    "VN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0i" +
                    "QWRvYmUgWE1QIENvcmUgNi4wLWMwMDYgNzkuZGFiYWNiYiwgMjAyMS8wNC8xNC0wMDozOTo0NCAgICA" +
                    "gICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi" +
                    "1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwO" +
                    "i8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1l" +
                    "bnRzLzEuMS8iIHhtbG5zOnBob3Rvc2hvcD0iaHR0cDovL25zLmFkb2JlLmNvbS9waG90b3Nob3AvMS4" +
                    "wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0RX" +
                    "Z0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiIHhtcDpDc" +
                    "mVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIDIyLjQgKE1hY2ludG9zaCkiIHhtcDpDcmVhdGVEYXRl" +
                    "PSIyMDIzLTAxLTI3VDAxOjQ2OjAzKzA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAyMy0wMS0yN1QwMTo" +
                    "0NjozOSswODowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAyMy0wMS0yN1QwMTo0NjozOSswODowMCIgZG" +
                    "M6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQc" +
                    "m9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDphZjQwOGEx" +
                    "ZC0wM2M4LTRiNTgtYmQxMC1kNDBkYTg4MDU0ODUiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6YWY" +
                    "0MDhhMWQtMDNjOC00YjU4LWJkMTAtZDQwZGE4ODA1NDg1IiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SU" +
                    "Q9InhtcC5kaWQ6YWY0MDhhMWQtMDNjOC00YjU4LWJkMTAtZDQwZGE4ODA1NDg1Ij4gPHhtcE1NOkhpc" +
                    "3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFu" +
                    "Y2VJRD0ieG1wLmlpZDphZjQwOGExZC0wM2M4LTRiNTgtYmQxMC1kNDBkYTg4MDU0ODUiIHN0RXZ0Ond" +
                    "oZW49IjIwMjMtMDEtMjdUMDE6NDY6MDMrMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIF" +
                    "Bob3Rvc2hvcCAyMi40IChNYWNpbnRvc2gpIi8+IDwvcmRmOlNlcT4gPC94bXBNTTpIaXN0b3J5PiA8L" +
                    "3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/" +
                    "PqALyqMAAAU1SURBVHic7Vvvces2DP+l1+9lJ6i7gTaoOkG0QdQJnEwQbxC/CaK3gd4EUSaIOkGcCSx" +
                    "PgH4AdVEk/gEp2sy95nfHsywBIAQRJABKV0SELwC/5Fbgs+AShtgAeATwBOABgBLwKABbzXMv5FkHIj" +
                    "pnq4joSB/xQkTKwaM0zRSvRFScU9dzj4gKy6dZALh18Nxqmik2AOoE+lhxdebJ8gjzsB4A/A2gn50vw" +
                    "O5g4jkA+DORXguc2xAu4T3YGIP+r8BGKBw8Vwl0MgvOaIgRvf4tBLRnM8SvkXxb8BMcAHyz0JRCWUVA" +
                    "vwWW7jRCopMdETPsk2FGL2c0G8PMnwJPtFxxCq3DnC7ovkJd4xYcC5jQgCc0BZ7hVfBTkaEHsAc/+Qr" +
                    "21eQfrZMIoYboAPwVwpARz5C7Z3Ac0QXS50QXQpzbECfwk3vWxynRhRDHLJ8d4tzjBKDV/C3e44c5FN" +
                    "j3S/37W0RfQW4BIGrVqANn+p4451iTr/SBfdah/cQoVgiVOcQo5Gi1lilBESo/JumSDLnv4OCniZBvQ" +
                    "6NlfhfQlsHSAy2naBm8zHGbcBTY2q1Hh1dyp/qLZpssCwDX+ngABzElOKBSDrsGBTErUYMLPjYcwLp0" +
                    "4PtR+vwPmMJ0g3Uqj7VzjoTQkWFDNZdlGvrHCMGNRdGSiB6JaEuBQ3Wiz1bLKC00TYS+x7kck+KhOJD" +
                    "5JtWMbm+g8bX9TIatn0OE3h8Mm6JUt4M5OCo8/yWQyBi0DutgsHDIUOs9T3SY0MbMIdM5YPDQ9gF6L1" +
                    "zZ5pedUOBi0jHIqmldBbrQMkxuMW2VUOfOJEv6NEzwPaEcbToCTahtvK45Yg/gznG9Xe2X6dE6rt3BE" +
                    "eP4JkuX4M7DmwOd41rrYpSk4TaC32FPpXNBgfdSTHBWwH1V7NpxrfXwfjbUcLiGa0QU4Fi+SKxQLvTg" +
                    "XKg3XTQZogRwgzPvNWZEA07lu+nJuSEe4N6g/Zmwx2RVnK4aO/x/jADwve7GP9PJsgwU9LxSkQLLwuw" +
                    "J9i09KUIKy+V4MDXEENhhFcEzRYel0j1iymzvULAvnyYM48HUNdrATqtA+kugCqRvx4OpIRrwLrJ0o6" +
                    "UM7PQSKIV0J/C9NuMJ0/KpwP67AVv4ek6gMYCjy1h0WLpG+MbMR9je0AG4VtmCa5k95m4tyOhKRzbnS" +
                    "8NdzZTqdyvkudLw0scvqVB1sO8l3Av4LwXb6wrfIEgQpaW6g+V8gc8RgdZgVzbhIBEgNcTguHaZF0Lt" +
                    "UHCPzEEiRGIIX0cb2IflJfAA+2gYryufEIkhJIJq5AnPd/C7poLkQXlm051jJjbBWhM0tLWrRh2o284" +
                    "lz9WRorhdr63wRtYY4j5CryM5KuEu11CImwT34IJODK8PSsveRfIq20WXIQ4A3iI6BNhvX8AFnlS40T" +
                    "LrSP5/4VhKfZPlznDuBA5Xf8BtqA04ln+BPUyX4BrAq5a1cdC9TfQy5Ut7Zy8Cf6yIt8haWu44KX1e6" +
                    "qOPRHRD9t20Tl+70bRHoezWoFetzzckSAVSvJSuwEMu5u23FDiBR8qwRkiK3fAB66tKa9Ajwf7K18dt" +
                    "GqkM8UciOdn6TvXhSu6PR1d/0HIJ17gDK3oF/nRJEpu8adqRz7Urnwa+ZUXYBsuyZso9NoLlcGPgs+U" +
                    "WQ4p7SGWIwmAMkxHG1jiM0Dj45sYYKNH3oLHfdM3R42O1qoO7PNbBHn67+BpwzFLO/q/Gub/ys0HBHI" +
                    "QlCY5ikCuOGGDOY3bI9PJJrhExosT77lSLjK8j5TbEp8FXiK3xH4bAld9cxsQDAAAAAElFTkSuQmCC").bitmap
        }
    }

    /**
     * 已存储的 JSON 数据
     * @return [String]
     */
    internal val storageDataJson get() = (context?.modulePrefs ?: param?.prefs)?.direct()?.get(DataConst.NOTIFY_ICON_DATAS)

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
                            add(convertToBean(array.get(i) as JSONObject)!!)
                        }.onFailure { context?.snake(msg = "部分规则加载失败") }
                    }
                }.onFailure { context?.snake(msg = "规则加载发生错误") }
            }
        }

    /**
     * 转换为 [IconDataBean]
     * @param jsonObject Json 实例
     * @return [IconDataBean] or null
     */
    private fun convertToBean(jsonObject: JSONObject) = safeOfNull {
        jsonObject.let {
            IconDataBean(
                appName = it.getString("appName"),
                packageName = it.getString("packageName"),
                isEnabled = it.getBoolean("isEnabled"),
                isEnabledAll = it.getBoolean("isEnabledAll"),
                iconBitmap = it.getString("iconBitmap").bitmap,
                iconColor = safeOfNan { Color.parseColor(it.getString("iconColor")) },
                contributorName = it.getString("contributorName")
            )
        }
    }

    /**
     * 拼接图标数组数据
     * @param dataJson1 图标数据 JSON
     * @param dataJson2 图标数据 JSON
     * @return [String] 拼接后的数据
     */
    fun splicingJsonArray(dataJson1: String, dataJson2: String) = safeOf(default = "[]") {
        dataJson1.replace("]", "") + "," + dataJson2.replace("[", "")
    }

    /**
     * 是否不为合法 JSON
     * @param json 数据
     * @return [Boolean]
     */
    fun isNotVaildJson(json: String) = !isJsonArray(json) && !isJsonObject(json)

    /**
     * 是否为 JSON 数组
     * @param json 数据
     * @return [Boolean]
     */
    fun isJsonArray(json: String) = json.trim().let { it.startsWith("[") && it.endsWith("]") }

    /**
     * 是否为 JSON 实例
     * @param json 数据
     * @return [Boolean]
     */
    fun isJsonObject(json: String) = json.trim().let { it.startsWith("{") && it.endsWith("}") }

    /**
     * 是否为异常地址
     * @param json 数据
     * @return [Boolean]
     */
    fun isHackString(json: String) = json.contains("Checking your browser before accessing")

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
    fun save(dataJson: String) = context?.modulePrefs?.put(DataConst.NOTIFY_ICON_DATAS, dataJson)
}
