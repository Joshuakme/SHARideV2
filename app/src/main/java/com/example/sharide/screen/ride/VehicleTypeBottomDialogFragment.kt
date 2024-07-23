package com.example.sharide.screen.ride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharide.R
import com.example.sharide.adapter.VehicleTypeAdapter
import com.example.sharide.data.model.VehicleType
import com.example.sharide.viewmodel.SharedSearchRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VehicleTypeBottomDialogFragment :
    BottomSheetDialogFragment(),
    VehicleTypeAdapter.OnVehicleTypeClickListener {

    private var initVehicleTypeList: List<String> = getInitVehicleTypeList()
    private lateinit var adapter: VehicleTypeAdapter
    private var recyclerView: RecyclerView?= null
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_dialog_vehicle_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recyclerView here
        recyclerView = view.findViewById(R.id.recycler_bottom_dialog_vehicle_type)

        // ELEMENT VARIABLES

        // Initialize adapter
        setupRecyclerView(initVehicleTypeList)

        // LAYOUT


        // EVENT LISTENERS

    }

    private fun setupRecyclerView(vehicleTypeList: List<String>) {
        adapter = VehicleTypeAdapter(vehicleTypeList, this)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = adapter
    }

    override fun onVehicleTypeClick(vehicleType: String) {
        try {
            val selectedVehicleType: VehicleType = enumValueOf(vehicleType)

            searchRideViewModel.setVehicleType(selectedVehicleType)
        } catch (e: IllegalArgumentException) {

        }

        // Hide the bottom dialog after click
        dismiss()
    }

    private fun getInitVehicleTypeList(): List<String> {
        return enumValues<VehicleType>().map { it.name }
    }
}