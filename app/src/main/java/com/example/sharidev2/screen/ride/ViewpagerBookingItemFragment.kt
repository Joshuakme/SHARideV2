package com.example.sharidev2.screen.ride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.R
import com.example.sharidev2.adapter.BookingAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentViewpagerBookingItemBinding
import com.example.sharidev2.screen.navigation.BookingFragmentDirections
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.RideViewModel


class ViewpagerBookingItemFragment :
    Fragment(),
    BookingAdapter.OnBookingClickListener {
    private lateinit var binding: FragmentViewpagerBookingItemBinding
    private val rideViewModel: RideViewModel by viewModels()
    private lateinit var adapter: BookingAdapter
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    // CONSTANT
    val STATUS_SUCCESS = 0
    val STATUS_LOADING = 1
    val STATUS_ERROR = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_viewpager_booking_item, container, false)


        // ELEMENT VARIABLES
        val recyclerView = binding.recyclerBooking



        adapter = BookingAdapter(requireContext(), currentUser?.uid?: "", emptyList(),this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabPosition = requireArguments().getInt(ARG_TAB_POSITION)
        loadDataForTab(tabPosition)
    }

    private fun loadDataForTab(tabPosition: Int) {
        val recyclerView = binding.recyclerBooking

        if(currentUser != null) {
            when (tabPosition) {
                0 -> {
                    rideViewModel.activeRideList.observe(viewLifecycleOwner) {activeRideList ->
                        if(activeRideList != null && activeRideList.isNotEmpty()) {
                            adapter.updateList(activeRideList)
                            recyclerView.adapter = adapter

                            showLoadingStatusProgressBar(STATUS_SUCCESS)
                        } else {
                            showLoadingStatusProgressBar(STATUS_ERROR)
                        }

                    }
                }

                1 -> {
                    rideViewModel.pastRideList.observe(viewLifecycleOwner) { pastRideList ->
                        if(pastRideList != null && pastRideList.isNotEmpty()) {
                            adapter.updateList(pastRideList)
                            recyclerView.adapter = adapter

                            showLoadingStatusProgressBar(STATUS_SUCCESS)
                        } else {
                            showLoadingStatusProgressBar(STATUS_ERROR)
                        }
                    }
                }
                else -> {
                    showLoadingStatusProgressBar(STATUS_ERROR)
                }
            }

        } else {
            adapter.updateList(emptyList())
            showLoadingStatusProgressBar(STATUS_ERROR)
        }
    }

    companion object {
        private const val ARG_TAB_POSITION = "tab_position"

        fun newInstance(tabPosition: Int): ViewpagerBookingItemFragment {
            val fragment = ViewpagerBookingItemFragment()
            val args = Bundle().apply {
                putInt(ARG_TAB_POSITION, tabPosition)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBookingClick(booking: Ride) {
        val action = BookingFragmentDirections.actionBookingFragmentToBookingDetailFragment(booking)

        findNavController().navigate(action)
    }

    private fun showLoadingStatusProgressBar(status: Int) {
        val bookingRecyclerView = binding.recyclerBooking
        val loadingProgressBar = binding.clBookingLoadingSpinner
        val errorLoadingCard = binding.cardErrorLoadBookingHistory

        when(status) {
            STATUS_SUCCESS -> {
                // Show Recycler View
                bookingRecyclerView.visibility = View.VISIBLE
                loadingProgressBar.visibility = View.GONE
                errorLoadingCard.visibility = View.GONE
            }
            STATUS_LOADING -> {
                // Show Loading
                bookingRecyclerView.visibility = View.GONE
                loadingProgressBar.visibility = View.VISIBLE
                errorLoadingCard.visibility = View.GONE
            }
            STATUS_ERROR -> {
                // Show Error
                bookingRecyclerView.visibility = View.GONE
                loadingProgressBar.visibility = View.GONE
                errorLoadingCard.visibility = View.VISIBLE
            }
        }
    }

}