package com.example.sharidev2.utility

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.ChatStatus
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.data.model.VehicleType
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson


class Converters() {
    private val gson = Gson()

    companion object {
        fun metersToKiloMeters(value: Int): Double {
            return value.toDouble() / 1000
        }
    }



    // RIDE
    fun toRideHashMap(ride: Ride): HashMap<String, *> {
        return hashMapOf(
            "rideId" to ride.id,
            "origin" to ride.origin,
            "destination" to ride.destination,
            "datetime" to ride.datetime,
            "driver" to toDriverHashMapWithoutUser(ride.driver),
            "passengers" to ride.passengers,
            "rideStatus" to ride.rideStatus,
            "startTime" to ride.startTime,
            "completeTime" to ride.completeTime,
            "availableSeats" to ride.availableSeats,
            "reviews" to ride.reviews,
            "chat" to ride.chat,
            "completedRoute" to ride.completedRoute,
            "createdAt" to ride.createdAt
        )
    }

    fun toRideHashMap(ride: Ride, rideId:String, chatId: String?): HashMap<String, *> {
        val passengerIds = mutableListOf<String>()
        for(passenger in ride.passengers) {
            if(passenger.userUid != null)
                passengerIds.add(passenger.userUid)
        }

        return hashMapOf(
            "rideId" to rideId,
            "origin" to ride.origin,
            "destination" to ride.destination,
            "datetime" to ride.datetime,
            "driver" to toDriverHashMapWithoutUser(ride.driver),
            "passengers" to ride.passengers,
            "passengerIds" to passengerIds,
            "rideStatus" to ride.rideStatus,
            "startTime" to ride.startTime,
            "completeTime" to ride.completeTime,
            "availableSeats" to ride.availableSeats,
            "reviews" to ride.reviews,
            "chat" to chatId,
            "completedRoute" to ride.completedRoute,
            "createdAt" to ride.createdAt
        )
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

        val fcmToken = map["fcmToken"] as String

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
            fcmToken,
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

    private fun toDriverHashMapWithoutUser(driver: Driver): HashMap<String, *> {
        // Remove user field from uploading to Firestore

        return hashMapOf(
            "userUid" to driver.userUid,
            "location" to driver.location,
            "status" to driver.status,
            "vehicle" to driver.vehicle
        )
    }

    private suspend fun toPassenger(map: Map<String, Any>): Passenger {
        val userUid = map["userUid"] as String
        val user = FirebaseClient.getUserFromUid(userUid)

        val locationMap = map["location"] as Map<String, Any>?
        val location = if(locationMap != null) {
            val latitude = locationMap["latitude"] as Double
            val longitude = locationMap["longitude"] as Double

            LatLng(latitude, longitude)
        } else {
            null
        }

        val origin = if(map["origin"] as Map<String, Any>? != null) {
            toSearchLocation(map["origin"] as Map<String, Any>)
        } else {
            null
        }

        val destination = if(map["destination"] as Map<String, Any> != null) {
            toSearchLocation(map["destination"] as Map<String, Any>)
        } else {
            null
        }

        val status = UserStatus.valueOf((map["status"] as String))
        val ridePrice = map["ridePrice"] as? Double?

        val requestedDateTime = map["requestedDateTime"] as Timestamp?

        return Passenger(
            userUid = userUid,
            user = user,
            location = location,
            origin = origin,
            destination = destination,
            status = status,
            ridePrice = ridePrice,
            requestedDateTime = requestedDateTime
        )
    }

    suspend fun toPassengerList(mapList: List<Map<String, Any>>): List<Passenger> {
        val passengerList = mutableListOf<Passenger>()

        for(map in mapList) {
            passengerList.add(toPassenger(map))
        }

        return passengerList
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


    // VEHICLE DOC CONVERTERS
    fun toVehicleDoc(map: Map<String, Any>): VehicleDoc {
        val userUid = map["userUid"] as String
        val firstName = map["firstName"] as String
        val lastName = map["lastName"] as String
        val vehicleTypeString = map["type"] as String
        val vehicleType = VehicleType.valueOf(vehicleTypeString)
        val vehicleModel = map["vehicleModel"] as String
        val carPlate = map["carPlate"] as String
        val manufactureDate = map["manufactureDate"] as Timestamp


//        val vehicleRegisCertMap = map["vehicleRegisCert"] as Map<String, Any>
//        val vehicleRegisCertList = mutableListOf<Uri>()
//
//        vehicleRegisCertMap.forEach {(s, cert) ->
//            vehicleRegisCertList.add(Uri.parse(cert as String))
//        }

        val vehicleRegisCert = map["vehicleRegisCert"] as String
        val roadtax = map["roadtax"] as String
        val insurance = map["insurance"] as String


//        val roadtaxMap = map["roadtax"] as Map<String, Any>
//        val roadtaxList = mutableListOf<Uri>()

//        roadtaxMap.forEach {(r, roadtax) ->
//            roadtaxList.add(Uri.parse(roadtax as String))
//        }
//
//
//        val insuranceMap = map["insurance"] as Map<String, Any>
//        val insuranceList = mutableListOf<Uri>()
//
//        insuranceMap.forEach {(i, insurance) ->
//            insuranceList.add(Uri.parse(insurance as String))
//        }


        val vehicleId = map["vehicleId"] as String


        return VehicleDoc(
            userUid,
            firstName,
            lastName,
            vehicleTypeString, // Pass vehicleTypeString instead of vehicleType
            vehicleModel,
            carPlate,
            manufactureDate,
            vehicleRegisCert,
            roadtax,
            insurance,
            vehicleId

        )
    }


    // CHAT CONVERTERS
    fun toChat(map: Map<String, Any>): Chat {
        val chatId = map["chatId"] as String
        val chatTitle = map["chatTitle"] as String
        val members = map["members"] as List<String>
        val lastMessage = map["lastMessage"] as String
        val timestamp = map["timestamp"] as Timestamp

        return Chat(
            chatId,
            chatTitle,
            members,
            lastMessage = lastMessage,
            timestamp = timestamp
        )
    }

    fun toChatFull(map: Map<String, Any>): Chat {
        val chatId = map["chatId"] as String
        val chatTitle = map["chatTitle"] as String
        val members = map["members"] as List<String>
        val memberFcmTokens = map["memberFcmTokens"] as List<String>
        val lastMessage = map["lastMessage"] as String
        val timestamp = map["timestamp"] as Timestamp
        val messages = toMessageList(map["messages"] as List<Map<String, Any>>).toMutableList()
        val rideId = map["rideId"] as String
        val chatStatus = ChatStatus.valueOf(map["chatStatus"] as String)

        return Chat(
            chatId,
            chatTitle,
            members,
            memberFcmTokens,
            lastMessage,
            timestamp,
            messages,
            rideId,
            chatStatus
        )
    }

    fun toChatFull(document: DocumentSnapshot): Chat {
        return Chat(
            chatId = document.getString("chatId"),
            chatTitle = document.getString("chatTitle"),
            members =  document.get("members") as List<String>,
            memberFcmTokens = document.get("memberFcmTokens") as List<String>,
            lastMessage = document.getString("lastMessage"),
            timestamp = document.getTimestamp("timestamp"),
            messages =toMessageList(document.get("messages") as List<Map<String, Any>>).toMutableList(),
            rideId = document.getString("rideId"),
            chatStatus = ChatStatus.valueOf(document.getString("chatStatus")!!)
        )
    }


    fun toChatHashMap(chat: Chat): HashMap<String, Any?> {
        val messageMapList = mutableListOf<HashMap<String, Any?>>()

        for(message in chat.messages!!) {
            messageMapList.add(toMessageHashmap(message))
        }

        return hashMapOf(
            "chatId" to chat.chatId,
            "chatTitle" to chat.chatTitle,
            "members" to chat.members,
            "memberFcmTokens" to chat.memberFcmTokens,
            "lastMessage" to chat.lastMessage,
            "timestamp" to chat.timestamp,
            "messages" to messageMapList,
            "rideId" to chat.rideId,
            "chatStatus" to chat.chatStatus,
        )
    }

    fun toChatHashMap(chat: Chat, lastMessage: String): HashMap<String, Any?> {
        return hashMapOf(
            "chatId" to chat.chatId,
            "members" to chat.members,
            "lastMessage" to lastMessage,
            "messages" to chat.messages,
            "rideId" to chat.rideId,
            "chatStatus" to chat.chatStatus
        )
    }


    // MESSAGE CONVERTERS
    private fun toMessage(map: Map<String, Any>): Message {
        val messageId = map["messageId"] as String
        val senderID = map["senderId"] as String
        val senderName = map["senderName"] as String
        val text = map["text"] as String
        val timestamp = map["timestamp"] as Timestamp
        val attachmentURL = map["attachmentURL"] as String?
        val readBy = map["readBy"] as List<String>
        val messageType = MessageType.valueOf(map["messageType"] as String)

        val photoUrl = if(map["photoUrl"] != null) {
            Uri.parse(map["photoUrl"] as String)
        } else {
            null
        }


        return Message(
            messageId,
            senderID,
            senderName,
            text,
            timestamp,
            attachmentURL,
            readBy,
            messageType,
            photoUrl
        )
    }

    fun toMessageList(mapList: List<Map<String, Any>>): List<Message> {
        val messageList = mutableListOf<Message>()

        for(map in mapList) {
            messageList.add(toMessage(map))
        }

        return messageList
    }

    private fun toMessageHashmap(message: Message): HashMap<String, Any?> {
        return hashMapOf(
            "messageId" to message.messageId,
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "text" to message.text,
            "timestamp" to Timestamp.now(),
            "readBy" to message.readBy,
            "attachmentURL" to message.attachmentURL,
            "messageType" to message.messageType,
            "photoUrl" to message.photoUrl?.toString()
        )
    }

    fun toMessageListHashmap(message: Message): HashMap<String, Any?> {
        return hashMapOf(
            "messageId" to message.messageId,
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "text" to message.text,
            "timestamp" to Timestamp.now(),
            "readBy" to message.readBy,
            "attachmentURL" to message.attachmentURL,
            "messageType" to message.messageType,
            "photoUrl" to message.photoUrl
        )
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


    // LATLNG CONVERTERS
    private fun toLatLng(latlngMap: Map<String, Any>): LatLng {
        val latitute = latlngMap["latitude"] as Double
        val longitude = latlngMap["longitude"] as Double

        return(LatLng(latitute, longitude))
    }

    fun toLatLngList(list: List<Map<String, Any>>): MutableList<LatLng> {
        val latLngList = mutableListOf<LatLng>()

        for(latlngMap in list) {
            latLngList.add(toLatLng(latlngMap))
        }

        return latLngList
    }



    // Dimension CONVERTERS
    fun toPixel(context: Context, dimensionInDp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dimensionInDp,
            context.resources.displayMetrics
        ).toInt()
    }


}