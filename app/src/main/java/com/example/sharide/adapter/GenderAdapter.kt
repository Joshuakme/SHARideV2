package com.example.sharide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R

class GenderAdapter (
    private var genderList: List<String>,
    private val clickListener: GenderAdapter.OnGenderClickListener
): RecyclerView.Adapter<GenderAdapter.ViewHolder>() {

    interface OnGenderClickListener {
        fun onGenderClick(gender: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var genderName: TextView

        init {
            genderName = itemView.findViewById(R.id.text_bottom_dialog_gender_item_name)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_bottom_dialog_gender, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gender: String = genderList[position]

        // Bind data into UI
        holder.genderName.text = gender

        holder.itemView.setOnClickListener {
            clickListener.onGenderClick(gender)
        }
    }

    override fun getItemCount(): Int {
        return genderList.size
    }

    // Method to update data
    fun updateData(newData: List<String>) {
        genderList = newData
        notifyDataSetChanged()
    }
}