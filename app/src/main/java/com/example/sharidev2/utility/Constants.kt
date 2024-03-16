package com.example.sharidev2.utility

class Constants {
    companion object {
        // PERMISIONS
        val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002
        val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
        val PERMISSIONS_REQUEST_POST_NOTIFICATION = 9004



        // FIREBASE REQUEST STATUS
        val FIREBASE_REQUEST_SUCCESS = 1000 // Success
        val FIREBASE_REQUEST_NOT_BELONG_USER = 1001 // Contact doesn't belong to the current user
        val FIREBASE_REQUEST_USER_NOT_AUTHENTICATED = 1002 // User not authenticated
        val FIREBASE_REQUEST_EXCEPTION = 1003 // Handle exceptions
        val FIREBASE_REQUEST_DATA_NOT_VALID = 1004
        val FIREBASE_REQUEST_FAILED = 1005


        // UI DATA LOADING STATUS
        val UI_DATA_LOADING = 2000
        val UI_DATA_SUCCESS = 2001
        val UI_DATA_FAILED = 2002


        // NOTIFICATION CHANNEL
        val NOTIF_MESSAGE_CHANNEL = 3000
        val NOTIF_MESSAGE = 3001


    }
}