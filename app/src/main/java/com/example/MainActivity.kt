package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PulseDatabase
import com.example.data.repository.PulseRepository
import com.example.data.system.AudioAndLiveVoiceEngine
import com.example.data.system.ConnectivityObserver
import com.example.data.system.PulseNotificationHelper
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import com.example.ui.MainTab
import com.example.ui.PulseQuizViewModel
import com.example.ui.components.MultiUserProfileAndOAuthSheet
import com.example.ui.components.PulseTopArenaBar
import com.example.ui.screens.AdaptiveMasteryScreen
import com.example.ui.screens.AiVoiceAndChatScreen
import com.example.ui.screens.ArenaHomeScreen
import com.example.ui.screens.McqStudioScreen
import com.example.ui.screens.QuizSessionScreen
import com.example.ui.screens.RealTimeLeaderboardScreen
import com.example.ui.screens.SecurityAuditScreen
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PulseNotificationHelper.ensureChannelCreated(this)

        val database = PulseDatabase.getInstance(applicationContext)
        val repository = PulseRepository(database.pulseDao())
        val connectivityObserver = ConnectivityObserver(applicationContext)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val appScope = rememberCoroutineScope()
                val audioAndLiveEngine = remember {
                    AudioAndLiveVoiceEngine(applicationContext, appScope)
                }
                val quizViewModel: PulseQuizViewModel = viewModel(
                    factory = PulseQuizViewModel.provideFactory(
                        repository = repository,
                        connectivityObserver = connectivityObserver,
                        audioAndLiveVoiceEngine = audioAndLiveEngine
                    )
                )
                PulseQuizAppRoot(viewModel = quizViewModel)
            }
        }
    }
}

@Composable
fun PulseQuizAppRoot(viewModel: PulseQuizViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val quizSession by viewModel.quizSession.collectAsStateWithLifecycle()
    val liveVoiceState by viewModel.liveVoiceState.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
    val revisionQuestions by viewModel.revisionQuestions.collectAsStateWithLifecycle()
    val masteryNodes by viewModel.activeMasteryNodes.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.triggerSyncAndNotification(context)
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleMicrophoneRecordingForTranscription(
                sendToLiveIfConnected = liveVoiceState.isConnected
            )
        }
    }

    val handleMicToggle = {
        if (!viewModel.hasRecordAudioPermission()) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.toggleMicrophoneRecordingForTranscription(
                sendToLiveIfConnected = liveVoiceState.isConnected
            )
        }
    }

    val handleSyncAndNotify = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PulseNotificationHelper.hasNotificationPermission(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.triggerSyncAndNotification(context)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!quizSession.isActive) {
                    PulseTopArenaBar(
                        activeProfile = activeProfile,
                        effectiveOnline = uiState.effectiveOnlineMode,
                        connectivityOverride = uiState.connectivityOverride,
                        statusBanner = uiState.statusBannerMessage,
                        onProfileClick = { viewModel.setProfileSheetVisible(true) },
                        onToggleConnectivityMode = { viewModel.cycleConnectivityMode() },
                        onSyncAndNotifyClick = handleSyncAndNotify
                    )
                }
            },
            bottomBar = {
                if (!quizSession.isActive) {
                    NavigationBar(
                        containerColor = SlateSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .testTag("main_bottom_navigation")
                    ) {
                        MainTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                MainTab.ARENA -> Icons.Default.Home
                                MainTab.STUDIO -> Icons.Default.AutoAwesome
                                MainTab.AI_VOICE -> Icons.Default.RecordVoiceOver
                                MainTab.MASTERY -> Icons.Default.Psychology
                                MainTab.LEADERBOARD -> Icons.Default.EmojiEvents
                                MainTab.SECURITY -> Icons.Default.VerifiedUser
                            }
                            NavigationBarItem(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElectricEmerald,
                                    selectedTextColor = ElectricEmerald,
                                    indicatorColor = ElectricEmerald.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (quizSession.isActive) {
                    QuizSessionScreen(
                        session = quizSession,
                        activePlayerName = activeProfile?.displayName ?: "Nova_Guest",
                        onSelectOption = { idx -> viewModel.submitAnswer(idx) },
                        onToggleBookmark = { viewModel.toggleCurrentQuestionBookmark() },
                        onNextQuestion = { viewModel.advanceToNextQuestion(context) },
                        onReplayQuiz = {
                            viewModel.startQuickPlay(
                                category = quizSession.category,
                                mode = quizSession.mode,
                                startingTier = quizSession.currentAdaptiveTier
                            )
                        },
                        onExitQuiz = { viewModel.exitQuizSession() }
                    )
                } else {
                    when (uiState.selectedTab) {
                        MainTab.ARENA -> ArenaHomeScreen(
                            activeProfile = activeProfile,
                            totalCachedQuestions = allQuestions.size,
                            revisionQuestions = revisionQuestions,
                            effectiveOnline = uiState.effectiveOnlineMode,
                            isPassAndPlayEnabled = uiState.isPassAndPlayEnabled,
                            onTogglePassAndPlay = { enabled ->
                                viewModel.updateStudioConfig(passAndPlay = enabled)
                            },
                            onStartQuickPlay = { category, mode, tier ->
                                viewModel.startQuickPlay(category, mode, tier)
                            },
                            onOpenStudioForCategory = { category ->
                                viewModel.updateStudioConfig(category = category)
                                viewModel.selectTab(MainTab.STUDIO)
                            }
                        )

                        MainTab.STUDIO -> McqStudioScreen(
                            uiState = uiState,
                            cachedQuestions = allQuestions,
                            onUpdateConfig = { cat, mode, topic, tier, count ->
                                viewModel.updateStudioConfig(
                                    category = cat,
                                    gameMode = mode,
                                    customTopic = topic,
                                    difficultyTier = tier,
                                    questionCount = count
                                )
                            },
                            onSetConnectivityMode = { mode ->
                                viewModel.setConnectivityMode(mode)
                            },
                            onGenerateAndPlay = {
                                viewModel.generateMcqsAndStartQuiz()
                            }
                        )

                        MainTab.AI_VOICE -> AiVoiceAndChatScreen(
                            chatHistory = uiState.chatHistory,
                            selectedModelTier = uiState.selectedChatModelTier,
                            selectedPersonaRole = uiState.selectedPersonaRole,
                            isChatLoading = uiState.isChatLoading,
                            isRecordingMic = uiState.isRecordingMic,
                            isTranscribingAudio = uiState.isTranscribingAudio,
                            lastTranscribedText = uiState.lastTranscribedText,
                            liveVoiceState = liveVoiceState,
                            onSelectModelTier = { tier -> viewModel.setChatModelTier(tier) },
                            onSelectPersonaRole = { role -> viewModel.setTutorPersonaRole(role) },
                            onSendChatMessage = { text -> viewModel.sendChatMessage(text) },
                            onToggleMicTranscribe = handleMicToggle,
                            onGenerateQuizFromText = { topicText ->
                                viewModel.updateStudioConfig(
                                    category = SubjectCategory.CUSTOM,
                                    customTopic = topicText
                                )
                                viewModel.generateMcqsAndStartQuiz(
                                    category = SubjectCategory.CUSTOM,
                                    customTopic = topicText
                                )
                            },
                            onConnectLiveVoice = { viewModel.connectLiveVoice() },
                            onDisconnectLiveVoice = { viewModel.disconnectLiveVoice() },
                            onSendLiveVoicePrompt = { prompt -> viewModel.sendLiveVoicePrompt(prompt) }
                        )

                        MainTab.MASTERY -> AdaptiveMasteryScreen(
                            activeProfile = activeProfile,
                            masteryNodes = masteryNodes,
                            revisionQuestions = revisionQuestions,
                            onStartPracticeForCategory = { category, tier ->
                                viewModel.startQuickPlay(
                                    category = category,
                                    mode = QuizGameMode.ADAPTIVE_LEARNING,
                                    startingTier = tier
                                )
                            },
                            onStartRevisionDeck = {
                                viewModel.startQuickPlay(
                                    category = uiState.selectedCategory,
                                    mode = QuizGameMode.REVISION_DECK,
                                    startingTier = 2
                                )
                            }
                        )

                        MainTab.LEADERBOARD -> RealTimeLeaderboardScreen(
                            entries = leaderboard,
                            activeProfile = activeProfile,
                            effectiveOnline = uiState.effectiveOnlineMode,
                            onTriggerSyncAndNotify = handleSyncAndNotify,
                            onStartBlitzChallenge = {
                                viewModel.startQuickPlay(
                                    category = uiState.selectedCategory,
                                    mode = QuizGameMode.COMPETITIVE_BLITZ,
                                    startingTier = 3
                                )
                            }
                        )

                        MainTab.SECURITY -> SecurityAuditScreen(
                            checks = uiState.securityAuditResults,
                            activeProfile = activeProfile,
                            onReRunSecurityAudit = { viewModel.runSecurityAudit() },
                            onOpenProfileOAuthSheet = { viewModel.setProfileSheetVisible(true) }
                        )
                    }
                }
            }
        }

        if (uiState.showProfileSheet) {
            MultiUserProfileAndOAuthSheet(
                profiles = profiles,
                activeProfile = activeProfile,
                onDismiss = { viewModel.setProfileSheetVisible(false) },
                onSelectProfile = { profileId -> viewModel.switchProfile(profileId) },
                onCreateGuestProfile = { name, emoji ->
                    viewModel.createNewGuestProfile(name, emoji)
                },
                onLinkGoogleOAuth = { viewModel.linkGoogleOAuth(context) },
                onUnlinkOAuth = { viewModel.unlinkOAuthAndRevertToGuest(context) }
            )
        }
    }
}
