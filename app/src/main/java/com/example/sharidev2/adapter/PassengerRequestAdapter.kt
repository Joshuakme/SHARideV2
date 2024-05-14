package com.example.sharidev2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.utility.CommonUtils
import com.google.android.material.card.MaterialCardView

class PassengerRequestAdapter(
    private val context: Context,
    private var passengerList: MutableList<Passenger>,
    private val clickListener: OnRequestClickListener
): RecyclerView.Adapter<PassengerRequestAdapter.ViewHolder>() {

    interface OnRequestClickListener {
        fun onRequestAcceptClick(passenger: Passenger, onSuccess: (Boolean) -> Unit)
        fun onRequestRejectClick(passenger: Passenger, onSuccess: (Boolean) -> Unit)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var passengerImg: ImageView
        var locationRecyclerView: RecyclerView
        var rideDate: TextView
        var rejectBtn: MaterialCardView
        var acceptBtn: MaterialCardView
        var loadingProgressBar: MaterialCardView

        init {
            passengerImg = itemView.findViewById(R.id.img_passenger_request_passenger_icon)
            locationRecyclerView = itemView.findViewById(R.id.recycler_passenger_request_location_timeline)
            rideDate = itemView.findViewById(R.id.text_passenger_request_ride_date)
            rejectBtn = itemView.findViewById(R.id.card_passenger_request_reject)
            acceptBtn = itemView.findViewById(R.id.card_passenger_request_accept)
            loadingProgressBar = itemView.findViewById(R.id.card_passenger_request_loading)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_passenger_request, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return passengerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val passenger = passengerList[position]

        // Passenger Image
        if(passenger.user?.photoUrl != null) {
            Glide.with(context)
                .load(passenger.user!!.photoUrl.toString())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                .into(holder.passengerImg)
        }

        // Location (Origin & Destination)
        holder.locationRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val rideLocationList = listOf(passenger.origin!!.name, passenger.destination!!.name)

        holder.locationRecyclerView.adapter = RideTimeLineAdapter(rideLocationList)

        if(passenger.requestedDateTime != null) {
            holder.rideDate.text = CommonUtils.formatDateTime(passenger.requestedDateTime)
        }

        holder.rejectBtn.setOnClickListener {
            onClickLoading(true, holder)

            clickListener.onRequestRejectClick(passenger) {isSuccess ->
                onClickLoading(false, holder)

                if(isSuccess) {
                    passengerList.removeAt(position)

                    notifyItemRemoved(position)
                }
            }
        }

        holder.acceptBtn.setOnClickListener {
            onClickLoading(true, holder)

            clickListener.onRequestAcceptClick(passenger) {isSuccess ->
                onClickLoading(false, holder)

                if(isSuccess) {
                    passengerList.removeAt(position)

                    notifyItemRemoved(position)
                }
            }
        }
    }

    private fun onClickLoading(loading: Boolean, holder: ViewHolder) {
        val acceptBtn = holder.acceptBtn
        val rejectBtn = holder.rejectBtn
        val loadingProgressBar = holder.loadingProgressBar

        if(loading) {
            acceptBtn.visibility = View.GONE
            rejectBtn.visibility = View.GONE
            loadingProgressBar.visibility = View.VISIBLE
        } else {
            acceptBtn.visibility = View.VISIBLE
            rejectBtn.visibility = View.VISIBLE
            loadingProgressBar.visibility = View.GONE
        }
    }
}