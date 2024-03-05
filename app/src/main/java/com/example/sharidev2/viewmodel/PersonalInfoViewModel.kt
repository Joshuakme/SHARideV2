package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.repository.DisplayNameRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel() {    // LiveData for current display name
    private val _displayName = MutableLiveData<String>()
    private val currentUser = FirebaseInitializer.firebaseAuth.currentUser
    private val repository = DisplayNameRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)

    val displayName: LiveData<String>
        get() = _displayName

    // Function to update the display name
    suspend fun updateDisplayName(newDisplayName: String) {
        repository.updateDisplayName(newDisplayName)
    }


    fun fetchDisplayNameFromDatabase() {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                val displayName = repository.fetchDisplayName(userId)
                _displayName.value = displayName
            }
        }
    }
}
