package com.example.sharide.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharide.R
import com.example.sharide.data.model.PopularLocation
import kotlin.math.roundToInt


class PopularLocationAdapter(
    private val context: Context,
    private var locationList: List<PopularLocation>,
    private val clickListener: OnLocationClickListener
): RecyclerView.Adapter<PopularLocationAdapter.ViewHolder>() {

    interface OnLocationClickListener {
        fun onLocationClick(location: PopularLocation)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnail: ImageView
        var title: TextView
        var distanceFromOrigin: TextView

        init {
            thumbnail = itemView.findViewById(R.id.img_recycler_item_popular_location)
            title = itemView.findViewById(R.id.text_recycler_item_popular_location_title)
            distanceFromOrigin = itemView.findViewById(R.id.text_recycler_item_popular_location_distance)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_home_popular_location, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       return locationList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locationList[position]

        holder.title.text = "Ride to " + location.title
        holder.distanceFromOrigin.text = "${location.distanceFromOrigin.roundToInt()} km away"

        if(location.thumbnail != null) {
            Glide.with(context)
                .load(location.thumbnail.toString())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                .into(holder.thumbnail)
        } else {

        }
    }
}