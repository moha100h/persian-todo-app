package ir.moha.persiantodo.util

import java.util.Calendar

/**
 * تبدیل تاریخ میلادی به شمسی و برعکس
 * الگوریتم: Borkowski
 */
object JalaliCalendar {

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        override fun toString(): String =
            "%04d-%02d-%02d".format(year, month, day)

        fun toDisplayString(): String =
            "%04d/%02d/%02d".format(year, month, day)

        fun toPersianDisplay(): String {
            val months = listOf(
                "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
                "مهر","آبان","آذر","دی","بهمن","اسفند"
            )
            return "${day.toPersianDigits()} ${months[month - 1]} ${year.toString().toPersianDigits()}"
        }

        fun dayOfWeekName(): String {
            val g = toGregorian()
            val cal = Calendar.getInstance().apply {
                set(g.first, g.second - 1, g.third)
            }
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY  -> "شنبه"
                Calendar.SUNDAY    -> "یک‌شنبه"
                Calendar.MONDAY    -> "دوشنبه"
                Calendar.TUESDAY   -> "سه‌شنبه"
                Calendar.WEDNESDAY -> "چهارشنبه"
                Calendar.THURSDAY  -> "پنج‌شنبه"
                Calendar.FRIDAY    -> "جمعه"
                else -> ""
            }
        }

        fun toGregorian(): Triple<Int, Int, Int> = jalaliToGregorian(year, month, day)

        fun isToday(): Boolean = toString() == now().toString()

        fun daysInMonth(): Int {
            return if (month <= 6) 31
            else if (month <= 11) 30
            else if (isLeapYear(year)) 30
            else 29
        }

        fun firstDayOfMonthWeekday(): Int {
            val first = JalaliDate(year, month, 1)
            val g = first.toGregorian()
            val cal = Calendar.getInstance().apply {
                set(g.first, g.second - 1, g.third)
            }
            // شنبه = 0
            return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        }

        operator fun plus(days: Int): JalaliDate {
            val g = toGregorian()
            val cal = Calendar.getInstance().apply {
                set(g.first, g.second - 1, g.third)
                add(Calendar.DAY_OF_MONTH, days)
            }
            return gregorianToJalali(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        operator fun minus(other: JalaliDate): Int {
            val g1 = toGregorian()
            val g2 = other.toGregorian()
            val c1 = Calendar.getInstance().apply { set(g1.first, g1.second - 1, g1.third) }
            val c2 = Calendar.getInstance().apply { set(g2.first, g2.second - 1, g2.third) }
            return ((c1.timeInMillis - c2.timeInMillis) / 86400000).toInt()
        }
    }

    fun now(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun parse(s: String): JalaliDate? {
        val parts = s.split("-")
        if (parts.size != 3) return null
        return try {
            JalaliDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (e: Exception) { null }
    }

    fun isLeapYear(year: Int): Boolean {
        val leapYears = listOf(1, 5, 9, 13, 17, 22, 26, 30)
        return leapYears.contains(year % 33)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val g_d_no = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        var gm2 = gm
        val gDays = intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334)
        val gd_no = g_d_no + gDays[gm2 - 1] + gd - 1
        if (gm2 > 2 && (gy % 4 == 0 && (gy % 100 != 0 || gy % 400 == 0))) {
            // leap
        }
        val j_d_no = gd_no - 79

        val j_np = j_d_no / 12053
        var jd = j_d_no % 12053
        var jy = 979 + 33 * j_np + 4 * (jd / 1461)
        jd %= 1461
        if (jd >= 366) {
            jy += (jd - 1) / 365
            jd = (jd - 1) % 365
        }
        val jDays = intArrayOf(0,31,62,93,124,155,186,216,246,276,306,336)
        var jm = 0
        for (i in 11 downTo 0) {
            if (jd >= jDays[i]) { jm = i + 1; jd -= jDays[i]; break }
        }
        return JalaliDate(jy, jm, jd + 1)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm - 1
        val jDays = intArrayOf(0,31,62,93,124,155,186,216,246,276,306,336)
        var j_day_no = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4
        j_day_no += jDays[jm2] + jd - 1
        var g_day_no = j_day_no + 79
        var gy = 1600 + 400 * (g_day_no / 146097)
        g_day_no %= 146097
        var leap = true
        if (g_day_no >= 36525) {
            g_day_no--
            gy += 100 * (g_day_no / 36524)
            g_day_no %= 36524
            if (g_day_no >= 365) g_day_no++ else leap = false
        }
        gy += 4 * (g_day_no / 1461)
        g_day_no %= 1461
        if (g_day_no >= 366) { leap = false; g_day_no--; gy += g_day_no / 365; g_day_no %= 365 }
        val gDays = intArrayOf(31, if (leap) 29 else 28, 31,30,31,30,31,31,30,31,30,31)
        var gm = 0
        for (i in gDays.indices) { if (g_day_no < gDays[i]) { gm = i + 1; break }; g_day_no -= gDays[i] }
        return Triple(gy, gm, g_day_no + 1)
    }
}

fun Int.toPersianDigits(): String {
    val persian = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return toString().map { if (it.isDigit()) persian[it - '0'] else it }.joinToString("")
}

fun String.toPersianDigits(): String {
    val persian = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return map { if (it.isDigit()) persian[it - '0'] else it }.joinToString("")
}
