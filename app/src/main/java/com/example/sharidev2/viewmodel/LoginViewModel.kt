package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class LoginViewModel : ViewModel() {

    private val _countryCode = MutableLiveData<Int>().apply {
        value = 60  // Set your default country code here
    }
    val countryCode: LiveData<Int> get() = _countryCode

    fun setCountryCode(newCountryCode: Int) {
        _countryCode.value = newCountryCode
    }

    companion object {
        fun getInstance(owner: ViewModelStoreOwner): LoginViewModel {
            return ViewModelProvider(owner)[LoginViewModel::class.java]
        }
    }
}