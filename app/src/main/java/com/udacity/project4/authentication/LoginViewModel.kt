package com.udacity.project4.authentication

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val _firebaseUser = MutableLiveData(FirebaseAuth.getInstance().currentUser)
    val firebaseUserState: LiveData<FirebaseUser?> = _firebaseUser

    fun setFirebaseAuth(currentUser: FirebaseUser?) {
        _firebaseUser.value = currentUser
    }

    fun initAuthUI(them: Int, logo: Int, intent: (Intent) -> Unit) {
        // Sign in options
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        intent(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(logo)
                .setTheme(them)
                .setAvailableProviders(providers)
                .build()
        )
    }
}
