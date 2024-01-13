package com.example.sharidev2.receiverbroadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.example.sharidev2.utility.NetworkUtils

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val networkUtils = NetworkUtils(context!!)
            networkUtils.showNetworkStatus()
        }
    }
}