package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.repository.DisplayNameRepository
import com.example.sharidev2.data.repository.EditMobileRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel() {    // LiveData for current display name
    private val _displayName = MutableLiveData<String>()
    private val _mobile= MutableLiveData<String>()
    private val currentUser = FirebaseInitializer.firebaseAuth.currentUser
    private val displayNameRepository = DisplayNameRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)
    private val mobileRepository = EditMobileRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)


    val displayName: LiveData<String>
        get() = _displayName

    val mobile: LiveData<String>
        get() = _mobile

    // Function to update the display name
    suspend fun updateDisplayName(newDisplayName: String) {
        displayNameRepository.updateDisplayName(newDisplayName)
    }


    fun fetchDisplayNameFromDatabase() {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                val displayName = displayNameRepository.fetchDisplayName(userId)
                _displayName.value = displayName
            }
        }
    }


    // Function to update the user mobile phone number
    suspend fun updateMobile(newMobile: String) {
        mobileRepository.updateMobile(newMobile)
    }

    fun fetchMobileFromDatabase() {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                val mobile = mobileRepository.fetchMobile(userId)
                _mobile.value = mobile
            }
        }
    }
}
