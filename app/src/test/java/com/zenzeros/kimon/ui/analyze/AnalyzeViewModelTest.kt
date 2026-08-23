package com.zenzeros.kimon.ui.analyze

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzeViewModelTest {

    @Test
    fun `formatDuration formats zero and sub-hour values correctly`() {
        assertEquals("0m", AnalyzeViewModel.formatDuration(0))
        assertEquals("5m", AnalyzeViewModel.formatDuration(300))
        assertEquals("45m", AnalyzeViewModel.formatDuration(2700))
    }

    @Test
    fun `formatDuration formats exact hours correctly`() {
        assertEquals("1h", AnalyzeViewModel.formatDuration(3600))
        assertEquals("2h", AnalyzeViewModel.formatDuration(7200))
    }

    @Test
    fun `formatDuration formats hours and minutes correctly`() {
        assertEquals("1h 15m", AnalyzeViewModel.formatDuration(3600 + 900))
        assertEquals("3h 45m", AnalyzeViewModel.formatDuration(3 * 3600 + 45 * 60))
    }
}
