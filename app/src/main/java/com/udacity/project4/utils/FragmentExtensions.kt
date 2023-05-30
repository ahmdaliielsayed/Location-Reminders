package com.udacity.project4.utils

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snackBar(
    text: String,
    duration: Int = Snackbar.LENGTH_LONG
): Snackbar? = activity?.run {
    snackBar(text, duration)
}
