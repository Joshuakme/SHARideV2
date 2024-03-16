package com.example.sharidev2.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.toLowerCase
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.UserStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter (
    private val context: Context,
    private val currentUserUid: String,
    private var bookingList: List<Ride>,
    private val clickListener: OnBookingClickListener
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    interface OnBookingClickListener {
        fun onBookingClick(booking: Ride)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView
        var dateText: TextView
        var priceText: TextView
        var bookingTagText: TextView

        init {
            titleText = itemView.findViewById(R.id.text_booking_title)
            dateText = itemView.findViewById(R.id.text_booking_date)
            priceText = itemView.findViewById(R.id.text_booking_price)
            bookingTagText = itemView.findViewById(R.id.text_booking_tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_booking_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking: Ride = bookingList[position]

        // Date Time Format
        val bookingDateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

        // Bind data into UI
        holder.titleText.text = booking.destination.name
        holder.dateText.text = bookingDateFormatter.format(Date(booking.datetime.seconds * 1000))
                                    .replace("AM", "am")
                                    .replace("PM", "pm")


        // Price
        if(isDriver(booking)) {
            val driver = booking.driver
            when(driver.status) {
                UserStatus.COMPLETED -> {
                    val totalPrice = if(booking.passengers.isNotEmpty()) {
                        var totalPrice = 0.0
                        for(passenger in booking.passengers) {
                            totalPrice += passenger.ridePrice?: 0.0
                        }
                        totalPrice
                    } else {
                        0.0
                    }
                    holder.priceText.text = holder.itemView.context.getString(R.string.booking_item_price, totalPrice)
                }

                UserStatus.IN_VEHICLE -> {
                    holder.priceText.text = "ongoing"
                }

                else -> holder.priceText.text = driver.status.toString().lowercase()
            }
        } else if(isPassenger(booking)) {
            for(passenger in booking.passengers) {
                if(passenger.userUid == currentUserUid) {
                    when(passenger.status) {
                        UserStatus.COMPLETED -> {
                            holder.priceText.text = holder.itemView.context.getString(R.string.booking_item_price, passenger.ridePrice)
                        }

                        UserStatus.IN_VEHICLE -> {
                            holder.priceText.text = "ongoing"
                        }

                        else -> holder.priceText.text = passenger.status.toString().lowercase()
                    }
                }
            }
        }




        val typedValue = TypedValue()
        context.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        context.theme?.resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true)
        val colorError = typedValue.data


        if(isDriver(booking)) {
            holder.bookingTagText.text = "Driver"
            holder.priceText.setTextColor(colorPrimary)
        } else if(isPassenger(booking)) {
            holder.bookingTagText.text = "Passenger"
            holder.priceText.setTextColor(colorError)
        } else {
            holder.bookingTagText.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            clickListener.onBookingClick(booking)
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    // Method to update data
    fun updateList(newBookingList: List<Ride>) {
        bookingList = newBookingList

        notifyDataSetChanged()
    }

    private fun isDriver(ride: Ride): Boolean {
        return ride.driver.userUid == currentUserUid
    }

    private fun isPassenger(ride: Ride): Boolean {
        return ride.passengers.any { passenger ->
            passenger.userUid == currentUserUid }
    }

}