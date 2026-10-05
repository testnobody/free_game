package com.testnobody.freegame

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 游戏清单仓库。
 *
 * 数据来源（按优先级）：
 * 1. 内置：assets/www/games.json（随 APK 发布，不可变）
 * 2. 外部 overlay：<filesDir>/games/games.json（预留的「游戏下载通道」）
 *
 * 外部 overlay 约定（现在只留接口，不做联网下载）：
 * - 目录结构与 assets/www 完全一致：
 *      <filesDir>/games/games.json
 *      <filesDir>/games/games/<id>/index.html (+ game.js / styles.css …)
 * - 合并规则：同 id 覆盖内置条目，新增 id 追加到列表末尾；
 *   games.json 解析失败则整体回退到内置清单。
 * - GameActivity 加载游戏文件时优先使用外部目录中的文件。
 */
object GameRepository {

    private const val ASSET_ROOT = "www"
    private const val OVERLAY_DIR = "games"

    fun loadGames(context: Context): List<Game> {
        val base = parseGames(
            context.assets.open("$ASSET_ROOT/games.json").bufferedReader().use { it.readText() }
        )
        val overlayFile = File(context.filesDir, "$OVERLAY_DIR/games.json")
        if (!overlayFile.isFile) return base
        return try {
            merge(base, parseGames(overlayFile.readText()))
        } catch (_: Exception) {
            base
        }
    }

    private fun merge(base: List<Game>, overlay: List<Game>): List<Game> {
        val byId = base.associateBy { it.id }.toMutableMap()
        val order = base.map { it.id }.toMutableList()
        for (g in overlay) {
            if (byId.containsKey(g.id)) {
                byId[g.id] = g
            } else {
                byId[g.id] = g
                order.add(g.id)
            }
        }
        return order.mapNotNull { byId[it] }
    }

    /** 游戏入口 URL：外部 overlay 中的文件优先，否则走内置 assets。 */
    fun entryUrl(context: Context, game: Game): String {
        val external = File(context.filesDir, "$OVERLAY_DIR/${game.entry}")
        if (external.isFile) return "file://${external.absolutePath}"
        return "file:///android_asset/$ASSET_ROOT/${game.entry}"
    }

    fun toJson(game: Game): String = JSONObject()
        .put("id", game.id)
        .put("name", game.name)
        .put("nameEn", game.nameEn)
        .put("category", game.category)
        .put("entry", game.entry)
        .put("icon", game.icon)
        .put("desc", game.desc)
        .put("source", game.source)
        .put("touch", game.touch)
        .toString()

    fun fromJson(json: String): Game {
        val o = JSONObject(json)
        return Game(
            id = o.getString("id"),
            name = o.getString("name"),
            nameEn = o.optString("nameEn"),
            category = o.optString("category"),
            entry = o.getString("entry"),
            icon = o.optString("icon", "\uD83C\uDFAE"),
            desc = o.optString("desc"),
            source = o.optString("source"),
            touch = o.optString("touch", Game.TOUCH_TAP),
        )
    }

    private fun parseGames(text: String): List<Game> {
        val arr = JSONArray(text)
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Game(
                id = o.getString("id"),
                name = o.getString("name"),
                nameEn = o.optString("nameEn"),
                category = o.getString("category"),
                entry = o.getString("entry"),
                icon = o.optString("icon", "\uD83C\uDFAE"),
                desc = o.optString("desc"),
                source = o.optString("source"),
                touch = o.optString("touch", Game.TOUCH_TAP),
            )
        }
    }
}
