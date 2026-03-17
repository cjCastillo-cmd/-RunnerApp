package com.gymnasioforce.runnerapp.ui.run

import android.content.Intent
import android.os.Bundle
import com.gymnasioforce.runnerapp.databinding.ActivityRunSummaryBinding
import com.gymnasioforce.runnerapp.ui.BaseActivity

class RunSummaryActivity : BaseActivity() {

    private lateinit var b: ActivityRunSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRunSummaryBinding.inflate(layoutInflater)
        setContentView(b.root)

        val km = intent.getDoubleExtra("distance_km", 0.0)
        val sec = intent.getIntExtra("duration_sec", 0)
        val cal = intent.getIntExtra("calories", 0)
        val pace = intent.getDoubleExtra("avg_pace", 0.0)
        val date = intent.getStringExtra("created_at") ?: ""

        b.tvKm.text = "%.2f".format(km)
        b.tvDuration.text = "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)
        b.tvCalories.text = "$cal"
        b.tvDate.text = date.take(10)

        if (pace > 0) {
            b.tvPace.text = "${pace.toInt()}:${"%02d".format(((pace % 1) * 60).toInt())}"
        } else {
            b.tvPace.text = "-"
        }

        // Animacion de entrada
        b.tvKm.alpha = 0f
        b.tvKm.translationY = 40f
        b.tvKm.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(300).start()

        b.btnShare.setOnClickListener { shareRun(km, sec) }
        b.btnClose.setOnClickListener { finish() }
    }

    private fun shareRun(km: Double, sec: Int) {
        val duration = "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)
        val text = "Acabo de correr %.2f km en %s con Runner App!".format(km, duration)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Compartir carrera"))
    }
}
