package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehicleDocImgViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val repository = VehicleDocRepository()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    private val _regisCertUri = MutableLiveData<Uri?>()

    // DATA KEY CONSTANT
    private val VEHICLE_CERT_KEY = "vehicle_cert_list"
    private val VEHICLE_INSURANCE_KEY = "vehicle_insurance_list"
    private val VEHICLE_ROADTAX_KEY = "vehicle_roadtax_list"
    private val VEHICLE_ID_KEY = "vehicle_id_list"
    private val VEHICLE_DOC_IMAGE_LIST_KEY = "vehicle_doc_img_list"


    val vehicleCert: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_CERT_KEY)
    val vehicleInsurance: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_INSURANCE_KEY)
    val vehicleRoadtax: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ROADTAX_KEY)


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

    init {
        viewModelScope.launch(Dispatchers.Main) {
            val vehicleDocImgMap = repository.getVehicleDocImg()

            setRegisterCertImageUri(vehicleDocImgMap["vehicleRegisCertUri"]?: Uri.EMPTY)
            setInsuranceImageUri(vehicleDocImgMap["insuranceUri"]?: Uri.EMPTY)
            setRoadtaxImageUri(vehicleDocImgMap["roadtaxUri"]?: Uri.EMPTY)
        }
    }

    // Function to set the registration certificate image URI
    fun setRegisterCertImageUri(uri: Uri?) {
        _regisCertUri.value = uri
    }

    // Function to set the insurance image URI
    fun setInsuranceImageUri(uri: Uri?){
        _insuranceUri.value = uri
    }

    // Function to set the roadtax image URI
    fun setRoadtaxImageUri(uri: Uri?) {
        _roadtaxUri.value = uri
    }

    suspend fun addImagesToDB(): Int {
        return repository.addVehicleDocImg(regisCertUri.value, insuranceUri.value, roadtaxUri.value)
    }

    suspend fun updateVehicleDocImage(): Int {
        if (currentUser?.uid != null) {
            val newVehicleDocImage = VehicleDoc(
                vehicleRegisCert = vehicleCert.value,
                insurance = vehicleInsurance.value,
                roadtax = vehicleRoadtax.value
            )

            vehicleDocImgList.value?.add(newVehicleDocImage)
            return repository.addVehicleDoc(newVehicleDocImage)
        } else {
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
    }

}