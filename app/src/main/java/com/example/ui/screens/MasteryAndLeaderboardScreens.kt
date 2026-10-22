package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.GuestProfileEntity
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.McqQuestionEntity
import com.example.data.local.TopicMasteryEntity
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import com.example.model.ValidationCheckResult
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceElevated

@Composable
fun AdaptiveMasteryScreen(
    activeProfile: GuestProfileEntity?,
    masteryNodes: List<TopicMasteryEntity>,
    revisionQuestions: List<McqQuestionEntity>,
    onStartPracticeForCategory: (SubjectCategory, Int) -> Unit,
    onStartRevisionDeck: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adaptive_mastery_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Mastery Summary & Weak-Topic Revision Deck Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ElectricEmerald,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Adaptive Mastery Tree",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeProfile?.displayName ?: "Guest"} • Elo ${activeProfile?.eloRating ?: 1200} • Overall Accuracy ${activeProfile?.overallAccuracyPercent ?: 84}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = if (revisionQuestions.isEmpty()) {
                            ElectricEmerald.copy(alpha = 0.12f)
                        } else {
                            CrimsonCoral.copy(alpha = 0.14f)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (revisionQuestions.isEmpty()) ElectricEmerald.copy(alpha = 0.4f) else CrimsonCoral.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weak-Topic Revision Deck (${revisionQuestions.size} Flagged)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (revisionQuestions.isEmpty()) ElectricEmerald else CrimsonCoral
                                )
                                Text(
                                    text = if (revisionQuestions.isEmpty()) {
                                        "All practiced concepts mastered! Bookmark or miss any question to queue it here."
                                    } else {
                                        "Spaced-repetition deck ready for targeted remediation."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (revisionQuestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = onStartRevisionDeck,
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCoral),
                                    modifier = Modifier.testTag("practice_revision_button")
                                ) {
                                    Text("Review Now")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Skill Nodes & Badges List
        item {
            Text(
                text = "SUB-TOPIC MASTERY NODES & UNLOCKED BADGES (${masteryNodes.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(masteryNodes, key = { it.masteryKey }) { node ->
            val category = SubjectCategory.fromId(node.categoryId)
            val progress = (node.masteryScore / 100f).coerceIn(0.05f, 1f)
            val badgeColor = when (node.currentDifficultyTier) {
                4 -> BlitzAmber
                3 -> ElectricEmerald
                2 -> PulseCyan
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${category.badgeIcon} ${node.subTopic}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${category.shortTitle} • Tier ${node.currentDifficultyTier} • ${node.correctCount}/${node.attemptsCount} Correct",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = badgeColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "🏅 ${node.unlockedBadge} (${node.masteryScore}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = badgeColor,
                        trackColor = Color(0xFF0B0F17)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onStartPracticeForCategory(category, node.currentDifficultyTier)
                            },
                            modifier = Modifier.testTag("mastery_practice_${node.subTopic.hashCode()}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Train Tier ${node.currentDifficultyTier}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealTimeLeaderboardScreen(
    entries: List<LeaderboardEntryEntity>,
    activeProfile: GuestProfileEntity?,
    effectiveOnline: Boolean,
    onTriggerSyncAndNotify: () -> Unit,
    onStartBlitzChallenge: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredEntries = remember(entries, selectedFilter) {
        if (selectedFilter == "ALL") entries
        else entries.filter { it.gameMode == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Leaderboard Header & Sync Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = BorderStroke(1.dp, BlitzAmber.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = BlitzAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Real-Time Arena Leaderboard",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (effectiveOnline) {
                                        "Live Sync Active • Instant Guest & OAuth 2.0 Contenders"
                                    } else {
                                        "Offline Cache Active • Scores queued for automatic sync"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All Arenas") }
                        )
                        FilterChip(
                            selected = selectedFilter == QuizGameMode.COMPETITIVE_BLITZ.id,
                            onClick = { selectedFilter = QuizGameMode.COMPETITIVE_BLITZ.id },
                            label = { Text("⚡ Timed Blitz") }
                        )
                        FilterChip(
                            selected = selectedFilter == QuizGameMode.ADAPTIVE_LEARNING.id,
                            onClick = { selectedFilter = QuizGameMode.ADAPTIVE_LEARNING.id },
                            label = { Text("🧠 Adaptive") }
                        )
                        FilterChip(
                            selected = selectedFilter == QuizGameMode.HEAD_TO_HEAD_DUEL.id,
                            onClick = { selectedFilter = QuizGameMode.HEAD_TO_HEAD_DUEL.id },
                            label = { Text("⚔️ H2H Duels") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onStartBlitzChallenge,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("leaderboard_play_blitz_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlitzAmber,
                                contentColor = Color(0xFF3B1803)
                            )
                        ) {
                            Text("Climb Daily Blitz", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onTriggerSyncAndNotify,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("leaderboard_sync_notify_btn")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync & Notify")
                        }
                    }
                }
            }
        }

        // 2. Ranked Contenders List
        itemsIndexed(filteredEntries, key = { _, item -> item.entryId }) { index, entry ->
            val rank = index + 1
            val isCurrentUser = entry.playerName == activeProfile?.displayName || entry.isUserEntry
            val medalColor = when (rank) {
                1 -> BlitzAmber
                2 -> Color(0xFFCBD5E1)
                3 -> Color(0xFFD97706)
                else -> PulseCyan
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser) {
                        ElectricEmerald.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrentUser) ElectricEmerald else SlateBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = medalColor.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#$rank",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = medalColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = entry.avatarEmoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.playerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrentUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(YOU)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricEmerald
                                    )
                                }
                            }
                            Text(
                                text = "Elo ${entry.eloRating} • ${entry.accuracyPercent}% Acc • ${entry.maxStreak}x Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.score} PTS",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (rank == 1) BlitzAmber else ElectricEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (entry.isSyncedToCloud) PulseCyan else BlitzAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (entry.isSyncedToCloud) "Synced" else "Cached",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.isSyncedToCloud) PulseCyan else BlitzAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityAuditScreen(
    checks: List<ValidationCheckResult>,
    activeProfile: GuestProfileEntity?,
    onReRunSecurityAudit: () -> Unit,
    onOpenProfileOAuthSheet: () -> Unit
) {
    val allPassed = checks.all { it.passed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_audit_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Security & Schema Audit Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = BorderStroke(1.dp, if (allPassed) ElectricEmerald else CrimsonCoral)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ElectricEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pydantic Schema & Security Suite",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${checks.count { it.passed }}/${checks.size} Verification Suites Passing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricEmerald
                                )
                            }
                        }

                        Button(
                            onClick = onReRunSecurityAudit,
                            modifier = Modifier.testTag("rerun_security_audit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Tests")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = onOpenProfileOAuthSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_manage_oauth_btn")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeProfile?.oauthEmail != null) {
                                "OAuth 2.0 Linked (${activeProfile.oauthEmail}) • Manage Identity"
                            } else {
                                "Instant Guest Session Active • Configure Optional OAuth 2.0"
                            }
                        )
                    }
                }
            }
        }

        // 2. Individual Security & Schema Verification Checks
        items(checks, key = { it.checkId }) { check ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (check.passed) ElectricEmerald.copy(alpha = 0.4f) else CrimsonCoral
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (check.passed) ElectricEmerald else CrimsonCoral,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = check.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${check.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = PulseCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = check.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Prototype APK Key Extraction Caution Notice (Mandatory per gemini-api / android-secret-management skills)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlitzAmber.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, BlitzAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = BlitzAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Prototype Security Advisory: Gemini API keys configured via BuildConfig are intended for prototyping only. Android APKs can be decompiled; do not share the generated APK publicly if it contains production API keys.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
