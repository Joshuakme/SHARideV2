package com.example.sharidev2.screen.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.databinding.FragmentEditVehicleDocBinding
import com.example.sharidev2.viewmodel.VehicleDocViewModel

class EditVehicleDocFragment: Fragment(){
    private lateinit var binding: FragmentEditVehicleDocBinding
    private lateinit var vehicleDoc: VehicleDoc
    private val viewModel: VehicleDocViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //ELEMENT VARIABLE
        val backVehicleDocBtn = binding.btnBackEditVehicleDoc


        //NAVIGATION EVENT LISTENERS
        //Enter Emergency Contact Details -> Emergency Contact Fragment
        backVehicleDocBtn.setOnClickListener{
            //TODO:
            //findNavController().navigate(R.id.)
        }


        // Retrieve the Parcelable Vehicle Doc from arguments
        vehicleDoc = arguments?.getParcelable("vehicleDoc")!!


        vehicleDoc.let {
            val firstName = it.firstName
            val lastName = it.lastName
            val vehicleType = it.vehicleType
            val vehicleModel = it.vehicleModel
            val carPlate = it.carPlate
            val manufactureDate = it.manufactureDate
            val vehicleRegisCert = it.vehicleRegisCert
            val roadtax = it.roadtax
            val insurance = it.insurance
            val vehicleId = it.vehicleId
            val userUid = it.userUid


//            vehicleDoc = VehicleDoc(
//                firstName,
//                lastName,
//                vehicleType,
//                vehicleModel,
//                carPlate.toString(),
//                manufactureDate,
//                vehicleRegisCert,
//                roadtax,
//                insurance,
//                vehicleId,
//                userUid
//            )
        }

        }




}