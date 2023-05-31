package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.AppConstants.DEFAULT_ZOOM_LEVEL
import com.udacity.project4.utils.AppConstants.PERMISSION_CODE_LOCATION_REQUEST
import com.udacity.project4.utils.AppConstants.TAG_SELECT_LOCATION
import com.udacity.project4.utils.checkPermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment() {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private var selectedPOI: PointOfInterest? = null
    private var isRequestingLocationUpdates = false

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
        }
    }

    private val locationRequest by lazy {
        LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        // Create a LatLngBounds object. --> start to egypt map
        val egyptBounds = LatLngBounds.builder()
            .include(LatLng(31.4021, 25.0534))
            .include(LatLng(21.8623, 36.7628))
            .build()

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(egyptBounds, 20))

        if (isLocationPermissionDENIED()) {
            requestLocationPermission()
        } else {
            enableMyLocation()
            getUserLocation()
        }

        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
        setOnPoiClick(map)
        setOnMapDataClick(map)
    }

    private fun isLocationPermissionDENIED(): Boolean = !requireContext().checkPermissionUtils(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // For APIs < N
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE_LOCATION_REQUEST)
        }
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                setMapStyle(map)
                setPoiClick(map)
                setMapLongClick(map)
                setOnPoiClick(map)
                enableMyLocation()
                getUserLocation()
                setOnMapDataClick(map)
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG_SELECT_LOCATION, "Styling failed!")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG_SELECT_LOCATION, "Style not found. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.showInfoWindow()

            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude)
            // map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .draggable(true)
            )

            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun setOnPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            selectedPOI = poi
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                poi.latLng.latitude,
                poi.latLng.longitude
            )
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(snippet)
                    .draggable(true)
            )
            marker?.showInfoWindow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        startLocationUpdates(locationRequest)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isRequestingLocationUpdates = true
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM_LEVEL))
                    marker = map.addMarker(
                        MarkerOptions()
                            .position(userLocation)
                            .title(getString(R.string.my_location))
                    )
                    marker?.showInfoWindow()
                } else {
                    val builder = AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.error_last_location_null))
                        .setPositiveButton(getString(android.R.string.ok), null)
                    builder.show()
                }
            }
    }

    private fun setOnMapDataClick(map: GoogleMap) {
        map.setOnInfoWindowClickListener { marker ->
            val builder = android.app.AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.alert_prompt_for_option))
                .setPositiveButton(getString(R.string.alert_add_location)) { _, _ ->
                    onLocationSelected()
                }
                .setNegativeButton(getString(R.string.alert_delete_location)) { _, _ -> marker.remove() }
                .setNeutralButton(getString(R.string.alert_cancel_dialog), null)
            builder.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.apply {
            viewModel = _viewModel
            lifecycleOwner = this@SelectLocationFragment

            saveBtn.setOnClickListener {
                onLocationSelected()
            }
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        setMenu()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.map_options, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.normal_map -> {
                            map.mapType = GoogleMap.MAP_TYPE_NORMAL
                            return true
                        }
                        R.id.hybrid_map -> {
                            map.mapType = GoogleMap.MAP_TYPE_HYBRID
                            return true
                        }
                        R.id.satellite_map -> {
                            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                            return true
                        }
                        R.id.terrain_map -> {
                            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                            return true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun onLocationSelected() {
        if (marker != null) {
            _viewModel.selectedPOI.value = selectedPOI
            _viewModel.latitude.value = marker?.position?.latitude
            _viewModel.longitude.value = marker?.position?.longitude
            _viewModel.reminderSelectedLocationStr.value = marker?.title
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            requestLocationPermission()
            Toast.makeText(requireContext(), getString(R.string.location_permission_message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            getUserLocation()
        } else {
            showRationalePermission()
        }
    }

    private fun showRationalePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.location_permission)
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _ ->
                    requestLocationPermission()
                }
                .create()
                .show()
        } else {
            requestLocationPermission()
        }
    }
}
