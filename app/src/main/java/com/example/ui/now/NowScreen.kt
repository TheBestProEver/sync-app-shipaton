package com.example.ui.now

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NowScreen(
    currentSpace: SyncSpace,
    userProfile: UserProfile,
    circles: List<SyncCircle>,
    pulses: List<SyncPulse>,
    mixerMatch: MixerMatch?,
    calendarSyncStatus: String,
    onOpenStatusSheet: () -> Unit,
    onEndStatus: () -> Unit,
    onOpenPulseComposer: (prefillCircleId: String?, prefillTitle: String?, prefillTime: String?) -> Unit,
    onOpenPulseDetail: (SyncPulse) -> Unit,
    onRespondPulse: (pulseId: String, isIn: Boolean) -> Unit,
    onRespondMixer: (isIn: Boolean) -> Unit,
    onSelectPerson: (CircleMember) -> Unit,
    onSelectCircle: (SyncCircle) -> Unit,
    onTurnOffGhost: () -> Unit,
    onToggleFocusHours: () -> Unit,
    onSyncCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spaceCircles = circles.filter { it.spaceId == currentSpace.id }
    val spacePulses = pulses.filter { it.spaceId == currentSpace.id && it.state != "CANCELLED" }

    val allMembers = spaceCircles.flatMap { it.members }.distinctBy { it.id }
    val freeMembers = allMembers.filter { it.family == StatusFamily.OPEN }

    val isWorkplace = currentSpace.type == SpaceType.WORKPLACE

    // Runtime Permission Launcher for Calendar
    val context = LocalContext.current
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSyncCalendar()
        } else {
            // Permission denied or simulated, still trigger sync fallback
            onSyncCalendar()
        }
    }

    Scaffold(
        containerColor = if (isWorkplace) WorkplaceNavy else Night,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            SyncButton(
                text = if (isWorkplace) "Send team pulse" else "Send pulse",
                onClick = { onOpenPulseComposer(null, null, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
        ) {
            // 1. Differentiated Mode Banner (Atmospheric Night Facade)
            item {
                NightFacadeHeader(
                    freeCount = freeMembers.size,
                    totalCount = allMembers.size,
                    isWorkplace = isWorkplace,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // 2. Workplace Focus Banner (if workplace)
            if (isWorkplace) {
                item {
                    Surface(
                        color = if (currentSpace.isFocusHoursActive) WorkplaceCard else Wall,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (currentSpace.isFocusHoursActive) WorkplaceSteel.copy(alpha = 0.6f) else WorkplaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (currentSpace.isFocusHoursActive) Blinds else Dusk)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentSpace.isFocusHoursActive) "Protected Focus Hours" else "Focus Hours Idle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentSpace.isFocusHoursActive) WorkplaceSteel else Haze
                                )
                                Text(
                                    text = currentSpace.focusHoursBannerText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Moonlight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            SyncOutlinedButton(
                                text = if (currentSpace.isFocusHoursActive) "End Focus" else "Focus Now",
                                onClick = onToggleFocusHours
                            )
                        }
                    }
                }
            }

            // 3. User Status Card
            item {
                UserStatusCard(
                    space = currentSpace,
                    userProfile = userProfile,
                    isWorkplace = isWorkplace,
                    onChangeStatus = onOpenStatusSheet,
                    onEndStatus = onEndStatus,
                    onTurnOffGhost = onTurnOffGhost,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // 4. Calendar Sync (Room DB Cached Slots) Card
            item {
                Surface(
                    color = if (isWorkplace) WorkplaceCard else Wall,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Calendar Sync",
                                tint = if (isWorkplace) WorkplaceSteel else Amber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Device Calendar Sync (Room)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Moonlight
                                )
                                Text(
                                    text = calendarSyncStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Haze
                                )
                            }
                        }
                        SyncOutlinedButton(
                            text = "Sync",
                            onClick = {
                                calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                            }
                        )
                    }
                }
            }

            // 5. Workplace Mixer Card (if active)
            if (isWorkplace && mixerMatch != null) {
                item {
                    MixerCard(
                        match = mixerMatch,
                        onRespond = onRespondMixer,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // 6. Open Pulses
            if (spacePulses.isNotEmpty()) {
                item {
                    Text(
                        text = if (isWorkplace) "Active team pulses" else "Open pulses",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isWorkplace) WorkplaceSteel else Haze,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
                    )
                }
                items(spacePulses.take(3)) { pulse ->
                    PulseCard(
                        pulse = pulse,
                        isWorkplace = isWorkplace,
                        onCardClick = { onOpenPulseDetail(pulse) },
                        onRespondIn = { onRespondPulse(pulse.id, true) },
                        onRespondCant = { onRespondPulse(pulse.id, false) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // 7. Free Now Roster Section
            item {
                FreeNowSection(
                    freeCount = freeMembers.size,
                    freeMembers = freeMembers,
                    isWorkplace = isWorkplace,
                    onSelectPerson = onSelectPerson,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            // 8. Overlap Card (Campus vs Workplace)
            item {
                Surface(
                    color = if (isWorkplace) WorkplaceCard else Wall,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isWorkplace) "Mutual working hours overlap" else "Next overlap",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isWorkplace) WorkplaceSteel else Haze
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                val overlapCircle = if (isWorkplace) "Platform Team" else "Physics '29"
                                Text(
                                    text = overlapCircle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Moonlight
                                )
                                Text(
                                    text = "3:30 to 4:45 · 5 teammates free",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Haze
                                )
                            }
                            SyncOutlinedButton(
                                text = "Pulse this",
                                onClick = {
                                    val cId = spaceCircles.firstOrNull()?.id
                                    onOpenPulseComposer(cId, if (isWorkplace) "Quick Sync" else "Meetup", "3:30 pm")
                                }
                            )
                        }
                    }
                }
            }

            // 9. Circle / Team Roll-ups
            if (spaceCircles.isNotEmpty()) {
                item {
                    Text(
                        text = if (isWorkplace) "Teams in ${currentSpace.name}" else "Circles in ${currentSpace.name}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isWorkplace) WorkplaceSteel else Haze,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(spaceCircles) { circle ->
                    CircleRollupRow(
                        circle = circle,
                        isWorkplace = isWorkplace,
                        onClick = { onSelectCircle(circle) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserStatusCard(
    space: SyncSpace,
    userProfile: UserProfile,
    isWorkplace: Boolean,
    onChangeStatus: () -> Unit,
    onEndStatus: () -> Unit,
    onTurnOffGhost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGhost = userProfile.ghostUntil != null && userProfile.ghostUntil > System.currentTimeMillis()

    Surface(
        color = if (isWorkplace) WorkplaceCard else Wall,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isGhost) {
                Text(
                    text = "Ghost mode active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Dusk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "until 6:00",
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    color = Moonlight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You're invisible in all spaces",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Turn off",
                    style = MaterialTheme.typography.titleMedium,
                    color = Moonlight,
                    modifier = Modifier.clickable { onTurnOffGhost() }
                )
            } else if (space.currentStatusLabel.isNotBlank()) {
                Text(
                    text = "You're ${space.currentStatusLabel.lowercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Moonlight
                )
                Spacer(modifier = Modifier.height(4.dp))
                // End time in large serif
                Text(
                    text = "until 1:30",
                    fontFamily = FontFamily.Serif,
                    fontSize = 36.sp,
                    color = Moonlight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val placeStr = if (space.currentStatusPlace.isNotBlank()) " · ${space.currentStatusPlace}" else ""
                    Text(
                        text = "1h 42m left$placeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze
                    )
                    Row {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Moonlight,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onChangeStatus() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Dusk,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onEndStatus() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "No status set",
                    style = MaterialTheme.typography.labelSmall,
                    color = Dusk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isWorkplace) "Heads down until 11:00 (Focus window)" else "In class until 11:20",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Haze
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set a status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Moonlight,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onChangeStatus() }
                )
            }
        }
    }
}

@Composable
fun PulseCard(
    pulse: SyncPulse,
    isWorkplace: Boolean = false,
    onCardClick: () -> Unit,
    onRespondIn: () -> Unit,
    onRespondCant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfirmed = pulse.state == "CONFIRMED"

    Surface(
        color = if (isWorkplace) WorkplaceCard else Wall,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isConfirmed) Amber.copy(alpha = 0.5f) else if (isWorkplace) WorkplaceBorder else Mullion),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pulse.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Moonlight
                )
                if (isConfirmed) {
                    Text(
                        text = "It's on",
                        style = MaterialTheme.typography.labelLarge,
                        color = Amber,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "decide by 12:10",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "12:30",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = Moonlight
            )

            Text(
                text = "${pulse.place} · ${pulse.membersIn.size} of ${pulse.quorum} in",
                style = MaterialTheme.typography.bodyMedium,
                color = Haze
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pulse.membersIn.forEach { member ->
                        SyncWindow(
                            family = if (isConfirmed) StatusFamily.OPEN else member.family,
                            initials = member.initials,
                            toneName = member.tone,
                            size = WindowSize.ROLLUP
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pulse.isUserIn) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WallRaised,
                            border = BorderStroke(1.dp, Mullion)
                        ) {
                            Text(
                                text = "You're in",
                                color = Moonlight,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        SyncOutlinedButton(
                            text = "In",
                            onClick = onRespondIn
                        )
                        SyncOutlinedButton(
                            text = "Can't",
                            onClick = onRespondCant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MixerCard(
    match: MixerMatch,
    onRespond: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WorkplaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, WorkplaceSteel.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Coffee, contentDescription = null, tint = WorkplaceSteel, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "This week's Workplace Mixer",
                    style = MaterialTheme.typography.labelSmall,
                    color = WorkplaceSteel,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = match.title,
                style = MaterialTheme.typography.titleMedium,
                color = Moonlight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${match.slot}?",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = Moonlight
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (match.isUserIn) {
                    Text(
                        text = "You're in for coffee!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Moonlight
                    )
                } else {
                    SyncOutlinedButton(
                        text = "In",
                        onClick = { onRespond(true) }
                    )
                    SyncOutlinedButton(
                        text = "Can't",
                        onClick = { onRespond(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun FreeNowSection(
    freeCount: Int,
    freeMembers: List<CircleMember>,
    isWorkplace: Boolean,
    onSelectPerson: (CircleMember) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = "$freeCount free now",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    color = Moonlight
                )
                Text(
                    text = if (isWorkplace) "Colleagues free for quick syncs or coffee" else "Friends free on campus right now",
                    style = MaterialTheme.typography.labelSmall,
                    color = Haze
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick row of glowing window avatars
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(freeMembers) { member ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectPerson(member) }
                        .padding(vertical = 4.dp)
                ) {
                    SyncWindow(
                        family = StatusFamily.OPEN,
                        initials = member.initials,
                        toneName = member.tone,
                        size = WindowSize.DEFAULT
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = member.name.substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Moonlight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Meaningful detailed member cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            freeMembers.forEach { member ->
                Surface(
                    color = if (isWorkplace) WorkplaceCard else Wall,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPerson(member) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SyncWindow(
                                family = StatusFamily.OPEN,
                                initials = member.initials,
                                toneName = member.tone,
                                size = WindowSize.DEFAULT
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Moonlight,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val detailStr = buildString {
                                    append(member.statusLabel)
                                    if (member.place.isNotBlank()) append(" · ${member.place}")
                                    val tag = member.department ?: member.sharedCircles.firstOrNull()
                                    if (!tag.isNullOrBlank()) append(" ($tag)")
                                }
                                Text(
                                    text = detailStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Haze,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        SyncOutlinedButton(
                            text = "Meet",
                            onClick = { onSelectPerson(member) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircleRollupRow(
    circle: SyncCircle,
    isWorkplace: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isWorkplace) WorkplaceCard else Wall,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = circle.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Moonlight,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    circle.members.take(6).forEach { m ->
                        SyncWindow(
                            family = m.family,
                            initials = m.initials,
                            toneName = m.tone,
                            size = WindowSize.ROLLUP
                        )
                    }
                }

                Text(
                    text = "${circle.freeCount} of ${circle.memberCount} free",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )
            }
        }
    }
}
