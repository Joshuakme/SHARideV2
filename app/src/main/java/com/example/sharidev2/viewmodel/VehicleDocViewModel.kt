package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.FirebaseResponse
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.model.VehicleType
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VehicleDocViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repository = VehicleDocRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    // DATA KEY CONSTANT
    private val VEHICLE_DOC_KEY = "vehicle_doc_list"
    private val FIRST_NAME_KEY = "first_name_list"
    private val LAST_NAME_KEY = "last_name_list"
    private val VEHICLE_ID_KEY = "vehicle_id_list"
    private val MANUFACTURE_DATE_KEY = "manufacture_date_list"
    private val VEHICLE_DOC_LIST_KEY = "vehicle_doc_list"

    private val VEHICLE_BRAND_KEY = "vehicle_brand_list"
    private val VEHICLE_MODEL_KEY = "vehicle_model_list"
    private val VEHICLE_TYPE_KEY = "vehicle_type_list"
    private val VEHICLE_PLATE_KEY = "vehicle_plate_list"
    private val VEHICLE_COLOR_KEY = "vehicle_color_list"
    private val VEHICLE_CAPACITY_KEY = "vehicle_capacity_list"
    private val VEHICLE_DETAILS_LIST_KEY = "vehicle_details_list"

    private val VEHICLE_REGIS_CERT = "vehicle_regis_cert_list"
    private val VEHICLE_INSURANCE = "vehicle_insurance_list"
    private val VEHICLE_ROADTAX = "vehicle_roadtax_list"
    private val VEHICLE_DOC_IMAGE_LIST_KEY = "vehicle_doc_img_list"
    private val VEHILE_PHOTOS_KEY = "vehicle_photos_list"


    // VEHICLE DOC
    val currentVehicleDoc: LiveData<VehicleDoc> = savedStateHandle.getLiveData(VEHICLE_DOC_KEY)
    val firstName: LiveData<String> = savedStateHandle.getLiveData(FIRST_NAME_KEY)
    val lastName: LiveData<String> = savedStateHandle.getLiveData(LAST_NAME_KEY)
    val vehicleId: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ID_KEY)
    val manufactureDate: LiveData<Timestamp> = savedStateHandle.getLiveData(MANUFACTURE_DATE_KEY)
    val vehicleCert: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_REGIS_CERT)
    val vehicleInsurance: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_INSURANCE)
    val vehicleRoadtax: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ROADTAX)


    // VEHICLE
    val brand: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_BRAND_KEY)
    val model: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_MODEL_KEY)
    val plate: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_PLATE_KEY)
    val color: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_COLOR_KEY)
    val capacity: LiveData<Int> = savedStateHandle.getLiveData(VEHICLE_CAPACITY_KEY)
    val type: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY)
    val photos: LiveData<MutableList<Uri>> = savedStateHandle.getLiveData(VEHILE_PHOTOS_KEY)


    // LiveData for holding URIs of vehicle front and back images
    private val _vehicleFrontImageUri = MutableLiveData<Uri?>()
    val vehicleFrontImageUri: LiveData<Uri?>
        get() = _vehicleFrontImageUri

    private val _vehicleBackImageUri = MutableLiveData<Uri?>()
    val vehicleBackImageUri: LiveData<Uri?>
        get() = _vehicleBackImageUri


    // LiveData for holding URIs of vehicle document image
    private val _regisCertUri = MutableLiveData<Uri?>()
    val regisCertUri: LiveData<Uri?>
        get() = _regisCertUri

    private val _insuranceUri = MutableLiveData<Uri?>()
    val insuranceUri: LiveData<Uri?>
        get() = _insuranceUri

    private val _roadtaxUri = MutableLiveData<Uri?>()
    val roadtaxUri: LiveData<Uri?>
        get() = _roadtaxUri

    val vehicleDocImgList: LiveData<MutableList<VehicleDoc>> =
        savedStateHandle.getLiveData(VEHICLE_DOC_IMAGE_LIST_KEY)

    // SETTER in SavedStateHandle
    fun setVehicleDoc(newVehicleDoc: VehicleDoc) {
        savedStateHandle[VEHICLE_DOC_KEY] = newVehicleDoc
    }

    fun setFirstName(newFirstName: String) {
        savedStateHandle[FIRST_NAME_KEY] = newFirstName
    }

    fun setLastName(newLastName: String) {
        savedStateHandle[LAST_NAME_KEY] = newLastName
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

    fun setManufactureDate(newManufactureDate: Timestamp) {
        savedStateHandle[MANUFACTURE_DATE_KEY] = newManufactureDate
    }

    fun setVehicleList(newVehicleList: MutableList<VehicleDoc>) {
        savedStateHandle[VEHICLE_DOC_LIST_KEY] = newVehicleList
    }


    // SETTER to set the front image URI
    fun setVehicleFrontImageUri(uri: Uri?) {
        _vehicleFrontImageUri.value = uri
    }

    // Function to set the back image URI
    fun setVehicleBackImageUri(uri: Uri?) {
        _vehicleBackImageUri.value = uri
    }


    //SETTER to set the vehicle document image
    fun setRegisterCertImageUri(uri: Uri?) {
        _regisCertUri.value = uri
    }

    // Function to set the insurance image URI
    fun setInsuranceImageUri(uri: Uri?) {
        _insuranceUri.value = uri
    }

    // Function to set the roadtax image URI
    fun setRoadtaxImageUri(uri: Uri?) {
        _roadtaxUri.value = uri
    }


    // INTERNAL DATA MEMBERS
    // vehicle doc
    val vehicleDocList: LiveData<MutableList<VehicleDoc>> =
        savedStateHandle.getLiveData(VEHICLE_DOC_LIST_KEY)

    val vehicleDetailsList: LiveData<MutableList<Vehicle>> =
        savedStateHandle.getLiveData(VEHICLE_DETAILS_LIST_KEY)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            val vehicleDocs = repository.getAllVehicles().toMutableList()
            val vehicleDocUriList = repository.getAllVehicles()
            val licenseMap = repository.getVehicleImage()
            val vehicleDocImgMap = repository.getVehicleDocImg()



            if (vehicleDocUriList != null) {

            }

            setVehicleList(vehicleDocs)
            setVehicleFrontImageUri(licenseMap["vehicleFrontImageUri"] ?: Uri.EMPTY)
            setVehicleBackImageUri(licenseMap["vehicleBackImageUri"] ?: Uri.EMPTY)

            setRegisterCertImageUri(vehicleDocImgMap["vehicleRegisCertUri"] ?: Uri.EMPTY)
            setInsuranceImageUri(vehicleDocImgMap["insuranceUri"] ?: Uri.EMPTY)
            setRoadtaxImageUri(vehicleDocImgMap["roadtaxUri"] ?: Uri.EMPTY)

        }

        repository.listenForVehicleDocChanges { vehicles, exception ->
            if (exception != null) {
                // Handle error
                return@listenForVehicleDocChanges
            }


            val vehicleDocs = vehicles?.filter {
                it.userUid == currentUser?.uid
            }?.toMutableList()

            setVehicleList(vehicleDocs.orEmpty().toMutableList())
        }
    }


    suspend fun saveVehicleDocumentation(): Int {
        val newVehicleDoc = VehicleDoc(
            userUid = currentUser?.uid,
            firstName = firstName.value,
            lastName = lastName.value,
            manufactureDate = manufactureDate.value,
            vehicleId = vehicleId.value
        )
        // save Vehicle
        // Handle result and provide feedback to the fragment
        return repository.updateVehicleDoc(newVehicleDoc)
    }

    suspend fun addVehicle(): FirebaseResponse<String> {
        val photos = if(vehicleFrontImageUri.value != null && vehicleBackImageUri.value != null) mutableListOf(vehicleFrontImageUri.value!!, vehicleBackImageUri.value!!)
        else null
        return repository.addVehicle(
            Vehicle(
                brand = brand.value,
                model = model.value,
                type = type.value ?: VehicleType.Sedan,
                plateNumber = plate.value,
                color = color.value,
                photos = photos,
                capacity = capacity.value ?: 0
            )
        )
    }

    // Vehicle
    suspend fun addVehicleDoc(newVehicleId:String?): Int {
        return if (currentUser?.uid != null) {
            val newVehicleDoc = VehicleDoc(
                userUid = currentUser.uid,
                firstName = firstName.value,
                lastName = lastName.value,
                manufactureDate = manufactureDate.value,
                vehicleId = newVehicleId ?: vehicleId.value,     // vehicleId is null
                vehicleRegisCert = vehicleCert.value,
                insurance = vehicleInsurance.value,
                roadtax = vehicleRoadtax.value
            )

            vehicleDocList.value?.add(newVehicleDoc)
            repository.addVehicleDoc(newVehicleDoc)
        } else {
            Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
    }

//        suspend fun addVehicleDetails(): Int {
//                return if (currentUser?.uid != null) {
//                        val newVehicleDetails = Vehicle(
//                                vehicleID = vehicleId.value,    // vehicleId is null
//                                brand = brand.value,
//                                model = model.value,
//                                plateNumber = plate.value,
//                                color = color.value,
//                                capacity = capacity.value?:0
//                        )
//
//                        vehicleDetailsList.value?.add(newVehicleDetails)
//                        repository.addVehicle(newVehicleDetails)
//                } else {
//                        Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
//                }
//        }
}