package fi.tuni.sinipelto.gpsmapapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), MultiplePermissionsListener, OnMapReadyCallback {

    private val TAG = "fi.tuni.sinipelto.gpsmapapp.mainactivity"

    private lateinit var mainView: View
    private lateinit var locationValue: TextView
    private lateinit var locationCount: TextView
    private lateinit var mapView: MapView

    // List of requred permissions for the application
    private val requiredPermissions: List<String> = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    // Configure required structure for receiving location updates
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    // Store user saved locations in this list
    private val savedLocations: MutableList<Location> = mutableListOf()

    private lateinit var googleMap: GoogleMap
    private var mapConfigured = false
    private var follow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind content view
        mainView = findViewById(android.R.id.content)
        locationValue = findViewById(R.id.locationValue)
        locationCount = findViewById(R.id.locationCountValue)

        findViewById<Button>(R.id.saveLocationButton).setOnClickListener {
            if (currentLocation != null) {
                storeLocation(currentLocation!!)
                Log.d(TAG, "INFO: Location stored.")
            } else {
                Log.d(TAG, "ERROR: Current Location was null. Not stored.")
            }
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            clearLocations()
        }

        findViewById<Button>(R.id.followButton).setOnClickListener {
            it as Button
            if (follow) {
                it.text = getString(R.string.location_follow_on)
                follow = false
            } else {
                it.text = getString(R.string.location_follow_off)
                follow = true
            }
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@MainActivity)

        locationCount.text = savedLocations.count().toString()
        initLocationProvider()
    }

    override fun onStart() {
        super.onStart()

        // Ensure permissions asked or granted
        // every time the acitivty is started
        checkPerms()
        // prev method calls registerLocationListener()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        // Stop receiving location updates
        // if the app is not on foreground
        // to optimize battery usage
        unregisterLocationListener()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    // Start listening location updates
    private fun registerLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this@MainActivity, "Oikeudet sijaintiin on evätty.", Toast.LENGTH_SHORT)
                .show()
            checkPerms()
            return
        }

        // Init looper for requestor
        val looper: Looper = if (Looper.myLooper() == null) {
            Log.e(TAG, "MyLooper was null. Using MainLooper")
            Looper.getMainLooper()
        } else {
            Looper.myLooper()!!
        }

        // Request new location updates
        fusedLocationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            looper
        )

        // Set UI to let user know we are pending current location..
        locationValue.text = getString(R.string.location_pending)

        Log.d(TAG, "Location callback registered.")
    }

    // Stop receiving location updates
    private fun unregisterLocationListener() {
        // Unregister location callback from provider
        fusedLocationProvider.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location callback unregistered.")
    }

    private fun initLocationProvider() {
        // Initialize the fused location provider to pass requests on
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        // Create the location request object to pass for location provider
        locationRequest = LocationRequest.create()
            .setInterval(TimeUnit.SECONDS.toMillis(5))
            .setFastestInterval(TimeUnit.SECONDS.toMillis(3))
            .setMaxWaitTime(TimeUnit.SECONDS.toMillis(15))
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create a callback object to handle received location updates
        locationCallback = object : LocationCallback() {

            // Changes on location availability
            override fun onLocationAvailability(availability: LocationAvailability) {
                super.onLocationAvailability(availability)

                if (!availability.isLocationAvailable) {
                    Log.e(TAG, "Location not available!")
                    locationValue.text = getString(R.string.location_unavailable)
                }
            }

            // Handle received locations
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                Log.d(TAG, "Received new location update.")
                currentLocation = result.lastLocation
                locationUpdate()
            }
        }
        Log.d(TAG, "Location provider initialized.")
    }

    // Updates the current device location to the app
    @SuppressLint("SetTextI18n")
    private fun locationUpdate() {
        if (currentLocation == null) {
            Log.e(TAG, "ERROR: Current location null.")
            return
        }

        locationValue.text =
            "LAT: ${
                BigDecimal(
                    currentLocation!!.latitude,
                    MathContext(4, RoundingMode.HALF_UP)
                )
            } LNG: ${
                BigDecimal(
                    currentLocation!!.longitude,
                    MathContext(4, RoundingMode.HALF_UP)
                )
            } ALT: ${
                BigDecimal(
                    currentLocation!!.altitude,
                    MathContext(4, RoundingMode.HALF_UP)
                )
            } SPD: ${
                BigDecimal(
                    currentLocation!!.speed.toDouble(),
                    MathContext(4, RoundingMode.HALF_UP)
                )
            }"

        // Configure map here (as both location permission granted and location available)
        if (!mapConfigured) {
            configureMap()
            mapConfigured = true
            Log.d(TAG, "Map configured.")
        }

        if (follow) {
            val pos = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15F))
        }
    }

    private fun storeLocation(loc: Location) {
        savedLocations.add(loc)
        val marker = MarkerOptions()
        marker.position(LatLng(loc.latitude, loc.longitude))
        googleMap.addMarker(marker)
        locationCount.text = savedLocations.count().toString()
        Log.d(TAG, "Location stored.")
    }

    private fun clearLocations() {
        googleMap.clear()
        savedLocations.clear()
        locationCount.text = savedLocations.count().toString()
        Log.d(TAG, "Locations cleared.")
    }

    @SuppressLint("MissingPermission")
    private fun configureMap() {
        // If not yet configured, configure the mylocation features enabled
        // on the map to see the current device location and zoom buttons
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map == null) {
            Log.e(TAG, "Failed to receive GoogleMap.")
            return
        }
        googleMap = map
        Log.d(TAG, "Map ready.")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPerms()
            return
        }
    }

    private fun checkPerms() {
        Dexter.withContext(this@MainActivity)
            .withPermissions(requiredPermissions)
            .withListener(this@MainActivity)
            .check()
    }

    private fun onAllPermissionsGranted() {
        registerLocationListener()
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report == null) {
            Log.w(TAG, "WARN: Report was null.")
            return
        }

        Log.d(
            TAG,
            "DENIED PERMS: ${report.deniedPermissionResponses.joinToString { it.permissionName }}"
        )

        if (report.isAnyPermissionPermanentlyDenied) {
            Log.e(TAG, "ERROR: At least one Permission permanently denied.")
        }

        if (!report.areAllPermissionsGranted()) {
            Log.e(TAG, "ERROR: Permissions denied: ${report.deniedPermissionResponses}")

            Snackbar.make(
                this@MainActivity,
                mainView,
                getString(R.string.permission_denied_text),
                Snackbar.LENGTH_LONG
            ).setAction(
                getString(R.string.settings_shortcut),
                PermissionDeniedAction(this@MainActivity, this@MainActivity)
            )
                .show()
        } else {
            onAllPermissionsGranted()
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
        if (permissions == null) {
            Log.w(TAG, "WARN: Permissions were null.")
            return
        }
        if (token == null) {
            Log.w(TAG, "WARN: Token was null.")
            return
        }

        token.continuePermissionRequest()
    }
}