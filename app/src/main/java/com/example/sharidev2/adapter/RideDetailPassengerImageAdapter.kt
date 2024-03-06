package com.example.sharidev2.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.SearchLocation

class RideDetailPassengerImageAdapter(
    private var passengerImgList: List<Uri>,
): RecyclerView.Adapter<RideDetailPassengerImageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var passengerImg: ImageView

        init {
            passengerImg = itemView.findViewById(R.id.img_item_ride_detail_passenger_image)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_ride_detail_passenger_image, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val passengerImg = passengerImgList[position]

        holder.passengerImg.setImageURI(passengerImg)
    }

    override fun getItemCount(): Int {
        return passengerImgList.size
    }
}