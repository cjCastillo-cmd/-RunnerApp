package com.gymnasioforce.runnerapp.ui.run

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymnasioforce.runnerapp.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunSummaryActivityTest {

    private fun launchWithData(): ActivityScenario<RunSummaryActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), RunSummaryActivity::class.java).apply {
            putExtra("distance_km", 5.25)
            putExtra("duration_sec", 1800)
            putExtra("calories", 367)
            putExtra("avg_pace", 5.71)
            putExtra("created_at", "2026-03-16")
        }
        return ActivityScenario.launch(intent)
    }

    @Test
    fun summary_displaysRunData() {
        launchWithData().use {
            onView(withId(R.id.tvKm)).check(matches(withText("5.25")))
            onView(withId(R.id.tvDuration)).check(matches(withText("00:30:00")))
            onView(withId(R.id.tvCalories)).check(matches(withText("367")))
        }
    }

    @Test
    fun buttons_areVisible() {
        launchWithData().use {
            onView(withId(R.id.btnShare)).check(matches(isDisplayed()))
            onView(withId(R.id.btnClose)).check(matches(isDisplayed()))
        }
    }
}
