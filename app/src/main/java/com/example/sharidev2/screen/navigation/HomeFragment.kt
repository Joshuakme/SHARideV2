package com.example.sharidev2.screen.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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
                0 -> tab.text = "Find Ride"
                1 -> tab.text = "Offer Ride"
            }
        }.attach()


        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }

}