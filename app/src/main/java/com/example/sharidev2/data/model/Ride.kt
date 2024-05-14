package com.example.sharidev2.data.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ride (
    val id: String? = null,
    val origin: SearchLocation = SearchLocation(),
    val destination: SearchLocation = SearchLocation(),
    val datetime: Timestamp = Timestamp.now(),
    val driver: Driver = Driver(),
    var passengers: List<Passenger> = mutableListOf(),
    var rideStatus: RideStatus = RideStatus.CREATED,
    var startTime: Timestamp? = null,
    var completeTime: Timestamp? = null,
    val availableSeats: Int = 0,
    val reviews: List<Review> = mutableListOf(),
    val chat:  Chat? = null,
    val chatId: String? = null,
    val completedRoute: MutableList<LatLng>? = mutableListOf(),
    val fareList: List<Pair<Double, Double>>? = null,
    val createdAt: Timestamp
) : Parcelable {
    companion object : Parceler<Ride> {
        override fun Ride.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeParcelable(origin, flags)
            parcel.writeParcelable(destination, flags)
            parcel.writeSerializable(datetime.seconds * 1000L + datetime.nanoseconds / 1000000)
            parcel.writeParcelable(driver, flags)
            parcel.writePassengerList(passengers, flags)
            parcel.writeString(rideStatus.name)
            parcel.writeSerializable(null)
            parcel.writeSerializable((completeTime?.seconds?: 0) * 1000L + (completeTime?.nanoseconds?: 0) / 1000000)
            parcel.writeInt(availableSeats)
            parcel.writeReviewList(reviews, flags)
            parcel.writeParcelable(chat, flags)
            parcel.writeParcelable(completedRoute, flags)
            parcel.writeSerializable(createdAt.seconds * 1000L + createdAt.nanoseconds / 1000000)
        }


        override fun create(parcel: Parcel): Ride = Ride(
            id = parcel.readString(),
            origin = parcel.readParcelable(SearchLocation::class.java.classLoader)!!,
            destination = parcel.readParcelable(SearchLocation::class.java.classLoader)!!,
            datetime = parcel.readSerializable() as Timestamp,
            driver = parcel.readParcelable(Driver::class.java.classLoader)!!,
            passengers = parcel.readPassengerList(Passenger::class.java.classLoader),
            rideStatus = RideStatus.valueOf(parcel.readString()!!),
            startTime = parcel.readSerializable() as Timestamp?,
            completeTime = parcel.readSerializable() as Timestamp?,
            availableSeats = parcel.readInt(),
            reviews = parcel.readReviewList(Review::class.java.classLoader),
            chat = parcel.readParcelable(Chat::class.java.classLoader),
            completedRoute = parcel.readParcelable(LatLng::class.java.classLoader),
            createdAt = parcel.readSerializable() as Timestamp
        )
    }
}

private fun Parcel.writeSearchLocationList(locations: List<SearchLocation>?, flags: Int) {
    writeInt(locations?.size ?: 0) // Write the size of the list

    locations?.forEach { location ->
        writeParcelable(location, flags) // Write each SearchLocation object
    }
}

private fun Parcel.readSearchLocationList(classLoader: ClassLoader): List<SearchLocation> {
    val size = readInt() // Read the size of the list

    val locations = mutableListOf<SearchLocation>()

    repeat(size) {
        locations.add(readParcelable(classLoader)!!) // Read each SearchLocation object
    }

    return if (size > 0) locations else emptyList()
}


private fun Parcel.writePassengerList(passengers: List<Passenger>?, flags: Int) {
    writeInt(passengers?.size ?: 0) // Write the size of the list

    passengers?.forEach { passenger ->
        writeParcelable(passenger, flags) // Write each SearchLocation object
    }
}

private fun Parcel.readPassengerList(classLoader: ClassLoader): List<Passenger> {
    val size = readInt() // Read the size of the list

    val passengers = mutableListOf<Passenger>()

    repeat(size) {
        passengers.add(readParcelable(classLoader)!!) // Read each SearchLocation object
    }

    return if (size > 0) passengers else emptyList()
}


private fun Parcel.writeReviewList(reviews: List<Review>?, flags: Int) {
    writeInt(reviews?.size ?: 0) // Write the size of the list

    reviews?.forEach { review ->
        writeParcelable(review, flags) // Write each SearchLocation object
    }
}

private fun Parcel.readReviewList(classLoader: ClassLoader): List<Review> {
    val size = readInt() // Read the size of the list

    val reviews = mutableListOf<Review>()

    repeat(size) {
        reviews.add(readParcelable(classLoader)!!) // Read each SearchLocation object
    }

    return if (size > 0) reviews else emptyList()
}


private fun Parcel.writeParcelable(completedRoute: MutableList<LatLng>?, flags: Int) {
// Write the size of the list to the Parcel
    writeInt(completedRoute?.size ?: -1)
    // Write each LatLng object individually into the Parcel
    completedRoute?.forEach { latLng ->
        writeDouble(latLng.latitude)
        writeDouble(latLng.longitude)
    }
}
