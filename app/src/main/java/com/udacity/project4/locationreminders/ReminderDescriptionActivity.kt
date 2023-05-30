package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.AppConstants.ZERO
import com.udacity.project4.utils.AppConstants.ZERO_D
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding

    private val mViewModel: RemindersListViewModel by viewModel()

    private val geofencingClient: GeofencingClient by lazy { LocationServices.getGeofencingClient(this) }
    private lateinit var mGoogleMap: GoogleMap
    private var userLocation: LatLng? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap = googleMap

        // Create a LatLngBounds object. --> start to egypt map
        val egyptBounds = LatLngBounds.builder()
            .include(LatLng(31.4021, 25.0534))
            .include(LatLng(21.8623, 36.7628))
            .build()

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(egyptBounds, 20))

        moveCameraToLocation()
    }

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)

        (intent.getSerializableExtra(EXTRA_ReminderDataItem) as? ReminderDataItem?)?.let { item ->
            userLocation = LatLng(
                item.latitude ?: ZERO_D,
                item.longitude ?: ZERO_D
            )
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(callback)
            binding.reminderDataItem = item

            binding.delete.visibility = View.VISIBLE
            binding.delete.setOnClickListener {
                mViewModel.deleteReminder(item.id)
                geofencingClient.removeGeofences(listOf(item.id)).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.remove_geofence_success), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, getString(R.string.remove_geofence_fail), Toast.LENGTH_LONG).show()
                    }
                }
                finish()
            }
        }
    }

    private fun moveCameraToLocation() {
        val marker = MarkerOptions()
            .position(userLocation!!)
            .icon(getBitmapFromVector(this, R.drawable.ic_marker_user))

        mGoogleMap.addMarker(marker)
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15.0f))
    }

    private fun getBitmapFromVector(context: Context, resId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, resId)
        vectorDrawable?.setBounds(ZERO, ZERO, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable?.intrinsicWidth ?: ZERO,
            vectorDrawable?.intrinsicHeight ?: ZERO,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
