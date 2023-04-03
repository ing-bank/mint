package org.mint.android.rule.input.datetime

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Random

@SuppressLint("SimpleDateFormat")
object DateInputSupplier {
    private fun pastDateInput(random: Random): Calendar {
        val now = Calendar.getInstance()
        // only change the year since it's more complex to subtract day or months with Calendar
        now.add(Calendar.YEAR, -random.nextInt(20))
        return now
    }
    private fun futureDateInput(random: Random): Calendar {
        val now = Calendar.getInstance()
        // only change the year since it's more complex to subtract day or months with Calendar
        now.add(Calendar.YEAR, random.nextInt(20))
        return now
    }
    private fun leapYearDateInput(random: Random): GregorianCalendar {
        val leapDate1 = GregorianCalendar(2016, 1, 29)
        val leapDate2 = GregorianCalendar(2020, 1, 29)
        val leapDate3 = GregorianCalendar(2024, 1, 29)
        val leapDate4 = GregorianCalendar(2028, 1, 29)

        val dates = arrayOf(leapDate1, leapDate2, leapDate3, leapDate4)

        return dates[random.nextInt(dates.size)]
    }

    private fun suppliers(random: Random) = listOf(
        pastDateInput(random),
        futureDateInput(random),
        leapYearDateInput(random)
    )

    private val dateFormatter: (Locale) -> DateFormat = {
            l ->
        DateFormat.getDateInstance(DateFormat.SHORT, l)
    }

    val defaultDateFormatter = SimpleDateFormat("yyyy-MM-dd")

    fun get(random: Random): String {
        val sups = suppliers(random)
        return defaultDateFormatter.format(
            sups[random.nextInt(sups.size)]
                .time
        )
    }

    fun get(random: Random, l: Locale): String {
        val sups = suppliers(random)
        return dateFormatter.invoke(l).format(
            sups[random.nextInt(sups.size)]
                .time
        )
    }
}
