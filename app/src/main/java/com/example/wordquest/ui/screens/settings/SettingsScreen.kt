package com.example.wordquest.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var goalSliderValue by remember(uiState.dailyGoal) {
        mutableFloatStateOf(uiState.dailyGoal.toFloat())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SettingsSectionTitle("Appearance")
            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Use a darker color theme") },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.toggleable(
                    value = uiState.isDarkMode,
                    role = Role.Switch,
                    onValueChange = viewModel::toggleDarkMode
                )
            )

            HorizontalDivider()
            SettingsSectionTitle("Learning")
            Text(
                text = "Daily Goal",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${goalSliderValue.roundToInt()} words per day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = goalSliderValue,
                onValueChange = { goalSliderValue = it },
                onValueChangeFinished = {
                    viewModel.updateDailyGoal(goalSliderValue.roundToInt())
                },
                valueRange = SettingsManager.MIN_DAILY_GOAL.toFloat()..
                    SettingsManager.MAX_DAILY_GOAL.toFloat(),
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Quiz Mode",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Choose how definitions are reviewed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.quizMode == QuizMode.FLASHCARD,
                    onClick = { viewModel.updateQuizMode(QuizMode.FLASHCARD) },
                    label = { Text("Flashcard") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.quizMode == QuizMode.MULTIPLE_CHOICE,
                    onClick = { viewModel.updateQuizMode(QuizMode.MULTIPLE_CHOICE) },
                    label = { Text("Multiple Choice") },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            SettingsSectionTitle("Reminders")
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Allow learning reminder preferences") },
                trailingContent = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.toggleable(
                    value = uiState.notificationsEnabled,
                    role = Role.Switch,
                    onValueChange = viewModel::toggleNotifications
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "WordQuest 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}
