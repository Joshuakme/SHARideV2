package com.example.sharidev2.screen.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentPersonalInformationBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.PersonalInfoViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth


class PersonalInformationFragment : Fragment() {
    // Global Variables Init
    private lateinit var binding: FragmentPersonalInformationBinding
    private lateinit var viewModel: PersonalInfoViewModel
    private val personalInformationViewModel: PersonalInfoViewModel by viewModels()
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var profilePic: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Activity Result Launcher to handle the result of image picker activity.
         * Upon successful selection of an image, it sets the selected image URI,
         * updates the profile picture in Firebase storage, and updates the ViewModel
         * with the selected image URI.
         */
        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) {

                        Log.e("Personal Information Fragment", "data not null")
                        // Get the selected image URI
                        selectedImageUri = data.data!!

                        // Set the profile picture in Firebase storage
                        FirebaseClient.setProfilePic(requireContext(), selectedImageUri, profilePic)

                        // Update the ViewModel with the selected image URI
                        viewModel.setSelectedImageUri(selectedImageUri)

                        // Update the profile picture URI in the ViewModel
                        viewModel.updateProfilePictureUri(selectedImageUri)

                    }

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_information, container, false)

        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnProfilePersonalInfoNavBack
        val displayName = binding.cardPersonalInfoDisplayName
        val userId = binding.textPersonalInfoItemValueUserId
        val gender = binding.cardPersonalInfoGender
        val mobileNumber = binding.cardPersonalInfoMobileNumber
        val driverLicense = binding.cardPersonalInfoDrivingLicense
        val vehicleDoc = binding.cardPersonalInfoVehicleDoc
        val profilePictureImageView = binding.imgPersonalInfoUserPic

        // Find the profile pic ImageView
        profilePic = binding.imgPersonalInfoUserPic

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[PersonalInfoViewModel::class.java]

        personalInformationViewModel.selectedImageUri.observe(viewLifecycleOwner, Observer { uri ->
            // Update front image view
            if(uri != null) {
                if (isUrl(uri.toString())) {
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)) // Disable disk caching
                        .into(profilePic)
                } else {
                    profilePic.setImageURI(uri)
                }
            }
        })


        // Fetch display name, gender and mobile from Firestore
        viewModel.fetchDisplayNameFromDatabase()
        viewModel.fetchMobileFromDatabase()
        viewModel.fetchGenderFromDatabase()



        // Observe the display name, gender and mobile
        viewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.textPersonalInfoItemValueDisplayName.text = displayName
        }

        viewModel.mobile.observe(viewLifecycleOwner) { mobile ->
            binding.textPersonalInfoItemValueMobileNumber.text = formatPhoneNumberWithCountryCode(mobile)
        }

        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            binding.textPersonalInfoItemValueGender.text = gender
        }

        userId.text = FirebaseAuth.getInstance().currentUser?.uid



        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)



        // NAVIGATION EVENT LISTENERS
        // Personal Information Fragment -> Profile Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_profileFragment)
        }

        // Personal Information Fragment -> Add Profile Pic with Image Picker
        profilePic.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .createIntent { intent ->
                    imagePickLauncher.launch(intent)
                    null
                }
        }


        userId.setOnClickListener {
            CommonUtils().copyLinkToClipboard(requireContext(), userId.text.toString())
        }

        // Observe the selected image URI and update the ImageView when it changes
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            profilePic.setImageURI(uri)
        }
//
//        // If selectedImageUri is not null, update the ImageView
//        selectedImageUri?.let { uri ->
//            profilePic.setImageURI(uri)
//        }

        // Personal Information Fragment -> Edit Display Name Fragment
        displayName.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editDisplayNameFragment)
        }

        // Personal Information Fragment -> Edit Gender Fragment
        gender.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editGenderFragment)
        }

        // Personal Information Fragment -> Edit Mobile Fragment
        mobileNumber.setOnClickListener {
            findNavController().navigate(R.id.action_personalInformationFragment_to_editMobileFragment3)
        }

        //TODO：
        // Personal Information Fragment -> Vehicle Documentation Fragment
        vehicleDoc.setOnClickListener{
            findNavController().navigate(R.id.action_personalInformationFragment_to_vehicleDocFragment)
        }

        // Personal Information Fragment -> Driving License Fragment
        driverLicense.setOnClickListener{
            findNavController().navigate(R.id.action_personalInformationFragment_to_drivingLicenseFragment)
        }

        return binding.root
    }

    fun isUrl(imagePath: String): Boolean {
        return imagePath.startsWith("http://") || imagePath.startsWith("https://")
    }

    fun isUri(imagePath: String): Boolean {
        return !isUrl(imagePath) // Assume that if it's not a URL, it's a URI
    }


    fun formatPhoneNumberWithCountryCode(phoneNumber: String): String {
        val part1 = phoneNumber.substring(0, 3)
        val part2 = phoneNumber.substring(3)

        return "$part1 $part2"
    }
}
