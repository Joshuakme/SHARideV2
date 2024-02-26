package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentBookingBinding
import com.example.sharidev2.viewmodel.RideViewModel


class BookingFragment : Fragment() {
    private lateinit var binding: FragmentBookingBinding
    private val rideViewModel: RideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking, container, false)


        // ELEMENT VARIABLES
        rideViewModel.rideList.observe(viewLifecycleOwner) {rideList ->
            Toast.makeText(requireContext(), rideList.size.toString(), Toast.LENGTH_LONG).show()
        }

        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()



        return binding.root
    }

}