package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private val messageList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        // Set text, load images, etc.

        if(holder.javaClass == SentViewHolder::class.java) {
            // render sent message
            val viewHolder = holder as SentViewHolder

            holder.sentMessage.text = message.text
        } else {
            // render received message
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMessage.text = message.text
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
        val sentMessage = itemView.findViewById<TextView>(R.id.text_message_sent)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage = itemView.findViewById<TextView>(R.id.text_message_receive)
    }
}