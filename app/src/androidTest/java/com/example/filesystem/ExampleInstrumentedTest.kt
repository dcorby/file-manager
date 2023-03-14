package com.example.filesystem

import android.content.Intent
import android.provider.DocumentsContract
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    public val intentsTestRule: IntentsTestRule<MainActivity> = IntentsTestRule<MainActivity>(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.filesystem", appContext.packageName)
    }

    @Test
    fun validatePickDocumentIntent() {
        Espresso.onView(withId(R.id.button_init)).perform(click())
        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
        intended(hasType(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
    }
}
