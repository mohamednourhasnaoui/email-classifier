package com.example.emailclassifier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emailclassifier.data.ClassificationResult
import com.example.emailclassifier.data.LearningDataset
import com.example.emailclassifier.data.TrainingExample
import com.example.emailclassifier.viewmodel.EmailClassifierViewModel

@Composable
fun EmailClassifierScreen(viewModel: EmailClassifierViewModel) {
    val result by viewModel.classificationResult.collectAsState()
    val customText by viewModel.customText.collectAsState()
    val selectedDatasetCategory by viewModel.selectedDatasetCategory.collectAsState()
    val predictionHistory by viewModel.predictionHistory.collectAsState()
    var showResultDialog by rememberSaveable { mutableStateOf(false) }

    selectedDatasetCategory?.let { category ->
        DatasetExamplesScreen(
            category = category,
            examples = viewModel.getExamplesForCategory(category),
            onBack = {
                viewModel.closeDatasetCategory()
            }
        )
        return
    }

    result?.let { classification ->
        if (showResultDialog) {
            ClassificationPopup(
                result = classification,
                onDismiss = {
                    showResultDialog = false
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HeaderSection()
            }

            item {
                ManualTextInputCard(
                    text = customText,
                    onTextChange = {
                        viewModel.updateCustomText(it)
                    },
                    onClassify = {
                        viewModel.classifyCustomText()
                        showResultDialog = true
                    },
                    onClear = {
                        viewModel.clearInput()
                    },
                    onLoadSample = {
                        viewModel.loadSampleText(it)
                    }
                )
            }

            result?.let { classification ->
                item {
                    ResultCard(result = classification)
                }
            }

            if (predictionHistory.isNotEmpty()) {
                item {
                    PredictionHistoryCard(
                        history = predictionHistory,
                        onClearHistory = {
                            viewModel.clearHistory()
                        }
                    )
                }
            }

            item {
                HowItWorksCard()
            }

            item {
                LearningDatasetCard(
                    onCategoryClick = {
                        viewModel.openDatasetCategory(it)
                    }
                )
            }
        }
    }
}

@Composable
fun ClassificationPopup(
    result: ClassificationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "AI Classification Result",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column {
                Text(
                    text = "The AI model classified this email as:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.category,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Spam probability score: ${result.spamScore}%")
                Text(text = "Work probability score: ${result.workScore}%")
                Text(text = "Personal probability score: ${result.personalScore}%")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "AI Email Classifier",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Classify emails into Spam, Work, Personal, or Junk Email using supervised AI learning.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderBadge(text = "Naive Bayes")
                HeaderBadge(text = "Kotlin")
                HeaderBadge(text = "Compose")
            }
        }
    }
}

@Composable
fun HeaderBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ManualTextInputCard(
    text: String,
    onTextChange: (String) -> Unit,
    onClassify: () -> Unit,
    onClear: () -> Unit,
    onLoadSample: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Analyze an email",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Paste an email message and let the AI model predict its category.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = {
                    Text(text = "Example: Congratulations, you won a free prize...")
                },
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClassify,
                enabled = text.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Classify Email",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClear,
                enabled = text.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Clear Text",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Quick test samples",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SampleButton(
                    text = "Spam",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onLoadSample("Spam")
                    }
                )

                SampleButton(
                    text = "Work",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onLoadSample("Work")
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SampleButton(
                    text = "Personal",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onLoadSample("Personal")
                    }
                )

                SampleButton(
                    text = "Junk",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onLoadSample("Junk")
                    }
                )
            }
        }
    }
}

@Composable
fun SampleButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Final AI classification",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.category,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(18.dp))

            val maxScore = maxOf(
                result.spamScore,
                result.workScore,
                result.personalScore,
                1
            )

            ScoreRow(
                label = "Spam",
                score = result.spamScore,
                maxScore = maxScore
            )

            ScoreRow(
                label = "Work",
                score = result.workScore,
                maxScore = maxScore
            )

            ScoreRow(
                label = "Personal",
                score = result.personalScore,
                maxScore = maxScore
            )
        }
    }
}

@Composable
fun PredictionHistoryCard(
    history: List<ClassificationResult>,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prediction History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Clear",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onClearHistory()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            history.take(5).forEachIndexed { index, item ->
                HistoryLine(
                    number = index + 1,
                    result = item
                )
            }
        }
    }
}

@Composable
fun HistoryLine(
    number: Int,
    result: ClassificationResult
) {
    val confidence = maxOf(
        result.spamScore,
        result.workScore,
        result.personalScore
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 11.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$number. ${result.category}",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "$confidence%",
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun ScoreRow(
    label: String,
    score: Int,
    maxScore: Int
) {
    val progress = score.toFloat() / maxScore.toFloat()

    Column(
        modifier = Modifier.padding(vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$score%",
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        LinearProgressIndicator(
            progress = {
                progress
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50.dp))
        )
    }
}

@Composable
fun HowItWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "AI integration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoLine(text = "Supervised learning with labeled email examples.")
            InfoLine(text = "Naive Bayes learns word patterns from the dataset.")
            InfoLine(text = "The model calculates probability scores.")
            InfoLine(text = "Unknown or weak inputs become Junk Email.")
        }
    }
}

@Composable
fun InfoLine(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun LearningDatasetCard(
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Learning Dataset",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Open the training examples used by the AI model.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            DatasetLine(
                label = "Spam examples",
                value = LearningDataset.spamCount,
                onClick = {
                    onCategoryClick("Spam")
                }
            )

            DatasetLine(
                label = "Work examples",
                value = LearningDataset.workCount,
                onClick = {
                    onCategoryClick("Work")
                }
            )

            DatasetLine(
                label = "Personal examples",
                value = LearningDataset.personalCount,
                onClick = {
                    onCategoryClick("Personal")
                }
            )
        }
    }
}

@Composable
fun DatasetLine(
    label: String,
    value: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .padding(vertical = 14.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "$value examples",
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DatasetExamplesScreen(
    category: String,
    examples: List<TrainingExample>,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Back",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 7.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            text = "$category examples",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${examples.size} supervised learning examples used by the AI model.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            items(examples) { example ->
                ExampleCard(example = example)
            }
        }
    }
}

@Composable
fun ExampleCard(example: TrainingExample) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = example.category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = example.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}