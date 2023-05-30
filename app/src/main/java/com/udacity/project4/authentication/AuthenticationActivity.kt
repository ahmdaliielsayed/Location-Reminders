package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.MyApp.Companion.appPreferences
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.AppConstants.USER
import com.udacity.project4.utils.clearIntentClass
import com.udacity.project4.utils.snackBarError
import org.koin.android.ext.android.inject

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val dataBinder get() = _binding!!

    private val mViewModel: LoginViewModel by inject()

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(dataBinder.root)

        dataBinder.activity = this

        setUpViewModelStateObservers()
    }

    private fun setUpViewModelStateObservers() {
        mViewModel.firebaseUserState.observe(this) {
            if (it != null) {
                appPreferences.setValue(USER, it)
                snackBarError(
                    text = "${resources.getString(R.string.welcome)} ${it.displayName}",
                    bgColor = resources.getColor(R.color.white),
                    tvColor = resources.getColor(R.color.templateGreen)
                )
                clearIntentClass(RemindersActivity::class.java, Pair(USER, it))
            }
        }
    }

    fun onUiAuthClick() {
        mViewModel.initAuthUI(R.style.GreenTheme, R.drawable.firebase) {
            signInLauncher.launch(Intent(it))
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            mViewModel.setFirebaseAuth(user)
        } else {
            // Sign in failed.
            // If response is null the user canceled the sign-in flow using the back button.
            // Otherwise check response.getError().getErrorCode() and handle the error.
            snackBarError(
                text = response?.error?.errorCode.toString(),
                bgColor = resources.getColor(R.color.white),
                tvColor = resources.getColor(R.color.templateRed)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataBinder.unbind()
        _binding = null
    }
}
