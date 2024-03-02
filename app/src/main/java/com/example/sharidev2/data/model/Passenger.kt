package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Passenger (
    override val userUid: String? = null,
    override val user: User? = null,
    override val location: LatLng? = null,
    override val status: UserStatus = UserStatus.REQUESTED,
    val ridePrice: Double? = null
): RideParticipant(userUid, user, location, status)
