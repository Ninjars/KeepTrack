package com.jeremy.keepingtrack.features.reminders

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jeremy.keepingtrack.Environment
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.TimeUtils
import com.jeremy.keepingtrack.data.repository.Repository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_reminder.*
import timber.log.Timber

class ReminderActivity : AppCompatActivity() {
    private lateinit var repository: Repository
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = Environment.repository

        disposable.add(repository.getTimeSlotDrugs()
                .firstElement()
                .subscribe {
                    ReminderUtils.setAlarms(this, it)
                })

        val ids = intent.getLongArrayExtra(KEY_DRUG_IDS)
        when {
            ids.isEmpty() -> {
                Timber.w("launched ReminderActivity without drug ids")
                finish()
            }
            ids.size == 1 -> setupSingleReminderView(ids[0])
            else -> setupMultipleReminderView(ids)
        }

    }

    private fun setupSingleReminderView(id: Long) {
        setContentView(R.layout.activity_reminder)
        disposable.add(
                repository.fetchDrug(id)
                        .subscribe {
                            readout_name.text = it.name
                            // TODO: display intended time of this dose with offset to now
                            // TODO: display time since last dose vs interval for this drug
                            readout_time.text = TimeUtils.nowToHourMinute().toString()
                            button_takeDose.setOnClickListener {
                                // TODO: record drug taken at time
                                onBackPressed()
                            }
                            button_remindDose.setOnClickListener {
                                // TODO: schedule new reminder
                                onBackPressed()
                            }
                            button_rejectDose.setOnClickListener {
                                // TODO: record timeslot rejected
                                onBackPressed()
                            }
                        }
        )
    }

    private fun setupMultipleReminderView(ids: LongArray) {
        setContentView(R.layout.activity_reminder_multiple)
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }
}