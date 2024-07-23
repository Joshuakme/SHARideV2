package com.example.sharide.viewmodel

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.repository.CurrentUserRepository
import com.example.sharide.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel() {
    // Initialize repositories for handling display name, mobile, and gender data
    private val currentUserRepository = CurrentUserRepository()

    // DATA
    //Mutable Live Data
    private val _selectedImageUri = MutableLiveData<Uri>()
    private val _displayName = MutableLiveData<String>()
    private val _mobile = MutableLiveData<String>()
    private val _gender = MutableLiveData<String>()


    // Get the current user from FirebaseAuth
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    private lateinit var profilePicImageView: ImageView


    // LiveData representing the selected image URI
    val selectedImageUri: LiveData<Uri>
        get() = _selectedImageUri

    // LiveData representing the user's display name
    val displayName: LiveData<String>
        get() = _displayName

    // LiveData representing the user's mobile number
    val mobile: LiveData<String>
        get() = _mobile

    // LiveData representing the user's gender
    val gender: LiveData<String>
        get() = _gender


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val profilePicUri = currentUserRepository.getProfilePic()

            if(profilePicUri != null){
                setSelectedImageUri(profilePicUri)
            }
        }
    }


    // Function to set the selected image URI
    fun setSelectedImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

        // Function to update the profile picture URI in Firestore
        fun updateProfilePictureUri(uri: Uri) {
            currentUser?.uid?.let {
                viewModelScope.launch {
                    try {
                        currentUserRepository.updateProfilePicture(uri)
                    } catch (e: Exception) {
                        // Handle the exception
                        Log.e(
                            "PersonalInfoViewModel",
                            "Error updating profile picture URI: ${e.message}",
                            e
                        )
                    }
                }
            }
        }




        //Fetches the display name of the current user from the database and updates the LiveData
        fun fetchDisplayNameFromDatabase() {
            // Check if the current user is authenticated
            currentUser?.uid?.let { userId ->
                // Launch a coroutine to perform the database operation
                viewModelScope.launch {
                    // Fetch the display name from the repository
                    val displayName = currentUserRepository.getDisplayName(userId)
                    // Update the LiveData with the fetched display name
                    _displayName.value = displayName ?: ""
                }
            }
        }

    //Checks if the provided display name is valid
    fun isDisplayNameValid(displayName: String): Boolean {
        // Define the regex pattern for valid display names
        val regex = "^[a-zA-Z0-9_\\-\\.\\s]{2,25}$".toRegex()

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

    // Updates the mobile phone of the user in the repository

        //Fetches the mobile phone of the current user from the database and updates the LiveData
        fun fetchMobileFromDatabase() {
            // Check if the current user is authenticated
            currentUser?.uid?.let { userId ->
                // Launch a coroutine to perform the database operation
                viewModelScope.launch {
                    // Fetch the mobile phone from the repository
                    val mobile = currentUserRepository.getMobile(userId)
                    // Update the LiveData with the fetched mobile phone
                    _mobile.value = if(mobile != null) "+60$mobile" else ""
                }
            }
        }



        //Fetches the gender of the current user from the database and updates the LiveData
        fun fetchGenderFromDatabase() {
            // Launch a coroutine to perform the database operation
            viewModelScope.launch {
                // Fetch the gender from the repository
                val gender = currentUserRepository.getGender()
                // Update the LiveData with the fetched gender
                _gender.value = gender ?: ""
            }
        }
    }

