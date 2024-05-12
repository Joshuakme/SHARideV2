package com.example.sharidev2.viewmodel

import android.net.Uri
import android.util.Log
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
import java.util.Date


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
    private val VEHICLE_LIST_KEY = "vehicle_list"

    private val VEHICLE_BRAND_KEY = "vehicle_brand_list"
    private val VEHICLE_MODEL_KEY = "vehicle_model_list"
    private val VEHICLE_TYPE_KEY = "vehicle_type_list"
    private val VEHICLE_PLATE_KEY = "vehicle_plate_list"
    private val VEHICLE_COLOR_KEY = "vehicle_color_list"
    private val VEHICLE_CAPACITY_KEY = "vehicle_capacity_list"

    private val VEHICLE_REGIS_CERT = "vehicle_regis_cert_list"
    private val VEHICLE_INSURANCE = "vehicle_insurance_list"
    private val VEHICLE_ROADTAX = "vehicle_roadtax_list"
    private val VEHICLE_DOC_IMAGE_LIST_KEY = "vehicle_doc_img_list"
    private val VEHILE_PHOTOS_KEY = "vehicle_photos_list"

    private val VEHICLE_FRONT_IMAGE = "vehicle_front_image"
    private val VEHICLE_BACK_IMAGE = "vehicle_back_image"


    // VEHICLE DOC
    val currentVehicleDoc: LiveData<VehicleDoc> = savedStateHandle.getLiveData(VEHICLE_DOC_KEY)
    val firstName: LiveData<String> = savedStateHandle.getLiveData(FIRST_NAME_KEY, "")
    val lastName: LiveData<String> = savedStateHandle.getLiveData(LAST_NAME_KEY, "")
    val vehicleId: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ID_KEY, "")
    val manufactureDate: LiveData<Timestamp> = savedStateHandle.getLiveData(MANUFACTURE_DATE_KEY,
        Timestamp(Date(2023,2,14)
    ))


    // VEHICLE
    val brand: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_BRAND_KEY, "")
    val model: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_MODEL_KEY, "")
    val plate: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_PLATE_KEY, "")
    val color: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_COLOR_KEY, "")
    val capacity: LiveData<Int> = savedStateHandle.getLiveData(VEHICLE_CAPACITY_KEY, 0)
    val type: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY, VehicleType.Sedan)
    val photos: LiveData<MutableList<Uri>> = savedStateHandle.getLiveData(VEHILE_PHOTOS_KEY, emptyList<Uri>().toMutableList())

    val frontPhoto: LiveData<Uri> = savedStateHandle.getLiveData(VEHICLE_FRONT_IMAGE, Uri.parse(""))
    val backPhoto: LiveData<Uri> = savedStateHandle.getLiveData(VEHICLE_BACK_IMAGE, Uri.parse(""))



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

    fun setManufactureDate(newManufactureDate: Timestamp) {
        savedStateHandle[MANUFACTURE_DATE_KEY] = newManufactureDate
    }

    fun setCapacity(newCapacity: Int){
        savedStateHandle[VEHICLE_CAPACITY_KEY] = newCapacity
    }

    fun setPlateNumber(newPlateNumber: String){
        savedStateHandle[VEHICLE_PLATE_KEY] = newPlateNumber
    }

    fun setVehicleBrand(newVehicleBrand: String){
        savedStateHandle[VEHICLE_BRAND_KEY] = newVehicleBrand
    }

    fun setVehicleModel(newVehicleModel: String){
        savedStateHandle[VEHICLE_MODEL_KEY] = newVehicleModel
    }

    fun setVehicleColor(newVehicleColor: String){
        savedStateHandle[VEHICLE_COLOR_KEY] = newVehicleColor
    }

    fun setVehicleList(newVehicleList: MutableList<Vehicle>) {
        savedStateHandle[VEHICLE_LIST_KEY] = newVehicleList
    }

    fun setVehicleDocList(newVehicleDocList: MutableList<VehicleDoc>) {
        savedStateHandle[VEHICLE_DOC_LIST_KEY] = newVehicleDocList
    }

    fun setVehicleType(newVehicleType: VehicleType){
        savedStateHandle[VEHICLE_TYPE_KEY] = newVehicleType
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

    fun setPhotos(uriList: MutableList<Uri>){
        savedStateHandle[VEHILE_PHOTOS_KEY] = uriList
    }


    //SETTER for vehicle photos
    fun setFrontPhoto(uri: Uri?){
        savedStateHandle[VEHICLE_FRONT_IMAGE] = uri
    }

    fun setBackPhoto(uri: Uri?){
        savedStateHandle[VEHICLE_BACK_IMAGE] = uri
    }


    //Function to add vehicle photos
    fun addImageToVehiclePhotos(uri: Uri, position: Int?){
        val newList = mutableListOf<Uri>()
        photos.value!!.forEach {
            newList.add(it)
        }

        if(position != null && position >= 0 && position <= 1) {
            newList.add(position, uri)
        }else{
            newList.add(uri)
        }
        Log.e("Error Hereeeeeee", photos.value!!.size.toString())

        setPhotos(newList)
    }


    // INTERNAL DATA MEMBERS
    // vehicle doc
    val vehicleDocList: LiveData<MutableList<VehicleDoc>> =
        savedStateHandle.getLiveData(VEHICLE_DOC_LIST_KEY)

    val vehicleList: LiveData<MutableList<Vehicle>> =
        savedStateHandle.getLiveData(VEHICLE_LIST_KEY)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            val vehicleDocList = repository.getAllVehicleDoc().toMutableList()
            val vehicleList = repository.getVehicleList().toMutableList()

            setVehicleDocList(vehicleDocList)
            setVehicleList(vehicleList)
        }

        repository.listenForVehicleDocChanges { vehicles, exception ->
            if (exception != null) {
                // Handle error
                return@listenForVehicleDocChanges
            }

            val vehicleDocsList = vehicles?.toMutableList()

            setVehicleDocList(vehicleDocsList.orEmpty().toMutableList())
        }

        repository.listenForVehicleChanges { vehicles, exception ->
            if (exception != null) {
                // Handle error
                return@listenForVehicleChanges
            }

            val vehicleList = vehicles?.toMutableList()

            setVehicleList(vehicleList.orEmpty().toMutableList())
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
        return repository.addVehicle(
            Vehicle(
                brand = brand.value,
                model = model.value,
                type = type.value ?: VehicleType.Sedan,
                plateNumber = plate.value,
                color = color.value,
                photos = mutableListOf(frontPhoto.value!!, backPhoto.value!!),
                capacity = capacity.value ?: 0
            )
        )
    }

    // Vehicle
    suspend fun addVehicleDoc(newVehicleId:String): Int {
        Log.e("vehicleId", "VehicleId: " + newVehicleId)

        return if (currentUser?.uid != null) {
            val newVehicleDoc = VehicleDoc(
                userUid = currentUser.uid,
                firstName = firstName.value,
                lastName = lastName.value,
                manufactureDate = manufactureDate.value,
                vehicleId = newVehicleId,     // vehicleId is null
                vehicleRegisCert = _regisCertUri.value.toString(),
                insurance = _insuranceUri.value.toString(),
                roadtax = _roadtaxUri.value.toString()
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