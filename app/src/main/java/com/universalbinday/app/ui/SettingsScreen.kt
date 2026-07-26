package com.universalbinday.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.universalbinday.app.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BinViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val selectedCouncil by viewModel.selectedCouncil.collectAsState()

    var showCouncilPicker by remember { mutableStateOf(false) }
    var editingBin by remember { mutableStateOf<UserBinConfig?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Council selector
            item {
                Text("Council", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCouncilPicker = true }
                ) {
                    Text(
                        text = selectedCouncil?.name ?: "Tap to select your council",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Notification time
            item {
                Text("Night-before reminder time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Currently ${settings.notificationHour.toString().padStart(2, '0')}:${settings.notificationMinute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Simple fixed 19:00 for MVP – can expand later with time picker
                TextButton(onClick = { viewModel.setNotificationTime(19, 0) }) {
                    Text("Reset to 19:00")
                }
            }

            item {
                Text("Your bins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Enable the services you have, set days, frequency, colour and container type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(settings.bins) { bin ->
                val def = Defaults.binDefinitions.find { it.id == bin.binId }
                BinConfigRow(
                    bin = bin,
                    name = def?.name ?: bin.binId,
                    onEdit = { editingBin = bin }
                )
            }
        }
    }

    if (showCouncilPicker) {
        AlertDialog(
            onDismissRequest = { showCouncilPicker = false },
            title = { Text("Select Council") },
            text = {
                Column {
                    Defaults.councils.forEach { council ->
                        Text(
                            text = council.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCouncil(council)
                                    showCouncilPicker = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCouncilPicker = false }) { Text("Cancel") }
            }
        )
    }

    editingBin?.let { bin ->
        BinEditDialog(
            bin = bin,
            onDismiss = { editingBin = null },
            onSave = {
                viewModel.updateBin(it)
                editingBin = null
            }
        )
    }
}

@Composable
fun BinConfigRow(
    bin: UserBinConfig,
    name: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (bin.enabled) bin.color.toComposeColor() else Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium)
                Text(
                    if (bin.enabled) "${bin.containerType.displayName} • ${bin.frequency.displayName}"
                    else "N/A – not used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = bin.enabled,
                onCheckedChange = null // edit via dialog
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinEditDialog(
    bin: UserBinConfig,
    onDismiss: () -> Unit,
    onSave: (UserBinConfig) -> Unit
) {
    var enabled by remember { mutableStateOf(bin.enabled) }
    var frequency by remember { mutableStateOf(bin.frequency) }
    var container by remember { mutableStateOf(bin.containerType) }
    var color by remember { mutableStateOf(bin.color) }
    var selectedDays by remember { mutableStateOf(bin.daysOfWeek.toMutableSet()) }
    var fortnightlyThisWeek by remember { mutableStateOf(bin.fortnightlyIsThisWeek) }

    val dayLabels = listOf(
        2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu",
        6 to "Fri", 7 to "Sat", 1 to "Sun"
    )

    val presetColours = listOf(
        0xFF212121, 0xFF1565C0, 0xFF2E7D32, 0xFF5D4037,
        0xFF00838F, 0xFF6D4C41, 0xFFC62828, 0xFFF9A825,
        0xFF6A1B9A, 0xFF0277BD, 0xFFFFFFFF, 0xFF757575
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val def = Defaults.binDefinitions.find { it.id == bin.binId }
            Text(def?.name ?: bin.binId)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                if (enabled) {
                    Text("Collection days", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        dayLabels.forEach { (day, label) ->
                            FilterChip(
                                selected = day in selectedDays,
                                onClick = {
                                    selectedDays = selectedDays.toMutableSet().apply {
                                        if (day in this) remove(day) else add(day)
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Frequency", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Frequency.entries.forEach { f ->
                            FilterChip(
                                selected = frequency == f,
                                onClick = { frequency = f },
                                label = { Text(f.displayName) }
                            )
                        }
                    }

                    if (frequency == Frequency.FORTNIGHTLY) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("This week is a collection week")
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = fortnightlyThisWeek,
                                onCheckedChange = { fortnightlyThisWeek = it }
                            )
                        }
                    }

                    Text("Container type", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContainerType.entries.forEach { c ->
                            FilterChip(
                                selected = container == c,
                                onClick = { container = c },
                                label = { Text(c.displayName) }
                            )
                        }
                    }

                    Text("Colour", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetColours.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(
                                        width = if (color == c) 3.dp else 1.dp,
                                        color = if (color == c) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { color = c }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    bin.copy(
                        enabled = enabled,
                        daysOfWeek = selectedDays,
                        frequency = frequency,
                        containerType = container,
                        color = color,
                        fortnightlyIsThisWeek = fortnightlyThisWeek
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
