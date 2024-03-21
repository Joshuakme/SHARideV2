package com.example.sharidev2


import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.adapter.MessageAdapter
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.databinding.ActivityMainBinding
import com.example.sharidev2.service.LocationService
import com.example.sharidev2.service.NetworkService
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.sharidev2.utility.Constants.Companion.PERMISSIONS_REQUEST_POST_NOTIFICATION
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.example.sharidev2.viewmodel.ChatViewModel
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.example.sharidev2.viewmodel.SharedCurrentUserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavContainer: LinearLayout

    private val currentLocationViewModel: CurrentLocationViewModel by viewModels()
    private val currentUserViewModel: SharedCurrentUserViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var postNotificationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Variables
        val navHostFragment = supportFragmentManager.findFragmentById(binding.fragmentContainerMain.id) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigation
        bottomNavContainer = binding.llBottomNavigation


        bottomNav.setupWithNavController(navController)


        Places.initialize(applicationContext, getString(R.string.google_api_key))

        lifecycleScope.launch(Dispatchers.IO) {
            UserClient.setCurrentUser(FirebaseClient.firebaseAuth.currentUser?.uid ?: "")
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(locationPermissionGranted) {
            getLastKnownLocation()
        } else {
            getLocationPermission()
        }

        if(postNotificationPermissionGranted) {
            // Do nothing
        } else {
            getPostNotificationPermission()
        }

        // Check Network Connection
        startNetworkService()

        // Set up notification listener
        navigateToFragmentFromNotification()

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
        getFCMToken()
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

        fusedLocationClient.lastLocation
            .addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    val location = task.result

                    if(location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)

                        currentLocationViewModel.setLocation(currentLocation)
                    }
                }
            }

        startLocationService()
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

            PERMISSIONS_REQUEST_POST_NOTIFICATION -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    postNotificationPermissionGranted = true
                } else {
                    // Do nothing
                }
            }
        }
    }


    // SERVICES
    private fun startNetworkService() {
        if(!isNetworkServiceRunning()) {
            val serviceIntent = Intent(this, NetworkService::class.java)

            startService(serviceIntent)
        }
    }

    private fun isNetworkServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.example.sharidev2.service.NetworkService" == service.service.className) {
                Log.d(TAG, "isNetworkServiceRunning: network service is already running.")
                return true
            }
        }
        Log.d(TAG, "isNetworkServiceRunning: network service is not running.")
        return false
    }


    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val serviceIntent = Intent(this, LocationService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(postNotificationPermissionGranted) {
                    startForegroundService(serviceIntent)
                } else {
                    getPostNotificationPermission()
                }
            } else {
                startService(serviceIntent)
            }
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.example.sharidev2.service.LocationService" == service.service.className) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.")
                return true
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.")
        return false
    }

    private fun getFCMToken() {
        FirebaseClient.firebaseMessaging.token.addOnCompleteListener {task ->
            if(!task.isSuccessful) {
                Log.e(TAG, "Fetching FCM registration token failed", task.exception)
            }

            // Get new FCM registration token
            val token = task.result

            currentUserViewModel.setFcmToken(token)

            // Log and toast
            Log.e(TAG, "FirebaseMsg Token: $token")
        }
    }


    private fun getPostNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            postNotificationPermissionGranted = true
            // do nothing
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSIONS_REQUEST_POST_NOTIFICATION)
        }
    }

    private fun navigateToFragmentFromNotification() {
        val fragmentTag = intent.getStringExtra("fragment")
        if (fragmentTag != null) {
            // Navigate to the specified fragment
            val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_main, fragment)
                    .commit()
            }
        }
    }

//    private fun startListeningToChatUpdates() {
//        chatViewModel.startListeningForChatUpdates(chat.chatId!!, object: (Chat?) -> Unit {
//            override fun invoke(latestChat: Chat?) {
//                if(latestChat != null) {
//                    // Send notification
//                    CommonUtils().sendMessageNotification(applicationContext, latestChat)
//
//                    chatViewModel.setActiveChat(latestChat)
//
//                    // TODO: Set Unread Badge
//                } else {
//                    findNavController().navigate(R.id.action_chatFragment_to_messagesFragment)
//                }
//            }
//        })
//    }

    // TODO: Set up notification for the below
    // TODO: Set up listeners for Chat Updates
    // TODO: Set up listeners for Ride Status Updates (Passenger -> "$DriverName accepted your request",
    //  Driver -> "$PassengerName requested to join your ride to $PlaceName")



    override fun onResume() {
        super.onResume()

        if(locationPermissionGranted) {
            getLastKnownLocation()
        } else {
            getLocationPermission()
        }
    }
}