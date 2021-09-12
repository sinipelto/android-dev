package fi.tuni.sinipelto.currencyapp.services

import fi.tuni.sinipelto.currencyapp.models.CurrencyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyService {

    @GET("latest")
    fun latestRates(
        @Query("access_key") accessKey: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): Call<CurrencyResponse>
}