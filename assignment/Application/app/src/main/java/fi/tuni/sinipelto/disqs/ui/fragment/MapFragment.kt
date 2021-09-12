package fi.tuni.sinipelto.disqs.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.databinding.FragmentMapBinding
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.User
import fi.tuni.sinipelto.disqs.model.UserMarker
import fi.tuni.sinipelto.disqs.ui.activity.MainActivity
import fi.tuni.sinipelto.disqs.ui.activity.PermissionDeniedSettingsActivity
import fi.tuni.sinipelto.disqs.ui.viewmodel.PageViewModel
import fi.tuni.sinipelto.disqs.util.Database
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A fragment containing a simple view.
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    private lateinit var map: GoogleMap

    private lateinit var mainActivity: MainActivity

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    private var visibleToUser: Boolean = false

    // Store all currently drawn user circles to determine and prevent overlapping
    private val userMarkers: MutableList<UserMarker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Log.d(TAG, "ONCREATE")

        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }

        // Set the root parent activity in memory to access the activity data
        mainActivity = requireActivity() as MainActivity

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult?.lastLocation == null) return

                super.onLocationResult(locationResult)

                //Log.d(TAG, "Received Location Update!")

                if (locationResult.lastLocation.isFromMockProvider) {
                    handleMockLocation()
                } else {
                    // Store latest location update
                    currentLocation = locationResult.lastLocation

                    // Store user location in database
                    updateUserLocation(locationResult.lastLocation, null)

                    // Update map with updated details
                    populateMap(false)
                }
            }
        }

        //Log.d(TAG, "Location callback initialized.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.d(TAG, "ONCREATEVIEW")

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root = binding.root

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Log.d(TAG, "ONVIEWCREATED")
        if (visibleToUser) {
            startMap()
        }
    }

    override fun onResume() {
        super.onResume()
        //Log.d(TAG, "ONRESUME")
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //Log.d(TAG, "ONPAUSE")
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()

        mainActivity.fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
            .addOnSuccessListener {
                //Log.d(TAG, "Location Callback removed.")
            }.addOnFailureListener {
                Log.e(TAG, "Failed to remove Location Callback.")
            }
    }

    @Suppress("Deprecation")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //Log.d(TAG, "Map READY.")

        // Ensure location permissions granted
        // If not, load contents only after grantedresult
        if (!requiredPermissionsGranted()) {
            Log.e(TAG, "ERROR: Location permissions not granted.")
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            //Log.d(TAG, "Location Permissions OK. Proceeding to map population..")
            populateMap(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        //Log.d(TAG, "PermissionsResult called.")

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (requiredPermissionsGranted()) {
                attachLocationListener()
                startMap()
            } else {
                Snackbar.make(
                    requireContext(),
                    requireView(),
                    getString(R.string.location_perms_denied),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(
                        R.string.settings_button,
                        PermissionDeniedSettingsActivity(requireContext(), requireActivity())
                    )
                    .show()
            }
        }
    }

    @Suppress("Deprecation")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        visibleToUser = isVisibleToUser
        super.setUserVisibleHint(isVisibleToUser)

        //Log.d(
//            TAG,
//            "UserVisibleHint called. VisibleToUser: $isVisibleToUser - Activity: $activity - Context: $context"
//        )

        if (isVisibleToUser && activity != null) {
            startMap()
        }
    }

    companion object {
        private const val TAG = "disqs.MapFragment"

        private const val CIRCLE_RADIUS = 10000.0 // 10.0 KM

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        private const val REQUEST_CODE_PERMISSIONS = 20

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): MapFragment {
            return MapFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun updateUserLocation(location: Location?, nearestPlace: Place?) {
        if (location == null && nearestPlace == null) {
            Log.w(TAG, "Both parameters provided were null. Omitting update..")
            return
        }

        Database.updateUserLocation(
            mainActivity.appData.firebaseUser.uid,
            location,
            nearestPlace?.id,
            nearestPlace?.latLng
        )
    }

    @Suppress("Deprecation")
    private fun startMap() {
        //Log.d(TAG, "Starting map..")

        // Ensure location permissions granted
        // If not, load contents only after grantedresult
        if (!requiredPermissionsGranted()) {
            Log.e(TAG, "ERROR: Location permissions not granted.")
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            //Log.d(TAG, "Location Permissions OK. Proceeding to map configuration..")
            attachLocationListener()
            mapView.getMapAsync(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun attachLocationListener() {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()

        mainActivity.fusedLocationProviderClient.requestLocationUpdates(
            mainActivity.locationRequest,
            locationCallback,
            looper,
        )

        //Log.d(TAG, "Location updates listener attached.")
    }

    @SuppressLint("MissingPermission")
    private fun populateMap(focus: Boolean) {
        //Log.d(TAG, "Populating map...")

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)

        // Ensure latest location available
        if (currentLocation == null) {
            mainActivity
                .fusedLocationProviderClient
                .lastLocation
                .addOnSuccessListener {
                    if (it == null) return@addOnSuccessListener

                    if (it.isFromMockProvider) {
                        handleMockLocation()
                    } else {
                        currentLocation = it
                        updateUserLocation(it, null)
                        populateMap(true)
                    }
                }
                .addOnFailureListener {
                    Log.e(
                        TAG,
                        "ERROR: Failed to get initial location: ${it.message} => ${it.printStackTrace()}"
                    )
                }
        }

        currentLocation ?: return

        // Get current location as LatLng
        val currentLoc = asLatLng(currentLocation!!)

        // If user location focus requested, set the map to user location
        if (focus) {
            // Update camera
            //Log.d(TAG, "Focusing camera..")
            val cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(currentLoc, 10F)

            map.animateCamera(cameraUpdate)
        }

        Database.getAllUsers(object : FirebaseCallback<List<User>> {
            override fun onResult(result: List<User>?) {
                if (context == null) return

                if (!result.isNullOrEmpty()) {

                    // Update current user nearest location if necessary (to save unnecessary API calls)
                    // Determine if user location has changed too much from the nearest Places location
                    result.firstOrNull { it.firebaseUid == mainActivity.appData.firebaseUser.uid }
                        ?.run {

                            val nearLoc = if (this.nearestPointLocation == null) null else {
                                asLatLng(this.nearestPointLocation!!)
                            }

                            if (nearLoc == null || pointDistance(
                                    nearLoc,
                                    currentLoc
                                ) > (CIRCLE_RADIUS / 2) // If the current location has changed over half circle radius from the current nearest point
                            ) {
                                // Find closest place (location) to the user and store its ID
                                val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)
                                val placeRequest =
                                    FindCurrentPlaceRequest.builder(placeFields).build()
                                mainActivity.placesClient.findCurrentPlace(placeRequest)
                                    .addOnSuccessListener { placeResponse ->
                                        //Log.d(TAG, "Place request successful.")
                                        // Pick a random place close to the user
                                        // Using randomness aids from not taking a place too close to the user with high odds
                                        val closest =
                                            placeResponse.placeLikelihoods.filter {
                                                val dist = pointDistance(
                                                    currentLoc,
                                                    it.place.latLng!!
                                                )

                                                dist < CIRCLE_RADIUS // Ensure place is within the circle radius from the user at most
                                            }
                                                .randomOrNull()
                                        if (closest != null && closest.place.id != null && closest.place.latLng != null) {
                                            //Log.d(
                                            //TAG,
                                            //"Closest place: ${closest.place.name} - ${closest.place.address} == Likelihood: ${closest.likelihood}"
                                            //)
                                            updateUserLocation(null, closest.place)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e(
                                            TAG,
                                            "ERROR: Place request failed: ${it.message} => ${it.printStackTrace()}"
                                        )
                                    }
                            }
                        }

                    // If user data fetched successfully
                    // First, clear out any old user markers
                    userMarkers.clear()

                    // Loop through users and collect markers accordingly
                    result.forEach { user ->
                        // Ensure location available
                        if (user.nearestPointLocation != null) {
                            val loc = asLatLng(user.nearestPointLocation!!)

                            // Also ensure no circle is too close
                            // No closer than diamter of the circle, distance <= radius to this one to draw it
                            if (userMarkers.none { pointDistance(loc, it.pos) <= CIRCLE_RADIUS }) {
                                //Log.d(
//                                    TAG,
//                                    "No markers found closer than $limit metres. Adding new circle.."
//                                )
                                userMarkers.add(UserMarker(loc, 1))
                            }
                            // Otherwise find the closest existing cicle, and add count to it
                            else {
                                val closest = userMarkers.minByOrNull { pointDistance(loc, it.pos) }
                                //Log.d(
//                                    TAG,
//                                    "Found a nearby circle: Closest circle: ${closest?.pos} VS. $loc"
//                                )
                                if (closest != null) {
                                    closest.count += 1
                                }
                            }
                        }
                    }

                    drawLocationCircles()
                }
            }

            override fun onError(t: Throwable) {
                Log.e(
                    TAG,
                    "ERROR: Failed to read users from DB: ${t.message} => ${t.printStackTrace()}"
                )
            }
        })
    }

    private fun handleMockLocation() {
        if (context == null) return

        Toast.makeText(
            requireContext(),
            getString(R.string.mock_gps_error_text),
            Toast.LENGTH_LONG
        ).show()
        currentLocation = null
        userMarkers.clear()
        map.clear()
    }

    private fun drawLocationCircles() {
        if (context == null) return

        // On User Data fetch success, first, clear any old markers
        map.clear()

        //Log.d(TAG, "Drawing user circles..")
        userMarkers.forEach {
            val opts = CircleOptions()
            opts
                .visible(true)
                .center(it.pos)
                .radius(CIRCLE_RADIUS)
                .strokeColor(Color.MAGENTA)
                .strokeWidth(3F)
                .fillColor(0x40DD99EE)

            val mrkOpts = MarkerOptions()
            mrkOpts
                .visible(true)
                .draggable(false)
                .position(it.pos)
                .title(getString(R.string.users_marker_title) + " " + it.count.toString())

            map.addCircle(opts)
            map.addMarker(mrkOpts)
        }
    }

    private fun asLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    private fun asLatLng(location: GeoPoint): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    private fun pointDistance(p1: LatLng, p2: LatLng): Double {
        // sqrt( (x2-x1)^2 + (y2-y1)^2 ) converted to metres
        return sqrt(((p2.latitude - p1.latitude).pow(2)) + ((p2.longitude - p1.longitude).pow(2))) * 100000
    }

    private fun requiredPermissionsGranted(): Boolean {
        if (context == null) return false

        for (perm in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }

        return true
    }
}