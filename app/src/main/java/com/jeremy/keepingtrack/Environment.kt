package com.jeremy.keepingtrack

import android.content.Context
import com.jeremy.keepingtrack.data.repository.Repository
import com.jeremy.keepingtrack.data.repository.SharedPreferencesRepository

object Environment {
    lateinit var repository: Repository


    fun initialise(context: Context) {
        repository = SharedPreferencesRepository(context)
    }
}