package com.jeremy.keepingtrack.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import timber.log.Timber
import java.io.Serializable
import java.util.*

interface Repository {
    fun saveDrugCourse(course: DrugCourse): Boolean
    fun getAllDrugCourses(): List<DrugCourse>
    fun clearAllSavedCourses()
}

private const val SHARED_PREF_NAME = "PREFERENCES"
private const val KEY_COURSES = "COURSES"

class SharedPreferencesRepository(private val context: Context) : Repository {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
    private var gson: Gson = Gson()
    private var cachedCourses: List<DrugCourse> = Collections.emptyList()

    override fun saveDrugCourse(course: DrugCourse): Boolean {
        val jsonedEntry = gson.toJson(course)
        val jsonedSet = HashSet(sharedPreferences.getStringSet(KEY_COURSES, HashSet<String>()))
        val wasAdded = jsonedSet.add(jsonedEntry)
        return if (wasAdded) {
            cachedCourses = ArrayList(getAllDrugCourses()).apply { add(course) }
            sharedPreferences.edit().putStringSet(KEY_COURSES, jsonedSet).apply()
            true
        } else {
            false
        }
    }

    override fun getAllDrugCourses(): List<DrugCourse> {
        if (cachedCourses.isEmpty()) {
            cachedCourses = loadAllDrugCourses()
        }
        return cachedCourses
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

    override fun clearAllSavedCourses() {
        sharedPreferences.edit().remove(KEY_COURSES).apply()
    }
}

data class DrugCourse(val name: String, val dose: Float, val color: Int, val times: List<HourMinute>, val daysOfWeek: List<Int>) : Serializable