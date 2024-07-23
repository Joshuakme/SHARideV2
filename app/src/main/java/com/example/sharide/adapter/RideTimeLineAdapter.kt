package com.example.sharide.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
import com.github.vipulasri.timelineview.TimelineView




class RideTimeLineAdapter(
    private val rideNameList: List<String>
) : RecyclerView.Adapter<RideTimeLineAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater

    class TimeLineViewHolder(itemView: View, viewType: Int): RecyclerView.ViewHolder(itemView) {
        val rideTimeLine: TimelineView = itemView.findViewById(R.id.timeline_ride_item_location)
        val locationName: TextView = itemView.findViewById(R.id.text_timeline_location_name)

        init {
            rideTimeLine.initLine(viewType)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun getItemCount(): Int {
        return rideNameList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(mLayoutInflater.inflate(R.layout.timeline_item_ride_location, parent, false), viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        val timeLineRideName = rideNameList[position]

        holder.locationName.text = timeLineRideName

        if(rideNameList.size == 2) {

            holder.rideTimeLine.marker = if(position == 0) {
                holder.itemView.context.getDrawable(R.drawable.ic_hollow_circle)
            } else {
                holder.itemView.context.getDrawable(R.drawable.ic_hollow_circle_with_dot)
            }

        }
    }
}