package ir.moha.persiantodo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.ui.theme.*

fun Priority.toColor(): Color = when (this) {
    Priority.URGENT -> PriorityUrgent
    Priority.HIGH   -> PriorityHigh
    Priority.NORMAL -> PriorityNormal
    Priority.LOW    -> PriorityLow
}

fun Priority.toLabel(): String = when (this) {
    Priority.URGENT -> "فوری"
    Priority.HIGH   -> "مهم"
    Priority.NORMAL -> "عادی"
    Priority.LOW    -> "کم‌اهمیت"
}

fun Priority.toEmoji(): String = when (this) {
    Priority.URGENT -> "🔴"
    Priority.HIGH   -> "🟠"
    Priority.NORMAL -> "🔵"
    Priority.LOW    -> "🟢"
}

@Composable
fun PriorityChip(
    priority: Priority,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = priority.toColor()
    if (onClick != null) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text("${priority.toEmoji()} ${priority.toLabel()}", style = MaterialTheme.typography.labelMedium) },
            modifier = modifier,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = color.copy(alpha = 0.2f),
                selectedLabelColor = color
            )
        )
    } else {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Text(
                text = "${priority.toEmoji()} ${priority.toLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
