package com.jeremy.keepingtrack.features.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.AlarmManagerCompat
import com.jeremy.keepingtrack.data.Drug
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.TimeSlotDrugs
import timber.log.Timber
import java.util.*

object ReminderUtils {

    fun cancelAlarm(context: Context, hourMinute: HourMinute) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(KEY_DRUG_IDS, hourMinute)
        val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun setAlarms(context: Context, drugs: List<TimeSlotDrugs>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (timeSlotDrug in drugs) {
            val millis = hourMinuteToLong(timeSlotDrug.time)
            val ids = timeSlotDrug.drugs.map { it.drugId!! }.toTypedArray()
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(KEY_DRUG_IDS, ids)
            val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, millis, pendingIntent)
        }
    }

    fun setAlarmsForDrug(context: Context, drug: Drug) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (pair in createPendingIntentsForDrug(context, drug)) {
            Timber.i("setAlarm for ${drug.name} at ${pair.first}")
            setAlarm(alarmManager, pair.first, pair.second)
        }
    }

    private fun setAlarm(alarmManager: AlarmManager, time: Long, intent: PendingIntent) {
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, time, intent)
    }

    private fun createPendingIntentsForDrug(context: Context, drug: Drug): List<Pair<Long, PendingIntent>> {
        return drug.times.map {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(KEY_DRUG_IDS, arrayOf(drug.drugId))
            Pair(hourMinuteToLong(it), PendingIntent.getBroadcast(context, REMINDER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }

    private fun hourMinuteToLong(hourMinute: HourMinute): Long {
        val nowMillis = Calendar.getInstance().timeInMillis
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourMinute.hour)
        calendar.set(Calendar.MINUTE, hourMinute.minute)
        calendar.set(Calendar.SECOND, 0)
        var alarmMillis = calendar.timeInMillis

        while (alarmMillis < nowMillis) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1)
            alarmMillis = calendar.timeInMillis
        }

        return alarmMillis
    }
}