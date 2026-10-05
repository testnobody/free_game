# FreeGame

离线手机单机小游戏聚合 App：**35 款经典小游戏，全部内置，零联网、零权限、开箱即玩。**

原生 Android 壳（Kotlin + Material3）+ WebView 加载 HTML5 游戏，游戏清单由
`app/src/main/assets/www/games.json` 驱动。

## 游戏列表（35 款）

| # | 游戏 | 分类 | 来源 | 触屏 |
|---|---|---|---|---|
| 1 | 🧮 2048 | 益智 | litegame | 滑动 |
| 2 | 🧱 俄罗斯方块 | 益智 | litegame | 原生 |
| 3 | 💣 扫雷 | 益智 | litegame | 原生 |
| 4 | 9️⃣ 数独 | 益智 | litegame | 点按 |
| 5 | 🧩 华容道 | 益智 | litegame | 滑动 |
| 6 | 🔗 连连看 | 益智 | litegame | 点按 |
| 7 | 🌀 迷宫 | 益智 | litegame | 原生 |
| 8 | 🔢 数织 | 益智 | litegame | 点按 |
| 9 | 📦 推箱子 | 益智 | litegame | 滑动 |
| 10 | 🎴 记忆翻牌 | 益智 | litegame | 点按 |
| 11 | 🔤 猜词游戏 | 益智 | litegame | 原生 |
| 12 | ⚫ 五子棋 | 棋牌 | litegame | 点按 |
| 13 | ♞ 中国象棋 | 棋牌 | litegame | 点按 |
| 14 | ⚪ 黑白棋 | 棋牌 | litegame | 点按 |
| 15 | ⭕ 井字棋 | 棋牌 | litegame | 点按 |
| 16 | ♠️ 纸牌接龙 | 棋牌 | litegame | 点按 |
| 17 | 🃏 卡牌对战 | 卡牌 | litegame | 点按 |
| 18 | 🀄 斗地主 | 棋牌 | awesome-mini-game | 原生 |
| 19 | 👾 太空侵略者 | 射击 | litegame | 原生 |
| 20 | 🚀 雷电 | 射击 | awesome-mini-game | 原生 |
| 21 | 🛡️ 坦克大战 | 射击 | awesome-mini-game | 原生 |
| 22 | 🐝 小蜜蜂 | 射击 | awesome-mini-game | 原生 |
| 23 | 🕹️ 横版闯关 | 动作 | litegame | 原生 |
| 24 | 🍄 超级马里奥 | 动作 | awesome-mini-game | 原生 |
| 25 | 🐸 青蛙过河 | 动作 | litegame | 滑动映射方向键 |
| 26 | 🐍 贪吃蛇 | 休闲 | litegame | 滑动 |
| 27 | 💎 宝石消除 | 休闲 | litegame | 原生 |
| 28 | 🍎 接水果 | 休闲 | litegame | 原生 |
| 29 | 🏓 乒乓球 | 休闲 | litegame | 原生 |
| 30 | 🐹 打地鼠 | 休闲 | litegame | 点按 |
| 31 | 🐤 Flappy Bird | 休闲 | awesome-mini-game | 原生 |
| 32 | 🎯 打砖块 | 休闲 | awesome-mini-game | 原生 |
| 33 | 🫧 泡泡龙 | 休闲 | awesome-mini-game | 原生 |
| 34 | 🟡 吃豆人 | 街机 | litegame | 滑动映射方向键 |
| 35 | 🗼 塔防 | 策略 | litegame | 点按 |

> 青蛙过河 / 吃豆人原版只支持键盘，App 内通过「滑动屏幕 → 方向键」映射实现触屏操作。

## 本地构建

```bash
./gradlew assembleDebug
# APK 输出：app/build/outputs/apk/debug/app-debug.apk
```

push 到 `main` 分支会自动构建，APK 作为 artifact 上传（见 `.github/workflows/build.yml`）。

环境要求：JDK 17+，Gradle 8.7（wrapper 自动下载），Android SDK（compileSdk 34）。

## 添加新游戏

1. 把游戏目录放到 `app/src/main/assets/www/games/<id>/`（入口为 `index.html`）；
2. 在 `app/src/main/assets/www/games.json` 追加一条记录：

```json
{
  "id": "newgame",
  "name": "新游戏",
  "nameEn": "New Game",
  "category": "益智",
  "entry": "games/newgame/index.html",
  "icon": "🎮",
  "desc": "一句话介绍",
  "source": "litegame",
  "touch": "native"
}
```

字段说明：`category` 取值 街机/益智/棋牌/射击/动作/休闲/卡牌/策略；
`touch` 取值 `native`（原生触屏）/`tap`（点按可玩）/`swipe-keys`（滑动映射方向键）。

上游游戏可用 `tools/merge-games.py` 重新合并（需 `/tmp/litegame` 与
`/tmp/awesome-mini-game` 两个浅克隆存在，可重复执行）。

## 预留的游戏下载通道（扩展约定）

App 本身不联网，但 `GameRepository` 预留了外部游戏目录的加载接口，
以后要加"下载新游戏"功能时直接实现下载逻辑即可，约定如下：

- 外部目录：`<filesDir>/games/`（App 私有目录，无需存储权限）
- 结构与 `assets/www` 完全一致：
  ```
  <filesDir>/games/games.json
  <filesDir>/games/games/<id>/index.html (+ game.js / styles.css …)
  ```
- 合并规则：启动时若 `games.json` 存在，按 `id` 与内置清单合并——
  同 `id` 覆盖内置条目，新增 `id` 追加到列表末尾；解析失败则回退内置清单。
- 游戏加载时优先使用外部目录中的文件（`file://` 路径），
  `WebView` 已开启 `allowFileAccess`，可直接加载。

## 开源协议与版权

- 本项目 App 壳代码遵循 **MIT** 协议开源（Copyright (c) 2026 testnobody）。
- 内置游戏来自 [gtdong/litegame](https://github.com/gtdong/litegame)（© 2026 DongGuoTao）
  与 [davidzhanghui/awesome-mini-game](https://github.com/davidzhanghui/awesome-mini-game)
  （© 2026 davidzhanghui），均为 MIT 协议，版权声明见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)
  及 App 内「关于」页面。
- 游戏名称、玩法与设计归各自原版权方所有（如俄罗斯方块、马里奥、雷电、小蜜蜂、坦克大战等）。
  本项目为非商业性质的粉丝致敬 + 学习作品，请支持正版；不要声称获得原 IP 授权。
