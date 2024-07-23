package com.example.sharide.adapter


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sharide.screen.ride.ViewpagerBookingItemFragment

class BookingPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return ViewpagerBookingItemFragment.newInstance(position)
    }
}