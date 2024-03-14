package com.example.sharidev2.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.GlideApp
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Converters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class MessageAdapter(
    private val context: Context,
    private val messageList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastDisplayedDateLabel: String? = null
    private var lastDisplayedUsernameLabel: String? = null


    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Inflate the message item layout
        if (viewType == 1) {
            // Inflate Received
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_message_receive, parent, false)

            return ReceivedViewHolder(itemView)
        } else {
            // Inflate Sent
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_message_sent, parent, false)

            return SentViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Bind data to views in the message item
        val message = messageList[position]
        val currentDateLabel = getMessageDateLabel(message.timestamp!!)

        val previousSender: String? = if (position > 0) messageList[position - 1].senderId else null
        val currentSender: String = message.senderId!!
        val isSameSender = previousSender == currentSender


        if(holder is SentViewHolder) {
            // render sent message
            val viewHolder = holder

            if (lastDisplayedDateLabel != currentDateLabel) {
                viewHolder.messageDateTitle.text = formatMessageDate(message.timestamp)
                viewHolder.messageDateTitle.visibility = View.VISIBLE

                lastDisplayedDateLabel = currentDateLabel
            } else {
                viewHolder.messageDateTitle.visibility = View.GONE
            }


            if(isSameSender) {
                val layoutParams = viewHolder.messageItemLL.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    Converters().toPixel(context, 4f),
                    layoutParams.rightMargin,
                    layoutParams.bottomMargin
                )
                viewHolder.messageItemLL.layoutParams = layoutParams
            } else {
                val layoutParams = viewHolder.messageItemLL.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    Converters().toPixel(context, 16f),
                    layoutParams.rightMargin,
                    layoutParams.bottomMargin
                )
                viewHolder.messageItemLL.layoutParams = layoutParams
            }

            viewHolder.receivedMessage.text = message.text
            viewHolder.messageTime.text = CommonUtils.formatTime(message.timestamp, "HH: mm")
        } else {
            // render received message
            val viewHolder = holder as ReceivedViewHolder

            // Check Date Label Visibility
            if (lastDisplayedDateLabel != currentDateLabel) {
                viewHolder.messageDateTitle.text = formatMessageDate(message.timestamp)
                viewHolder.messageDateTitle.visibility = View.VISIBLE

                lastDisplayedDateLabel = currentDateLabel
            } else {
                viewHolder.messageDateTitle.visibility = View.GONE
            }

            // Check if same sender and adjust UI
            if(isSameSender) {
                // adjust margin between message item
                val layoutParams = viewHolder.messageItemLL.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    Converters().toPixel(context, 4f),
                    layoutParams.rightMargin,
                    layoutParams.bottomMargin
                )
                viewHolder.messageItemLL.layoutParams = layoutParams


                // Check user profile image visibility
                viewHolder.messageUserImg.visibility = View.INVISIBLE
                // Check message username visibility
                viewHolder.messageItemUsername.visibility = View.GONE
            } else {
                // Adjust margin between message item
                val layoutParams = viewHolder.messageItemLL.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    Converters().toPixel(context, 16f),
                    layoutParams.rightMargin,
                    layoutParams.bottomMargin
                )
                viewHolder.messageItemLL.layoutParams = layoutParams


                // Check user profile image visibility
                if(message.photoUrl != null) {
                    GlideApp.with(context)
                        .load(message.photoUrl)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC)) // Disable disk caching
                        .into(viewHolder.messageUserImg)

                    viewHolder.messageUserImg.clearColorFilter()
                } else {
                    val defaultUserImg = context.getDrawable(R.drawable.baseline_account_circle_24)
                    defaultUserImg!!.colorFilter = PorterDuffColorFilter(
                        CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOnBackground),
                        PorterDuff.Mode.SRC_IN
                    )
                    viewHolder.messageUserImg.setImageDrawable(defaultUserImg)
                }
                viewHolder.messageUserImg.visibility = View.VISIBLE
                // Check message username visibility
                viewHolder.messageItemUsername.text = message.senderName
                viewHolder.messageItemUsername.visibility = View.VISIBLE
            }



            viewHolder.receivedMessage.text = message.text
            viewHolder.messageTime.text = CommonUtils.formatTime(message.timestamp, "HH: mm")
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if(currentUser.equals(message.senderId)) {
            return ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemLL = itemView.findViewById<LinearLayout>(R.id.ll_message_item_sent)
        val messageDateTitle = itemView.findViewById<TextView>(R.id.text_message_sent_timestamp_title)
        val receivedMessage = itemView.findViewById<TextView>(R.id.text_message_sent)
        val messageTime = itemView.findViewById<TextView>(R.id.text_message_sent_time)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemUsername: TextView = itemView.findViewById(R.id.text_message_item_receive_username)
        val messageItemLL: LinearLayout = itemView.findViewById(R.id.ll_message_item_receive)
        val messageUserImg: ImageView = itemView.findViewById(R.id.img_message_receive_user)
        val messageDateTitle: TextView = itemView.findViewById(R.id.text_message_receive_timestamp_title)
        val receivedMessage: TextView = itemView.findViewById(R.id.text_message_receive)
        val messageTime: TextView = itemView.findViewById(R.id.text_message_receive_time)
    }


    private fun exceedHalfHour(lastMessageTime: Timestamp, datetime: Timestamp): Boolean {
        val timeDifferenceMillis = datetime.toDate().time - lastMessageTime.toDate().time
        val halfHourMillis = TimeUnit.MINUTES.toMillis(30) // 30 minutes in milliseconds

        return timeDifferenceMillis >= halfHourMillis
    }

    private fun formatMessageDate(date: Timestamp): String {
        return if(CommonUtils().isToday(date)) {
            "Today"
        } else if(CommonUtils().isYesterday(date)) {
            "Yesterday"
        }
        else {
            CommonUtils.formatDate(date, "yyyy/MM/dd")
        }
    }

    fun getMessageDateLabel(timestamp: Timestamp): String {

        return when {
            CommonUtils().isToday(timestamp) ->
                "Today"

            CommonUtils().isYesterday(timestamp) ->
                "Yesterday"
            else -> SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(timestamp.toDate())
        }
    }
}