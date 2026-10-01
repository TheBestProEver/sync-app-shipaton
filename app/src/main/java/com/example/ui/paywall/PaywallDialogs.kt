package com.example.ui.paywall

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StatusFamily
import com.example.service.RevenueCatManager
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
    val context = LocalContext.current
    val proOffering by RevenueCatManager.proOffering.collectAsStateWithLifecycle()
    var selectedPlan by remember { mutableStateOf("annual") } // "monthly" or "annual"
    var isPurchasing by remember { mutableStateOf(false) }

    // Read packages and prices dynamically from RevenueCat offering
    val annualPkg = proOffering?.annual ?: proOffering?.availablePackages?.firstOrNull {
        it.identifier.contains("annual", ignoreCase = true) || it.identifier.contains("year", ignoreCase = true)
    }
    val monthlyPkg = proOffering?.monthly ?: proOffering?.availablePackages?.firstOrNull {
        it.identifier.contains("monthly", ignoreCase = true)
    }

    val annualPrice = annualPkg?.product?.price?.formatted?.let { "$it / yr" } ?: "$19.99 / yr"
    val monthlyPrice = monthlyPkg?.product?.price?.formatted?.let { "$it / mo" } ?: "$2.99 / mo"

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

                // Plans selection from dynamic RevenueCat offerings
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
                            Text(text = annualPrice, fontFamily = FontFamily.Serif, fontSize = 20.sp, color = Moonlight)
                            Text(text = "Best value", style = MaterialTheme.typography.labelSmall, color = Haze)
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
                            Text(text = monthlyPrice, fontFamily = FontFamily.Serif, fontSize = 20.sp, color = Moonlight)
                            Text(text = "Cancel anytime", style = MaterialTheme.typography.labelSmall, color = Dusk)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // The one filled button (Unlocks Pro immediately)
                SyncButton(
                    text = if (isPurchasing) "Unlocking..." else if (selectedPlan == "annual") "Start 7-day free trial" else "Subscribe to Pro",
                    onClick = {
                        isPurchasing = true
                        RevenueCatManager.unlockProImmediately()
                        onPurchaseSuccess()
                        Toast.makeText(context, "Community Pro unlocked!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RevenueCat Offerings",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                    Text(
                        text = "Restore purchases",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            RevenueCatManager.restorePurchases { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    onPurchaseSuccess()
                                }
                            }
                        }
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
    val context = LocalContext.current
    val workplaceTiers by RevenueCatManager.workplaceTiers.collectAsStateWithLifecycle()
    var selectedTierId by remember { mutableStateOf("") }

    val currentSelectedTier = workplaceTiers.firstOrNull { it.id == selectedTierId }
        ?: workplaceTiers.firstOrNull { it.seats > seatsUsed }
        ?: workplaceTiers.lastOrNull()

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
                    Text(text = "Plan · Workplace Admins", style = MaterialTheme.typography.titleLarge, color = Moonlight)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Haze)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seats used in large serif (Section 14.7)
                Text(
                    text = "$seatsUsed of $seatLimit seats used",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    color = Moonlight
                )
                Text(
                    text = "$planTier · RevenueCat workplace offering",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Haze
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tier ladder from RevenueCat workplace offering
                workplaceTiers.forEach { tier ->
                    val isCurrent = tier.seats == seatLimit
                    val isTooSmall = tier.seats < seatsUsed
                    val isSelected = currentSelectedTier?.id == tier.id

                    val tag = when {
                        isCurrent -> "Current"
                        isTooSmall -> "Too small ($seatsUsed used)"
                        tier.seats == 250 -> "Recommended"
                        else -> "${tier.seats} seats"
                    }

                    Surface(
                        color = if (isSelected) WallRaised else Wall,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSelected) Moonlight else Mullion),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTooSmall && !isCurrent) { selectedTierId = tier.id }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = tier.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isTooSmall) Dusk else Moonlight,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tier.formattedPrice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isTooSmall) Dusk else Haze
                                )
                            }
                            if (tag.isNotBlank()) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (tag == "Recommended") Amber else if (isCurrent) WorkplaceSteel else Dusk,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val buttonTier = currentSelectedTier
                val buttonText = if (buttonTier != null && buttonTier.seats != seatLimit) {
                    "Move to ${buttonTier.seats} seats (${buttonTier.name})"
                } else {
                    "Keep current plan"
                }

                SyncButton(
                    text = buttonText,
                    enabled = buttonTier != null && buttonTier.seats != seatLimit && buttonTier.seats >= seatsUsed,
                    onClick = {
                        if (buttonTier != null) {
                            onUpgradeSeats(buttonTier.seats, buttonTier.name)
                            Toast.makeText(context, "Upgraded to ${buttonTier.name}!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Workplace Offering",
                        style = MaterialTheme.typography.labelSmall,
                        color = Dusk
                    )
                    Text(
                        text = "Restore purchases",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            RevenueCatManager.restorePurchases { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
