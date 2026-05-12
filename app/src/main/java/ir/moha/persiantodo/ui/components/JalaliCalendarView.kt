package ir.moha.persiantodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.moha.persiantodo.data.local.dao.DateStat
import ir.moha.persiantodo.util.JalaliCalendar
import ir.moha.persiantodo.util.toPersianDigits

@Composable
fun JalaliCalendarView(
    selectedDate: JalaliCalendar.JalaliDate,
    onDateSelected: (JalaliCalendar.JalaliDate) -> Unit,
    dateStats: List<DateStat> = emptyList(),
    modifier: Modifier = Modifier
) {
    val today = JalaliCalendar.now()
    var displayYear  by remember { mutableIntStateOf(selectedDate.year) }
    var displayMonth by remember { mutableIntStateOf(selectedDate.month) }

    val statsMap = remember(dateStats) { dateStats.associateBy { it.jalaliDate } }

    val monthNames = listOf(
        "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
        "مهر","آبان","آذر","دی","بهمن","اسفند"
    )
    val dayNames = listOf("ش","ی","د","س","چ","پ","ج")

    fun prevMonth() {
        if (displayMonth == 1) { displayMonth = 12; displayYear-- }
        else displayMonth--
    }
    fun nextMonth() {
        if (displayMonth == 12) { displayMonth = 1; displayYear++ }
        else displayMonth++
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // هدر ماه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = ::nextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "ماه بعد")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthNames[displayMonth - 1],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = displayYear.toString().toPersianDigits(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = ::prevMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "ماه قبل")
                }
            }

            Spacer(Modifier.height(8.dp))

            // نام روزهای هفته
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // روزهای ماه
            val firstDay = JalaliCalendar.JalaliDate(displayYear, displayMonth, 1)
            val daysInMonth = firstDay.daysInMonth()
            val startOffset = firstDay.firstDayOfMonthWeekday()

            val cells = startOffset + daysInMonth
            val rows = (cells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - startOffset + 1
                        val isValid = dayNum in 1..daysInMonth
                        val date = if (isValid) JalaliCalendar.JalaliDate(displayYear, displayMonth, dayNum) else null
                        val isSelected = date?.toString() == selectedDate.toString()
                        val isToday = date?.toString() == today.toString()
                        val stat = date?.let { statsMap[it.toString()] }
                        val hasTasks = (stat?.total ?: 0) > 0
                        val allDone = hasTasks && stat?.total == stat?.completed

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday    -> MaterialTheme.colorScheme.primaryContainer
                                        else       -> Color.Transparent
                                    }
                                )
                                .then(if (isValid) Modifier.clickable { onDateSelected(date!!) } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValid) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString().toPersianDigits(),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday    -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else       -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasTasks) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (allDone) Color(0xFF4CAF50)
                                                    else if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // دکمه برگشت به امروز
            if (displayYear != today.year || displayMonth != today.month) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { displayYear = today.year; displayMonth = today.month; onDateSelected(today) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("برگشت به امروز", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
