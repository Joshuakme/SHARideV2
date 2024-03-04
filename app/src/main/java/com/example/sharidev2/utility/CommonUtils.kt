package com.example.sharidev2.utility

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sharidev2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

class CommonUtils {
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }


    // Function to generate a random string of specified length
    fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // DATE RELATED METHODS
    fun getMinSelectableDate(): Calendar {
        val calendar = Calendar.getInstance()

        // Set minimum selectable date to current date
        return calendar
    }

    fun getMaxSelectableDate(): Calendar {
        val calendar = Calendar.getInstance()

        // Set maximum selectable date to one year from current date
        calendar.add(Calendar.YEAR, 1)

        return calendar
    }


    // ANDROID
    fun getThemeColor(context: Context, themeColorId: Int): Int {
        val typedValue = TypedValue()
        // Resolve the attribute to get the color value programmatically
        context?.theme?.resolveAttribute(themeColorId, typedValue, true)
        return typedValue.data
    }

    fun getBitmapFromVector(context: Context, color: Int): BitmapDescriptor {
        val SCALE_FACTOR = 1.0f

        // Create a VectorDrawable from the default marker resource
        val vectorDrawable = ContextCompat.getDrawable(context, R.drawable.location) as? VectorDrawable

        vectorDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        val bitmapWidth = (vectorDrawable?.intrinsicWidth ?: 0 * SCALE_FACTOR).toInt()
        val bitmapHeight = (vectorDrawable?.intrinsicHeight ?: 0 * SCALE_FACTOR).toInt()

        // Convert the VectorDrawable to a BitmapDescriptor
        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun createMarkerWithCircularImage(context: Context, profilePicture: Bitmap, backgroundColor: Int): BitmapDescriptor {
        // Load circular background drawable
        val circularBackground = ContextCompat.getDrawable(context, R.drawable.circular_background)

        // Set the background color
        circularBackground?.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)

        // Convert the drawable to a Bitmap
        val backgroundBitmap = Bitmap.createBitmap(circularBackground?.intrinsicWidth ?: 0, circularBackground?.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888)
        val backgroundCanvas = Canvas(backgroundBitmap)
        circularBackground?.setBounds(0, 0, backgroundCanvas.width, backgroundCanvas.height)
        circularBackground?.draw(backgroundCanvas)

        // Calculate dimensions for the profile picture
        val profilePictureWidth = (backgroundCanvas.width * 0.8).toInt() // Adjust this value as needed
        val profilePictureHeight = (backgroundCanvas.height * 0.8).toInt() // Adjust this value as needed
        val left = (backgroundCanvas.width - profilePictureWidth) / 2
        val top = (backgroundCanvas.height - profilePictureHeight) / 2

        // Create a circular mask for the profile picture
        val paint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
        val profilePictureBitmap = Bitmap.createScaledBitmap(profilePicture, profilePictureWidth, profilePictureHeight, true)
        val profilePictureMask = Bitmap.createBitmap(backgroundCanvas.width, backgroundCanvas.height, Bitmap.Config.ARGB_8888)
        val profilePictureCanvas = Canvas(profilePictureMask)
        profilePictureCanvas.drawCircle(backgroundCanvas.width / 2f, backgroundCanvas.height / 2f, backgroundCanvas.width / 2f, paint)

        // Draw the profile picture on the canvas with the circular mask
        backgroundCanvas.drawBitmap(profilePictureBitmap, left.toFloat(), top.toFloat(), null)

        // Convert the composite bitmap to a BitmapDescriptor
        return BitmapDescriptorFactory.fromBitmap(backgroundBitmap)
    }




    /*
    fun getDeviceCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onLocationResult: (LatLng) -> Unit,
        onLocationError: () -> Unit
    ) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)

                        onLocationResult.invoke(latLng)
                    } else {
                        // Handle the case where lastKnownLocation is null
                        onLocationError.invoke()
                    }
                } else {
                    // Handle the case where the task is not successful
                    onLocationError.invoke()
                    Log.d(ContentValues.TAG, "Current location is null. Using defaults.")
                }
            }
        } catch (e: SecurityException) {
            // Handle the case where a SecurityException occurs
            onLocationError.invoke()
            Log.e("Exception: %s", e.message, e)
        }
    }
*/


    fun copyLinkToClipboard(context: Context, textToCopy: String) {
        // Get ClipboardManager
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a ClipData object
        val clipData = ClipData.newPlainText("URL", textToCopy)

        // Set the ClipData object to the clipboard
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun closeKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }



}