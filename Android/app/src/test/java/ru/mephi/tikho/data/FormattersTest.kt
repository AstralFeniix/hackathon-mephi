package ru.mephi.tikho.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun clockFormatsMinutesAndSeconds() {
        assertEquals("00:00", formatClock(0))
        assertEquals("01:05", formatClock(65_000))
        assertEquals("06:24", formatClock(384_000))
    }

    @Test
    fun fileSizeUsesRussianDecimalComma() {
        assertEquals("5,9 МБ", formatFileSize(6_144_044))
        assertEquals("512 КБ", formatFileSize(524_288))
        assertEquals("—", formatFileSize(0))
    }

    @Test
    fun processedTimeRoundsToNearestSecond() {
        assertEquals("04:06", formatProcessed(0.64f, 384_000))
        assertEquals("06:24", formatProcessed(1f, 384_000))
    }

    @Test
    fun remainingLabelMatchesExpectedCopy() {
        assertEquals("Осталось около минуты", remainingLabel(0.64f, 384_000))
        assertEquals("Осталось меньше минуты", remainingLabel(0.95f, 384_000))
    }
}
