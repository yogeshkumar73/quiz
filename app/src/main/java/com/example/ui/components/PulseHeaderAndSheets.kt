package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.GuestProfileEntity
import com.example.model.EngineConnectivityMode
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceElevated

@Composable
fun PulseTopArenaBar(
    activeProfile: GuestProfileEntity?,
    effectiveOnline: Boolean,
    connectivityOverride: EngineConnectivityMode,
    statusBanner: String,
    onProfileClick: () -> Unit,
    onToggleConnectivityMode: () -> Unit,
    onSyncAndNotifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF131B2E),
                        Color(0xFF0B0F17)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Multi-User Guest / OAuth Profile Switcher Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onProfileClick() }
                    .testTag("profile_switcher_button"),
                color = SlateSurfaceElevated,
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeProfile?.avatarEmoji ?: "⚡",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeProfile?.displayName ?: "Nova_Guest",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (activeProfile?.oauthEmail != null) {
                                    PulseCyan.copy(alpha = 0.2f)
                                } else {
                                    ElectricEmerald.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (activeProfile?.oauthEmail != null) "OAUTH" else "GUEST",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (activeProfile?.oauthEmail != null) PulseCyan else ElectricEmerald,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Lv.${activeProfile?.level ?: 1} • ${activeProfile?.totalXp ?: 0} XP • Elo ${activeProfile?.eloRating ?: 1200}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Online / Offline Engine Toggle Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleConnectivityMode() }
                        .testTag("connectivity_mode_toggle"),
                    color = if (effectiveOnline) {
                        PulseCyan.copy(alpha = 0.14f)
                    } else {
                        BlitzAmber.copy(alpha = 0.16f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (effectiveOnline) PulseCyan.copy(alpha = 0.5f) else BlitzAmber.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (effectiveOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Connectivity engine mode",
                            tint = if (effectiveOnline) PulseCyan else BlitzAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when (connectivityOverride) {
                                EngineConnectivityMode.AUTO_DETECT -> if (effectiveOnline) "Online AI" else "Offline Engine"
                                EngineConnectivityMode.FORCE_ONLINE_AI -> "Forced Online"
                                EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT -> "Offline Mode"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (effectiveOnline) PulseCyan else BlitzAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sync & Push Notification Trigger
                IconButton(
                    onClick = onSyncAndNotifyClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceElevated)
                        .border(1.dp, SlateBorder, CircleShape)
                        .testTag("sync_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger real-time sync and push notification",
                        tint = ElectricEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Status Telemetry Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (effectiveOnline) ElectricEmerald else BlitzAmber)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusBanner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MultiUserProfileAndOAuthSheet(
    profiles: List<GuestProfileEntity>,
    activeProfile: GuestProfileEntity?,
    onDismiss: () -> Unit,
    onSelectProfile: (Long) -> Unit,
    onCreateGuestProfile: (name: String, emoji: String) -> Unit,
    onLinkGoogleOAuth: () -> Unit,
    onUnlinkOAuth: () -> Unit
) {
    var newGuestName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    val emojiChoices = listOf("⚡", "🦊", "🚀", "🧬", "👑", "🐍", "🎯", "🛡️")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Instant Guest Profiles & OAuth 2.0 Sync",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zero login required! Switch between isolated local guest profiles immediately or optionally link Google OAuth 2.0 for cloud leaderboard backup.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Active Profiles List
            Text(
                text = "MULTI-USER LOCAL & SYNCED PROFILES",
                style = MaterialTheme.typography.labelMedium,
                color = PulseCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            profiles.forEach { profile ->
                val isCurrent = profile.profileId == activeProfile?.profileId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectProfile(profile.profileId) }
                        .testTag("profile_item_${profile.profileId}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) {
                            ElectricEmerald.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCurrent) ElectricEmerald else SlateBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.avatarEmoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (profile.oauthEmail != null) "• OAuth Linked" else "• Instant Guest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (profile.oauthEmail != null) PulseCyan else ElectricEmerald
                                    )
                                }
                                Text(
                                    text = "Level ${profile.level} • ${profile.totalXp} XP • Elo ${profile.eloRating} • Streak ${profile.currentStreak}🔥",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active profile",
                                tint = ElectricEmerald
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SlateBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Optional OAuth 2.0 Section
            Text(
                text = "OPTIONAL SECURE OAUTH 2.0 CLOUD BINDING",
                style = MaterialTheme.typography.labelMedium,
                color = BlitzAmber
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (activeProfile?.oauthEmail == null) {
                FilledTonalButton(
                    onClick = onLinkGoogleOAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("link_google_oauth_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PulseCyan.copy(alpha = 0.2f),
                        contentColor = PulseCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google (OAuth 2.0 CredentialManager)")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Linked: ${activeProfile.oauthEmail}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricEmerald
                        )
                        Text(
                            text = "Real-time cloud sync & leaderboard backup enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onUnlinkOAuth,
                        modifier = Modifier.testTag("unlink_oauth_button")
                    ) {
                        Text("Revert to Guest")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SlateBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Create New Instant Guest Profile
            Text(
                text = "ADD NEW INSTANT GUEST PROFILE (NO LOGIN)",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricEmerald
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojiChoices.forEach { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { selectedEmoji = emoji },
                        color = if (selectedEmoji == emoji) {
                            ElectricEmerald.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (selectedEmoji == emoji) ElectricEmerald else SlateBorder
                        ),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = newGuestName,
                    onValueChange = { newGuestName = it },
                    placeholder = { Text("Enter Guest Callsign (e.g. Pixel_Hero)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_guest_name_input")
                )
                Button(
                    onClick = {
                        onCreateGuestProfile(
                            newGuestName.ifBlank { "Guest_${profiles.size + 1}" },
                            selectedEmoji
                        )
                        newGuestName = ""
                    },
                    modifier = Modifier.testTag("create_guest_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
