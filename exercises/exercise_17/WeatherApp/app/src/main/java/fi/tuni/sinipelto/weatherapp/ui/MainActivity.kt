package fi.tuni.sinipelto.weatherapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import fi.tuni.sinipelto.weatherapp.R
import fi.tuni.sinipelto.weatherapp.WeatherDataXmlParser
import fi.tuni.sinipelto.weatherapp.services.WeatherService
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParserException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val TAG = "fi.tuni.sinipelto.weatherapp.MAIN.LOGGER"

    private val apiUrl = HttpUrl.Builder()
        .scheme("https")
        .host("opendata.fmi.fi")
        .port(443)
        .build()

    private lateinit var retrofit: Retrofit
    private lateinit var weatherService: WeatherService

    private lateinit var errorTextView: TextView
    private lateinit var stationNameView: TextView
    private lateinit var regionView: TextView
    private lateinit var temperatureListingView: TextView

    private lateinit var locationInputEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        errorTextView = findViewById(R.id.errorText)
        stationNameView = findViewById(R.id.stationNameValue)
        regionView = findViewById(R.id.regionValue)
        temperatureListingView = findViewById(R.id.temperatureListing)
        locationInputEdit = findViewById(R.id.placeInput)

        findViewById<Button>(R.id.fetchButton).setOnClickListener {
            clearEntries()
            if (!locationInputEdit.text.isNullOrEmpty()) {
                requestWeather(locationInputEdit.text.toString())
            }
        }
    }

    private fun clearEntries() {
        errorTextView.isVisible = false
        stationNameView.text = ""
        regionView.text = ""
        temperatureListingView.text = ""
    }

    private fun requestWeather(place: String) {
        weatherService.getWeatherObservationForPlace(place)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (!response.isSuccessful) {
                        processException(response)
                    } else {
                        processWeatherData(response)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "ONFAILURE: ${call.isCanceled} ${call.isExecuted}")
                    Log.e(TAG, "Ex: ${t.message} ${t.printStackTrace()}")
                    processFailure()
                }
            })
    }

    private fun processFailure() {
        errorTextView.text = getString(R.string.error_failure)
        errorTextView.isVisible = true
    }

    private fun processNotFound() {

        errorTextView.text = getString(R.string.error_notfound)
        errorTextView.isVisible = true
    }

    private fun processException(response: Response<ResponseBody>) {
        Log.e(
            TAG,
            "PROCESS EXCEPTION: CODE: ${response.code()} MSG: ${response.message()} ERRBODY: ${
                response.errorBody()?.charStream()?.readText()
            }"
        )
        if (response.code() == 400) {
            processNotFound()
        } else {
            processFailure()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun processWeatherData(response: Response<ResponseBody>) {
        Log.d(
            TAG,
            "PROCESS REPONSE: CODE: ${response.code()} MSG: ${response.message()} ERRBODY: ${
                response.errorBody()?.charStream()?.readText()
            }"
        )

        val data = response.body() ?: run {
            Log.e(TAG, "Data object was null. Cannot proceed.")
            return
        }

        Log.d(
            TAG,
            "DATA: $data"
        )

        val parser = WeatherDataXmlParser()
        try {
            val weatherData = parser.parse(data.byteStream())
            Log.d(TAG,
                "WEATHER DATA EXTRACT: ${weatherData.region} ${weatherData.station} ${weatherData.measurements.first().rawTime} ${weatherData.measurements.first().value}")
            regionView.text = weatherData.region
            stationNameView.text = weatherData.station
            temperatureListingView.text =
                getString(R.string.temp_header) + weatherData.measurements.sortedByDescending { it.localTime() }
                    .take(5).joinToString("\n\n") {
                    "${
                        it.localTime().format(
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    }:  ${it.value}"
                }
        } catch (ex: XmlPullParserException) {
            Log.e(TAG, "Failed to parse result: ${ex.message} ${ex.printStackTrace()}")
            processFailure()
        } catch (ex: NullPointerException) {
            Log.e(TAG, "NULLPTR EXCEPTION: ${ex.message} ${ex.printStackTrace()}")
            processNotFound()
        } catch (ex: Exception) {
            Log.e(TAG, "Unexpected exception occurred: ${ex.message} ${ex.printStackTrace()}")
            processFailure()
        }
    }
}