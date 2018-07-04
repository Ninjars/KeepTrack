package com.jeremy.keepingtrack.data

import io.reactivex.Observable
import java.io.Serializable

interface Repository {
    fun saveDrugCourse(course: DrugCourse): Boolean
    fun getAllDrugCourses(): Observable<List<DrugCourse>>
    fun getNextDrugCourses(hourMinute: HourMinute): Observable<ScheduledSlot>
    fun getCoursesForTime(hourMinute: HourMinute): Observable<List<DrugCourse>>
    fun getTimesForCourse(course: DrugCourse): Observable<List<HourMinute>>
    fun clearAllSavedCourses()
}

data class DrugCourse(val name: String, val dose: Float, val color: Int, val times: List<HourMinute>) : Serializable

data class ScheduledSlot(val time: HourMinute, val courses: List<DrugCourse>)
