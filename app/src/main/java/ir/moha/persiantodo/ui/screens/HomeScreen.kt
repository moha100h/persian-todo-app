package ir.moha.persiantodo.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.moha.persiantodo.ui.components.*
import ir.moha.persiantodo.ui.viewmodel.TaskViewModel
import ir.moha.persiantodo.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showCalendar by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.selectedDate.toPersianDisplay(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.selectedDate.dayOfWeekName() +
                                " · ${state.pendingCount.toString().toPersianDigits()} وظیفه باقی‌مانده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCalendar = !showCalendar }) {
                        Icon(
                            if (showCalendar) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "تقویم"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            if (state.showCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "نمایش انجام‌شده‌ها",
                            tint = if (state.showCompleted) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("وظیفه جدید") },
                expanded = true
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // تقویم
            item {
                AnimatedVisibility(
                    visible = showCalendar,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    JalaliCalendarView(
                        selectedDate = state.selectedDate,
                        onDateSelected = { viewModel.selectDate(it) },
                        dateStats = state.dateStats,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // آمار روز
            item {
                if (state.pendingCount > 0 || state.completedCount > 0) {
                    DayStatsBar(
                        pending = state.pendingCount,
                        completed = state.completedCount
                    )
                }
            }

            // لیست وظایف
            if (state.tasks.isEmpty()) {
                item {
                    EmptyState(
                        message = if (state.searchQuery.isNotBlank())
                            "نتیجه‌ای یافت نشد"
                        else
                            "وظیفه‌ای برای این روز ندارید
برای افزودن روی + بزنید"
                    )
                }
            } else {
                items(state.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleComplete(task) },
                        onTogglePin = { viewModel.togglePin(task) },
                        onEdit = { onEditTask(task.id) },
                        onDelete = { showDeleteDialog = task.id },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // دیالوگ حذف
    showDeleteDialog?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("حذف وظیفه") },
            text = { Text("آیا مطمئن هستید؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.tasks.find { it.id == taskId }?.let { viewModel.deleteTask(it) }
                        showDeleteDialog = null
                    }
                ) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun DayStatsBar(pending: Int, completed: Int, modifier: Modifier = Modifier) {
    val total = pending + completed
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "✅ ${completed.toString().toPersianDigits()} انجام‌شده",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "⏳ ${pending.toString().toPersianDigits()} باقی‌مانده",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
