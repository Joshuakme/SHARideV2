package com.example.sharidev2.screen.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentSearchSelectOriginBinding
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.viewmodel.SearchRideViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions


class SearchSelectOriginFragment : Fragment() {
    private lateinit var binding: FragmentSearchSelectOriginBinding
    private val searchRideViewModel: SearchRideViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_select_origin, container, false)

        // ELEMENT VARIABLES
        val backBtn = binding.cardSearchSelectOriginBackContainer
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_search_origin_container) as SupportMapFragment
        val originNameText = binding.textSearchSelectOriginLocationName
        val originDistanceAddress = binding.textSearchSelectOriginLocationDistanceAddress



        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)


        //Toast.makeText(requireContext(), searchRideViewModel.origin.value?.name.toString(), Toast.LENGTH_SHORT).show()
        // GOOGLE MAP
        searchRideViewModel.origin.observe(viewLifecycleOwner) {

            mapFragment.getMapAsync { googleMap ->
                // Handle the GoogleMap instance
                // You can use the googleMap object to add markers, set camera position, etc.

                Toast.makeText(requireContext(), it.geolocation?.longitude.toString(), Toast.LENGTH_SHORT).show()

                it.geolocation?.let { location ->
                    val markerOptions = MarkerOptions().position(location).title(it.name)
                    googleMap.addMarker(markerOptions)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f))
                }
            }

            originNameText.text = it.name
            originDistanceAddress.text = it.detailAddress
        }





        // NAVIGATION EVENT LISTENERS
        // Search Fragment -> Home Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_searchSelectOriginFragment_to_searchFragment)
        }

        return binding.root
    }


}