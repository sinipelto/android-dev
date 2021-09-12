package fi.tuni.sinipelto.weatherapp.models

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class MeasurementTVP(

    val rawTime: String,

    val value: String,

    ) {

    fun localTime(): ZonedDateTime {
        return ZonedDateTime.parse(rawTime, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
    }

}