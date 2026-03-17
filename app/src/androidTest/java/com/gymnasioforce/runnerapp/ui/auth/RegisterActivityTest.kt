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
class RegisterActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    @Test
    fun registerForm_displaysAllElements() {
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.etConfirmPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun typeFields_displaysCorrectly() {
        onView(withId(R.id.etName))
            .perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.etName))
            .check(matches(withText("Test User")))
    }

    @Test
    fun registerButton_isEnabled() {
        onView(withId(R.id.btnRegister)).check(matches(isEnabled()))
    }
}
