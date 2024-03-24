package com.example.sharidev2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Contact

class ContactAdapter(
    private val contactList: List<Contact>,
    private var cListener: OnItemClickListener
):RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }


    class ContactHolder(contactView:View, listener: OnItemClickListener):RecyclerView.ViewHolder(contactView){
        val contactname: TextView = contactView.findViewById(R.id.tv_display_contact_name)
        val contactPhone: TextView = contactView.findViewById(R.id.tv_display_contact_phone)
        val divider = contactView.findViewById<View>(R.id.divider_emergency_contact_item)
        init{
            contactView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val contactView =LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_emergency_contact,parent,false)
        return ContactHolder(contactView,cListener)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val currentContact = contactList[position]
        holder.contactname.text = currentContact.contactName.toString()
        holder.contactPhone.text = "+60 " + currentContact.contactPhone.toString()


        if(position == contactList.lastIndex) {
            holder.divider.visibility = View.INVISIBLE
        }
    }
}