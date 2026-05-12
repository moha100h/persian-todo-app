package ir.moha.persiantodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.ui.components.toColor
import ir.moha.persiantodo.ui.components.toLabel
import ir.moha.persiantodo.ui.viewmodel.TaskViewModel
import ir.moha.persiantodo.util.JalaliCalendar
import ir.moha.persiantodo.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val today = JalaliCalendar.now()

    // آمار ۷ روز اخیر
    val last7Days = remember {
        (6 downTo 0).map { today + (-it) }
    }

    val statsMap = remember(state.dateStats) {
        state.dateStats.associateBy { it.jalaliDate }
    }

    // آمار کلی
    val totalTasks    = state.dateStats.sumOf { it.total }
    val totalDone     = state.dateStats.sumOf { it.completed }
    val totalPending  = state.totalPending
    val completionRate = if (totalTasks > 0) (totalDone * 100 / totalTasks) else 0

    // آمار اولویت از وظایف امروز
    val priorityStats = remember(state.tasks) {
        Priority.entries.map { p ->
            p to state.tasks.count { it.priority == p && !it.isCompleted }
        }.filter { it.second > 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("آمار و گزارش") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // کارت‌های خلاصه
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "کل وظایف",
                        value = totalTasks.toString().toPersianDigits(),
                        icon = "📋",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "انجام‌شده",
                        value = totalDone.toString().toPersianDigits(),
                        icon = "✅",
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "باقی‌مانده",
                        value = totalPending.toString().toPersianDigits(),
                        icon = "⏳",
                        color = Color(0xFFFF9800).copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // نرخ تکمیل
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نرخ تکمیل کلی", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${completionRate.toString().toPersianDigits()}٪",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { completionRate / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // نمودار ۷ روز اخیر
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("۷ روز اخیر", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7Days.forEach { date ->
                                val stat = statsMap[date.toString()]
                                val total = stat?.total ?: 0
                                val done  = stat?.completed ?: 0
                                val isToday = date.toString() == today.toString()
                                val maxHeight = 80.dp
                                val barHeight = if (total > 0) maxHeight * (total.coerceAtMost(10) / 10f) else 4.dp

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.width(36.dp)
                                ) {
                                    if (total > 0) {
                                        Text(
                                            total.toString().toPersianDigits(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(barHeight.coerceAtLeast(4.dp))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (isToday) MaterialTheme.colorScheme.primary
                                                else if (total > 0 && done == total) Color(0xFF4CAF50)
                                                else MaterialTheme.colorScheme.primaryContainer
                                            )
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        date.day.toString().toPersianDigits(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // آمار اولویت امروز
            if (priorityStats.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("وظایف باقی‌مانده امروز بر اساس اولویت",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            priorityStats.forEach { (priority, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${priority.toLabel()} ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = priority.toColor()
                                    )
                                    Text(
                                        count.toString().toPersianDigits(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = priority.toColor()
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / (priorityStats.maxOf { it.second }) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = priority.toColor(),
                                    trackColor = priority.toColor().copy(alpha = 0.1f)
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
