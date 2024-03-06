import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sharidev2.adapter.ContactAdapter
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.databinding.FragmentAddContactBinding

class AddContactFragment : Fragment() {
    private lateinit var binding: FragmentAddContactBinding
    private lateinit var contactAdapter: ContactAdapter // Assuming you have a RecyclerView adapter
    private val emergencyContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and its adapter
//        contactAdapter = ContactAdapter(emergencyContacts)
//        binding.contactRecyclerView.apply {
//            layoutManager = LinearLayoutManager(context)
//            adapter = contactAdapter
//        }

        // Fetch emergency contacts from Firestore
        //fetchEmergencyContacts()
    }

//    private fun fetchEmergencyContacts() {
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//            try {
//                val contacts = EmergencyContactRepository.fetchEmergencyContact()
//                emergencyContacts.clear()
//                for (contactMap in contacts) {
//                    for ((_, contact) in contactMap) {
//                        emergencyContacts.add(contact)
//                    }
//                }
//                contactAdapter.notifyDataSetChanged()
//            } catch (e: Exception) {
//                Toast.makeText(requireContext(), "Failed to fetch emergency contacts", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
