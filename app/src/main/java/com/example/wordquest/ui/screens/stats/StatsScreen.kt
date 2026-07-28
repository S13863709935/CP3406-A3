package com.example.wordquest.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.statsList

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress") },
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
            // Daily Goal Progress
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Daily Goal Progress", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (uiState.dailyGoal > 0) {
                        uiState.learnedToday.toFloat() / uiState.dailyGoal
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.learnedToday} / ${uiState.dailyGoal} words learned today",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (stats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No stats available yet. Start learning!")
                }
            } else {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Quizzes", style = MaterialTheme.typography.bodySmall)
                                Text(text = "${stats.size}", style = MaterialTheme.typography.headlineSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val totalScore = stats.sumOf { it.score }
                                val totalQuestions = stats.sumOf { it.totalQuestions }
                                val avgAccuracy = if (totalQuestions > 0) (totalScore.toDouble() / totalQuestions * 100).toInt() else 0
                                Text(text = "Avg. Accuracy", style = MaterialTheme.typography.bodySmall)
                                Text(text = "$avgAccuracy%", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Accuracy Trend", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            stats.takeLast(7).forEach { stat ->
                                val accuracy = if (stat.totalQuestions > 0) (stat.score.toFloat() / stat.totalQuestions) else 0f
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .fillMaxHeight(accuracy.coerceAtLeast(0.1f))
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "History", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(stats.reversed()) { stat ->
                        ListItem(
                            headlineContent = { Text(stat.date) },
                            supportingContent = { Text("Score: ${stat.score}/${stat.totalQuestions}") },
                            trailingContent = {
                                val accuracy = if (stat.totalQuestions > 0) (stat.score.toDouble() / stat.totalQuestions * 100).toInt() else 0
                                Text(text = "$accuracy%", fontWeight = FontWeight.Bold)
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}
