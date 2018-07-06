package com.jeremy.keepingtrack.features.scheduledose

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.jeremy.keepingtrack.Environment
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.Drug
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.repository.Repository
import com.pes.androidmaterialcolorpickerdialog.ColorPicker
import kotlinx.android.synthetic.main.activity_schedule.*
import timber.log.Timber
import java.util.*

class ScheduleActivity : AppCompatActivity() {
    private lateinit var repository: Repository
    private var color: Int
    private var startTime: HourMinute = HourMinute(7, 0)
    private var interval: HourMinute = HourMinute(3, 0)

    init {
        val rnd = Random()
        color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        repository = Environment.repository

        updateStartTime(startTime)
        updateEndTime(interval)

        button_firstDose.setOnClickListener {
            TimePickerDialog(
                    it.context,
                    { _, hourOfDay, minute -> updateStartTime(HourMinute(hourOfDay, minute)) },
                    startTime.hour, startTime.minute, true)
                    .apply {
                        setTitle(getString(R.string.title_first_dose))
                        show()
                    }
        }

        button_interval.setOnClickListener {
            TimePickerDialog(
                    it.context,
                    { _, hourOfDay, minute -> updateEndTime(HourMinute(hourOfDay, minute)) },
                    interval.hour, interval.minute, true)
                    .apply {
                        setTitle(R.string.title_last_dose)
                        show()
                    }
        }

        button_confirm.setOnClickListener { onConfirm() }
        readout_colour.setBackgroundColor(color)
        readout_colour.setOnClickListener {
            ColorPicker(this, Color.red(color), Color.green(color), Color.blue(color))
                    .apply {
                        this.setCancelable(true)
                        this.show()
                        this.setCallback {
                            updateColorSelection(it)
                            this.dismiss()
                        }
                    }
        }
    }

    private fun updateStartTime(hourMinute: HourMinute) {
        startTime = hourMinute
        button_firstDose.text = hourMinute.toString()
    }

    private fun updateEndTime(hourMinute: HourMinute) {
        interval = hourMinute
        button_interval.text = hourMinute.toString()
    }

    private fun updateColorSelection(color: Int) {
        this.color = color
        readout_colour.setBackgroundColor(color)
    }

    private fun onConfirm() {
        val name = input_name.editableText.toString()
        val doses = input_dose.value

        when {
            name.isBlank() -> {
                Toast.makeText(this, "please add a name", Toast.LENGTH_SHORT).show()
            }
            else -> {
                repository
                        .saveDrug(Drug(null, name, color, doses, startTime, interval))
                        .subscribe(
                                {
                                    onBackPressed()
                                },
                                {
                                    Timber.e(it)
                                    Toast.makeText(this, "couldn't save - maybe this already exists?", Toast.LENGTH_SHORT).show()
                                })
            }
        }
    }
}