package com.jeremy.keepingtrack.features.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jeremy.keepingtrack.TimeUtils
import timber.log.Timber

const val REMINDER_CODE = 1
const val KEY_DRUG_IDS = "drugIds"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("onReceive $intent ${intent.extras} ${intent.getLongExtra(KEY_DRUG_IDS, -1)}")
        when {
            intent.action == "android.intent.action.BOOT_COMPLETED" -> {
                // TODO: inject dependencies
                val now = TimeUtils.nowToHourMinute()
//                Environment.repository.getNextTimeSlot(now)
//                        .firstOrError()
//                        .subscribe(
//                                {
//                                    ReminderUtils.setNextAlarm(context, it.time)
//                                },
//                                {
//                                    Timber.e("failed to get next drug course")
//                                })


            }
            intent.hasExtra(KEY_DRUG_IDS) -> {
                val reminderIntent = Intent(context, ReminderActivity::class.java)
                reminderIntent.putExtra(KEY_DRUG_IDS, intent.getLongArrayExtra(KEY_DRUG_IDS))
                context.startActivity(reminderIntent)

            }
            else -> Timber.w("received unexpected intent $intent")
        }
    }
}