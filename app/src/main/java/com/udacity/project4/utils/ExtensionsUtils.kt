package com.udacity.project4.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun Context.checkPermissionUtils(permission: String) = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, permission)