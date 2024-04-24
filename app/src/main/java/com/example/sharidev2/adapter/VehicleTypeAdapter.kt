package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.VehicleType

class VehicleTypeAdapter(
    private var vehicleTypeList: List<String>,
    private val clickListener: VehicleTypeAdapter.OnVehicleTypeClickListener
): RecyclerView.Adapter<VehicleTypeAdapter.ViewHolder>() {

    interface OnVehicleTypeClickListener {
        fun onVehicleTypeClick(gender: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var vehicleTypeName: TextView
        var vehicleTypeIcon: ImageView

        init {
            vehicleTypeName = itemView.findViewById(R.id.text_bottom_dialog_vehicle_type_item_name)
            vehicleTypeIcon = itemView.findViewById(R.id.img_bottom_dialog_vehicle_type_icon)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_bottom_dialog_vehicle_type, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicleType: String = vehicleTypeList[position]

        // Bind data into UI
        holder.vehicleTypeName.text = vehicleType

        when(VehicleType.valueOf(vehicleType)) {
            VehicleType.Sedan -> {
                holder.vehicleTypeIcon.setImageResource(R.drawable.outline_sedan_24)
            }
            VehicleType.Hatchback -> {
                holder.vehicleTypeIcon.setImageResource(R.drawable.outline_hatchback_24)
            }
            VehicleType.Minivan -> {
                holder.vehicleTypeIcon.setImageResource(R.drawable.outline_minivan_24)
            }
            VehicleType.SUV -> {
                holder.vehicleTypeIcon.setImageResource(R.drawable.outline_suv_24)
            }

            else -> {

            }
        }

        holder.itemView.setOnClickListener {
            clickListener.onVehicleTypeClick(vehicleType)
        }
    }

    override fun getItemCount(): Int {
        return vehicleTypeList.size
    }

    // Method to update data
    fun updateData(newData: List<String>) {
        vehicleTypeList = newData
        notifyDataSetChanged()
    }
}