package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BengaliDateUtils {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: Any?): String {
        if (input == null) return ""
        val str = input.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatCurrency(amount: Double, isBengali: Boolean = true): String {
        val rounded = if (amount % 1.0 == 0.0) amount.toLong() else amount
        val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(rounded)
        return if (isBengali) {
            "৳ ${toBengaliDigits(formattedNumber)}"
        } else {
            "৳ $formattedNumber"
        }
    }

    val bengaliDaysFull = mapOf(
        Calendar.SATURDAY to "শনিবার",
        Calendar.SUNDAY to "রবিবার",
        Calendar.MONDAY to "সোমবার",
        Calendar.TUESDAY to "মঙ্গলবার",
        Calendar.WEDNESDAY to "বুধবার",
        Calendar.THURSDAY to "বৃহস্পতিবার",
        Calendar.FRIDAY to "শুক্রবার"
    )

    val englishDaysFull = mapOf(
        Calendar.SATURDAY to "Saturday",
        Calendar.SUNDAY to "Sunday",
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday"
    )

    val dayCodes = listOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")

    val dayCodeToBengaliShort = mapOf(
        "SAT" to "শনি",
        "SUN" to "রবি",
        "MON" to "সোম",
        "TUE" to "মঙ্গল",
        "WED" to "বুধ",
        "THU" to "বৃহঃ",
        "FRI" to "শুক্র"
    )

    val dayCodeToEnglishShort = mapOf(
        "SAT" to "Sat",
        "SUN" to "Sun",
        "MON" to "Mon",
        "TUE" to "Tue",
        "WED" to "Wed",
        "THU" to "Thu",
        "FRI" to "Fri"
    )

    val calendarDayToCode = mapOf(
        Calendar.SATURDAY to "SAT",
        Calendar.SUNDAY to "SUN",
        Calendar.MONDAY to "MON",
        Calendar.TUESDAY to "TUE",
        Calendar.WEDNESDAY to "WED",
        Calendar.THURSDAY to "THU",
        Calendar.FRIDAY to "FRI"
    )

    private val bengaliMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val englishMonths = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getTodayDayCode(): String {
        val cal = Calendar.getInstance()
        return calendarDayToCode[cal.get(Calendar.DAY_OF_WEEK)] ?: "SAT"
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun formatFullDate(dateString: String, isBengali: Boolean = true): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateString) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val monthIdx = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

            if (isBengali) {
                val dayStr = toBengaliDigits(dayOfMonth)
                val monthStr = bengaliMonths.getOrElse(monthIdx) { "" }
                val yearStr = toBengaliDigits(year)
                val dayOfWeekStr = bengaliDaysFull[dayOfWeek] ?: ""
                "$dayStr $monthStr $yearStr, $dayOfWeekStr"
            } else {
                val monthStr = englishMonths.getOrElse(monthIdx) { "" }
                val dayOfWeekStr = englishDaysFull[dayOfWeek] ?: ""
                "$dayOfMonth $monthStr $year, $dayOfWeekStr"
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatMonthYearDisplay(yearMonth: String, isBengali: Boolean = true): String {
        return try {
            val parts = yearMonth.split("-")
            if (parts.size == 2) {
                val year = parts[0].toInt()
                val monthIdx = parts[1].toInt() - 1
                if (isBengali) {
                    val m = bengaliMonths.getOrElse(monthIdx) { "" }
                    "$m ${toBengaliDigits(year)}"
                } else {
                    val m = englishMonths.getOrElse(monthIdx) { "" }
                    "$m $year"
                }
            } else {
                yearMonth
            }
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun getGreeting(isBengali: Boolean = true): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (isBengali) {
            when (hour) {
                in 5..11 -> "শুভ সকাল"      // 5:00 AM – 11:59 AM
                in 12..15 -> "শুভ দুপুর"     // 12:00 PM – 3:59 PM
                in 16..19 -> "শুভ সন্ধ্যা"    // 4:00 PM – 7:59 PM
                else -> "শুভ রাত্রি"         // 8:00 PM – 4:59 AM
            }
        } else {
            when (hour) {
                in 5..11 -> "Good Morning"    // 5:00 AM – 11:59 AM
                in 12..15 -> "Good Afternoon"  // 12:00 PM – 3:59 PM
                in 16..19 -> "Good Evening"    // 4:00 PM – 7:59 PM
                else -> "Good Night"          // 8:00 PM – 4:59 AM
            }
        }
    }

    fun formatTimeSlot(startTime: String, endTime: String, isBengali: Boolean = true): String {
        fun formatSingle(time: String): String {
            val parts = time.split(":")
            if (parts.size < 2) return time
            val h = parts[0].toIntOrNull() ?: return time
            val m = parts[1].toIntOrNull() ?: 0
            val isPm = h >= 12
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            val formatted = String.format(Locale.US, "%02d:%02d", h12, m)
            return if (isBengali) {
                val bengaliNum = toBengaliDigits(formatted)
                val period = when {
                    h in 5..11 -> "সকাল"
                    h in 12..14 -> "দুপুর"
                    h in 15..17 -> "বিকাল"
                    h in 18..21 -> "সন্ধ্যা"
                    else -> "রাত"
                }
                "$period $bengaliNum"
            } else {
                val period = if (isPm) "PM" else "AM"
                "$formatted $period"
            }
        }
        return "${formatSingle(startTime)} - ${formatSingle(endTime)}"
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size >= 2) {
            val h = parts[0].trim().toIntOrNull() ?: 0
            val m = parts[1].trim().toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 0
    }

    fun isTimesOverlapping(startA: String, endA: String, startB: String, endB: String): Boolean {
        val sA = parseTimeToMinutes(startA)
        val eA = parseTimeToMinutes(endA)
        val sB = parseTimeToMinutes(startB)
        val eB = parseTimeToMinutes(endB)
        return sA < eB && sB < eA
    }
}
