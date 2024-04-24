package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.screen.user.VehicleDocFragment

class VehicleDocAdapter(
    private val vehicleDocList: List<VehicleDoc>,
    private val vListener: VehicleDocFragment
): RecyclerView.Adapter<VehicleDocAdapter.VehicleDocHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }


    class VehicleDocHolder(vehicleDocView: View, listener: VehicleDocFragment): RecyclerView.ViewHolder(vehicleDocView){
        val carPlate: TextView = vehicleDocView.findViewById(R.id.tv_display_car_plate)
        val vehicleModel: TextView = vehicleDocView.findViewById(R.id.tv_display_vehicle_model)

        init{
            vehicleDocView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleDocAdapter.VehicleDocHolder {
        val vehicleDocView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_vehicle_doc,parent,false)
        return VehicleDocAdapter.VehicleDocHolder(vehicleDocView, vListener)
    }

    override fun getItemCount(): Int {
        return vehicleDocList.size
    }

    override fun onBindViewHolder(holder: VehicleDocAdapter.VehicleDocHolder, position: Int) {
        val currentVehicleDoc = vehicleDocList[position]

    }
}