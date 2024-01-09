package com.example.sharidev2.screen.user

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentLoginBinding
import com.example.sharidev2.model.Country
import com.example.sharidev2.viewmodel.LoginViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private var countdownTimer: CountDownTimer? = null
    private var secondsLeft: Long = 59 // Initial countdown time in seconds


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
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
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
        bottomNav?.visibility = View.GONE
        // Set a maximum length for the EditText (e.g., 13 characters)
        val mobileNumMaxLength = 12
        val otpCodeMaxLength = 6
        val mobileNumberFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(mobileNumMaxLength))
        val otpCodeFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(otpCodeMaxLength))
        mobileNumberEditText.filters = mobileNumberFilters
        otpCodeEditText.filters = otpCodeFilters

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

                verifyCodeContainer.visibility = View.GONE
            }

        })

        // Check if the input mobile number is valid, then display the verification code input
        loginBtn.setOnClickListener {
            // Display the verification code input
            verifyCodeContainer.visibility = if (isValidNumber) View.VISIBLE else View.GONE

            // TODO: Send OTP code to user
            Toast.makeText(requireContext(), "OTP Sent", Toast.LENGTH_SHORT).show()

            // TODO: Display loading spinner when still loading

            // TODO: Hide loading spinner after OPT code is sent to the user
            loadingSpinner.visibility = View.GONE
            countdownText.visibility = View.VISIBLE
            resendText.visibility = View.GONE

            // Start countdown the resend code timer
            startCountdownTimer()
        }

        loginWithGoogleBtn.setOnClickListener {

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
        countdownTimer = object : CountDownTimer(secondsLeft * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the seconds left and the timer text
                secondsLeft = millisUntilFinished / 1000
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
                    secondsLeft = 60  // Initial countdown time in seconds
                }

            }

        }.start()
    }

    private fun updateTimerText() {
        val countdownTimerText =
            view?.findViewById<TextView>(R.id.text_login_resend_verify_code_countdown)
        // Update the timer text in the format "Resend(*secondsLeft*)"
        countdownTimerText?.text =
            getString(R.string.login_fragment_btn_verify_code_resend_countdown, secondsLeft)
    }

    // Method to show the CountryCodeBottomDialogFragment
    private fun showCountryCodeDialog() {
        val dialogFragment = CountryCodeBottomDialogFragment()
        dialogFragment.show(childFragmentManager, dialogFragment.tag)
    }
}