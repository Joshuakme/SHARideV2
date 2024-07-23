package com.example.sharide.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharide.R
import com.example.sharide.data.model.Chat
import com.example.sharide.data.model.Message
import com.example.sharide.utility.CommonUtils
import com.example.sharide.viewmodel.ChatViewModel
import com.google.firebase.Timestamp

class ChatAdapter(
    private val oldChatList: List<Chat>,
    private val newChatList: List<Chat>,
    private val chatViewModel: ChatViewModel,
    private val clickListener: OnChatClickListener,
    private val updateListener: OnChatUpdateListener,
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


    interface OnChatClickListener {
        fun onChatClick(chat: Chat)
    }

    interface OnChatUpdateListener {
        fun onChatUpdate(message: Message)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Add views for a message item (e.g., TextViews, ImageViews)
        val chatPicCard: CardView
        var chatPic: ImageView
        var chatTitle: TextView
        var chatLastMessage: TextView
        var chatDate: TextView
        var chatNewMessageBadgeText: TextView

        init {
            chatPicCard = itemView.findViewById(R.id.chat_icon)
            chatPic = itemView.findViewById(R.id.image_button_chat_pic)
            chatTitle = itemView.findViewById(R.id.text_chat_title)
            chatLastMessage = itemView.findViewById(R.id.text_chat_last_message)
            chatDate = itemView.findViewById(R.id.text_chat_date)
            chatNewMessageBadgeText = itemView.findViewById(R.id.text_chat_new_message_badge)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the message item layout
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_messages_chat, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = newChatList[position]

        val context = holder.itemView.context

        if(chat.messages!!.size > 1) {
            Glide.with(holder.itemView.context)
                .load(chat.messages!![1].photoUrl)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                .into(holder.chatPic)

            holder.chatPic.clearColorFilter()
            holder.chatPic.scaleX = 1f
            holder.chatPic.scaleY = 1f
        } else {
            val groupDrawable = context.getDrawable(R.drawable.baseline_group_24)
            groupDrawable!!.colorFilter = PorterDuffColorFilter(
                context.getColor(R.color.chat_group_image_icon_tint),
                PorterDuff.Mode.SRC_IN
            )
            holder.chatPic.setImageDrawable(groupDrawable)
            holder.chatPic.scaleX = 0.7f
            holder.chatPic.scaleY = 0.7f

            val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)
            holder.chatPicCard.setCardBackgroundColor(colorOutline)
        }

        holder.chatTitle.text = chat.chatTitle
        holder.chatLastMessage.text = chat.lastMessage
        holder.chatDate.text = formatChatDate(chat.messages?.last()?.timestamp?: Timestamp.now())

        // Check if new messages exist
        for (newChat in newChatList) {
            val oldChat = oldChatList.find { it.chatId == newChat.chatId }
            if (oldChat != null) {
                // Compare messages
                val newMessage = chatViewModel.hasNewMessagesInChat(newChat.messages!!.toList(), oldChat.messages!!.toList())
                if (newMessage != null) {
                    // Found new messages
                    holder.chatNewMessageBadgeText.visibility =  View.VISIBLE

                    // Send Notification
                    updateListener.onChatUpdate(newMessage)
                } else {
                    holder.chatNewMessageBadgeText.visibility =  View.GONE
                }
            } else {
                holder.chatNewMessageBadgeText.visibility =  View.GONE
            }
        }


        holder.itemView.setOnClickListener {
            clickListener.onChatClick(chat)
        }
    }



    override fun getItemCount(): Int {
        return newChatList.size
    }

    private fun formatChatDate(date: Timestamp): String {
        return if(CommonUtils().isToday(date)) {
            CommonUtils.formatTime(date, "HH: mm")
        } else if(CommonUtils().isYesterday(date)) {
            "Yesterday"
        }
        else {
            CommonUtils.formatDate(date, "yyyy/MM/dd")
        }

    }
}