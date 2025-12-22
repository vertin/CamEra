package com.task.camera.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatterTest {

    @Test
    fun `formatDuration should return zero time for zero milliseconds`() {
        // Given
        val ms = 0L

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("00:00", result)
    }

    @Test
    fun `formatDuration should format seconds only when less than one minute`() {
        // Given
        val ms = 5000L // 5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("00:05", result)
    }

    @Test
    fun `formatDuration should format minutes and seconds when less than one hour`() {
        // Given
        val ms = 125000L // 2 minutes 5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("02:05", result)
    }

    @Test
    fun `formatDuration should format hours minutes and seconds when one hour or more`() {
        // Given
        val ms = 3665000L // 1 hour 1 minute 5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("01:01:05", result)
    }

    @Test
    fun `formatDuration should handle 59 seconds correctly`() {
        // Given
        val ms = 59000L // 59 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("00:59", result)
    }

    @Test
    fun `formatDuration should handle 59 minutes 59 seconds correctly`() {
        // Given
        val ms = 3599000L // 59 minutes 59 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("59:59", result)
    }

    @Test
    fun `formatDuration should handle exactly one hour`() {
        // Given
        val ms = 3600000L // 1 hour

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("01:00:00", result)
    }

    @Test
    fun `formatDuration should handle exactly one minute`() {
        // Given
        val ms = 60000L // 1 minute

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("01:00", result)
    }

    @Test
    fun `formatDuration should handle large hours correctly`() {
        // Given
        val ms = 36661000L // 10 hours 11 minutes 1 second

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("10:11:01", result)
    }

    @Test
    fun `formatDuration should handle milliseconds less than one second`() {
        // Given
        val ms = 500L // 0.5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("00:00", result)
    }

    @Test
    fun `formatDuration should handle 23 hours 59 minutes 59 seconds`() {
        // Given
        val ms = 86399000L // 23:59:59

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("23:59:59", result)
    }

    @Test
    fun `formatDuration should handle 24 hours`() {
        // Given
        val ms = 86400000L // 24 hours

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("24:00:00", result)
    }

    @Test
    fun `formatDuration should handle single digit minutes and seconds`() {
        // Given
        val ms = 65000L // 1 minute 5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("01:05", result)
    }

    @Test
    fun `formatDuration should handle single digit hours minutes and seconds`() {
        // Given
        val ms = 3665000L // 1 hour 1 minute 5 seconds

        // When
        val result = Formatter.formatDuration(ms)

        // Then
        assertEquals("01:01:05", result)
    }
}
