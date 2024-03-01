import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentDrivingLicenseBinding
import com.example.sharidev2.firebase.FirebaseInitializer
import com.example.sharidev2.utility.FirebaseUtils
import com.example.sharidev2.viewmodel.LicenseUploadViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class DrivingLicenseFragment : Fragment() {
    private lateinit var binding: FragmentDrivingLicenseBinding
    private val licenseUploadViewModel: LicenseUploadViewModel by viewModels()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private val auth = FirebaseInitializer.firebaseAuth
    private val currentUser = auth.currentUser

    private lateinit var licenseFrontImageView: ImageView
    private lateinit var licenseBackImageView: ImageView
    private lateinit var uploadFrontButton: Button
    private lateinit var uploadBackButton: Button
    private lateinit var saveButton: MaterialCardView
    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null


    private var isFrontImage = true
    private val CAMERA_PERMISSION_CODE = 400

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrivingLicenseBinding.inflate(inflater, container, false)

        // Initialize views
        licenseFrontImageView = binding.licenseFront
        licenseBackImageView = binding.licenseBack
        uploadFrontButton = binding.btnUploadLicenseFront
        uploadBackButton = binding.btnUploadLicenseBack
        saveButton = binding.cardSaveLicenseCta

        val backButton = binding.btnBackDrivingLicense


        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_drivingLicenseFragment_to_personalInformationFragment)
        }

        initImageUri()

        // Observe ViewModel for image changes
        licenseUploadViewModel.frontImageUri.observe(viewLifecycleOwner, Observer { uri ->
            // Update front image view
            if(uri != null) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(licenseFrontImageView)
                } else {
                    licenseFrontImageView?.setImageURI(uri)
                }

            }
        })

        licenseUploadViewModel.backImageUri.observe(viewLifecycleOwner, Observer { uri ->
            // Update back image view
            if(uri != null) {

                if (isUrl(uri.toString())) {

                    //Toast.makeText(requireContext(), "Back Image: URL", Toast.LENGTH_SHORT).show()
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(licenseBackImageView)
                } else {
                    licenseBackImageView?.setImageURI(uri)
                }
            } else {
                Log.e("Driver License View Model", "KOPI AIS KOSONG")
            }
        })

        setupOnClickListeners()

        return binding.root
    }


    private fun setupOnClickListeners() {
        // Set click listeners
        uploadFrontButton?.setOnClickListener {
            isFrontImage = true
            checkCameraPermissionAndOpenCamera()
        }
        uploadBackButton?.setOnClickListener {
            isFrontImage = false
            checkCameraPermissionAndOpenCamera()
        }
        saveButton?.setOnClickListener {
            if(frontImageUri != null && backImageUri != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    saveToFirestore()
                }
            }
        }

    }


    private fun initImageUri() {
        frontImageUri = createImageUri("${currentUser!!.uid}_license_front")
        backImageUri = createImageUri("${currentUser!!.uid}_license_back")

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
                        licenseUploadViewModel.setFrontImageUri(frontImageUri)
                    } else {
                        licenseUploadViewModel.setBackImageUri(backImageUri)
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
            takePictureLauncher.launch(if(isFrontImage) frontImageUri else backImageUri)
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
                takePictureLauncher.launch(if(isFrontImage) frontImageUri else backImageUri)
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


//
//    // Function to check if the selected file is a PDF
//    private fun isPDF(context: Context, uri: Uri): Boolean {
//        return context.contentResolver.getType(uri)?.startsWith("application/pdf") ?: false
//    }
//
//    // Function to handle PDF file
//    private fun handlePDF(requestCode: Int, uri: Uri) {
//        if (uri.toString().endsWith(".pdf")) {
//            // PDF file selected
//            // Handle PDF file here, for example, you can display a message to the user
//            Toast.makeText(requireContext(), "PDF file selected: $uri", Toast.LENGTH_SHORT).show()
//        } else {
//            // Image file selected
//            if (requestCode == REQUEST_IMAGE_GALLERY_FRONT) {
//                // Set front image URI in ViewModel
//                frontImageUri = uri
//                licenseUploadViewModel.setFrontImageUri(frontImageUri)
//            } else {
//                // Set back image URI in ViewModel
//                backImageUri = uri
//                licenseUploadViewModel.setBackImageUri(backImageUri)
//            }
//        }
//    }
//
//

    private suspend fun saveToFirestore() {
        isLoading(true)

        if(frontImageUri != null && backImageUri != null) {
            val response = licenseUploadViewModel.addImagesToDB()

            when(response) {
                FirebaseUtils.SUCCESS -> {
                    isLoading(false)

                    Toast.makeText(
                        requireContext(),
                        "License data saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                FirebaseUtils.USER_NOT_AUTHENTICATED ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "User not authenticated. Please log in again.",Toast.LENGTH_SHORT).show()
                }

                FirebaseUtils.NOT_BELONG_USER ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "Please log in your account",Toast.LENGTH_SHORT).show()
                }

                FirebaseUtils.DATA_NOT_VALID ->{
                    isLoading(false)
                    Toast.makeText(requireContext(), "Please take your front and back driving license by image",Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            isLoading(false)
            Toast.makeText(requireContext(), "Please take image of your front and back driving license",Toast.LENGTH_SHORT).show()
        }

    }

    private fun isLoading(loading: Boolean) {
        val saveLicenseBtnText = binding.textSaveLicenseCta
        val loadingProgressBar = binding.progressBarSaveLicenseCta


        if(loading) {
            loadingProgressBar.visibility = View.VISIBLE
            saveLicenseBtnText.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.INVISIBLE
            saveLicenseBtnText.visibility = View.VISIBLE
        }
    }
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_FRONT = 101
        private const val REQUEST_IMAGE_CAPTURE_BACK = 102
        private const val REQUEST_IMAGE_GALLERY_FRONT = 201
        private const val REQUEST_IMAGE_GALLERY_BACK = 202
    }
}