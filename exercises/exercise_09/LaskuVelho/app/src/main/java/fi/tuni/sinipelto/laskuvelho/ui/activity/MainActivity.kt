package fi.tuni.sinipelto.laskuvelho.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
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

    private val newInvoiceActivityRequestCode = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val total = findViewById<TextView>(R.id.invoicesTotalValueTextView)
        val due = findViewById<TextView>(R.id.invoicesDueValueTextView)
        val sum = findViewById<TextView>(R.id.invoicesSumValueTextView)
        val info = findViewById<TextView>(R.id.emptyInfoTextVIew)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        FirebaseFirestore.setLoggingEnabled(true)

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

            total.text = "${it.size} kpl"
            val dueCount =
                it.filter { x -> x.dueDate!!.before(DateFormatter.getTodayMidnight()) }.size
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
            sum.text = "${
                it.map { it.amount!! }.sum().toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            } ${getString(R.string.currency_eur)}"
        })

        Log.d(TAG, "Recycler view configured.")

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            Log.d(TAG, "Fab button clicked.")
            val intent = Intent(this@MainActivity, NewInvoiceActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, newInvoiceActivityRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == newInvoiceActivityRequestCode && resultCode == Activity.RESULT_OK) {
            try {
                val invoice = intentData?.getSerializableExtra(NewInvoiceActivity.INVOICE_ENTITY)
                Log.d(TAG, "Got entity from intent: $invoice")
                invoiceViewModel.insert(invoice as InvoiceEntity)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.insert_success),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Throwable) {
                Log.e(TAG, "Got exception while parsing invoice entity", e)
                // Tell user an error occurred while new invoice add failed
                Toast.makeText(
                    applicationContext,
                    getString(R.string.insert_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}