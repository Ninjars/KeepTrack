package com.jeremy.keepingtrack.features.reminders

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jeremy.keepingtrack.Environment
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.repository.Repository

class ReminderActivity : AppCompatActivity() {
    private lateinit var repository: Repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        repository = Environment.repository
    }
}