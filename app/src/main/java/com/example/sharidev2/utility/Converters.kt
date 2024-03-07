package com.example.sharidev2.utility

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleType
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf


class Converters() {
    private val gson = Gson()

    companion object {
        fun metersToKiloMeters(value: Int): Double {
            return value.toDouble() / 1000
        }

        fun getBitmapFromVectorDrawable(vectorDrawable: Drawable): Bitmap {
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }
    }


    // SEARCH LOCATION CONVERTERS
    fun toSearchLocation(map: Map<String, Any>): SearchLocation {
        val placeId = map["placeId"] as String
        val name = map["name"] as String
        val distanceMetersFromOrigin = (map["distanceMetersFromOrigin"] as Long).toInt()
        val detailAddress = map["detailAddress"] as String

        val geolocationMap = map.getValue("geolocation") as Map<String, Double>
        val latitude = geolocationMap["latitude"]
        val longitude = geolocationMap["longitude"]

        return SearchLocation(
            placeId,
            name,
            distanceMetersFromOrigin,
            detailAddress,
            LatLng(latitude?: 0.0, longitude?: 0.0)
        )
    }

    fun toSearchLocationList(mapList: List<Map<String, Any>>): List<SearchLocation> {
        val searchLocationList = mutableListOf<SearchLocation>()


        for(map in mapList) {
            searchLocationList.add(toSearchLocation(map))
        }

        return searchLocationList
    }


    // USER CONVERTERS
    fun toUser(map: Map<String, Any>): User {
        val uid = map["uid"] as String
        val displayName = map["displayName"] as String?
        val email = map["email"] as String?
        val phoneNumber = map["phoneNumber"] as String?
        val photoUrl = if(map["photoUrl"] != null) Uri.parse(map["photoUrl"] as String) else null

        val rideOptionMap = map["rideOption"] as Map<String, String>
        val driverGender = if(rideOptionMap["driverGender"] != null) Gender.valueOf(rideOptionMap["driverGender"] as String) else null
        val vehicleType = if(rideOptionMap["vehicleType"] != null) VehicleType.valueOf(rideOptionMap["vehicleType"] as String) else null
        val petFriendly = if(rideOptionMap["petFriendly"] != null) rideOptionMap["petFriendly"] as Boolean else null
        val rideOption = RideOption(driverGender, vehicleType, petFriendly)

        val rating = if(map["rating"] != null) map["rating"] as Double else null
        //val savedAddresses = toSearchLocationList(map["savedAddresses"] as List<Map<String, Any>>).toMutableList()
        val savedAddresses = mapOf<String, SearchLocation>()
        val gender = if(map["gender"] != null) Gender.valueOf(map["gender"] as String) else null
        val joinedDate = map["joinedDate"] as Timestamp

        return User(
            uid,
            displayName,
            email,
            phoneNumber,
            photoUrl,
            rideOption,
            rating,
            savedAddresses,
            gender,
            joinedDate
        )
    }

    fun toUserList(mapList: List<Map<String, Any>>): List<User> {
        val userList = mutableListOf<User>()

        for(map in mapList) {
            userList.add(toUser(map))
        }

        return userList
    }

    suspend fun toDriver(map: Map<String, Any>): Driver {
        val userUid = map["userUid"] as String

//        val driverUser = toUser(map["user"] as Map<String, Any>)
        val driverUser = FirebaseClient.getUserFromUid(userUid)

        val locationMap = map["location"] as Map<String, Any>?
        val location = if(locationMap != null) {
            val latitude = locationMap["latitude"] as Double
            val longitude = locationMap["longitude"] as Double

            LatLng(latitude, longitude)
        } else {
            null
        }

        val status = UserStatus.valueOf((map["status"] as String))
        val vehicle = toVehicle(map["vehicle"] as Map<String, Any>)

        return Driver(
            userUid,
            driverUser,
            location,
            status,
            vehicle
        )
    }

    suspend fun toPassenger(map: Map<String, Any>): Passenger {
        val userUid = map["userUid"] as String

//        val user = if(map["user"] != null) toUser(map["user"] as Map<String, Any>) else User()
        val user = FirebaseClient.getUserFromUid(userUid)

        val locationMap = map["location"] as Map<String, Any>?
        val location = if(locationMap != null) {
            val latitude = locationMap["latitude"] as Double
            val longitude = locationMap["longitude"] as Double

            LatLng(latitude, longitude)
        } else {
            null
        }

//        val origin = if(map["origin"] as Map<String, Any> != null) {
//            toSearchLocation(map["origin"] as Map<String, Any>)
//        } else {
//            null
//        }
//
//        val destination = if(map["destination"] as Map<String, Any> != null) {
//            toSearchLocation(map["destination"] as Map<String, Any>)
//        } else {
//            null
//        }

        val status = UserStatus.valueOf((map["status"] as String))
        val ridePrice = (map["price"] as Long?)?.toDouble()

        return Passenger(
            userUid,
            user,
            location,
            null,null,
            status,
            ridePrice
        )
    }



    // PHOTOS CONVERTERS
    fun toUriList(photoUrlList: List<String>): List<Uri> {
        val uriList = mutableListOf<Uri>()

        for(photo in photoUrlList) {
            uriList.add(Uri.parse(photo))
        }

        return uriList
    }

    // REVIEW CONVERTERS
    fun toReview(map: Map<String, Any>): Review {
        val reviewId = map["reviewId"] as String
        val reviewer = map["reviewer"] as String
        val reviewedUser = map["reviewedUser"] as String
        val rating = (map["rating"] as Long).toFloat()
        val comment = map["comment"] as String
        val dateTime = map["datetime"] as Timestamp

        return Review(
            reviewId,
            reviewer,
            reviewedUser,
            rating,
            comment,
            dateTime
        )
    }

    fun toReviewList(mapList: List<Map<String, Any>>): List<Review> {

        val reviewList = mutableListOf<Review>()

        for(map in mapList) {
            if(map != null) {
                reviewList.add(toReview(map))
            }
        }

        return reviewList
    }


    // VEHICLE CONVERTERS
    fun toVehicle(map: Map<String, Any>): Vehicle {
        val vehicleID = map["vehicleID"] as String
        val brand = map["brand"] as String
        val model = map["model"] as String
        val type = VehicleType.valueOf(map["type"] as String)
        val plateNumber = map["plateNumber"] as String
        val color = map["color"] as String

        val photosString = map["photos"] as List<String>
        val photos = mutableListOf<Uri>()
        for (photo in photosString) {
            photos.add(Uri.parse(photo))
        }

        val capacity = (map["capacity"] as Long).toInt()

        return Vehicle(
            vehicleID,
            brand,
            model,
            type,
            plateNumber,
            color,
            photos,
            capacity
        )
    }


    // CHAT CONVERTERS
    fun toChat(map: Map<String, Any>): Chat {
        val chatId = map["chatId"] as String
        val members = map["members"] as List<String>
        val lastMessage = map["lastMessage"] as String
        val timestamp = map["timestamp"] as Timestamp

        return Chat(
            chatId,
            members,
            lastMessage,
            timestamp
        )
    }

    // MESSAGE CONVERTERS
    fun toMessage(map: Map<String, Any>): Message {
        val messageId = map["messageId"] as String
        val senderID = map["senderId"] as String
        val text = map["text"] as String
        val timestamp = map["timestamp"] as Timestamp
        val attachmentURL = map["attachmentURL"] as String
        val readBy = map["messageId"] as List<String>
        val messageType = MessageType.valueOf(map["messageType"] as String)

        return Message(
            messageId,
            senderID,
            text,
            timestamp,
            attachmentURL,
            readBy,
            messageType
        )
    }

    fun toMessageList(mapList: List<Map<String, Any>>): List<Message> {

        val messageList = mutableListOf<Message>()

        for(map in mapList) {
            messageList.add(toMessage(map))
        }

        return messageList
    }


    // PASSENGER STATUS
    fun toPassengersStatus(map: Map<String, String>): Map<String, UserStatus> {
        val passengerStatuses: MutableMap<String, UserStatus> = mutableMapOf()
        if(map.isNotEmpty()) {
            for (entry in map) {
                val userId = entry.key
                val statusString = entry.value

                // Convert string status to UserStatus enum
                val userStatus = UserStatus.valueOf(statusString)

                // Add to the map using userId as key and converted UserStatus as value
                passengerStatuses[userId] = userStatus
            }
        }
        return passengerStatuses.toMap() // Convert to immutable map
    }


    // PRICE CONVERTERS
    fun toPrice(map: Map<String, Long>): Map<String, Double> {
        val prices: MutableMap<String, Double> = mutableMapOf()

        if(map.isNotEmpty()) {
            for (entry in map) {
                val userId = entry.key
                val price = entry.value.toDouble()


                // Add to the map using userId as key and converted UserStatus as value
                prices[userId] = price
            }
        }

        return prices.toMap() // Convert to immutable map
    }


    // CONTACT CONVERTERS
    fun toContact(map: Map<String, Any>): Contact {
        val contactId = map["contactId"] as String
        val contactName = map["contactName"] as String
        val contactPhone = map["contactPhone"] as String
        val userUid = map["userUid"] as String

        return Contact(
            contactId = contactId,
            contactName = contactName,
            contactPhone = contactPhone,
            userUid = userUid
        )
    }

}