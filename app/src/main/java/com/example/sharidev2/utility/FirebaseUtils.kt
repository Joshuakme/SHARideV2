package com.example.sharidev2.utility

import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseUtils() {
    // Variables
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // COROUTINES FUNCTIONS


    suspend fun assignUserDefaultInfo(additionalUserInfo: AdditionalUserInfo?) {
        val defaultUsername = generateUniqueUsername()
        updateProfileWithDefaultUsername(defaultUsername, additionalUserInfo)
    }


    // Function to check if a username is already taken (suspended version)
    private suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        // Implementation to check if the username is already taken
        // You need to replace this with your own logic to check if the username exists in your database
        // For example, you might query your database to see if the username already exists
        // For demonstration purposes, let's assume there's a list of existing usernames
        val existingUsernames = listOf("user1", "user2", "user3") // Replace this with your actual list of usernames

        existingUsernames.contains(username)
    }

    // Generate a unique random username for the user
    private suspend fun generateUniqueUsername(): String {
        var username: String
        do {
            // Generate a random string for the username
            username = CommonUtils().generateRandomString(8) // You can customize the length of the username as needed
        } while (isUsernameTaken(username)) // Keep generating until a unique username is found
        return username
    }

    // Update the user's profile with the generated default username
    private fun updateProfileWithDefaultUsername(defaultUsername: String, additionalUserInfo: AdditionalUserInfo?) {
        val user = firebaseAuth.currentUser

        if (user != null && additionalUserInfo?.isNewUser == true) {
            // Create a UserProfileChangeRequest with the new display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(defaultUsername)
                .build()

            // Update the user's profile
            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Username updated successfully
                        // You can notify the user or perform any additional actions here
                    } else {
                        // Username update failed
                        // Handle the error gracefully, such as displaying an error message to the user
                    }
                }
        }

    }
}