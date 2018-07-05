package com.jeremy.keepingtrack.data.repository

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.jeremy.keepingtrack.data.HourMinute
import io.reactivex.Flowable
import io.reactivex.Maybe
import timber.log.Timber

@Database(entities = [TimeSlotDb::class, DrugDb::class, DrugTimeJoin::class, RecordedDoseDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun TimeSlotDao(): TimeSlotDao
    abstract fun DrugDao(): DrugDao
    abstract fun DrugTimeJoinDao(): DrugTimeJoinDao
    abstract fun RecordedDoseDao(): RecordedDoseDao
}

@Entity
data class DrugDb(@PrimaryKey(autoGenerate = true) var id: Long = 0, val name: String, val color: Int, val dose: Float, val isActive: Boolean)

@Entity(indices = [Index(value = ["hour", "minute"], unique = true)])
data class TimeSlotDb(@PrimaryKey(autoGenerate = true) var id: Long = 0, val hour: Int, val minute: Int)

@Entity(
        tableName = "drug_time_join",
        primaryKeys = ["drugId", "timeSlotId"],
        indices = [Index(value = ["drugId", "timeSlotId"])],
        foreignKeys = [
            ForeignKey(entity = DrugDb::class, parentColumns = ["id"], childColumns = ["drugId"]),
            ForeignKey(entity = TimeSlotDb::class, parentColumns = ["id"], childColumns = ["timeSlotId"])
        ]
)
data class DrugTimeJoin(val drugId: Long, val timeSlotId: Long)

@Entity(
        foreignKeys = [
            ForeignKey(entity = DrugDb::class, parentColumns = ["id"], childColumns = ["drugId"], onDelete = CASCADE)
        ],
        indices = [Index(value = ["drugId"])]
)
data class RecordedDoseDb(@PrimaryKey(autoGenerate = true) var id: Long = 0, val drugId: Int, val timestamp: Long)

@Dao
abstract class DrugDao : BaseDao<DrugDb>() {
    @Query("SELECT * FROM DrugDb")
    abstract fun getAll(): Flowable<List<DrugModel>>
}

@Dao
abstract class TimeSlotDao : BaseDao<TimeSlotDb>() {
    @Query("SELECT * FROM TimeSlotDb")
    abstract fun getAll(): Flowable<List<TimeSlotDb>>

    @Query("SELECT * FROM TimeSlotDb WHERE hour=:hour AND minute=:minute")
    abstract fun getMatchingImmediate(hour: Int, minute: Int): TimeSlotDb?
}

@Dao
abstract class DrugTimeJoinDao : BaseDao<DrugTimeJoin>() {
    @Query("DELETE FROM drug_time_join WHERE drugId=:drugId")
    abstract fun deleteAllForDrug(drugId: Long)

    @Query("SELECT * FROM DrugDb INNER JOIN drug_time_join ON DrugDb.id=drug_time_join.drugId WHERE drug_time_join.timeSlotId=:timeSlotId")
    abstract fun getDrugsAtTimeSlot(timeSlotId: Long): Flowable<List<DrugDb>>

    @Query("SELECT * FROM DrugDb INNER JOIN drug_time_join ON DrugDb.id=drug_time_join.drugId WHERE drug_time_join.timeSlotId=:timeSlotId")
    abstract fun getDrugsAtTimeSlotImmediate(timeSlotId: Long): List<DrugDb>

    @Query("SELECT * FROM TimeSlotDb INNER JOIN drug_time_join ON TimeSlotDb.id=drug_time_join.timeSlotId WHERE drug_time_join.drugId=:drugId")
    abstract fun getTimeSlotsForDrugImmediate(drugId: Long): List<TimeSlotDb>

    @Query("SELECT * FROM TimeSlotDb INNER JOIN drug_time_join ON TimeSlotDb.id=drug_time_join.timeSlotId WHERE drug_time_join.drugId=:drugId")
    abstract fun getTimeSlotsForDrug(drugId: Long): Flowable<List<TimeSlotDb>>

    @Transaction
    open fun createOrUpdate(database: AppDatabase, drugId: Long, times: List<HourMinute>) {
        deleteAllForDrug(drugId)
        val timeSlots = times.map {
            TimeSlotDb(hour = it.hour, minute = it.minute)
        }
        database.TimeSlotDao().upsert(timeSlots)
        val timeSlotIds = times.map {
            val existingTimeSlotEntry = database.TimeSlotDao().getMatchingImmediate(it.hour, it.minute)
            when (existingTimeSlotEntry) {
                null -> database.TimeSlotDao().insert(TimeSlotDb(hour = it.hour, minute = it.minute))
                else -> existingTimeSlotEntry.id
            }
        }
        val joinEntries = timeSlotIds.map {
            Timber.i("creating drugTimeJoint $drugId, $it")
            DrugTimeJoin(drugId, it)
        }
        insert(joinEntries)
    }

    @Transaction
    open fun getAllTimeSlotsPopulated(database: AppDatabase): Maybe<List<TimeSlotModel>> {
        return database.TimeSlotDao()
                .getAll()
                .map {
                    it.map { timeSlot ->
                        TimeSlotModel(timeSlot, getDrugsAtTimeSlotImmediate(timeSlot.id))
                    }
                }
                .firstElement()
    }
}

@Dao
abstract class RecordedDoseDao : BaseDao<RecordedDoseDb>() {
    @Query("SELECT * FROM RecordedDoseDb")
    abstract fun getAll(): Flowable<List<RecordedDoseDb>>

    @Query("SELECT * FROM RecordedDoseDb WHERE drugId=:drugId")
    abstract fun getAllForDrug(drugId: Long): Flowable<List<RecordedDoseDb>>
}

class DrugModel {
    @Embedded
    lateinit var drug: DrugDb
    @Relation(parentColumn = "id", entityColumn = "id")
    lateinit var times: List<TimeSlotDb>
}

data class TimeSlotModel(val timeSlot: TimeSlotDb, val drugs: List<DrugDb>)
