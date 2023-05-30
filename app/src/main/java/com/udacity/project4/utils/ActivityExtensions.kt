package com.udacity.project4.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar

fun Activity.snackBarError(
    text: String,
    @ColorInt bgColor: Int,
    @ColorInt tvColor: Int = Color.WHITE,
    duration: Int = Snackbar.LENGTH_LONG
) {
    val snackBar = Snackbar.make(findViewById(android.R.id.content), text, duration)
    snackBar.apply {
        view.setBackgroundColor(bgColor)
        val mTextView = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        mTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        setTextColor(tvColor)
        show()
    }
}

fun Activity.snackBar(
    text: String,
    duration: Int = Snackbar.LENGTH_LONG
): Snackbar {
    return Snackbar.make(findViewById(android.R.id.content), text, duration).apply {
        val mTextView = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        mTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }
}

fun Activity.clearIntentClass(cls: Class<*>?, vararg extra: Pair<String, Any>) {
    val intent = intentClass(cls)
    extra.forEach {
        when (it.second) {
            is String -> { intent.putExtra(it.first, it.second as String) }
            is Float -> { intent.putExtra(it.first, it.second as Float) }
            is Double -> { intent.putExtra(it.first, it.second as Double) }
            is Int -> { intent.putExtra(it.first, it.second as Int) }
            is Boolean -> { intent.putExtra(it.first, it.second as Boolean) }
        }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.intentClass(cls: Class<*>?): Intent {
    return Intent(this, cls)
}
