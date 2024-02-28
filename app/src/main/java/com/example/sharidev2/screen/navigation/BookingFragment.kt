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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.BookingAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentBookingBinding
import com.example.sharidev2.firebase.FirebaseInitializer
import com.example.sharidev2.viewmodel.RideViewModel
import com.google.firebase.auth.FirebaseUser


class BookingFragment : Fragment() {
    private lateinit var binding: FragmentBookingBinding
    private val rideViewModel: RideViewModel by viewModels()

    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking, container, false)


        // ELEMENT VARIABLES

        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(true)
        (activity as MainActivity).resetBottomNavPosition()

        showLoadingProgressBar(true)
        initRecyclerView()


        return binding.root
    }

    private fun initRecyclerView() {
        val recyclerView = binding.recyclerBooking
        val currentUser = FirebaseInitializer.firebaseAuth.currentUser

        rideViewModel.rideList.observe(viewLifecycleOwner) {rideList ->
            if(rideList != null) {
                val filteredList = rideList.filter { ride ->
                    if (currentUser != null && ride.price != null) {
                        ride.price.containsKey(currentUser.uid) || (ride.driver.uid == currentUser.uid)
                    } else {
                        false
                    }
                }

                if(currentUser != null && filteredList.isNotEmpty()) {
                    bookingAdapter = BookingAdapter(requireContext(), currentUser, filteredList, object: BookingAdapter.OnBookingClickListener {
                        override fun onBookingClick(booking: Ride) {
                            TODO("Navigate to booking detail page")
                            TODO("Pass data to the detail page")
                        }
                    })
                    recyclerView.adapter = bookingAdapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())

                    showLoadingProgressBar(false)
                } else {
                    // No
                    showLoadingProgressBar(false)
                    showErrorLoading(true)
                }
            } else {
                showLoadingProgressBar(false)
            }
        }
    }


    private fun showLoadingProgressBar(show: Boolean) {
        val bookingRecyclerView = binding.recyclerBooking
        val loadingProgressBar = binding.clBookingLoadingSpinner
        val errorLoadingCard = binding.cardErrorLoadBookingHistory

        if(show) {
            bookingRecyclerView.visibility = View.GONE
            loadingProgressBar.visibility = View.VISIBLE
            errorLoadingCard.visibility = View.GONE
        } else {
            bookingRecyclerView.visibility = View.VISIBLE
            loadingProgressBar.visibility = View.GONE
            errorLoadingCard.visibility = View.GONE
        }
    }

    private fun showErrorLoading(show: Boolean) {
        val errorLoadingCard = binding.cardErrorLoadBookingHistory

        if(show) {
            errorLoadingCard.visibility = View.VISIBLE
        } else {
            errorLoadingCard.visibility = View.GONE
        }
    }
}