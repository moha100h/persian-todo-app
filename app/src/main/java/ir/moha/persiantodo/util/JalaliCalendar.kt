package ir.moha.persiantodo.util

import java.util.Calendar

/**
 * تبدیل تاریخ میلادی به شمسی و برعکس — الگوریتم Borkowski
 */
object JalaliCalendar {

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {

        override fun toString(): String = "%04d-%02d-%02d".format(year, month, day)

        fun toPersianDisplay(): String {
            val months = listOf(
                "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
                "مهر","آبان","آذر","دی","بهمن","اسفند"
            )
            return "${day.toString().toPersianDigits()} ${months[month - 1]} ${year.toString().toPersianDigits()}"
        }

        fun dayOfWeekName(): String {
            val g = toGregorian()
            val cal = Calendar.getInstance().apply { set(g.first, g.second - 1, g.third) }
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

        fun daysInMonth(): Int = when {
            month <= 6  -> 31
            month <= 11 -> 30
            isLeapYear(year) -> 30
            else -> 29
        }

        /** روز اول ماه چندمین روز هفته است (0=شنبه … 6=جمعه) */
        fun firstDayOfMonthWeekday(): Int {
            val g = JalaliDate(year, month, 1).toGregorian()
            val cal = Calendar.getInstance().apply { set(g.first, g.second - 1, g.third) }
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY  -> 0
                Calendar.SUNDAY    -> 1
                Calendar.MONDAY    -> 2
                Calendar.TUESDAY   -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY  -> 5
                Calendar.FRIDAY    -> 6
                else -> 0
            }
        }

        operator fun plus(days: Int): JalaliDate {
            val g = toGregorian()
            val cal = Calendar.getInstance().apply {
                set(g.first, g.second - 1, g.third)
                add(Calendar.DAY_OF_YEAR, days)
            }
            return gregorianToJalali(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        operator fun compareTo(other: JalaliDate): Int = toString().compareTo(other.toString())
    }

    fun now(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun parse(s: String): JalaliDate? = runCatching {
        val parts = s.split("-")
        JalaliDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }.getOrNull()

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val g = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val jm: Int
        val jd: Int
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
                ((gy2 + 399) / 400) + gd + g[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1
        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4
        for (i in 0 until jm2) jDayNo += if (i < 6) 31 else 30
        jDayNo += jd2
        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097
        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++ else leap = false
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }
        val gMonthDays = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 12 && gDayNo >= gMonthDays[gm]) {
            gDayNo -= gMonthDays[gm]
            gm++
        }
        return Triple(gy, gm + 1, gDayNo + 1)
    }

    fun isLeapYear(jy: Int): Boolean {
        val rem = jy % 33
        return rem in listOf(1, 5, 9, 13, 17, 22, 26, 30)
    }
}

// ── Extensions ─────────────────────────────────────────────
fun Int.toPersianDigits(): String {
    val persian = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return toString().map { if (it.isDigit()) persian[it - '0'] else it }.joinToString("")
}

fun String.toPersianDigits(): String {
    val persian = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return map { if (it.isDigit()) persian[it - '0'] else it }.joinToString("")
}
