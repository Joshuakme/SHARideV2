package com.example.sharidev2.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.IBinder
import com.example.sharidev2.utility.NetworkUtils

class ConnectivityService : Service() {

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val networkUtils = NetworkUtils(applicationContext)
            networkUtils.showNetworkStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            val networkUtils = NetworkUtils(applicationContext)
            networkUtils.showNetworkStatus()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        return START_STICKY
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        val connectivityManager =
//            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        connectivityManager.unregisterNetworkCallback(networkCallback)
//    }
}
