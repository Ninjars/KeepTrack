package com.jeremy.keepingtrack

import com.jeremy.keepingtrack.data.HourMinute
import java.util.*

object TimeUtils {
    fun nowToHourMinute(): HourMinute {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        return HourMinute(hour, minute)
    }
}