package com.ing.mint.android.rule.input.datetime

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

@SuppressLint("SimpleDateFormat")
object TimeInputSupplier {
    private fun timeInput(random: Random): Calendar {
        val time = Calendar.getInstance()
        time.set(Calendar.HOUR, random.nextInt(24))
        time.set(Calendar.MINUTE, random.nextInt(60))
        return time
    }

    val defaultTimeFormatter = SimpleDateFormat("HH:mm")

    private val timeFormatter: (Locale) -> DateFormat = {
            l ->
        DateFormat.getTimeInstance(DateFormat.SHORT, l)
    }

    fun get(random: Random): String {
        return defaultTimeFormatter.format(timeInput(random).time.time)
    }

    fun get(random: Random, l: Locale): String {
        return timeFormatter.invoke(l).format(timeInput(random).time.time)
    }
}
