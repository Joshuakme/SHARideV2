package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.PassengerCapacityAdapter
import com.example.sharidev2.adapter.VehicleAdapter
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.viewmodel.DriverVehicleViewModel
import com.example.sharidev2.viewmodel.SharedCreateRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class PassengerCapacityBottomDialogFragment :
    BottomSheetDialogFragment(),
    PassengerCapacityAdapter.OnCapacityClickListener {

    private lateinit var adapter: PassengerCapacityAdapter
    private lateinit var recyclerView: RecyclerView
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_bottom_dialog_passenger_capacity,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize the recyclerView here
        recyclerView = requireView().findViewById(R.id.recycler_bottom_dialog_passenger_capacity)
        val loadingProgressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar_passenger_capacity)

        // ELEMENT VARIABLES

        createRideViewModel.vehicle.observe(viewLifecycleOwner) {vehicle ->
            // Initialize adapter
            if(vehicle != null) {
                loadingProgressBar.visibility = View.GONE
                setupRecyclerView(createCapacityListFromVehicle(vehicle.capacity))
                recyclerView.visibility = View.VISIBLE
            }

        }
    }


    private fun setupRecyclerView(capacityList: List<Int>) {
        adapter = PassengerCapacityAdapter(capacityList, this)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }

    private fun createCapacityListFromVehicle(capacity: Int) : List<Int> {
        val capacityList: MutableList<Int> = mutableListOf()

        return if(capacity > 0) {
            for(i in 1..capacity) {
                capacityList.add(i)
            }
            capacityList
        } else {
            emptyList()
        }
    }

    override fun onCapacityClick(capacity: Int) {
        if(capacity > 0) {
            try {
                createRideViewModel.setCapacity(capacity)
            } catch (e: IllegalArgumentException) {
                Log.e("Passenger Capacity Bottom Dialog Fragment", e.message.toString())
            }
        }

        // Hide the bottom dialog after click
        dismiss()
    }

}