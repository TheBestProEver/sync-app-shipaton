package com.example.ui.you

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.service.FirebaseAuthService
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun YouScreen(
    userProfile: UserProfile,
    spaces: List<SyncSpace>,
    authService: FirebaseAuthService?,
    calendarSyncStatus: String,
    onUpdateProfile: (name: String, tone: String, timezone: String, workingHours: String) -> Unit,
    onSetGhost: (durationMinutes: Int) -> Unit,
    onOpenProPaywall: () -> Unit,
    onOpenWorkplaceUpgrade: () -> Unit,
    onOpenStatusSheetForSpace: (SyncSpace) -> Unit,
    onSyncCalendar: () -> Unit,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUser = authService?.currentUser?.collectAsStateWithLifecycle()?.value
    val authMessage = authService?.authStatusMessage?.collectAsStateWithLifecycle()?.value ?: ""

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onSyncCalendar() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Night)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Card with signature Window
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SyncWindow(
                                family = StatusFamily.OPEN,
                                initials = userProfile.displayName.take(2).uppercase(),
                                toneName = userProfile.windowTone,
                                size = WindowSize.LARGE
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = userProfile.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Moonlight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified student",
                                        tint = BadgeGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${userProfile.verifiedDomain} verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BadgeGold
                                    )
                                }
                            }
                        }

                        SyncOutlinedButton(
                            text = "Edit",
                            onClick = { showEditProfileDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Mullion, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Time zone: ${userProfile.timezone} · Hours: ${userProfile.workingHours}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Dusk
                    )
                }
            }
        }

        // 2. Firebase & Google Sign-In with Credential Manager
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Authentication & Google Sign-In",
                        style = MaterialTheme.typography.titleMedium,
                        color = Moonlight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Powered by Android Credential Manager and Firebase Auth.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Haze
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentUser != null) {
                        Surface(
                            color = WallRaised,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = currentUser.displayName ?: "Google User",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Moonlight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = currentUser.email ?: "Signed in",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Haze
                                    )
                                }
                                SyncOutlinedButton(
                                    text = "Sign out",
                                    onClick = { authService.signOut() }
                                )
                            }
                        }
                    } else {
                        GoogleSignInButton(
                            buttonText = "Sign in with Google (Credential Manager)",
                            onClick = {
                                coroutineScope.launch {
                                    authService?.signInWithGoogle(context)
                                }
                            }
                        )
                    }

                    if (authMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber
                        )
                    }
                }
            }
        }

        // 3. Calendar & Room Local Persistence
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Amber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Calendar Sync (Room DB)", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                        }
                        SyncOutlinedButton(
                            text = "Sync now",
                            onClick = { calendarLauncher.launch(Manifest.permission.READ_CALENDAR) }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = calendarSyncStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Room caches free/busy slots locally so SYNC can calculate overlaps without ever broadcasting your private event names.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                }
            }
        }

        // 4. My Statuses (one row per space)
        item {
            Text(text = "My statuses across spaces", style = MaterialTheme.typography.titleMedium, color = Moonlight)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    spaces.forEachIndexed { idx, space ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenStatusSheetForSpace(space) }
                                .padding(vertical = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = space.name, style = MaterialTheme.typography.bodyLarge, color = Moonlight)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TypeBadge(type = space.type)
                                }
                                val st = if (space.currentStatusLabel.isNotBlank()) space.currentStatusLabel else "No status set"
                                Text(text = st, style = MaterialTheme.typography.bodyMedium, color = if (space.currentStatusFamily == StatusFamily.OPEN) Amber else Haze)
                            }
                            SyncOutlinedButton(text = "Change", onClick = { onOpenStatusSheetForSpace(space) })
                        }
                        if (idx < spaces.size - 1) {
                            Divider(color = Mullion, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // 5. Privacy & Ghost Mode
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text(text = "Privacy & ghost mode", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                        val ghostText = if (userProfile.ghostUntil != null && userProfile.ghostUntil > System.currentTimeMillis()) {
                            "Ghost mode active"
                        } else {
                            "Share level: Titles · Ghost mode off"
                        }
                        Text(text = ghostText, style = MaterialTheme.typography.bodyMedium, color = Haze)
                    }
                    Text(text = "Manage", style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                }
            }
        }

        // 6. Notifications Rules
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNotificationsDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text(text = "Notifications", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                        Text(text = "Max 3 alerts/day · Quiet: ${userProfile.quietHoursStart} - ${userProfile.quietHoursEnd}", style = MaterialTheme.typography.bodyMedium, color = Haze)
                    }
                    Text(text = "Settings", style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                }
            }
        }

        // 7. Subscription & Upgrades
        item {
            Surface(
                color = Wall,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Mullion),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Subscriptions", style = MaterialTheme.typography.titleMedium, color = Moonlight)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pro section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Community Pro", style = MaterialTheme.typography.bodyLarge, color = Moonlight)
                            Text(
                                text = if (userProfile.isPro) "Active (Unlimited circles & rituals)" else "Free tier (Up to 12 members)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (userProfile.isPro) Amber else Haze
                            )
                        }
                        if (!userProfile.isPro) {
                            SyncButton(text = "Get Pro", onClick = onOpenProPaywall)
                        } else {
                            Text(text = "Active", style = MaterialTheme.typography.bodyMedium, color = Amber)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Mullion, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Workplace plan section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Kestrel Labs (Workplace)", style = MaterialTheme.typography.bodyLarge, color = Moonlight)
                            Text(text = "Business 100 · 100/100 seats used", style = MaterialTheme.typography.bodyMedium, color = Haze)
                        }
                        SyncOutlinedButton(text = "Manage", onClick = onOpenWorkplaceUpgrade)
                    }
                }
            }
        }

        // 8. Footer
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SYNC v1.0 (RevenueCat Shipaton 2026)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Dusk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Presence, not performance. Plans, not threads.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Dusk
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, tone, tz, hours ->
                onUpdateProfile(name, tone, tz, hours)
                showEditProfileDialog = false
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            userProfile = userProfile,
            onDismiss = { showPrivacyDialog = false },
            onSetGhost = { mins ->
                onSetGhost(mins)
                showPrivacyDialog = false
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }
}

@Composable
fun EditProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, tone: String, tz: String, hours: String) -> Unit
) {
    var name by remember { mutableStateOf(userProfile.displayName) }
    var selectedTone by remember { mutableStateOf(userProfile.windowTone) }
    var timezone by remember { mutableStateOf(userProfile.timezone) }
    var workingHours by remember { mutableStateOf(userProfile.workingHours) }

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
                    Text(text = "Edit profile", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SyncWindow(
                        family = StatusFamily.OPEN,
                        initials = name.take(2).uppercase().ifBlank { "AR" },
                        toneName = selectedTone,
                        size = WindowSize.LARGE
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Window tone: $selectedTone\n(2px sill marks who's who)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Pick tone", style = MaterialTheme.typography.labelSmall, color = Haze)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AllWindowTones) { (toneName, color) ->
                        val isSel = selectedTone.equals(toneName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(2.dp, if (isSel) Moonlight else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedTone = toneName },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Night, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display name", color = Haze) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moonlight,
                        unfocusedBorderColor = Mullion,
                        focusedTextColor = Moonlight,
                        unfocusedTextColor = Moonlight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("Working hours", color = Haze) },
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
                    text = "Save changes",
                    onClick = { onSave(name, selectedTone, timezone, workingHours) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PrivacyDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSetGhost: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Privacy & ghost mode", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SYNC never tracks GPS coordinates. Places are typed names. Ghost mode hides you from all circles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Activate Ghost Mode", style = MaterialTheme.typography.labelSmall, color = Haze)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SyncOutlinedButton(text = "1 hour", onClick = { onSetGhost(60) })
                    SyncOutlinedButton(text = "4 hours", onClick = { onSetGhost(240) })
                    SyncOutlinedButton(text = "Tomorrow", onClick = { onSetGhost(1440) })
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (userProfile.ghostUntil != null) {
                    SyncButton(text = "Turn off ghost mode", onClick = { onSetGhost(0) }, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(14.dp))
                SyncOutlinedButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun NotificationsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Notification principles", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                Spacer(modifier = Modifier.height(8.dp))

                val principles = listOf(
                    "At most 3 alerts a day.",
                    "Held during quiet hours (11 pm - 8 am).",
                    "Never alerts when someone becomes free.",
                    "No unread badges, no red dots.",
                    "Workplace focus hours hold pulse alerts."
                )

                principles.forEach { p ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(Amber))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = p, style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                SyncButton(text = "Got it", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
