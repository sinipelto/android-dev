package fi.tuni.sinipelto.laskuvelho.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import fi.tuni.sinipelto.laskuvelho.R
import fi.tuni.sinipelto.laskuvelho.adapter.InvoiceListAdapter
import fi.tuni.sinipelto.laskuvelho.application.MainApplication
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.model.DateFormatter
import fi.tuni.sinipelto.laskuvelho.viewmodel.InvoiceViewModel
import fi.tuni.sinipelto.laskuvelho.viewmodel.InvoiceViewModelFactory
import java.math.RoundingMode
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "laskuvelho.MainActivity"

    private val invoiceViewModel: InvoiceViewModel by viewModels {
        InvoiceViewModelFactory((application as MainApplication).repo)
    }

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "VIEW.ONCREATE")
        createView()
    }

    private fun createView() {
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            Log.d(TAG, "Fab button clicked.")
            val intent = Intent(this@MainActivity, NewInvoiceActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, RC_NEW_INVOICE)
        }

        val total = findViewById<TextView>(R.id.invoicesTotalValueTextView)
        val due = findViewById<TextView>(R.id.invoicesDueValueTextView)
        val sum = findViewById<TextView>(R.id.invoicesSumValueTextView)
        val info = findViewById<TextView>(R.id.emptyInfoTextVIew)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        info.visibility = View.GONE

        Log.d(TAG, "Creating recycler view..")

        val adapter = InvoiceListAdapter(invoiceViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Register livedata observer to ui adapter
        invoiceViewModel.getAll().observe(this, { i -> i?.let { adapter.submitList(it) } })

        invoiceViewModel.allInvoices.observe(this, { it ->
            if (it.isEmpty()) {
                info.visibility = View.VISIBLE
            } else {
                info.visibility = View.GONE
            }

            @SuppressLint("SetTextI18n")
            total.text = "${it.size} kpl"
            val dueCount =
                it.filter { x -> x.dueDate!!.before(DateFormatter.getTodayMidnight()) }.size
            @SuppressLint("SetTextI18n")
            due.text = "$dueCount kpl"
            if (dueCount > 0) {
                val span = Spannable.Factory().newSpannable(due.text)
                span.setSpan(
                    ForegroundColorSpan(DataConstants.DANGER_TEXT_COLOR),
                    0,
                    due.text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                due.text = span
            }
            @SuppressLint("SetTextI18n")
            sum.text = "${
                it.map { it.amount!! }.sum().toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            } ${getString(R.string.currency_eur)}"
        })

        Log.d(TAG, "Recycler view configured.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "VIEW.ONSTART")
        // onStart called after sign-in at highest level if app not killed.
        checkSignIn()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "VIEW.ONRESUME")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "VIEW.ONPAUSE")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "VIEW.ONSTOP")
        // Ensure recyclerview cleaned
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VIEW.ONDESTROY")
    }

    private fun checkSignIn() {
        // Check sign-in status
        // If user not logged in, Create and launch sign-in intent
        if (firebaseAuth.currentUser == null) {
            Log.d(TAG, "User not signed in. Begin auth..")
            beginAuth()
        } else {
            Log.d(TAG, "Sign in status ok.")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.itemId

        if (id == R.id.logoutItem) {
            Log.d(TAG, "Logout requested using logout menuitem")
            AuthUI.getInstance().signOut(applicationContext).addOnCompleteListener {
                if (firebaseAuth.currentUser != null) {
                    Log.e(TAG, "Signout failed: CurrentUser not null.")
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.logout_failed),
                        Toast.LENGTH_LONG
                    ).show()
                    beginAuth()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.logout_success),
                        Toast.LENGTH_LONG
                    ).show()
                    beginAuth()
                }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun beginAuth() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build(),
        )

        ActivityCompat.startActivityForResult(
            this@MainActivity,
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher)
                .setTosAndPrivacyPolicyUrls(
                    "https://peltonet.com/laskuvelho/terms",
                    "https://peltonet.com/laskuvelho/privacy"
                )
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN,
            Bundle.EMPTY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(intentData)
            Log.d(TAG, "Callback from RC_SIGN_IN received.")

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Log.d(
                    TAG,
                    "AUTH OK. USER: ${firebaseAuth.currentUser} UID: ${firebaseAuth.currentUser?.uid}"
                )
                createView()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.d(TAG, "AUTH FAILED with resp: $response => ERROR: ${response?.error} CODE: ${response?.error?.errorCode}")

                Toast.makeText(
                    applicationContext,
                    getString(R.string.login_failed) + " (err: $resultCode)",
                    Toast.LENGTH_LONG
                ).show()

                // Retry authentication
                beginAuth()
            }
        } else if (requestCode == RC_NEW_INVOICE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val invoice =
                        intentData?.getSerializableExtra(NewInvoiceActivity.INVOICE_ENTITY)
                    Log.d(TAG, "Got entity from intent: $invoice")
                    invoiceViewModel.insert(invoice as InvoiceEntity)
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.insert_success),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Throwable) {
                    Log.e(TAG, "Got exception while parsing invoice entity: ${e.message}", e)
                    // Tell user an error occurred while new invoice add failed
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.insert_error),
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                Log.e(TAG, "ERROR: Failed to add invoice: ResultCode was not RESULT_OK.")
                Toast.makeText(
                    applicationContext,
                    getString(R.string.insert_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val RC_NEW_INVOICE = 999
        private const val RC_SIGN_IN = 998
    }
}