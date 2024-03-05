package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.repository.DisplayNameRepository
import com.example.sharidev2.data.repository.EditMobileRepository
import com.example.sharidev2.data.repository.GenderRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel() {    // LiveData for current display name
    private val _displayName = MutableLiveData<String>()
    private val _mobile= MutableLiveData<String>()
    private val _gender = MutableLiveData<String>()
    private val currentUser = FirebaseInitializer.firebaseAuth.currentUser
    private val displayNameRepository = DisplayNameRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)
    private val mobileRepository = EditMobileRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)
    private val genderRepository = GenderRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)


    val displayName: LiveData<String>
        get() = _displayName

    val mobile: LiveData<String>
        get() = _mobile

    val gender: LiveData<String>
        get() = _gender

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

    fun isDisplayNameValid(displayName: String): Boolean {
        // Define the regex pattern for valid display names
        val regex = "^[a-zA-Z0-9_\\-\\.]{2,24}$".toRegex()

        // Check if the display name matches the pattern and does not contain invalid characters
        return regex.matches(displayName) && !displayName.contains("!") &&
                !displayName.contains("@") && !displayName.contains("#") &&
                !displayName.contains("$") && !displayName.contains("%") &&
                !displayName.contains("^") && !displayName.contains("&") &&
                !displayName.contains("*") && !displayName.contains("(") &&
                !displayName.contains(")") && !displayName.contains("-") &&
                !displayName.contains("_") && !displayName.contains("=") &&
                !displayName.contains("+") && !displayName.contains("/") &&
                !displayName.contains("<") && !displayName.contains(">") &&
                !displayName.contains("?") && !displayName.contains("`") &&
                !displayName.contains("~")
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


    // Function to update the user gender
    suspend fun updateGender(newGender: String) {
        genderRepository.updateGender(newGender)
    }

    fun fetchGenderFromDatabase() {
        viewModelScope.launch {
            val gender = genderRepository.fetchGender()
            _gender.value = gender
        }
    }
}
