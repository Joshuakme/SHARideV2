package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.model.VehicleType
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VehicleDetailsViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val repository = VehicleDocRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    // DATA KEY CONSTANT
    private val VEHICLE_DETAILS_KEY = "vehicle_details_list"
    private val VEHICLE_TYPE_KEY = "vehicle_type_list"
    private val VEHICLE_BRAND_KEY = "vehicle_brand_list"
    private val VEHICLE_MODEL_KEY = "vehicle_model_list"
    private val VEHICLE_COLOR_KEY = "vehicle_color_list"
    private val VEHICLE_CAPACITY_KEY = "vehicle_capacity_list"
    private val VEHICLE_PLATE_KEY = "vehicle_plate_list"
    private val VEHICLE_PHOTOS_KEY = "vehicle_photo_list"
    private val VEHICLE_ID_KEY = "vehicle_id_list"
    private val VEHICLE_DETAILS_LIST_KEY = "vehicle_details_list"



    val currentVehicleDoc: LiveData<VehicleDoc> = savedStateHandle.getLiveData(VEHICLE_DETAILS_KEY)
    val vehicleId: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ID_KEY)
    val vehicleType: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY, VehicleType.Sedan)
    val vehicleModel: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_MODEL_KEY)
    val vehicleBrand: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_BRAND_KEY)
    val vehicleColor: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_COLOR_KEY)
    val vehicleCapacity: LiveData<Int> = savedStateHandle.getLiveData(VEHICLE_CAPACITY_KEY, 0)
    val vehiclePlate: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_PLATE_KEY)
    val vehiclePhotos: LiveData<MutableList<Uri>> = savedStateHandle.getLiveData(VEHICLE_PHOTOS_KEY)


    // INTERNAL DATA MEMBERS
    // vehicle doc
    val vehicleDetailsList: LiveData<MutableList<Vehicle>> =
        savedStateHandle.getLiveData(VEHICLE_DETAILS_LIST_KEY)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            val vehicleDetails = repository.getAllVehicles().toMutableList()
            val vehicleDetailsUriList = repository.getAllVehicles()

            if (vehicleDetailsUriList != null) {

            }

            setVehicleList(vehicleDetails)

        }

        repository.listenForVehicleDocChanges { vehicles, exception ->
            if (exception != null) {
                // Handle error
                return@listenForVehicleDocChanges
            }


            val vehicleDetails = vehicles?.filter {
                it.userUid == currentUser?.uid
            }?.toMutableList()

            setVehicleList(vehicleDetails.orEmpty().toMutableList())
        }
    }


    // SETTER in SavedStateHandle
    fun setVehicleDoc(newVehicleDoc: VehicleDoc) {
        savedStateHandle[VEHICLE_DETAILS_KEY] = newVehicleDoc
    }

    fun setVehicleType(newVehicleType: VehicleType){
        savedStateHandle[VEHICLE_TYPE_KEY] = newVehicleType
    }

    fun setVehicleModel(newVehicleModel: String){
        savedStateHandle[VEHICLE_MODEL_KEY] = newVehicleModel
    }

    fun setVehicleBrand(newVehicleBrand: String){
        savedStateHandle[VEHICLE_BRAND_KEY] = newVehicleBrand
    }

    fun setVehicleColor(newVehicleColor: String){
        savedStateHandle[VEHICLE_COLOR_KEY] = newVehicleColor
    }

    fun setVehicleCapacity(newVehicleCapacity: Int){
        savedStateHandle[VEHICLE_TYPE_KEY] = newVehicleCapacity
    }

    fun setVehiclePlate(newVehiclePlate: String){
        savedStateHandle[VEHICLE_PLATE_KEY] = newVehiclePlate
    }

    fun addVehiclePhotos(newVehiclePhotos: Uri) {
        if (vehiclePhotos.isInitialized) {
            vehiclePhotos.value!!.add(newVehiclePhotos)
        }
    }

    fun setVehicleId(newVehicleId: String) {
        savedStateHandle[VEHICLE_ID_KEY] = newVehicleId

        viewModelScope.launch {
            val newVehicleDoc = repository.getVehicleDoc(newVehicleId)

            if (newVehicleDoc != null) {
                setVehicleDoc(newVehicleDoc)
            }
        }
    }

    fun setVehicleList(newVehicleList: MutableList<VehicleDoc>) {
        savedStateHandle[VEHICLE_DETAILS_LIST_KEY] = newVehicleList
    }


    suspend fun saveVehicleDocumentation(): Int {
        val newVehicleDetails = Vehicle(
            vehicleID = vehicleId.value,
            brand = vehicleBrand.value,
            model = vehicleModel.value,
            type = vehicleType.value!!,
            plateNumber = vehiclePlate.value,
            color = vehicleColor.value,
            photos = vehiclePhotos.value,
            capacity = vehicleCapacity.value!!
        )
        // save Vehicle
        // Handle result and provide feedback to the fragment
        return repository.addVehicle(newVehicleDetails)
    }


    // Vehicle
    suspend fun addVehicleDetails(): Int {
        if (currentUser?.uid != null) {
            val newVehicleDetails = Vehicle(
                vehicleID = vehicleId.value,
                brand = vehicleBrand.value,
                model = vehicleModel.value,
                type = vehicleType.value!!,
                plateNumber = vehiclePlate.value,
                color = vehicleColor.value,
                photos = vehiclePhotos.value,
                capacity = vehicleCapacity.value!!
            )

            vehicleDetailsList.value?.add(newVehicleDetails)
            return repository.addVehicle(newVehicleDetails)
        } else {
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
    }
}