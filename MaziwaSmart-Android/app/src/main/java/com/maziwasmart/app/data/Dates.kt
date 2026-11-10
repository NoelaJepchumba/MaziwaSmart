package com.maziwasmart.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Small helpers for dates. Dates are stored as text "yyyy-MM-dd" so they are easy to compare. */
object Dates {
    private val shortFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val longFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)

    fun today(): String = LocalDate.now().toString()

    fun daysAgo(days: Long): String = LocalDate.now().minusDays(days).toString()

    /** e.g. the last 7 days, oldest first */
    fun lastNDays(n: Int): List<String> = (n - 1 downTo 0).map { daysAgo(it.toLong()) }

    fun pretty(date: String): String =
        runCatching { LocalDate.parse(date).format(shortFormat) }.getOrDefault(date)

    fun todayLong(): String = LocalDate.now().format(longFormat)

    fun dayName(date: String): String =
        runCatching { LocalDate.parse(date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }
            .getOrDefault("")

    fun isThisMonth(date: String): Boolean = date.startsWith(today().substring(0, 7))

    fun of(year: Int, month: Int, day: Int): String = LocalDate.of(year, month, day).toString()
}

/** Number formatting helpers */
fun Double.oneDecimal(): String = String.format(Locale.US, "%.1f", this)

/** 8.0 -> "8", 8.5 -> "8.5" */
fun Double.clean(): String = if (this % 1.0 == 0.0) this.toLong().toString() else oneDecimal()

fun Double.ksh(): String = "KSh " + String.format(Locale.US, "%,.0f", this)
