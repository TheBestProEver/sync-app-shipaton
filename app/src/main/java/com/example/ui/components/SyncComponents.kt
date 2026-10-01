package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpaceType
import com.example.data.model.StatusFamily
import com.example.ui.theme.*

enum class WindowSize(val width: Dp, val height: Dp, val fontSize: Int) {
    ROLLUP(20.dp, 26.dp, 9),
    DEFAULT(28.dp, 36.dp, 11),
    LARGE(44.dp, 56.dp, 15)
}

@Composable
fun SyncWindow(
    family: StatusFamily,
    initials: String,
    toneName: String,
    size: WindowSize = WindowSize.DEFAULT,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val sillColor = getToneColor(toneName)
    val windowShape = RoundedCornerShape(4.dp)

    // Breathing glow animation when OPEN
    val infiniteTransition = rememberInfiniteTransition(label = "window_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val targetBgColor = when (family) {
        StatusFamily.OPEN -> Amber.copy(alpha = glowAlpha)
        StatusFamily.FOCUSED, StatusFamily.BUSY -> Wall
        StatusFamily.IN_OFFICE -> Moonlight.copy(alpha = 0.12f)
        StatusFamily.AWAY, StatusFamily.GHOST -> Color.Transparent
    }
    val animatedBg by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(200),
        label = "window_bg"
    )

    val textCol = when (family) {
        StatusFamily.OPEN -> Night
        StatusFamily.FOCUSED -> Moonlight
        StatusFamily.BUSY -> Haze
        StatusFamily.IN_OFFICE -> Moonlight
        StatusFamily.AWAY, StatusFamily.GHOST -> Dusk
    }

    val accessibilityDesc = "$initials. ${family.name.lowercase().replaceFirstChar { it.uppercase() }}"

    Box(
        modifier = modifier
            .size(size.width, size.height + 2.dp)
            .semantics { contentDescription = accessibilityDesc }
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        // Window Body
        Box(
            modifier = Modifier
                .size(size.width, size.height)
                .clip(windowShape)
                .background(animatedBg)
                .then(
                    when (family) {
                        StatusFamily.AWAY -> Modifier.border(1.dp, Dusk, windowShape)
                        StatusFamily.IN_OFFICE -> Modifier.border(1.dp, Moonlight, windowShape)
                        StatusFamily.GHOST -> Modifier.drawBehind {
                            drawRoundRect(
                                color = Dusk,
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            )
                        }
                        else -> Modifier
                    }
                )
                .then(
                    // If Focused: draw 3 horizontal blinds stripes in Blinds color (#6C7FA8)
                    if (family == StatusFamily.FOCUSED) {
                        Modifier.drawBehind {
                            val stripeH = size.height.toPx() / 4
                            drawLine(Blinds, Offset(2f, stripeH), Offset(size.width.toPx() - 2f, stripeH), strokeWidth = 1.5.dp.toPx())
                            drawLine(Blinds, Offset(2f, stripeH * 2), Offset(size.width.toPx() - 2f, stripeH * 2), strokeWidth = 1.5.dp.toPx())
                            drawLine(Blinds, Offset(2f, stripeH * 3), Offset(size.width.toPx() - 2f, stripeH * 3), strokeWidth = 1.5.dp.toPx())
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = textCol,
                fontSize = size.fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // 2px Sill under the window
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(sillColor)
        )
    }
}

@Composable
fun TypeBadge(
    type: SpaceType,
    modifier: Modifier = Modifier
) {
    val isWorkplace = type == SpaceType.WORKPLACE
    val badgeText = if (isWorkplace) "Workplace · Business" else "Campus Community"
    val borderCol = if (isWorkplace) WorkplaceSteel.copy(alpha = 0.6f) else Amber.copy(alpha = 0.4f)
    val bgCol = if (isWorkplace) WorkplaceCard else WallRaised.copy(alpha = 0.6f)
    val textCol = if (isWorkplace) WorkplaceSteel else Amber

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgCol,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            if (isWorkplace) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = WorkplaceSteel,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = badgeText,
                color = textCol,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatusChip(
    statusText: String,
    family: StatusFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WallRaised,
        border = androidx.compose.foundation.BorderStroke(1.dp, Mullion),
        modifier = modifier
            .clickable { onClick() }
            .defaultMinSize(minHeight = 36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (family == StatusFamily.OPEN) Amber else Haze)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (statusText.isNotBlank()) statusText else "Set a status",
                color = Moonlight,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Change status",
                tint = Haze,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SpaceHeader(
    spaceName: String,
    spaceType: SpaceType,
    statusText: String,
    statusFamily: StatusFamily,
    onSpaceClick: () -> Unit,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWorkplace = spaceType == SpaceType.WORKPLACE
    val headerBg = if (isWorkplace) WorkplaceNavy else Night

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(headerBg)
            .statusBarsPadding() // Ensures content sits strictly below battery, clock, and camera notch
    ) {
        // Generous negative space between phone UI / status bar and header content
        Spacer(modifier = Modifier.height(6.dp))

        // Ambient accent top stripe for workplace / community
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    if (isWorkplace)
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(WorkplaceSteel.copy(alpha = 0.9f), WorkplaceBorder, WorkplaceNavy)
                        )
                    else
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Amber.copy(alpha = 0.9f), ToneRose.copy(alpha = 0.7f), Night)
                        )
                )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onSpaceClick() }
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spaceName,
                                color = Moonlight,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch space",
                                tint = if (isWorkplace) WorkplaceSteel else Haze,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypeBadge(type = spaceType)
                            if (isWorkplace) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "78 / 100 seats",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WorkplaceSteel.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                StatusChip(
                    statusText = statusText,
                    family = statusFamily,
                    onClick = onStatusClick
                )
            }
        }
        Divider(
            color = if (isWorkplace) WorkplaceBorder.copy(alpha = 0.7f) else Mullion.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

// Single Filled Button (Main action - Moonlight fill, Night text)
@Composable
fun SyncButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Moonlight,
            contentColor = Night,
            disabledContainerColor = Mullion,
            disabledContentColor = Dusk
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) Night else Dusk,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Outlined Button (Secondary action - 1px Haze border, Moonlight text)
@Composable
fun SyncOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) Haze else Mullion),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) Moonlight else Dusk,
            disabledContentColor = Dusk
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier.defaultMinSize(minHeight = 44.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) Moonlight else Dusk,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// Google Sign-In with Credential Manager button
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Sign in with Google"
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WallRaised,
        border = androidx.compose.foundation.BorderStroke(1.dp, Mullion),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Google 'G' 4-color stylized dot badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = Night,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                color = Moonlight,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Atmospheric night building facade banner
@Composable
fun NightFacadeHeader(
    freeCount: Int,
    totalCount: Int,
    isWorkplace: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isWorkplace) WorkplaceCard else Wall,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isWorkplace) WorkplaceBorder else Mullion),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isWorkplace) WorkplaceSteel else Amber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isWorkplace) "Live Workplace Atmosphere" else "Live Campus Windows",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWorkplace) WorkplaceSteel else Amber,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Mini animated facade
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    SyncWindow(StatusFamily.OPEN, "S", "Rose", WindowSize.ROLLUP)
                    SyncWindow(StatusFamily.OPEN, "M", "Sky", WindowSize.ROLLUP)
                    SyncWindow(if (isWorkplace) StatusFamily.FOCUSED else StatusFamily.OPEN, "E", "Sage", WindowSize.ROLLUP)
                    SyncWindow(if (isWorkplace) StatusFamily.IN_OFFICE else StatusFamily.AWAY, "L", "Teal", WindowSize.ROLLUP)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isWorkplace)
                    "$freeCount colleagues available · 7 in focus"
                else
                    "$freeCount friends free nearby right now",
                style = MaterialTheme.typography.titleMedium,
                color = Moonlight,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isWorkplace)
                    "Pier 57: Design & Core Mobile clusters active"
                else
                    "Clusters at The Regenstein Library & Ex Libris Café",
                style = MaterialTheme.typography.bodyMedium,
                color = Haze
            )
        }
    }
}
