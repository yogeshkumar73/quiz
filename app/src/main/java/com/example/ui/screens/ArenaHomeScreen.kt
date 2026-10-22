package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.GuestProfileEntity
import com.example.data.local.McqQuestionEntity
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceElevated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArenaHomeScreen(
    activeProfile: GuestProfileEntity?,
    totalCachedQuestions: Int,
    revisionQuestions: List<McqQuestionEntity>,
    effectiveOnline: Boolean,
    isPassAndPlayEnabled: Boolean,
    onTogglePassAndPlay: (Boolean) -> Unit,
    onStartQuickPlay: (SubjectCategory, QuizGameMode, Int) -> Unit,
    onOpenStudioForCategory: (SubjectCategory) -> Unit
) {
    val coreCategories = listOf(
        SubjectCategory.CS_TECH,
        SubjectCategory.SCIENCE_MATH,
        SubjectCategory.EXAM_APTITUDE,
        SubjectCategory.LANGUAGE_VOCAB
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("arena_home_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Neo-Arcade Hero Banner Card with Custom Canvas Atmosphere
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.45f)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10192D))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ElectricEmerald.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.85f, size.height * 0.2f),
                                    radius = size.width * 0.65f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PulseCyan.copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                                    radius = size.width * 0.5f
                                )
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ElectricEmerald.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (effectiveOnline) {
                                        "⚡ HYBRID PYDANTIC AI + OFFLINE CACHE"
                                    } else {
                                        "🛡️ OFFLINE LIGHTWEIGHT ENGINE ACTIVE"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricEmerald,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "🔥 ${activeProfile?.currentStreak ?: 1} Day Streak",
                                style = MaterialTheme.typography.labelLarge,
                                color = BlitzAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Master Any Topic. Online or Offline.",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Adaptive Elo difficulty scaling, 3.0x speed-blitz tournaments, and schema-verified MCQ synthesis—with instant anonymous guest usability.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Player Telemetry Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatPill(
                                label = "SKILL RATING",
                                value = "${activeProfile?.eloRating ?: 1200} Elo",
                                accent = PulseCyan,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "ACCURACY",
                                value = "${activeProfile?.overallAccuracyPercent ?: 84}%",
                                accent = ElectricEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "OFFLINE BANK",
                                value = "$totalCachedQuestions MCQs",
                                accent = BlitzAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onStartQuickPlay(
                                        SubjectCategory.CS_TECH,
                                        QuizGameMode.ADAPTIVE_LEARNING,
                                        2
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_adaptive_quick_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Adaptive Mode")
                            }

                            FilledTonalButton(
                                onClick = {
                                    onStartQuickPlay(
                                        SubjectCategory.CS_TECH,
                                        QuizGameMode.COMPETITIVE_BLITZ,
                                        3
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_blitz_quick_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BlitzAmber.copy(alpha = 0.22f),
                                    contentColor = BlitzAmber
                                )
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Daily Blitz 3x")
                            }
                        }
                    }
                }
            }
        }

        // 2. Competitive Head-to-Head Challenge Room & Weak-Topic Revision Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = PulseCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Head-to-Head Challenge Arena",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPassAndPlayEnabled) {
                                        "2-Player Local Pass-and-Play Duel active"
                                    } else {
                                        "Live Ghost Rival Pace Duel (vs CyberBot_Elo1400)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "2P Local",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isPassAndPlayEnabled,
                                onCheckedChange = onTogglePassAndPlay,
                                modifier = Modifier.testTag("pass_and_play_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PulseCyan,
                                    checkedTrackColor = PulseCyan.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onStartQuickPlay(
                                    SubjectCategory.EXAM_APTITUDE,
                                    QuizGameMode.HEAD_TO_HEAD_DUEL,
                                    2
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("launch_h2h_duel_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PulseCyan,
                                contentColor = Color(0xFF062338)
                            )
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPassAndPlayEnabled) "Start 2P Duel" else "Duel Ghost Rival")
                        }

                        if (revisionQuestions.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = {
                                    onStartQuickPlay(
                                        SubjectCategory.CS_TECH,
                                        QuizGameMode.REVISION_DECK,
                                        2
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("launch_revision_deck_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CrimsonCoral.copy(alpha = 0.2f),
                                    contentColor = CrimsonCoral
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Revision (${revisionQuestions.size})")
                            }
                        }
                    }
                }
            }
        }

        // 3. Pre-Loaded Offline & Online Subject Categories
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRICULUM & COMPETITIVE TRACKS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "4 Pre-Loaded Domains",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricEmerald
                )
            }
        }

        items(coreCategories, key = { it.id }) { category ->
            val accentColor = when (category) {
                SubjectCategory.CS_TECH -> ElectricEmerald
                SubjectCategory.SCIENCE_MATH -> PulseCyan
                SubjectCategory.EXAM_APTITUDE -> BlitzAmber
                SubjectCategory.LANGUAGE_VOCAB -> RoyalViolet
                SubjectCategory.CUSTOM -> ElectricEmerald
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onStartQuickPlay(category, QuizGameMode.ADAPTIVE_LEARNING, 2)
                    }
                    .testTag("category_card_${category.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = accentColor.copy(alpha = 0.16f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = category.badgeIcon,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = category.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        category.defaultSubTopics.take(3).forEach { sub ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onStartQuickPlay(category, QuizGameMode.ADAPTIVE_LEARNING, 2)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("adaptive_btn_${category.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = accentColor.copy(alpha = 0.16f),
                                contentColor = accentColor
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adaptive Study")
                        }

                        FilledTonalButton(
                            onClick = {
                                onStartQuickPlay(category, QuizGameMode.COMPETITIVE_BLITZ, 3)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("blitz_btn_${category.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Timed Blitz")
                        }

                        FilledTonalButton(
                            onClick = { onOpenStudioForCategory(category) },
                            modifier = Modifier.testTag("studio_btn_${category.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = PulseCyan
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Generate custom MCQs for ${category.shortTitle}", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF17223B),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
