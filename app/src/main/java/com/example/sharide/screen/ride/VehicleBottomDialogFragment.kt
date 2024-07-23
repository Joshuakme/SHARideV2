package com.example.sharide.screen.ride

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
import com.example.sharide.adapter.VehicleAdapter
import com.example.sharide.data.model.Vehicle
import com.example.sharide.viewmodel.DriverVehicleViewModel
import com.example.sharide.viewmodel.SharedCreateRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class VehicleBottomDialogFragment :
    BottomSheetDialogFragment(),
    VehicleAdapter.OnVehicleClickListener {

    private lateinit var adapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView
    private val driverVehicleViewModel: DriverVehicleViewModel = DriverVehicleViewModel()
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_dialog_vehicle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize the recyclerView here
        recyclerView = requireView().findViewById(R.id.recycler_bottom_dialog_vehicle)
        val loadingProgressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar_vehicle)

        // ELEMENT VARIABLES


        driverVehicleViewModel.vehicleList.observe(viewLifecycleOwner) {vehicleList ->
            // Initialize adapter
            if(vehicleList.isNotEmpty()) {
                loadingProgressBar.visibility = View.GONE
                setupRecyclerView(vehicleList)
                recyclerView.visibility = View.VISIBLE
            }

        }

    }

    private fun setupRecyclerView(vehicleList: List<Vehicle>) {
        adapter = VehicleAdapter(vehicleList, this)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }

    override fun onVehicleClick(vehicle: Vehicle) {
        // Update the text in the spinner when a recycler item is pressed
        try {
            createRideViewModel.setVehicle(vehicle)
        } catch (e: IllegalArgumentException) {
            Log.e("Vehicle Bottom Dialog Fragment", e.message.toString())
        }

        // Hide the bottom dialog after click
        dismiss()
    }
}