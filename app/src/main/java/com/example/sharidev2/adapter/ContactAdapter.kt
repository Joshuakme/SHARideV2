package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.screen.emergency.contactClass

class ContactAdapter(private val contactList:ArrayList<contactClass>):RecyclerView.Adapter<ContactAdapter.contactHolder>() {
    private lateinit var cListener:OnItemClickListener
    interface  OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        cListener =listener
    }

    class contactHolder(contactView:View,listener: OnItemClickListener):RecyclerView.ViewHolder(contactView){
        val contactname: TextView =contactView.findViewById(R.id.tv_display_contact_name)
        val contactPhone: TextView =contactView.findViewById(R.id.tv_display_contact_phone)
        init{
            contactView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): contactHolder {
        val contactView =LayoutInflater.from(parent.context).inflate(R.layout.contact,parent,false)
        return contactHolder(contactView,cListener)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: contactHolder, position: Int) {
        val currentContact =contactList[position]
        holder.contactname.text =currentContact.contactName.toString()
        holder.contactPhone.text =currentContact.contactPhoneNo.toString()
    }
}