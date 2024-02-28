package com.example.sharidev2.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.room.TypeConverter
import com.example.sharidev2.data.model.Address
import com.example.sharidev2.data.model.Chat
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
import com.google.common.reflect.TypeToken
import com.google.firebase.Timestamp
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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
        val displayName = map["displayName"] as String
        val email = map["email"] as String
        val phoneNumber = map["phoneNumber"] as String
        val photoUrl = map["photoUrl"] as String

        val rideOptionMap = map["rideOption"] as Map<String, String>
        val driverGender = Gender.valueOf(rideOptionMap["driverGender"] as String)
        val vehicleType = VehicleType.valueOf(rideOptionMap["vehicleType"] as String)
        val petFriendly = rideOptionMap["petFriendly"] as Boolean
        val rideOption = RideOption(driverGender, vehicleType, petFriendly)

        val rating = map["rating"] as Float
        val savedAddresses = toSearchLocationList(map["savedAddresses"] as List<Map<String, Any>>).toMutableList()
        val gender = Gender.valueOf(map["gender"] as String)
        val joinedDate = map["joinedDate"] as Timestamp

        return User(
            uid,
            displayName,
            email,
            phoneNumber,
            Uri.parse(photoUrl),
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

    fun toPassenger(map: Map<String, Any>): Passenger {

        val userId = map["userId"] as String
        val locationMap = map["location"] as Map<String, Any>
        val latitude = (locationMap["lattitude"] as Long).toDouble()
        val longitude = (locationMap["longitude"] as Long).toDouble()
        val location = LatLng(latitude, longitude)

        return Passenger(userId, location)
    }

    fun toPassengerList(mapList: List<Map<String, Any>>): List<Passenger> {

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
           reviewList.add(toReview(map))
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

        val capacity = map["capacity"] as Int

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
        for (entry in map) {
            val userId = entry.key
            val statusString = entry.value

            // Convert string status to UserStatus enum
            val userStatus = UserStatus.valueOf(statusString)

            // Add to the map using userId as key and converted UserStatus as value
            passengerStatuses[userId] = userStatus
        }
        return passengerStatuses.toMap() // Convert to immutable map
    }



    // DATE & TIME Converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let {
            LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME)
        }
    }


    // LOCATION converters
    @TypeConverter
    fun fromSearchLocation(searchLocation: SearchLocation?): String? {
        return searchLocation?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toSearchLocation(value: String?): SearchLocation? {
        val type = object : TypeToken<SearchLocation>() {}.type
        return value?.let { Gson().fromJson(it, type) }
    }

    @TypeConverter
    fun fromLatLng(latLng: LatLng?): String? {
        return latLng?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toLatLng(value: String?): LatLng? {
        return value?.let { Gson().fromJson(it, LatLng::class.java) }
    }


    // User converters
    @TypeConverter
    fun fromUser(user: User?): String? {
        return gson.toJson(user)
    }

    @TypeConverter
    fun toUser(userString: String?): User? {
        return gson.fromJson(userString, User::class.java)
    }

    // Address converters
    @TypeConverter
    fun fromAddressList(addressList: MutableList<Address>?): String? {
        return gson.toJson(addressList)
    }

    @TypeConverter
    fun toAddressList(addressListString: String?): MutableList<Address>? {
        val type: Type = object : TypeToken<MutableList<Address>?>() {}.type
        return gson.fromJson(addressListString, type)
    }

    // Gender converters
    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return gender?.name
    }

    @TypeConverter
    fun toGender(genderString: String?): Gender? {
        return genderString?.let { Gender.valueOf(it) }
    }

    // Chat converters
    @TypeConverter
    fun fromChat(chat: Chat?): String? {
        return gson.toJson(chat)
    }

    @TypeConverter
    fun toChat(chatString: String?): Chat? {
        return gson.fromJson(chatString, Chat::class.java)
    }

    // List<String> converters
    @TypeConverter
    fun fromStringList(stringList: List<String>?): String? {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringListString: String?): List<String>? {
        val type = object : TypeToken<List<String>?>() {}.type
        return gson.fromJson(stringListString, type)
    }

    // MutableList<Message> converters
    @TypeConverter
    fun fromMessageList(messageList: MutableList<Message>?): String? {
        return gson.toJson(messageList)
    }

    @TypeConverter
    fun toMessageList(messageListString: String?): MutableList<Message>? {
        val type = object : TypeToken<MutableList<Message>?>() {}.type
        return gson.fromJson(messageListString, type)
    }



    // UserStatus converters
    @TypeConverter
    fun fromUserStatus(userStatus: UserStatus?): String? {
        return userStatus?.name
    }

    @TypeConverter
    fun toUserStatus(userStatusString: String?): UserStatus? {
        return userStatusString?.let { UserStatus.valueOf(it) }
    }

    // MutableList<User> converters
    @TypeConverter
    fun fromUserList(userList: MutableList<User>?): String? {
        return gson.toJson(userList)
    }

    @TypeConverter
    fun toUserList(userListString: String?): MutableList<User>? {
        val type = object : TypeToken<MutableList<User>?>() {}.type
        return gson.fromJson(userListString, type)
    }

    // Review converters
    @TypeConverter
    fun fromReviewList(reviewList: List<Review>?): String? {
        return gson.toJson(reviewList)
    }

    @TypeConverter
    fun toReviewList(reviewListString: String?): List<Review>? {
        val type: Type = object : TypeToken<List<Review>?>() {}.type
        return gson.fromJson(reviewListString, type)
    }

    @TypeConverter
    fun fromVehicle(vehicle: Vehicle): String {
        val gson = Gson()
        return gson.toJson(vehicle)
    }

    @TypeConverter
    fun fromVehicleType(vehicleType: VehicleType): String {
        return vehicleType.name
    }

    @TypeConverter
    fun toVehicleType(value: String): VehicleType {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromBitmapList(bitmapList: List<Bitmap>?): String {
        val gson = Gson()
        return gson.toJson(bitmapList)
    }

    @TypeConverter
    fun toBitmapList(value: String): List<Bitmap> {
        val gson = Gson()
        val type: Type = object : TypeToken<List<Bitmap>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}