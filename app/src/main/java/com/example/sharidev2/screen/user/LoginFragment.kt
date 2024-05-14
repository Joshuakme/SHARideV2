package com.example.sharidev2.screen.user

import android.content.Context
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
import com.example.sharidev2.data.model.User
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.viewmodel.LoginViewModel
import com.example.sharidev2.viewmodel.SharedCurrentUserViewModel
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
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val currentUserViewModel: SharedCurrentUserViewModel by activityViewModels()

    private var timeoutSeconds: Long = 60 // Initial countdown time in seconds

    private val auth = FirebaseClient.firebaseAuth

    private lateinit var context: Context
    private var verificationCode: String? = null
    private lateinit var forceResendingToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        if(getContext() != null) {
            context = requireContext()
        } else {
            context = requireActivity().applicationContext
        }

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
        enableGetOtpBtn(false)
        enableLoginBtn(false)


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

                enableGetOtpBtn(isValidNumber)

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

                enableLoginBtn(isValidOTP)
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
                Toast.makeText(context, "Invalid Phone Number", Toast.LENGTH_SHORT).show()
            }
        }

        // Login Button
        loginBtn.setOnClickListener {
            if (verificationCode != null) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationCode!!,
                    otpCodeEditText.text.toString()
                )
                signInWithPhone(credential)
            } else {
                Toast.makeText(context, "Please enter OTP code", Toast.LENGTH_SHORT).show()
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
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
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

    private fun enableGetOtpBtn(enable: Boolean) {
        val getOtpBtn = binding.btnLoginCtaGetOtp
        val getOtpBtnText = binding.textLoginCtaBtnGetOtp

        getOtpBtn.isEnabled = enable
        getOtpBtn.isClickable = enable

        if(enable) {
            val colorOnPrimary = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOnPrimary)
            val colorSurfaceInverse = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceInverse)

            getOtpBtn.setCardBackgroundColor(colorSurfaceInverse)
            getOtpBtnText.setTextColor(colorOnPrimary)

        } else {
            val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)
            val colorSurfaceContainerHighest = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceContainerHighest)

            getOtpBtn.setCardBackgroundColor(colorSurfaceContainerHighest)
            getOtpBtnText.setTextColor(colorOutline)
        }
    }

    private fun enableLoginBtn(enable: Boolean) {
        val loginBtn = binding.btnLoginCtaLogin
        val loginBtnLoginText = binding.textLoginCtaBtnLogin

        loginBtn.isEnabled = enable
        loginBtn.isClickable = enable

        if(enable) {
            val colorOnPrimary = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOnPrimary)
            val colorSurfaceInverse = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceInverse)

            loginBtn.setCardBackgroundColor(colorSurfaceInverse)
            loginBtnLoginText.setTextColor(colorOnPrimary)

        } else {
            val colorOutline = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorOutline)
            val colorSurfaceContainerHighest = CommonUtils().getThemeColor(context, com.google.android.material.R.attr.colorSurfaceContainerHighest)

            loginBtn.setCardBackgroundColor(colorSurfaceContainerHighest)
            loginBtnLoginText.setTextColor(colorOutline)
        }
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
        countdownTimerText.visibility = View.VISIBLE
        resendText.visibility = View.GONE


        // Start a new countdown timer
        resendTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeoutSeconds--

                if (isAdded) {
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
                } else {
                    // Fragment is not attached to the activity, handle the case accordingly
                    resendTimer.cancel() // Cancel the timer task
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
                    getOtpLoading(false)

                    signInWithPhone(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    getOtpLoading(false)

                    Log.e("Login Fragment", e.message.toString())
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    getOtpLoading(false)


                    verificationCode = verificationId
                    forceResendingToken = token
                    Log.e("LoginFragment", "forceResendingToken: $forceResendingToken")

                    Toast.makeText(context, "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                }
            })

        if (isResend) {
            if(forceResendingToken != null) {
                PhoneAuthProvider.verifyPhoneNumber(
                    builder.setForceResendingToken(forceResendingToken).build()
                )
            }
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

                    CoroutineScope(Dispatchers.Main).launch {
                        // Call assignUserDefaultInfo from within the coroutine
                        FirebaseClient.assignUserDefaultInfo(task.result?.additionalUserInfo)

                        addNewUserToFirestore(task)
                        currentUserViewModel.signIn()
                    }

                    Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    loginLoading(false)

                    Toast.makeText(context, "OTP verification failed", Toast.LENGTH_SHORT)
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
                        savedAddress = mapOf(),
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

