#!/usr/bin/env python3
"""Merge litegame + awesome-mini-game into app/src/main/assets/www/.

Idempotent: wipes the www/ tree and rebuilds it from the upstream checkouts.
Does NOT touch games.json (hand-authored, lives in www/ as a source file).

Upstream checkouts (shallow clones):
    /tmp/litegame
    /tmp/awesome-mini-game
"""
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC_LITE = Path("/tmp/litegame")
SRC_AMG = Path("/tmp/awesome-mini-game")
WWW = ROOT / "app" / "src" / "main" / "assets" / "www"

# 27 litegame dirs (with -game suffix); id = dir minus suffix
LITE_GAMES = [
    "2048-game", "cardbattle-game", "fruitcatcher-game", "gomoku-game",
    "invaders-game", "klotski-game", "link-game", "match3-game",
    "maze-game", "memory-game", "minesweeper-game", "nonogram-game",
    "platformer-game", "pong-game", "reversi-game", "snake-game",
    "sokoban-game", "solitaire-game", "sudoku-game", "tetris-game",
    "tictactoe-game", "towerdefense-game", "whackmole-game",
    "wordguess-game", "xiangqi-game", "frogger-game", "pacman-game",
]

# 8 awesome-mini-game dirs (id = original dir name)
AMG_GAMES = [
    "raiden", "super-mario-bros", "tank-battle", "galaga",
    "dou-dizhu", "flappy-bird", "breakout", "bubble-dragon",
]

AMG_SHARED = ["i18n.js", "theme.js", "touch.js", "ui.css"]
TEXT_SUFFIXES = {".html", ".js", ".css"}

LITE_I18N_RE = re.compile(r"\.\./assets/i18n\.js")
AMG_ASSETS_RE = re.compile(r"((?:\.\./)+)assets/")
# 离线 App 用 file:///android_asset 加载：query string (?v=...) 在某些
# WebView/AssetManager 组合下会导致子资源 404，且离线场景无缓存意义，直接去掉
CACHEBUST_RE = re.compile(r"\?v=\d+")


def rewrite_lite(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    new = LITE_I18N_RE.sub("../assets/i18n-lite.js", text)
    if new != text:
        path.write_text(new, encoding="utf-8")


def rewrite_amg(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    new = AMG_ASSETS_RE.sub(lambda m: m.group(1) + "assets/amg/", text)
    new = CACHEBUST_RE.sub("", new)
    if new != text:
        path.write_text(new, encoding="utf-8")


def main() -> None:
    for src in (SRC_LITE, SRC_AMG):
        if not src.is_dir():
            sys.exit(f"missing upstream checkout: {src}")

    if WWW.exists():
        shutil.rmtree(WWW)
    (WWW / "games").mkdir(parents=True)
    (WWW / "assets" / "amg" / "fonts").mkdir(parents=True)
    (WWW / "assets" / "licenses").mkdir(parents=True)

    # ---- shared assets ----
    shutil.copy(SRC_LITE / "assets" / "i18n.js", WWW / "assets" / "i18n-lite.js")
    for f in AMG_SHARED:
        shutil.copy(SRC_AMG / "assets" / f, WWW / "assets" / "amg" / f)
    # fonts/ is referenced by assets/amg/ui.css (@font-face), so keep it
    shutil.copy(
        SRC_AMG / "assets" / "fonts" / "zcool-qkhy-subset.woff2",
        WWW / "assets" / "amg" / "fonts" / "zcool-qkhy-subset.woff2",
    )
    # upstream licenses (MIT attribution requirement)
    shutil.copy(SRC_LITE / "LICENSE", WWW / "assets" / "licenses" / "LITEGAME-LICENSE")
    shutil.copy(SRC_AMG / "LICENSE", WWW / "assets" / "licenses" / "AWESOME-MINI-GAME-LICENSE")

    # ---- litegame games: single-file index.html, strip "-game" suffix ----
    for d in LITE_GAMES:
        gid = d[: -len("-game")]
        dest = WWW / "games" / gid
        shutil.copytree(
            SRC_LITE / d, dest, ignore=shutil.ignore_patterns("README*")
        )
        for html in dest.rglob("*.html"):
            rewrite_lite(html)
        print(f"lite {d:22s} -> games/{gid}")

    # ---- awesome-mini-game games: keep original names & relative layout ----
    for gid in AMG_GAMES:
        dest = WWW / "games" / gid
        shutil.copytree(
            SRC_AMG / gid,
            dest,
            ignore=shutil.ignore_patterns("README*", "--selector"),
        )
        for f in dest.rglob("*"):
            if f.is_file() and f.suffix.lower() in TEXT_SUFFIXES:
                rewrite_amg(f)
        print(f"amg  {gid:22s} -> games/{gid}")

    total = sum(f.stat().st_size for f in WWW.rglob("*") if f.is_file())
    n_games = len(list((WWW / "games").iterdir()))
    print(f"\ndone: {n_games} games, www/ = {total / 1024:.0f} KB")


if __name__ == "__main__":
    main()
