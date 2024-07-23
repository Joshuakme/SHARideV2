package com.example.sharide.utility

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.sharide.R

class LoadingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}