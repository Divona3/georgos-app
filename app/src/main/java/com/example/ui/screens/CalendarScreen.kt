package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.voice.ReminderNotifier
import com.example.data.local.FarmingReminderEntity
import com.example.ui.GrievousUiState
import com.example.ui.components.FarmerCalendarLogo
import com.example.ui.components.LeavesLogo
import com.example.ui.components.VoiceInputTextField
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.OffWhite

@Composable
fun CalendarScreen(
    state: GrievousUiState,
    onAddReminder: (String, String, String, String, Int) -> Unit,
    onCompleteReminder: (FarmingReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onSpeakText: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCadence by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedDay by remember { mutableIntStateOf(14) }
    var showAddDialog by remember { mutableStateOf(false) }

    var pendingNotificationReminder by remember { mutableStateOf<FarmingReminderEntity?>(null) }
    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingNotificationReminder?.let {
                ReminderNotifier.postReminderNotification(context, it.title, it.notes, it.id.toInt())
            }
        }
        pendingNotificationReminder = null
    }

    val filteredReminders = remember(state.reminders, selectedCadence, selectedCategory) {
        state.reminders.filter { item ->
            (selectedCadence == "ALL" || item.cadence == selectedCadence) &&
                (selectedCategory == "ALL" || item.category == selectedCategory)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FarmerCalendarLogo(size = 32.dp, primaryColor = ForestGreen, accentColor = EarthBrown)
                        Column {
                            Text(state.strings.calendarTitle, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Complete tasks properly to earn Leaves",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_reminder_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(state.strings.addReminder)
                    }
                }
            }

            // Monthly Interactive Day Strip
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "November Cultivation Window",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (12..28).forEach { day ->
                                val hasReminder = state.reminders.any { it.dayOfMonth == day && !it.isCompleted }
                                val isSelected = selectedDay == day
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) ForestGreen else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedDay = day }
                                        .minimumInteractiveComponentSize()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$day",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) OffWhite else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    hasReminder && isSelected -> AmberHarvest
                                                    hasReminder -> ForestGreen
                                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cadence Filters (All / Daily / Weekly / Monthly)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cadences = listOf(
                            "ALL" to state.strings.allLabel,
                            "DAILY" to state.strings.daily,
                            "WEEKLY" to state.strings.weekly,
                            "MONTHLY" to state.strings.monthly
                        )
                        cadences.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedCadence == key,
                                onClick = { selectedCadence = key },
                                label = { Text(label) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All Tasks",
                            "FERTILIZER" to "Fertilizer",
                            "TOOLING" to "Farming Tools",
                            "HARVESTING" to "Harvesting"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedCategory == key,
                                onClick = { selectedCategory = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            if (filteredReminders.isEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FarmerCalendarLogo(size = 44.dp, primaryColor = ForestGreen, accentColor = EarthBrown)
                            Text("No reminders match this filter", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Tap '${state.strings.addReminder}' to schedule fertilizer, tooling, or harvesting alerts.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(filteredReminders, key = { it.id }) { reminder ->
                    ReminderTaskCard(
                        reminder = reminder,
                        completeLabel = state.strings.completeForLeaves,
                        onComplete = { onCompleteReminder(reminder) },
                        onNotify = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                pendingNotificationReminder = reminder
                                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                ReminderNotifier.postReminderNotification(context, reminder.title, reminder.notes, reminder.id.toInt())
                            }
                            onSpeakText("${reminder.title}. ${reminder.notes}")
                        },
                        onDelete = { onDeleteReminder(reminder.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            languageCode = state.languageCode,
            defaultDay = selectedDay,
            onDismiss = { showAddDialog = false },
            onAdd = { title, notes, category, cadence, day ->
                onAddReminder(title, notes, category, cadence, day)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ReminderTaskCard(
    reminder: FarmingReminderEntity,
    completeLabel: String,
    onComplete: () -> Unit,
    onNotify: () -> Unit,
    onDelete: () -> Unit
) {
    val (catIcon, catTint) = when (reminder.category) {
        "FERTILIZER" -> Icons.Rounded.Science to ForestGreen
        "TOOLING" -> Icons.Rounded.Build to EarthBrown
        else -> Icons.Rounded.Agriculture to AmberHarvest
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = catTint.copy(alpha = 0.14f),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(catIcon, contentDescription = reminder.category, tint = catTint, modifier = Modifier.padding(8.dp))
                    }
                    Column {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "${reminder.category} • ${reminder.scheduledLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LeavesLogo(size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${reminder.leavesReward}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestGreen
                        )
                    }
                }
            }

            Text(reminder.notes, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!reminder.isCompleted) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("complete_reminder_${reminder.id}")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(completeLabel)
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Completed • +${reminder.leavesReward} Leaves Awarded",
                            style = MaterialTheme.typography.labelLarge,
                            color = ForestGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNotify,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = "Test Alert", modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete reminder")
                }
            }
        }
    }
}

@Composable
private fun AddReminderDialog(
    languageCode: String,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FERTILIZER") }
    var cadence by remember { mutableStateOf("DAILY") }
    var dayText by remember { mutableStateOf(defaultDay.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Cultivation Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceInputTextField(title, { title = it }, "Reminder Title", languageCode)
                VoiceInputTextField(notes, { notes = it }, "Instructions / Tools Needed", languageCode, singleLine = false, maxLines = 2)
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("FERTILIZER", "TOOLING", "HARVESTING").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text("Schedule Cadence", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY").forEach { cad ->
                        FilterChip(
                            selected = cadence == cad,
                            onClick = { cadence = cad },
                            label = { Text(cad.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                VoiceInputTextField(dayText, { dayText = it }, "Day of Month (1–31)", languageCode)
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(
                    title.ifBlank { "$category Checkup" },
                    notes.ifBlank { "Follow scheduled cultivation protocol." },
                    category,
                    cadence,
                    dayText.toIntOrNull() ?: defaultDay
                )
            }) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
