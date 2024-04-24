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
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VehicleDocViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
        private val repository = VehicleDocRepository()

        private val currentUser = FirebaseClient.firebaseAuth.currentUser


        // DATA KEY CONSTANT
        private val VEHICLE_DOC_KEY = "vehicle_doc_list"
        private val FIRST_NAME_KEY = "first_name_list"
        private val LAST_NAME_KEY = "last_name_list"
        private val VEHICLE_ID_KEY = "vehicle_id_list"
        private val MANUFACTURE_DATE_KEY = "manufacture_date_list"
        private val VEHICLE_LIST_KEY = "vehicle_list"


        val currentVehicleDoc: LiveData<VehicleDoc> = savedStateHandle.getLiveData(VEHICLE_DOC_KEY)
        val firstName: LiveData<String> = savedStateHandle.getLiveData(FIRST_NAME_KEY)
        val lastName: LiveData<String> = savedStateHandle.getLiveData(LAST_NAME_KEY)
        val vehicleId: LiveData<String> = savedStateHandle.getLiveData(VEHICLE_ID_KEY)
        val manufactureDate: LiveData<Timestamp> = savedStateHandle.getLiveData(MANUFACTURE_DATE_KEY)


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
                savedStateHandle[VEHICLE_LIST_KEY] = newVehicleList
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


        // Vehicle
        suspend fun addVehicleDoc(): Int {
                if (currentUser?.uid != null) {
                        val newVehicleDoc = VehicleDoc(
                                userUid = currentUser.uid,
                                firstName = firstName.value,
                                lastName = lastName.value,
                                manufactureDate = manufactureDate.value,
                                vehicleId = vehicleId.value
                        )

                        vehicleDocList.value?.add(newVehicleDoc)
                        return repository.addVehicle(newVehicleDoc)
                } else {
                        return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
                }
        }
}