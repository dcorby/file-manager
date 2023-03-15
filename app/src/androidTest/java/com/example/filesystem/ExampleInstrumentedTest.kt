package com.example.filesystem

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    var intentsTestRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.filesystem", appContext.packageName)
    }

    /*
    navController.setGraph(...) throws:
        java.lang.IllegalStateException: Method addObserver must be called on the main thread
    @Test
    fun testNavController() {
        // https://stackoverflow.com/questions/62515314/android-espresso-does-not-have-a-navcontroller-set-error
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.InitFragment)
        navController.setGraph(navGraph, null)

        onView(withId(R.id.button_init)).check(matches(isDisplayed()))
    }
    */

    @Test
    fun testInitFragment() {
        // an empty activity to host the fragment
        val scenario = launchFragmentInContainer<InitFragment>()
        scenario.onFragment { fragment ->
            // can call public functions of the fragment
        }
        onView(withId(R.id.button_init)).check(matches(isDisplayed()))
    }

    @Test
    fun testInitFragmentGetIntent() {
        launchFragmentInContainer<InitFragment>()
        onView(withId(R.id.button_init)).perform(click())
        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
    }

    @Test
    fun testInitFragmentGetActivityResult() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        intent.data = "foo".toUri()
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        val data = result.resultData.data
        assertTrue(data is Uri)
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE)).respondWith(result)
    }
}