package com.example.sharidev2.screen.user

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.utility.FirebaseUtils
import com.example.sharidev2.viewmodel.LoginViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private var countdownTimer: CountDownTimer? = null
    private var timeoutSeconds: Long = 60 // Initial countdown time in seconds

    private val auth = FirebaseAuth.getInstance()
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
        val loginBtn = binding.btnLoginCtaLogin
        val loadingSpinner = binding.progressBarLoginResendVerificationCode
        val countdownText = binding.textLoginResendVerifyCodeCountdown
        val resendText = binding.textLoginResendVerificationCode
        val loginWithGoogleBtn = binding.btnLoginContinueWithGoogle



        // DATA VARIABLES
        var isValidNumber: Boolean = false


        // LAYOUT SETTINGS
        (activity as MainActivity).setBottomNavVisible(false)
        // Set a maximum length for the EditText (e.g., 13 characters)
        val mobileNumMaxLength = 12
        val otpCodeMaxLength = 6
        val mobileNumberFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(mobileNumMaxLength))
        val otpCodeFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(otpCodeMaxLength))
        mobileNumberEditText.filters = mobileNumberFilters
        otpCodeEditText.filters = otpCodeFilters
        loginBtn.isEnabled = false


        // VIEW MODEL
        loginViewModel.countryCode.observe(viewLifecycleOwner, Observer { newCountryCode ->
            countryCodeText.text = getString(R.string.login_fragment_input_country_code, newCountryCode)
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

                loginBtn.isEnabled = isValidNumber

                verifyCodeContainer.visibility = View.GONE
            }

        })

        // Check if the input mobile number is valid, then display the verification code input
        loginBtn.setOnClickListener {
            // Display the verification code input
            verifyCodeContainer.visibility = if (isValidNumber) View.VISIBLE else View.GONE

            // TODO: Send OTP code to user
            if(otpCodeEditText.text.toString().isNullOrEmpty()) {
                sendOTP(countryCodeText.text.toString() + mobileNumberEditText.text.toString(), false)

                // TODO: Display loading spinner when still loading

                // TODO: Hide loading spinner after OPT code is sent to the user

                loadingSpinner.visibility = View.GONE
                countdownText.visibility = View.VISIBLE
                resendText.visibility = View.GONE

                // Start countdown the resend code timer
                startCountdownTimer()
            }
            else {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationCode, otpCodeEditText.text.toString())
                signInWithPhone(credential)
            }

        }

        loginWithGoogleBtn.setOnClickListener {

        }

        resendText.setOnClickListener {
            // TODO: Send OTP code to user
            sendOTP(countryCodeText.text.toString() + mobileNumberEditText.text.toString(), true)

            // TODO: Display loading spinner when still loading

            // TODO: Hide loading spinner after OPT code is sent to the user
            loadingSpinner.visibility = View.GONE

            // Show the countdown text and hide the "Resend" text
            countdownText.visibility = View.VISIBLE
            resendText.visibility = View.GONE

            // Start countdown the resend code timer
            startCountdownTimer()
        }


        // NAVIGATION EVENT LISTENERS
        // Profile Fragment -> Personal Information Fragment
        backBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_profileFragment_to_personalInformationFragment)
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        }
    }


    private fun formatMobileNumber(originalText: String): String {
        val formattedText = StringBuilder()

        for (i in originalText.indices) {
            if(originalText.length in 1..9) {
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

    private fun startCountdownTimer() {
        // Cancel the previous timer if it exists
        countdownTimer?.cancel()

        // Start a new countdown timer
        countdownTimer = object : CountDownTimer((timeoutSeconds - 1) * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the seconds left and the timer text
                timeoutSeconds = millisUntilFinished / 1000
                updateTimerText()
            }

            override fun onFinish() {
                val countdownTimerText =
                    view?.findViewById<TextView>(R.id.text_login_resend_verify_code_countdown)
                val resendText =
                    view?.findViewById<TextView>(R.id.text_login_resend_verification_code)

                if (countdownTimerText != null && resendText != null) {
                    countdownTimerText.visibility = View.GONE
                    resendText.visibility = View.VISIBLE
                    // Reset timer
                    timeoutSeconds = 60  // Initial countdown time in seconds
                }

            }

        }.start()
    }

    private fun updateTimerText() {
        val countdownTimerText =
            view?.findViewById<TextView>(R.id.text_login_resend_verify_code_countdown)
        // Update the timer text in the format "Resend(*secondsLeft*)"
        countdownTimerText?.text =
            getString(R.string.login_fragment_btn_verify_code_resend_countdown, timeoutSeconds)
    }

    // Method to show the CountryCodeBottomDialogFragment
    private fun showCountryCodeDialog() {
        val dialogFragment = CountryCodeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }

    private fun sendOTP(phoneNumber: String, isResend: Boolean) {
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

                    signInWithPhone(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {

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

                    verificationCode = verificationId
                    forceResendingToken = token

                    Toast.makeText(requireContext(), "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                }
            })

        if(isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(forceResendingToken).build())
        } else {
            // Start the phone number verification process
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }


    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        // login and navigate to next screen
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // TODO: save the phone number to firebase database
                    CoroutineScope(Dispatchers.Main).launch {
                        // Call assignUserDefaultInfo from within the coroutine
                        FirebaseUtils().assignUserDefaultInfo(task.result?.additionalUserInfo)
                    }
                    Toast.makeText(requireContext(), "Logged in successfully!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "OTP verification failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}

