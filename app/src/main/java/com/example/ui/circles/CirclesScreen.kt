package com.example.ui.circles

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun CirclesScreen(
    currentSpace: SyncSpace,
    userProfile: UserProfile,
    circles: List<SyncCircle>,
    discoverCircles: List<DiscoverCircle>,
    onSelectPerson: (CircleMember) -> Unit,
    onPulseCircle: (circleId: String, circleName: String) -> Unit,
    onOpenProPaywall: () -> Unit,
    onAddMemberToCircle: (circleId: String, name: String) -> Boolean,
    onRequestJoinDiscoverCircle: (circleId: String) -> Unit,
    onJoinCircleByCode: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spaceCircles = circles.filter { it.spaceId == currentSpace.id }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Circles/Teams, 1 = Discover (Community only)
    var selectedCircleForDetail by remember { mutableStateOf<SyncCircle?>(null) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }

    val screenTitle = if (currentSpace.type == SpaceType.WORKPLACE) "Teams" else "Circles"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Night)
    ) {
        // Tab Segment if Community (Circles vs Discover)
        if (currentSpace.type == SpaceType.COMMUNITY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Mullion),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 0) Moonlight else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "My Circles",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 0) Night else Haze
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == 1) Moonlight else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Discover",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 1) Night else Haze
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == 1 && currentSpace.type == SpaceType.COMMUNITY) {
            // Discover Screen (Section 10.8)
            DiscoverContent(
                discoverList = discoverCircles,
                onRequestJoin = onRequestJoinDiscoverCircle
            )
        } else {
            // Circles / Teams List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Actions: Start a circle (filled), Join (outlined)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    SyncButton(
                        text = "Start a $screenTitle",
                        onClick = {
                            if (!userProfile.isPro && spaceCircles.count { it.isOwner } >= 2) {
                                onOpenProPaywall()
                            } else {
                                // Handled
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SyncOutlinedButton(
                        text = "Join code",
                        onClick = { showJoinCodeDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(spaceCircles) { circle ->
                        CircleCard(
                            circle = circle,
                            onClick = { selectedCircleForDetail = circle }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog for Circle
    selectedCircleForDetail?.let { circle ->
        // Keep synced with current state
        val updatedCircle = circles.firstOrNull { it.id == circle.id } ?: circle
        CircleDetailDialog(
            circle = updatedCircle,
            userProfile = userProfile,
            onDismiss = { selectedCircleForDetail = null },
            onSelectPerson = onSelectPerson,
            onPulseCircle = { onPulseCircle(circle.id, circle.name) },
            onAdd13thMember = {
                val ok = onAddMemberToCircle(circle.id, "Jordan Taylor")
                if (!ok) {
                    onOpenProPaywall()
                }
            },
            onOpenProPaywall = onOpenProPaywall
        )
    }

    // Join Code Dialog
    if (showJoinCodeDialog) {
        JoinCodeDialog(
            onDismiss = { showJoinCodeDialog = false },
            onJoin = { code ->
                onJoinCircleByCode(code)
                showJoinCodeDialog = false
            }
        )
    }
}

@Composable
fun CircleCard(
    circle: SyncCircle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Wall,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Mullion),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = circle.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Moonlight
                    )
                    Text(
                        text = "${circle.kind} · code ${circle.joinCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                }
                Text(
                    text = "${circle.freeCount} free now",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = if (circle.freeCount > 0) Amber else Haze
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Facade of members in circle
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                circle.members.take(8).forEach { member ->
                    SyncWindow(
                        family = member.family,
                        initials = member.initials,
                        toneName = member.tone,
                        size = WindowSize.DEFAULT
                    )
                }
                if (circle.memberCount > 8) {
                    Box(
                        modifier = Modifier
                            .size(28.dp, 36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(WallRaised)
                            .border(1.dp, Mullion, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${circle.memberCount - 8}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Haze
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircleDetailDialog(
    circle: SyncCircle,
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSelectPerson: (CircleMember) -> Unit,
    onPulseCircle: () -> Unit,
    onAdd13thMember: () -> Unit,
    onOpenProPaywall: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
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
                        Text(text = circle.name, style = MaterialTheme.typography.titleLarge, color = Moonlight)
                        Text(text = "${circle.memberCount} members · Code: ${circle.joinCode}", style = MaterialTheme.typography.labelSmall, color = Haze)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: Pulse this circle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SyncButton(
                        text = "Pulse circle",
                        onClick = {
                            onDismiss()
                            onPulseCircle()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SyncOutlinedButton(
                        text = "+ Member",
                        onClick = onAdd13thMember,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If circle is at 12 members and not Pro: prompt indicator
                if (circle.memberCount >= 12 && !userProfile.isPro) {
                    Surface(
                        color = WallRaised,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProPaywall() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Circle full at 12. Tap to unlock Pro for unlimited members.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Roster (${circle.memberCount})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Moonlight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Full member roster
                circle.members.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPerson(m) }
                            .padding(vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SyncWindow(
                                family = m.family,
                                initials = m.initials,
                                toneName = m.tone,
                                size = WindowSize.DEFAULT
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = m.name, style = MaterialTheme.typography.bodyLarge, color = Moonlight)
                                Text(text = m.statusLabel, style = MaterialTheme.typography.labelSmall, color = if (m.family == StatusFamily.OPEN) Amber else Haze)
                            }
                        }
                        if (m.place.isNotBlank()) {
                            Text(text = m.place, style = MaterialTheme.typography.labelSmall, color = Dusk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverContent(
    discoverList: List<DiscoverCircle>,
    onRequestJoin: (String) -> Unit
) {
    var selectedCity by remember { mutableStateOf("Chicago") }
    val cities = listOf("Chicago", "Lisbon", "Mumbai", "London")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Discover public circles",
            style = MaterialTheme.typography.labelLarge,
            color = Haze,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Typed city chips (Section 10.8: Type a city, never GPS!)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cities) { city ->
                val isSel = selectedCity == city
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSel) Moonlight else WallRaised,
                    modifier = Modifier.clickable { selectedCity = city }
                ) {
                    Text(
                        text = city,
                        color = if (isSel) Night else Moonlight,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            val filtered = discoverList.filter { it.city.equals(selectedCity, ignoreCase = true) }
            items(filtered) { circle ->
                Surface(
                    color = Wall,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Mullion),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = circle.name, style = MaterialTheme.typography.titleMedium, color = Moonlight)
                            Text(text = "${circle.memberCount} members", style = MaterialTheme.typography.labelSmall, color = Haze)
                        }

                        Text(text = "${circle.category} · ${circle.neighborhood}", style = MaterialTheme.typography.bodyMedium, color = Haze)

                        if (!circle.ritual.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = circle.ritual, style = MaterialTheme.typography.labelSmall, color = Amber)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (circle.isRequested) {
                            Text(text = "Request sent to admins", style = MaterialTheme.typography.bodyMedium, color = Haze)
                        } else {
                            SyncOutlinedButton(
                                text = "Request to join",
                                onClick = { onRequestJoin(circle.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JoinCodeDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Join a circle", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Enter the 6-character circle code", style = MaterialTheme.typography.bodyMedium, color = Haze)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.uppercase() },
                    placeholder = { Text("e.g. QK7M9P", color = Dusk) },
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
                    text = "Join",
                    onClick = { onJoin(code) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
