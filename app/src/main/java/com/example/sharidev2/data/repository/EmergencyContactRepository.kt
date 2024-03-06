package com.example.sharidev2.data.repository


import android.util.Log
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EmergencyContactRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    // Variables
    private val contactsRef = firestore.collection("contact")
    private val converters = Converters()
    private val currentUser = firebaseAuth.currentUser
    private val isUserLogin = currentUser != null



    suspend fun addContact(contact: Contact): Int {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val newContact = hashMapOf(
                    "contactName" to contact.contactName,
                    "contactPhone" to contact.contactPhone,
                    "userUid" to currentUser.uid
                )
                try {
                    val documentReference = firestore.collection("contact").add(newContact).await()
                    val contactId = documentReference.id

                    // Update the document with the contact ID
                    documentReference.update("contactId", contactId).await()


                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    e.printStackTrace()

                    return@withContext Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {

                return@withContext Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
        }
    }




// Retrieve Emergency Contact
    fun listenForContactChanges(callback: (List<Contact>?, Exception?) -> Unit) {
        contactsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                callback(null, exception)
                return@addSnapshotListener
            }

            // Parse and handle changes in the snapshot
            val contacts = snapshot?.documents?.mapNotNull { document ->
                document.toObject<Contact>()
            }

            // Invoke the callback with the updated data
            callback(contacts, null)
        }
    }


    suspend fun getAllContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            val contactList = mutableListOf<Contact>()

            try {
                val querySnapshot = contactsRef
                    .whereEqualTo("userUid", currentUser?.uid ?: "")
                    .get()
                    .await() // Using await() to suspend until the Firestore operation completes

                for (document in querySnapshot.documents) {
                    val contactData = document.data
                    if (contactData != null) {
                        contactList.add(converters.toContact(contactData))

                    } else {
                        Log.e(
                            "Get Emergency Contacts",
                            "Contact data is null for document ID: ${document.id}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Get Emergency Contacts", "Error fetching emergency contacts: ${e.message}")
            }

            contactList
        }
    }



    //Function to update the contact to firestore after edited
    suspend fun updateContact(newContact: Contact): Int {
        return withContext(Dispatchers.IO) {
            try {
                val contactId = newContact.contactId
                val contactUserId = newContact.userUid

                val contactRef = firestore.collection("contact").document(contactId?: "")
                val emergencyContactSnapshot = contactRef.get().await()
                val userId = emergencyContactSnapshot.getString("userUid")


                if(isUserLogin){
                    if (userId == contactUserId) {
                        // Update the emergency contact content
                        contactRef.update("contactName", newContact.contactName).await()
                        contactRef.update("contactPhone", newContact.contactPhone).await()

                        Log.d("UPDATE CONTACT", "SUCESSFUL")

                        Constants.FIREBASE_REQUEST_SUCCESS // Update successful
                    } else {
                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER // Contact doesn't belong to the current user
                    }
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED // User not authenticated
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION // Handle exceptions
                Log.d("PROBLEMMMM", e.message.toString())
            }
        }

    }

    //Function that allow the user to delete emergency contact
    suspend fun deleteContact(contactId: String): Int {
        val userUid = firebaseAuth.currentUser
        return withContext(Dispatchers.IO) {
            try {
                if (userUid != null) {
                    // Check if the contact belongs to the current user
                    val contactRef = firestore.collection("contact").document(contactId)
                    val contactSnapshot = contactRef.get().await()
                    val userId = contactSnapshot.getString("userUid")

                    if (userId == userUid.uid) {
                        // Delete the emergency contact
                        contactRef.delete().await()
                        0 // Deletion successful
                    } else {
                        1 // Contact doesn't belong to the current user
                    }
                } else {
                    2 // User not authenticated
                }
            } catch (e: Exception) {
                3 // Handle exceptions
            }
        }
    }

}