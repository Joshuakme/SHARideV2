package com.example.sharidev2.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.repository.CurrentUserRepository
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class SharedCurrentUserViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val repository = CurrentUserRepository()

    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    // DATA KEY CONSTANT
    private val USER_KEY = "user"
    private val USER_UID_KEY = "user_uid"
    private val DISPLAY_NAME_KEY = "display_name"
    private val EMAIL_KEY = "email"
    private val PHONE_NUMBER_KEY = "phone_number"
    private val IMAGE_URI_KEY = "image_uri"
    private val RIDE_OPTION_KEY = "ride_option"
//    private val RATING_KEY = "rating"
//    private val SAVED_ADDRESSES_KEY = "saved_addresses"
    private val GENDER_KEY = "gender"
    private val FCM_TOKEN_KEY = "fcm_token"
    private val JOINED_DATE_KEY = "joined_date"

    private val SIGN_OUT_RESULT_KEY = "sign_out_result"


    // INTERNAL DATA MEMBERS
    // FCM Token
    val user: LiveData<User?> = savedStateHandle.getLiveData(USER_KEY)
    val userUid: LiveData<String?> = savedStateHandle.getLiveData(USER_UID_KEY)
    val displayName: LiveData<String?> = savedStateHandle.getLiveData(DISPLAY_NAME_KEY)
    val email: LiveData<String?> = savedStateHandle.getLiveData(EMAIL_KEY)
    val phoneNumber: LiveData<String?> = savedStateHandle.getLiveData(PHONE_NUMBER_KEY)
    val imageUri: LiveData<Uri?> = savedStateHandle.getLiveData(IMAGE_URI_KEY)
    val rideOption: LiveData<RideOption?> = savedStateHandle.getLiveData(RIDE_OPTION_KEY)
    val gender: LiveData<Gender?> = savedStateHandle.getLiveData(GENDER_KEY)
    val fcmToken: LiveData<String?> = savedStateHandle.getLiveData(FCM_TOKEN_KEY)
    val joinedDate: LiveData<Timestamp?> = savedStateHandle.getLiveData(JOINED_DATE_KEY)

    // STATUS
    val signOutResult: LiveData<Boolean> = savedStateHandle.getLiveData(SIGN_OUT_RESULT_KEY)


    init {
        if (!user.isInitialized && currentUser != null) {
            currentUser.displayName?.let { setDisplayName(it) }
            currentUser.photoUrl?.let { setImageUri(it) }

            viewModelScope.launch {
                val user = repository.getCurrentUser()
                user?.let { setUser(it) }
            }
        }
    }



    // SETTER in SavedStateHandle
    // User
    private fun setUser(newUser: User) {
        savedStateHandle[USER_KEY] = newUser

        viewModelScope.launch {
            val user = repository.getCurrentUser()

            Log.e("Current User ViewModel", "user is null: ${user == null}")

            if(user != null) {
                user.uid?.let { setUserUid(it) }
                user.displayName?.let { setDisplayName(it) }
                user.email?.let { setEmail(it) }
                user.phoneNumber?.let { setPhoneNumber(it) }
                user.photoUrl?.let { setImageUri(it) }
                user.rideOption?.let { setRideOption(it) }
                user.gender?.let { setGender(it) }
                user.fcmToken?.let { setFcmToken(it) }
                user.joinedDate?.let { setJoinedDate(it) }
            }
        }
    }

    // User Uid
    private fun setUserUid(newUserUid: String) {
        savedStateHandle[USER_UID_KEY] = newUserUid
    }

    // Display Name
    fun setDisplayName(newDisplayName: String) {
        savedStateHandle[DISPLAY_NAME_KEY] = newDisplayName
    }

    // Email
    fun setEmail(newEmail: String) {
        savedStateHandle[EMAIL_KEY] = newEmail
    }

    // Phone Number
    fun setPhoneNumber(newPhoneNumber: String) {
        savedStateHandle[PHONE_NUMBER_KEY] = newPhoneNumber
    }

    // Image Uri
    fun setImageUri(newImageUri: Uri) {
        savedStateHandle[IMAGE_URI_KEY] = newImageUri
    }

    // Ride Option
    fun setRideOption(newRideOption: RideOption) {
        savedStateHandle[RIDE_OPTION_KEY] = newRideOption
    }

    // Gender
    fun setGender(newGender: String) {
        savedStateHandle[RIDE_OPTION_KEY] = Gender.valueOf(newGender)
    }

    fun setGender(newGender: Gender) {
        savedStateHandle[RIDE_OPTION_KEY] = newGender
    }

    // FCM Token
    fun setFcmToken(newFcmToken: String) {
        savedStateHandle[FCM_TOKEN_KEY] = newFcmToken

        viewModelScope.launch {
            val respond = repository.saveFcmToken(newFcmToken)

            when(respond) {
                Constants.FIREBASE_REQUEST_SUCCESS -> {
                    Log.e("CurrentUserViewModel: setFcmToken()", "Set FCM Token: Success")
                }

                else -> {
                    Log.e("CurrentUserViewModel: setFcmToken()", "Set FCM Token: Failed. Detailed error please look at log below")
                    Log.e("CurrentUserViewModel: setFcmToken()", "Set FCM Token: Error code: $respond")
                }
            }
        }
    }

    // FCM Token
    private fun setJoinedDate(newJoinedDate: Timestamp) {
        savedStateHandle[JOINED_DATE_KEY] = newJoinedDate
    }



    // UPDATES
    // Updates the display name of the user in the repository
    fun updateDisplayName(newDisplayName: String) {
        setDisplayName(newDisplayName)

        viewModelScope.launch {
            repository.updateDisplayName(newDisplayName)
        }
    }

    // Updates the mobile phone of the user in the repository
    suspend fun updateMobile(newMobile: String) {
        setPhoneNumber(newMobile)
        repository.updateMobile(newMobile)
    }

    // Updates the gender of the user in the repository
    suspend fun updateGender(newGender: String) {
        setGender(newGender)
        repository.updateGender(newGender)
    }



    // OTHER
    private fun setSignOutResult(success: Boolean) {
        savedStateHandle[SIGN_OUT_RESULT_KEY] = success
    }

    private fun resetData() {
        savedStateHandle[USER_KEY] = null
        savedStateHandle[DISPLAY_NAME_KEY] = null
        savedStateHandle[EMAIL_KEY] = null
        savedStateHandle[PHONE_NUMBER_KEY] = null
        savedStateHandle[IMAGE_URI_KEY] = null
        savedStateHandle[RIDE_OPTION_KEY] = null
        savedStateHandle[RIDE_OPTION_KEY] = null
        savedStateHandle[FCM_TOKEN_KEY] = null
        savedStateHandle[JOINED_DATE_KEY] = null
        savedStateHandle[SIGN_OUT_RESULT_KEY] = false
    }

//    private fun startListeningForUserUpdate() {
//        if(user.isInitialized && user.value?.uid != null) {
//            repository.listenForUserUpdate(user.value!!.uid!!, object: (User) -> Unit {
//                override fun invoke(newUser: User) {
//                    setUser(newUser)
//
//                    newUser.displayName?.let { setDisplayName(it) }
//                    newUser.email?.let { setEmail(it) }
//                    newUser.phoneNumber?.let { setPhoneNumber(it) }
//                    newUser.photoUri?.let { setImageUri(it) }
//                    newUser.rideOption?.let { setRideOption(it) }
//                    newUser.gender?.let { setGender(it) }
//                    newUser.fcmToken?.let { setFcmToken(it) }
//                    newUser.joinedDate?.let { setJoinedDate(it) }
//                }
//            })
//        }
//    }

    fun isLoggedIn(): Boolean {
        return user.value != null
    }

    suspend fun signIn() {
        val newUser = FirebaseClient.getCurrentUser()

        if(newUser != null)  {
            setUser(newUser)

            setDisplayName(currentUser!!.displayName!!)
            setImageUri(currentUser.photoUrl!!)
        }
    }

    fun signOut() {
        repository.signOut(object: (Boolean) -> Unit {
            override fun invoke(success: Boolean) {
                setSignOutResult(success)
                resetData()
            }
        })
    }
}