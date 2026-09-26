package com.example.ui.now

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun StatusBottomSheet(
    space: SyncSpace,
    onDismiss: () -> Unit,
    onSetStatus: (family: StatusFamily, label: String, note: String, place: String, durationMin: Int) -> Unit,
    onSetGhost: (durationMin: Int) -> Unit
) {
    var selectedFamily by remember { mutableStateOf(StatusFamily.OPEN) }
    var selectedLabel by remember {
        mutableStateOf(
            if (space.statusWordsTemplate == "Workplace") "Open to chat"
            else if (space.statusWordsTemplate == "Club") "Up for a run"
            else "Free for food"
        )
    }
    var note by remember { mutableStateOf("") }
    var place by remember { mutableStateOf(space.places.firstOrNull() ?: "") }
    var durationMinutes by remember { mutableIntStateOf(120) } // Default 2 hr

    val openWords = when (space.statusWordsTemplate) {
        "Workplace" -> listOf("Open to chat", "Free for lunch", "Up for coffee")
        "Club" -> listOf("Up for a run", "Looking for a partner")
        else -> listOf("Free for food", "Study company welcome", "Free for timepass")
    }
    val focusedWords = when (space.statusWordsTemplate) {
        "Workplace" -> listOf("Heads down")
        "Club" -> listOf("Training")
        else -> listOf("Studying alone", "Recharging")
    }
    val busyWords = when (space.statusWordsTemplate) {
        "Workplace" -> listOf("In a meeting", "On a call", "Presenting")
        "Club" -> listOf("At practice")
        else -> listOf("In class", "At work")
    }
    val awayWords = when (space.statusWordsTemplate) {
        "Workplace" -> listOf("Off today", "Travelling")
        "Club" -> listOf("Rest day", "Out of town")
        else -> listOf("Out", "Asleep", "Off campus")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WallRaised,
        scrimColor = Night.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Set status in ${space.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Moonlight
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Family selector
            Text(text = "Family", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    StatusFamily.OPEN to "Open",
                    StatusFamily.FOCUSED to "Focused",
                    StatusFamily.BUSY to "Busy",
                    StatusFamily.AWAY to "Away"
                ).forEach { (fam, title) ->
                    val isSel = selectedFamily == fam
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedFamily = fam
                            selectedLabel = when (fam) {
                                StatusFamily.OPEN -> openWords.first()
                                StatusFamily.FOCUSED -> focusedWords.first()
                                StatusFamily.BUSY -> busyWords.first()
                                StatusFamily.AWAY -> awayWords.first()
                                else -> ""
                            }
                        },
                        label = { Text(title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Moonlight,
                            selectedLabelColor = Night,
                            containerColor = Wall,
                            labelColor = Haze
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = Mullion,
                            selectedBorderColor = Moonlight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Words options
            Text(text = "Status word", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(6.dp))
            val currentWords = when (selectedFamily) {
                StatusFamily.OPEN -> openWords
                StatusFamily.FOCUSED -> focusedWords
                StatusFamily.BUSY -> busyWords
                StatusFamily.AWAY -> awayWords
                else -> emptyList()
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentWords) { word ->
                    val isSel = selectedLabel == word
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) Moonlight else Wall,
                        border = BorderStroke(1.dp, if (isSel) Moonlight else Mullion),
                        modifier = Modifier.clickable { selectedLabel = word }
                    ) {
                        Text(
                            text = word,
                            color = if (isSel) Night else Moonlight,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duration chips: 30 min, 1 hr, 2 hr (default), 4 hr, End of day
            Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(6.dp))
            val durations = listOf(30 to "30m", 60 to "1h", 120 to "2h (default)", 240 to "4h", 480 to "End of day")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(durations) { (mins, title) ->
                    val isSel = durationMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) Moonlight else Wall,
                        border = BorderStroke(1.dp, if (isSel) Moonlight else Mullion),
                        modifier = Modifier.clickable { durationMinutes = mins }
                    ) {
                        Text(
                            text = title,
                            color = if (isSel) Night else Moonlight,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Place (typed with suggestions)
            Text(text = "Place (optional)", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                placeholder = { Text("e.g. Bartlett, The Reg", color = Dusk) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Moonlight,
                    unfocusedBorderColor = Mullion,
                    focusedTextColor = Moonlight,
                    unfocusedTextColor = Moonlight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Place suggestions chips
            if (space.places.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(space.places.take(5)) { p ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Wall,
                            modifier = Modifier.clickable { place = p }
                        ) {
                            Text(
                                text = p.substringBefore("(").trim(),
                                color = Haze,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note (optional, max 60 chars)
            Text(text = "Note (optional, up to 60 chars)", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 60) note = it },
                placeholder = { Text("Add detail...", color = Dusk) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Moonlight,
                    unfocusedBorderColor = Mullion,
                    focusedTextColor = Moonlight,
                    unfocusedTextColor = Moonlight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Audience privacy line (Section 10.3)
            Text(
                text = "Circles in ${space.name} see the label, note, and place.",
                style = MaterialTheme.typography.labelSmall,
                color = Dusk
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Go ghost option row
            Surface(
                color = Wall,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column {
                        Text(text = "Go ghost", style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                        Text(text = "Disappear from this space", style = MaterialTheme.typography.labelSmall, color = Dusk)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SyncOutlinedButton(text = "1h", onClick = { onSetGhost(60); onDismiss() })
                        SyncOutlinedButton(text = "4h", onClick = { onSetGhost(240); onDismiss() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // The one filled button: Set status
            SyncButton(
                text = "Set status",
                onClick = {
                    onSetStatus(selectedFamily, selectedLabel, note, place, durationMinutes)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseComposerBottomSheet(
    space: SyncSpace,
    circles: List<SyncCircle>,
    prefillCircleId: String? = null,
    prefillTitle: String? = null,
    prefillTime: String? = null,
    onDismiss: () -> Unit,
    onSendPulse: (circleId: String, circleName: String, title: String, place: String, quorum: Int, cap: Int?) -> Unit
) {
    val spaceCircles = circles.filter { it.spaceId == space.id }
    var selectedCircle by remember {
        mutableStateOf(spaceCircles.firstOrNull { it.id == prefillCircleId } ?: spaceCircles.firstOrNull())
    }

    val whatSuggestions = when (space.statusWordsTemplate) {
        "Workplace" -> listOf("Quick sync", "Coffee chat", "Team lunch", "Pairing")
        "Club" -> listOf("Run", "Hill intervals", "Long run", "Post-run smoothie")
        else -> listOf("Lunch", "Coffee", "Study", "Run", "Walk")
    }

    var title by remember { mutableStateOf(prefillTitle ?: whatSuggestions.first()) }
    var place by remember { mutableStateOf(space.places.firstOrNull() ?: "") }
    var quorum by remember { mutableIntStateOf(3) }
    var maxCap by remember { mutableStateOf<Int?>(null) }
    var isCapActive by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "New pulse", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Haze)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // What chips
            Text(text = "What", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(whatSuggestions) { suggestion ->
                    val isSel = title == suggestion
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) Moonlight else Wall,
                        modifier = Modifier.clickable { title = suggestion }
                    ) {
                        Text(
                            text = suggestion,
                            color = if (isSel) Night else Moonlight,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 40) title = it },
                label = { Text("Title", color = Haze) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Moonlight,
                    unfocusedBorderColor = Mullion,
                    focusedTextColor = Moonlight,
                    unfocusedTextColor = Moonlight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Where
            Text(text = "Where", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                placeholder = { Text("e.g. Bartlett, Promontory Point", color = Dusk) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Moonlight,
                    unfocusedBorderColor = Mullion,
                    focusedTextColor = Moonlight,
                    unfocusedTextColor = Moonlight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Who (Circle selector)
            Text(text = "Who", style = MaterialTheme.typography.labelSmall, color = Haze)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(spaceCircles) { circle ->
                    val isSel = selectedCircle?.id == circle.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) Moonlight else Wall,
                        modifier = Modifier.clickable { selectedCircle = circle }
                    ) {
                        Text(
                            text = "${circle.name} (${circle.memberCount})",
                            color = if (isSel) Night else Moonlight,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quorum (Need) & Cap (Max) steppers
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Need stepper
                Column {
                    Text(text = "Need (quorum)", style = MaterialTheme.typography.labelSmall, color = Haze)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SyncOutlinedButton(text = "–", onClick = { if (quorum > 2) quorum-- })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$quorum",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = Moonlight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SyncOutlinedButton(text = "+", onClick = { quorum++ })
                    }
                }

                // Max Cap stepper
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Max cap", style = MaterialTheme.typography.labelSmall, color = Haze)
                        Spacer(modifier = Modifier.width(6.dp))
                        Checkbox(
                            checked = isCapActive,
                            onCheckedChange = {
                                isCapActive = it
                                maxCap = if (it) maxOf(quorum, 6) else null
                            }
                        )
                    }
                    if (isCapActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SyncOutlinedButton(text = "–", onClick = { if ((maxCap ?: quorum) > quorum) maxCap = (maxCap ?: quorum) - 1 })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${maxCap ?: quorum}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 24.sp,
                                color = Moonlight
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SyncOutlinedButton(text = "+", onClick = { maxCap = (maxCap ?: quorum) + 1 })
                        }
                    } else {
                        Text(text = "Off", style = MaterialTheme.typography.bodyMedium, color = Dusk)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Serif live preview (Section 10.4)
            Surface(
                color = Wall,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "$title at ${prefillTime ?: "Now"}. Need $quorum.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = Moonlight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Decide by in 40 minutes. Disappears quietly if unfilled.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Haze
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Send pulse filled button
            SyncButton(
                text = "Send pulse",
                onClick = {
                    val c = selectedCircle ?: spaceCircles.firstOrNull()
                    if (c != null) {
                        onSendPulse(c.id, c.name, title, place, quorum, if (isCapActive) maxCap else null)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PulseDetailDialog(
    pulse: SyncPulse,
    onDismiss: () -> Unit,
    onToggleIn: (Boolean) -> Unit,
    onCancelPulse: () -> Unit
) {
    val isConfirmed = pulse.state == "CONFIRMED"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isConfirmed) Amber.copy(alpha = 0.6f) else Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pulse.circleName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Haze
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Text(
                    text = pulse.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Moonlight
                )

                // Large serif time
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "12:30",
                    fontFamily = FontFamily.Serif,
                    fontSize = 36.sp,
                    color = Moonlight
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pulse.place} · by ${pulse.creatorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status banner: "It's on. 4 of 4." or "3 of 4 in · decide by 12:10"
                if (isConfirmed) {
                    Surface(
                        color = WallRaised,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Amber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "It's on. ${pulse.membersIn.size} of ${pulse.quorum} in",
                                style = MaterialTheme.typography.titleMedium,
                                color = Amber
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${pulse.membersIn.size} of ${pulse.quorum} in · decide by 12:10",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Who's in", style = MaterialTheme.typography.labelSmall, color = Haze)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pulse.membersIn.forEach { m ->
                        SyncWindow(
                            family = if (isConfirmed) StatusFamily.OPEN else m.family,
                            initials = m.initials,
                            toneName = m.tone,
                            size = WindowSize.DEFAULT
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (pulse.isUserIn) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "You're in",
                            style = MaterialTheme.typography.titleMedium,
                            color = Moonlight
                        )
                        Text(
                            text = "Can't make it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Signal,
                            modifier = Modifier.clickable { onToggleIn(false) }
                        )
                    }
                } else {
                    SyncButton(
                        text = "I'm In",
                        onClick = { onToggleIn(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (pulse.creatorId == "user_arnav") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cancel pulse",
                        style = MaterialTheme.typography.labelMedium,
                        color = Signal,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { onCancelPulse(); onDismiss() }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonSheet(
    person: CircleMember,
    onDismiss: () -> Unit,
    onAskToMeet: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SyncWindow(
                            family = person.family,
                            initials = person.initials,
                            toneName = person.tone,
                            size = WindowSize.LARGE
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = person.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = Moonlight
                            )
                            if (!person.department.isNullOrBlank()) {
                                Text(
                                    text = person.department,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Haze
                                )
                            }
                            if (person.localTimeStr.isNotBlank()) {
                                Text(
                                    text = person.localTimeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Dusk
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current status
                Surface(
                    color = WallRaised,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Status", style = MaterialTheme.typography.labelSmall, color = Dusk)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = person.statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (person.family == StatusFamily.OPEN) Amber else Moonlight
                        )
                        if (person.place.isNotBlank()) {
                            Text(text = person.place, style = MaterialTheme.typography.bodyMedium, color = Haze)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (person.sharedCircles.isNotEmpty()) {
                    Text(
                        text = "Shared circles: ${person.sharedCircles.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Haze
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SyncButton(
                    text = "Ask to meet",
                    onClick = {
                        onDismiss()
                        onAskToMeet()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
