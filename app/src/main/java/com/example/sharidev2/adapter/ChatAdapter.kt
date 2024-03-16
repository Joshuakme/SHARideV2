package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.viewmodel.ChatViewModel
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
        var chatPic: ImageButton
        var chatTitle: TextView
        var chatLastMessage: TextView
        var chatDate: TextView
        var chatNewMessageBadgeText: TextView

        init {
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