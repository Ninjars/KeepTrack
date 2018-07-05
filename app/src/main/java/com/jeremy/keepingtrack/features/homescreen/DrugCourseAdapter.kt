package com.jeremy.keepingtrack.features.homescreen

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeremy.keepingtrack.FormatUtils
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.*
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

    fun updateData(hourMinute: HourMinute, newDataset: List<TimeSlotDrugs>) {
        val changes = DiffUtil.calculateDiff(DrugCourseDiffer(dataset, newDataset), true)
        dataset.clear()
        dataset.addAll(newDataset)
        changes.dispatchUpdatesTo(this)
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
        itemView.readout_time.text = FormatUtils.formatHourMinute(time)
        val container = itemView.readout_drugCourses.apply { removeAllViews() }
        val inflater = LayoutInflater.from(itemView.context)
        for (drug in drugs) {
            val view = inflater.inflate(R.layout.row_drug_course, container, false)
            view.readout_name.text = drug.name
            view.readout_dose.text = FormatUtils.formatDose(drug.dose)
            view.readout_icon.setBackgroundColor(drug.color)
            container.addView(view)
        }
    }
}