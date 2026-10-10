package com.example.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val localeId = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        return try {
            val nf = NumberFormat.getNumberInstance(localeId)
            "Rp " + nf.format(amount)
        } catch (e: Exception) {
            "Rp $amount"
        }
    }

    fun formatSignedRupiah(amount: Long, isExpense: Boolean): String {
        val formatted = formatRupiah(amount)
        return if (isExpense) "-$formatted" else "+$formatted"
    }

    fun formatDate(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("d MMMM yyyy", localeId)
            sdf.format(Date(millis))
        } catch (e: Exception) {
            "Hari ini"
        }
    }

    fun formatShortDate(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("d MMM yyyy", localeId)
            sdf.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatRelativeTime(millis: Long): String {
        return try {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = millis }

            val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

            if (isSameDay) {
                val timeFormat = SimpleDateFormat("HH:mm", localeId)
                return "Hari ini, " + timeFormat.format(Date(millis))
            }

            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

            if (isYesterday) {
                val timeFormat = SimpleDateFormat("HH:mm", localeId)
                return "Kemarin, " + timeFormat.format(Date(millis))
            }

            formatShortDate(millis)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDetailedDateTime(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("EEEE, d MMM yyyy • HH:mm", localeId)
            sdf.format(Date(millis))
        } catch (e: Exception) {
            formatDate(millis)
        }
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        return try {
            val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }
}
