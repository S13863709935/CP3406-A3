package com.example.wordquest.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wordquest.data.settings.QuizMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Quest") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadNextWord() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.quizFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Quiz Finished!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Your Score: ${uiState.score}/${uiState.totalQuestions}", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                uiState.currentWord != null -> {
                    val word = uiState.currentWord
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.quizMode == QuizMode.FLASHCARD) {
                            Text(
                                text = word.word,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            word.phonetic?.let {
                                Text(text = it, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        } else {
                            Text(
                                text = "What word matches this definition?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (uiState.quizMode == QuizMode.FLASHCARD && uiState.isRevealed) {
                            FlashcardMeaning(word)
                            Spacer(modifier = Modifier.height(32.dp))
                            FlashcardActions(viewModel)
                        } else if (uiState.quizMode == QuizMode.FLASHCARD) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.revealMeaning() }) {
                                    Text("Reveal Meaning")
                                }
                            }
                        } else if (uiState.quizMode == QuizMode.MULTIPLE_CHOICE) {
                            MultipleChoiceQuiz(word, uiState, viewModel)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Question ${uiState.totalQuestions} of ${uiState.maxQuestions}")
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardMeaning(word: com.example.wordquest.data.api.WordResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Meaning:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            val meaning = word.meanings.firstOrNull()
            val definition = meaning?.definitions?.firstOrNull()
            Text(
                text = definition?.definition ?: "No definition found",
                fontSize = 16.sp
            )
            definition?.example?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Example: \"$it\"",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun FlashcardActions(viewModel: ActivityViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Did you know this word?", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.submitAnswer(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Yes, I knew it")
            }
            OutlinedButton(
                onClick = { viewModel.submitAnswer(false) }
            ) {
                Text("No, I didn't")
            }
        }
    }
}

@Composable
fun MultipleChoiceQuiz(
    word: com.example.wordquest.data.api.WordResponse,
    uiState: ActivityUiState,
    viewModel: ActivityViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FlashcardMeaning(word)
        Spacer(modifier = Modifier.height(24.dp))
        uiState.options.forEach { option ->
            val isSelected = uiState.selectedOption == option
            val isCorrect = option == word.word
            val color = when {
                isSelected && isCorrect -> MaterialTheme.colorScheme.primary
                isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                uiState.selectedOption != null && isCorrect -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Button(
                onClick = { viewModel.selectOption(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                enabled = uiState.selectedOption == null
            ) {
                Text(text = option)
            }
        }

        if (uiState.selectedOption != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadNextWord() }) {
                Text("Next Word")
            }
        }
    }
}
