package com.example.sharidev2.utility

import android.content.Context
import android.location.Location
import com.example.sharidev2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class GoogleMapUtils {
    private val ZOOM_INDEX = 17.8f

    // MAP DRAWING
    fun addMarker(mMap: GoogleMap, location: LatLng?, icon: BitmapDescriptor?) {
        mMap.clear() // Clear previous markers

        location?.let {
            mMap.addMarker(MarkerOptions().position(it).icon(icon))
        }
    }
    fun drawRoute(mMap: GoogleMap, origin: LatLng?, destination: LatLng?) {
        if (origin != null && destination != null) {
            // TODO: Draw route from origin to destination
            // You need to implement this part using Google Maps Directions API
        }
    }

    fun updateMapZoomAndCamera(context: Context, mMap: GoogleMap, origin: LatLng?, destination: LatLng?) {
        if (origin != null && destination != null) {
            // If both origin and destination are available, adjust camera to show both
            val bounds = LatLngBounds.Builder().apply {
                include(origin)
                include(destination)
            }.build()
            val padding = context.resources.getDimensionPixelSize(R.dimen.ss_map_padding) // Define your padding
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.moveCamera(cameraUpdate)
        } else if (origin != null) {
            // If only origin is available, move camera to origin
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, ZOOM_INDEX))
        } else if (destination != null) {
            // If only destination is available, move camera to destination
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, ZOOM_INDEX))
        }
    }


    // MAP STATE
    fun isMapOnCurrentLocation(
        currentCameraPosition: LatLng,
        currentLocation: LatLng
    ): Boolean {
        val locationCamera = Location("Camera")
        val locationUser = Location("User")

        locationCamera.latitude = currentCameraPosition.latitude
        locationCamera.longitude = currentCameraPosition.longitude

        locationUser.latitude = currentLocation.latitude
        locationUser.longitude = currentLocation.longitude

        val distance = locationCamera.distanceTo(locationUser)

        return distance < 0.03f
    }
}