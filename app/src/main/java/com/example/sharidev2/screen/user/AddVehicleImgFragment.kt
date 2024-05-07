package com.example.sharidev2.screen.user

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentAddVehicleImgBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.io.File
import android.Manifest
import com.example.sharidev2.databinding.FragmentAddVehicleDocBinding
import com.example.sharidev2.databinding.FragmentAddVehicleDocImgBinding
import com.example.sharidev2.viewmodel.VehicleDocViewModel

class AddVehicleImgFragment : Fragment() {
    private lateinit var vehicleImgBinding: FragmentAddVehicleImgBinding
    private lateinit var vehicleDocBinding: FragmentAddVehicleDocBinding
    private lateinit var vehicleDocImgBinding: FragmentAddVehicleDocImgBinding
    private val vehicleViewModel: VehicleDocViewModel by viewModels()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>



    private val auth = FirebaseClient.firebaseAuth
    private val currentUser = auth.currentUser

    private lateinit var vehicleFrontImageView: ImageView
    private lateinit var vehicleBackImageView: ImageView
    private lateinit var uploadVehicleFrontButton: TextView
    private lateinit var uploadVehicleBackButton: TextView
    private lateinit var saveButton: MaterialCardView
    private var vehicleFrontImageUri: Uri? = null
    private var vehicleBackImageUri: Uri? = null


    private var isFrontImage = true
    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vehicleImgBinding = FragmentAddVehicleImgBinding.inflate(inflater, container, false)

        // Initialize views
        vehicleFrontImageView = vehicleImgBinding.vehicleFront
        vehicleBackImageView = vehicleImgBinding.vehicleBack
        uploadVehicleFrontButton = vehicleImgBinding.btnUploadVehicleFront
        uploadVehicleBackButton = vehicleImgBinding.btnUploadVehicleBack
        saveButton = vehicleImgBinding.cardSaveVehicleImg

        val backButton = vehicleImgBinding.btnBackVehicleImg


        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_addVehicleImgFragment_to_addVehicleDocImgFragment)
        }

        initImageUri()

        // Observe ViewModel for image changes
        vehicleViewModel.vehicleFrontImageUri.observe(viewLifecycleOwner){ uri ->
            // Update front image view
            if(uri != null && !uri.toString().isNullOrBlank()) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(vehicleFrontImageView)
                } else {
                    vehicleFrontImageView.setImageURI(uri)
                }

            }
        }

        vehicleViewModel.vehicleBackImageUri.observe(viewLifecycleOwner, Observer { uri ->
            // Update back image view
            if((uri != null) && uri.toString().isNotBlank()) {
                if (isUrl(uri.toString())) {
                    //Toast.makeText(requireContext(), "Back Image: URL", Toast.LENGTH_SHORT).show()
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(vehicleBackImageView)
                } else {
                    vehicleBackImageView.setImageURI(uri)
                }
            } else {
                Log.e("Vehicle Doc View Model", "SOMETHING WRONG IN VEHICLE DOC VIEW MODEL")
            }
        })

        setupOnClickListeners()

        return vehicleImgBinding.root
    }


    private fun setupOnClickListeners() {
        // Set click listeners
        uploadVehicleFrontButton.setOnClickListener {
            isFrontImage = true
            checkCameraPermissionAndOpenCamera()
        }

        uploadVehicleBackButton.setOnClickListener {
            isFrontImage = false
            checkCameraPermissionAndOpenCamera()
        }



        //怎样save完它全部的东西哦， 还有VehicleID还没有给
        saveButton.setOnClickListener {
            if(isAllFieldValid()) {
                lifecycleScope.launch {
                    val response = vehicleViewModel.addVehicle()
                    val vehicleDocResponse = vehicleViewModel.addVehicleDoc(response.data)


                    // when(response) {
                    // Constants.FIREBASE_REQUEST_SUCCESS -> {
                    // Success message
//                            Toast.makeText(context, "Vehicle Documentation Submitted", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_addVehicleDocFragment_to_addVehicleDocImgFragment)
//                        }
//
//                        Constants.FIREBASE_REQUEST_FAILED -> {
//                            // Failed message
//                            Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show()
//                        }
//
//                        else -> {
//                            Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
//                        }
                }
            }
        }


    }


    private fun initImageUri() {
        vehicleFrontImageUri = createImageUri("${currentUser!!.uid}_vehicle_front")
        vehicleBackImageUri = createImageUri("${currentUser.uid}_vehicle_back")

        // Initialize ActivityResultLauncher for taking pictures
        registerPictureLauncher()
    }

    private fun createImageUri(imageName: String): Uri {
        val imageFile = File(requireActivity().applicationContext.filesDir, "${imageName}.jpg")

        return FileProvider.getUriForFile(
            requireActivity().applicationContext,
            "com.example.sharidev2.fileprovider",
            imageFile
        )
    }

    private fun chooseImageFromGallery() {
        val mimeTypes = arrayOf("image/*", "application/pdf")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(Intent.createChooser(intent, "Select Picture or PDF"), if (isFrontImage) REQUEST_IMAGE_GALLERY_FRONT else REQUEST_IMAGE_GALLERY_BACK)
    }

    private fun registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if(success) {
                try{
                    if (isFrontImage) {
                        vehicleViewModel.setVehicleFrontImageUri(vehicleFrontImageUri)
                    } else {
                        vehicleViewModel.setVehicleBackImageUri(vehicleBackImageUri)
                    }
                } catch (e: Exception) {
                    Log.e("Register Picture Launcher", e.message.toString())
                }
            } else {
                //Toast.makeText(requireContext(), "Camera Failed", Toast.LENGTH_SHORT).show()
                Log.e("Register Picture Launcher", "Driving License: Camera Failed")
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if(ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE)
        } else {
            takePictureLauncher.launch(if(isFrontImage) vehicleFrontImageUri else vehicleBackImageUri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(if(isFrontImage) vehicleFrontImageUri else vehicleBackImageUri)
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isUrl(imagePath: String): Boolean {
        return imagePath.startsWith("http://") || imagePath.startsWith("https://")
    }

    fun isUri(imagePath: String): Boolean {
        return !isUrl(imagePath) // Assume that if it's not a URL, it's a URI
    }


//    private suspend fun saveToFirestore() {
//        isLoading(true)
//
//        if(vehicleFrontImageUri != null && vehicleBackImageUri != null) {
//            //val response = vehicleViewModel.addImagesToDB()
//
//            when(response) {
//                Constants.FIREBASE_REQUEST_SUCCESS -> {
//                    isLoading(false)
//
//                    Toast.makeText(
//                        requireContext(),
//                        "Vehicle data saved successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED ->{
//                    isLoading(false)
//                    Toast.makeText(requireContext(), "User not authenticated. Please log in again.",Toast.LENGTH_SHORT).show()
//                }
//
//                Constants.FIREBASE_REQUEST_NOT_BELONG_USER ->{
//                    isLoading(false)
//                    Toast.makeText(requireContext(), "Please log in your account",Toast.LENGTH_SHORT).show()
//                }
//
//                Constants.FIREBASE_REQUEST_DATA_NOT_VALID ->{
//                    isLoading(false)
//                    Toast.makeText(requireContext(), "Please take your front and back part of vehicle by image",Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        } else {
//            isLoading(false)
//            Toast.makeText(requireContext(), "Please take image of your front and back part of vehicle",Toast.LENGTH_SHORT).show()
//        }
//
//    }

    private fun isLoading(loading: Boolean) {
        val saveLicenseBtnText = vehicleImgBinding.textSaveVehicleImg
        val loadingProgressBar = vehicleImgBinding.progressBarSaveVehicleImg


        if(loading) {
            loadingProgressBar.visibility = View.VISIBLE
            saveLicenseBtnText.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.INVISIBLE
            saveLicenseBtnText.visibility = View.VISIBLE
        }
    }



    private fun isAllFieldValid(): Boolean {
        val firstName = vehicleDocBinding.inputVehicleFirstName.text.toString()
        val lastName = vehicleDocBinding.inputVehicleLastName.text.toString()
        val vehicleBrand = vehicleDocBinding.inputVehicleBrand.text.toString()
        val vehicleColor = vehicleDocBinding.inputVehicleColor.text.toString()
        val vehicleCapacity = vehicleDocBinding.inputVehicleCapacity.text.toString()
        val carPlate = vehicleDocBinding.inputCarPlate.text.toString()
        val selectedVehicleType = vehicleDocBinding.spinnerVehicleType.selectedItem.toString()
        val vehicleModel = vehicleDocBinding.inputVehicleModel.text.toString()
        val manufactureDate = vehicleDocBinding.dateManufacture.text.toString()

        val vehicleCert = vehicleDocImgBinding.imgVehicleRegisterCert
        val vehicleInsurance = vehicleDocImgBinding.imgInsurance
        val vehicleRoadtax = vehicleDocImgBinding.imgRoadtax

        val vehicleFront = vehicleImgBinding.vehicleFront
        val vehicleBack = vehicleImgBinding.vehicleBack

        if (firstName.isNotEmpty() && lastName.isNotEmpty() && vehicleBrand.isNotEmpty() &&
            vehicleColor.isNotEmpty() && vehicleCapacity.isNotEmpty() && carPlate.isNotEmpty() &&
            selectedVehicleType.isNotEmpty() && vehicleModel.isNotEmpty() && manufactureDate.isNotEmpty() &&
            vehicleCert != null && vehicleInsurance != null && vehicleRoadtax != null &&
            vehicleFront != null && vehicleBack != null
        ) {

            return true
        } else {

            return false
            Toast.makeText(
                context,
                "Please make sure all the documents are provided",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE_FRONT = 101
        private const val REQUEST_IMAGE_CAPTURE_BACK = 102
        private const val REQUEST_IMAGE_GALLERY_FRONT = 201
        private const val REQUEST_IMAGE_GALLERY_BACK = 202
    }

}