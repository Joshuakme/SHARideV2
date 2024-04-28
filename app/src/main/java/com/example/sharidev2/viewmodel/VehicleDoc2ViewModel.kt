package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.repository.VehicleDocRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VehicleDoc2ViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val repository = VehicleDocRepository()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    // DATA KEY CONSTANT
    private val VEHICLE_DOC_KEY = "vehicle_doc_list"
    private val VEHICLE_ID_KEY = "vehicle_id_list"
    private val REGISTER_CERT_KEY = "register_cert_list"
    private val ROADTAX_KEY = "roadtax_list"
    private val INSURANCE_KEY = "insurance_list"
    private val VEHICLE_LIST_KEY = "vehicle_list"


    val currentVehicleDoc: LiveData<VehicleDoc> = savedStateHandle.getLiveData(VEHICLE_DOC_KEY)
    val vehicleId: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ID_KEY)
    val vehicleRegisCertUri: LiveData<String> = savedStateHandle.getLiveData(REGISTER_CERT_KEY)
    val roadtaxUri: LiveData<String> = savedStateHandle.getLiveData(REGISTER_CERT_KEY)
    val insuranceUri: LiveData<String> = savedStateHandle.getLiveData(REGISTER_CERT_KEY)


    // INTERNAL DATA MEMBERS
    // vehicle doc
    val vehicleDocList: LiveData<MutableList<VehicleDoc>> =
        savedStateHandle.getLiveData(VEHICLE_LIST_KEY)


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val vehicleDocs = repository.getAllVehicles().toMutableList()
            val vehicleDocUriList = repository.getAllVehicles()

            if (vehicleDocUriList != null) {

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
    fun setVehicleDoc(newVehicleDoc: VehicleDoc) {
        savedStateHandle[VEHICLE_DOC_KEY] = newVehicleDoc
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

    // Function to set the vehicle registration cert URI
    fun setVehicleRegisCertUri(newRegisterCert: Uri) {
        savedStateHandle[REGISTER_CERT_KEY] = newRegisterCert
    }


    fun setRoadTaxUri(newRoadTaxUri: Uri) {
        savedStateHandle[ROADTAX_KEY] = newRoadTaxUri
    }


    fun setInsuranceUri(newInsuranceUri: Uri) {
        savedStateHandle[INSURANCE_KEY] = newInsuranceUri
    }


    fun setVehicleList(newVehicleList: MutableList<VehicleDoc>) {
        savedStateHandle[VEHICLE_LIST_KEY] = newVehicleList
    }


    suspend fun saveVehicleDocumentation(): Int {
        val newVehicleDoc = VehicleDoc(
            userUid = currentUser?.uid,
            vehicleId = vehicleId.value,
            vehicleRegisCert = vehicleRegisCertUri.value,
            roadtax = roadtaxUri.value,
            insurance = insuranceUri.value
        )
        // save Vehicle
        // Handle result and provide feedback to the fragment
        return repository.updateVehicleDoc(newVehicleDoc)
    }


    // Vehicle
    suspend fun addVehicleDoc(): Int {
        if (currentUser?.uid != null) {
            val newVehicleDoc = VehicleDoc(
                userUid = currentUser.uid,
                vehicleId = vehicleId.value,
                vehicleRegisCert = vehicleRegisCertUri.value,
                roadtax = roadtaxUri.value,
                insurance = insuranceUri.value
            )

            vehicleDocList.value?.add(newVehicleDoc)
            return repository.addVehicle(newVehicleDoc)
        } else {
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
    }
}