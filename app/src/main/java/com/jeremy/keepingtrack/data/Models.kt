package com.jeremy.keepingtrack.data

class Drug(val drugId: Long?, val name: String, val color: Int, val dosesPerDay: Int, val first: HourMinute, val interval: HourMinute) {
    val times: List<HourMinute>

    init {
        times = ArrayList()
        for (i in 0 until dosesPerDay) {
            val hour = interval.hour * i
            val minute = interval.minute * i
            times.add(first.increment(hour, minute))
        }
    }

    fun nextTime(currentTime: HourMinute): HourMinute {
        for (time in times) {
            if (currentTime.isLessThan(time)) {
                return time
            }
        }
        return first
    }
}

data class TimeSlotDrugs(val time: HourMinute, val drugs: List<Drug>)