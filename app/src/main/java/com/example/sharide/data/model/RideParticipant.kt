package com.example.sharide.data.model

import com.google.android.gms.maps.model.LatLng

open class RideParticipant(
    open val userUid: String? = null,
    open val user: User? = null,
    open val location: LatLng? = null,
    open val status: UserStatus = UserStatus.REQUESTED
)