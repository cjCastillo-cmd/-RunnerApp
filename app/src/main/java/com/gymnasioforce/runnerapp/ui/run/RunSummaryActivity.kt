package com.gymnasioforce.runnerapp.ui.run

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivityRunSummaryBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.BaseActivity
import com.gymnasioforce.runnerapp.utils.resolvePhotoUrl
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RunSummaryActivity : BaseActivity() {

    private lateinit var b: ActivityRunSummaryBinding
    private var runId: Int = 0

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadRunPhoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRunSummaryBinding.inflate(layoutInflater)
        setContentView(b.root)

        runId = intent.getIntExtra("run_id", 0)
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

        b.btnAddPhoto.setOnClickListener { pickPhoto.launch("image/*") }
        b.btnShare.setOnClickListener { shareRun(km, sec) }
        b.btnClose.setOnClickListener { finish() }

        // Ocultar boton de foto si no hay runId
        if (runId == 0) b.btnAddPhoto.visibility = View.GONE
    }

    private fun uploadRunPhoto(uri: Uri) {
        if (runId == 0) return
        val stream = contentResolver.openInputStream(uri) ?: return
        val file = File(cacheDir, "run_photo.jpg")
        file.outputStream().use { stream.copyTo(it) }

        val body = MultipartBody.Part.createFormData(
            "photo", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.uploadRunPhoto(runId, body)
                if (resp.isSuccessful) {
                    val url = resp.body()?.data?.get("photo_url")
                    resolvePhotoUrl(url)?.let {
                        b.ivRunPhoto.visibility = View.VISIBLE
                        Glide.with(this@RunSummaryActivity).load(it).into(b.ivRunPhoto)
                        b.btnAddPhoto.text = getString(R.string.success_photo_updated)
                        b.btnAddPhoto.isEnabled = false
                    }
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_uploading_photo))
            }
        }
    }

    private fun shareRun(km: Double, sec: Int) {
        val duration = "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)
        val text = getString(R.string.msg_share_run).format(km, duration)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_chooser)))
    }
}
