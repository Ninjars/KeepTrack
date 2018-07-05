package com.jeremy.keepingtrack.data.repository

import android.arch.persistence.room.Room
import android.content.Context
import com.jeremy.keepingtrack.data.Drug
import com.jeremy.keepingtrack.data.DrugWithTimes
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.TimeSlotDrugs
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface Repository {
    fun saveDrugCourse(drug: Drug, times: List<HourMinute>): Single<Long>
    fun getAllDrugCourses(): Flowable<List<DrugWithTimes>>
    fun getAllTimeSlotDrugs(hourMinute: HourMinute): Maybe<List<TimeSlotDrugs>>
    fun getNextTimeSlotDrugs(hourMinute: HourMinute): Maybe<TimeSlotDrugs>
}

class RoomRepository(context: Context) : Repository {

    private val database: AppDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app-database").build()

    override fun saveDrugCourse(drug: Drug, times: List<HourMinute>): Single<Long> {
        return Single.just(drug)
                .map {
                    val id = when {
                        it.drugId == null -> {
                            database.DrugDao().insert(DrugDb(name = it.name, color = it.color, dose = it.dose, isActive = true))

                        }
                        else -> {
                            database.DrugDao().update(DrugDb(id = it.drugId, name = it.name, color = it.color, dose = it.dose, isActive = true))
                            it.drugId
                        }
                    }
                    database.DrugTimeJoinDao().createOrUpdate(database, id, times)
                    id
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getAllDrugCourses(): Flowable<List<DrugWithTimes>> {
        return database.DrugDao()
                .getAll()
                .map {
                    it.map {
                        DrugWithTimes(it.drug.id, it.drug.name, it.drug.dose, it.drug.color, it.times.map {
                            HourMinute(it.hour, it.minute)
                        })
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getAllTimeSlotDrugs(hourMinute: HourMinute): Maybe<List<TimeSlotDrugs>> {
        return Single.just(database)
                .flatMapMaybe {
                    it.DrugTimeJoinDao()
                            .getAllTimeSlotsPopulated(it)
                            .map {
                                it.sortedWith(TimeSlotModelComparator(hourMinute))
                            }
                            .map {
                                it.map {
                                    val drugs = it.drugs.map {
                                        Drug(it.id, it.name, it.dose, it.color)
                                    }
                                    TimeSlotDrugs(HourMinute(it.timeSlot.hour, it.timeSlot.minute), drugs)
                                }
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getNextTimeSlotDrugs(hourMinute: HourMinute): Maybe<TimeSlotDrugs> {
        TODO()
//        return database.TimeSlotDao()
//                .getAll()
//                .map {
//                    it.sortedWith(TimeSlotModelComparator(hourMinute))
//                }
//                .flatMapIterable { it }
//                .firstElement()
//                .map {
//                    TimeSlotDrugs(HourMinute(it.timeSlot.hour, it.timeSlot.minute), it.drugs.map { Drug(it.id, it.name, it.dose, it.color) })
//                }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())

    }

    private class TimeSlotModelComparator(private val currentTime: HourMinute) : Comparator<TimeSlotModel> {
        override fun compare(a: TimeSlotModel, b: TimeSlotModel): Int {
            val offsetA = currentTime.deltaTo(a.timeSlot.hour, a.timeSlot.minute)
            val offsetB = currentTime.deltaTo(b.timeSlot.hour, b.timeSlot.minute)
            return when {
                offsetA.isPositive() && !offsetB.isPositive() -> -1
                offsetB.isPositive() && !offsetA.isPositive() -> 1
                isLessThan(b, a) -> 1
                isLessThan(a, b) -> -1
                else -> 0
            }
        }

        private fun isLessThan(a: TimeSlotModel, b: TimeSlotModel): Boolean {
            return when {
                a.timeSlot.hour < b.timeSlot.hour -> true
                a.timeSlot.hour == b.timeSlot.hour && a.timeSlot.minute < b.timeSlot.minute -> true
                else -> false
            }
        }
    }
}