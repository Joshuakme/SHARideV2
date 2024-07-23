package com.example.sharide.screen.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sharide.R
import com.example.sharide.data.model.Vehicle
import com.example.sharide.databinding.FragmentViewVehicleBinding
import com.example.sharide.utility.CommonUtils
import com.example.sharide.utility.Constants
import com.example.sharide.viewmodel.ViewVehicleViewModel
import kotlinx.coroutines.launch


class ViewVehicleFragment : Fragment() {

    private lateinit var binding: FragmentViewVehicleBinding
    private val viewModel: ViewVehicleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewVehicleBinding.inflate(inflater, container, false)

        val backButton = binding.btnBackViewVehicleDoc
        val deleteVehicleButton = binding.btnDeleteVehicle

        val vehicleArguments = arguments?.get("vehicle") as Vehicle?


        if (vehicleArguments != null) {
            viewModel.setVehicle(vehicleArguments)
            lifecycleScope.launch {
                viewModel.getVehicleDoc()

                if(viewModel.vehicleDoc.value == null) {
                    Toast.makeText(context, "No VehicleDoc Record Found", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_viewVehicleFragment_to_vehicleDocFragment)
                }
            }

        }else {
            Toast.makeText(context, "No Vehicle Record Found", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_viewVehicleFragment_to_vehicleDocFragment)
        }



        val firstName = binding.layoutViewVehicleFirstName
        val lastName = binding.layoutViewVehicleLastName
        val model = binding.layoutViewVehicleModel
        val type = binding.spinnerViewVehicleType
        val brand = binding.layoutViewVehicleBrand
        val color = binding.layoutViewVehicleColor
        val plate = binding.layoutViewCarPlate
        val manufactureDate = binding.viewDateManufacture
        val capacity = binding.layoutViewVehicleCapacity

        viewModel.vehicleLiveData.observe(viewLifecycleOwner) { vehicle ->
            if (vehicle != null) {
                model.text = vehicle.model
                type.text = vehicle.type.toString()
                brand.text = vehicle.brand
                color.text = vehicle.color
                plate.text = vehicle.plateNumber
                capacity.text = vehicle.capacity.toString()
            }
        }

        viewModel.vehicleDoc.observe(viewLifecycleOwner) { vehicleDoc ->
            if (vehicleDoc != null) {


                firstName.text = vehicleDoc.firstName
                lastName.text = vehicleDoc.lastName

                if (vehicleDoc.manufactureDate != null) {
                    manufactureDate.text = CommonUtils.formatDate(vehicleDoc.manufactureDate!!)
                }
            }
        }

        //OnClickListener
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewVehicleFragment_to_vehicleDocFragment)
        }

        deleteVehicleButton.setOnClickListener {

            lifecycleScope.launch {
                val response = viewModel.deleteVehicle()

                when (response) {
                    Constants.FIREBASE_REQUEST_SUCCESS -> {

                        Toast.makeText(
                            requireContext(),
                            "Vehicle Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().popBackStack()
                    }

                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED -> {
                        Toast.makeText(
                            requireContext(),
                            "User not authenticated. Please log in again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Constants.FIREBASE_REQUEST_NOT_BELONG_USER -> {
                        Toast.makeText(
                            requireContext(),
                            "Please log in your account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else ->{
                    Toast.makeText(
                        requireContext(),
                        "Error ${response}",
                        Toast.LENGTH_SHORT
                    ).show()
                    }
                }

            }



        }

        return binding.root


    }


}