package com.example.sharide.screen.navigation

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.example.sharide.MainActivity
import com.example.sharide.R
import com.example.sharide.adapter.BookingPagerAdapter
import com.example.sharide.databinding.FragmentBookingBinding
import com.example.sharide.utility.CommonUtils
import com.google.android.material.tabs.TabLayoutMediator


class BookingFragment : Fragment() {
    private lateinit var binding: FragmentBookingBinding

    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking, container, false)


        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }

        // ELEMENT VARIABLES

        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(true)
        activity.resetBottomNavPosition()



       initTabLayout(this)


        return binding.root
    }



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
                    tab.view.minimumWidth = 0  // Reset minimum width
                    tab.view.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
                1 -> {
                    tab.text = "Past"
                    tab.customView = null  // Reset custom view
                    tab.view.minimumWidth = 0  // Reset minimum width
                    tab.view.layoutParams = LinearLayout.LayoutParams(
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