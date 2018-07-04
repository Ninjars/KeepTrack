package com.jeremy.keepingtrack.data.repository

import com.jeremy.keepingtrack.data.DrugCourse
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.ScheduledSlot
import io.reactivex.Observable

interface Repository {
    fun saveDrugCourse(course: DrugCourse): Boolean
    fun getAllDrugCourses(): Observable<List<DrugCourse>>
    fun getNextDrugCourses(hourMinute: HourMinute): Observable<ScheduledSlot>
    fun getCoursesForTime(hourMinute: HourMinute): Observable<List<DrugCourse>>
    fun getTimesForCourse(course: DrugCourse): Observable<List<HourMinute>>
    fun clearAllSavedCourses()
}
