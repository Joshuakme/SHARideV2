package com.example.sharidev2.screen.user

import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.data.model.RideOption
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.example.sharidev2.viewmodel.LoginViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Collections
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by activityViewModels()

    private var timeoutSeconds: Long = 60 // Initial countdown time in seconds

    private val auth = FirebaseClient.firebaseAuth
    private val currentUser = UserClient

    private lateinit var verificationCode: String
    private lateinit var forceResendingToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ELEMENT VARIABLES
        val backBtn = binding.imgBtnLoginNavBack
        val spinnerCountry: MaterialCardView = binding.spinnerLoginMobileCountryCode
        val countryCodeText = binding.textLoginSpinnerMobileCountryCode
        val mobileNumberEditText = binding.editTextLoginEnterPhoneNumber
        val verifyCodeContainer = binding.llLoginInputVerifyCode
        val otpCodeEditText = binding.editTextLoginVerificationCode
        val getOtpBtn = binding.btnLoginCtaGetOtp
        val loginBtn = binding.btnLoginCtaLogin
        val countdownText = binding.textLoginResendVerifyCodeCountdown
        val resendText = binding.textLoginResendVerificationCode
        val loginWithGoogleBtn = binding.btnLoginContinueWithGoogle


        // DATA VARIABLES
        var isValidNumber: Boolean = false
        var isValidOTP: Boolean = false
        val mobileNumMaxLength = 12
        val otpCodeMaxLength = 6


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)
        // Set a maximum length for the EditText (e.g., 13 characters)
        val mobileNumberFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(mobileNumMaxLength))
        val otpCodeFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(otpCodeMaxLength))
        mobileNumberEditText.filters = mobileNumberFilters
        otpCodeEditText.filters = otpCodeFilters
        getOtpBtn.isEnabled = false
        loginBtn.isEnabled = false


        // VIEW MODEL
        loginViewModel.countryCode.observe(viewLifecycleOwner, Observer { newCountryCode ->
            countryCodeText.text =
                getString(R.string.login_fragment_input_country_code, newCountryCode)
        })

        // EVENT LISTENERS
        spinnerCountry.setOnClickListener {
            showCountryCodeDialog()
        }


        // Check if the mobile number input is valid
        mobileNumberEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed in this case
            }

            override fun afterTextChanged(s: Editable?) {
                // Check if the input is a valid number
                val input = s.toString().replace(" ", "")
                isValidNumber =
                    input.isNotEmpty() && input.toDoubleOrNull() != null && (input.length == 9 || input.length == 10)   // Exclude starting "0"

                // Add new spacing
                val formattedText = formatMobileNumber(input)

                // Update the EditText with the formatted text
                if (formattedText != s.toString()) {
                    mobileNumberEditText.setText(formattedText)
                    mobileNumberEditText.setSelection(formattedText.length)
                }

                getOtpBtn.isEnabled = isValidNumber

                verifyCodeContainer.visibility = View.GONE
                showLoginBtn(false)
            }

        })

        otpCodeEditText.addTextChangedListener((object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not needed in this case
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not needed in this case
            }

            override fun afterTextChanged(s: Editable?) {
                // Check if the input is a valid number
                val input = s.toString().replace(" ", "")
                isValidOTP =
                    input.isNotEmpty() && input.toDoubleOrNull() != null && input.length == 6

                loginBtn.isEnabled = isValidOTP
            }

        }))

        // Check if the input mobile number is valid, then display the verification code input
        getOtpBtn.setOnClickListener {
            val countryCode = countryCodeText.text.toString()
            val unformatedPhoneNumber = mobileNumberEditText.text.toString().replace(" ", "")
            val completePhoneNumber = countryCode + unformatedPhoneNumber

            if (unformatedPhoneNumber.length == 9 || unformatedPhoneNumber.length == 10) {
                // Display the verification code input
                verifyCodeContainer.visibility = if (isValidNumber) View.VISIBLE else View.GONE

                sendOTP(completePhoneNumber, false)


                countdownText.visibility = View.VISIBLE
                resendText.visibility = View.GONE
            } else {
                Toast.makeText(requireContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show()
            }
        }

        // Login Button
        loginBtn.setOnClickListener {
            if (verificationCode != null) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationCode,
                    otpCodeEditText.text.toString()
                )
                signInWithPhone(credential)
            }
        }

        loginWithGoogleBtn.setOnClickListener {
            // TODO: Google Auth Implementation
        }

        resendText.setOnClickListener {
            sendOTP(countryCodeText.text.toString() + mobileNumberEditText.text.toString(), true)

        }


        // NAVIGATION EVENT LISTENERS
        // Profile Fragment -> Personal Information Fragment
        backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.action_profileFragment_to_loginFragment, true)
        }
    }


    private fun showLoginBtn(show: Boolean) {
        val getOtpBtn = binding.btnLoginCtaGetOtp
        val loginBtn = binding.btnLoginCtaLogin


        getOtpBtn.visibility = if (show) View.GONE else View.VISIBLE
        loginBtn.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun getOtpLoading(loading: Boolean) {
        val loginBtnGetOtpText = binding.textLoginCtaBtnGetOtp
        val getOtpLoadingSpinner = binding.progressBarGetOtpCtaBtn

        loginBtnGetOtpText.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        getOtpLoadingSpinner.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun loginLoading(loading: Boolean) {
        val loginBtnLoginText = binding.textLoginCtaBtnLogin
        val loginBtnLoadingSpinner = binding.progressBarLoginCtaBtn

        loginBtnLoginText.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        loginBtnLoadingSpinner.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun formatMobileNumber(originalText: String): String {
        val formattedText = StringBuilder()

        for (i in originalText.indices) {
            if (originalText.length in 1..9) {
                if (i > 0 && i == 2 || i > 0 && i == 5) {
                    formattedText.append(" ") // Add a space after every 4 characters
                }
            } else if (originalText.length == 10) {
                if (i > 0 && i == 2 || i > 0 && i == 6) {
                    formattedText.append(" ") // Add a space after every 4 characters
                }
            }

            formattedText.append(originalText[i])
        }
        return formattedText.toString()
    }

    private fun startResendTimer() {
        var resendTimer = Timer()

        val countdownTimerText = binding.textLoginResendVerifyCodeCountdown
        val resendText = binding.textLoginResendVerificationCode

        // Show the countdown text and hide the "Resend" text
        countdownTimerText?.visibility = View.VISIBLE
        resendText?.visibility = View.GONE


        // Start a new countdown timer
        resendTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeoutSeconds--

                requireActivity().runOnUiThread {
                    countdownTimerText?.text =
                        getString(
                            R.string.login_fragment_btn_verify_code_resend_countdown,
                            timeoutSeconds
                        )

                    if (timeoutSeconds <= 0) {
                        timeoutSeconds = 60L
                        resendTimer.cancel()

                        countdownTimerText?.visibility = View.GONE
                        resendText?.visibility = View.VISIBLE
                    }
                }
            }

        }, 0, 1000)
    }

    // Method to show the CountryCodeBottomDialogFragment
    private fun showCountryCodeDialog() {
        val dialogFragment = CountryCodeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun sendOTP(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        getOtpLoading(true)

        val builder: PhoneAuthOptions.Builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1. Instant verification. In some cases the phone number can be instantly
                    //    verified without needing to send or enter a verification code.
                    // 2. Auto-retrieval. On some devices Google Play services can automatically
                    //    detect the incoming verification SMS and perform verification without
                    //    user action.
                    // Here, you can handle the verification completion logic.
                    getOtpLoading(false)
                    signInWithPhone(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    getOtpLoading(false)
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // This callback is invoked when the verification code is successfully sent.
                    // `verificationId` is the verification code sent to the user's phone number.
                    // You can save this code and use it to verify the user later.
                    // `token` can be used to resend the verification code, if needed.
                    // Here, you can handle the code sent logic.
                    getOtpLoading(false)

                    verificationCode = verificationId
                    forceResendingToken = token

                    Toast.makeText(requireContext(), "OTP sent successfully!", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(
                builder.setForceResendingToken(forceResendingToken).build()
            )
        } else {
            // Start the phone number verification process
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }

        showLoginBtn(true)
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        loginLoading(true)

        // login and navigate to next screen
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginLoading(false)

                    // TODO: save the phone number to firebase database

                    CoroutineScope(Dispatchers.Main).launch {
                        // Call assignUserDefaultInfo from within the coroutine
                        FirebaseClient.assignUserDefaultInfo(task.result?.additionalUserInfo)


                        addNewUserToFirestore(task)

                        currentUser.setCurrentUser(task.result.user?.uid ?: "")
                    }

                    Toast.makeText(requireContext(), "Logged in successfully!", Toast.LENGTH_SHORT)
                        .show()

                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    loginLoading(false)

                    Toast.makeText(requireContext(), "OTP verification failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private suspend fun addNewUserToFirestore(task: Task<AuthResult>) {
        val userUid = task.result.user?.uid
        if (userUid != null) {
            try {
                val userDocument = FirebaseClient.firestore.collection("user")
                    .document(userUid)
                    .get()
                    .await()

                if (!userDocument.exists()) {
                    // User document does not exist, so add the new user
                    val user = User(
                        uid = userUid,
                        displayName = task.result.user?.displayName,
                        email = task.result.user?.email,
                        phoneNumber = task.result.user?.phoneNumber,
                        photoUrl = task.result.user?.photoUrl,
                        rideOption = RideOption(),
                        savedAddress = mutableListOf(),
                        joinedDate = Timestamp.now()
                    )

                    FirebaseClient.firestore.collection("user")
                        .document(userUid)
                        .set(user)
                        .await()

                    if(user.savedAddress != null){
                        for (address in user.savedAddress) {
                            // Add each address as a document within the 'addresses' subcollection
                            FirebaseClient.firestore.collection("user")
                                .document(userUid)
                                .collection("addresses")
                                .add(address)
                                .await()
                        }
                    } else {
                    }


                } else {
                    // User document already exists
                    Log.d("Save new user to Firestore", "User document already exists")
                }
            } catch (e: Exception) {
                Log.e("Save new user to Firestore", e.message.toString())
            }
        } else {
            Log.e("Save new user to Firestore", "User UID is null")
        }
    }
}

