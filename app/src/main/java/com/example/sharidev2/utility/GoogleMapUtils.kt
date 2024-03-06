package com.example.sharidev2.utility

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.example.sharidev2.R
import com.example.sharidev2.data.model.DirectionsResponse
import com.example.sharidev2.data.model.Route
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.card.MaterialCardView
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
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
//    fun getRoutesAndDrawOnMap(
//        context: Context,
//        googleMap: GoogleMap,
//        origin: LatLng,
//        destination: LatLng,
//        waypoints: List<LatLng>
//    ) {
//        // Retrofit client setup
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://maps.googleapis.com/maps/api/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val service = retrofit.create(DirectionsService::class.java)
//
//        val originString = "${origin.latitude},${origin.longitude}"
//        val destinationString = "${destination.latitude},${destination.longitude}"
//        var waypointsString = ""
//        for(waypoint in waypoints) {
//            val waypointString = "${waypoint.latitude},${waypoint.longitude}"
//
//            waypointsString = waypointsString.plus("${waypointString}|")
//        }
//        waypointsString.dropLast(1) // Drop last character "|"
//
//
//
//        val call = service.getDirections(originString, destinationString, waypointsString, context.getString(R.string.map_id))
//        call.enqueue(object : Callback<DirectionsResponse> {
//            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
//                if (response.isSuccessful) {
//                    // 2. Receive and Parse the Response
//                    val directionsResponse = response.body()
//                    // Parse the response to extract route information
//                    val routes = directionsResponse?.routes ?: emptyList()
//
//                    // drawRoutes
//                    for (route in routes) {
//                        val polylinePoints = decodePolyline(route.polyline.points)
//                        val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
//                        options.addAll(polylinePoints)
//                        googleMap.addPolyline(options)
//                    }
//                } else {
//                    // Handle unsuccessful response
//                }
//            }
//
//            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                // Handle network errors
//            }
//        })
//    }

    fun calculateDirections(context: Context, googleMap: GoogleMap, origin: LatLng, destination: LatLng) {
        val geoApiContext = GeoApiContext.Builder()
            .apiKey(context.getString(R.string.google_api_key))
            .build()

        Log.d("GoogleMapUtils: Calculate Directions", "calculateDirections: calculating directions.")

        val directions = DirectionsApiRequest(geoApiContext)
        directions.alternatives(true)
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
                if (result != null) {
                    addPolylinesToMap(context, googleMap, result)

                    Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString())
                    Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration)
                    Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance)
                    Log.d(
                        TAG,
                        "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString()
                    )
                }
            }

            override fun onFailure(e: Throwable?) {
                Log.e("GoogleMapUtils: Calculate Directions", e?.message.toString())
            }
        })
    }


    fun addPolylinesToMap(context: Context, googleMap: GoogleMap, result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post(Runnable {
            Log.d(TAG, "run: result routes: " + result.routes.size)
            for (route in result.routes) {
                Log.d(TAG, "run: leg: " + route.legs[0].toString())
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath: MutableList<LatLng> = ArrayList()

                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {
                    newDecodedPath.add(
                        LatLng(
                            latLng.lat,
                            latLng.lng
                        )
                    )
                }

                val polyline: Polyline = googleMap.addPolyline(PolylineOptions().addAll(newDecodedPath))

                val typedValue = TypedValue()
                context.theme?.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
                val colorSecondary = typedValue.data

                polyline.color = Color.DKGRAY
                polyline.isClickable = true
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