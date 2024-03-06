import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.repository.EmergencyContactRepository
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EmergencyContactViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repository = EmergencyContactRepository(FirebaseClient.firestore, FirebaseClient.firebaseAuth)
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    // DATA KEY CONSTANT
    private val CONTACT_LIST_KEY = "contact_list"


    // INTERNAL DATA MEMBERS
    // emergency contact
    val emergencyContactList: LiveData<MutableList<Contact>> = savedStateHandle.getLiveData(CONTACT_LIST_KEY)


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val emergencyContacts = repository.getAllContacts().toMutableList()

            setContactList(emergencyContacts)
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
        emergencyContactList.value?.add(newContact)


        return repository.addContact(newContact)

    }

    fun setContactList(newContactList: MutableList<Contact>) {
        savedStateHandle[CONTACT_LIST_KEY] = newContactList
    }

    suspend fun updateContact(newContact: Contact): Int {
        return repository.updateContact(newContact)
    }

    suspend fun deleteContact(contactId: String): Int {
        return repository.deleteContact(contactId)
    }
}
