package ir.moha.persiantodo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.moha.persiantodo.data.local.dao.DateStat
import ir.moha.persiantodo.data.local.entity.CategoryEntity
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.data.local.entity.RepeatType
import ir.moha.persiantodo.data.local.entity.TaskEntity
import ir.moha.persiantodo.domain.repository.TaskRepository
import ir.moha.persiantodo.notification.ReminderScheduler
import ir.moha.persiantodo.util.JalaliCalendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Helper: Quad برای combine چهار Flow ──────────────────
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = d

// ── UI State ──────────────────────────────────────────────
data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedDate: JalaliCalendar.JalaliDate = JalaliCalendar.now(),
    val selectedCategoryId: Long? = null,
    val showCompleted: Boolean = false,
    val searchQuery: String = "",
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val dateStats: List<DateStat> = emptyList(),
    val totalPending: Int = 0,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _selectedDate       = MutableStateFlow(JalaliCalendar.now())
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _showCompleted      = MutableStateFlow(false)
    private val _searchQuery        = MutableStateFlow("")

    init {
        observeTasks()
        observeCategories()
        observeStats()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        combine(_selectedDate, _selectedCategoryId, _showCompleted, _searchQuery) {
            date, catId, showDone, query -> Quad(date, catId, showDone, query)
        }.flatMapLatest { (date, catId, showDone, query) ->
            repository.getTasks(
                date          = date.toString(),
                categoryId    = catId,
                showCompleted = showDone,
                query         = query
            )
        }.onEach { tasks ->
            _uiState.update { it.copy(tasks = tasks) }
        }.launchIn(viewModelScope)

        _selectedDate.flatMapLatest { date ->
            combine(
                repository.getPendingCount(date.toString()),
                repository.getCompletedCount(date.toString())
            ) { p, c -> Pair(p, c) }
        }.onEach { (p, c) ->
            _uiState.update { it.copy(pendingCount = p, completedCount = c) }
        }.launchIn(viewModelScope)
    }

    private fun observeCategories() {
        repository.getAllCategories()
            .onEach { cats -> _uiState.update { it.copy(categories = cats) } }
            .launchIn(viewModelScope)
    }

    private fun observeStats() {
        repository.getDateStats()
            .onEach { stats -> _uiState.update { it.copy(dateStats = stats) } }
            .launchIn(viewModelScope)

        repository.getTotalPendingCount()
            .onEach { count -> _uiState.update { it.copy(totalPending = count) } }
            .launchIn(viewModelScope)
    }

    // ── Actions ───────────────────────────────────────────
    fun selectDate(date: JalaliCalendar.JalaliDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun selectCategory(id: Long?) {
        _selectedCategoryId.value = id
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun toggleShowCompleted() {
        val new = !_showCompleted.value
        _showCompleted.value = new
        _uiState.update { it.copy(showCompleted = new) }
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun addTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.NORMAL,
        date: JalaliCalendar.JalaliDate? = null,
        reminderTime: String? = null,
        repeatType: RepeatType = RepeatType.NONE,
        categoryId: Long? = null,
        tags: String = ""
    ) = viewModelScope.launch {
        if (title.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "عنوان وظیفه نمی‌تواند خالی باشد") }
            return@launch
        }
        val task = TaskEntity(
            title       = title.trim(),
            description = description.trim(),
            priority    = priority,
            jalaliDate  = date?.toString(),
            reminderTime = reminderTime,
            repeatType  = repeatType,
            categoryId  = categoryId,
            tags        = tags
        )
        val id = repository.insertTask(task)
        if (reminderTime != null && date != null) {
            ReminderScheduler.schedule(context, task.copy(id = id))
        }
        _uiState.update { it.copy(snackbarMessage = "وظیفه اضافه شد ✓") }
    }

    fun updateTask(task: TaskEntity) = viewModelScope.launch {
        repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        ReminderScheduler.cancel(context, task.id)
        if (task.reminderTime != null && task.jalaliDate != null) {
            ReminderScheduler.schedule(context, task)
        }
        _uiState.update { it.copy(snackbarMessage = "وظیفه ویرایش شد ✓") }
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        ReminderScheduler.cancel(context, task.id)
        repository.deleteTask(task)
        _uiState.update { it.copy(snackbarMessage = "وظیفه حذف شد") }
    }

    fun toggleComplete(task: TaskEntity) = viewModelScope.launch {
        val done = !task.isCompleted
        repository.setCompleted(task.id, done)
        if (done) ReminderScheduler.cancel(context, task.id)
    }

    fun togglePin(task: TaskEntity) = viewModelScope.launch {
        repository.setPinned(task.id, !task.isPinned)
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun addCategory(name: String, colorHex: String, icon: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.insertCategory(CategoryEntity(name = name, colorHex = colorHex, icon = icon))
    }

    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    suspend fun getTaskById(id: Long): TaskEntity? = repository.getTaskById(id)
}
