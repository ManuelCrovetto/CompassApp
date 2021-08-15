package com.macrosystems.compassapp.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.ui.view.core.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CompassFragmentTest {

    @get: Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun test_isCompassVisible_onLaunch(){
        onView(withId(R.id.ivCompass)).check(matches(isDisplayed()))
    }

    @Test
    fun test_isDegreesIndicatorVisible_onLaunch(){
        onView(withId(R.id.tvDegrees)).check(matches(isDisplayed()))
    }
}