package com.jeremy.keepingtrack.features.scheduledose

import org.junit.Test

import org.junit.Assert.*

class HourMinuteTest {

    @Test
    fun whenValuesAreTheSame_deltaTo_returns0() {
        val a = HourMinute(0, 30)
        val b = HourMinute(0, 30)
        val delta = a.deltaTo(b)
        assertEquals(0, delta.hour)
        assertEquals(0, delta.minute)
    }

    @Test
    fun whenValuesAreSequentialSameHour_deltaTo_returnsCorrectDelta() {
        val a = HourMinute(0, 15)
        val b = HourMinute(0, 30)
        val delta = a.deltaTo(b)
        assertEquals(0, delta.hour)
        assertEquals(15, delta.minute)
    }

    @Test
    fun whenValuesAreReversedSameHour_deltaTo_returnsCorrectDelta() {
        val a = HourMinute(0, 25)
        val b = HourMinute(0, 10)
        val delta = a.deltaTo(b)
        assertEquals(0, delta.hour)
        assertEquals(-15, delta.minute)
    }

    @Test
    fun whenValuesAreSequentialDifferentHour_deltaTo_returnsCorrectDelta() {
        val a = HourMinute(0, 0)
        val b = HourMinute(1, 0)
        val delta = a.deltaTo(b)
        assertEquals(1, delta.hour)
        assertEquals(0, delta.minute)
    }

    @Test
    fun whenValuesAreReversedDifferentHour_deltaTo_returnsCorrectDelta() {
        val a = HourMinute(1, 0)
        val b = HourMinute(0, 0)
        val delta = a.deltaTo(b)
        assertEquals(-1, delta.hour)
        assertEquals(0, delta.minute)
    }

    @Test
    fun whenValuesAreInDifferentHours_deltaTo_returnsCorrectDelta() {
        val a = HourMinute(1, 0)
        val b = HourMinute(0, 50)
        val delta = a.deltaTo(b)
        assertEquals(0, delta.hour)
        assertEquals(-10, delta.minute)
    }
}