package com.jeremy.keepingtrack.features.homescreen

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeremy.keepingtrack.FormatUtils
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.DrugCourse
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.HourMinuteComparator
import com.jeremy.keepingtrack.data.HourMinuteOffsetComparator
import kotlinx.android.synthetic.main.row_drug_course.view.*
import kotlinx.android.synthetic.main.row_planned_dose.view.*


class DrugCourseAdapter : RecyclerView.Adapter<CourseViewHolder>() {

    private val dataset: ArrayList<List<DrugCourseEntry>> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_planned_dose, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.setData(dataset[position])
    }

    fun updateData(hourMinute: HourMinute, newDataset: List<DrugCourse>) {
        // TODO: handle days of week
        val newEntries = newDataset
                .flatMap { drugCourse ->
                    drugCourse.times.map { DrugCourseEntry(drugCourse, it) }
                }
                .sortedWith(Comparator { a, b ->
                    HourMinuteComparator.compare(a.time, b.time)
                })
                .groupBy {
                    it.time
                }
                .toSortedMap(HourMinuteOffsetComparator(hourMinute))
                .map { it.value }

        val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, newEntries), true)
        dataset.clear()
        dataset.addAll(newEntries)
        changes.dispatchUpdatesTo(this)
    }

    fun updateCurrentTime(hourMinute: HourMinute) {
        val sortedData = ArrayList(dataset)
                .groupBy {
                    it.first().time
                }
                .toSortedMap(HourMinuteOffsetComparator(hourMinute))
                .flatMap { it.value }
        val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, sortedData), true)
        dataset.clear()
        dataset.addAll(sortedData)
        changes.dispatchUpdatesTo(this)
    }
}

private class DrugCourseDiffer(val oldList: List<List<DrugCourseEntry>>, val newList: List<List<DrugCourseEntry>>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].count() == newList[newItemPosition].count()
                && oldList[oldItemPosition].first() == newList[newItemPosition].first()
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
        if (oldEntry.count() != newEntry.count()) {
            return false
        }
        for (i in 0 until oldEntry.count()) {
            if (oldEntry[i] != newEntry[i]) {
                return false
            }
        }
        return true
    }
}

class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun setData(courses: List<DrugCourseEntry>) {
        itemView.readout_time.text = FormatUtils.formatHourMinute(courses.first().time)
        val container = itemView.readout_drugCourses.apply { removeAllViews() }
        val inflater = LayoutInflater.from(itemView.context)
        for (entry in courses) {
            val view = inflater.inflate(R.layout.row_drug_course, container, false)
            val course = entry.course
            view.readout_name.text = course.name
            view.readout_dose.text = FormatUtils.formatDose(course.dose)
            view.readout_icon.setBackgroundColor(course.color)
            container.addView(view)
        }
    }
}

data class DrugCourseEntry(val course: DrugCourse, val time: HourMinute)