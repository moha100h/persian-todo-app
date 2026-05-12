package ir.moha.persiantodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.data.local.entity.RepeatType
import ir.moha.persiantodo.data.local.entity.TaskEntity
import ir.moha.persiantodo.ui.components.JalaliCalendarView
import ir.moha.persiantodo.ui.components.PriorityChip
import ir.moha.persiantodo.ui.viewmodel.TaskViewModel
import ir.moha.persiantodo.util.JalaliCalendar
import ir.moha.persiantodo.util.toPersianDigits
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long? = null,
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var existingTask by remember { mutableStateOf<TaskEntity?>(null) }

    // بارگذاری وظیفه موجود
    LaunchedEffect(taskId) {
        if (taskId != null && taskId > 0) {
            existingTask = viewModel.getTaskById(taskId)
        }
    }

    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority    by remember { mutableStateOf(Priority.NORMAL) }
    var selectedDate by remember { mutableStateOf<JalaliCalendar.JalaliDate?>(JalaliCalendar.now()) }
    var reminderHour   by remember { mutableIntStateOf(9) }
    var reminderMinute by remember { mutableIntStateOf(0) }
    var hasReminder by remember { mutableStateOf(false) }
    var repeatType  by remember { mutableStateOf(RepeatType.NONE) }
    var showCalendar by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var titleError  by remember { mutableStateOf(false) }

    // پر کردن فیلدها از وظیفه موجود
    LaunchedEffect(existingTask) {
        existingTask?.let { t ->
            title = t.title
            description = t.description
            priority = t.priority
            selectedDate = t.jalaliDate?.let { JalaliCalendar.parse(it) }
            repeatType = t.repeatType
            t.reminderTime?.let { time ->
                hasReminder = true
                val parts = time.split(":")
                reminderHour = parts[0].toIntOrNull() ?: 9
                reminderMinute = parts[1].toIntOrNull() ?: 0
            }
        }
    }

    val isEdit = existingTask != null
    val timePickerState = rememberTimePickerState(initialHour = reminderHour, initialMinute = reminderMinute)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ویرایش وظیفه" else "وظیفه جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) { titleError = true; return@TextButton }
                            val reminderStr = if (hasReminder) "%02d:%02d".format(reminderHour, reminderMinute) else null
                            scope.launch {
                                if (isEdit && existingTask != null) {
                                    viewModel.updateTask(
                                        existingTask!!.copy(
                                            title = title.trim(),
                                            description = description.trim(),
                                            priority = priority,
                                            jalaliDate = selectedDate?.toString(),
                                            reminderTime = reminderStr,
                                            repeatType = repeatType,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                } else {
                                    viewModel.addTask(
                                        title = title,
                                        description = description,
                                        priority = priority,
                                        date = selectedDate,
                                        reminderTime = reminderStr,
                                        repeatType = repeatType
                                    )
                                }
                                onBack()
                            }
                        }
                    ) {
                        Text(if (isEdit) "ذخیره" else "افزودن",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // عنوان
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("عنوان وظیفه *") },
                isError = titleError,
                supportingText = if (titleError) {{ Text("عنوان الزامی است") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Title, null) }
            )

            // توضیحات
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                leadingIcon = { Icon(Icons.Filled.Notes, null) }
            )

            // اولویت
            Text("اولویت", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    PriorityChip(
                        priority = p,
                        selected = priority == p,
                        onClick = { priority = p }
                    )
                }
            }

            // تاریخ
            OutlinedCard(
                onClick = { showCalendar = !showCalendar },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("تاریخ", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            selectedDate?.toPersianDisplay() ?: "انتخاب نشده",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (showCalendar) {
                JalaliCalendarView(
                    selectedDate = selectedDate ?: JalaliCalendar.now(),
                    onDateSelected = { selectedDate = it; showCalendar = false }
                )
            }

            // یادآور
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Alarm, null, tint = MaterialTheme.colorScheme.primary)
                    Text("یادآور", style = MaterialTheme.typography.titleSmall)
                }
                Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
            }

            if (hasReminder) {
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "%02d:%02d".format(reminderHour, reminderMinute).toPersianDigits(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // تکرار
            Text("تکرار", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    RepeatType.NONE to "بدون تکرار",
                    RepeatType.DAILY to "روزانه",
                    RepeatType.WEEKLY to "هفتگی",
                    RepeatType.MONTHLY to "ماهانه"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = repeatType == type,
                        onClick = { repeatType = type },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("زمان یادآور") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timePickerState.hour
                    reminderMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("انصراف") }
            }
        )
    }
}
