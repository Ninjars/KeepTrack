package com.jeremy.keepingtrack.data

import java.io.Serializable

data class HourMinute(val hour: Int, val minute: Int) : Serializable {
    fun deltaTo(other: HourMinute): HourMinute {
        val hourDelta = other.hour - hour
        val totalMinuteDelta = hourDelta * 60 + other.minute - minute
        return HourMinute(totalMinuteDelta / 60, totalMinuteDelta % 60)
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