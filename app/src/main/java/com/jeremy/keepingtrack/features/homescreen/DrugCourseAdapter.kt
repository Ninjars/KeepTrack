package com.jeremy.keepingtrack.features.homescreen

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeremy.keepingtrack.FormatUtils
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.DrugCourse
import com.jeremy.keepingtrack.features.scheduledose.HourMinute
import com.jeremy.keepingtrack.features.scheduledose.HourMinuteComparator
import kotlinx.android.synthetic.main.row_drug_course.view.*


class DrugCourseAdapter : RecyclerView.Adapter<CourseViewHolder>() {

    private val dataset: ArrayList<DrugCourseEntry> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_drug_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.setData(dataset[position])
    }

    fun updateData(newDataset: List<DrugCourse>) {
        // TODO: handle days of week
        val newEntries = newDataset
                .flatMap { drugCourse ->
                    drugCourse.times.map { DrugCourseEntry(drugCourse.name, drugCourse.dose, drugCourse.color, it) }
                }
                .sortedWith(Comparator { a, b ->
                    HourMinuteComparator.compare(a.time, b.time)
                })

        val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, newEntries), true)
        dataset.clear()
        dataset.addAll(newEntries)
        changes.dispatchUpdatesTo(this)
    }
}

private class DrugCourseDiffer(val oldList: List<DrugCourseEntry>, val newList: List<DrugCourseEntry>) : DiffUtil.Callback() {
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
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

class CourseViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setData(course: DrugCourseEntry) {
        itemView.readout_name.text = course.name
        itemView.readout_dose.text = FormatUtils.formatDose(course.dose)
        itemView.readout_time.text = FormatUtils.formatHourMinute(course.time)
        itemView.readout_icon.setBackgroundColor(course.color)
    }
}

data class DrugCourseEntry(val name: String, val dose: Float, val color: Int, val time: HourMinute)