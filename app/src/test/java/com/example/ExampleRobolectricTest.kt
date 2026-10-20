package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.FileUtils
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
        assertEquals("MarkFlow", appName)
    }

    @Test
    fun `calculate document statistics correctly`() {
        val markdown = "# Hello World\n\nThis is a test of MarkFlow editor."
        val stats = FileUtils.calculateStats(markdown)
        assertEquals(3, stats.lines)
        assertTrue(stats.words >= 8)
        assertTrue(stats.characters > 20)
    }
}
