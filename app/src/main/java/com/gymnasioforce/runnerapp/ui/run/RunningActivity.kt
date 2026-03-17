package com.gymnasioforce.runnerapp.ui.run

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.databinding.ActivityRunningBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.SaveRunRequest
import com.gymnasioforce.runnerapp.services.RunningService
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class RunningActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var b: ActivityRunningBinding
    private var gMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())
    private val uiUpdater = object : Runnable {
        override fun run() {
            if (RunningService.isRunning) {
                updateUI()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) { isBound = true }
        override fun onServiceDisconnected(name: ComponentName?) { isBound = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRunningBinding.inflate(layoutInflater)
        setContentView(b.root)

        val mapFrag = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFrag?.getMapAsync(this)

        b.btnFinish.setOnClickListener { toggleRun() }
        b.btnCancel.setOnClickListener { finish() }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        // Aplicar estilo oscuro al mapa
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
        } catch (_: Exception) {}

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            gMap?.isMyLocationEnabled = true
        }
    }

    private fun toggleRun() {
        if (!RunningService.isRunning) checkPermissionsAndStart()
        else stopRun()
    }

    private fun checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            return
        }
        startRun()
    }

    private fun startRun() {
        val intent = Intent(this, RunningService::class.java).apply { action = RunningService.ACTION_START }
        startForegroundService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        b.btnFinish.text = "FINALIZAR"
        b.btnCancel.visibility = View.GONE
        handler.post(uiUpdater)
    }

    private fun stopRun() {
        handler.removeCallbacks(uiUpdater)
        RunningService.isRunning = false

        val km = RunningService.totalDistance / 1000.0
        val duration = RunningService.durationSec
        val start = RunningService.startLocation
        val end = RunningService.lastLocation
        val route = Gson().toJson(RunningService.routePoints)
        val calories = (km * 70).toInt()

        if (km < 0.01) {
            showToast("Recorrido muy corto")
            stopService(Intent(this, RunningService::class.java))
            finish(); return
        }

        b.btnFinish.isEnabled = false

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.saveRun(SaveRunRequest(
                    distanceKm = km, durationSec = duration,
                    startLat = start?.latitude ?: 0.0, startLng = start?.longitude ?: 0.0,
                    endLat = end?.latitude ?: 0.0, endLng = end?.longitude ?: 0.0,
                    routeJson = route
                ))
                if (resp.isSuccessful) {
                    // Guardar en SQLite local
                    resp.body()?.data?.let { run ->
                        val entity = RunEntity(
                            id = run.id, userId = run.userId,
                            distanceKm = run.distanceKm, calories = run.calories,
                            durationSec = run.durationSec,
                            startLat = run.startLat, startLng = run.startLng,
                            endLat = run.endLat, endLng = run.endLng,
                            avgPace = run.avgPace, routeJson = run.routeJson,
                            createdAt = run.createdAt
                        )
                        AppDatabase.getInstance(this@RunningActivity).runDao().insert(entity)

                        // Mostrar pantalla de resumen
                        val summaryIntent = Intent(this@RunningActivity, RunSummaryActivity::class.java).apply {
                            putExtra("distance_km", run.distanceKm)
                            putExtra("duration_sec", run.durationSec)
                            putExtra("calories", run.calories)
                            putExtra("avg_pace", run.avgPace ?: 0.0)
                            putExtra("created_at", run.createdAt)
                        }
                        startActivity(summaryIntent)
                    }
                } else {
                    showToast("Error al guardar la carrera")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                stopService(Intent(this@RunningActivity, RunningService::class.java))
                finish()
            }
        }
    }

    private fun updateUI() {
        val km = RunningService.totalDistance / 1000.0
        val sec = RunningService.durationSec
        b.tvKm.text = "%.2f".format(km)
        b.tvTimer.text = "%02d:%02d".format(sec / 60, sec % 60)
        b.tvKcal.text = "${(km * 70).toInt()}"

        if (RunningService.routePoints.size > 1) {
            val points = RunningService.routePoints.map { LatLng(it["lat"]!!, it["lng"]!!) }
            polyline?.remove()
            polyline = gMap?.addPolyline(PolylineOptions().addAll(points).color(0xFFC8FF00.toInt()).width(8f))
            gMap?.animateCamera(CameraUpdateFactory.newLatLng(points.last()))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startRun()
        else showToast("Se requieren permisos de ubicacion")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(uiUpdater)
        if (isBound) unbindService(serviceConnection)
    }
}
