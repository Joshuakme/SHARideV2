package com.example.sharidev2


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sharidev2.databinding.ActivityMainBinding
import com.example.sharidev2.service.ConnectivityService
import com.example.sharidev2.utility.Constants.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavContainer: LinearLayout

    private val currentLocationViewModel: CurrentLocationViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Variables
        val navHostFragment = supportFragmentManager.findFragmentById(binding.fragmentContainerMain.id) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigation
        bottomNavContainer = binding.llBottomNavigation


        bottomNav.setupWithNavController(navController)

        Places.initialize(applicationContext, "AIzaSyBTPyaUpFhz9GMIpFq40zi9cZlCeZZZtQc")

        lifecycleScope.launch(Dispatchers.IO) {
            UserClient.setCurrentUser(FirebaseClient.firebaseAuth.currentUser?.uid ?: "")
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(locationPermissionGranted) {
            getLastKnownLocation()
        } else {
            getLocationPermission()
        }

        // Check Network Connection
        startService(Intent(this, ConnectivityService::class.java))


//        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
//            window.statusBarColor = Color.TRANSPARENT
//        }
    }


    // UTILITIES METHODS
    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun setBottomNavVisible(visible: Boolean) {
        bottomNavContainer.visibility = if(visible) View.VISIBLE else View.GONE
    }

    fun resetBottomNavPosition() {
        bottomNavContainer.translationY = 0f
    }


    private fun getLastKnownLocation() {
        // If no permission granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a LocationRequest object
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the priority to high accuracy
            interval = 10000  // Update location (10 seconds)
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Get the latest location from the result
                val latestLocation = locationResult.lastLocation

                val currentLocation = LatLng(latestLocation.latitude, latestLocation.longitude)

                currentLocationViewModel.setLocation(currentLocation)

                fusedLocationClient.removeLocationUpdates(this)
            }
        }, null)
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;

            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                } else {
                    //Toast.makeText(applicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}