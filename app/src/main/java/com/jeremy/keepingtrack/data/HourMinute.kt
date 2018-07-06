package com.jeremy.keepingtrack.data

import java.io.Serializable
import java.text.DecimalFormat

data class HourMinute(val hour: Int, val minute: Int) : Serializable {
    private val timeFormat = DecimalFormat("00")

    fun deltaTo(other: HourMinute): HourMinute {
        val hourDelta = other.hour - hour
        val totalMinuteDelta = hourDelta * 60 + other.minute - minute
        return HourMinute(totalMinuteDelta / 60, totalMinuteDelta % 60)
    }

    fun deltaTo(otherHour: Int, otherMinute: Int): HourMinute {
        val hourDelta = otherHour - hour
        val totalMinuteDelta = hourDelta * 60 + otherMinute - minute
        return HourMinute(totalMinuteDelta / 60, totalMinuteDelta % 60)
    }

    fun increment(hour: Int, minute: Int): HourMinute {
        val minutesSum = this.minute + minute
        val hourFromMinutes = minutesSum / 60
        val hourSum = (this.hour + hour + hourFromMinutes) % 24
        return HourMinute(hourSum, minutesSum % 60)
    }

    fun increment(value: HourMinute): HourMinute {
        val minutesSum = value.minute + minute
        val hourFromMinutes = minutesSum / 60
        val hourSum = value.hour + hourFromMinutes % 24
        return HourMinute(hourSum, minutesSum % 60)
    }

    fun isPositive(): Boolean {
        return hour >= 0 && minute >= 0
    }

    fun isLessThan(other: HourMinute): Boolean {
        return when {
            hour < other.hour -> true
            hour == other.hour && minute < other.minute -> true
            else -> false
        }
    }

    override fun toString(): String {
        return "${timeFormat.format(hour)}:${timeFormat.format(minute)}"
    }
}

object HourMinuteComparator : Comparator<HourMinute> {
    override fun compare(a: HourMinute, b: HourMinute): Int {
        return when {
            a.hour < b.hour -> -1
            a.hour > b.hour -> 1
            a.minute < b.minute -> -1
            a.minute > b.minute -> 1
            else -> 0
        }
    }
}

class HourMinuteOffsetComparator(private val currentTime: HourMinute) : Comparator<HourMinute> {
    override fun compare(a: HourMinute, b: HourMinute): Int {
        val offsetA = currentTime.deltaTo(a)
        val offsetB = currentTime.deltaTo(b)
        return when {
            offsetA.isPositive() && !offsetB.isPositive() -> -1
            offsetB.isPositive() && !offsetA.isPositive() -> 1
            a.isLessThan(b) -> -1
            b.isLessThan(a) -> 1
            else -> 0
        }
    }
}