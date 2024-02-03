package com.example.sharidev2.screen.user

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.LoginSpinnerAdapter
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.model.Country
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.MainScope
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private var countdownTimer: CountDownTimer? = null
    private var secondsLeft: Long = 59 // Initial countdown time in seconds
    private var currentUser:FirebaseUser? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,  R.layout.fragment_login, container, false)
        currentUser = auth.currentUser


        // ELEMENT VARIABLES
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val backBtn = binding.imgBtnLoginNavBack
        val spinnerCountry: Spinner = binding.spinnerLoginMobileCountryCode
        val mobileNumberEditText = binding.editTextLoginEnterPhoneNumber
        val verifyCodeContainer = binding.llLoginInputVerifyCode
        val loginBtn = binding.btnLoginCtaLogin
        val loadingSpinner = binding.progressBarLoginResendVerificationCode
        val countdownText = binding.textLoginResendVerifyCodeCountdown
        val resendText = binding.textLoginResendVerificationCode


        // DATA VARIABLES
        val countryList = getLoginCountryList()
        var isValidNumber : Boolean = false


        // LAYOUT SETTINGS
        bottomNav?.visibility = View.GONE
        // Set a maximum length for the EditText (e.g., 13 characters)
        val maxLength = 12
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        mobileNumberEditText.filters = filters



        // ADAPTER
        val adapter = LoginSpinnerAdapter(requireContext(), countryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = adapter


        // EVENT LISTENERS
        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCountry: Country = countryList[position]
                // Handle the selected country (e.g., store the code in a variable)
                val selectedCountryCode = selectedCountry.countryCode
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
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
                isValidNumber = input.isNotEmpty() && input.toDoubleOrNull() != null && (input.length == 9 || input.length == 10)   // Exclude starting "0"

                // Add new spacing
                val formattedText = formatMobileNumber(input)

                // Update the EditText with the formatted text
                if (formattedText != s.toString()) {
                    mobileNumberEditText.setText(formattedText)
                    mobileNumberEditText.setSelection(formattedText.length)
                }

                verifyCodeContainer.visibility = View.GONE
            }

        })

        // Check if the input mobile number is valid, then display the verification code input
        loginBtn.setOnClickListener {
            // Display the verification code input
            verifyCodeContainer.visibility = if(isValidNumber) View.VISIBLE else View.GONE

            // TODO: Send OTP code to user
            Toast.makeText(requireContext(), "OTP Sent", Toast.LENGTH_SHORT).show()

            // TODO: Display loading spinner when still loading

            // TODO: Hide loading spinner after OPT code is sent to the user
            loadingSpinner.visibility = View.GONE
            countdownText.visibility = View.VISIBLE
            resendText.visibility = View.GONE

            // Start countdown the resend code timer
            //startCountdownTimer()
        }

        resendText.setOnClickListener {
            // TODO: Send OTP code to user
            Toast.makeText(requireContext(), "OTP Resent!", Toast.LENGTH_SHORT).show()

            // TODO: Display loading spinner when still loading

            // TODO: Hide loading spinner after OPT code is sent to the user
            loadingSpinner.visibility = View.GONE

            // Show the countdown text and hide the "Resend" text
            countdownText.visibility = View.VISIBLE
            resendText.visibility = View.GONE

            // Start countdown the resend code timer
            //startCountdownTimer()
        }


        // NAVIGATION EVENT LISTENERS
        // Profile Fragment -> Personal Information Fragment
        backBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        }




        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnLoginCtaLogin.setOnClickListener {
//            generateOtp()
//        }
    }

//    private fun generateOtp() {
//        val phoneNumber = binding.editTextLoginEnterPhoneNumber.text.toString()
//
//        if (phoneNumber.isNotBlank()) {
//            val options = PhoneAuthOptions.newBuilder(auth)
//                .setPhoneNumber(phoneNumber)
//                .setTimeout(60L, TimeUnit.SECONDS)
//                .setActivity(requireActivity())
//                .setCallbacks(callbacks)
//                .build()
//
//            PhoneAuthProvider.verifyPhoneNumber(options)
//        }
//    }

//    private fun sendHome() {
//        val loginIntent = Intent(requireContext(), MainActivity::class.java)
//        startActivity(loginIntent)
//        // Note: There is no direct equivalent to 'finish()' in a Fragment, as Fragments are part of the Activity's lifecycle.
//    }
//
//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(requireActivity()) { task: Task<AuthResult> ->
//                if (task.isSuccessful) {
//                    val user = task.result?.user
//                    sendHome()
//                } else {
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        // The verification code entered was invalid
//                    }
//                    // Update UI
//                }
//            }
//    }
//
//
//    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//            signInWithPhoneAuthCredential(credential)
//        }
//
//        override fun onVerificationFailed(e: FirebaseException) {
//            if (e is FirebaseAuthInvalidCredentialsException) {
//                // Invalid request
//            } else if (e is FirebaseTooManyRequestsException) {
//                // The SMS quota for the project has been exceeded
//            } else {
//                // Other cases
//            }
//
//            // Show a message and update the UI
//        }
//
//        override fun onCodeSent(
//            verificationId: String,
//            token: PhoneAuthProvider.ForceResendingToken,
//        ) {
//            storedVerificationId = verificationId
//            resendToken = token
//
//            // Start OtpActivity with the verificationId
//            val otpIntent = Intent(requireContext(), OtpActivity::class.java)
//            otpIntent.putExtra("otpcr", verificationId)
//            startActivity(otpIntent)
//            // Note: If you're using startActivity for result, you should handle the result accordingly.
//        }
//    }
//}

    private fun getLoginCountryList(): List<Country> {
        return listOf(
            Country("Malaysia", "+60"),
            Country("Singapore", "+65"),
            Country("Indonesia", "+62")
        )
    }

    private fun formatMobileNumber(originalText: String): String {
        val formattedText = StringBuilder()

        for (i in originalText.indices) {
            if (originalText.length == 9) {
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
}


//    private fun startCountdownTimer() {
//        // Cancel the previous timer if it exists
//        countdownTimer?.cancel()
//
//        // Start a new countdown timer
//        countdownTimer = object : CountDownTimer(secondsLeft * 1000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                // Update the seconds left and the timer text
//                secondsLeft = millisUntilFinished / 1000
//                updateTimerText()
//            }
//
//            override fun onFinish() {
//                val countdownTimerText = view?.findViewById<TextView>(R.id.text_login_resend_verify_code_countdown)
//                val resendText = view?.findViewById<TextView>(R.id.text_login_resend_verification_code)
//
//                if(countdownTimerText != null && resendText != null) {
//                    countdownTimerText.visibility = View.GONE
//                    resendText.visibility = View.VISIBLE
//                    // Reset timer
//                    secondsLeft = 60  // Initial countdown time in seconds
//                }
//
//            }
//
//        }.start()
//    }
//
//    private fun updateTimerText() {
//        val countdownTimerText = view?.findViewById<TextView>(R.id.text_login_resend_verify_code_countdown)
//        // Update the timer text in the format "Resend(*secondsLeft*)"
//        countdownTimerText?.text = getString(R.string.login_fragment_btn_verify_code_resend_countdown, secondsLeft)
//    }
//}