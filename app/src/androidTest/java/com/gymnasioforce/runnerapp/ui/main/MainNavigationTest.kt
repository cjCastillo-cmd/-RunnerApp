package com.gymnasioforce.runnerapp.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymnasioforce.runnerapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun bottomNavigation_isVisible() {
        onView(withId(R.id.bottomNav)).check(matches(isDisplayed()))
    }

    @Test
    fun fab_isVisible() {
        onView(withId(R.id.fabRun)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToStats_showsStatsFragment() {
        onView(withId(R.id.nav_stats)).perform(click())
        onView(withText(R.string.title_your_stats)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSocial_showsFriendsFragment() {
        onView(withId(R.id.nav_social)).perform(click())
        onView(withText(R.string.title_social)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToProfile_showsProfileFragment() {
        onView(withId(R.id.nav_profile)).perform(click())
        onView(withText(R.string.label_edit_profile)).check(matches(isDisplayed()))
    }
}
