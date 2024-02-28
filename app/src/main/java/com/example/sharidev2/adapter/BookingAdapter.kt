package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.firebase.FirebaseInitializer
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter (
    private val currentUser: FirebaseUser,
    private val bookingList: List<Ride>,
    private val clickListener: OnBookingClickListener
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    interface OnBookingClickListener {
        fun onBookingClick(booking: Ride)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView
        var dateText: TextView
        var priceText: TextView

        init {
            titleText = itemView.findViewById(R.id.text_booking_title)
            dateText = itemView.findViewById(R.id.text_booking_date)
            priceText = itemView.findViewById(R.id.text_booking_price)
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
        val bookingDateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH)

        // Bind data into UI
        holder.titleText.text = booking.destination.name
        holder.dateText.text = bookingDateFormatter.format(Date(booking.datetime.seconds * 1000))
        holder.priceText.text = holder.itemView.context.getString(R.string.booking_item_price,
            booking.price?.get(currentUser.uid ?: ""))



        holder.itemView.setOnClickListener {
            clickListener.onBookingClick(booking)
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    // Method to update data
    fun updateData() {
        notifyDataSetChanged()
    }
}