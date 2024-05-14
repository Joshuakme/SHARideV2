package com.example.sharidev2.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


class LocationService: Service() {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val currentLocationViewModel = CurrentLocationViewModel()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val CHANNEL_ID = "my_channel_01"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "My Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!.createNotificationChannel(
            channel
        )
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS is running")
            .setContentText("").build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: called.")
        getLocation()
        return START_NOT_STICKY
    }

    private fun getLocation() {
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        val locationRequestHighAccuracy = LocationRequest()
        locationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequestHighAccuracy.setInterval(UPDATE_INTERVAL)
        locationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL)


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "getLocation: stopping the location service.")
            stopSelf()
            return
        }

        fusedLocationClient!!.requestLocationUpdates(locationRequestHighAccuracy, object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Get the latest location from the result
                val location = locationResult.lastLocation

                val currentLocation = LatLng(location.latitude, location.longitude)

                if(currentUser != null) {

                    currentLocationViewModel.setLocation(currentLocation)
                } else {
                    //stopSelf()
                }
            }
        }, Looper.myLooper())   // Looper.myLooper tells this to repeat forever until thread is destroyed
    }




    companion object {
        private const val TAG = "LocationService"
        private const val UPDATE_INTERVAL = (8 * 1000 /* 4 secs */).toLong()
        private const val FASTEST_INTERVAL: Long = 5000 /* 2 sec */
    }

}