package com.example.sharidev2.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.utility.Constants.Companion.NOTIF_MESSAGE_CHANNEL
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    companion object {
        private const val datetimeFormat = "dd MMM yyyy, hh:mm a"
        private const val dateFormat = "yyyy MMM dd"
        private const val timeFormat = "hh : mm a"


        fun formatDateTime(date: Timestamp): String {
            return formatFirebaseTimestamp(date, datetimeFormat)
        }

        fun formatDate(date: Timestamp): String {
            return formatFirebaseTimestamp(date, dateFormat)
        }

        fun formatDate(date: Timestamp, format: String): String {
            return formatFirebaseTimestamp(date, format)
        }

        fun formatTime(time: Timestamp): String {
            return formatFirebaseTimestamp(time, timeFormat)
        }

        fun formatTime(time: Timestamp, format: String): String {
            return formatFirebaseTimestamp(time, format)
        }

        private fun formatFirebaseTimestamp(timestamp: com.google.firebase.Timestamp, pattern: String): String {
            val instant = Instant.ofEpochMilli(timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000)
            val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        }

        fun formatHiddenPhoneNumber(phoneNumber: String): String {
            // Check if the phone number has at least 4 characters
            if (phoneNumber.length < 9) {
                return "" // Return the original number if it's too short
            }

            // Get the first two and last two characters of the phone number
            val firstTwoDigits = phoneNumber.take(2)
            val lastTwoDigits = phoneNumber.takeLast(2)

            // Replace all characters between the first two and last two with two asterisks
            val hiddenDigits = "****"

            // Combine the formatted number
            val formattedPhoneNumber = "+60 $firstTwoDigits$hiddenDigits$lastTwoDigits"

            return formattedPhoneNumber
        }
    }



    fun isToday(timestamp: com.google.firebase.Timestamp): Boolean {
        val currentDate = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp.toDate().time }

        // Compare year, month, and day of month
        return currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == messageDate.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == messageDate.get(Calendar.DAY_OF_MONTH)
    }

    fun isYesterday(timestamp: com.google.firebase.Timestamp): Boolean {
        val currentDate = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp.toDate().time }

        // Compare year, month, and day of month
        return currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == messageDate.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) - messageDate.get(Calendar.DAY_OF_MONTH) == 1
    }

    fun calculateTimestampDurationInSeconds(startTime: Timestamp, endTime: Timestamp): Long {
        val startTimeMillis: Long = startTime.toDate().time
        val endTimeMillis: Long = endTime.toDate().time

        val durationTimeMillis = endTimeMillis - startTimeMillis

        return durationTimeMillis / 1000    // in Seconds
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

    fun isUrl(imagePath: String): Boolean {
        return imagePath.startsWith("http://") || imagePath.startsWith("https://")
    }


    // ANDROID
     fun getMapOriginMarkerColor(context: Context): Int {
        return context.getColor(R.color.origin_marker)
     }

    fun getMapDestMarkerColor(context: Context): Int {
        return context.getColor(R.color.dest_marker)
    }

    fun getThemeColor(context: Context, themeColorId: Int): Int {
        val typedValue = TypedValue()
        // Resolve the attribute to get the color value programmatically
        context.theme?.resolveAttribute(themeColorId, typedValue, true)

        return typedValue.data
    }

    fun getAndroidThemeColor(context: Context, themeColorId: Int): Int {
        val attrs = intArrayOf(themeColorId)
        val typedArray = context.obtainStyledAttributes(attrs)
        try {
            return typedArray.getColor(0, 0)
        } finally {
            typedArray.recycle()
        }
    }

    fun getLocationBitmapFromVector(context: Context, color: Int): BitmapDescriptor {
        val SCALE_FACTOR = 1.0f

        // Create a VectorDrawable from the default marker resource
        val vectorDrawable = ContextCompat.getDrawable(context, R.drawable.location) as? VectorDrawable

        vectorDrawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

        val bitmapWidth = ((vectorDrawable?.intrinsicWidth ?: (0 * SCALE_FACTOR))).toInt()
        val bitmapHeight = ((vectorDrawable?.intrinsicHeight ?: (0 * SCALE_FACTOR))).toInt()

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

    fun getUriFromVectorDrawable(imageView: ImageView): Uri {
        // Get the resource ID of the vector drawable
        val resourceId = imageView.context.resources.getIdentifier(
            imageView.tag as String, "drawable", imageView.context.packageName)

        // Construct a Uri using the resource ID
        return Uri.parse("android.resource://${imageView.context.packageName}/$resourceId")
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

    fun createDrawableFromView(context: Context, view: View): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }






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


    fun openKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun openKeyboard(editText: EditText, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        editText.requestFocus()
        editText.post {
            editText.post {
                imm.showSoftInput(editText, 0)
            }
        }
    }


    fun addKeyboardListenerToView(contentView: View, listener: OnKeyboardListenersToView) {
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            contentView.getWindowVisibleDisplayFrame(r)
            val screenHeight: Int = contentView.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight: Int = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                listener.onKeyboardVisibilityChanged(true)
            } else {
                // keyboard is closed
                listener.onKeyboardVisibilityChanged(false)
            }
        }
    }

    interface OnKeyboardListenersToView {
        fun onKeyboardVisibilityChanged(change: Boolean)
    }


    fun sendMessageNotification(context: Context, message: Message) {
        val notificationManager = getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager


        val channelId = NOTIF_MESSAGE_CHANNEL.toString()
        val notificationId = Constants.NOTIF_MESSAGE // Unique ID for the notification

        // Create a notification channel
        val channelName = "Receive Chat Update Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Notification channel for messaging"
        }
        // Optionally configure other channel properties
        notificationManager.createNotificationChannel(channel)

        // Create an intent to launch your activity or fragment
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("fragment", "chat_fragment") // Pass any extra data if needed

        // Create a PendingIntent to open the chat activity
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )


        // Build the notification
        val builder = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_message_24)
            .setContentTitle("New Message from ${message.senderName}")
            .setContentText(message.text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when clicked
            .setShowWhen(true)

        // Show the notification
        notificationManager.notify(notificationId, builder.build())
    }


}