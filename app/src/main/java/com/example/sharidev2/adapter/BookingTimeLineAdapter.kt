package com.example.sharidev2.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.utility.CommonUtils
import com.github.vipulasri.timelineview.TimelineView
import com.github.vipulasri.timelineview.TimelineView.LineStyle


class BookingTimeLineAdapter(
    private val bookingNameList: List<String>
) : RecyclerView.Adapter<BookingTimeLineAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater

    class TimeLineViewHolder(itemView: View, viewType: Int): RecyclerView.ViewHolder(itemView) {
        val bookingTimeLine: TimelineView = itemView.findViewById(R.id.timeline_ride_item_booking_location)
        val locationName: TextView = itemView.findViewById(R.id.text_timeline_booking_location_name)

        init {
            bookingTimeLine.initLine(viewType)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun getItemCount(): Int {
        return bookingNameList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(mLayoutInflater.inflate(R.layout.timeline_item_booking_location, parent, false), viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        val timeLineRideName = bookingNameList[position]

        holder.locationName.text = timeLineRideName
        holder.bookingTimeLine.lineStyle = LineStyle.DASHED

        if(position == 0) {
            val colorPrimary = CommonUtils().getThemeColor(holder.itemView.context, com.google.android.material.R.attr.colorPrimary)

            holder.bookingTimeLine.marker = holder.itemView.context.getDrawable(R.drawable.ic_hollow_circle_thick)
            holder.bookingTimeLine.marker.colorFilter = PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.MULTIPLY)
        } else if (position == bookingNameList.lastIndex) {
            val colorError = CommonUtils().getThemeColor(holder.itemView.context, com.google.android.material.R.attr.colorError)

            holder.bookingTimeLine.marker = holder.itemView.context.getDrawable(R.drawable.ic_hollow_circle_thick_with_dot)
            holder.bookingTimeLine.marker.colorFilter = PorterDuffColorFilter(colorError, PorterDuff.Mode.MULTIPLY)
        } else {
            holder.bookingTimeLine.marker = holder.itemView.context.getDrawable(R.drawable.ic_hollow_circle)
            holder.bookingTimeLine.marker.clearColorFilter()
        }
    }
}