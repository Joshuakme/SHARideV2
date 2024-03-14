import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharidev2.utility.FirebaseClient

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseClient.firestore
    private val auth = FirebaseClient.firebaseAuth

    private val _profilePicUrl = MutableLiveData<String?>()
    val profilePicUrl: LiveData<String?> = _profilePicUrl

    private val _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> = _displayName

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId

    private val currentUser = auth.currentUser

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        currentUser?.let { user ->
            val userDocRef = firestore.collection("user").document(user.uid)

            userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profilePicUrl = snapshot.getString("photoUrl")
                    val displayName = snapshot.getString("displayName")

                    _profilePicUrl.value = profilePicUrl
                    _displayName.value = displayName
                    _userId.value = user.uid
                }
            }
        }
    }
}
