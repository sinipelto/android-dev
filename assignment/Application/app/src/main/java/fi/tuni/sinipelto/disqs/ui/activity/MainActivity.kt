package fi.tuni.sinipelto.disqs.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fi.tuni.sinipelto.disqs.BuildConfig
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.databinding.ActivityMainBinding
import fi.tuni.sinipelto.disqs.interfaces.FirebaseCallback
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.model.User
import fi.tuni.sinipelto.disqs.ui.adapter.SectionsPagerAdapter
import fi.tuni.sinipelto.disqs.util.Database
import fi.tuni.sinipelto.disqs.util.UserManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val userFile = "userdata.json"

    lateinit var appData: AppData

    private lateinit var binding: ActivityMainBinding

    private lateinit var pagerAdapter: SectionsPagerAdapter
    private lateinit var viewPager: ViewPager

    lateinit var tabLayout: TabLayout

    // FusedLocationProviderClient - Main class for receiving location updates.
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
    // should receive updates, the priority, etc.
    lateinit var locationRequest: LocationRequest

    // Client for accessing Places SDK functionality
    lateinit var placesClient: PlacesClient

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        // Init location services on MainActivity level to ensure better performance later
        configureLocationServices()

        startSignIn()
    }

    override fun onResume() {
        super.onResume()

        // Recreate toolbar context menu on resume
        // Otherwise the menu items are not updated on login/sync
        setSupportActionBar(findViewById(R.id.toolbar_main))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu_main, menu)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null && !user.isAnonymous) {
            (menu!!.findItem(R.id.authItem) as MenuItem).title =
                getString(R.string.logout_button_text)
        } else if (user != null && user.isAnonymous) {
            (menu!!.findItem(R.id.authItem) as MenuItem).title =
                getString(R.string.sync_button_text)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.authItem -> {
                //Log.d(TAG, "Auth Item Click detected.")

                val user = FirebaseAuth.getInstance().currentUser

                if (user != null && !user.isAnonymous) {
                    startSignIn(syncLogin = false, signOut = true)
                } else if (user != null && user.isAnonymous) {
                    startSignIn(syncLogin = true)
                } else {
                    throw IllegalAccessException("ERROR: NULL USER CANNOT ENTER THE APPLICATION")
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun configureLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
            // Wait until an accurate location is available
            // To have any reason to update the stored location
            .setWaitForAccurateLocation(true)

            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            .setInterval(TimeUnit.SECONDS.toMillis(60))

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            .setFastestInterval(TimeUnit.SECONDS.toMillis(5))

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            .setMaxWaitTime(TimeUnit.SECONDS.toMillis(120))

            // High accuracy
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Initialize Places API & client
        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val user = FirebaseAuth.getInstance().currentUser

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            //Log.d(
//                TAG,
//                "Auth OK: ${result.resultCode} => ${response?.idpToken} NewUser: ${response?.isNewUser}"
//            )

            // Sync local data if non-anonymous user logged in
            if (!user!!.isAnonymous) {
                val local = UserManager.getLocalUser(user, applicationContext, userFile)

                if (local.firebaseUid != user.uid) {
                    //Log.d(TAG, "Local and Remote User UID mismatch detected -> Local user override")
                    UserManager.updateLocalUser(user, local, applicationContext, userFile)
                    Toast.makeText(
                        this,
                        "Successfully synced with user account!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (!user.isAnonymous && (user.displayName != null || user.email != null)) {
                Toast.makeText(
                    this,
                    "Successfully logged in as ${user.displayName} (${user.email})!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Logged in as Guest!", Toast.LENGTH_SHORT).show()
            }

            authenticate(user)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.

            val response = result.idpResponse

            Log.e(
                TAG,
                "AUTH ERROR: ${response?.error} => ${response?.error?.message}: ${response?.error?.errorCode}"
            )

            // Show an error text and offer a second change to log in (relaunch app)
            if (result.resultCode == 0) {
                Toast.makeText(this, R.string.auth_cancel_text, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.auth_error_text, Toast.LENGTH_LONG).show()
            }

            // We cannot let a user in without being logged in at least some way (guest or real account)
            // Thus we need to relaunch the signIn Activity in such situatiom
            if (user == null) {
                Log.w(TAG, "User currently not logged in any way -> restarting signIn activity..")
                startSignIn()
            }
        }
    }

    private fun startSignIn(syncLogin: Boolean = false, signOut: Boolean = false) {
        // Choose authentication providers
        val providers = arrayListOf<AuthUI.IdpConfig>()

        if (!syncLogin) {
            providers.add(AuthUI.IdpConfig.AnonymousBuilder().build())
        }

        providers.addAll(
            listOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build(),
                AuthUI.IdpConfig.GitHubBuilder().build(),
                AuthUI.IdpConfig.MicrosoftBuilder().build(),
                AuthUI.IdpConfig.YahooBuilder().build(),
                AuthUI.IdpConfig.AppleBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build(),
            )
        )

        // Create and launch sign-in intent
        var signInIntentBuilder = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTosAndPrivacyPolicyUrls(TOS_URL, PRIVACY_URL)

        if (syncLogin) {
            signInIntentBuilder = signInIntentBuilder
                .setAlwaysShowSignInMethodScreen(true)
        }

        val signInIntent = signInIntentBuilder.build()

        val auth = FirebaseAuth.getInstance()

        // If it is about syncing the account, always show the auth UI
        if (syncLogin) {
            //Log.d(TAG, "Sync Login requested - forcelaunching Auth UI..")
            signInLauncher.launch(signInIntent)
            return
        }

        if (signOut) {
            auth.signOut()
            LoginManager.getInstance().logOut()
            //Log.d(TAG, "Signed out from Firebase, Facebook.")
        }

        // Check current auth status
        // If no current user -> launch auth UI
        // Otherwise pass the current logged in user
        // If force login requested, always launch the login intent
        if (auth.currentUser != null) {
            //Log.d(TAG, "Current user found, proceeding to create view..")
            authenticate(auth.currentUser!!)
        } else {
            //Log.d(TAG, "Current user NULL, proceeding to authUI..")
            signInLauncher.launch(signInIntent)
        }
    }

    private fun authenticate(firebaseUser: FirebaseUser) {
        try {
            Database.getUser(firebaseUser.uid, object : FirebaseCallback<User> {
                override fun onResult(result: User?) {
                    if (result != null) {
                        Database.updateUserActivity(firebaseUser.uid)
                        createView(firebaseUser, result)
                    } else {
                        //Log.d(TAG, "Remote user was NULL. Inserting user..")
                        val localUser =
                            UserManager.getLocalUser(firebaseUser, applicationContext, userFile)
                        UserManager.fillUserInfo(firebaseUser, localUser)
                        Database.insertUser(firebaseUser.uid, localUser)
                        createView(firebaseUser, localUser)
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(
                        TAG,
                        "Fetch remote user threw exception: ${t.message} => ${t.printStackTrace()}"
                    )
                    //Log.d(TAG, "Passing NULL value onResult")
                    onResult(null)
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to handle user auth: ${t.message} => ${t.printStackTrace()}")
            throw t
        }
    }

    private fun createView(firebaseUser: FirebaseUser, currentUser: User) {
        // Create the app data instance to be used within the app
        val data = AppData(
            currentUser,
            firebaseUser,
        )

        // Set appdata globally available
        appData = data

        // Inflate and configure view elements
        fillView(data)
    }

    private fun fillView(appData: AppData) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        tabLayout = binding.tabs

        pagerAdapter = SectionsPagerAdapter(this, appData, supportFragmentManager)

        // Configure view pager
        viewPager = binding.viewPager
        viewPager.adapter = pagerAdapter

        // Configure tab layout
        tabLayout.setupWithViewPager(viewPager)

        // Enable toolbar context menu
        setSupportActionBar(findViewById(R.id.toolbar_main))
    }

    companion object {
        private const val TAG = "disqs.MainActivity"

        private const val TOS_URL = "https://peltonet.com/disqs/terms"
        private const val PRIVACY_URL = "https://peltonet.com/disqs/privacy"
    }
}