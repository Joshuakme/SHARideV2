package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.sharidev2.R
import com.example.sharidev2.adapter.RidePagerAdapter
import com.example.sharidev2.databinding.FragmentHomeBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment(), OnMapReadyCallback {
    // Variables Init
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mGoogleMap: GoogleMap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_home_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager2 = binding.viewPager

        val pagerAdapter = RidePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Find Ride"
                    tab.customView = null  // Reset custom view
                    tab.view?.setBackgroundResource(R.drawable.left_tab_background)
                    tab.view?.minimumWidth = 0  // Reset minimum width
                    tab.view?.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
                1 -> {
                    tab.text = "Offer Ride"
                    tab.customView = null  // Reset custom view
                    tab.view?.setBackgroundResource(R.drawable.right_tab_background)
                    tab.view?.minimumWidth = 0  // Reset minimum width
                    tab.view?.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }.attach()

        // Handle tab selection events
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Customize appearance for the selected tab
                tab?.let {
                    it.view.setBackgroundResource(R.drawable.selected_tab_background)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Customize appearance for unselected tabs
                tab?.let {
                    it.view.setBackgroundResource(R.drawable.unselected_tab_background)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing when a tab is reselected
            }
        })

        // Select the default tab (e.g., the first tab)
        tabLayout.getTabAt(0)?.select()



        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }

}