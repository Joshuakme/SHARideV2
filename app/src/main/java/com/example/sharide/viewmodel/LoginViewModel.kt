package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharide.data.model.OtpSentState
import com.example.sharide.data.model.SignInResult
import com.example.sharide.data.model.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    // Country Code
    private val _countryCode = MutableLiveData<Int>()
    val countryCode: LiveData<Int> get() = _countryCode

    // OTP sent state
    private val _otpSentState = MutableStateFlow(OtpSentState())
    val otpSentState get() = _otpSentState.asStateFlow()

    // Log In State
    private val _state = MutableStateFlow(SignInState())
    val state get() = _state.asStateFlow()



    // GETTER & SETTER
    fun setCountryCode(newCountryCode: Int) {
        _countryCode.value = newCountryCode
    }

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }

    fun resetState() {
        _state.update { SignInState() }
    }



}