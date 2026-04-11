package com.gymnasioforce.runnerapp.ui.run

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.repository.RunRepository
import com.gymnasioforce.runnerapp.databinding.ActivityRunningBinding
import com.gymnasioforce.runnerapp.network.SaveRunRequest
import com.gymnasioforce.runnerapp.services.RunningService
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class RunningActivity : BaseActivity() {

    private lateinit var b: ActivityRunningBinding
    private lateinit var mapView: MapView
    private var polyline: Polyline? = null
    private var locationOverlay: MyLocationNewOverlay? = null
    private var isBound = false
    private var initialCentered = false

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

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        b = ActivityRunningBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupMap()

        b.btnFinish.setOnClickListener { toggleRun() }
        b.btnCancel.setOnClickListener { cancelRun() }
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
        mapView.controller.setZoom(16.0)
    }

    private fun enableLocationOverlay() {
        if (locationOverlay != null) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val provider = GpsMyLocationProvider(this).apply {
            locationUpdateMinTime = 2000
            locationUpdateMinDistance = 3f
        }
        locationOverlay = MyLocationNewOverlay(provider, mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            runOnFirstFix {
                runOnUiThread {
                    val loc = myLocation
                    if (loc != null && !initialCentered) {
                        mapView.controller.animateTo(loc)
                        mapView.controller.setZoom(17.0)
                        initialCentered = true
                    }
                }
            }
        }
        mapView.overlays.add(locationOverlay)
        mapView.invalidate()
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
        enableLocationOverlay()
        val intent = Intent(this, RunningService::class.java).apply { action = RunningService.ACTION_START }
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        b.btnFinish.text = getString(R.string.btn_finish)
        b.btnCancel.visibility = View.VISIBLE
        handler.post(uiUpdater)
    }

    private fun cancelRun() {
        handler.removeCallbacks(uiUpdater)
        RunningService.isRunning = false
        stopService(Intent(this, RunningService::class.java))
        finish()
    }

    private fun stopRun() {
        handler.removeCallbacks(uiUpdater)
        RunningService.isRunning = false

        val km = RunningService.totalDistance / 1000.0
        val duration = RunningService.durationSec
        val start = RunningService.startLocation
        val end = RunningService.lastLocation
        val route = Gson().toJson(RunningService.routePoints)

        if (km < 0.01) {
            showToast(getString(R.string.msg_short_run))
            stopService(Intent(this, RunningService::class.java))
            finish(); return
        }

        b.btnFinish.isEnabled = false

        // Guardar carrera via Repository (soporta offline)
        val runRepo = RunRepository(this)
        lifecycleScope.launch {
            val request = SaveRunRequest(
                distanceKm = km, durationSec = duration,
                startLat = start?.latitude ?: 0.0, startLng = start?.longitude ?: 0.0,
                endLat = end?.latitude ?: 0.0, endLng = end?.longitude ?: 0.0,
                routeJson = route
            )
            runRepo.saveRun(request)
                .onSuccess { run ->
                    val summaryIntent = Intent(this@RunningActivity, RunSummaryActivity::class.java).apply {
                        putExtra("run_id", run.id)
                        putExtra("distance_km", run.distanceKm)
                        putExtra("duration_sec", run.durationSec)
                        putExtra("calories", run.calories)
                        putExtra("avg_pace", run.avgPace ?: 0.0)
                        putExtra("created_at", run.createdAt)
                    }
                    startActivity(summaryIntent)
                }
                .onFailure {
                    showToast(getString(R.string.error_saving_run))
                }

            stopService(Intent(this@RunningActivity, RunningService::class.java))
            finish()
        }
    }

    private fun updateUI() {
        val km = RunningService.totalDistance / 1000.0
        val sec = RunningService.durationSec
        b.tvKm.text = "%.2f".format(km)
        b.tvTimer.text = "%02d:%02d".format(sec / 60, sec % 60)
        b.tvKcal.text = "${(km * 70).toInt()}"

        // Actualizar posicion en el mapa
        val lastLoc = RunningService.lastLocation
        if (lastLoc != null) {
            val currentPoint = GeoPoint(lastLoc.latitude, lastLoc.longitude)
            if (!initialCentered) {
                mapView.controller.setCenter(currentPoint)
                mapView.controller.setZoom(17.0)
                initialCentered = true
            } else {
                mapView.controller.animateTo(currentPoint)
            }
        }

        // Dibujar ruta
        if (RunningService.routePoints.size > 1) {
            val geoPoints = RunningService.routePoints.mapNotNull { m ->
                val lat = m["lat"] ?: return@mapNotNull null
                val lng = m["lng"] ?: return@mapNotNull null
                GeoPoint(lat, lng)
            }

            polyline?.let { mapView.overlays.remove(it) }
            polyline = Polyline().apply {
                setPoints(geoPoints)
                outlinePaint.color = getColor(R.color.volt)
                outlinePaint.strokeWidth = 8f
            }
            mapView.overlays.add(polyline)
            mapView.invalidate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationOverlay()
            startRun()
        } else {
            showToast(getString(R.string.error_permissions_location))
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(uiUpdater)
        locationOverlay?.disableMyLocation()
        if (isBound) unbindService(serviceConnection)
    }
}
