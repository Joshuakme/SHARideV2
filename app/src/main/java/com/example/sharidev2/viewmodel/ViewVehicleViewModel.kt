package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient

class ViewVehicleViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private val repository = VehicleDocRepository()

    //DATA KEY CONSTANT
    private val VEHICLE_KEY = "vehicle"
    private val VEHICLE_DOC_KEY = "vehicle_doc"

    //LIVE DATA
    val vehicleLiveData: LiveData<Vehicle?> = savedStateHandle.getLiveData(VEHICLE_KEY, null)
    val vehicleDoc: LiveData<VehicleDoc?> = savedStateHandle.getLiveData(VEHICLE_DOC_KEY, null)

    //SETTER
    fun setVehicle(newVehicle: Vehicle){
        savedStateHandle[VEHICLE_KEY] = newVehicle
    }

    fun setVehicleDoc(newVehicleDoc: VehicleDoc){
        savedStateHandle[VEHICLE_DOC_KEY] = newVehicleDoc
    }

    suspend fun getVehicleDoc(){
        val vehicle = vehicleLiveData.value
        if(vehicle?.documentId != null){
            val vehicleDoc = FirebaseClient.getVehicleDoc(vehicle.documentId)



            if (vehicleDoc != null) {
                setVehicleDoc(newVehicleDoc = vehicleDoc)
            }
        }

    }

    suspend fun deleteVehicle(): Int{
        if(vehicleLiveData.value != null) {

                return repository.deleteVehicle(vehicleLiveData.value?.vehicleId!!)


        }else{
            return Constants.FIREBASE_REQUEST_FAILED
        }
    }
}