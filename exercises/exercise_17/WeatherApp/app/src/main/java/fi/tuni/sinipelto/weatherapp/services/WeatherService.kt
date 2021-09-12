package fi.tuni.sinipelto.weatherapp.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("wfs")
    fun getWeatherObservationForPlace(
        @Query("place") place: String,
        @Query("service") service: String = "WFS",
        @Query("version") version: String = "2.0.0",
        @Query("request") request: String = "getFeature",
        @Query("storedquery_id") storedQuery: String = "fmi::observations::weather::timevaluepair",
        @Query("maxlocations") maxLocations: String = "1",
    ): Call<ResponseBody>
}