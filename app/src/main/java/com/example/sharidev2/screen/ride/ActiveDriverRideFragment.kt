package com.example.sharidev2.screen.ride

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ActiveRidePassengerImageAdapter
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.PolylineData
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.databinding.FragmentActiveDriverRideBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.GoogleMapUtils
import com.example.sharidev2.viewmodel.ActiveRideViewModel
import com.example.sharidev2.viewmodel.CurrentLocationViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.internal.PolylineEncoding
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder


class ActiveDriverRideFragment : Fragment() {
    private lateinit var binding: FragmentActiveDriverRideBinding
    private val activeRideViewModel: ActiveRideViewModel by viewModels()
    private val currentLocationViewModel: CurrentLocationViewModel by activityViewModels()

    private lateinit var googleMapFragment: SupportMapFragment

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val polylineList = mutableListOf<PolylineData>()
    private lateinit var context: Context

    private val googleMapUtils = GoogleMapUtils()
    private val mHandler: Handler = Handler()
    private lateinit var mRunnable: Runnable
    private val LOCATION_UPDATE_INTERVAL = 8000.toLong()

    private var isSosButtonLongPressed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_active_driver_ride,
            container,
            false
        )


        // Args
        try {
            val activeRide = arguments?.get("ride") as Ride

            activeRideViewModel.setActiveRide(activeRide)
        } catch (e: Exception) {
            Log.e("Booking Detail Fragment", e.message.toString())
        }

        context = if (isAdded) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }


        // ELEMENT VARIABLES
        googleMapFragment =
            childFragmentManager.findFragmentById(R.id.map_active_driver_ride_container) as SupportMapFragment


        setupMap()
        setupData()

        startUserLocationsRunnable()

        return binding.root
    }


    private fun setupMap() {
        googleMapFragment.getMapAsync { googleMap ->
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
            }

            currentLocationViewModel.currentLocation.observe(viewLifecycleOwner) { currentLocation ->
                if (currentLocation != null) {
                    val myLocationBtn = binding.cardActiveDriverRideMyLocationContainer

                    googleMapUtils.setupMapListeners(googleMap, currentLocation, myLocationBtn,
                        object : GoogleMapUtils.MyLocationButtonCallback {
                            override fun showMyLocationButton(show: Boolean) {
                                showMyLocationBtn(show)
                            }
                        }
                    )
                }
            }

            // Add User Markers into map
            activeRideViewModel.activeRide.observe(viewLifecycleOwner) { activeRide ->
                if (activeRide != null) {

                    val colorPrimary = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorPrimary)
                    val colorError = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorError)

                    // Origin
                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.origin.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorPrimary)
                    )

                    // Destination
                    GoogleMapUtils().addMarker(
                        googleMap,
                        activeRide.destination.geolocation!!,
                        CommonUtils().getLocationBitmapFromVector(requireContext(), colorError)
                    )


                    if (currentUser != null) {
                        // Driver
                        if (activeRide.driver.location != null) {
                            // Resolve the attribute to get the color value programmatically

                            val colorPrimaryDark = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorPrimaryDark)

                            val icon = CommonUtils().getLocationBitmapFromVector(
                                requireContext(),
                                colorPrimaryDark
                            )
                            GoogleMapUtils().addMarker(googleMap, activeRide.driver.location, icon)
                        }


                        // Passengers
                        activeRide.passengers.forEach() { passenger ->
                            if (passenger.location != null) {

                                // Add a marker to the map
                                if (passenger.userUid == currentUser.uid) {

                                }

                                GoogleMapUtils().moveMapCamera(googleMap, passenger.location)

                                Glide.with(requireContext())
                                    .asBitmap()
                                    .load(passenger.user?.photoUri) // Replace profilePictureUrl with the actual URL
                                    .transform(RoundedCornersTransformation(8, 2))
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {

                                            GoogleMapUtils().addOverlayToMap(
                                                googleMap,
                                                resource,
                                                passenger.location,
                                                30f,
                                                30f
                                            )
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Handle resource clearing if needed
                                        }
                                    })
                            }
                        }
                    }

                    GoogleMapUtils().calculateDirections(
                        requireContext(),
                        activeRide.origin.geolocation!!,
                        activeRide.destination.geolocation!!,
                        true
                    ) { result ->
                        if (result != null) {
                            Handler(Looper.getMainLooper()).post {
                                if (polylineList.isNotEmpty()) {
                                    for (polylineData in polylineList) {
                                        polylineData.polyline.remove()
                                    }
                                    polylineList.clear()
                                }
                                val routePathsList = mutableListOf<MutableList<LatLng>>()

                                for (route in result.routes) {
                                    val decodedPath =
                                        PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                                    val newDecodedPath = mutableListOf<LatLng>()

                                    for (latLng in decodedPath) {
                                        newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                                        Log.e(
                                            "GoogleMapUtils: Polyline",
                                            "Lat: ${latLng.lat}, Lng: ${latLng.lng}"
                                        )
                                    }


                                    val polyline: Polyline = googleMap.addPolyline(
                                        PolylineOptions().addAll(newDecodedPath).clickable(true)
                                    )


                                    polylineList.add(PolylineData(polyline, route.legs[0]))
                                    routePathsList.add(newDecodedPath)
                                }


                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    activeRideViewModel.addRoutePathList(routePathsList)
                                }
                            }
                        }
                    }
                }
            }

            googleMap.setOnPolylineClickListener {
                val colorSecondary = CommonUtils().getThemeColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSecondary
                )

                for (polylineData in polylineList) {
                    if (it.id == polylineData.polyline.id) {
                        polylineData.polyline.color = colorSecondary
                        polylineData.polyline.zIndex = 1f
                    } else {
                        polylineData.polyline.color = Color.DKGRAY
                        polylineData.polyline.zIndex = 0f
                    }
                }
            }
        }
    }

    private fun setupData() {
        val passengerPhotoImg = binding.imgActiveDriverRidePassengerPhoto
        val passengerName = binding.textActiveDriverRidePassengerName
        val passengerContact = binding.textActiveDriverRidePassengerPhone
        val passengersRecyclerView = binding.recyclerViewActiveDriverRidePassengers
        val pickUpDetailsName = binding.textActiveDriverRidePickUpName
        val pickUpDetailsDetailedAddress = binding.textActiveDriverRidePickUpDetailedAddress
        val destinationDetailsName = binding.textActiveDriverRideDestinationName
        val destinationDetailsDetailedAddress =
            binding.textActiveDriverRideDestinationDetailedAddress


        // Selected Passenger
        activeRideViewModel.activeDriverRideSelectedPassenger.observe(viewLifecycleOwner) { selectedPassenger ->
            val photoUri = selectedPassenger.user?.photoUri

            if (photoUri != null) {
                if (CommonUtils().isUrl(photoUri.toString())) {
                    Glide.with(context)
                        .load(photoUri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(passengerPhotoImg)
                } else {
                    passengerPhotoImg.setImageURI(photoUri)
                }
            }

            if (selectedPassenger.user != null) {
                passengerName.text = selectedPassenger.user!!.displayName
                passengerContact.text = selectedPassenger.user!!.phoneNumber
            }

            // OnCLickListener
            setupSelectedPassengerOnClickListener(selectedPassenger)
        }

        activeRideViewModel.activeRide.observe(viewLifecycleOwner) { activeRide ->
            if (activeRide != null) {
                // Passengers
                val adapter =
                    ActiveRidePassengerImageAdapter(requireContext(), activeRide.passengers,
                        object : ActiveRidePassengerImageAdapter.OnPassengerImageClickListener {
                            override fun OnPassengerImageClick(passenger: Passenger) {
                                googleMapFragment.getMapAsync { googleMap ->
                                    googleMapUtils.moveMapCamera(googleMap, passenger.location!!)
                                }
                                activeRideViewModel.setDriverActiveRideSelectedPassenger(passenger)
                            }
                        })
                passengersRecyclerView.adapter = adapter
                passengersRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)


                // Pick Up Details
                pickUpDetailsName.text = activeRide.origin.name
                pickUpDetailsDetailedAddress.text = activeRide.origin.detailAddress


                // Destination Details
                destinationDetailsName.text = activeRide.destination.name
                destinationDetailsDetailedAddress.text = activeRide.destination.detailAddress


                setupOnClickListener(activeRide)
            }
        }


    }

    private fun setupOnClickListener(activeRide: Ride) {
        val backBtn = binding.btnActiveDriverRideNavBack
        val rideInfoScrollLView = binding.svActiveDriverRideRideInfo
        val expandMapBtn = binding.imgBtnActiveRideExpandRideDetail
        val shareRideBtn = binding.btnActiveRideShareRide
        val sosCallBtn = binding.btnActiveRideSosCall
        val pickUpPassengerBtn = binding.btnActiveDriverDriverRidePickUpPassenger
        val dropOffPassengerBtn = binding.btnActiveDriverDriverRideDropOffPassenger
        val cancelRideBtn = binding.btnActiveDriverRideCancelBooking
        val completeRideBtn = binding.btnActiveDriverRideFinishBooking

        // Drawer Open Status
        var drawerOpen = false

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        expandMapBtn.setOnClickListener {
            // Expand the ride info segment
            val params = rideInfoScrollLView.layoutParams
            params.height =
                if (drawerOpen) resources.getDimensionPixelSize(R.dimen.ss_height_350dp) else ViewGroup.LayoutParams.MATCH_PARENT

            rideInfoScrollLView.layoutParams = params

            expandMapBtn.rotation = if (drawerOpen) 0f else 180f

            drawerOpen = !drawerOpen
        }

        shareRideBtn.setOnClickListener {
            //Share link to other app
            val activeRideLink = "sharide.com/active/${activeRide.id}"

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, activeRideLink)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // Handle SOS accordingly
        sosCallBtn.setOnTouchListener { view, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    isSosButtonLongPressed = true
                    mHandler.postDelayed(
                        sosLongPressRunnable,
                        3000
                    ) // Start checking after 3 seconds
                }

                MotionEvent.ACTION_UP -> {
                    isSosButtonLongPressed = false
                    Toast.makeText(
                        context,
                        "Please hold 3 seconds to activate emergency button",
                        Toast.LENGTH_SHORT
                    ).show()
                    mHandler.removeCallbacks(sosLongPressRunnable) // Stop checking if released before 3 seconds
                }
            }
            true
        }

        cancelRideBtn.setOnClickListener {
            // Prompt confirmation (Remind to charge RM3 for cancellation, RM5 fee after ride started 5 mins)
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Cancel this ride?")
                .setMessage("Are you sure you want to proceed? If you cancel your ride now, a cancellation fee of RM3 will be charged to your account.")
                .setPositiveButton("Cancel") { dialogInterface: DialogInterface, _: Int ->

                    lifecycleScope.launch(Dispatchers.IO) {
                        // Set passenger status to UserStatus.CANCELLED
                        // Set fare of that passenger to RM3 / RM5
                        activeRideViewModel.cancelRide()
                    }

                    dialogInterface.dismiss() // Dismiss the dialog
                }
                .setNegativeButton("Dismiss") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss() // Dismiss the dialog
                }
                .create()
        }

        val selectedPassenger = activeRideViewModel.activeDriverRideSelectedPassenger.value
        for (passenger in activeRide.passengers) {
            if(passenger.userUid == selectedPassenger?.userUid) {
                if(passenger.status == UserStatus.IN_VEHICLE) {
                    dropOffPassengerBtn.visibility = View.VISIBLE
                    pickUpPassengerBtn.visibility = View.INVISIBLE

                    dropOffPassengerBtn.setOnClickListener {
                        lifecycleScope.launch {
                            val response = activeRideViewModel.dropOffPassenger()
                            activeRide.passengers = activeRide.passengers.map { passenger ->
                                if(passenger.userUid == activeRideViewModel.activeDriverRideSelectedPassenger.value!!.userUid) {
                                    passenger.status = UserStatus.COMPLETED
                                }

                                passenger
                            }

                            when (response) {
                                Constants.FIREBASE_REQUEST_SUCCESS -> {
                                    Toast.makeText(context, "Passenger dropped up!", Toast.LENGTH_SHORT).show()
                                }

                                else -> {
                                    Toast.makeText(context, "Failed to drop up passenger!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                } else if(passenger.status != UserStatus.COMPLETED && passenger.status != UserStatus.CANCELED) {
                    pickUpPassengerBtn.visibility = View.VISIBLE
                    dropOffPassengerBtn.visibility = View.INVISIBLE

                    pickUpPassengerBtn.setOnClickListener {
                        lifecycleScope.launch {
                            val response = activeRideViewModel.pickUpPassenger()
                            activeRide.passengers = activeRide.passengers.map { passenger ->
                                if(passenger.userUid == activeRideViewModel.activeDriverRideSelectedPassenger.value!!.userUid) {
                                    passenger.status = UserStatus.IN_VEHICLE
                                }

                                passenger
                            }

                            when (response) {
                                Constants.FIREBASE_REQUEST_SUCCESS -> {
                                    Toast.makeText(context, "Passenger picked up!", Toast.LENGTH_SHORT).show()
                                }

                                else -> {
                                    Toast.makeText(context, "Failed to pick up passenger!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                } else {
                    dropOffPassengerBtn.visibility = View.INVISIBLE
                    pickUpPassengerBtn.visibility = View.INVISIBLE
                }
            }
        }

        completeRideBtn.setOnClickListener {
            lifecycleScope.launch {
                val response = activeRideViewModel.completeRide()

                when(response) {
                    Constants.FIREBASE_REQUEST_SUCCESS -> {
                        Toast.makeText(context, "Ride completed!", Toast.LENGTH_SHORT).show()

                        findNavController().popBackStack(R.id.bookingFragment, false)
                    }

                    else -> {
                        Toast.makeText(context, "Failed to complete ride!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSelectedPassengerOnClickListener(passenger: Passenger) {
        val callPassengerBtn = binding.btnActiveDriverRideCallPassenger
        val messagePassengerBtn = binding.btnActiveDriverRideMessagePassenger
        val pickUpPassengerBtn = binding.btnActiveDriverDriverRidePickUpPassenger
        val dropOffPassengerBtn = binding.btnActiveDriverDriverRideDropOffPassenger

        callPassengerBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)

            if (passenger.user?.phoneNumber != null) {
                intent.data = Uri.parse("tel:${passenger.user!!.phoneNumber}")
            }
        }

        messagePassengerBtn.setOnClickListener {
            // Navigate to message chat fragment
            if (passenger.user?.phoneNumber != null) {
                val messageIntent = Intent(Intent.ACTION_VIEW)
                val defaultMsg = "Hello ${passenger.user!!.displayName}, I would like to get in touch regarding my ride."
                val url =
                    "https://api.whatsapp.com/send?phone=${passenger.user!!.phoneNumber}&text=${
                        Uri.encode(defaultMsg)
                    }"
                messageIntent.data = Uri.parse(url)
                startActivity(messageIntent)
            }
        }

        val activeRide = activeRideViewModel.activeRide.value!!
        if(passenger.status == UserStatus.IN_VEHICLE) {
            dropOffPassengerBtn.visibility = View.VISIBLE
            pickUpPassengerBtn.visibility = View.INVISIBLE

            dropOffPassengerBtn.setOnClickListener {
                lifecycleScope.launch {
                    val response = activeRideViewModel.dropOffPassenger()
                    activeRide.passengers = activeRide.passengers.map { passenger ->
                        if(passenger.userUid == activeRideViewModel.activeDriverRideSelectedPassenger.value!!.userUid) {
                            passenger.status = UserStatus.COMPLETED
                        }

                        passenger
                    }

                    when (response) {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(context, "Passenger dropped off!", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(context, "Failed to drop off passenger!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        } else if(passenger.status != UserStatus.COMPLETED && passenger.status != UserStatus.CANCELED) {
            pickUpPassengerBtn.visibility = View.VISIBLE
            dropOffPassengerBtn.visibility = View.INVISIBLE

            pickUpPassengerBtn.setOnClickListener {
                lifecycleScope.launch {
                    val response = activeRideViewModel.pickUpPassenger()
                    activeRide.passengers = activeRide.passengers.map { passenger ->
                        if(passenger.userUid == activeRideViewModel.activeDriverRideSelectedPassenger.value!!.userUid) {
                            passenger.status = UserStatus.IN_VEHICLE
                        }

                        passenger
                    }

                    when (response) {
                        Constants.FIREBASE_REQUEST_SUCCESS -> {
                            Toast.makeText(context, "Passenger picked up!", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(context, "Failed to pick up passenger!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        } else {
            dropOffPassengerBtn.visibility = View.INVISIBLE
            pickUpPassengerBtn.visibility = View.INVISIBLE
        }
    }

    private fun sendMessage(phoneNumber: String, message: String) {
        // Check if Whatsapp is installed
        if (CommonUtils().isAppInstalled(context, "com.whatsapp")) {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=60$phoneNumber&text=${
                URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            }"

            intent.setPackage("com.whatsapp")
            intent.data = Uri.parse(url)

            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(intent)
                Toast.makeText(context, "Emergency Message Sent", Toast.LENGTH_SHORT).show()
            }
        } else {
            val uri = Uri.parse("smsto:$phoneNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
            }
            startActivity(intent)
            Toast.makeText(context, "Emergency Message Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndSendSms() {
        // Check Send SMS Permission
        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.SEND_SMS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            val userLocation = activeRideViewModel.userLocation.value

            val locationMessage = if (userLocation?.location != null) {
                "My current location is " + "(${userLocation.location.latitude}, ${userLocation.location.longitude}). "
            } else {
                ""
            }
            val emergencyMessage =
                "Emergency Alert: I'm in distress and need assistance. " + locationMessage + "Please come to my aid immediately. Thank you. [Generated by system]"


            activeRideViewModel.contactList.value?.forEach { contact ->
                sendMessage(contact.contactPhone!!, emergencyMessage)
            }

        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS), Constants.PERMISSIONS_REQUEST_SEND_SMS
            )
        }
    }

    private fun showMyLocationBtn(show: Boolean) {
        val myLocationBtn = binding.cardActiveDriverRideMyLocationContainer

        myLocationBtn.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startUserLocationsRunnable() {
        Log.d(
            ContentValues.TAG,
            "startUserLocationsRunnable: starting runnable for retrieving updated locations."
        )
        mHandler.postDelayed(Runnable {
            getUserLocation()
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL)
        }.also { mRunnable = it }, LOCATION_UPDATE_INTERVAL)
    }

    private fun stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable)
    }

    private fun getUserLocation() {
        if(isAdded) {
            activeRideViewModel.getCurrentUserLocation()
            activeRideViewModel.getUsersLocation()

            googleMapFragment.getMapAsync { googleMap ->
                activeRideViewModel.activeRideUserLocationList.observe(viewLifecycleOwner) { locationList ->
                    if (locationList != null) {
                        for (location in locationList) {
                            if (location.location != null) {
                                googleMap.clear()

                                if (location.user?.photoUri != null) {
                                    Glide.with(requireContext())
                                        .asBitmap()
                                        .load(location.user!!.photoUri.toString()) // Replace profilePictureUrl with the actual URL
                                        .transform(RoundedCornersTransformation(8, 2))
                                        .into(object : CustomTarget<Bitmap>() {
                                            override fun onResourceReady(
                                                resource: Bitmap,
                                                transition: Transition<in Bitmap>?
                                            ) {
                                                GoogleMapUtils().addOverlayToMap(
                                                    googleMap,
                                                    resource,
                                                    location.location,
                                                    30f,
                                                    30f
                                                )
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {
                                                // Handle resource clearing if needed
                                            }
                                        })
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    private val sosLongPressRunnable = Runnable {
        if (isSosButtonLongPressed) {
            // Button is pressed for 3 secondsDriver
            checkAndSendSms()
        }
    }

    override fun onResume() {
        super.onResume()

        startUserLocationsRunnable()
    }

    override fun onDetach() {
        super.onDetach()

        stopLocationUpdates()
    }

    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.PERMISSIONS_REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do nothing
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}