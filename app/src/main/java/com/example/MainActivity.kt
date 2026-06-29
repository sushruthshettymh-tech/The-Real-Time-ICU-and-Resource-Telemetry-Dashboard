package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.ManagementScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HospitalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent()
            }
        }
    }
}

enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Control Center", Icons.Default.Dashboard, "tab_dashboard"),
    MANAGEMENT("Log Assets", Icons.Default.EditNote, "tab_management"),
    LOGS("Audit Trails", Icons.Default.ReceiptLong, "tab_logs"),
    AI_ADVISOR("AI Diagnostics", Icons.Default.AutoAwesome, "tab_ai_advisor")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: HospitalViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    // Collect reactive Room states
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MEDPULSE CONSOLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Quick Reset Action button
                    IconButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.testTag("app_quick_reset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Database",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag(screen.tag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        resources = resources,
                        onUpdateResource = { id, amount, reason ->
                            viewModel.updateResource(id, amount, reason)
                        }
                    )
                }
                Screen.MANAGEMENT -> {
                    ManagementScreen(
                        resources = resources,
                        onUpdateResource = { id, amount, reason ->
                            viewModel.updateResource(id, amount, reason)
                        },
                        onSaveNewCapacity = { id, newCapacity ->
                            viewModel.saveNewCapacity(id, newCapacity)
                        }
                    )
                }
                Screen.LOGS -> {
                    LogsScreen(
                        logs = logs,
                        onClearLogs = { viewModel.clearHistory() },
                        onResetDatabase = { viewModel.resetToDefaults() }
                    )
                }
                Screen.AI_ADVISOR -> {
                    AiAdvisorScreen(
                        aiResponse = aiResponse,
                        isLoading = isAiLoading,
                        onRunAudit = { viewModel.runAiAnalysis() },
                        onAskQuestion = { question -> viewModel.askAiAdvisor(question) }
                    )
                }
            }
        }
    }
}
