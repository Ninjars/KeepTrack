package com.jeremy.keepingtrack.features.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.AlarmManagerCompat
import com.jeremy.keepingtrack.data.HourMinute
import timber.log.Timber
import java.util.*

object ReminderUtils {
    fun setNextAlarm(context: Context, hourMinute: HourMinute) {
        val nowMillis = Calendar.getInstance().timeInMillis
        val alarm = Calendar.getInstance()
        alarm.set(Calendar.HOUR_OF_DAY, hourMinute.hour)
        alarm.set(Calendar.MINUTE, hourMinute.minute)
        var alarmMillis = alarm.timeInMillis

        while (alarmMillis < nowMillis) {
            alarm.set(Calendar.DAY_OF_YEAR, alarm.get(Calendar.DAY_OF_YEAR) + 1)
            alarmMillis = alarm.timeInMillis
        }

        Timber.d("setting alarm for $alarm")

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(KEY_INTENT_HOUR_MINUTE, hourMinute)
        val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alarmMillis, pendingIntent)
    }

    fun cancelAlarm(context: Context, hourMinute: HourMinute) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(KEY_INTENT_HOUR_MINUTE, hourMinute)
        val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}