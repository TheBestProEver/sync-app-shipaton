package com.example.ui.space

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceSwitcherBottomSheet(
    currentSpaceId: String,
    spaces: List<SyncSpace>,
    onSelectSpace: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenAdminConsole: () -> Unit,
    onCreateSpace: (name: String, type: SpaceType) -> Unit
) {
    var showCreateSpaceDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WallRaised,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Switch spaces", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spaces list
            spaces.forEach { space ->
                val isSelected = space.id == currentSpaceId
                Surface(
                    color = if (isSelected) Wall else WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) Moonlight else Mullion),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectSpace(space.id)
                            onDismiss()
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = space.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Moonlight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TypeBadge(type = space.type)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val st = if (space.currentStatusLabel.isNotBlank()) space.currentStatusLabel else "No status set"
                            Text(text = st, style = MaterialTheme.typography.bodySmall, color = if (space.currentStatusFamily == StatusFamily.OPEN) Amber else Haze)
                        }

                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Moonlight)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentSpace = spaces.firstOrNull { it.id == currentSpaceId }
            if (currentSpace?.type == SpaceType.WORKPLACE) {
                SyncOutlinedButton(
                    text = "Workplace Admin Console",
                    onClick = {
                        onDismiss()
                        onOpenAdminConsole()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            SyncButton(
                text = "Create a space",
                onClick = { showCreateSpaceDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showCreateSpaceDialog) {
        CreateSpaceDialog(
            onDismiss = { showCreateSpaceDialog = false },
            onCreate = { name, type ->
                onCreateSpace(name, type)
                showCreateSpaceDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun CreateSpaceDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: SpaceType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SpaceType.COMMUNITY) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Create a space", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "One app, two space types.", style = MaterialTheme.typography.bodyMedium, color = Haze)

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == SpaceType.COMMUNITY,
                        onClick = { selectedType = SpaceType.COMMUNITY },
                        label = { Text("Community (Free)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Moonlight, selectedLabelColor = Night)
                    )
                    FilterChip(
                        selected = selectedType == SpaceType.WORKPLACE,
                        onClick = { selectedType = SpaceType.WORKPLACE },
                        label = { Text("Workplace (14-day trial)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Moonlight, selectedLabelColor = Night)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Space name", color = Haze) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moonlight,
                        unfocusedBorderColor = Mullion,
                        focusedTextColor = Moonlight,
                        unfocusedTextColor = Moonlight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                SyncButton(
                    text = "Create space",
                    onClick = { if (name.isNotBlank()) onCreate(name, selectedType) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AdminConsoleDialog(
    space: SyncSpace,
    onDismiss: () -> Unit,
    onRunMixerNow: () -> Unit,
    onOpenWorkplaceUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(text = "${space.name} Admin", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                        Text(text = "Tools for employees, aggregates for managers.", style = MaterialTheme.typography.labelSmall, color = Haze)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // People & Seats Card
                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "People and seats", style = MaterialTheme.typography.labelSmall, color = Haze)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${space.seatsUsed} of ${space.seatLimit} seats used",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = Moonlight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyncOutlinedButton(text = "Invite teammate", onClick = {})
                            SyncButton(text = "Manage plan", onClick = onOpenWorkplaceUpgrade)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Aggregate Workplace Insights (Section 14.6)
                Text(text = "Aggregate Insights", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                Text(text = "Numbers covering fewer than 5 people are never shown.", style = MaterialTheme.typography.labelSmall, color = Dusk)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Office attendance by weekday", style = MaterialTheme.typography.bodyMedium, color = Haze)
                        Spacer(modifier = Modifier.height(10.dp))

                        val days = listOf("Mon" to 37, "Tue" to 58, "Wed" to 81, "Thu" to 64, "Fri" to 22)
                        days.forEach { (d, pct) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Text(text = d, style = MaterialTheme.typography.labelSmall, color = Haze, modifier = Modifier.width(36.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Wall)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(pct / 100f)
                                            .background(Moonlight)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "$pct%", style = MaterialTheme.typography.labelSmall, color = Moonlight)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Mullion, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = "144", fontFamily = FontFamily.Serif, fontSize = 28.sp, color = Moonlight)
                                Text(text = "pulses completed", style = MaterialTheme.typography.labelSmall, color = Haze)
                            }
                            Column {
                                Text(text = "38", fontFamily = FontFamily.Serif, fontSize = 28.sp, color = Moonlight)
                                Text(text = "cross-team connections", style = MaterialTheme.typography.labelSmall, color = Haze)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mixer Demo Action
                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Mixer pairings", style = MaterialTheme.typography.labelSmall, color = Haze)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Connects people across departments for coffee.", style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                        Spacer(modifier = Modifier.height(10.dp))
                        SyncOutlinedButton(
                            text = "Run this week's Mixer now",
                            onClick = {
                                onRunMixerNow()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
