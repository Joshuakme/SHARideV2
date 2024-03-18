package com.example.sharidev2.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.GlideApp
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.utility.CommonUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideAdapter (
    private val context: Context,
    private var rideList: List<Ride>,
    private val clickListener: OnRideClickListener
) : RecyclerView.Adapter<RideAdapter.ViewHolder>() {

    interface OnRideClickListener {
        fun onRideClick(ride: Ride)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImg: ImageView
        var locationRecyclerView: RecyclerView
        var vehicleCapacityText: TextView
        var dateText: TextView
        var availableSeatsText: TextView

        init {
            profileImg = itemView.findViewById(R.id.img_ride_driver_icon)
            locationRecyclerView = itemView.findViewById(R.id.recycler_ride_item_location_timeline)
            vehicleCapacityText = itemView.findViewById(R.id.text_ride_item_seats_chip)
            dateText = itemView.findViewById(R.id.text_passenger_request_ride_date)
            availableSeatsText = itemView.findViewById(R.id.text_ride_available_seats)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_ride_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ride: Ride = rideList[position]

        // Date Time Format
        val bookingDateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

        // Bind data into UI
        if(ride.driver.user?.photoUri != null) {
            val photoUri = ride.driver.user?.photoUri

            GlideApp.with(context)
                .load(photoUri.toString())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                .into(holder.profileImg)
        } else {
            val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)
            holder.profileImg.setColorFilter(colorOutline)
        }

        holder.locationRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val rideLocationList = listOf(ride.origin.name, ride.destination.name)

        holder.locationRecyclerView.adapter = BookingTimeLineAdapter(rideLocationList)

        if(ride.driver.vehicle?.capacity != null) {
            holder.vehicleCapacityText.text =  holder.itemView.context.getString(
                R.string.matched_ride_fragment_vehicle_capacity,
                ride.driver.vehicle.capacity - 1    // exclude driver
            )
        } else {
            holder.vehicleCapacityText.visibility = View.GONE
        }

        holder.dateText.text = bookingDateFormatter.format(Date(ride.datetime.seconds * 1000)).replace("AM", "am").replace("PM", "pm")

        holder.availableSeatsText.text = holder.itemView.context.getString(
                R.string.matched_ride_fragment_ride_available_seats,
            ride.availableSeats
        )


        val typedValue = TypedValue()
        context.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        context.theme?.resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true)
        val colorError = typedValue.data




        holder.itemView.setOnClickListener {
            clickListener.onRideClick(ride)
        }
    }

    override fun getItemCount(): Int {
        return rideList.size
    }

    // Method to update data
    fun updateList(newRideList: List<Ride>) {
        rideList = newRideList

        notifyDataSetChanged()
    }

}