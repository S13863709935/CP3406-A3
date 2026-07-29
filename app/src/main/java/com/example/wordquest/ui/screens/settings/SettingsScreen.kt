package com.example.wordquest.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ListItem(
                headlineContent = { Text("Dark Mode") },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Daily Goal") },
                supportingContent = { Text("${uiState.dailyGoal} words per day") },
                trailingContent = {
                    Slider(
                        value = uiState.dailyGoal.toFloat(),
                        onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                        valueRange = SettingsManager.MIN_DAILY_GOAL.toFloat()..
                            SettingsManager.MAX_DAILY_GOAL.toFloat(),
                        steps = 8,
                        modifier = Modifier.width(150.dp)
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Quiz Mode") },
                supportingContent = {
                    Text(
                        if (uiState.quizMode == QuizMode.FLASHCARD) {
                            "Flashcard"
                        } else {
                            "Multiple Choice"
                        }
                    )
                },
                trailingContent = {
                    Row {
                        FilterChip(
                            selected = uiState.quizMode == QuizMode.FLASHCARD,
                            onClick = { viewModel.updateQuizMode(QuizMode.FLASHCARD) },
                            label = { Text("Flashcard") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.quizMode == QuizMode.MULTIPLE_CHOICE,
                            onClick = { viewModel.updateQuizMode(QuizMode.MULTIPLE_CHOICE) },
                            label = { Text("Quiz") }
                        )
                    }
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Notifications") },
                trailingContent = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
