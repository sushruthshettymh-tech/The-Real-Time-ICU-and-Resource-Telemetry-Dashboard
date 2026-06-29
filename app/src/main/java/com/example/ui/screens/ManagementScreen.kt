package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HospitalResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    resources: List<HospitalResource>,
    onUpdateResource: (String, Int, String) -> Unit,
    onSaveNewCapacity: (String, Int) -> Unit, // Direct callback to modify capacity
    modifier: Modifier = Modifier
) {
    var selectedResource by remember { mutableStateOf(resources.firstOrNull()) }
    
    // Sync selection when resource list initializes
    LaunchedEffect(resources) {
        if (selectedResource == null && resources.isNotEmpty()) {
            selectedResource = resources.first()
        } else if (selectedResource != null) {
            // keep selection synced
            selectedResource = resources.find { it.id == selectedResource!!.id }
        }
    }

    var adjustmentText by remember { mutableStateOf("") }
    var changeReasonText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Manage Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Asset Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a resource from the list to modify occupied quantities or update standard limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resource Selector list
                    Text(
                        text = "1. SELECT RESOURCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (resources.isEmpty()) {
                        Text(
                            text = "No resources loaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            resources.forEach { resource ->
                                val isSelected = selectedResource?.id == resource.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .clickable { 
                                            selectedResource = resource
                                            adjustmentText = ""
                                            capacityText = resource.maxValue.toString()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Selected",
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = resource.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Text(
                                        text = "${resource.currentValue}/${resource.maxValue} ${resource.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedResource?.let { resource ->
            // Allocation Adjustment Block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. ALLOCATION ADJUSTMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = adjustmentText,
                                onValueChange = { adjustmentText = it },
                                label = { Text("Delta / Change Amount") },
                                placeholder = { Text("e.g. +3 or -5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("adjustment_input_field"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // Quick helper modifiers
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val amount = if (resource.id == "oxygen_supply") 1000 else 5
                                Button(
                                    onClick = { adjustmentText = "+$amount" },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.testTag("quick_plus_btn")
                                ) {
                                    Text("+$amount")
                                }
                                Button(
                                    onClick = { adjustmentText = "-$amount" },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.testTag("quick_minus_btn")
                                ) {
                                    Text("-$amount")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = changeReasonText,
                            onValueChange = { changeReasonText = it },
                            label = { Text("Staff Note / Reason for telemetry change") },
                            placeholder = { Text("e.g., Critical shift rotation bed release") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reason_input_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val cleanText = adjustmentText.replace("+", "").trim()
                                val amount = cleanText.toIntOrNull()
                                if (amount == null) {
                                    snackbarMessage = "Invalid entry. Enter a valid positive or negative integer."
                                    showSnackbar = true
                                } else {
                                    onUpdateResource(resource.id, amount, changeReasonText.trim())
                                    snackbarMessage = "Successfully updated ${resource.name} by $amount."
                                    showSnackbar = true
                                    adjustmentText = ""
                                    changeReasonText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_adjustment_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Commit Adjustment")
                        }
                    }
                }
            }

            // Total Facility Capacity Update Block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. CONFIGURE TOTAL CAPACITY LIMIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Log structural expansions or inventory re-stocks (e.g. buying 10 new ICU beds).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = capacityText,
                                onValueChange = { capacityText = it },
                                label = { Text("Max Structural Capacity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("capacity_input_field"),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val cap = capacityText.toIntOrNull()
                                    if (cap == null || cap <= 0) {
                                        snackbarMessage = "Maximum capacity must be a positive integer."
                                        showSnackbar = true
                                    } else {
                                        onSaveNewCapacity(resource.id, cap)
                                        snackbarMessage = "Updated max limit of ${resource.name} to $cap."
                                        showSnackbar = true
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("submit_capacity_btn")
                                ) {
                                Text("Set Limit")
                            }
                        }
                    }
                }
            }
        }

        // Notification area
        item {
            AnimatedVisibility(visible = showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    }
}
