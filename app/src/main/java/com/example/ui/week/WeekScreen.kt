package com.example.ui.week

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CalendarSlotEntity
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WeekScreen(
    currentSpace: SyncSpace,
    circles: List<SyncCircle>,
    scheduleBlocks: List<ScheduleBlock>,
    calendarSlots: List<CalendarSlotEntity>,
    officeDays: List<OfficeDayPlan>,
    onAddBlock: (day: Int, startH: Int, startM: Int, endH: Int, endM: Int, title: String, kind: String) -> Unit,
    onDeleteBlock: (id: String) -> Unit,
    onSetOfficeDay: (day: Int, weekIndex: Int, status: String) -> Unit,
    onSyncCalendar: () -> Unit,
    onOpenPulseComposer: (circleId: String?, title: String?, time: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val spaceCircles = circles.filter { it.spaceId == currentSpace.id }
    var selectedOverlayCircle by remember { mutableStateOf(spaceCircles.firstOrNull()) }
    var selectedWorkplaceTab by remember { mutableIntStateOf(0) } // 0 = Availability, 1 = Office days
    var showEditWeekDialog by remember { mutableStateOf(false) }
    var showSlotFinderSheet by remember { mutableStateOf(false) }
    var selectedSlotInfo by remember { mutableStateOf<SlotInfo?>(null) }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val isWorkplace = currentSpace.type == SpaceType.WORKPLACE

    // Permission launcher for calendar
    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onSyncCalendar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isWorkplace) WorkplaceNavy else Night)
    ) {
        // Controls Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
        ) {
            // Circle overlay selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val idx = spaceCircles.indexOfFirst { it.id == selectedOverlayCircle?.id }
                    if (idx >= 0 && spaceCircles.isNotEmpty()) {
                        selectedOverlayCircle = spaceCircles[(idx + 1) % spaceCircles.size]
                    }
                }
            ) {
                Text(
                    text = "Overlay: ${selectedOverlayCircle?.name ?: "All"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Moonlight,
                    fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Change overlay", tint = if (isWorkplace) WorkplaceSteel else Haze)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isWorkplace) {
                    SyncOutlinedButton(
                        text = "Slot finder",
                        onClick = { showSlotFinderSheet = true }
                    )
                }
                SyncOutlinedButton(
                    text = "Edit week",
                    onClick = { showEditWeekDialog = true }
                )
            }
        }

        // Calendar Room Cache Info Banner
        Surface(
            color = if (isWorkplace) WorkplaceCard else Wall,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.5.dp, if (isWorkplace) WorkplaceBorder else Mullion),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clickable { calendarLauncher.launch(Manifest.permission.READ_CALENDAR) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isWorkplace) WorkplaceSteel else Amber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (calendarSlots.isNotEmpty()) "${calendarSlots.size} calendar slots cached in Room (titles kept private)" else "Tap to sync Google Calendar into Room",
                        style = MaterialTheme.typography.labelSmall,
                        color = Haze
                    )
                }
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isWorkplace) WorkplaceSteel else Amber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Workplace Segmented Control: [ Availability ] [ Office days ]
        if (isWorkplace) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Surface(
                    color = WorkplaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkplaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedWorkplaceTab == 0) Moonlight else Color.Transparent)
                                .clickable { selectedWorkplaceTab = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Availability",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedWorkplaceTab == 0) Night else Haze
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedWorkplaceTab == 1) Moonlight else Color.Transparent)
                                .clickable { selectedWorkplaceTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Office days",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedWorkplaceTab == 1) Night else Haze
                            )
                        }
                    }
                }
            }
        }

        if (isWorkplace && selectedWorkplaceTab == 1) {
            // Office days View (Section 14.1)
            OfficeDaysView(
                officeDays = officeDays,
                onSetOfficeDay = onSetOfficeDay,
                onPulseOfficeLunch = {
                    onOpenPulseComposer(null, "Lunch in office", "12:30 pm")
                }
            )
        } else {
            // Availability 7-day Grid View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                // Day Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 54.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = Haze,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Hours Grid (8 am to 7 pm)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val hours = (8..19).toList()
                    items(hours) { hour ->
                        val timeStr = if (hour < 12) "${hour}am" else if (hour == 12) "12pm" else "${hour - 12}pm"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = Dusk,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // 7 day cells for this hour
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..7).forEach { dayNum ->
                                    val userManualBlock = scheduleBlocks.firstOrNull { b ->
                                        b.dayOfWeek == dayNum && b.startHour <= hour && b.endHour > hour
                                    }
                                    val userCalBlock = calendarSlots.firstOrNull { c ->
                                        c.dayOfWeek == dayNum && c.startHour <= hour && c.endHour > hour
                                    }

                                    val isUserBusy = userManualBlock != null || userCalBlock != null
                                    val busyTitle = userManualBlock?.title ?: userCalBlock?.let { "[Calendar] ${it.eventTitle}" }

                                    // Amber steps based on mock availability for circle
                                    val freeRatio = when ((dayNum + hour) % 4) {
                                        0 -> 0.15f
                                        1 -> 0.40f
                                        2 -> 0.65f
                                        else -> 0.85f
                                    }

                                    val cellColor = if (isUserBusy) {
                                        if (userCalBlock != null) WallRaised.copy(alpha = 0.9f) else WallRaised
                                    } else {
                                        when {
                                            freeRatio >= 0.75f -> Amber.copy(alpha = 0.90f)
                                            freeRatio >= 0.50f -> Amber.copy(alpha = 0.60f)
                                            freeRatio >= 0.25f -> Amber.copy(alpha = 0.35f)
                                            else -> Amber.copy(alpha = 0.15f)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(cellColor)
                                            .border(0.5.dp, if (userCalBlock != null) Dusk else Mullion, RoundedCornerShape(3.dp))
                                            .clickable {
                                                val freeCount = (freeRatio * 9).toInt().coerceAtLeast(1)
                                                selectedSlotInfo = SlotInfo(
                                                    dayName = daysOfWeek[dayNum - 1],
                                                    timeRange = "$timeStr to ${if (hour + 1 < 12) "${hour + 1}am" else if (hour + 1 == 12) "12pm" else "${hour - 11}pm"}",
                                                    freeCount = freeCount,
                                                    totalMembers = 9,
                                                    userBusyTitle = busyTitle
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid Legend (Section 10.6)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(WallRaised))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "you're busy", style = MaterialTheme.typography.labelSmall, color = Haze)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Amber.copy(alpha = 0.25f)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Amber.copy(alpha = 0.60f)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Amber))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "more of circle free", style = MaterialTheme.typography.labelSmall, color = Haze)
                }
            }
        }
    }

    selectedSlotInfo?.let { slot ->
        SlotBottomSheet(
            slotInfo = slot,
            onDismiss = { selectedSlotInfo = null },
            onSendPulse = {
                onOpenPulseComposer(selectedOverlayCircle?.id, "Meetup", "${slot.dayName} ${slot.timeRange.substringBefore(" to")}")
                selectedSlotInfo = null
            }
        )
    }

    if (showEditWeekDialog) {
        EditWeekDialog(
            scheduleBlocks = scheduleBlocks,
            calendarSlots = calendarSlots,
            onDismiss = { showEditWeekDialog = false },
            onAddBlock = onAddBlock,
            onDeleteBlock = onDeleteBlock
        )
    }

    if (showSlotFinderSheet) {
        SlotFinderDialog(
            circles = spaceCircles,
            calendarSlots = calendarSlots,
            onDismiss = { showSlotFinderSheet = false },
            onSendPulse = { title, time ->
                onOpenPulseComposer(null, title, time)
                showSlotFinderSheet = false
            }
        )
    }
}

data class SlotInfo(
    val dayName: String,
    val timeRange: String,
    val freeCount: Int,
    val totalMembers: Int,
    val userBusyTitle: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotBottomSheet(
    slotInfo: SlotInfo,
    onDismiss: () -> Unit,
    onSendPulse: () -> Unit
) {
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
            Text(
                text = "${slotInfo.dayName} ${slotInfo.timeRange}",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = Moonlight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${slotInfo.freeCount} of ${slotInfo.totalMembers} free",
                style = MaterialTheme.typography.titleMedium,
                color = Amber
            )

            if (!slotInfo.userBusyTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Wall,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You have: ${slotInfo.userBusyTitle}\n(Protected busy slot · Title private to you)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Teammates free in this slot", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Maya", "MC", "Rose"),
                    Triple("Tomás", "TR", "Sage"),
                    Triple("Liam", "LO", "Teal"),
                    Triple("Jordan", "JS", "Moss")
                ).take(slotInfo.freeCount).forEach { (_, init, tone) ->
                    SyncWindow(
                        family = StatusFamily.OPEN,
                        initials = init,
                        toneName = tone,
                        size = WindowSize.DEFAULT
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SyncButton(
                text = "Send pulse for this slot",
                onClick = onSendPulse,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun OfficeDaysView(
    officeDays: List<OfficeDayPlan>,
    onSetOfficeDay: (day: Int, weekIndex: Int, status: String) -> Unit,
    onPulseOfficeLunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Anchor day recommendation (Section 14.1)
        item {
            Surface(
                color = WorkplaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WorkplaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Best anchor day",
                        style = MaterialTheme.typography.labelSmall,
                        color = WorkplaceSteel
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wednesday",
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        color = Moonlight
                    )
                    Text(
                        text = "7 of 9 team members in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SyncButton(
                        text = "Lunch in office",
                        onClick = onPulseOfficeLunch
                    )
                }
            }
        }

        // This week section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "This week", style = MaterialTheme.typography.titleMedium, color = Moonlight)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = WorkplaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WorkplaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "You", style = MaterialTheme.typography.bodyLarge, color = Moonlight, modifier = Modifier.width(60.dp))
                        days.forEachIndexed { idx, day ->
                            val plan = officeDays.firstOrNull { it.dayOfWeek == idx + 1 && it.weekIndex == 0 }?.status ?: "NOT_PLANNED"
                            val icon = when (plan) {
                                "IN_OFFICE" -> "●"
                                "REMOTE" -> "–"
                                else -> "?"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        val next = when (plan) {
                                            "IN_OFFICE" -> "REMOTE"
                                            "REMOTE" -> "NOT_PLANNED"
                                            else -> "IN_OFFICE"
                                        }
                                        onSetOfficeDay(idx + 1, 0, next)
                                    }
                                    .padding(4.dp)
                            ) {
                                Text(text = day, style = MaterialTheme.typography.labelSmall, color = Haze)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = icon,
                                    color = if (plan == "IN_OFFICE") Moonlight else Dusk,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = WorkplaceBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Platform", style = MaterialTheme.typography.bodyMedium, color = Haze, modifier = Modifier.width(60.dp))
                        val counts = listOf("3", "6", "7", "5", "1")
                        counts.forEach { c ->
                            Text(text = c, style = MaterialTheme.typography.bodyMedium, color = Moonlight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }

        // Next week section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Next week", style = MaterialTheme.typography.titleMedium, color = Moonlight)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = WorkplaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WorkplaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "You", style = MaterialTheme.typography.bodyLarge, color = Moonlight, modifier = Modifier.width(60.dp))
                        days.forEachIndexed { idx, day ->
                            val plan = officeDays.firstOrNull { it.dayOfWeek == idx + 1 && it.weekIndex == 1 }?.status ?: "NOT_PLANNED"
                            val icon = when (plan) {
                                "IN_OFFICE" -> "●"
                                "REMOTE" -> "–"
                                else -> "?"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        val next = when (plan) {
                                            "IN_OFFICE" -> "REMOTE"
                                            "REMOTE" -> "NOT_PLANNED"
                                            else -> "IN_OFFICE"
                                        }
                                        onSetOfficeDay(idx + 1, 1, next)
                                    }
                                    .padding(4.dp)
                            ) {
                                Text(text = day, style = MaterialTheme.typography.labelSmall, color = Haze)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = icon,
                                    color = if (plan == "IN_OFFICE") Moonlight else Dusk,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = "● in office   – remote   ? not planned", style = MaterialTheme.typography.labelSmall, color = Dusk)
            }
        }
    }
}

@Composable
fun EditWeekDialog(
    scheduleBlocks: List<ScheduleBlock>,
    calendarSlots: List<CalendarSlotEntity>,
    onDismiss: () -> Unit,
    onAddBlock: (day: Int, startH: Int, startM: Int, endH: Int, endM: Int, title: String, kind: String) -> Unit,
    onDeleteBlock: (id: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableIntStateOf(1) }
    var startHour by remember { mutableIntStateOf(10) }
    var endHour by remember { mutableIntStateOf(12) }
    var kind by remember { mutableStateOf("class") }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
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
                    Text(text = "Edit regular week", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Text(
                    text = "Add regular blocks. Room DB combines these with your device calendar so SYNC finds overlaps without revealing private event titles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Private title (e.g. Physics lecture)", color = Haze) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moonlight,
                        unfocusedBorderColor = Mullion,
                        focusedTextColor = Moonlight,
                        unfocusedTextColor = Moonlight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Day", style = MaterialTheme.typography.labelSmall, color = Haze)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    days.indices.forEach { idx ->
                        val isSel = dayOfWeek == idx + 1
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Moonlight else WallRaised,
                            modifier = Modifier.clickable { dayOfWeek = idx + 1 }
                        ) {
                            Text(
                                text = days[idx],
                                color = if (isSel) Night else Moonlight,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Start hour", style = MaterialTheme.typography.labelSmall, color = Haze)
                        SyncOutlinedButton(text = "${startHour}:00", onClick = { startHour = if (startHour < 21) startHour + 1 else 8 })
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "End hour", style = MaterialTheme.typography.labelSmall, color = Haze)
                        SyncOutlinedButton(text = "${endHour}:00", onClick = { endHour = if (endHour < 22) endHour + 1 else startHour + 1 })
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SyncButton(
                    text = "Add block",
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddBlock(dayOfWeek, startHour, 0, endHour, 0, title, kind)
                            title = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Current commitments", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                Spacer(modifier = Modifier.height(8.dp))

                scheduleBlocks.take(4).forEach { block ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text(text = block.title, style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                            Text(text = "${days[block.dayOfWeek - 1]} ${block.startHour}:00 - ${block.endHour}:00", style = MaterialTheme.typography.labelSmall, color = Haze)
                        }
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = Signal,
                            modifier = Modifier.clickable { onDeleteBlock(block.id) }
                        )
                    }
                }

                if (calendarSlots.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Cached Calendar Events (Room)", style = MaterialTheme.typography.labelSmall, color = Amber)
                    calendarSlots.take(3).forEach { cal ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(text = cal.eventTitle, style = MaterialTheme.typography.bodySmall, color = Haze)
                            Text(text = "${days[cal.dayOfWeek - 1]} ${cal.startHour}:00", style = MaterialTheme.typography.labelSmall, color = Dusk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotFinderDialog(
    circles: List<SyncCircle>,
    calendarSlots: List<CalendarSlotEntity>,
    onDismiss: () -> Unit,
    onSendPulse: (title: String, time: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = WorkplaceCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, WorkplaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Workplace Slot Finder", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Text(
                    text = "Intersects free working hours across Chicago, New York, and London accounts using cached Room calendar slots.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkplaceSteel.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Mutual working free slot", style = MaterialTheme.typography.labelSmall, color = WorkplaceSteel)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Tomorrow at 2:00 pm", fontFamily = FontFamily.Serif, fontSize = 24.sp, color = Moonlight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "2:00 pm Chicago · 3:00 pm NY · 8:00 pm London", style = MaterialTheme.typography.labelSmall, color = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SyncButton(
                    text = "Send as team pulse",
                    onClick = { onSendPulse("Sprint Alignment", "Tomorrow 2:00 pm") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
