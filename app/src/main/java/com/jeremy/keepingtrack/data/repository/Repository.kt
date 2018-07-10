package com.jeremy.keepingtrack.data.repository

import android.arch.persistence.room.Room
import android.content.Context
import com.jeremy.keepingtrack.TimeUtils
import com.jeremy.keepingtrack.data.Drug
import com.jeremy.keepingtrack.data.HourMinuteOffsetComparator
import com.jeremy.keepingtrack.data.TimeSlotDrugs
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface Repository {
    fun saveDrug(drug: Drug): Single<Long>
    fun updateDrug(drug: Drug)
    fun getAllDrugCourses(): Flowable<List<Drug>>
    fun getTimeSlotDrugs(): Flowable<List<TimeSlotDrugs>>
    fun fetchDrug(id: Long): Maybe<Drug>
}

class RoomRepository(context: Context) : Repository {

    private val database: AppDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app-database").build()

    override fun saveDrug(drug: Drug): Single<Long> {
        return Single.just(drug)
                .map {
                    database.drugDao().insert(DrugDb(name = it.name, color = it.color, dosesPerDay = it.dosesPerDay, first = it.first, interval = it.interval, isActive = true))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateDrug(drug: Drug) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllDrugCourses(): Flowable<List<Drug>> {
        return database.drugDao()
                .getAll()
                .map {
                    it
                            .filter {
                                it.isActive
                            }
                            .map {
                                Drug(it.id, it.name, it.color, it.dosesPerDay, it.first, it.interval)
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getTimeSlotDrugs(): Flowable<List<TimeSlotDrugs>> {
        return getAllDrugCourses()
                .map {
                    val hourMinute = TimeUtils.nowToHourMinute()
                    it
                            .flatMap { drug ->
                                drug.times.map { Pair(it, drug) }
                            }
                            .groupBy { it.first }
                            .toSortedMap(HourMinuteOffsetComparator(hourMinute))
                            .map { TimeSlotDrugs(it.key, it.value.map { it.second }) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun fetchDrug(id: Long): Maybe<Drug> {
        return database.drugDao().get(id)
                .map { Drug(it.id, it.name, it.color, it.dosesPerDay, it.first, it.interval) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}