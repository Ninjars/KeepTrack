package com.jeremy.keepingtrack.data.repository

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.jeremy.keepingtrack.data.HourMinute
import io.reactivex.Flowable

@Database(entities = [DrugDb::class, RecordedDoseDb::class], version = 1)
@TypeConverters(HourMinuteConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun recordedDoseDao(): RecordedDoseDao
}

@Entity
data class DrugDb(@PrimaryKey(autoGenerate = true) var id: Long = 0, val name: String, val color: Int, val dosesPerDay: Int, val first: HourMinute, val interval: HourMinute, val isActive: Boolean)

@Dao
abstract class DrugDao : BaseDao<DrugDb>() {
    @Query("SELECT * FROM DrugDb")
    abstract fun getAll(): Flowable<List<DrugDb>>
}

@Entity(
        foreignKeys = [
            ForeignKey(entity = DrugDb::class, parentColumns = ["id"], childColumns = ["drugId"], onDelete = CASCADE)
        ],
        indices = [Index(value = ["drugId"])]
)
data class RecordedDoseDb(@PrimaryKey(autoGenerate = true) var id: Long = 0, val drugId: Int, val timestamp: Long)


@Dao
abstract class RecordedDoseDao : BaseDao<RecordedDoseDb>() {
    @Query("SELECT * FROM RecordedDoseDb")
    abstract fun getAll(): Flowable<List<RecordedDoseDb>>

    @Query("SELECT * FROM RecordedDoseDb WHERE drugId=:drugId")
    abstract fun getAllForDrug(drugId: Long): Flowable<List<RecordedDoseDb>>
}

class HourMinuteConverter {
    @TypeConverter
    fun convertHourMinuteToStoreVal(hourMinute: HourMinute): String {
        return hourMinute.toString()
    }

    @TypeConverter
    fun convertFomStoreValueToHourMinute(value: String): HourMinute {
        val splits = value.split(":", ignoreCase = true)
        return HourMinute(splits[0].toInt(), splits[1].toInt())
    }
}
