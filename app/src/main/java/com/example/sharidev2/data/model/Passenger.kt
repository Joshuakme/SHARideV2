package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Passenger (
    override val userUid: String? = null,
    override var user: User? = null,
    override val location: LatLng? = null,
    val origin: SearchLocation? = null,
    val destination: SearchLocation? = null,
    override val status: UserStatus = UserStatus.REQUESTED,
    var ridePrice: Double? = null
): RideParticipant(userUid, user, location, status)
