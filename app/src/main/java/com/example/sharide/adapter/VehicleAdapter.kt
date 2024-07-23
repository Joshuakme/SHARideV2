package com.example.sharide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
import com.example.sharide.data.model.Vehicle


class VehicleAdapter (
    private val vehicleList: List<Vehicle>,
    private val clickListener: OnVehicleClickListener
) : RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    interface OnVehicleClickListener {
        fun onVehicleClick(vehicle: Vehicle)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var vehiclePlateNumberText: TextView
        var vehicleColorText: TextView
        var vehicleBrandModelText: TextView
        var vehicleThumbnailImage: ImageView

        init {
            vehiclePlateNumberText = itemView.findViewById(R.id.text_bottom_dialog_vehicle_plate_number)
            vehicleColorText = itemView.findViewById(R.id.text_bottom_dialog_vehicle_color)
            vehicleBrandModelText = itemView.findViewById(R.id.text_bottom_dialog_vehicle_brand_model)
            vehicleThumbnailImage = itemView.findViewById(R.id.img_bottom_dialog_vehicle_thumbnail)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_bottom_dialog_vehicle, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicle: Vehicle = vehicleList[position]

        // Bind data into UI
        holder.vehiclePlateNumberText.text = vehicle.plateNumber
        holder.vehicleColorText.text = vehicle.color
        holder.vehicleBrandModelText.text = "${vehicle.brand} ${vehicle.model}"
        holder.vehicleThumbnailImage.setImageBitmap(null)   //vehicle.photos?.get(0)


        holder.itemView.setOnClickListener {
            clickListener.onVehicleClick(vehicle)
        }
    }

    override fun getItemCount(): Int {
        return vehicleList.size
    }

    // Method to update data
    fun updateData() {
        notifyDataSetChanged()
    }
}