package com.gymnasioforce.runnerapp.ui.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymnasioforce.runnerapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginForm_displaysAllElements() {
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun emptyFields_loginButtonStillClickable() {
        onView(withId(R.id.btnLogin)).check(matches(isEnabled()))
        onView(withId(R.id.btnLogin)).perform(click())
    }

    @Test
    fun typeEmail_displaysCorrectly() {
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.etEmail))
            .check(matches(withText("test@example.com")))
    }

    @Test
    fun registerLink_isVisible() {
        onView(withId(R.id.btnGoRegister)).check(matches(isDisplayed()))
    }
}
