import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Contact
import com.example.sharide.data.repository.EmergencyContactRepository
import com.example.sharide.utility.Constants
import com.example.sharide.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EmergencyContactViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repository = EmergencyContactRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    // DATA KEY CONSTANT
    private val CONTACT_LIST_KEY = "contact_list"



    // INTERNAL DATA MEMBERS
    // emergency contact
    private val _emergencyContactList: MutableLiveData<MutableList<Contact>> = savedStateHandle.getLiveData(CONTACT_LIST_KEY, mutableListOf())

    val emergencyContactList: LiveData<MutableList<Contact>> = _emergencyContactList


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val allContacts = repository.getAllContacts()

            if(allContacts != null) {
                setContactList(allContacts.toMutableList())
            } else {
                setContactList(mutableListOf())
            }
        }

        repository.listenForContactChanges { contacts, exception ->
            if (exception != null) {
                // Handle error
                return@listenForContactChanges
            }


            val emergencyContacts = contacts?.filter {
                it.userUid == currentUser?.uid
            }?.toMutableList()

            setContactList(emergencyContacts.orEmpty().toMutableList())
        }
    }

    // SETTER in SavedStateHandle
    // Contact
    suspend fun addContact(newContact: Contact): Int {
        Log.e("EmergencyContactViewModel", emergencyContactList.value?.size.toString())
        emergencyContactList.value!!.add(newContact)

        return try {
            repository.addContact(newContact)
        } catch (e: Exception) {
            Log.e("Add Contact", e.message.toString())
            Constants.FIREBASE_REQUEST_FAILED // Return failure code
        }
    }

    fun setContactList(newContactList: MutableList<Contact>) {
        if (emergencyContactList.isInitialized) {
            savedStateHandle[CONTACT_LIST_KEY] = newContactList
        } else {

        }
    }

    suspend fun updateContact(newContact: Contact): Int {
        return repository.updateContact(newContact)
    }

    suspend fun deleteContact(contactId: String): Int {
        return repository.deleteContact(contactId)
    }
}