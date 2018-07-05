package com.jeremy.keepingtrack.data

data class Drug(val drugId: Long?, val name: String, val dose: Float, val color: Int)

data class DrugWithTimes(val drugId: Long?, val name: String, val dose: Float, val color: Int, val times: List<HourMinute>) {
    fun toDrug(): Drug {
        return Drug(drugId, name, dose, color)
    }
}

data class TimeSlotDrugs(val time: HourMinute, val drugs: List<Drug>)