package com.jeremy.keepingtrack.features.scheduledose

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeremy.keepingtrack.FormatUtils
import com.jeremy.keepingtrack.R
import com.jeremy.keepingtrack.data.HourMinute
import com.jeremy.keepingtrack.data.HourMinuteComparator
import kotlinx.android.synthetic.main.row_schedule_time.view.*

class ScheduledTimingsAdapter : RecyclerView.Adapter<TimingsViewHolder>() {
    private val dataList = ArrayList<HourMinute>()

    fun addItem(hourMinute: HourMinute) {
        dataList.add(hourMinute)
        dataList.sortWith(HourMinuteComparator)
        val index = dataList.indexOf(hourMinute)
        notifyItemInserted(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_schedule_time, parent, false)
        return TimingsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: TimingsViewHolder, position: Int) {
        val hourMinute = dataList[position]
        holder.setData(FormatUtils.formatHourMinute(hourMinute), { removeItem(position) })
    }

    private fun removeItem(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getData(): List<HourMinute> {
        return dataList
    }
}

class TimingsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setData(hourMinute: CharSequence, removeCallback: () -> Unit) {
        itemView.button_remove.setOnClickListener { removeCallback() }
        itemView.readout_time.text = hourMinute
    }
}