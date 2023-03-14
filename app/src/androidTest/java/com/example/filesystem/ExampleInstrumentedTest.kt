package com.example.filesystem

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.filesystem", appContext.packageName)
    }

    @Test
    fun testInitFragment() {
        val scenario = launchFragmentInContainer<InitFragment>()
        scenario.onFragment { fragment ->
            // https://stackoverflow.com/questions/62515314/android-espresso-does-not-have-a-navcontroller-set-error
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.InitFragment)
            navController.setGraph(navGraph, null)
        }
        onView(withId(R.id.button_init)).check(matches(isDisplayed()))
    }
}
