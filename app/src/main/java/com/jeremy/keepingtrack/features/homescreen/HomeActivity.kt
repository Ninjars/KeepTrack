package com.jeremy.keepingtrack.features.homescreen

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.jeremy.keepingtrack.Environment
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.TimeUtils
import com.jeremy.keepingtrack.data.Repository
import com.jeremy.keepingtrack.features.scheduledose.ScheduleActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

    private lateinit var repository: Repository

    private lateinit var courseAdapter: DrugCourseAdapter

    private lateinit var updateDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        repository = Environment.repository
        courseAdapter = DrugCourseAdapter()
        readout_courses.adapter = courseAdapter

        fab.setOnClickListener { _ ->
            this.startActivity(Intent(this, ScheduleActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        val hourMinute = TimeUtils.nowToHourMinute()
        courseAdapter.updateData(hourMinute, repository.getAllDrugCourses())
        updateDisposable = Observable.interval(30, TimeUnit.SECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { refreshNextDose() }
    }

    override fun onPause() {
        updateDisposable.dispose()
        super.onPause()
    }

    private fun refreshNextDose() {
        val hourMinute = TimeUtils.nowToHourMinute()
        courseAdapter.updateCurrentTime(hourMinute)
        // TODO: how to show on ui?
    }

}
