package com.testnobody.freegame

import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GameActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val game = GameRepository.fromJson(intent.getStringExtra(EXTRA_GAME).orEmpty())

        webView = findViewById(R.id.webview)
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true // localStorage 存最高分/进度
            allowFileAccess = true   // 加载 file:///android_asset 与外部 overlay 目录
            mediaPlaybackRequiresUserGesture = false
            builtInZoomControls = false
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // 键盘操作类游戏：滑动屏幕映射为方向键
                if (game.touch == Game.TOUCH_SWIPE_KEYS) {
                    view.evaluateJavascript(SWIPE_KEYS_JS, null)
                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        webView.loadUrl(GameRepository.entryUrl(this, game))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_GAME = "extra_game"

        /**
         * 触屏滑动 -> 键盘方向键映射（用于 frogger / pacman 等纯键盘操作游戏）。
         * touchstart/touchend 计算主滑动方向，派发 keydown/keyup。
         */
        private const val SWIPE_KEYS_JS = """
(function () {
  var sx = 0, sy = 0, TH = 24;
  function fire(type, key) {
    var e = new KeyboardEvent(type, { key: key, bubbles: true, cancelable: true });
    document.dispatchEvent(e);
    document.body.dispatchEvent(e);
    if (document.activeElement) document.activeElement.dispatchEvent(e);
    window.dispatchEvent(e);
  }
  document.addEventListener('touchstart', function (e) {
    var t = e.changedTouches[0];
    sx = t.clientX; sy = t.clientY;
  }, { passive: true });
  document.addEventListener('touchend', function (e) {
    var t = e.changedTouches[0];
    var dx = t.clientX - sx, dy = t.clientY - sy;
    if (Math.max(Math.abs(dx), Math.abs(dy)) < TH) return;
    var key = Math.abs(dx) > Math.abs(dy)
      ? (dx > 0 ? 'ArrowRight' : 'ArrowLeft')
      : (dy > 0 ? 'ArrowDown' : 'ArrowUp');
    fire('keydown', key);
    setTimeout(function () { fire('keyup', key); }, 80);
  }, { passive: true });
})();
"""
    }
}
