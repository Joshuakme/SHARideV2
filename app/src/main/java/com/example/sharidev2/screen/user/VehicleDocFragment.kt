package com.example.sharidev2.screen.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.VehicleAdapter
import com.example.sharidev2.adapter.ViewVehicleAdapter
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.databinding.FragmentVehicleDocBinding
import com.example.sharidev2.viewmodel.VehicleDocViewModel

class VehicleDocFragment : Fragment() {

    private val viewModel: VehicleDocViewModel by viewModels()
    private lateinit var binding: FragmentVehicleDocBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vehicle_doc, container, false)


        // ELEMENT VARIABLES
        val addVehicleBtn = binding.btnAddVehicleDoc
        val backVehicleDocBtn = binding.btnBackVehicleDoc
        val reminderAddVehicleDocCard = binding.cardVehicleDocReminder
        val vehicleDocProgressBar = binding.progressBarVehicleDoc
        val vehicleDocRecyclerView = binding.recyclerViewVehicleDoc
        var adapter: ViewVehicleAdapter



        // Observe the LiveData from the ViewModel
        viewModel.vehicleList.observe(viewLifecycleOwner) { vehicleList ->
            val vehicleDocList = viewModel.vehicleDocList.value

            if(vehicleList == null || vehicleDocList == null) {
                vehicleDocProgressBar.visibility = View.VISIBLE
                vehicleDocRecyclerView.visibility = View.GONE
                reminderAddVehicleDocCard.visibility = View.VISIBLE
            } else {
                   if (vehicleList.isNotEmpty() && vehicleDocList.isNotEmpty()) {
                        adapter = ViewVehicleAdapter(vehicleList, vehicleDocList = vehicleDocList.toList(), clickListener = object:  ViewVehicleAdapter.OnVehicleClickListener {
                            override fun onVehicleClick(vehicle: Vehicle) {
                                val directions = VehicleDocFragmentDirections.actionVehicleDocFragmentToViewVehicleFragment(vehicle)
                                findNavController().navigate(directions)
                            }

                        })
                        vehicleDocRecyclerView.layoutManager = LinearLayoutManager(context,  RecyclerView.VERTICAL, false)
                        vehicleDocRecyclerView.adapter = adapter

                        vehicleDocProgressBar.visibility = View.GONE
                        vehicleDocRecyclerView.visibility = View.VISIBLE
                        reminderAddVehicleDocCard.visibility = View.GONE
                       Toast.makeText(context,"Got List", Toast.LENGTH_SHORT).show()
                    } else {
                        // If the list is empty, hide the RecyclerView and show the reminder card
                        vehicleDocProgressBar.visibility = View.GONE
                        vehicleDocRecyclerView.visibility = View.GONE
                        reminderAddVehicleDocCard.visibility = View.VISIBLE
                      // Toast.makeText(context,"No Vehicle Found", Toast.LENGTH_SHORT).show()
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
