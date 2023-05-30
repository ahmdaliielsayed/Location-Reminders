package com.udacity.project4.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.udacity.project4.utils.AppConstants.EMPTY
import com.udacity.project4.utils.AppConstants.ZERO
import com.udacity.project4.utils.AppConstants.ZERO_L

class MyPreferences(private val mSharedPreferences: SharedPreferences, private val mSharedPreferencesEditor: SharedPreferences.Editor) {

    init {
        mSharedPreferencesEditor.apply()
    }

    fun setValue(key: String, value: Any?) {
        when (value) {
            is Int? -> {
                mSharedPreferencesEditor.putInt(key, value ?: ZERO)
                mSharedPreferencesEditor.apply()
            }
            is String? -> {
                mSharedPreferencesEditor.putString(key, value)
                mSharedPreferencesEditor.apply()
            }
            is Double? -> {
                mSharedPreferencesEditor.putString(key, value.toString())
                mSharedPreferencesEditor.apply()
            }
            is Long? -> {
                mSharedPreferencesEditor.putLong(key, value ?: ZERO_L)
                mSharedPreferencesEditor.apply()
            }
            is Boolean? -> {
                mSharedPreferencesEditor.putBoolean(key, value ?: false)
                mSharedPreferencesEditor.apply()
            }
            is Any -> {
                mSharedPreferencesEditor.putString(key, Gson().toJson(value))
                mSharedPreferencesEditor.apply()
            }
        }
    }

    fun getStringValue(key: String, defaultValue: String = EMPTY): String {
        return mSharedPreferences.getString(key, defaultValue) ?: EMPTY
    }

    fun getIntValue(key: String, defaultValue: Int = ZERO): Int {
        return mSharedPreferences.getInt(key, defaultValue)
    }

    fun getLongValue(key: String, defaultValue: Long = ZERO_L): Long {
        return mSharedPreferences.getLong(key, defaultValue)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean = false): Boolean {
        return mSharedPreferences.getBoolean(key, defaultValue)
    }

    fun removeKey(key: String) {
        mSharedPreferencesEditor.remove(key)
        mSharedPreferencesEditor.apply()
    }

    fun clear() {
        mSharedPreferencesEditor.clear().apply()
    }
}
