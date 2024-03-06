package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Driver(
    override val userUid: String? = null,
    override var user: User? = null,
    override val location: LatLng? = null,
    override val status: UserStatus = UserStatus.REQUESTED,
    val vehicle: Vehicle? = null, // Additional driver-specific property
) : RideParticipant(userUid, user, location, status)