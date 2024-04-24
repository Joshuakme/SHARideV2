package com.example.sharidev2.screen.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.R
import com.example.sharidev2.adapter.VehicleDocAdapter
import com.example.sharidev2.databinding.FragmentVehicleDocBinding
import com.example.sharidev2.viewmodel.VehicleDocViewModel

class VehicleDocFragment : Fragment() {

    private val viewModel: VehicleDocViewModel by viewModels()
    private lateinit var binding: FragmentVehicleDocBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vehicle_doc, container, false)


        // ELEMENT VARIABLES
        val addVehicleBtn = binding.btnAddVehicleDoc
        val backVehicleDocBtn = binding.btnBackVehicleDoc
        val reminderAddVehicleDocCard = binding.cardVehicleDocReminder
        val vehicleDocProgressBar = binding.progressBarVehicleDoc
        val vehicleDocRecyclerView = binding.recyclerViewVehicleDoc
        var adapter: VehicleDocAdapter



        // Observe the LiveData from the ViewModel
        viewModel.vehicleDocList.observe(viewLifecycleOwner) { vehicleDocList ->


            if(vehicleDocList == null) {
                vehicleDocProgressBar.visibility = View.VISIBLE
                vehicleDocRecyclerView.visibility = View.GONE
                reminderAddVehicleDocCard.visibility = View.GONE
            } else {
                vehicleDocList?.let { list ->
                    if (list.isNotEmpty()) {
                        adapter = VehicleDocAdapter(list, this)
                        vehicleDocRecyclerView.layoutManager = LinearLayoutManager(context)
                        vehicleDocRecyclerView.adapter = adapter

                        vehicleDocProgressBar.visibility = View.GONE
                        vehicleDocRecyclerView.visibility = View.VISIBLE
                        reminderAddVehicleDocCard.visibility = View.GONE
                    } else {
                        // If the list is empty, hide the RecyclerView and show the reminder card
                        vehicleDocProgressBar.visibility = View.GONE
                        vehicleDocRecyclerView.visibility = View.GONE
                        reminderAddVehicleDocCard.visibility = View.VISIBLE
                    }
                }
            }
        }


        // EVENT LISTENERS
        addVehicleBtn.setOnClickListener {
            findNavController().navigate(R.id.action_vehicleDocFragment_to_addVehicleDocFragment)
        }

        backVehicleDocBtn.setOnClickListener {
            findNavController().navigate(R.id.action_vehicleDocFragment_to_personalInformationFragment)
        }

        return binding.root
    }


    fun onItemClick(position: Int) {
        // Retrieve the clicked vehicle from the adapter
        val clickedVehicle = viewModel.vehicleDocList.value?.get(position)


        // Check if the clicked vehicle is not null
        clickedVehicle?.let {
            // Create a bundle to pass vehicle data to the EditVehicleDocFragment
            val bundle = Bundle().apply {
                putParcelable("vehicleDoc", it) // Put Parcelable vehicle doc into the bundle
            }

            //TODO:
            // Navigate to the EditContactFragment and pass the bundle
            //findNavController().navigate(R.id.action_emergencyContactFragment_to_editContactFragment, bundle)
        }
    }
}
