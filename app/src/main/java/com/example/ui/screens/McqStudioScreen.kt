package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.McqQuestionEntity
import com.example.domain.McqSchemaValidator
import com.example.model.DifficultyTier
import com.example.model.EngineConnectivityMode
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import com.example.ui.PulseUiState
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.SlateBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun McqStudioScreen(
    uiState: PulseUiState,
    cachedQuestions: List<McqQuestionEntity>,
    onUpdateConfig: (SubjectCategory, QuizGameMode, String, Int, Int) -> Unit,
    onSetConnectivityMode: (EngineConnectivityMode) -> Unit,
    onGenerateAndPlay: () -> Unit
) {
    val isPromptSafe = uiState.customTopicInput.isBlank() ||
        McqSchemaValidator.isTopicPromptSafe(uiState.customTopicInput)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mcq_studio_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hybrid Engine Mode Selector (Online Gemini vs Offline Lightweight Engine)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, PulseCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.effectiveOnlineMode) Icons.Default.CloudDone else Icons.Default.Memory,
                                contentDescription = null,
                                tint = if (uiState.effectiveOnlineMode) PulseCyan else BlitzAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Hybrid Pydantic MCQ Generator Studio",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.effectiveOnlineMode) {
                                        "Online Mode: Gemini 3.5 Flash JSON Schema + Auto Room Caching"
                                    } else {
                                        "Offline Mode: Lightweight On-Device Concept & Template Synthesizer"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.connectivityOverride == EngineConnectivityMode.AUTO_DETECT,
                            onClick = { onSetConnectivityMode(EngineConnectivityMode.AUTO_DETECT) },
                            label = { Text("Auto Network") },
                            modifier = Modifier.testTag("mode_chip_auto")
                        )
                        FilterChip(
                            selected = uiState.connectivityOverride == EngineConnectivityMode.FORCE_ONLINE_AI,
                            onClick = { onSetConnectivityMode(EngineConnectivityMode.FORCE_ONLINE_AI) },
                            label = { Text("Online Gemini AI") },
                            modifier = Modifier.testTag("mode_chip_online")
                        )
                        FilterChip(
                            selected = uiState.connectivityOverride == EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT,
                            onClick = { onSetConnectivityMode(EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT) },
                            label = { Text("Offline Lightweight") },
                            modifier = Modifier.testTag("mode_chip_offline")
                        )
                    }
                }
            }
        }

        // 2. Generator Configuration Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. SELECT SUBJECT DOMAIN TRACK",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricEmerald
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectCategory.entries.forEach { cat ->
                            val selected = uiState.selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateConfig(
                                        cat,
                                        uiState.selectedGameMode,
                                        uiState.customTopicInput,
                                        uiState.targetDifficultyTier,
                                        uiState.requestedQuestionCount
                                    )
                                },
                                label = { Text("${cat.badgeIcon} ${cat.shortTitle}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricEmerald.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricEmerald
                                ),
                                modifier = Modifier.testTag("studio_cat_${cat.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. CUSTOM TOPIC OR SUB-TOPIC PROMPT (OPTIONAL)",
                        style = MaterialTheme.typography.labelMedium,
                        color = PulseCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.customTopicInput,
                        onValueChange = { newText ->
                            onUpdateConfig(
                                uiState.selectedCategory,
                                uiState.selectedGameMode,
                                newText,
                                uiState.targetDifficultyTier,
                                uiState.requestedQuestionCount
                            )
                        },
                        placeholder = {
                            Text("e.g., Pydantic v2 Validators, Graph Algorithms, Quantum Optics...")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_topic_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Preset Sub-topic Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.selectedCategory.defaultSubTopics.forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        onUpdateConfig(
                                            uiState.selectedCategory,
                                            uiState.selectedGameMode,
                                            preset,
                                            uiState.targetDifficultyTier,
                                            uiState.requestedQuestionCount
                                        )
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+ $preset",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "3. TARGET DIFFICULTY TIER & GAMEPLAY MODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = BlitzAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DifficultyTier.entries.forEach { tier ->
                            val selected = uiState.targetDifficultyTier == tier.level
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateConfig(
                                        uiState.selectedCategory,
                                        uiState.selectedGameMode,
                                        uiState.customTopicInput,
                                        tier.level,
                                        uiState.requestedQuestionCount
                                    )
                                },
                                label = { Text("T${tier.level} ${tier.label}") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tier_chip_${tier.level}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            QuizGameMode.ADAPTIVE_LEARNING,
                            QuizGameMode.COMPETITIVE_BLITZ,
                            QuizGameMode.HEAD_TO_HEAD_DUEL
                        ).forEach { mode ->
                            FilterChip(
                                selected = uiState.selectedGameMode == mode,
                                onClick = {
                                    onUpdateConfig(
                                        uiState.selectedCategory,
                                        mode,
                                        uiState.customTopicInput,
                                        uiState.targetDifficultyTier,
                                        uiState.requestedQuestionCount
                                    )
                                },
                                label = { Text(mode.badge) },
                                modifier = Modifier.testTag("game_mode_chip_${mode.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Schema Sanitization & Validation Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (isPromptSafe) ElectricEmerald else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPromptSafe) {
                                "Pydantic v2 Schema Guard Active • 4-Choice Bounds & XSS Filter Ready"
                            } else {
                                "Unsafe script/SQLi pattern detected—input will be sanitized before synthesis."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPromptSafe) ElectricEmerald else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onGenerateAndPlay,
                        enabled = !uiState.isGeneratingMcqs,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_and_start_quiz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                    ) {
                        if (uiState.isGeneratingMcqs) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF042F2E),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Synthesizing & Validating Schema...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.effectiveOnlineMode) {
                                    "Generate Validated MCQs & Launch Arena"
                                } else {
                                    "Synthesize Offline MCQs & Launch Arena"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Local Room SQLite Offline-Cached Question Bank Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CACHED PYDANTIC-VALIDATED QUESTION BANK (${cachedQuestions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "100% Offline Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = PulseCyan
                )
            }
        }

        items(cachedQuestions.take(10), key = { it.id }) { q ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${q.subTopic} • Tier ${q.difficulty}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PulseCyan
                        )
                        Surface(
                            color = if (q.sourceEngine == "ONLINE_GEMINI") {
                                PulseCyan.copy(alpha = 0.16f)
                            } else {
                                ElectricEmerald.copy(alpha = 0.16f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = q.sourceEngine,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (q.sourceEngine == "ONLINE_GEMINI") PulseCyan else ElectricEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
