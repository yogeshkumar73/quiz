package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DifficultyTier
import com.example.model.QuizGameMode
import com.example.ui.ActiveQuizSessionState
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceElevated

@Composable
fun QuizSessionScreen(
    session: ActiveQuizSessionState,
    activePlayerName: String,
    onSelectOption: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onNextQuestion: () -> Unit,
    onReplayQuiz: () -> Unit,
    onExitQuiz: () -> Unit
) {
    BackHandler {
        onExitQuiz()
    }

    if (session.isFinished) {
        QuizSummaryView(
            session = session,
            activePlayerName = activePlayerName,
            onReplayQuiz = onReplayQuiz,
            onExitQuiz = onExitQuiz
        )
        return
    }

    val currentQuestion = session.currentQuestion ?: return
    val tierMeta = DifficultyTier.fromLevel(session.currentAdaptiveTier)
    val timerFraction by animateFloatAsState(
        targetValue = (session.timeRemainingSeconds.toFloat() / session.totalTimeForQuestion.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "timer_progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_quiz_session_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Quiz Arena HUD (Progress, Timer, Multiplier, Exit)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onExitQuiz,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("exit_quiz_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit quiz session",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${session.mode.title} • Q${session.currentIndex + 1}/${session.questions.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tier ${tierMeta.level} (${tierMeta.label}) • +${session.earnedXp} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PulseCyan
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Streak Multiplier Badge
                            Surface(
                                color = BlitzAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, BlitzAmber)
                            ) {
                                Text(
                                    text = "${session.streakMultiplier}x 🔥${session.currentComboStreak}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BlitzAmber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Countdown Pill
                            Surface(
                                color = if (session.timeRemainingSeconds <= 5) {
                                    CrimsonCoral.copy(alpha = 0.22f)
                                } else {
                                    ElectricEmerald.copy(alpha = 0.16f)
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (session.timeRemainingSeconds <= 5) CrimsonCoral else ElectricEmerald,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${session.timeRemainingSeconds}s",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (session.timeRemainingSeconds <= 5) CrimsonCoral else ElectricEmerald
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { timerFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (session.timeRemainingSeconds <= 5) CrimsonCoral else ElectricEmerald,
                        trackColor = Color(0xFF0B0F17)
                    )

                    // Head-to-Head Rival / Pass-and-Play Turn Banner
                    if (session.mode == QuizGameMode.HEAD_TO_HEAD_DUEL) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF101829),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PulseCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (session.isPassAndPlayDuel) {
                                    Text(
                                        text = "P1 ($activePlayerName): ${session.player1Score} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (!session.isPlayerTwoTurn) ElectricEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        color = PulseCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (session.isPlayerTwoTurn) "TURN: ${session.player2Name}" else "TURN: $activePlayerName",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PulseCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "P2 (${session.player2Name}): ${session.player2Score} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (session.isPlayerTwoTurn) BlitzAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "You ($activePlayerName): ${session.player1Score} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ElectricEmerald
                                    )
                                    Text(
                                        text = "VS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlitzAmber
                                    )
                                    Text(
                                        text = "${session.rivalName}: ${session.rivalScore} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrimsonCoral
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Question Stem & Optional Code/Formula Snippet Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PulseCyan.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${currentQuestion.subTopic} • ${currentQuestion.sourceEngine}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PulseCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.testTag("bookmark_revision_button")
                        ) {
                            Icon(
                                imageVector = if (currentQuestion.needsRevision) {
                                    Icons.Default.Bookmark
                                } else {
                                    Icons.Default.BookmarkBorder
                                },
                                contentDescription = "Toggle Weak-Topic Revision flag",
                                tint = if (currentQuestion.needsRevision) BlitzAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!currentQuestion.codeSnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFF080D16),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentQuestion.codeSnippet,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricEmerald,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. 4 Validated MCQ Option Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                session.currentOptions.forEachIndexed { idx, optionText ->
                    val optionLabels = listOf("A", "B", "C", "D")
                    val isSelected = session.selectedOptionIndex == idx
                    val isCorrectOption = currentQuestion.correctIndex == idx

                    val borderColor = when {
                        !session.isAnswerRevealed && isSelected -> PulseCyan
                        !session.isAnswerRevealed -> SlateBorder
                        isCorrectOption -> ElectricEmerald
                        isSelected && !isCorrectOption -> CrimsonCoral
                        else -> SlateBorder
                    }

                    val containerColor = when {
                        !session.isAnswerRevealed && isSelected -> PulseCyan.copy(alpha = 0.14f)
                        !session.isAnswerRevealed -> MaterialTheme.colorScheme.surface
                        isCorrectOption -> ElectricEmerald.copy(alpha = 0.18f)
                        isSelected && !isCorrectOption -> CrimsonCoral.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !session.isAnswerRevealed) {
                                onSelectOption(idx)
                            }
                            .testTag("mcq_option_$idx"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = borderColor.copy(alpha = 0.22f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionLabels.getOrElse(idx) { "${idx + 1}" },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (session.isAnswerRevealed && isCorrectOption) {
                                            ElectricEmerald
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (session.isAnswerRevealed) {
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isCorrectOption) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct option",
                                        tint = ElectricEmerald
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Incorrect option",
                                        tint = CrimsonCoral
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Adaptive Telemetry & Deep Conceptual Explanation Reveal
        item {
            AnimatedVisibility(visible = session.isAnswerRevealed) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explanation_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                    border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = BlitzAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONCEPTUAL EXPLANATION & ADAPTIVE ENGINE",
                                style = MaterialTheme.typography.labelLarge,
                                color = BlitzAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color(0xFF0B0F17),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = session.lastAdaptationNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = PulseCyan,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("next_question_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                        ) {
                            Text(
                                text = if (session.currentIndex + 1 >= session.questions.size) {
                                    "Complete Arena & Sync Leaderboard"
                                } else {
                                    "Next Question"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizSummaryView(
    session: ActiveQuizSessionState,
    activePlayerName: String,
    onReplayQuiz: () -> Unit,
    onExitQuiz: () -> Unit
) {
    val total = session.questions.size.coerceAtLeast(1)
    val accuracy = ((session.correctCount * 100f) / total).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("quiz_summary_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
            border = BorderStroke(1.5.dp, ElectricEmerald)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = BlitzAmber,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${session.mode.title} Complete!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.category.title} • Verified & Cached",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryMetricBox(label = "ACCURACY", value = "$accuracy%", color = ElectricEmerald)
                    SummaryMetricBox(label = "EARNED XP", value = "+${session.earnedXp}", color = PulseCyan)
                    SummaryMetricBox(label = "MAX STREAK", value = "${session.maxSessionStreak}x 🔥", color = BlitzAmber)
                }

                if (session.mode == QuizGameMode.HEAD_TO_HEAD_DUEL) {
                    Spacer(modifier = Modifier.height(18.dp))
                    val opponentLabel = if (session.isPassAndPlayDuel) session.player2Name else session.rivalName
                    val opponentPts = if (session.isPassAndPlayDuel) session.player2Score else session.rivalScore
                    val winnerText = when {
                        session.player1Score > opponentPts -> "🏆 Victory! $activePlayerName (${session.player1Score}) defeated $opponentLabel ($opponentPts)"
                        session.player1Score < opponentPts -> "⚔️ Close Duel! $opponentLabel ($opponentPts) edged out $activePlayerName (${session.player1Score})"
                        else -> "🤝 Dead Heat Draw! Both scored ${session.player1Score} pts"
                    }
                    Surface(
                        color = PulseCyan.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = winnerText,
                            style = MaterialTheme.typography.titleSmall,
                            color = PulseCyan,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onReplayQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("replay_quiz_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Again")
                    }

                    Button(
                        onClick = onExitQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("return_arena_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                    ) {
                        Text("Back to Arena")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricBox(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
