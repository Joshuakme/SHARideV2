package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R


class PassengerCapacityAdapter(
    private val capacityList: List<Int>,
    private val clickListener: OnCapacityClickListener
) : RecyclerView.Adapter<PassengerCapacityAdapter.ViewHolder>() {
    interface OnCapacityClickListener {
        fun onCapacityClick(capacity: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       var capacityText: TextView

        init {
            capacityText = itemView.findViewById(R.id.text_bottom_dialog_passenger_capacity_item_name)

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_bottom_dialog_passenger_capacity, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val capacity: Int = capacityList[position]

        // Bind data into UI
        holder.capacityText.text = "${capacity} pax"


        holder.itemView.setOnClickListener {
            clickListener.onCapacityClick(capacity)
        }
    }

    override fun getItemCount(): Int {
        return capacityList!!.size
    }
}