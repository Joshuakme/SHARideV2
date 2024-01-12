package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Country
import com.example.sharidev2.data.model.SearchLocation

class SearchRideAdapter(
    private var searchPlaceList: List<SearchLocation>,
    ) : RecyclerView.Adapter<SearchRideAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var placeName: TextView
        var placeDetailedAddress: TextView

        init {
            placeName = itemView.findViewById(R.id.text_search_place_result_title)
            placeDetailedAddress = itemView.findViewById(R.id.text_search_place_result_detailed_address)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_search_place_result, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return searchPlaceList!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchPlace: SearchLocation = searchPlaceList[position]

        // Bind data into UI
        holder.placeName.text = searchPlace.name
        holder.placeDetailedAddress.text = searchPlace.detailAddress
    }

}