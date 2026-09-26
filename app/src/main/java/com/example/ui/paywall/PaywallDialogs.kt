package com.example.ui.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.data.model.StatusFamily
import com.example.ui.components.SyncButton
import com.example.ui.components.SyncOutlinedButton
import com.example.ui.components.SyncWindow
import com.example.ui.components.WindowSize
import com.example.ui.theme.*

@Composable
fun ProPaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("annual") } // "monthly" or "annual"
    var isPurchasing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header with close
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Small lit windows brand mark
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SyncWindow(StatusFamily.OPEN, "S", "Rose", WindowSize.ROLLUP)
                        SyncWindow(StatusFamily.AWAY, "Y", "Sky", WindowSize.ROLLUP)
                        SyncWindow(StatusFamily.FOCUSED, "N", "Sage", WindowSize.ROLLUP)
                        SyncWindow(StatusFamily.BUSY, "C", "Teal", WindowSize.ROLLUP)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The exact copy from Section 18.9
                Text(
                    text = "This circle is full at 12.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Moonlight
                )
                Text(
                    text = "Pro makes room for everyone.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    color = Amber
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Benefits
                val features = listOf(
                    "Unlimited members in circles you own",
                    "Unlimited circles",
                    "Weekly rituals (auto-opening pulses)",
                    "Custom status words for your spaces"
                )
                features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feat, style = MaterialTheme.typography.bodyMedium, color = Moonlight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plans selection
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    // Annual with trial
                    Surface(
                        color = if (selectedPlan == "annual") WallRaised else Wall,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selectedPlan == "annual") Moonlight else Mullion),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = "annual" }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "7-day trial", style = MaterialTheme.typography.labelSmall, color = Amber)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$19.99 / yr", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = Moonlight)
                            Text(text = "$1.66 / mo", style = MaterialTheme.typography.labelSmall, color = Haze)
                        }
                    }

                    // Monthly
                    Surface(
                        color = if (selectedPlan == "monthly") WallRaised else Wall,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selectedPlan == "monthly") Moonlight else Mullion),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = "monthly" }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Monthly", style = MaterialTheme.typography.labelSmall, color = Haze)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$2.99 / mo", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = Moonlight)
                            Text(text = "Cancel anytime", style = MaterialTheme.typography.labelSmall, color = Dusk)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // The one filled button (Test Store simulated purchase)
                SyncButton(
                    text = if (isPurchasing) "Processing..." else "Start 7-day free trial",
                    onClick = {
                        isPurchasing = true
                        onPurchaseSuccess()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Restore purchases · RevenueCat Test Store",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                }
            }
        }
    }
}

@Composable
fun WorkplaceUpgradeDialog(
    seatLimit: Int,
    seatsUsed: Int,
    planTier: String,
    onDismiss: () -> Unit,
    onUpgradeSeats: (newLimit: Int, newTier: String) -> Unit
) {
    var selectedTier by remember { mutableStateOf("Business 250") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Wall,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Mullion),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Plan · Kestrel Labs", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seats used in large serif (Section 14.7)
                Text(
                    text = "$seatsUsed of $seatLimit seats used",
                    fontFamily = FontFamily.Serif,
                    fontSize = 32.sp,
                    color = Moonlight
                )
                Text(
                    text = "$planTier renews Sep 29, 2027",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tier ladder
                val tiers = listOf(
                    Triple("Team 25", "$299 a year", "Too small"),
                    Triple("Business 100", "$999 a year", if (seatLimit == 100) "Current" else ""),
                    Triple("Business 250", "$2,199 a year", if (seatLimit == 250) "Current" else "Recommended"),
                    Triple("Business 500", "$3,999 a year", "")
                )

                tiers.forEach { (name, price, tag) ->
                    val isCurrent = name == planTier
                    val isTooSmall = tag == "Too small"
                    val isSelected = selectedTier == name

                    Surface(
                        color = if (isSelected) WallRaised else Wall,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSelected) Moonlight else Mullion),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTooSmall && !isCurrent) { selectedTier = name }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isTooSmall) Dusk else Moonlight,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = price, style = MaterialTheme.typography.labelSmall, color = if (isTooSmall) Dusk else Haze)
                            }
                            if (tag.isNotBlank()) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (tag == "Recommended") Amber else Dusk,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SyncButton(
                    text = "Move to 250 seats",
                    onClick = {
                        onUpgradeSeats(250, "Business 250")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Restore purchases · Manage subscription",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                }
            }
        }
    }
}
