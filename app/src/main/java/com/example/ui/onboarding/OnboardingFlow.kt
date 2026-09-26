package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Verified
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
import com.example.data.model.StatusFamily
import com.example.ui.components.SyncButton
import com.example.ui.components.SyncOutlinedButton
import com.example.ui.components.SyncWindow
import com.example.ui.components.WindowSize
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingFlow(
    onComplete: (name: String, tone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) } // 0 = Welcome, 1 = Email, 2 = Code, 3 = Age, 4 = Profile, 5 = Ready
    var email by remember { mutableStateOf("arnav@uchicago.edu") }
    var code by remember { mutableStateOf("842109") }
    var birthYear by remember { mutableIntStateOf(2007) }
    var displayName by remember { mutableStateOf("Arnav") }
    var windowTone by remember { mutableStateOf("Rose") }

    // Facade animation on Welcome step
    var litCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..4) {
            delay(180)
            litCount = i
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Night)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            0 -> {
                // 1. Welcome Screen (Section 10.1)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 2x2 Facade that lights up
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyncWindow(
                                family = if (litCount >= 1) StatusFamily.OPEN else StatusFamily.AWAY,
                                initials = "S",
                                toneName = "Sky",
                                size = WindowSize.LARGE
                            )
                            SyncWindow(
                                family = if (litCount >= 2) StatusFamily.OPEN else StatusFamily.AWAY,
                                initials = "Y",
                                toneName = "Sage",
                                size = WindowSize.LARGE
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyncWindow(
                                family = if (litCount >= 3) StatusFamily.OPEN else StatusFamily.AWAY,
                                initials = "N",
                                toneName = "Rose",
                                size = WindowSize.LARGE
                            )
                            SyncWindow(
                                family = if (litCount >= 4) StatusFamily.OPEN else StatusFamily.AWAY,
                                initials = "C",
                                toneName = "Lavender",
                                size = WindowSize.LARGE
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "SYNC",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        color = Moonlight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "See who's free. Go meet them.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = Moonlight,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Turns overlapping free time into plans without a chat thread.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    SyncButton(
                        text = "Get started",
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "I have an invite code or link",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Haze,
                        modifier = Modifier.clickable { step = 1 }
                    )
                }
            }

            1 -> {
                // Email step
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "What's your email?", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "We'll email you a 6-digit code. No passwords, no magic links.", style = MaterialTheme.typography.bodyMedium, color = Haze)

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address", color = Haze) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Moonlight,
                            unfocusedBorderColor = Mullion,
                            focusedTextColor = Moonlight,
                            unfocusedTextColor = Moonlight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (email.endsWith(".edu")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "School domain qualifies for verified student badge", style = MaterialTheme.typography.labelSmall, color = Amber)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    SyncButton(
                        text = "Continue",
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            2 -> {
                // Code step
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Enter 6-digit code", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Sent to $email", style = MaterialTheme.typography.bodyMedium, color = Haze)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Moonlight,
                            unfocusedBorderColor = Mullion,
                            focusedTextColor = Moonlight,
                            unfocusedTextColor = Moonlight
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Resend in 0:30 · Wrong email? Change it", style = MaterialTheme.typography.labelSmall, color = Dusk)

                    Spacer(modifier = Modifier.height(28.dp))

                    SyncButton(
                        text = "Verify",
                        onClick = { step = 3 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            3 -> {
                // Birth year (Section 10.1)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "When were you born?", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "A year picker, not a full birth date, to collect less data.", style = MaterialTheme.typography.bodyMedium, color = Haze)

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        color = Wall,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Mullion),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Birth Year", style = MaterialTheme.typography.bodyLarge, color = Moonlight)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SyncOutlinedButton(text = "–", onClick = { birthYear-- })
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "$birthYear", fontFamily = FontFamily.Serif, fontSize = 28.sp, color = Moonlight)
                                Spacer(modifier = Modifier.width(12.dp))
                                SyncOutlinedButton(text = "+", onClick = { if (birthYear < 2013) birthYear++ })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "SYNC is for people 13 and older.", style = MaterialTheme.typography.labelSmall, color = Dusk)

                    Spacer(modifier = Modifier.height(28.dp))

                    SyncButton(
                        text = "Continue",
                        onClick = { step = 4 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            4 -> {
                // Name and window tone (Section 10.1)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Your name and window", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Every person is a window. Choose your window sill tone.", style = MaterialTheme.typography.bodyMedium, color = Haze)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Live window preview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SyncWindow(
                            family = StatusFamily.OPEN,
                            initials = displayName.take(2).uppercase().ifBlank { "ME" },
                            toneName = windowTone,
                            size = WindowSize.LARGE
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = displayName.ifBlank { "Your Name" }, style = MaterialTheme.typography.titleLarge, color = Moonlight)
                            Text(text = "Tone: $windowTone", style = MaterialTheme.typography.labelSmall, color = Haze)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Pick tone", style = MaterialTheme.typography.labelSmall, color = Haze)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AllWindowTones) { (toneName, color) ->
                            val isSel = windowTone.equals(toneName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(2.dp, if (isSel) Moonlight else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { windowTone = toneName },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Night, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    SyncButton(
                        text = "Enter SYNC",
                        onClick = { onComplete(displayName, windowTone) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
