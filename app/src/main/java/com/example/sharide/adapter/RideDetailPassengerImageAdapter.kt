package com.example.sharide.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharide.R
import com.example.sharide.utility.CommonUtils

class RideDetailPassengerImageAdapter(
    private val context: Context,
    private var passengerImgList: List<Uri>,
): RecyclerView.Adapter<RideDetailPassengerImageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var passengerImg: ImageView

        init {
            passengerImg = itemView.findViewById(R.id.img_item_ride_detail_passenger_image)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_ride_detail_passenger_image, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val passengerImg = passengerImgList[position]

        if(CommonUtils().isUrl(passengerImg.toString())) {
            Glide.with(context)
                .load(passengerImg.toString())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                .into(holder.passengerImg)
        } else {
            holder.passengerImg.setImageURI(passengerImg)

            val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)
            holder.passengerImg.setColorFilter(colorOutline)
        }

    }

    override fun getItemCount(): Int {
        return passengerImgList.size
    }
}