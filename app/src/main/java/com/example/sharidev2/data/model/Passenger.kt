package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Passenger (
    override val userUid: String? = null,
    override val location: LatLng? = null,
    override val status: UserStatus = UserStatus.REQUESTED,
    val ridePrice: Int? = null
): RideParticipant(userUid, location, status)
