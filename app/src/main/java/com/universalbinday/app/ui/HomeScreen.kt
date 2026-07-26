package com.universalbinday.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universalbinday.app.model.Defaults
import com.universalbinday.app.model.UserBinConfig
import com.universalbinday.app.model.toComposeColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BinViewModel,
    onNavigateToSettings: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val council by viewModel.selectedCouncil.collectAsState()
    val context = LocalContext.current
    val canReport = viewModel.canReportMissed()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniversalBinDay") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canReport && council?.reportEmail != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.createReportIntent()?.let { intent ->
                            try {
                                context.startActivity(Intent.createChooser(intent, "Report missed collection"))
                            } catch (_: Exception) { }
                        }
                    },
                    icon = { Icon(Icons.Default.Email, contentDescription = null) },
                    text = { Text("Report Missed") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Council header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = council?.name ?: "No council selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (council == null) {
                        Text(
                            "Go to Settings to choose your council",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier = Modifier.height(16.dp))

            Text(
                "Your bins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val enabledBins = settings.bins.filter { it.enabled }
            if (enabledBins.isEmpty()) {
                Text(
                    "No bins configured yet. Open Settings to set up your collection days, colours and container types.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(enabledBins) { bin ->
                        BinCard(bin)
                    }
                }
            }

            if (canReport) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "It is after 5pm on a collection day – you can report a missed collection using the button below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BinCard(bin: UserBinConfig) {
    val def = Defaults.binDefinitions.find { it.id == bin.binId }
    val name = def?.name ?: bin.binId
    val dayNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysText = bin.daysOfWeek.sorted().joinToString(", ") { dayNames.getOrElse(it) { "?" } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colour + container visual
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bin.color.toComposeColor())
                    .border(2.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bin.containerType.displayName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${bin.containerType.displayName} • ${bin.frequency.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Days: $daysText",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
