package com.gymnasioforce.runnerapp.ui.run

import android.os.Bundle
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.databinding.ActivityRunDetailBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class RunDetailActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var b: ActivityRunDetailBinding
    private var gMap: GoogleMap? = null
    private var runId: Int = 0
    private var routeJson: String? = null
    private var distanceKm: Double = 0.0
    private var durationSec: Int = 0
    private var calories: Int = 0
    private var avgPace: Double? = null
    private var createdAt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRunDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        runId = intent.getIntExtra("run_id", 0)
        distanceKm = intent.getDoubleExtra("distance_km", 0.0)
        durationSec = intent.getIntExtra("duration_sec", 0)
        calories = intent.getIntExtra("calories", 0)
        avgPace = intent.getDoubleExtra("avg_pace", 0.0).let { if (it == 0.0) null else it }
        routeJson = intent.getStringExtra("route_json")
        createdAt = intent.getStringExtra("created_at") ?: ""

        val mapFrag = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFrag?.getMapAsync(this)

        displayStats()

        b.btnBack.setOnClickListener { finish() }
        b.btnDelete.setOnClickListener { deleteRun() }
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

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
        } catch (_: Exception) {}
        map.uiSettings.isZoomControlsEnabled = true
        drawRoute()
    }

    private fun drawRoute() {
        val json = routeJson ?: return
        val type = object : TypeToken<List<Map<String, Double>>>() {}.type
        val points: List<Map<String, Double>> = try {
            Gson().fromJson(json, type)
        } catch (_: Exception) { return }

        if (points.isEmpty()) return

        val latLngs = points.mapNotNull { m ->
            val lat = m["lat"] ?: return@mapNotNull null
            val lng = m["lng"] ?: return@mapNotNull null
            LatLng(lat, lng)
        }

        if (latLngs.isEmpty()) return

        gMap?.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .color(getColor(R.color.volt))
                .width(8f)
        )

        val bounds = LatLngBounds.Builder()
        latLngs.forEach { bounds.include(it) }
        gMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80))
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
}
