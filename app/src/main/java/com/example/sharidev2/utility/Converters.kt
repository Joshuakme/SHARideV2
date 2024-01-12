package com.example.sharidev2.utility

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.sharidev2.data.model.Address
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.DriverStatus
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Location
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.PassengerStatus
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.UserStatus
import com.google.android.gms.maps.model.LatLng
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class Converters {
    private val gson = Gson()


    // DATE & TIME Converters
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    // LocalTime converters
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let {
            LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME)
        }
    }


    // LOCATION converters
    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let {
            "${it.latitude},${it.longitude},${it.address}"
        }
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return locationString?.let {
            val parts = it.split(",")
            if (parts.size == 3) {
                Location(parts[0].toDouble(), parts[1].toDouble(), parts[2])
            } else {
                null
            }
        }
    }

    // LatLng converters
    @TypeConverter
    fun fromLatLng(latLng: LatLng?): String? {
        return latLng?.let {
            "${it.latitude},${it.longitude}"
        }
    }

    @TypeConverter
    fun toLatLng(latLngString: String?): LatLng? {
        return latLngString?.let {
            val parts = it.split(",")
            if (parts.size == 2) {
                LatLng(parts[0].toDouble(), parts[1].toDouble())
            } else {
                null
            }
        }
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

    // DriverStatus converters
    @TypeConverter
    fun fromDriverStatus(driverStatus: DriverStatus?): String? {
        return gson.toJson(driverStatus)
    }

    @TypeConverter
    fun toDriverStatus(driverStatusString: String?): DriverStatus? {
        return gson.fromJson(driverStatusString, DriverStatus::class.java)
    }

    // PassengerStatus converters
    @TypeConverter
    fun fromPassengerStatus(passengerStatus: PassengerStatus?): String? {
        return gson.toJson(passengerStatus)
    }

    @TypeConverter
    fun toPassengerStatus(passengerStatusString: String?): PassengerStatus? {
        return gson.fromJson(passengerStatusString, PassengerStatus::class.java)
    }

    // MutableList<PassengerStatus> converters
    @TypeConverter
    fun fromPassengerStatusList(passengerStatusList: MutableList<PassengerStatus>?): String? {
        return gson.toJson(passengerStatusList)
    }

    @TypeConverter
    fun toPassengerStatusList(passengerStatusListString: String?): MutableList<PassengerStatus>? {
        val type = object : TypeToken<MutableList<PassengerStatus>?>() {}.type
        return gson.fromJson(passengerStatusListString, type)
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

}