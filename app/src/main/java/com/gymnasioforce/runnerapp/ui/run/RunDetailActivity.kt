package com.gymnasioforce.runnerapp.ui.run

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.databinding.ActivityRunDetailBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.utils.resolvePhotoUrl
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

class RunDetailActivity : BaseActivity() {

    private lateinit var b: ActivityRunDetailBinding
    private lateinit var mapView: MapView
    private var runId: Int = 0
    private var routeJson: String? = null
    private var distanceKm: Double = 0.0
    private var durationSec: Int = 0
    private var calories: Int = 0
    private var avgPace: Double? = null
    private var createdAt: String = ""
    private var photoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        b = ActivityRunDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        runId = intent.getIntExtra("run_id", 0)
        distanceKm = intent.getDoubleExtra("distance_km", 0.0)
        durationSec = intent.getIntExtra("duration_sec", 0)
        calories = intent.getIntExtra("calories", 0)
        avgPace = intent.getDoubleExtra("avg_pace", 0.0).let { if (it == 0.0) null else it }
        routeJson = intent.getStringExtra("route_json")
        createdAt = intent.getStringExtra("created_at") ?: ""
        photoUrl = intent.getStringExtra("photo_url")

        setupMap()
        displayStats()
        loadPhoto()
        drawRoute()

        b.btnBack.setOnClickListener { finish() }
        b.btnDelete.setOnClickListener { deleteRun() }
    }

    private fun setupMap() {
        mapView = b.mapView

        val tileSource = XYTileSource(
            "CartoDB_Voyager", 0, 19, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
            )
        )
        mapView.setTileSource(tileSource)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
    }

    private fun displayStats() {
        b.tvKm.text = "%.2f".format(distanceKm)
        b.tvDate.text = createdAt.take(10)
        b.tvCalories.text = "$calories"
        b.tvDuration.text = "%02d:%02d:%02d".format(
            durationSec / 3600, (durationSec % 3600) / 60, durationSec % 60
        )
        avgPace?.let { p ->
            b.tvPace.text = if (p > 0) "${p.toInt()}:${"%02d".format(((p % 1) * 60).toInt())}" else "-"
        } ?: run { b.tvPace.text = "-" }
    }

    private fun drawRoute() {
        val json = routeJson ?: return
        val type = object : TypeToken<List<Map<String, Double>>>() {}.type
        val points: List<Map<String, Double>> = try {
            Gson().fromJson(json, type)
        } catch (_: Exception) { return }

        if (points.isEmpty()) return

        val geoPoints = points.mapNotNull { m ->
            val lat = m["lat"] ?: return@mapNotNull null
            val lng = m["lng"] ?: return@mapNotNull null
            GeoPoint(lat, lng)
        }

        if (geoPoints.isEmpty()) return

        val polyline = Polyline().apply {
            setPoints(geoPoints)
            outlinePaint.color = getColor(R.color.volt)
            outlinePaint.strokeWidth = 8f
        }
        mapView.overlays.add(polyline)

        // Ajustar camara a los bounds de la ruta
        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
        mapView.post {
            mapView.zoomToBoundingBox(boundingBox, true, 80)
        }
    }

    private fun loadPhoto() {
        val resolved = resolvePhotoUrl(photoUrl)
        if (resolved != null) {
            b.ivRunPhoto.visibility = View.VISIBLE
            Glide.with(this).load(resolved).into(b.ivRunPhoto)
        }
    }

    private fun deleteRun() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.deleteRun(runId)
                if (resp.isSuccessful) {
                    val dao = AppDatabase.getInstance(this@RunDetailActivity).runDao()
                    dao.getById(runId)?.let { dao.delete(it) }
                    showToast(getString(R.string.success_run_deleted))
                    finish()
                } else {
                    showToast(getString(R.string.error_deleting_run))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_connection))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
