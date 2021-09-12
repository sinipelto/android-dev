package fi.tuni.sinipelto.laskuvelho.data.model

import java.text.SimpleDateFormat
import java.util.*

// Class for handling dates in required level and format
// Currently: Resolution: dd (00:00:00.00) Format: dd.MM.yyyy
class DateFormatter {

    companion object {

        private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale("fi", "FI"))

        fun getTodayMidnight() = getLocalDateMidnight(Date()) // Today 00:00.0000

        fun getLocalDateMidnight(date: Date): Date {
            return dateFormatter.parse(getLocalDateString(date))!!
        }

        fun getLocalDateString(date: Date): String = dateFormatter.format(date)

        fun getLocalDateString(date: String): String = dateFormatter.format(date)
    }
}