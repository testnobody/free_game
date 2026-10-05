package com.testnobody.freegame

/** 单款游戏的元数据，对应 assets/www/games.json 中的一条记录。 */
data class Game(
    val id: String,
    val name: String,
    val nameEn: String,
    val category: String,
    val entry: String,
    val icon: String,
    val desc: String,
    val source: String,
    val touch: String,
) {
    companion object {
        const val TOUCH_NATIVE = "native"       // 原生触屏支持
        const val TOUCH_TAP = "tap"            // 点按可玩
        const val TOUCH_SWIPE_KEYS = "swipe-keys" // 滑动映射方向键（键盘操作类游戏）
    }
}
