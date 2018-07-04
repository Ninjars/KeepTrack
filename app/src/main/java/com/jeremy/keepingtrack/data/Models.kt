package com.jeremy.keepingtrack.data

import java.io.Serializable

data class DrugCourse(val name: String, val dose: Float, val color: Int, val times: List<HourMinute>) : Serializable

data class ScheduledSlot(val time: HourMinute, val courses: List<DrugCourse>)