package com.gymnasioforce.runnerapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivitySplashBinding
import androidx.appcompat.app.AppCompatDelegate
import com.gymnasioforce.runnerapp.ui.auth.LoginActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var b: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema guardado antes de inflar
        AppCompatDelegate.setDefaultNightMode(Prefs(this).themeMode)
        super.onCreate(savedInstanceState)
        b = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.splashContent.alpha = 0f
        b.splashContent.scaleX = 0.8f
        b.splashContent.scaleY = 0.8f

        b.splashContent.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .start()

        lifecycleScope.launch {
            delay(2200)

            b.splashContent.animate()
                .alpha(0f)
                .scaleY(1.1f)
                .scaleX(1.1f)
                .setDuration(400)
                .withEndAction {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                }
                .start()
        }
    }
}
