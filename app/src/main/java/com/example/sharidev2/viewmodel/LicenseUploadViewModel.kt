package com.example.sharidev2.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.repository.DriverVehicleRepository
import com.example.sharidev2.data.repository.DrivingLicenseRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LicenseUploadViewModel : ViewModel() {
    private val repository = DrivingLicenseRepository(
        FirebaseInitializer.firestore,
        FirebaseInitializer.firebaseAuth,
        FirebaseInitializer.firebaseStorage
    )

    private val _frontImageUri = MutableLiveData<Uri?>()
    val frontImageUri: LiveData<Uri?>
        get() = _frontImageUri

    private val _backImageUri = MutableLiveData<Uri?>()
    val backImageUri: LiveData<Uri?>
        get() = _backImageUri


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val licenseMap = repository.getDrivingLicense()

            if(licenseMap != null) {
                setFrontImageUri(licenseMap["frontImgUri"]?: Uri.EMPTY)
                setBackImageUri(licenseMap["backImgUri"]?: Uri.EMPTY)
            }
        }
    }


    // Function to set the front image URI
    fun setFrontImageUri(uri: Uri?) {
        _frontImageUri.value = uri
    }

    // Function to set the back image URI
    fun setBackImageUri(uri: Uri?) {
        _backImageUri.value = uri
    }

    suspend fun addImagesToDB(): Int {
        return repository.addDriverLicense(frontImageUri.value, backImageUri.value)
    }
}
