package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R

class VehicleTypeAdapter(
    private var vehicleTypeList: List<String>,
    private val clickListener: VehicleTypeAdapter.OnVehicleTypeClickListener
): RecyclerView.Adapter<VehicleTypeAdapter.ViewHolder>() {

    interface OnVehicleTypeClickListener {
        fun onVehicleTypeClick(gender: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var vehicleTypeName: TextView

        init {
            vehicleTypeName = itemView.findViewById(R.id.text_bottom_dialog_vehicle_type_item_name)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_bottom_dialog_vehicle_type, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gender: String = vehicleTypeList[position]

        // Bind data into UI
        holder.vehicleTypeName.text = gender

        holder.itemView.setOnClickListener {
            clickListener.onVehicleTypeClick(gender)
        }
    }

    override fun getItemCount(): Int {
        return vehicleTypeList!!.size
    }

    // Method to update data
    fun updateData(newData: List<String>) {
        vehicleTypeList = newData
        notifyDataSetChanged()
    }
}