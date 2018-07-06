package com.jeremy.keepingtrack.features.homescreen

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.Drug
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.HourMinuteOffsetComparator
import com.jeremy.keepingtrack.data.TimeSlotDrugs
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.row_drug_course.view.*
import kotlinx.android.synthetic.main.row_planned_dose.view.*


class DrugCourseAdapter : RecyclerView.Adapter<CourseViewHolder>() {

    private val dataset: ArrayList<TimeSlotDrugs> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_planned_dose, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val item = dataset[position]
        holder.setData(item.time, item.drugs)
    }

    fun updateData(hourMinute: HourMinute, newDataset: List<Drug>) {
        val newEntries = newDataset
                .flatMap { drug ->
                    drug.times.map { Pair(it, drug) }
                }
                .groupBy { it.first }
                .toSortedMap(HourMinuteOffsetComparator(hourMinute))
                .map { TimeSlotDrugs(it.key, it.value.map { it.second }) }
        val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, newEntries), true)
        dataset.clear()
        dataset.addAll(newEntries)
        changes.dispatchUpdatesTo(this)
    }

    fun updateTime(timeSignal: Flowable<HourMinute>): Disposable {
        return timeSignal.subscribe { hourMinute ->
            val sortedEntries = ArrayList(dataset).apply { this.sortWith(TimeSlotModelComparator(hourMinute)) }
            val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, sortedEntries), true)
            dataset.clear()
            dataset.addAll(sortedEntries)
            changes.dispatchUpdatesTo(this)
        }
    }
}

private class TimeSlotModelComparator(private val currentTime: HourMinute) : Comparator<TimeSlotDrugs> {
    override fun compare(a: TimeSlotDrugs, b: TimeSlotDrugs): Int {
        val offsetA = currentTime.deltaTo(a.time.hour, a.time.minute)
        val offsetB = currentTime.deltaTo(b.time.hour, b.time.minute)
        return when {
            offsetA.isPositive() && !offsetB.isPositive() -> -1
            offsetB.isPositive() && !offsetA.isPositive() -> 1
            isLessThan(b, a) -> 1
            isLessThan(a, b) -> -1
            else -> 0
        }
    }

    private fun isLessThan(a: TimeSlotDrugs, b: TimeSlotDrugs): Boolean {
        return when {
            a.time.hour < b.time.hour -> true
            a.time.hour == b.time.hour && a.time.minute < b.time.minute -> true
            else -> false
        }
    }
}

private class DrugCourseDiffer(val oldList: List<TimeSlotDrugs>, val newList: List<TimeSlotDrugs>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEntry = oldList[oldItemPosition]
        val newEntry = newList[newItemPosition]
        return oldEntry == newEntry
    }
}

class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setData(time: HourMinute, drugs: List<Drug>) {
        itemView.readout_time.text = time.toString()
        val container = itemView.readout_drugCourses.apply { removeAllViews() }
        val inflater = LayoutInflater.from(itemView.context)
        for (drug in drugs) {
            val view = inflater.inflate(R.layout.row_drug_course, container, false)
            view.readout_name.text = drug.name
            view.readout_icon.setBackgroundColor(drug.color)
            container.addView(view)
        }
    }
}