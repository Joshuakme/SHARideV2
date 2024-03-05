package com.example.sharidev2.screen.ride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentViewBookingRequestsBinding


class ViewBookingRequestsFragment : Fragment() {
    private lateinit var binding: FragmentViewBookingRequestsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_view_booking_requests, container, false)


        // ELEMENT VARIABLES



        return binding.root
    }

    private fun setupOnClickListeners() {

    }
}