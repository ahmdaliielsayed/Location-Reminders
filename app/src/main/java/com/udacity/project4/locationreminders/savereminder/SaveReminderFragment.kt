package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.AppConstants.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.AppConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.AppConstants.LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.AppConstants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.utils.AppConstants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.AppConstants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.utils.AppConstants.TAG_SAVE_REMINDER
import com.udacity.project4.utils.AppConstants.ZERO
import com.udacity.project4.utils.AppConstants.ZERO_D
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.snackBar
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    private lateinit var reminderDataItem: ReminderDataItem

    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.locationreminders.action.ACTION_GEOFENCE_EVENT"
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(requireContext(), ZERO, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(requireContext(), ZERO, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@SaveReminderFragment

            selectLocation.setOnClickListener {
                // Navigate to another fragment to get the user location
                val directions = SaveReminderFragmentDirections
                    .actionSaveReminderFragmentToSelectLocationFragment()
                _viewModel.navigationCommand.value = NavigationCommand.To(directions)
            }

            saveReminder.setOnClickListener {
                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value

                reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

                if (_viewModel.validateEnteredData(reminderDataItem)) {
                    if (foregroundAndBackgroundLocationPermission()) {
                        setSettingLocationAndStartGeofence()
                    } else {
                        requestForegroundAndBackgroundLocationPermissions()
                    }
                }
            }
        }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
    }

//    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermission(): Boolean {
        val foregroundLocationApproved =
            (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                )
        val backgroundPermissionApproved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun setSettingLocationAndStartGeofence(b: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationBuilder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
        }

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(locationBuilder.build()).apply {
                addOnSuccessListener {
                    createGeoFenceForRemainder()
                }
                addOnFailureListener {
                    if (it is ResolvableApiException && b) {
                        try {
                            startIntentSenderForResult(
                                it.resolution.intentSender,
                                REQUEST_TURN_DEVICE_LOCATION_ON,
                                null,
                                ZERO,
                                ZERO,
                                ZERO,
                                null
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.d(
                                TAG_SAVE_REMINDER,
                                "Error getting location settings resolution: " + sendEx.message
                            )
                        }
                    } else {
                        snackBar(getString(R.string.location_required_error))
                            ?.setAction(getString(R.string.try_again)) {
                                setSettingLocationAndStartGeofence()
                            }
                            ?.show()
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun createGeoFenceForRemainder() {
        val geofenceBuilder = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude ?: ZERO_D,
                reminderDataItem.longitude ?: ZERO_D,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceBuilder)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
            }
        }
    }

//    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermission()) {
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(permissionsArray, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (
                requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
                )
        ) {
            Snackbar.make(binding.root, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings) {
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }.show()
        } else {
            setSettingLocationAndStartGeofence()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                createGeoFenceForRemainder()
            } else {
                setSettingLocationAndStartGeofence(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
