package com.example.sharidev2.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleType
import com.google.firebase.Timestamp
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object FirebaseClient {
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }


    // Variables
    private val converters = Converters()
    private val commonUtils = CommonUtils()

    // COROUTINES FUNCTIONS
    suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser

        if(currentUser != null) {
            return getUserFromUid(currentUser.uid)
        }

        return null
    }
    suspend fun getUserFromUid(userUid: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val userData = firestore.collection("user")
                    .document(userUid)
                    .get()
                    .await().data

                if(userData != null) {
                    val uid = userData["uid"] as String
                    val displayName = userData["displayName"] as String
                    val email = userData["email"] as String?
                    val phoneNumber = userData["phoneNumber"] as String

                    val photoUri = if(userData["photoUrl"] != null) {
                        Uri.parse(userData["photoUrl"] as String)
                    } else {
                        null
                    }


                    val rideOptionMap = userData["rideOption"] as Map<String, String>
                    val driverGender = rideOptionMap["driverGender"]?.let { Gender.valueOf(it) }
                    val vehicleType = rideOptionMap["vehicleType"]?.let { VehicleType.valueOf(it) }
                    val petFriendly = rideOptionMap["petFriendly"] as? Boolean

                    val rideOption = RideOption(driverGender, vehicleType, petFriendly)

                    val rating = if(userData["rating"] != null) {
                        (userData["rating"] as Long).toDouble()
                    } else {
                        null
                    }


//                val savedAddresses =
//                    converters.toSearchLocationList(user.get("savedAddress") as List<Map<String, Any>>)
//                        .toMutableList()

                    val gender = if(userData["gender"] != null) {
                        Gender.valueOf(userData["gender"] as String)
                    } else {
                        null
                    }

                    val joinedDate = userData["joinedDate"] as Timestamp


                    return@withContext User(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        phoneNumber = phoneNumber,
                        photoUri = photoUri,
                        rideOption = rideOption,
                        rating = rating,
                        savedAddress = null,
                        gender = gender,
                        joinedDate = joinedDate
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Get User From Uid", e.message.toString())
                return@withContext null
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

    suspend fun getChatFromChatId(chatId: String): Chat? {
        return withContext(Dispatchers.IO) {  // Use Dispatchers.IO for network calls
            val chatRef = firestore.collection("chat").document(chatId)

            val chatData = try {
                chatRef.get().await().data ?: return@withContext null // Handle missing document
            } catch (e: Exception) {
                Log.e("Get Chat From ChatId", e.message.toString())
                return@withContext null
            }

            val chatId = chatRef.id
            val chatTitle = chatData["chatTitle"] as? String ?: ""
            val members = chatData["members"] as? List<String> ?: emptyList()
            val lastMessage = chatData["lastMessage"] as? String ?: ""
            val timestamp = chatData["timestamp"] as? Timestamp
            val typingUsers = chatData["typingUsers"] as? List<String> ?: emptyList()
            val messages = chatData["messages"] as List<Map<String, Any>>
            val rideId = chatData["rideId"] as String

            val messageList = mutableListOf<Message>()
            // Retrieve messages with proper suspend handling
            try {
                if(messages != null) {
                    messages.forEach { messageMap ->
                        val message = Message(
                            messageId = messageMap["messageId"] as? String ?: "",
                            senderId = messageMap["senderId"] as? String ?: "",
                            text = messageMap["text"] as? String ?: "",
                            timestamp = messageMap["timestamp"] as? Timestamp,
                            attachmentURL = messageMap["attachmentURL"] as? String,
                            readBy = messageMap["readBy"] as? List<String> ?: emptyList(),
                            messageType = MessageType.valueOf(
                                messageMap["messageType"] as? String ?: ""
                            )
                        )
                        messageList.add(message)
                    }
                }
            } catch (e: Exception) {
                Log.e("Get Chat From ChatId (Messages)", e.message.toString())
            }

            return@withContext Chat(
                chatId,
                chatTitle,
                members,
                lastMessage,
                timestamp,
                messageList,
                rideId
            )
        }
    }

    suspend fun getReviewFromChatId(reviewId: String): Review {
        return withContext(Dispatchers.IO) {
            try {
                val review = firestore.collection("review")
                    .document(reviewId)
                    .get()
                    .await()

                val reviewID = review.id
                val reviewer = review.getString("reviewer") ?: ""
                val reviewedUser = review.getString("reviewedUser")
                val rating = (review.get("rating") as Long).toFloat()
                val comment = review.getString("comment")
                val dateTime = review.getTimestamp("datetime")


                return@withContext Review(
                    reviewID,
                    reviewer,
                    reviewedUser,
                    rating,
                    comment,
                    dateTime
                )
            } catch (e: Exception) {
                Log.e("Get Vehicle From ID", e.message.toString())
                return@withContext Review()
            }
        }
    }

    suspend fun getRideFromRideId(rideId: String): Ride? {
        return withContext(Dispatchers.IO) {
            try {
                val ride = firestore.collection("ride")
                    .document(rideId)
                    .get()
                    .await()

                createRideFromDocumentSnapshot(ride)
            } catch (e: Exception) {
                Log.e("Get Ride From ID", e.message.toString())
                null
            }
        }
    }

    suspend fun createRideFromDocumentSnapshot(document: DocumentSnapshot): Ride? {
        return withContext(Dispatchers.IO) {
            try {
                val origin = converters.toSearchLocation(document.get("origin") as Map<String, Any>)
                val destination = converters.toSearchLocation(document.get("destination") as Map<String, Any>)
                val datetime = document.getTimestamp("datetime")!!
                val driver = converters.toDriver(document.get("driver") as Map<String, Any>)

                // Passengers
                val passengers = if(document.get("passengers") != null) {
                    converters.toPassengerList(document.get("passengers") as List<Map<String, Any>>)
                } else {
                    emptyList()
                }

                val rideStatus = RideStatus.valueOf(document.getString("rideStatus") ?: "")
                val startTime = document.getTimestamp("startTime")
                val completeTime = document.getTimestamp("completeTime")
                val availableSeats = (document.get("availableSeats") as Long).toInt()

                // Reviews
                val reviewList = if(document.get("reviews") != null) {
                    converters.toReviewList(document.get("reviews") as List<Map<String, Any>>)
                } else {
                    emptyList()
                }


                // Chat Sub-Collection
                //val chat = FirebaseClient.getChatFromChatId(getString("chat") ?: "")
                //val chat = converters.toChat(document.get("chat") as Map<String, Any>)


                // Completed Route
                val completedRoute = if(document.get("completedRoute") != null) {
                    converters.toLatLngList(document.get("completedRoute") as List<Map<String, Any>>)
                } else {
                    mutableListOf()
                }

                val createdAt = document.getTimestamp("createdAt")!!


                val ride = Ride(
                    id = document.id,
                    origin = origin,
                    destination = destination,
                    datetime = datetime,
                    driver = driver,
                    passengers = passengers,
                    rideStatus = rideStatus,
                    startTime = startTime,
                    completeTime = completeTime,
                    availableSeats = availableSeats,
                    reviews = reviewList,
                    chat = null,
                    completedRoute = completedRoute,
                    createdAt = createdAt
                )

               ride
            } catch (e: Exception) {
                Log.e("Create Ride List From Query Snapshot", e.message.toString())

                null
            }
        }
    }


    suspend fun assignUserDefaultInfo(additionalUserInfo: AdditionalUserInfo?) {
        val defaultUsername = generateUniqueUsername()
        updateProfileWithDefaultUsername(defaultUsername, additionalUserInfo)
    }

    // Function to check if a username is already taken (suspended version)
    private suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        // TODO: Get username list from Firestore
        val existingUsernames =
            listOf("user1", "user2", "user3") // Replace this with your actual list of usernames

        existingUsernames.contains(username)
    }

    // Generate a unique random username for the user
    private suspend fun generateUniqueUsername(): String {
        var username: String
        do {
            // Generate a random string for the username
            username =
                commonUtils.generateRandomString(8) // You can customize the length of the username as needed
        } while (isUsernameTaken(username)) // Keep generating until a unique username is found
        return username
    }

    // Update the user's profile with the generated default username
    private suspend fun updateProfileWithDefaultUsername(
        defaultUsername: String,
        additionalUserInfo: AdditionalUserInfo?
    ) {
        val user = firebaseAuth.currentUser

        if (user != null && additionalUserInfo?.isNewUser == true) {
            // Create a UserProfileChangeRequest with the new display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(defaultUsername)
                .build()

            // Update the user's profile
            user?.updateProfile(profileUpdates)
                ?.await()
        }

    }

    fun convertFirebaseImageToBitmap(
        storageReference: StorageReference,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Create a temporary file to store the downloaded image
            val localFile = File.createTempFile("temp_image", "jpg")

            // Download the image file from Firebase Storage
            storageReference.getFile(localFile)
                .addOnSuccessListener {
                    // Image downloaded successfully, decode it into a Bitmap
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)

                    // Callback with the Bitmap
                    onSuccess(bitmap)

                    // Delete the temporary file
                    localFile.delete()
                }
                .addOnFailureListener { exception ->
                    // Error occurred while downloading the image
                    onFailure(exception)
                }
        } catch (e: IOException) {
            // Error occurred while creating a temporary file
            onFailure(e)
        }
    }


    fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView) {
        Glide.with(context)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }

}