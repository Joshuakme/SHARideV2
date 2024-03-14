package com.example.sharidev2.data.repository


import android.util.Log
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EmergencyContactRepository {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth

    // Variables
    private val contactsRef = firestore.collection("contact")
    private val converters = Converters()
    private val currentUser = firebaseAuth.currentUser
    private val isUserLogin = currentUser != null


    suspend fun addContact(contact: Contact): Int {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {

                try {
                    val contactId = contactsRef.document().id

                    val newContact = hashMapOf(
                        "contactId" to contactId,
                        "contactName" to contact.contactName,
                        "contactPhone" to contact.contactPhone,
                        "userUid" to currentUser.uid,
                    )

                    contactsRef
                        .document(contactId)
                        .set(newContact)
                        .await()

                    Log.e("Add Contact", "Added Successfully")
                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Add Contact", e.message.toString())

                    return@withContext Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                Log.e("Add Contact", "User not login")
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

            if(currentUser != null) {
                try {
                    val querySnapshot = contactsRef
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await() // Using await() to suspend until the Firestore operation completes

                    Log.e("Get ALl Contacts", "Contact Num: " + querySnapshot.size())
                    if(!querySnapshot.isEmpty) {
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
                    }
                } catch (e: Exception) {
                    Log.e("Get Emergency Contacts", "Error fetching emergency contacts: ${e.message}")
                }
            }

            contactList
        }
    }



    //Function to update the contact to firestore after edited
    suspend fun updateContact(newContact: Contact): Int {
        return withContext(Dispatchers.IO) {
            try {
                val contactId = newContact.contactId!!
                val contactUserId = newContact.userUid


                val contactDocRef = contactsRef.document(contactId)
                val contactData = contactDocRef
                                        .get()
                                        .await()
                                        .data


                if(contactData != null) {
                    val userId = contactData["userUid"] as String

                    if(isUserLogin){
                        if (userId == contactUserId) {
                            // Update the emergency contact content
                            contactDocRef.update("contactName", newContact.contactName).await()
                            contactDocRef.update("contactPhone", newContact.contactPhone).await()

                            Constants.FIREBASE_REQUEST_SUCCESS  // Update successful
                        } else {
                            Log.d("UPDATE CONTACT", "NOT YOUR CONTACT BRO")
                            Constants.FIREBASE_REQUEST_NOT_BELONG_USER // Contact doesn't belong to the current user
                        }
                    } else {
                        Log.d("UPDATE CONTACT", "LOGIN PLEASE BRO")
                        Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED // User not authenticated
                    }
                } else {
                    Log.e("Update Contact", "TAK ADA CONTACT DATA")
                    Constants.FIREBASE_REQUEST_FAILED
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION // Handle exceptions
                Log.d("Update Contact", e.message.toString())
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

                        Constants.FIREBASE_REQUEST_SUCCESS
                    } else {
                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER
                    }
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }

}