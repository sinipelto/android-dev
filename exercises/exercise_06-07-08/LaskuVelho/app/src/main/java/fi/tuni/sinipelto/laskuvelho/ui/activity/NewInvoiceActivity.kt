package fi.tuni.sinipelto.laskuvelho.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import fi.tuni.sinipelto.laskuvelho.R
import fi.tuni.sinipelto.laskuvelho.data.model.InvoiceType
import java.util.*

class NewInvoiceActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var payeeInput: EditText
    private lateinit var typeInput: Spinner
    private lateinit var amountInput: EditText
    private lateinit var dueInput: DatePicker

    private var selectedType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_invoice)

        // Configure drop-down menu for invoice types
        val typeAdapter: ArrayAdapter<InvoiceType> =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        typeAdapter.addAll(InvoiceType.getObjects().sortedBy { x -> x.toString() })

        typeInput = findViewById(R.id.invoiceTypeSpinner)
        typeInput.adapter = typeAdapter
        typeInput.onItemSelectedListener = this

        payeeInput = findViewById(R.id.payeeEditText)
        amountInput = findViewById(R.id.amountEditText)
        dueInput = findViewById(R.id.dueDatePicker)

        val button = findViewById<Button>(R.id.addBtn)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (validateInputs()) {
                val payee = payeeInput.text.toString()
                val amount = amountInput.text.toString().toDouble()
                val cal = Calendar.getInstance()
                cal.set(dueInput.year, dueInput.month, dueInput.dayOfMonth)

                replyIntent.putExtra(EXTRA_PAYEE, payee)
                replyIntent.putExtra(EXTRA_AMOUNT, amount)
                replyIntent.putExtra(EXTRA_TYPE, selectedType)
                replyIntent.putExtra(EXTRA_DUE, cal.time.time)

                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            } else {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
        }
    }

    // Check that all input fields are set and values within their validi limits
    private fun validateInputs(): Boolean {
        if (TextUtils.isEmpty(payeeInput.text)) {
            Toast.makeText(
                applicationContext,
                "${getString(R.string.empty_field)}: Saaja",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (TextUtils.isEmpty(amountInput.text)) {
            Toast.makeText(
                applicationContext,
                "${getString(R.string.empty_field)}: Määrä",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (selectedType == null) {
            if (TextUtils.isEmpty(amountInput.text)) {
                Toast.makeText(
                    applicationContext,
                    "${getString(R.string.empty_field)}: Tyyppi",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        return true
    }

    // This method is called when a dropdown menu item is selected by the user
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            val sel = it.getItemAtPosition(position) as InvoiceType
            selectedType = InvoiceType.getInvoiceTypeCodeById(sel.id)
        }
    }

    // Thsi is called if no item is selected from the dropdown menu
    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedType = null
    }

    fun backButtonHandle(@Suppress("UNUSED_PARAMETER") view: View) {
        // Set reply to canceled and go back to main view
        val replyIntent = Intent()
        setResult(Activity.RESULT_CANCELED, replyIntent)
        finish()
    }

    companion object {
        const val EXTRA_PAYEE = "fi.tuni.sinipelto.laskuvelho.EXTRA_PAYEE"
        const val EXTRA_TYPE = "fi.tuni.sinipelto.laskuvelho.EXTRA_TYPE"
        const val EXTRA_AMOUNT = "fi.tuni.sinipelto.laskuvelho.EXTRA_AMOUNT"
        const val EXTRA_DUE = "fi.tuni.sinipelto.laskuvelho.EXTRA_DUE"
    }
}