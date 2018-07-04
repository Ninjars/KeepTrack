package com.jeremy.keepingtrack.features.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jeremy.keepingtrack.Environment
import com.jeremy.keepingtrack.TimeUtils
import timber.log.Timber

const val REMINDER_CODE = 1
const val KEY_INTENT_HOUR_MINUTE = "hourMinute"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when {
            intent.action == "android.intent.action.BOOT_COMPLETED" -> {
                // TODO: inject dependencies
                val now = TimeUtils.nowToHourMinute()
                Environment.repository.getNextDrugCourses(now)
                        .firstOrError()
                        .subscribe(
                                {
                                    ReminderUtils.setNextAlarm(context, it.time)
                                },
                                {
                                    Timber.e("failed to get next drug course")
                                })


            }
            intent.hasExtra(KEY_INTENT_HOUR_MINUTE) -> {
                val reminderIntent = Intent(context, ReminderActivity::class.java)
                reminderIntent.putExtra(KEY_INTENT_HOUR_MINUTE, intent.getSerializableExtra(KEY_INTENT_HOUR_MINUTE))
                context.startActivity(reminderIntent)

            }
            else -> Timber.w("received unexpected intent $intent")
        }
    }
}