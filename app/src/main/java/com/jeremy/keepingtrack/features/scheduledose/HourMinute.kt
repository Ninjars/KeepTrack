package com.jeremy.keepingtrack.features.scheduledose

import java.io.Serializable

data class HourMinute(val hour: Int, val minute: Int) : Serializable {
    fun deltaTo(other: HourMinute): HourMinute {
        val hourDelta = other.hour - hour
        val totalMinuteDelta = hourDelta * 60 + other.minute - minute
        return HourMinute(totalMinuteDelta / 60, totalMinuteDelta % 60)
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