package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.BookingAdapter
import com.example.sharidev2.adapter.BookingPagerAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentBookingBinding
import com.example.sharidev2.firebase.FirebaseInitializer
import com.example.sharidev2.viewmodel.RideViewModel
import com.google.android.material.tabs.TabLayoutMediator


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

//        showLoadingProgressBar(true)
//        initRecyclerView()
       initTabLayout(this)


        return binding.root
    }

//    private fun initRecyclerView() {
//        val recyclerView = binding.recyclerBooking
//        val currentUser = FirebaseInitializer.firebaseAuth.currentUser
//
//        rideViewModel.rideList.observe(viewLifecycleOwner) {rideList ->
//            if(rideList != null) {
//                val filteredList = rideList.filter { ride ->
//                    if (currentUser != null && ride.price != null) {
//                        ride.price.containsKey(currentUser.uid) || (ride.driver.uid == currentUser.uid)
//                    } else {
//                        false
//                    }
//                }
//
//                if(currentUser != null && filteredList.isNotEmpty()) {
//                    bookingAdapter = BookingAdapter(requireContext(), currentUser, filteredList, object: BookingAdapter.OnBookingClickListener {
//                        override fun onBookingClick(booking: Ride) {
//                            TODO("Navigate to booking detail page")
//                            TODO("Pass data to the detail page")
//                        }
//                    })
//                    recyclerView.adapter = bookingAdapter
//                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//                    showLoadingProgressBar(false)
//                } else {
//                    // No
//                    showLoadingProgressBar(false)
//                    showErrorLoading(true)
//                }
//            } else {
//                showLoadingProgressBar(false)
//            }
//        }
//    }


    private fun initTabLayout(fragment: Fragment) {
        val tabLayout = binding.tabLayoutBooking
        val viewpager = binding.viewpagerBooking

        val adapter = BookingPagerAdapter(fragment)

        viewpager.adapter = adapter


        TabLayoutMediator(tabLayout, viewpager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Active"
                    tab.customView = null  // Reset custom view
                    tab.view?.minimumWidth = 0  // Reset minimum width
                    tab.view?.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
                1 -> {
                    tab.text = "Past"
                    tab.customView = null  // Reset custom view
                    tab.view?.minimumWidth = 0  // Reset minimum width
                    tab.view?.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }.attach()

        // Select the default tab (e.g., the first tab)
        tabLayout.getTabAt(0)?.select()
    }

}