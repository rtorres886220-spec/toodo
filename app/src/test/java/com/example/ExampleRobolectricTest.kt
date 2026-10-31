package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiService
import com.example.data.AnchorItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Anchor", appName)
    }

    @Test
    fun `heuristic parser extracts multiple items and tags`() {
        val geminiService = GeminiService()
        val input = "Buy milk #groceries and then call dentist tomorrow at 3pm urgent 30 mins"
        val parsed = geminiService.parseWithLocalHeuristics(input)

        assertTrue(parsed.isNotEmpty())
        val first = parsed[0]
        assertTrue(first.tags.contains("groceries"))
    }

    @Test
    fun `preferences persist user settings properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.data.AnchorPreferences.get(context)
        prefs.morningBriefTime = "07:30"
        prefs.eveningReviewTime = "21:15"
        prefs.quietHours = "23:00 - 06:00"

        val settings = prefs.getSettings()
        assertEquals("07:30", settings.morningTime)
        assertEquals("21:15", settings.eveningTime)
        assertEquals("23:00 - 06:00", settings.quietHours)
    }
}
