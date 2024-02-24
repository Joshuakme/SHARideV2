package com.example.sharidev2.utility

import android.content.Context
import android.location.Location
import android.os.AsyncTask
import com.example.sharidev2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GoogleMapUtils {
    private val ZOOM_INDEX = 17.8f

    // MAP DRAWING
    fun addMarker(gMap: GoogleMap, location: LatLng, icon: BitmapDescriptor?) {
        gMap.addMarker(MarkerOptions().position(location).icon(icon))
    }

    fun drawRoute(gMap: GoogleMap, origin: LatLng?, destination: LatLng?) {
        // TODO: Draw route on the map
    }


    // MAP CAMERA
    fun moveMapCamera(gMap: GoogleMap, location: LatLng) {
        gMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                ZOOM_INDEX
            )
        )
    }

    fun updateMapZoomAndCamera(
        context: Context,
        gMap: GoogleMap,
        origin: LatLng?,
        destination: LatLng?
    ) {
        if (origin != null && destination != null) {
            // If both origin and destination are available, adjust camera to show both
            val bounds = LatLngBounds.Builder().apply {
                include(origin)
                include(destination)
            }.build()
            val padding =
                context.resources.getDimensionPixelSize(R.dimen.ss_map_padding) // Define your padding
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            gMap.moveCamera(cameraUpdate)
        } else if (origin != null) {
            // If only origin is available, move camera to origin
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, ZOOM_INDEX))
        } else if (destination != null) {
            // If only destination is available, move camera to destination
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, ZOOM_INDEX))
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