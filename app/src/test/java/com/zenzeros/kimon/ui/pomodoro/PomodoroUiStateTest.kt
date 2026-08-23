package com.zenzeros.kimon.ui.pomodoro

import org.junit.Assert.assertEquals
import org.junit.Test

class PomodoroUiStateTest {

    @Test
    fun `initial state formatted time is 25 00`() {
        val state = PomodoroUiState(remainingSeconds = 25 * 60, totalSeconds = 25 * 60)
        assertEquals("25:00", state.formattedTime)
        assertEquals(25, state.minutesPart)
        assertEquals(0, state.secondsPart)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `halfway formatted time and progress are accurate`() {
        val state = PomodoroUiState(remainingSeconds = 12 * 60 + 30, totalSeconds = 25 * 60)
        assertEquals("12:30", state.formattedTime)
        assertEquals(12, state.minutesPart)
        assertEquals(30, state.secondsPart)
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `completed session formatted time is 00 00 and progress is 1 0`() {
        val state = PomodoroUiState(remainingSeconds = 0, totalSeconds = 25 * 60)
        assertEquals("00:00", state.formattedTime)
        assertEquals(1.0f, state.progress, 0.001f)
    }

    @Test
    fun `selectedTag defaults to null and can be assigned`() {
        val state = PomodoroUiState()
        assertEquals(null, state.selectedTag)

        val tag = com.zenzeros.kimon.data.local.entity.TagEntity(id = 1L, name = "Coding", colorHex = "#6366F1")
        val updatedState = state.copy(selectedTag = tag)
        assertEquals("Coding", updatedState.selectedTag?.name)
        assertEquals("#6366F1", updatedState.selectedTag?.colorHex)
    }
}
