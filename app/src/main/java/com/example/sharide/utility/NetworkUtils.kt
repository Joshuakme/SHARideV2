package com.example.sharide.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class NetworkUtils(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun isInternetAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    fun showNetworkStatus() {
        if (!isInternetAvailable()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            // Do nothing
        }
    }

    fun getJsonDataFromUrl(url: String): String {
        val connection = URL(url).openConnection()
        val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
        val jsonData = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            jsonData.append(line)
        }
        reader.close()

        return jsonData.toString()
    }
}
