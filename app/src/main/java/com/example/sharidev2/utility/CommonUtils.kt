package com.example.sharidev2.utility

import android.content.Context
import android.location.Location
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.core.content.ContentProviderCompat.requireContext
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
    fun closeKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}