package com.gymnasioforce.runnerapp.ui.main

import android.os.Bundle
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        b.bottomNav.setupWithNavController(navHost.navController)
    }
}
