package com.example.sharidev2.utility

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import com.example.sharidev2.R
import com.example.sharidev2.data.model.DirectionsResponse
import com.example.sharidev2.data.model.Route
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


class GoogleMapUtils {
    private val ZOOM_INDEX = 17.8f

    // MAP DRAWING
    fun addMarker(gMap: GoogleMap, location: LatLng, icon: BitmapDescriptor?) {
        gMap.addMarker(MarkerOptions().position(location).icon(icon))
    }

    fun drawRoute(googleMap: GoogleMap, routes: List<Route>) {
        // TODO: Draw route on the map

    }

    fun addOverlayToMap(googleMap: GoogleMap, bitmap: Bitmap, location: LatLng, width: Float, height: Float) {
        val overlay = googleMap.addGroundOverlay(
            GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(LatLng(location.latitude, location.longitude), width, height)
        ) as GroundOverlay
    }

    // MAP CAMERA
    private fun handleCameraMove(
        googleMap: GoogleMap,
        location: LatLng,
        callback: MyLocationButtonCallback
    ) {
        val currentCameraPosition = googleMap.cameraPosition.target

        if (!GoogleMapUtils().isMapOnCurrentLocation(currentCameraPosition, location)) {
            callback.showMyLocationButton(true)
        } else {
            callback.showMyLocationButton(false)
        }
    }

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


    // DIRECTION
    fun calculateDirections(context: Context, origin: LatLng, destination: LatLng, alternativeRoute: Boolean, callback: (DirectionsResult?) -> Unit) {
        val geoApiContext = GeoApiContext.Builder()
            .apiKey(context.getString(R.string.google_map_key))
            .build()


        val directions = DirectionsApiRequest(geoApiContext)
        directions.alternatives(alternativeRoute)
        directions.origin(
            com.google.maps.model.LatLng(
                origin.latitude,
                origin.longitude
            )
        )

        directions.destination(
            com.google.maps.model.LatLng(
                destination.latitude,
                destination.longitude
            )
        ).setCallback(object : PendingResult.Callback<DirectionsResult?> {
            override fun onResult(result: DirectionsResult?) {
               callback(result)
            }

            override fun onFailure(e: Throwable?) {
                Log.e("GoogleMapUtils: Calculate Directions", e?.message.toString())
                callback(null)
            }
        })
    }


    // MAP STATE
    fun setupMapListeners(
        googleMap: GoogleMap,
        location: LatLng,
        myLocationBtn: MaterialCardView,
        callback: MyLocationButtonCallback
    ) {
        googleMap.setOnCameraMoveListener {
            handleCameraMove(googleMap, location, callback)
        }

        myLocationBtn.setOnClickListener {
            GoogleMapUtils().moveMapCamera(googleMap, location)
        }
    }

    private fun isMapOnCurrentLocation(
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


    interface MyLocationButtonCallback {
        fun showMyLocationButton(show: Boolean)
    }

    // Retrofit service interface
    interface DirectionsService {
        @GET("directions/json")
        fun getDirections(
            @Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("waypoints") waypoints: String,
            @Query("key") apiKey: String
        ): Call<DirectionsResponse>
    }
}