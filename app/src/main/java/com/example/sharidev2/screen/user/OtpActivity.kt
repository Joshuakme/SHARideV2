package com.example.sharidev2.screen.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sharidev2.MainActivity
import com.example.sharidev2.databinding.ActivityOtpActivitiesBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpActivity : AppCompatActivity() {
    private var binding: ActivityOtpActivitiesBinding? = null
    private var auth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private val phoneNumber = ""
    private var authId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpActivitiesBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()
        currentUser = auth!!.currentUser
        authId = intent.getStringExtra("otpcr")
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = task.result.user
                    sendHome()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendHome() {
        val loginIntent = Intent(this, MainActivity::class.java)
        startActivity(loginIntent)
        // Note: There is no direct equivalent to 'finish()' in a Fragment, as Fragments are part of the Activity's lifecycle.
    }

    override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            sendHome()
        }
    }

    fun VerifyOtp(view: View?) {
        val otp: String = binding.toString()
        if (!otp.isEmpty()) {
            val credential = PhoneAuthProvider.getCredential(authId!!, otp)
            signInWithPhoneAuthCredential(credential)
        }
    }
}
