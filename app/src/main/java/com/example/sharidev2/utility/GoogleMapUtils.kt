package com.example.sharidev2.utility

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import com.example.sharidev2.R
import com.example.sharidev2.data.model.DirectionsResponse
import com.example.sharidev2.data.model.Route
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.card.MaterialCardView
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
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

    fun drawRoute(googleMap: GoogleMap, routes: List<Route>) {
        // TODO: Draw route on the map

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
    fun getRoutesAndDrawOnMap(
        context: Context,
        googleMap: GoogleMap,
        origin: LatLng,
        destination: LatLng,
        waypoints: List<LatLng>
    ) {
        // Retrofit client setup
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(DirectionsService::class.java)

        val originString = "${origin.latitude},${origin.longitude}"
        val destinationString = "${destination.latitude},${destination.longitude}"
        var waypointsString = ""
        for(waypoint in waypoints) {
            val waypointString = "${waypoint.latitude},${waypoint.longitude}"

            waypointsString = waypointsString.plus("${waypointString}|")
        }
        waypointsString.dropLast(1) // Drop last character "|"



        val call = service.getDirections(originString, destinationString, waypointsString, context.getString(R.string.map_id))
        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    // 2. Receive and Parse the Response
                    val directionsResponse = response.body()
                    // Parse the response to extract route information
                    val routes = directionsResponse?.routes ?: emptyList()

                    // drawRoutes
                    for (route in routes) {
                        val polylinePoints = decodePolyline(route.polyline.points)
                        val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
                        options.addAll(polylinePoints)
                        googleMap.addPolyline(options)
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                // Handle network errors
            }
        })
    }

    fun decodePolyline(polylineString: String): List<LatLng> {
        return PolyUtil.decode(polylineString)
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
            true
        }

        myLocationBtn.setOnClickListener {
            GoogleMapUtils().moveMapCamera(googleMap, location)

            true
        }
    }

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