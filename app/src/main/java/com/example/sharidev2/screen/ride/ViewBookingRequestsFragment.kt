package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.PassengerRequestAdapter
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.databinding.FragmentViewBookingRequestsBinding
import com.example.sharidev2.utility.Constants

import com.example.sharidev2.viewmodel.ViewBookingRequestsViewModel
import kotlinx.coroutines.launch

class ViewBookingRequestsFragment : Fragment() {
    private lateinit var binding: FragmentViewBookingRequestsBinding
    private val viewBookingRequestsViewModel: ViewBookingRequestsViewModel by viewModels()

    // GLobal Variables
    private lateinit var rideId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_view_booking_requests, container, false)


        // Data Args
        try {
            rideId = arguments?.get("rideId") as String
            val passengers = (arguments?.get("passengers") as Array<Passenger>).toList()

            if(passengers != null) {
                viewBookingRequestsViewModel.setPassengerList(passengers)
            }
        } catch (e: Exception) {
            Log.e("View Booking Requests Fragment", e.message.toString())
        }


        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnViewBookingRequestsNavBack
        val recyclerView = binding.recyclerViewBookingRequests


        viewBookingRequestsViewModel.passengerList.observe(viewLifecycleOwner) {passengerList ->
            val passengerRequests = passengerList.filter { it.status == UserStatus.REQUESTED }

            if(passengerRequests.isNotEmpty()) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)


                recyclerView.adapter = PassengerRequestAdapter(requireContext(), passengerRequests.toMutableList(),
                    object:PassengerRequestAdapter.OnRequestClickListener {
                        override fun onRequestAcceptClick(
                            passenger: Passenger,
                            onSuccess: (Boolean) -> Unit
                        ) {
                            lifecycleScope.launch {
                                val respond = viewBookingRequestsViewModel.acceptPassengerToRide(passenger, rideId)

                                when(respond) {
                                    Constants.FIREBASE_REQUEST_SUCCESS -> {
                                        onSuccess(true)
                                    }

                                    else -> {
                                        onSuccess(false)

                                        Toast.makeText(requireContext(), "Failed to accept", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                            Toast.makeText(requireContext(), "Accepted", Toast.LENGTH_SHORT).show()
                        }

                        override fun onRequestRejectClick(
                            passenger: Passenger,
                            onSuccess: (Boolean) -> Unit
                        ) {
                            lifecycleScope.launch {
                                val respond = viewBookingRequestsViewModel.rejectPassengerToRide(passenger, rideId)

                                when(respond) {
                                    Constants.FIREBASE_REQUEST_SUCCESS -> {
                                        onSuccess(true)

                                        Toast.makeText(requireContext(), "Rejected", Toast.LENGTH_SHORT).show()
                                    }

                                    else -> {
                                        onSuccess(false)

                                        Toast.makeText(requireContext(), "Failed to reject", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                        }
                    }
                )
            }
        }


        // NAVIGATION LISTENERS
        // View Booking Request Fragment -> Booking Detail Fragment
        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }

}