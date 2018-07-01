package com.jeremy.keepingtrack

import com.jeremy.keepingtrack.features.scheduledose.HourMinute
import java.text.DecimalFormat

object FormatUtils {

    private val doseFormat = DecimalFormat("#.##")
    private val timeFormat = DecimalFormat("00")

    fun formatDose(dose: Float): CharSequence {
        return doseFormat.format(dose)
    }

    fun formatHourMinute(hourMinute: HourMinute): CharSequence {
        return "${timeFormat.format(hourMinute.hour)}:${timeFormat.format(hourMinute.minute)}"
    }
}