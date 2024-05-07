package com.example.sharidev2.screen.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentAddVehicleDocBinding
import com.example.sharidev2.databinding.FragmentAddVehicleDocImgBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.VehicleDocImgViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AddVehicleDocImgFragment : Fragment() {
    private lateinit var binding: FragmentAddVehicleDocImgBinding
    private val vehicleDocImgViewModel: VehicleDocImgViewModel by viewModels()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private val auth = FirebaseClient.firebaseAuth
    private val currentUser = auth.currentUser

    private lateinit var registerCertImageView: ImageView
    private lateinit var insuranceImageView: ImageView
    private lateinit var roadtaxImageView: ImageView
    private lateinit var uploadCertButton: TextView
    private lateinit var uploadInsuranceButton: TextView
    private lateinit var uploadRoadtaxButton: TextView
    private lateinit var nextButton: MaterialCardView
    private lateinit var backButton: ImageButton
    private var registerCertImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null
    private var roadtaxImageUri: Uri? = null

    private var isRegisterCertImage = true
    private var isInsuracneImage = true
    private var isRoadtaxImage = true

    private val CAMERA_PERMISSION_CODE = 100


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddVehicleDocImgBinding.inflate(inflater, container, false)

        //Initialize views
        registerCertImageView = binding.imgVehicleRegisterCert
        insuranceImageView = binding.imgInsurance
        roadtaxImageView = binding.imgRoadtax
        uploadCertButton = binding.btnUploadVehicleRegisterCert
        uploadInsuranceButton = binding.btnUploadInsurance
        uploadRoadtaxButton = binding.btnUploadRoadtax
        nextButton = binding.cardNextVehicleDocImgCta
        backButton = binding.btnBackVehicleDocImg

        val backButton = binding.btnBackVehicleDocImg

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_addVehicleDocImgFragment_to_addVehicleDocFragment)
        }

        initImageUri()

        // Observe ViewModel for image changes
        vehicleDocImgViewModel.regisCertUri.observe(viewLifecycleOwner) { uri ->
            // Update vehicle registration certification image view
            if (uri != null && !uri.toString().isNullOrBlank()) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(registerCertImageView)
                } else {
                    registerCertImageView.setImageURI(uri)
                }
            }
        }

        vehicleDocImgViewModel.insuranceUri.observe(viewLifecycleOwner) { uri ->
            // Update vehicle insurance image view
            if (uri != null && !uri.toString().isNullOrBlank()) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(insuranceImageView)
                } else {
                    insuranceImageView.setImageURI(uri)
                }
            }
        }

        vehicleDocImgViewModel.roadtaxUri.observe(viewLifecycleOwner) { uri ->
            // Update vehicle insurance image view
            if (uri != null && !uri.toString().isNullOrBlank()) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(roadtaxImageView)
                } else {
                    roadtaxImageView.setImageURI(uri)
                }
            } else {
                Log.e("Vehicle Doc View Model", "Error in View Model kah")
            }
        }

        setupOnClickListeners()

        return binding.root
    }

    private fun setupOnClickListeners() {
        // Set click listeners
        uploadCertButton.setOnClickListener {
            isRegisterCertImage = true
            isInsuracneImage = false
            isRoadtaxImage = false
            checkCameraPermissionAndOpenCamera()
        }

        uploadInsuranceButton.setOnClickListener {
            isRegisterCertImage = false
            isInsuracneImage = true
            isRoadtaxImage = false
            checkCameraPermissionAndOpenCamera()
        }

        uploadRoadtaxButton.setOnClickListener {
            isRegisterCertImage = false
            isInsuracneImage = false
            isRoadtaxImage = true
            checkCameraPermissionAndOpenCamera()
        }

        nextButton.setOnClickListener {
            if (registerCertImageUri != null && insuranceImageUri != null && roadtaxImageUri != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val response = vehicleDocImgViewModel.addImagesToDB()
                }
                findNavController().navigate(R.id.action_addVehicleDocImgFragment_to_addVehicleImgFragment)
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_addVehicleDocImgFragment_to_addVehicleDocFragment)

        }
    }


    private fun initImageUri() {
        registerCertImageUri = createImageUri("${currentUser!!.uid}_register_cert")
        insuranceImageUri = createImageUri("${currentUser.uid}_insurance")
        roadtaxImageUri = createImageUri("${currentUser!!.uid}_roadtax")

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
        startActivityForResult(Intent.createChooser(intent, "Select Picture or PDF"),
            if (isRegisterCertImage) REQUEST_IMAGE_GALLERY_CERT
            else if (isInsuracneImage) REQUEST_IMAGE_GALLERY_INSURANCE
            else REQUEST_IMAGE_GALLERY_ROADTAX)
    }

    private fun registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if(success) {
                try{
                    if (isRegisterCertImage) {
                        vehicleDocImgViewModel.setRegisterCertImageUri(registerCertImageUri)
                    } else if (isInsuracneImage){
                        vehicleDocImgViewModel.setInsuranceImageUri(insuranceImageUri)
                    }
                    else{
                        vehicleDocImgViewModel.setRoadtaxImageUri(roadtaxImageUri)
                    }
                } catch (e: Exception) {
                    Log.e("Register Picture Launcher", e.message.toString())
                }
            } else {
                //Toast.makeText(requireContext(), "Camera Failed", Toast.LENGTH_SHORT).show()
                Log.e("Register Picture Launcher", "Vehicle Doc Image: Camera Failed")
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
            takePictureLauncher.launch(
                if(isRegisterCertImage) registerCertImageUri
                else if (isInsuracneImage) insuranceImageUri
                else roadtaxImageUri)
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
                takePictureLauncher.launch(
                    if(isRegisterCertImage) registerCertImageUri
                    else if (isInsuracneImage) insuranceImageUri
                    else roadtaxImageUri)
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


    private suspend fun saveToFirestore() {
        isLoading(true)

        if(registerCertImageUri != null && insuranceImageUri != null && roadtaxImageUri != null) {
            val response = vehicleDocImgViewModel.addImagesToDB()

            when(response) {
                Constants.FIREBASE_REQUEST_SUCCESS -> {
                    isLoading(false)

                    Toast.makeText(
                        requireContext(),
                        "Vehicle Doc Image saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "User not authenticated. Please log in again.",Toast.LENGTH_SHORT).show()
                }

                Constants.FIREBASE_REQUEST_NOT_BELONG_USER ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "Please log in your account",Toast.LENGTH_SHORT).show()
                }

                Constants.FIREBASE_REQUEST_DATA_NOT_VALID ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "Please take your front and back driving license by image",Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            isLoading(false)
            Toast.makeText(requireContext(), "Please take image of your vehicle documentations",Toast.LENGTH_SHORT).show()
        }

    }

    private fun isLoading(loading: Boolean) {
        val nextButton = binding.cardNextVehicleDocImgCta
        val loadingProgressBar = binding.progressBarNextVehicleDocImgCta


        if(loading) {
            loadingProgressBar.visibility = View.VISIBLE
            nextButton.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.INVISIBLE
            nextButton.visibility = View.VISIBLE
        }
    }



    companion object {
        private const val REQUEST_IMAGE_CAPTURE_CERT = 101
        private const val REQUEST_IMAGE_CAPTURE_INSURANCE = 102
        private const val REQUEST_IMAGE_CAPTURE_ROADTAX = 103
        private const val REQUEST_IMAGE_GALLERY_CERT = 201
        private const val REQUEST_IMAGE_GALLERY_INSURANCE = 202
        private const val REQUEST_IMAGE_GALLERY_ROADTAX = 203

    }
}