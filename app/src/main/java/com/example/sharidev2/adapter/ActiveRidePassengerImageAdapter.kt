package com.example.sharidev2.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.material.card.MaterialCardView

class ActiveRidePassengerImageAdapter(
    private val context: Context,
    private var passengerImgList: List<Passenger>,
    private val clickListener: OnPassengerImageClickListener
): RecyclerView.Adapter<ActiveRidePassengerImageAdapter.ViewHolder>() {
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var passengerImgConstraintLayout: ConstraintLayout
        var passengerImgCard: MaterialCardView
        var passengerImg: ImageView
        var passengerName: TextView
        var passengerStatusBadgeCard: MaterialCardView
        var passengerStatusIcon: ImageButton

        init {
            passengerImgConstraintLayout = itemView.findViewById(R.id.cl_active_ride_item_passenger_image)
            passengerImgCard = itemView.findViewById(R.id.card_item_active_ride_passenger_image)
            passengerImg = itemView.findViewById(R.id.img_item_active_ride_passenger_image)
            passengerName = itemView.findViewById(R.id.text_item_active_ride_passenger_name)
            passengerStatusBadgeCard = itemView.findViewById(R.id.card_active_ride_passenger_status_badge)
            passengerStatusIcon = itemView.findViewById(R.id.img_active_ride_passenger_status_badge)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_active_ride_passenger_image, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val passenger = passengerImgList[position]

        // Image
        if(passenger.user?.photoUrl != null) {
            holder.passengerImg.setImageURI(passenger.user!!.photoUrl)
        } else {
            holder.passengerImg.setImageResource(R.drawable.baseline_account_circle_24)
        }

        // Name
        if(passenger.user?.displayName != null) {
            holder.passengerName.text = passenger.user?.displayName
        }

        // Status Badge
        if(passenger.status != null) {
            val typedValue = TypedValue()
            // Resolve the attribute to get the color value programmatically
            context.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            val colorPrimary = typedValue.data
            context.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true)
            val colorPrimaryContainer = typedValue.data
            context.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHighest, typedValue, true)
            val colorSurfaceContainerHighest = typedValue.data
            context.theme?.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            val colorOnSurface = typedValue.data

            if(passenger.userUid == currentUser?.uid) {
                holder.passengerImgCard.strokeWidth = (1 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
                holder.passengerImgCard.strokeColor = colorPrimary
            } else {
                holder.passengerImgCard.strokeWidth = 0
            }

            when(passenger.status) {
                UserStatus.WAITING -> {
                    holder.passengerStatusBadgeCard.setCardBackgroundColor(colorSurfaceContainerHighest)
                    holder.passengerStatusIcon.setImageResource(R.drawable.baseline_hourglass_empty_24)

                }

                UserStatus.IN_VEHICLE -> {
                    holder.passengerStatusBadgeCard.setCardBackgroundColor(colorPrimaryContainer)
                   holder.passengerStatusIcon.setImageResource(R.drawable.baseline_check_24)
                }

                else -> {
                    holder.passengerStatusBadgeCard.setCardBackgroundColor(colorSurfaceContainerHighest)
                    holder.passengerStatusIcon.setImageResource(R.drawable.baseline_question_mark_24)

                    val colorFilter = PorterDuffColorFilter(colorOnSurface, PorterDuff.Mode.SRC_IN)
                    holder.passengerStatusIcon.colorFilter = colorFilter
                }
            }
            holder.passengerStatusBadgeCard
        }


        holder.passengerImgConstraintLayout.setOnClickListener {
            clickListener.OnPassengerImageClick(passenger)
        }
    }

    override fun getItemCount(): Int {
        return passengerImgList.size
    }

    interface OnPassengerImageClickListener {
        fun OnPassengerImageClick(passenger: Passenger)
    }
}
