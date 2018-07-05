package com.jeremy.keepingtrack.data.repository

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import io.reactivex.Flowable

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
    abstract fun getAll(): Flowable<List<TimeSlotModel>>

    @Query("SELECT * FROM TimeSlotDb WHERE hour=:hour AND minute=:minute")
    abstract fun getMatchingImmediate(hour: Int, minute: Int): TimeSlotDb
}

@Dao
abstract class DrugTimeJoinDao : BaseDao<DrugTimeJoin>() {
    @Query("DELETE FROM drug_time_join WHERE drugId=:drugId")
    abstract fun deleteAllForDrug(drugId: Long)

    @Query("SELECT * FROM DrugDb INNER JOIN drug_time_join ON DrugDb.id=drug_time_join.drugId WHERE drug_time_join.timeSlotId=:timeSlotId")
    abstract fun getDrugsAtTimeSlot(timeSlotId: Long): Flowable<List<DrugDb>>

    @Query("SELECT * FROM TimeSlotDb INNER JOIN drug_time_join ON TimeSlotDb.id=drug_time_join.timeSlotId WHERE drug_time_join.drugId=:drugId")
    abstract fun getTimeSlotsForDrugImmediate(drugId: Long): List<TimeSlotDb>

    @Query("SELECT * FROM TimeSlotDb INNER JOIN drug_time_join ON TimeSlotDb.id=drug_time_join.timeSlotId WHERE drug_time_join.drugId=:drugId")
    abstract fun getTimeSlotsForDrug(drugId: Long): Flowable<List<TimeSlotDb>>
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

class TimeSlotModel {
    @Embedded
    lateinit var timeSlot: TimeSlotDb
    @Relation(parentColumn = "id", entityColumn = "id")
    lateinit var drugs: List<DrugDb>
}