package com.example.sharidev2.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Ride (
    val id: String? = null,
    val origin: SearchLocation = SearchLocation(),
    val destination: SearchLocation = SearchLocation(),
    val waypoints: MutableMap<String, SearchLocation>? = mutableMapOf(),
    val datetime: Timestamp = Timestamp.now(),
    val driver: Driver = Driver(),
    val passengers: MutableMap<String, Passenger> = mutableMapOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val availableSeats: Int = 0,
    val reviews: MutableMap<String, Review> = mutableMapOf(),
    val chat:  Chat? = Chat(),
    val completedRoute: MutableList<LatLng>? = null,
    val createdAt: Timestamp
) : Parcelable {
    companion object : Parceler<Ride> {
        override fun Ride.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeParcelable(origin, flags)
            parcel.writeParcelable(destination, flags)
            parcel.writeMap(waypoints)
            parcel.writeSerializable(datetime.seconds * 1000L + datetime.nanoseconds / 1000000)
            parcel.writeParcelable(driver, flags)
            parcel.writeMap(passengers)
            parcel.writeString(rideStatus.name)
            parcel.writeSerializable(null)
            parcel.writeSerializable((completeTime?.seconds?: 0) * 1000L + (completeTime?.nanoseconds?: 0) / 1000000)
            parcel.writeInt(availableSeats)
            parcel.writeMap(reviews)
            parcel.writeParcelable(chat, flags)
            parcel.writeParcelable(completedRoute, flags)
            parcel.writeSerializable(createdAt.seconds * 1000L + createdAt.nanoseconds / 1000000)
        }



        override fun create(parcel: Parcel): Ride = Ride(
            parcel.readString(),
            parcel.readParcelable(SearchLocation::class.java.classLoader)!!,
            parcel.readParcelable(SearchLocation::class.java.classLoader)!!,
            parcel.readHashMap(SearchLocation::class.java.classLoader) as MutableMap<String, SearchLocation>?,
            parcel.readSerializable() as Timestamp,
            parcel.readParcelable(Driver::class.java.classLoader)!!,
            parcel.readHashMap(Passenger::class.java.classLoader) as MutableMap<String, Passenger>,
            RideStatus.valueOf(parcel.readString()!!),
            parcel.readSerializable() as Timestamp?,
            parcel.readSerializable() as Timestamp?,
            parcel.readInt(),
            parcel.readHashMap(Review::class.java.classLoader) as MutableMap<String, Review>,
            parcel.readParcelable(Chat::class.java.classLoader),
            parcel.readParcelable(LatLng::class.java.classLoader),
            parcel.readSerializable() as Timestamp
        )
    }
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
