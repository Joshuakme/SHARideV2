package com.example.sharide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
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
            holder.bookingTimeLine.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_origin_circle_marker)
        } else if (position == bookingNameList.lastIndex) {
            holder.bookingTimeLine.marker = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.ic_dest_circle_marker)
        } else {
            holder.bookingTimeLine.marker = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.ic_hollow_circle)
            holder.bookingTimeLine.marker.clearColorFilter()
        }
    }
}