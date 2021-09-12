package fi.tuni.sinipelto.currencyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import fi.tuni.sinipelto.currencyapp.models.CurrencyResponse
import fi.tuni.sinipelto.currencyapp.services.CurrencyService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private val TAG = "CURRENCYAPP.MAINACTIVITY.LOGGER"

    // Date formatter to parse dates into local human readable format
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("fi", "FI"))

    // Timer object for scheduling udpates to currency rates
    private val updateTimer: Timer = Timer("UpdateScheduler")

    private lateinit var retrofit: Retrofit

    private lateinit var currencyService: CurrencyService
    private var currencyRates: CurrencyResponse? = null
    private lateinit var currencyMapping: Map<String, TextView>

    private lateinit var input: EditText
    private lateinit var errorText: TextView
    private lateinit var lastUpdatedText: TextView

    // Somehow gives an error when mapping views directly
    // However seems not to be an error, thus suppressed
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.valueInput)
        errorText = findViewById(R.id.errorText)
        lastUpdatedText = findViewById(R.id.updatedValue)

        // Set timezone Finland (should be detected from the user current locale etc.)
        dateFormatter.timeZone = TimeZone.getTimeZone("Europe/Helsinki")

        // Map currencies with textviews
        currencyMapping = mapOf(
            "USD" to findViewById(R.id.valueUsd),
            "GBP" to findViewById(R.id.valueGbp),
            "SEK" to findViewById(R.id.valueSek),
            "RUB" to findViewById(R.id.valueRub),
            "CHF" to findViewById(R.id.valueChf),
            "CNY" to findViewById(R.id.valueCny),
        )

        // Init retrofit with fixer.io API endpoint
        retrofit = Retrofit.Builder()
            .baseUrl("http://data.fixer.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Init required services
        currencyService = retrofit.create(CurrencyService::class.java)

        // Initial request for currency rates on create
        requestRates()

        findViewById<Button>(R.id.calcButton).setOnClickListener {
            calculate()
        }
    }

    // Member function for Double to round the ending decimals to a specific count
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this@round * multiplier) / multiplier
    }

    private fun calculate() {
        if (currencyRates == null) {
            Log.e(TAG, "ERROR: currencyRates was null, cannot convert currencies.")
            return
        }

        val rates = currencyRates!!

        // Fetch user input as double
        val value = input.text.toString().toDouble()

        // Update currency rate displays
        for (currency in currencyMapping) {
            currency.value.text =
                (value * rates.rates[currency.key]!!).round(6).toString()
        }
    }

    private fun createUpdateTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                requestRates()
            }
        }
    }

    private fun updateSuccess() {
        errorText.isInvisible = true

        // Schedule the next rate update (after 5 mins)
        updateTimer.schedule(createUpdateTask(), TimeUnit.SECONDS.toMillis(5))
    }

    private fun updateFail() {
        errorText.isInvisible = false

        // If update failed, schedule new update after a short grace period
        updateTimer.schedule(createUpdateTask(), 5000L)
    }

    private fun requestRates() {
        // TODO if network or connectivity error -> longer timeout!!!!
        try {
            // Try fetching currencies from the API
            currencyService.latestRates(
                BuildConfig.FIXER_APIKEY,
                "EUR",
                currencyMapping.keys.joinToString(",")
            ).enqueue(object : Callback<CurrencyResponse> {
                override fun onResponse(
                    call: Call<CurrencyResponse>,
                    response: Response<CurrencyResponse>
                ) {
                    // Response received
                    if (call.isExecuted && !call.isCanceled) {
                        updateRates(response)
                    } else {
                        Log.e(TAG, "Rates update request was canceled!")
                        updateFail()
                    }
                }

                override fun onFailure(call: Call<CurrencyResponse>, t: Throwable) {
                    // Failed to get response for request
                    Log.e(TAG, "Failed to request currencies: ${t.message} => $t")
                    updateFail()
                }
            })
        } catch (ex: Exception) {
            // If request fails, log the incident
            Log.e(TAG, "ERROR: Failed to retrieve currency rates::: ${ex.message} => $ex")
            updateFail()
        }
    }

    private fun updateRates(resp: Response<CurrencyResponse>) {
        // Check for any errors and return
        if (!resp.isSuccessful || resp.code() != 200) {
            Log.e(
                TAG,
                "Resp was unsuccesful. CODE: ${resp.code()} ERROR_BODY: ${resp.errorBody()}"
            )
            updateFail()
            return
        }

        // Transform response into object form and store into memory
        currencyRates = resp.body()
        val rates = currencyRates!!

        // Check for any errors in the successful response for precaution
        if (!rates.success.toBoolean()) {
            Log.e(TAG, "Response success value was NOT true. Cannot continue")
            updateFail()
            return
        }

        // Display update time to user
        // API response timestamp epoch is in SECONDS -> convert to millis
        lastUpdatedText.text =
            dateFormatter.format(Date(TimeUnit.SECONDS.toMillis(rates.timestamp)))

        // Signal a successful update
        updateSuccess()
    }
}