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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.generator.ChatModelTier
import com.example.data.generator.ChatTurnMessage
import com.example.data.generator.GeminiMcqService
import com.example.data.generator.TutorPersonaRole
import com.example.data.system.LiveVoiceSessionState
import com.example.ui.theme.BlitzAmber
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceElevated

enum class AiHubMode(val label: String) {
    MULTI_TURN_CHAT("💬 Gemini Chat"),
    VOICE_TRANSCRIBE("🎙️ Transcribe"),
    LIVE_VOICE_API("🔊 Live Voice")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiVoiceAndChatScreen(
    chatHistory: List<ChatTurnMessage>,
    selectedModelTier: ChatModelTier,
    selectedPersonaRole: TutorPersonaRole,
    isChatLoading: Boolean,
    isRecordingMic: Boolean,
    isTranscribingAudio: Boolean,
    lastTranscribedText: String,
    liveVoiceState: LiveVoiceSessionState,
    onSelectModelTier: (ChatModelTier) -> Unit,
    onSelectPersonaRole: (TutorPersonaRole) -> Unit,
    onSendChatMessage: (String) -> Unit,
    onToggleMicTranscribe: () -> Unit,
    onGenerateQuizFromText: (String) -> Unit,
    onConnectLiveVoice: () -> Unit,
    onDisconnectLiveVoice: () -> Unit,
    onSendLiveVoicePrompt: (String) -> Unit
) {
    var activeSubMode by remember { mutableStateOf(AiHubMode.MULTI_TURN_CHAT) }
    var chatInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("ai_voice_chat_screen")
    ) {
        // Sub-mode switcher: Chatbot | Audio Transcription (gemini-3.5-transcribe) | Live Voice (gemini-3.8-live)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiHubMode.entries.forEach { mode ->
                val selected = activeSubMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { activeSubMode = mode },
                    label = { Text(mode.label) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_hub_mode_${mode.name}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = ElectricEmerald
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (activeSubMode) {
            AiHubMode.MULTI_TURN_CHAT -> {
                MultiTurnChatbotPanel(
                    chatHistory = chatHistory,
                    selectedModelTier = selectedModelTier,
                    selectedPersonaRole = selectedPersonaRole,
                    isChatLoading = isChatLoading,
                    isRecordingMic = isRecordingMic,
                    isTranscribingAudio = isTranscribingAudio,
                    chatInput = chatInput,
                    onChatInputChange = { chatInput = it },
                    onSelectModelTier = onSelectModelTier,
                    onSelectPersonaRole = onSelectPersonaRole,
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            onSendChatMessage(chatInput)
                            chatInput = ""
                        }
                    },
                    onToggleMic = onToggleMicTranscribe,
                    onUseTranscriptInInput = { transcript ->
                        chatInput = transcript
                    },
                    lastTranscribedText = lastTranscribedText,
                    onGenerateQuizFromText = onGenerateQuizFromText
                )
            }

            AiHubMode.VOICE_TRANSCRIBE -> {
                AudioTranscribeStudioPanel(
                    isRecordingMic = isRecordingMic,
                    isTranscribingAudio = isTranscribingAudio,
                    lastTranscribedText = lastTranscribedText,
                    onToggleMicTranscribe = onToggleMicTranscribe,
                    onSendTranscriptToChat = { transcript ->
                        onSendChatMessage(transcript)
                        activeSubMode = AiHubMode.MULTI_TURN_CHAT
                    },
                    onGenerateMcqsFromTranscript = { transcript ->
                        onGenerateQuizFromText(transcript)
                    }
                )
            }

            AiHubMode.LIVE_VOICE_API -> {
                GeminiLiveVoicePanel(
                    liveVoiceState = liveVoiceState,
                    isRecordingMic = isRecordingMic,
                    isTranscribingAudio = isTranscribingAudio,
                    onConnectLiveVoice = onConnectLiveVoice,
                    onDisconnectLiveVoice = onDisconnectLiveVoice,
                    onToggleMicCaptureForLive = onToggleMicTranscribe,
                    onSendLiveVoicePrompt = onSendLiveVoicePrompt
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiTurnChatbotPanel(
    chatHistory: List<ChatTurnMessage>,
    selectedModelTier: ChatModelTier,
    selectedPersonaRole: TutorPersonaRole,
    isChatLoading: Boolean,
    isRecordingMic: Boolean,
    isTranscribingAudio: Boolean,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSelectModelTier: (ChatModelTier) -> Unit,
    onSelectPersonaRole: (TutorPersonaRole) -> Unit,
    onSend: () -> Unit,
    onToggleMic: () -> Unit,
    onUseTranscriptInInput: (String) -> Unit,
    lastTranscribedText: String,
    onGenerateQuizFromText: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Model & System Instruction Persona Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "GEMINI MODEL TIER (FAST / GENERAL / COMPLEX)",
                    style = MaterialTheme.typography.labelSmall,
                    color = PulseCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChatModelTier.entries.forEach { tier ->
                        val selected = selectedModelTier == tier
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectModelTier(tier) },
                            label = {
                                Text(
                                    text = tier.badge,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_model_chip_${tier.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseCyan.copy(alpha = 0.22f),
                                selectedLabelColor = PulseCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SYSTEM ROLE: ${selectedPersonaRole.title} • ${selectedModelTier.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TutorPersonaRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedPersonaRole == role,
                            onClick = { onSelectPersonaRole(role) },
                            label = {
                                Text(
                                    text = "${role.icon} ${role.title}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("persona_role_${role.id}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Multi-Turn Conversation Thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_message_thread"),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatHistory, key = { it.id }) { msg ->
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                val bubbleColor = if (msg.isUser) {
                    ElectricEmerald.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
                val borderColor = if (msg.isUser) ElectricEmerald else SlateBorder

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bubbleColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (msg.isUser) {
                                        if (msg.isAudioTranscript) "🎙️ YOU (via gemini-3.5-transcribe)" else "YOU"
                                    } else {
                                        "🤖 ${msg.roleTitle}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.isUser) ElectricEmerald else PulseCyan
                                )
                                Text(
                                    text = msg.modelUsed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlitzAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!msg.isUser) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onGenerateQuizFromText(msg.text.take(60))
                                            },
                                        color = ElectricEmerald.copy(alpha = 0.16f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = ElectricEmerald,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Synthesize Quiz on This",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElectricEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PulseCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedModelTier.modelId} thinking...",
                            style = MaterialTheme.typography.labelSmall,
                            color = PulseCyan
                        )
                    }
                }
            }
        }

        // Voice Transcript Quick-Insert Banner if available
        if (lastTranscribedText.isNotBlank() && chatInput != lastTranscribedText) {
            Surface(
                color = BlitzAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BlitzAmber.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { onUseTranscriptInInput(lastTranscribedText) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎙️ Transcribed: \"$lastTranscribedText\" (Tap to insert)",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlitzAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom Chat Input & Microphone Transcribe Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecordingMic) CrimsonCoral else SlateSurfaceElevated
                    )
                    .testTag("chat_mic_transcribe_button")
            ) {
                if (isTranscribingAudio) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BlitzAmber,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isRecordingMic) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record microphone audio for gemini-3.5-transcribe",
                        tint = if (isRecordingMic) Color.White else BlitzAmber
                    )
                }
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                placeholder = {
                    Text(
                        if (isRecordingMic) "Recording mic... tap stop to transcribe"
                        else "Ask ${selectedPersonaRole.title} (${selectedModelTier.modelId})..."
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input")
            )

            IconButton(
                onClick = onSend,
                enabled = chatInput.isNotBlank() && !isChatLoading,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ElectricEmerald)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send chat message",
                    tint = Color(0xFF042F2E)
                )
            }
        }
    }
}

@Composable
private fun AudioTranscribeStudioPanel(
    isRecordingMic: Boolean,
    isTranscribingAudio: Boolean,
    lastTranscribedText: String,
    onToggleMicTranscribe: () -> Unit,
    onSendTranscriptToChat: (String) -> Unit,
    onGenerateMcqsFromTranscript: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transcribe_studio_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
            border = BorderStroke(1.dp, BlitzAmber.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = BlitzAmber.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, BlitzAmber)
                ) {
                    Text(
                        text = "🎙️ MODEL: ${GeminiMcqService.TRANSCRIBE_MODEL_ID}",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlitzAmber,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Voice-to-Text Study & MCQ Dictation",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Speak any question, topic, or study note into your microphone. PulseQuiz transcribes it using gemini-3.5-transcribe so you can generate MCQs or ask your AI Tutor hands-free.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onToggleMicTranscribe,
                    enabled = !isTranscribingAudio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("primary_record_transcribe_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingMic) CrimsonCoral else BlitzAmber,
                        contentColor = if (isRecordingMic) Color.White else Color(0xFF3B1803)
                    )
                ) {
                    if (isTranscribingAudio) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF3B1803),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Transcribing with ${GeminiMcqService.TRANSCRIBE_MODEL_ID}...")
                    } else {
                        Icon(
                            imageVector = if (isRecordingMic) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecordingMic) {
                                "Stop Recording & Transcribe Now"
                            } else {
                                "Tap to Record Microphone Audio"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Transcription Output Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRANSCRIBED AUDIO OUTPUT (${GeminiMcqService.TRANSCRIBE_MODEL_ID})",
                    style = MaterialTheme.typography.labelMedium,
                    color = PulseCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lastTranscribedText.ifBlank {
                        "No voice clip transcribed yet. Tap the microphone button above to record and transcribe your voice!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (lastTranscribedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSendTranscriptToChat(lastTranscribedText) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_transcript_to_chat_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = Color(0xFF062338))
                        ) {
                            Text("Ask AI Chatbot")
                        }
                        FilledTonalButton(
                            onClick = { onGenerateMcqsFromTranscript(lastTranscribedText) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_mcqs_from_transcript_button")
                        ) {
                            Text("Generate MCQs")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeminiLiveVoicePanel(
    liveVoiceState: LiveVoiceSessionState,
    isRecordingMic: Boolean,
    isTranscribingAudio: Boolean,
    onConnectLiveVoice: () -> Unit,
    onDisconnectLiveVoice: () -> Unit,
    onToggleMicCaptureForLive: () -> Unit,
    onSendLiveVoicePrompt: (String) -> Unit
) {
    val quickVoiceTopics = listOf(
        "Quiz me orally on Python Pydantic v2 validators",
        "Give me a rapid 30-second mental math challenge",
        "Explain OAuth 2.0 PKCE in two spoken sentences"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_voice_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
            border = BorderStroke(
                1.5.dp,
                if (liveVoiceState.isConnected) ElectricEmerald else RoyalViolet.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = RoyalViolet.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, RoyalViolet)
                    ) {
                        Text(
                            text = "🔊 LIVE API: ${liveVoiceState.activeModelName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalViolet,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = if (liveVoiceState.isConnected) "● LIVE STREAM" else "○ STANDBY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (liveVoiceState.isConnected) ElectricEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pulsing Voice Orb
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = if (liveVoiceState.isConnected) {
                        ElectricEmerald.copy(alpha = 0.22f)
                    } else {
                        RoyalViolet.copy(alpha = 0.18f)
                    },
                    border = BorderStroke(
                        2.dp,
                        if (liveVoiceState.isConnected) ElectricEmerald else RoyalViolet
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (liveVoiceState.isModelSpeaking) {
                                Icons.Default.VolumeUp
                            } else {
                                Icons.Default.GraphicEq
                            },
                            contentDescription = "Live voice stream orb",
                            tint = if (liveVoiceState.isConnected) ElectricEmerald else RoyalViolet,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = liveVoiceState.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!liveVoiceState.isConnected) {
                        Button(
                            onClick = onConnectLiveVoice,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("connect_live_voice_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Start Live Voice (${GeminiMcqService.LIVE_VOICE_MODEL_ID})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onToggleMicCaptureForLive,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("live_voice_push_to_talk_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecordingMic) CrimsonCoral else PulseCyan,
                                contentColor = if (isRecordingMic) Color.White else Color(0xFF062338)
                            )
                        ) {
                            Icon(
                                imageVector = if (isRecordingMic) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isTranscribingAudio -> "Sending Audio..."
                                    isRecordingMic -> "Finish Speaking"
                                    else -> "Speak to Live AI"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onDisconnectLiveVoice,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("disconnect_live_voice_button")
                        ) {
                            Text("End")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickVoiceTopics.forEach { prompt ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!liveVoiceState.isConnected) {
                                        onConnectLiveVoice()
                                    }
                                    onSendLiveVoicePrompt(prompt)
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "🗣️ \"$prompt\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Real-time Live API Transcript Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("live_voice_transcript_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(liveVoiceState.events, key = { it.id }) { ev ->
                val isUser = ev.speaker == "YOU"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) {
                            PulseCyan.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isUser) PulseCyan else ElectricEmerald.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isUser) "🗣️ YOU" else "🔊 ${ev.speaker}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUser) PulseCyan else ElectricEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ev.transcript,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
