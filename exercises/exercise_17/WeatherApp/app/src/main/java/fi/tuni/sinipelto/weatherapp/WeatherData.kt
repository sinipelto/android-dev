package fi.tuni.sinipelto.weatherapp

import fi.tuni.sinipelto.weatherapp.models.MeasurementTVP

data class WeatherData(

    val station: String,

    val region: String,

    val measurements: List<MeasurementTVP>,

    )

data class MutableWeatherData(

    var station: String?,

    var region: String?,

    var measurements: MutableList<MeasurementTVP>?,

    ) {
    fun validate(): Boolean =
        station != null && region != null && measurements != null && measurements?.count()!! >= 5
}