package com.example.sharide.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class Driver(
    override val userUid: String? = null,
    override var user: User? = null,
    override val location: LatLng? = null,
    override var status: UserStatus = UserStatus.REQUESTED,
    val vehicle: Vehicle? = null, // Additional driver-specific property
) : RideParticipant(userUid, user, location, status), Parcelable