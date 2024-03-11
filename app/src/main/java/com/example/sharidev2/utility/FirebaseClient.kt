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
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.SearchLocation
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
    suspend fun getUserFromUid(userUid: String): User {
        return withContext(Dispatchers.IO) {
            try {
                val user = firestore.collection("user")
                    .document(userUid)
                    .get()
                    .await()

                val uid = user.getString("uid")
                val displayName = user.getString("displayName")
                val email = user.getString("email") ?: ""
                val phoneNumber = user.getString("phoneNumber")

                val photoUri = if(user.getString("photoUrl") != null) {
                    Uri.parse(user.getString("photoUrl"))
                } else {
                    null
                }


                val rideOptionMap = user.get("rideOption") as Map<String, String>
                val driverGender = rideOptionMap["driverGender"]?.let { Gender.valueOf(it) }
                val vehicleType = rideOptionMap["vehicleType"]?.let { VehicleType.valueOf(it) }
                val petFriendly = rideOptionMap["petFriendly"] as? Boolean

                val rideOption = RideOption(driverGender, vehicleType, petFriendly)

                val rating = (user.get("rating") as Long).toDouble()

//                val savedAddresses =
//                    converters.toSearchLocationList(user.get("savedAddress") as List<Map<String, Any>>)
//                        .toMutableList()

                val gender = if(user.getString("gender") != null) {
                    Gender.valueOf(user.getString("gender")!!)
                } else {
                    null
                }

                val joinedDate = user.getTimestamp("joinedDate")


                return@withContext User(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    phoneNumber = phoneNumber,
                    photoUri = photoUri,
                    rideOption = rideOption,
                    rating = rating,
                    savedAddress = mapOf(),
                    gender = gender,
                    joinedDate = joinedDate
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
            val messageMap = mutableMapOf<String,  Message>()

            // Retrieve messages with proper suspend handling
            try {
                val messagesRef =
                    firestore.collection("chat").document(chatId).collection("messages")
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
                        messageType = MessageType.valueOf(
                            messageData["messageType"] as? String ?: ""
                        )
                    )
                    messageMap[messageDoc.id] = message
                }
            } catch (e: Exception) {
                Log.e("Get Chat From ChatId (Messages)", e.message.toString())
            }

            return@withContext Chat(
                chatId,
                members,
                lastMessage,
                timestamp,
                messageMap
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

                // Waypoints Sub-Collection
                val waypointsSnapshot = document.reference.collection("waypoints")
                    .get()
                    .await()
                val waypointsMap = mutableMapOf<String, SearchLocation>()

                if(waypointsSnapshot != null && !waypointsSnapshot.isEmpty) {
                    for(waypointDoc in waypointsSnapshot.documents) {
                        val waypointData: Map<String, Any>? = waypointDoc.data

                        if(waypointData != null) {
                            waypointsMap[waypointDoc.id] = converters.toSearchLocation(waypointData)
                        }
                    }
                }


                val datetime = document.getTimestamp("datetime")!!
                val driver = converters.toDriver(document.get("driver") as Map<String, Any>)

                // Passenger Sub-Collection
                val passengersSnapshot = document.reference.collection("passengers")
                    .get()
                    .await()
                val passengersMap = mutableMapOf<String, Passenger>()

                if(passengersSnapshot != null && !passengersSnapshot.isEmpty) {
                    for(passengerDoc in passengersSnapshot.documents) {
                        val passengerData: Map<String, Any>? = passengerDoc.data

                        if(passengerData != null) {
                            passengersMap[passengerDoc.id] = converters.toPassenger(passengerData)
                        }
                    }
                }

                val rideStatus = RideStatus.valueOf(document.getString("rideStatus") ?: "")
                val startTime = document.getTimestamp("startTime")
                val completeTime = document.getTimestamp("completeTime")
                val availableSeats = (document.get("availableSeats") as Long).toInt()

                // Review Sub-Collection
                val reviewSnapshot = document.reference.collection("reviews")
                    .get()
                    .await()
                val reviewsMap = mutableMapOf<String, Review>()

                if(reviewSnapshot != null && !reviewSnapshot.isEmpty) {
                    for(reviewDoc in reviewSnapshot.documents) {
                        val reviewData = reviewDoc.data

                        if(reviewData != null) {
                            reviewsMap[reviewDoc.id] = converters.toReview(reviewData)
                        }
                    }
                }

                // Chat Sub-Collection
                //val chat = FirebaseClient.getChatFromChatId(getString("chat") ?: "")
                val chat = converters.toChat(document.get("chat") as Map<String, Any>)
                val messagesMap = mutableMapOf<String, Message>()

                val messageSnapshot = document.reference.collection("messages")
                    .orderBy("timestamp")
                    .get()
                    .await()

                if(!messageSnapshot.isEmpty && messageSnapshot != null) {
                    for(messageDoc in messageSnapshot.documents) {
                        val messageData = messageDoc.data

                        if(messageData != null) {
                            messagesMap[messageDoc.id] = converters.toMessage(messageData)
                        }
                    }
                }
                chat.messages = messagesMap

                val createdAt = document.getTimestamp("createdAt")!!


                val ride = Ride(
                    document.id,
                    origin,
                    destination,
                    waypointsMap,
                    datetime,
                    driver,
                    passengersMap,
                    rideStatus,
                    startTime,
                    completeTime,
                    availableSeats,
                    reviewsMap,
                    chat,
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