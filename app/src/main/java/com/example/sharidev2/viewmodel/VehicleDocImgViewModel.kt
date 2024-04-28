package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.repository.VehicleDocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehicleDocImgViewModel : ViewModel() {
    private val repository = VehicleDocRepository()

    private val _regisCertUri = MutableLiveData<Uri?>()
    val regisCertUri: LiveData<Uri?>
        get() = _regisCertUri

    private val _insuranceUri = MutableLiveData<Uri?>()
    val insuranceUri: LiveData<Uri?>
        get() = _insuranceUri

    private val _roadtaxUri = MutableLiveData<Uri?>()
    val roadtaxUri: LiveData<Uri?>
        get() = _roadtaxUri

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
}