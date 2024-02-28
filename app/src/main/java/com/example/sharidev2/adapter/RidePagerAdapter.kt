package com.example.sharidev2.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
<<<<<<< HEAD
import com.example.sharidev2.screen.home.FindRideFragment
import com.example.sharidev2.screen.home.OfferRideFragment
=======
import com.example.sharidev2.screen.ride.FindRideFragment
import com.example.sharidev2.screen.ride.OfferRideFragment
>>>>>>> main
import com.example.sharidev2.screen.navigation.HomeFragment

class RidePagerAdapter(fragmentActivity: HomeFragment) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FindRideFragment()
            1 -> OfferRideFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}