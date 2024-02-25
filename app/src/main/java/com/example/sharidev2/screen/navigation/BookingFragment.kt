package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentBookingBinding


class BookingFragment : Fragment() {
    private lateinit var binding: FragmentBookingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking, container, false)


        // ELEMENT VARIABLES


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)



        return binding.root
    }


}