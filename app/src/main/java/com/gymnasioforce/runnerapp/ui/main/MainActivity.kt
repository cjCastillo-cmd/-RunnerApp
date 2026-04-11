package com.gymnasioforce.runnerapp.ui.main

import android.os.Bundle
import android.util.Log
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.data.SyncManager
import com.gymnasioforce.runnerapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Activity principal con bottom navigation.
 * Al iniciar, sincroniza carreras pendientes (guardadas offline).
 */
class MainActivity : BaseActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Configurar navegacion
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        b.bottomNav.setupWithNavController(navHost.navController)

        // Sincronizar carreras pendientes al abrir la app
        syncPendingRuns()
    }

    private fun syncPendingRuns() {
        lifecycleScope.launch {
            val syncManager = SyncManager(this@MainActivity)
            val count = syncManager.syncPendingRuns()
            if (count > 0) {
                Log.d("MainActivity", "$count carrera(s) sincronizada(s)")
            }
        }
    }
}
