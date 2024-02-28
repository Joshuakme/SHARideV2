package com.example.sharidev2.utility

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleType
import com.google.firebase.Timestamp
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseUtils(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        // CONSTANT
        val SUCCESS = 0 // Success
        val NOT_BELONG_USER = 1 // Contact doesn't belong to the current user
        val USER_NOT_AUTHENTICATED = 2 // User not authenticated
        val EXCEPTION = 3 // Handle exceptions

    }

    // Variables
    private val converters = Converters()

    // COROUTINES FUNCTIONS
    suspend fun getUserFromUid(userUid: String): User {
        return withContext(Dispatchers.IO) {
            try {
                val user = firestore.collection("user")
                    .document(userUid)
                    .get()
                    .await()


                val uid = user.getString("uid")
                val displayName = user.getString("displayName")
                val email = user.getString("email")
                val phoneNumber = user.getString("phoneNumber")
                val photoUri = Uri.parse(user.getString("photoUrl"))

                val rideOptionMap = user.get("rideOption") as Map<String, String>
                val driverGender = Gender.valueOf(rideOptionMap["driverGender"] as String)
                val vehicleType = VehicleType.valueOf(rideOptionMap["vehicleType"] as String)
                val petFriendly = rideOptionMap["petFriendly"] as Boolean
                val rideOption = RideOption(driverGender, vehicleType, petFriendly)

                val rating = (user.get("rating") as Long).toFloat()
                //val savedAddresses = converters.toSearchLocationList(user.get("savedAddresses") as List<Map<String, Any>>).toMutableList()
                val gender = Gender.valueOf(user.getString("gender") ?: "")
                val joinedDate = user.getTimestamp("joinedDate")


                return@withContext User(
                    uid,
                    displayName,
                    email,
                    phoneNumber,
                    photoUri,
                    rideOption,
                    rating,
                    mutableListOf(),
                    gender,
                    joinedDate
                )
            } catch (e: Exception) {
                Log.e("Get User From Uid", e.message.toString())
                return@withContext User()
            }
        }
    }

    suspend fun getVehicleFromId(vehicleId: String): Vehicle {
        return withContext(Dispatchers.IO) {
            try {
                val vehicle = firestore.collection("vehicle")
                    .document(vehicleId)
                    .get()
                    .await()

                val brand = vehicle.getString("brand")
                val model = vehicle.getString("model")
                val vehicleType = VehicleType.valueOf(vehicle.getString("type") ?: "")
                val plateNumber = vehicle.getString("plateNumber")
                val color = vehicle.getString("color")

                val photosString = vehicle.get("photos") as List<String>
                val photos = mutableListOf<Uri>()
                for (photo in photosString) {
                    photos.add(Uri.parse(photo))
                }

                val capacity = (vehicle.get("capacity") as Long).toInt()

                return@withContext Vehicle(
                    vehicleId,
                    brand,
                    model,
                    vehicleType,
                    plateNumber,
                    color,
                    photos,
                    capacity
                )
            } catch (e: Exception) {
                Log.e("Get Vehicle From ID", e.message.toString())
                return@withContext Vehicle()
            }
        }
    }

    suspend fun getChatFromChatId(chatId: String): Chat {
        return withContext(Dispatchers.IO) {  // Use Dispatchers.IO for network calls
            val chatRef = firestore.collection("chat").document(chatId)

            val chatData = try {
                chatRef.get().await().data ?: return@withContext Chat() // Handle missing document
            } catch (e: Exception) {
                Log.e("Get Chat From ChatId", e.message.toString())
                return@withContext Chat()
            }

            val chatId = chatRef.id
            val members = chatData["members"] as? List<String> ?: emptyList()
            val lastMessage = chatData["lastMessage"] as? String ?: ""
            val timestamp = chatData["timestamp"] as? Timestamp
            val typingUsers = chatData["typingUsers"] as? List<String> ?: emptyList()
            val messageList = mutableListOf<Message>()

            // Retrieve messages with proper suspend handling
            try {
                val messagesRef = firestore.collection("chat").document(chatId).collection("messages")
                val messageDocs = messagesRef.get().await()
                messageDocs.forEach { messageDoc ->
                    val messageData = messageDoc.data ?: return@forEach
                    val message = Message(
                        messageId = messageDoc.id,
                        senderId = messageData["senderId"] as? String ?: "",
                        text = messageData["text"] as? String ?: "",
                        timestamp = messageData["timestamp"] as? Timestamp,
                        attachmentURL = messageData["attachmentURL"] as? String,
                        readBy = messageData["readBy"] as? List<String> ?: emptyList(),
                        messageType = MessageType.valueOf(messageData["messageType"] as? String ?: "")
                    )
                    messageList.add(message)
                }
            } catch (e: Exception) {
                Log.e("Get Chat From ChatId (Messages)", e.message.toString())
            }

            return@withContext Chat(
                chatId,
                members,
                lastMessage,
                timestamp,
                typingUsers,
                messageList
            )
        }
    }

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