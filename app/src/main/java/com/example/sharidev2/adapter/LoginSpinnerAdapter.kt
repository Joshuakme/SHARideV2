package com.example.sharidev2.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.model.Country

class LoginSpinnerAdapter (context: Context, countryList: List<Country>) :
ArrayAdapter<Country>(context, R.layout.spinner_dropdown_item_country_code, countryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.text_drop_down_country_code)
        textView.text = getItem(position)?.countryCode
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.text_drop_down_country_code)
        textView.text = getItem(position)?.countryCode
        return view
    }
}