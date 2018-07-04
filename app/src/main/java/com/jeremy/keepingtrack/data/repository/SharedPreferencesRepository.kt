package com.jeremy.keepingtrack.data.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jeremy.keepingtrack.data.DrugCourse
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.ScheduledSlot
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*

private const val SHARED_PREF_NAME = "PREFERENCES"
private const val KEY_COURSES = "COURSES"

class SharedPreferencesRepository(private val context: Context) : Repository {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
    private var gson: Gson = Gson()
    private var observableCache = BehaviorSubject.createDefault(Collections.emptyList<DrugCourse>())

    init {
        observableCache.onNext(loadAllDrugCourses())
    }

    override fun saveDrugCourse(course: DrugCourse): Boolean {
        val jsonedEntry = gson.toJson(course)
        val jsonedSet = HashSet(sharedPreferences.getStringSet(KEY_COURSES, HashSet<String>()))
        val wasAdded = jsonedSet.add(jsonedEntry)
        return if (wasAdded) {
            val list = ArrayList(observableCache.value).apply { add(course) }
            observableCache.onNext(list)
            sharedPreferences.edit().putStringSet(KEY_COURSES, jsonedSet).apply()
            true
        } else {
            false
        }
    }

    override fun getAllDrugCourses(): Observable<List<DrugCourse>> {
        return observableCache
    }

    private fun loadAllDrugCourses(): List<DrugCourse> {
        val values = sharedPreferences.getStringSet(KEY_COURSES, HashSet<String>())
        val jsonedList = ArrayList(values)

        Timber.d("loaded values: $values")
        Timber.d("loaded jsonedList: $jsonedList")
        return try {
            jsonedList.map { gson.fromJson(it, DrugCourse::class.java) }
        } catch (e: JsonSyntaxException) {
            Toast.makeText(context, "Failed to load saved courses", Toast.LENGTH_SHORT).show()
            clearAllSavedCourses()
            Collections.emptyList()
        }
    }

    override fun getNextDrugCourses(hourMinute: HourMinute): Observable<ScheduledSlot> {
        val allCourses = getAllDrugCourses()
        return allCourses.map {
            var bestOffset = HourMinute(23, 59)
            val courses = ArrayList<DrugCourse>()
            for (course in it) {
                for (time in course.times) {
                    val offset = hourMinute.deltaTo(time)
                    if (offset == bestOffset) {
                        courses.add(course)

                    } else if (offset.isPositive() && offset.isLessThan(bestOffset)) {
                        bestOffset = offset
                        courses.clear()
                        courses.add(course)
                        break
                    }
                }
            }
            ScheduledSlot(bestOffset, courses)
        }
    }

    override fun clearAllSavedCourses() {
        sharedPreferences.edit().remove(KEY_COURSES).apply()
    }

    override fun getCoursesForTime(hourMinute: HourMinute): Observable<List<DrugCourse>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTimesForCourse(course: DrugCourse): Observable<List<HourMinute>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}