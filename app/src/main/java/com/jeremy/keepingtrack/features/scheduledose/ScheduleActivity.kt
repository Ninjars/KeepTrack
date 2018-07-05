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

    private lateinit var adapter: ScheduledTimingsAdapter
    private lateinit var repository: Repository
    private var color: Int

    init {
        val rnd = Random()
        color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        repository = Environment.repository

        adapter = ScheduledTimingsAdapter()
        readout_times.adapter = adapter

        button_addTime.setOnClickListener {
            TimePickerDialog(
                    it.context,
                    { _, hourOfDay, minute -> adapter.addItem(HourMinute(hourOfDay, minute)) },
                    7, 0, true)
                    .apply {
                        setTitle("Select Time")
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

    private fun updateColorSelection(color: Int) {
        this.color = color
        readout_colour.setBackgroundColor(color)
    }

    private fun onConfirm() {
        val doseString = input_dose.editableText.toString()

        val name = input_name.editableText.toString()
        val dosage = if (doseString.isBlank()) -1f else doseString.toFloat()
        val hourMinutes = adapter.getData()

        when {
            name.isBlank() -> {
                Toast.makeText(this, "please add a name", Toast.LENGTH_SHORT).show()
            }
            dosage <= 0 -> {
                Toast.makeText(this, "please add a dosage", Toast.LENGTH_SHORT).show()
            }
            hourMinutes.isEmpty() -> {
                Toast.makeText(this, "please add at least one time", Toast.LENGTH_SHORT).show()
            }
            else -> {
                repository
                        .saveDrugCourse(Drug(null, name, dosage, color), hourMinutes)
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