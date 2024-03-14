package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VehicleDocViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
        private val repository = VehicleDocRepository(
                FirebaseClient.firestore,
                FirebaseClient.firebaseAuth,
                FirebaseClient.firebaseStorage)

        private val currentUser = FirebaseClient.firebaseAuth.currentUser

        // DATA KEY CONSTANT
        private val VEHICLE_LIST_KEY = "vehicle_list"



        private val _vehicleRegisCertUri = MutableLiveData<Uri?>()
        val vehicleRegisCertUri: LiveData<Uri?>
                get() = _vehicleRegisCertUri

        private val _roadtaxUri = MutableLiveData<Uri?>()
        val roadtaxUri: LiveData<Uri?>
                get() = _roadtaxUri

        private val _insuranceUri = MutableLiveData<Uri?>()
        val insuranceUri: LiveData<Uri?>
                get() = _insuranceUri


        // INTERNAL DATA MEMBERS
        // vehicle doc
        val vehicleDocList: LiveData<MutableList<VehicleDoc>> =
                savedStateHandle.getLiveData(VEHICLE_LIST_KEY)


        init {
                viewModelScope.launch(Dispatchers.Main) {
                        val vehicleDocs = repository.getAllVehicles().toMutableList()
                        val vehicleDocUriList = repository.getAllVehicles()

                        if(vehicleDocUriList !=null){

                        }

                        setVehicleList(vehicleDocs)


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

        // SETTER in SavedStateHandle
        // Vehicle
        suspend fun addVehicle(newVehicle: VehicleDoc,
                               vehicleRegisCertUri: Uri?,
                               roadtaxUri: Uri?,
                               insuranceUri: Uri?): Int {
                vehicleDocList.value?.add(newVehicle)
                return repository.addVehicle(newVehicle, vehicleRegisCertUri, roadtaxUri, insuranceUri)
        }

        // Function to set the vehicle registration cert URI
        fun setVehicleRegisCertUri(uri: Uri?) {
                _vehicleRegisCertUri.value = uri
        }

        // Function to set the roadtax URI
        fun setRoadtaxUri(uri: Uri?) {
                _roadtaxUri.value = uri
        }

        // Function to set the insurance URI
        fun setInsuranceUri(uri: Uri?) {
                _insuranceUri.value = uri
        }

//        suspend fun addImagesToDB(): Int {
//                val vehicleDocListValue = vehicleDocList.value
//                val regisCertUriValue = vehicleRegisCertUri.value
//                val roadtaxUriValue = roadtaxUri.value
//                val insuranceUriValue = insuranceUri.value
//
//                if (vehicleDocListValue != null && regisCertUriValue != null && roadtaxUriValue != null && insuranceUriValue != null) {
//                        return repository.addVehicle(vehicleDocListValue, regisCertUriValue, roadtaxUriValue, insuranceUriValue)
//                } else {
//                        // Handle the case when one or more values are null
//                        return Constants.FIREBASE_REQUEST_EXCEPTION
//                }
//        }

        fun setVehicleList(newVehicleList: MutableList<VehicleDoc>) {
                savedStateHandle.set(VEHICLE_LIST_KEY, newVehicleList)
        }

//        suspend fun updateVehicleDoc(newVehicle: VehicleDoc): Int {
//                return repository.updateVehicleDoc(newVehicle)
//        }
//
//        suspend fun deleteVehicle(vehicleId: String): Int {
//                return repository.deleteVehicle(vehicleId)
//        }
}



//    // DATA KEY CONSTANT
//    private val _firstName = MutableLiveData<String>()
//    private val _lastName = MutableLiveData<String>()
//    private val _vehicleType = MutableLiveData<VehicleType>()
//    private val _vehicleModel = MutableLiveData<String>()
//    private val _carPlate = MutableLiveData<String>()
//    private val _manufactureDate = MutableLiveData<Timestamp>()
//    private val _vehicleRegisCert = MutableLiveData<Uri>()
//    private val _roadtax= MutableLiveData<Uri>()
//    private val _insurance = MutableLiveData<Uri>()
//
//    //Live Data
//    val firstName: LiveData<String>
//        get() = _firstName
//
//    val lastName: LiveData<String>
//        get() = _lastName
//
//    val vehicleType: LiveData<VehicleType>
//        get() = _vehicleType
//
//    val vehicleModel: LiveData<String>
//        get() = _vehicleModel
//
//    val carPlate: LiveData<String>
//        get() = _carPlate
//
//    val vehicleRegisCert: LiveData<Uri>
//        get() = _vehicleRegisCert
//
//    val roadtax: LiveData<Uri>
//        get() = _roadtax
//
//    val insurance: LiveData<Uri>
//        get() = _insurance
//
//
//    init{
//
//    }
//
//
//
//    // Function to set the image URI
//    fun setVehicleRegisCert(uri: Uri) {
//        _vehicleRegisCert.value = uri
//    }
//
//    fun setRoadtax(uri: Uri) {
//        _roadtax.value = uri
//    }
//
//    fun setInsurance(uri: Uri) {
//        _insurance.value = uri
//    }
//
//
//    suspend fun updateFirstName(newDisplayName: String) {
//        displayNameRepository.updateDisplayName(newDisplayName)
//    }
