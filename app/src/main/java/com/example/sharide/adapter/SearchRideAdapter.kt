package com.example.sharide.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
import com.example.sharide.data.model.SearchLocation

class SearchRideAdapter(
    private val context: Context,
    private var searchPlaceList: List<SearchLocation>,
    private val onLocationClickListener: (SearchLocation) -> Unit
    ) : RecyclerView.Adapter<SearchRideAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var topDivider: View
        var placeName: TextView
        var placeDetailedAddress: TextView

        init {
            topDivider = itemView.findViewById(R.id.view_top_item_divider)
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
        return searchPlaceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchPlace: SearchLocation = searchPlaceList[position]

        // Bind data into UI
        holder.topDivider.visibility = if(position == 0) View.VISIBLE else View.GONE

        holder.placeName.text = searchPlace.name

        holder.placeDetailedAddress.text = searchPlace.getDistanceAddressText(context)


        holder.itemView.setOnClickListener {
            onLocationClickListener.invoke(searchPlace)
        }
    }

}