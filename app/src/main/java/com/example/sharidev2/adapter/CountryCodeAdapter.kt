package com.example.sharidev2.adapter

import com.example.sharidev2.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.model.Country




class CountryCodeAdapter (
    private var countryList: List<Country>,
    private val clickListener: OnCountryCodeClickListener
) : RecyclerView.Adapter<CountryCodeAdapter.ViewHolder>() {

    interface OnCountryCodeClickListener {
        fun onCountryCodeClick(country: Country)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var countryName: TextView
        var countryCode: TextView

        init {
            countryName = itemView.findViewById(R.id.text_bottom_dialog_country_name)
            countryCode = itemView.findViewById(R.id.text_bottom_dialog_country_code)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_bottom_dialog, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val countryCode: Country = countryList[position]

        // Bind data into UI
        holder.countryName.text = countryCode.name
        holder.countryCode.text = "+" + countryCode.countryCode

        holder.itemView.setOnClickListener {
            clickListener.onCountryCodeClick(countryCode)
        }
    }

    override fun getItemCount(): Int {
        return countryList!!.size
    }

    // Method to update data
    fun updateData(newData: List<Country>) {
        countryList = newData
        notifyDataSetChanged()
    }
}
