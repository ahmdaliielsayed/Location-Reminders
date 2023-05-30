package com.udacity.project4.utils

import android.content.Context
import android.content.SharedPreferences
import com.udacity.project4.R

object AppPreferences {

    fun initAppPreferences(context: Context): MyPreferences {
        val sharedPreferences = providesSharedPreferences(context)
        val sharedPreferencesEditor = providesSharedPreferencesEditor(sharedPreferences)
        return providesSharedPreferencesHelper(sharedPreferences, sharedPreferencesEditor)
    }

    private fun providesSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.app_name) + "1", Context.MODE_PRIVATE)
    }

    private fun providesSharedPreferencesEditor(mSharedPreferences: SharedPreferences): SharedPreferences.Editor {
        return mSharedPreferences.edit()
    }

    private fun providesSharedPreferencesHelper(sharedPreferences: SharedPreferences, sharedPreferencesEditor: SharedPreferences.Editor): MyPreferences {
        return MyPreferences(sharedPreferences, sharedPreferencesEditor)
    }
}
