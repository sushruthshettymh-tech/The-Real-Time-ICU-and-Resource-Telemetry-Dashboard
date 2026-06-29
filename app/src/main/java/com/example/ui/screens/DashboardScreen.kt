package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HospitalResource
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning

@Composable
fun DashboardScreen(
    resources: List<HospitalResource>,
    onUpdateResource: (String, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header Card
        item {
            HeaderCard()
        }

        // Live status banner
        item {
            OverallStatusBanner(resources)
        }

        // Resource Monitoring Grid/List
        item {
            Text(
                text = "Resource Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (resources.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Loading",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Initializing Hospital Database...")
                        }
                    }
                }
            }
        } else {
            items(resources, key = { it.id }) { resource ->
                ResourceTelemetryCard(
                    resource = resource,
                    onUpdate = { amount, reason -> onUpdateResource(resource.id, amount, reason) }
                )
            }
        }

        // Rapid Simulation Controls for Demos
        item {
            SimulationControlsCard(onUpdateResource)
        }
    }
}

@Composable
fun HeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "St. Jude Medical Center",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resource Monitor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time critical asset, telemetry, and capacity diagnostics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun OverallStatusBanner(resources: List<HospitalResource>) {
    if (resources.isEmpty()) return

    // Calculate overall status based on utilization
    val criticalCount = resources.count { 
        val ratio = it.currentValue.toFloat() / it.maxValue
        if (it.id == "oxygen_supply" || it.id == "ppe_kits") {
            ratio < 0.25f // Supply running out is critical
        } else {
            ratio > 0.85f // Beds/Ventilators full is critical
        }
    }

    val warningCount = resources.count {
        val ratio = it.currentValue.toFloat() / it.maxValue
        if (it.id == "oxygen_supply" || it.id == "ppe_kits") {
            ratio in 0.25f..0.45f
        } else {
            ratio in 0.70f..0.85f
        }
    }

    val bannerColor: Color
    val statusText: String
    val bannerIcon: ImageVector
    val textColor: Color

    when {
        criticalCount > 0 -> {
            bannerColor = StatusCritical.copy(alpha = 0.15f)
            textColor = StatusCritical
            statusText = "CRITICAL: $criticalCount resources require immediate clinical attention!"
            bannerIcon = Icons.Default.Warning
        }
        warningCount > 0 -> {
            bannerColor = StatusWarning.copy(alpha = 0.15f)
            textColor = Color(0xFFD68A00)
            statusText = "WARNING: $warningCount resources near maximum clinical load threshold."
            bannerIcon = Icons.Default.ErrorOutline
        }
        else -> {
            bannerColor = StatusSafe.copy(alpha = 0.15f)
            textColor = StatusSafe
            statusText = "HEALTHY: All medical resources operating in safe tolerances."
            bannerIcon = Icons.Default.CheckCircle
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = bannerIcon,
                contentDescription = "Status Icon",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun ResourceTelemetryCard(
    resource: HospitalResource,
    onUpdate: (Int, String) -> Unit
) {
    val utilizationRatio = resource.currentValue.toFloat() / resource.maxValue
    
    // Determine clinical safety status
    val isSupply = resource.id == "oxygen_supply" || resource.id == "ppe_kits"
    val isCritical = if (isSupply) utilizationRatio < 0.25f else utilizationRatio > 0.85f
    val isWarning = if (isSupply) utilizationRatio in 0.25f..0.45f else utilizationRatio in 0.70f..0.85f

    val statusColor = when {
        isCritical -> StatusCritical
        isWarning -> StatusWarning
        else -> StatusSafe
    }

    val statusLabel = when {
        isCritical -> "CRITICAL"
        isWarning -> "WARNING"
        else -> "STABLE"
    }

    val icon = when (resource.id) {
        "icu_beds" -> Icons.Default.Hotel
        "ventilators" -> Icons.Default.Air
        "oxygen_supply" -> Icons.Default.Opacity
        "emergency_beds" -> Icons.Default.LocalHospital
        else -> Icons.Default.Inventory
    }

    // Animate ratio for fluid loading bar
    val progressAnimated by animateFloatAsState(targetValue = utilizationRatio.coerceIn(0f, 1f), label = "Progress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resource_card_${resource.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = resource.name,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = resource.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Alert Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWarning && statusColor == StatusWarning) Color(0xFFC08A00) else statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resource Capacity Stat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Allocation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%,d", resource.currentValue),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / ${String.format("%,d", resource.maxValue)} ${resource.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }
                
                // Utilization Percentage
                Text(
                    text = "${(utilizationRatio * 100).toInt()}% Used",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Progress Indicator
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Inline quick adjustments for easy demo triggers
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Adjust",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val step = if (resource.id == "oxygen_supply") 500 else 1
                    
                    // Outflow (use/consume)
                    FilledTonalIconButton(
                        onClick = { 
                            if (isSupply) {
                                onUpdate(-step, "Consumed $step ${resource.unit} of oxygen during telemetry monitoring.")
                            } else {
                                onUpdate(step, "Admitted patient. Occupied 1 ICU Bed/Ventilator.")
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("adjust_down_${resource.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isSupply) Icons.Default.Remove else Icons.Default.Add,
                            contentDescription = "Increase Use",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Inflow (release/refill)
                    FilledTonalIconButton(
                        onClick = { 
                            if (isSupply) {
                                onUpdate(step, "Refilled $step ${resource.unit} of oxygen supply.")
                            } else {
                                onUpdate(-step, "Patient discharged/stabilized. Released 1 ICU Bed/Ventilator.")
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("adjust_up_${resource.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isSupply) Icons.Default.Add else Icons.Default.Remove,
                            contentDescription = "Decrease Use",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationControlsCard(
    onUpdateResource: (String, Int, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simulation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "Simulation",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scenario Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Simulate batch medical scenarios to test telemetry alerts and AI responses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row 1
            Button(
                onClick = {
                    onUpdateResource("icu_beds", 10, "MASS CASUALTY SURGE: Multiple emergency trauma admissions triggered.")
                    onUpdateResource("ventilators", 4, "MASS CASUALTY SURGE: Heavy critical care respiratory demand.")
                    onUpdateResource("emergency_beds", 8, "MASS CASUALTY SURGE: ER triaged to maximum capacity.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sim_surge_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Surge", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Mass Casualty Surge (+Load)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onUpdateResource("oxygen_supply", 3000, "SUPPLY LOGISTICS: Bulk cryogenic liquid oxygen tanker delivery complete.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_refill_oxygen"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSafe)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = "Refill", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refill Oxygen", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onUpdateResource("oxygen_supply", -2500, "TECHNICAL ALERT: Detected trace pressure valve oxygen leakage.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_leak_oxygen"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCritical)
                ) {
                    Icon(Icons.Default.GasMeter, contentDescription = "Leak", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Oxygen Leak", fontSize = 12.sp)
                }
            }
        }
    }
}
