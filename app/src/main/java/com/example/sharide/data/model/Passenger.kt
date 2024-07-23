package com.example.sharide.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize


@Parcelize
data class Passenger (
    override val userUid: String? = null,
    override var user: User? = null,
    override val location: LatLng? = null,
    val origin: SearchLocation? = null,
    val destination: SearchLocation? = null,
    override var status: UserStatus = UserStatus.REQUESTED,
    var ridePrice: Double? = null,
    val requestedDateTime: com.google.firebase.Timestamp? = null
): RideParticipant(userUid, user, location, status), Parcelable
