package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Message


class MessageAdapter(private val messages: List<Message>) :

    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Add views for a message item (e.g., TextViews, ImageViews)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Inflate the message item layout
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_messages_chat, parent, false)

        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // Bind data to views in the message item
        val message = messages[position]
        // Set text, load images, etc.
    }

    override fun getItemCount(): Int {
        return messages.size
    }

}