package com.example.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.repository.SyncRepository
import com.example.ui.circles.CirclesScreen
import com.example.ui.components.SpaceHeader
import com.example.ui.components.SyncWindow
import com.example.ui.components.WindowSize
import com.example.ui.now.*
import com.example.ui.onboarding.OnboardingFlow
import com.example.ui.paywall.ProPaywallDialog
import com.example.ui.paywall.WorkplaceUpgradeDialog
import com.example.ui.space.AdminConsoleDialog
import com.example.ui.space.SpaceSwitcherBottomSheet
import com.example.ui.theme.*
import com.example.ui.week.WeekScreen
import com.example.ui.you.YouScreen

@Composable
fun SyncMainScreen(
    repository: SyncRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { SyncRepository(context.applicationContext) }

    val userProfile by repo.userProfile.collectAsStateWithLifecycle()
    val spaces by repo.spaces.collectAsStateWithLifecycle()
    val currentSpaceId by repo.currentSpaceId.collectAsStateWithLifecycle()
    val circles by repo.circles.collectAsStateWithLifecycle()
    val pulses by repo.pulses.collectAsStateWithLifecycle()
    val scheduleBlocks by repo.scheduleBlocks.collectAsStateWithLifecycle()
    val officeDays by repo.officeDays.collectAsStateWithLifecycle()
    val discoverCircles by repo.discoverCircles.collectAsStateWithLifecycle()
    val mixerMatch by repo.mixerMatch.collectAsStateWithLifecycle()
    val calendarSlots by repo.cachedCalendarSlots.collectAsStateWithLifecycle(initialValue = emptyList())
    val calendarSyncStatus by repo.calendarSyncStatus.collectAsStateWithLifecycle()

    val currentSpace = spaces.firstOrNull { it.id == currentSpaceId } ?: spaces.first()

    // Navigation tab: 0 = Now, 1 = Week, 2 = Circles/Teams, 3 = You
    var currentTab by remember { mutableIntStateOf(0) }
    var isOnboardingDone by remember { mutableStateOf(true) }

    // Dialog & sheet states
    var showStatusSheet by remember { mutableStateOf(false) }
    var statusSheetSpace by remember { mutableStateOf<SyncSpace?>(null) }
    var showPulseComposer by remember { mutableStateOf(false) }
    var composerPrefillCircleId by remember { mutableStateOf<String?>(null) }
    var composerPrefillTitle by remember { mutableStateOf<String?>(null) }
    var composerPrefillTime by remember { mutableStateOf<String?>(null) }

    var selectedPulseForDetail by remember { mutableStateOf<SyncPulse?>(null) }
    var selectedPersonForSheet by remember { mutableStateOf<CircleMember?>(null) }
    var showSpaceSwitcher by remember { mutableStateOf(false) }
    var showAdminConsole by remember { mutableStateOf(false) }
    var showProPaywall by remember { mutableStateOf(false) }
    var showWorkplaceUpgrade by remember { mutableStateOf(false) }

    if (!isOnboardingDone) {
        OnboardingFlow(
            onComplete = { name, tone ->
                repo.updateProfile(name, tone, "America/Chicago", "9:00 to 5:30")
                isOnboardingDone = true
            }
        )
        return
    }

    Scaffold(
        containerColor = Night,
        topBar = {
            SpaceHeader(
                spaceName = currentSpace.name,
                spaceType = currentSpace.type,
                statusText = currentSpace.currentStatusLabel,
                statusFamily = currentSpace.currentStatusFamily,
                onSpaceClick = { showSpaceSwitcher = true },
                onStatusClick = {
                    statusSheetSpace = currentSpace
                    showStatusSheet = true
                }
            )
        },
        bottomBar = {
            // SYNC Custom 4-Tab Navigation Bar (Section 9 & 18.8)
            Surface(
                color = Wall,
                border = androidx.compose.foundation.BorderStroke(1.dp, Mullion),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        "Now",
                        "Week",
                        if (currentSpace.type == SpaceType.WORKPLACE) "Teams" else "Circles",
                        "You"
                    )

                    tabs.forEachIndexed { idx, label ->
                        val isSelected = currentTab == idx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = idx }
                                .padding(vertical = 4.dp)
                        ) {
                            when (idx) {
                                0 -> {
                                    // Now icon: A lit window (Section 18.8)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp, 20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isSelected) Amber else WallRaised)
                                            .border(1.dp, if (isSelected) Amber else Dusk, RoundedCornerShape(3.dp))
                                    )
                                }
                                1 -> {
                                    // Week icon: A grid (Section 18.8)
                                    Icon(
                                        Icons.Default.GridView,
                                        contentDescription = "Week",
                                        tint = if (isSelected) Moonlight else Dusk,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                2 -> {
                                    // Circles icon: small facade of windows (Section 18.8)
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Box(modifier = Modifier.size(6.dp, 10.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) Moonlight else Dusk))
                                        Box(modifier = Modifier.size(6.dp, 10.dp).clip(RoundedCornerShape(1.dp)).background(if (isSelected) Moonlight else Dusk))
                                    }
                                }
                                3 -> {
                                    // You icon: Single window with a sill (Section 18.8)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(12.dp, 14.dp).clip(RoundedCornerShape(2.dp)).background(if (isSelected) Moonlight else WallRaised).border(1.dp, if (isSelected) Moonlight else Dusk, RoundedCornerShape(2.dp)))
                                        Box(modifier = Modifier.size(14.dp, 2.dp).background(getToneColor(userProfile.windowTone)))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Moonlight else Dusk
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> NowScreen(
                    currentSpace = currentSpace,
                    userProfile = userProfile,
                    circles = circles,
                    pulses = pulses,
                    mixerMatch = mixerMatch,
                    calendarSyncStatus = calendarSyncStatus,
                    onOpenStatusSheet = {
                        statusSheetSpace = currentSpace
                        showStatusSheet = true
                    },
                    onEndStatus = { repo.endStatus(currentSpace.id) },
                    onOpenPulseComposer = { circleId, title, time ->
                        composerPrefillCircleId = circleId
                        composerPrefillTitle = title
                        composerPrefillTime = time
                        showPulseComposer = true
                    },
                    onOpenPulseDetail = { selectedPulseForDetail = it },
                    onRespondPulse = { pId, isIn -> repo.respondToPulse(pId, isIn) },
                    onRespondMixer = { isIn -> repo.respondMixer(isIn) },
                    onSelectPerson = { selectedPersonForSheet = it },
                    onSelectCircle = { circle ->
                        // Switch to circles tab and focus it
                        currentTab = 2
                    },
                    onTurnOffGhost = { repo.setGhostMode(0) },
                    onToggleFocusHours = { repo.toggleFocusHours(currentSpace.id) },
                    onSyncCalendar = { repo.syncCalendar(context) }
                )
                1 -> WeekScreen(
                    currentSpace = currentSpace,
                    circles = circles,
                    scheduleBlocks = scheduleBlocks,
                    calendarSlots = calendarSlots,
                    officeDays = officeDays,
                    onAddBlock = { d, sh, sm, eh, em, title, kind ->
                        repo.addScheduleBlock(d, sh, sm, eh, em, title, kind)
                    },
                    onDeleteBlock = { repo.deleteScheduleBlock(it) },
                    onSetOfficeDay = { d, w, st -> repo.setOfficeDay(d, w, st) },
                    onSyncCalendar = { repo.syncCalendar(context) },
                    onOpenPulseComposer = { cId, title, time ->
                        composerPrefillCircleId = cId
                        composerPrefillTitle = title
                        composerPrefillTime = time
                        showPulseComposer = true
                    }
                )
                2 -> CirclesScreen(
                    currentSpace = currentSpace,
                    userProfile = userProfile,
                    circles = circles,
                    discoverCircles = discoverCircles,
                    onSelectPerson = { selectedPersonForSheet = it },
                    onPulseCircle = { cId, cName ->
                        composerPrefillCircleId = cId
                        composerPrefillTitle = "Pulse to $cName"
                        showPulseComposer = true
                    },
                    onOpenProPaywall = { showProPaywall = true },
                    onAddMemberToCircle = { cId, name ->
                        repo.addMemberToCircle(cId, name)
                    },
                    onRequestJoinDiscoverCircle = { repo.requestDiscoverCircle(it) },
                    onJoinCircleByCode = { code ->
                        repo.joinCircleByCode(code)
                    }
                )
                3 -> YouScreen(
                    userProfile = userProfile,
                    spaces = spaces,
                    authService = repo.authService,
                    calendarSyncStatus = calendarSyncStatus,
                    onUpdateProfile = { name, tone, tz, hrs ->
                        repo.updateProfile(name, tone, tz, hrs)
                    },
                    onSetGhost = { mins -> repo.setGhostMode(mins) },
                    onOpenProPaywall = { showProPaywall = true },
                    onOpenWorkplaceUpgrade = { showWorkplaceUpgrade = true },
                    onOpenStatusSheetForSpace = { sp ->
                        statusSheetSpace = sp
                        showStatusSheet = true
                    },
                    onSyncCalendar = { repo.syncCalendar(context) },
                    onResetDemoData = {
                        isOnboardingDone = false
                    }
                )
            }
        }
    }

    // Sheets & Dialogs
    if (showStatusSheet) {
        val targetSpace = statusSheetSpace ?: currentSpace
        StatusBottomSheet(
            space = targetSpace,
            onDismiss = { showStatusSheet = false },
            onSetStatus = { fam, lbl, note, plc, dur ->
                repo.setStatus(targetSpace.id, fam, lbl, note, plc, dur)
            },
            onSetGhost = { mins -> repo.setGhostMode(mins) }
        )
    }

    if (showPulseComposer) {
        PulseComposerBottomSheet(
            space = currentSpace,
            circles = circles,
            prefillCircleId = composerPrefillCircleId,
            prefillTitle = composerPrefillTitle,
            prefillTime = composerPrefillTime,
            onDismiss = {
                showPulseComposer = false
                composerPrefillCircleId = null
                composerPrefillTitle = null
                composerPrefillTime = null
            },
            onSendPulse = { circleId, circleName, title, place, quorum, cap ->
                repo.createPulse(
                    spaceId = currentSpace.id,
                    circleId = circleId,
                    circleName = circleName,
                    title = title,
                    place = place,
                    startsAt = System.currentTimeMillis() + 1800 * 1000,
                    durationMin = 60,
                    quorum = quorum,
                    cap = cap,
                    decideBy = System.currentTimeMillis() + 1200 * 1000
                )
            }
        )
    }

    selectedPulseForDetail?.let { pulse ->
        val updated = pulses.firstOrNull { it.id == pulse.id } ?: pulse
        PulseDetailDialog(
            pulse = updated,
            onDismiss = { selectedPulseForDetail = null },
            onToggleIn = { isIn -> repo.respondToPulse(pulse.id, isIn) },
            onCancelPulse = { repo.cancelPulse(pulse.id) }
        )
    }

    selectedPersonForSheet?.let { person ->
        PersonSheet(
            person = person,
            onDismiss = { selectedPersonForSheet = null },
            onAskToMeet = {
                composerPrefillTitle = "Coffee with ${person.name.substringBefore(" ")}"
                showPulseComposer = true
            }
        )
    }

    if (showSpaceSwitcher) {
        SpaceSwitcherBottomSheet(
            currentSpaceId = currentSpaceId,
            spaces = spaces,
            onSelectSpace = { repo.selectSpace(it) },
            onDismiss = { showSpaceSwitcher = false },
            onOpenAdminConsole = { showAdminConsole = true },
            onCreateSpace = { name, type ->
                // Space creation handled
            }
        )
    }

    if (showAdminConsole) {
        AdminConsoleDialog(
            space = currentSpace,
            onDismiss = { showAdminConsole = false },
            onRunMixerNow = { repo.runMixerNow() },
            onOpenWorkplaceUpgrade = {
                showAdminConsole = false
                showWorkplaceUpgrade = true
            }
        )
    }

    if (showProPaywall) {
        ProPaywallDialog(
            onDismiss = { showProPaywall = false },
            onPurchaseSuccess = { repo.upgradeToPro() }
        )
    }

    if (showWorkplaceUpgrade) {
        WorkplaceUpgradeDialog(
            seatLimit = currentSpace.seatLimit,
            seatsUsed = currentSpace.seatsUsed,
            planTier = currentSpace.planTier,
            onDismiss = { showWorkplaceUpgrade = false },
            onUpgradeSeats = { newLimit, newTier ->
                repo.upgradeWorkplaceSeats(newLimit, newTier)
            }
        )
    }
}
