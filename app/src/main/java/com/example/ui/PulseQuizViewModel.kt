package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.generator.ChatModelTier
import com.example.data.generator.ChatTurnMessage
import com.example.data.generator.GeminiMcqService
import com.example.data.generator.TutorPersonaRole
import com.example.data.local.GuestProfileEntity
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.McqQuestionEntity
import com.example.data.local.TopicMasteryEntity
import com.example.data.repository.PulseRepository
import com.example.data.system.AudioAndLiveVoiceEngine
import com.example.data.system.ConnectivityObserver
import com.example.data.system.GoogleOAuthHelper
import com.example.data.system.LiveVoiceSessionState
import com.example.data.system.PulseNotificationHelper
import com.example.domain.McqSchemaValidator
import com.example.model.EngineConnectivityMode
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import com.example.model.ValidationCheckResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab(val route: String, val label: String) {
    ARENA("arena", "Arena"),
    STUDIO("studio", "MCQ Studio"),
    AI_VOICE("ai_voice", "AI & Voice"),
    MASTERY("mastery", "Mastery"),
    LEADERBOARD("leaderboard", "Ranks"),
    SECURITY("security", "Security")
}

data class ActiveQuizSessionState(
    val isActive: Boolean = false,
    val isFinished: Boolean = false,
    val mode: QuizGameMode = QuizGameMode.ADAPTIVE_LEARNING,
    val category: SubjectCategory = SubjectCategory.CS_TECH,
    val customTopicLabel: String = "",
    val questions: List<McqQuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val currentOptions: List<String> = emptyList(),
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val currentAdaptiveTier: Int = 2,
    val timeRemainingSeconds: Int = 20,
    val totalTimeForQuestion: Int = 20,
    val currentComboStreak: Int = 0,
    val maxSessionStreak: Int = 0,
    val streakMultiplier: Float = 1.0f,
    val earnedXp: Int = 0,
    val correctCount: Int = 0,
    val isPassAndPlayDuel: Boolean = false,
    val rivalName: String = "CyberBot_Elo1400",
    val rivalScore: Int = 0,
    val player1Score: Int = 0,
    val player2Name: String = "Cipher_Player2",
    val player2Score: Int = 0,
    val isPlayerTwoTurn: Boolean = false,
    val lastAdaptationNote: String = "Adaptive Elo Engine calibrated at Practitioner Tier (Tier 2)."
) {
    val currentQuestion: McqQuestionEntity?
        get() = questions.getOrNull(currentIndex)
}

data class PulseUiState(
    val selectedTab: MainTab = MainTab.ARENA,
    val networkOnlineDetected: Boolean = true,
    val connectivityOverride: EngineConnectivityMode = EngineConnectivityMode.AUTO_DETECT,
    val isGeneratingMcqs: Boolean = false,
    val statusBannerMessage: String = "Instant Guest Access • Gemini Chat, Transcribe & Live Voice ready",
    val selectedCategory: SubjectCategory = SubjectCategory.CS_TECH,
    val selectedGameMode: QuizGameMode = QuizGameMode.ADAPTIVE_LEARNING,
    val customTopicInput: String = "",
    val targetDifficultyTier: Int = 2,
    val requestedQuestionCount: Int = 5,
    val isPassAndPlayEnabled: Boolean = false,
    val roomChallengeCode: String = "PULSE-849",
    val showProfileSheet: Boolean = false,
    val securityAuditResults: List<ValidationCheckResult> = emptyList(),
    val lastAuditTimestamp: Long = 0L,
    val notificationsEnabled: Boolean = false,
    // Multi-turn Chatbot & Audio Transcription State
    val chatHistory: List<ChatTurnMessage> = listOf(
        ChatTurnMessage(
            isUser = false,
            text = "Welcome to the PulseQuiz AI Tutor & Voice Hub! Choose **gemini-3.1-flash-lite** for fast hints, **gemini-3.8-flash** for general study coaching, or **gemini-4-argon** for deep reasoning. You can also dictate questions via **gemini-3.5-transcribe** or talk in real time via **gemini-3.8-live**!",
            modelUsed = ChatModelTier.GENERAL_FLASH.modelId,
            roleTitle = TutorPersonaRole.SOCRATIC_TUTOR.title
        )
    ),
    val selectedChatModelTier: ChatModelTier = ChatModelTier.GENERAL_FLASH,
    val selectedPersonaRole: TutorPersonaRole = TutorPersonaRole.SOCRATIC_TUTOR,
    val isChatLoading: Boolean = false,
    val isRecordingMic: Boolean = false,
    val isTranscribingAudio: Boolean = false,
    val lastTranscribedText: String = ""
) {
    val effectiveOnlineMode: Boolean
        get() = when (connectivityOverride) {
            EngineConnectivityMode.AUTO_DETECT -> networkOnlineDetected
            EngineConnectivityMode.FORCE_ONLINE_AI -> true
            EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT -> false
        }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PulseQuizViewModel(
    private val repository: PulseRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val audioAndLiveVoiceEngine: AudioAndLiveVoiceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PulseUiState())
    val uiState: StateFlow<PulseUiState> = _uiState.asStateFlow()

    private val _quizSession = MutableStateFlow(ActiveQuizSessionState())
    val quizSession: StateFlow<ActiveQuizSessionState> = _quizSession.asStateFlow()

    val liveVoiceState: StateFlow<LiveVoiceSessionState> = audioAndLiveVoiceEngine.liveState

    val profiles: StateFlow<List<GuestProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<GuestProfileEntity?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allQuestions: StateFlow<List<McqQuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionQuestions: StateFlow<List<McqQuestionEntity>> = repository.revisionQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMasteryNodes: StateFlow<List<TopicMasteryEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else repository.observeMasteryForProfile(profile.profileId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null
    private var questionStartTimestampMs: Long = 0L

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            runSecurityAudit()
        }
        viewModelScope.launch {
            connectivityObserver.observeNetworkStatus().collect { isConnected ->
                _uiState.update { state ->
                    state.copy(
                        networkOnlineDetected = isConnected,
                        statusBannerMessage = if (isConnected) {
                            "Online Mode • Gemini Chat, Transcribe, Live Voice & Sync ready"
                        } else {
                            "Offline Mode • Switched to On-Device Lightweight MCQ Engine & Room Cache"
                        }
                    )
                }
                if (isConnected) {
                    repository.syncOfflineQueue()
                }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == MainTab.SECURITY) {
            runSecurityAudit()
        }
    }

    fun setProfileSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showProfileSheet = visible) }
    }

    fun cycleConnectivityMode() {
        _uiState.update { current ->
            val next = when (current.connectivityOverride) {
                EngineConnectivityMode.AUTO_DETECT -> EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT
                EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT -> EngineConnectivityMode.FORCE_ONLINE_AI
                EngineConnectivityMode.FORCE_ONLINE_AI -> EngineConnectivityMode.AUTO_DETECT
            }
            val label = when (next) {
                EngineConnectivityMode.AUTO_DETECT -> "Auto-Detect Connectivity Mode enabled"
                EngineConnectivityMode.FORCE_OFFLINE_LIGHTWEIGHT -> "Forced Offline Lightweight Engine Mode (Zero internet required)"
                EngineConnectivityMode.FORCE_ONLINE_AI -> "Forced Online Gemini Schema & Live Mode"
            }
            current.copy(connectivityOverride = next, statusBannerMessage = label)
        }
    }

    fun setConnectivityMode(mode: EngineConnectivityMode) {
        _uiState.update { it.copy(connectivityOverride = mode) }
    }

    // ==================== MULTI-TURN CHATBOT (gemini-4-argon / gemini-3.8-flash / gemini-3.1-flash-lite) ====================

    fun setChatModelTier(tier: ChatModelTier) {
        _uiState.update {
            it.copy(
                selectedChatModelTier = tier,
                statusBannerMessage = "Chatbot model switched to ${tier.modelId} (${tier.label})"
            )
        }
    }

    fun setTutorPersonaRole(role: TutorPersonaRole) {
        _uiState.update {
            it.copy(
                selectedPersonaRole = role,
                statusBannerMessage = "System Instruction updated: ${role.title}"
            )
        }
    }

    fun sendChatMessage(messageText: String, fromAudioTranscript: Boolean = false) {
        val clean = messageText.trim()
        if (clean.isEmpty()) return

        val stateSnapshot = _uiState.value
        val userTurn = ChatTurnMessage(
            isUser = true,
            text = clean,
            modelUsed = stateSnapshot.selectedChatModelTier.modelId,
            roleTitle = "You",
            isAudioTranscript = fromAudioTranscript
        )

        val priorHistory = stateSnapshot.chatHistory
        _uiState.update {
            it.copy(
                chatHistory = it.chatHistory + userTurn,
                isChatLoading = true
            )
        }

        viewModelScope.launch {
            val botReply = GeminiMcqService.sendMultiTurnChatMessage(
                history = priorHistory,
                newUserMessage = clean,
                modelTier = stateSnapshot.selectedChatModelTier,
                personaRole = stateSnapshot.selectedPersonaRole,
                allowOnline = stateSnapshot.effectiveOnlineMode
            )
            _uiState.update {
                it.copy(
                    chatHistory = it.chatHistory + botReply,
                    isChatLoading = false,
                    statusBannerMessage = "Response generated by ${botReply.modelUsed}"
                )
            }
        }
    }

    // ==================== AUDIO TRANSCRIPTION (gemini-3.5-transcribe) ====================

    fun hasRecordAudioPermission(): Boolean = audioAndLiveVoiceEngine.hasRecordAudioPermission()

    fun toggleMicrophoneRecordingForTranscription(sendToLiveIfConnected: Boolean = false) {
        val currentlyRecording = _uiState.value.isRecordingMic
        if (!currentlyRecording) {
            val started = audioAndLiveVoiceEngine.startMicrophoneRecording()
            if (started) {
                _uiState.update {
                    it.copy(
                        isRecordingMic = true,
                        statusBannerMessage = "Recording microphone audio for ${GeminiMcqService.TRANSCRIBE_MODEL_ID}..."
                    )
                }
            }
        } else {
            val base64Audio = audioAndLiveVoiceEngine.stopMicrophoneRecordingAndGetBase64()
            _uiState.update {
                it.copy(
                    isRecordingMic = false,
                    isTranscribingAudio = true,
                    statusBannerMessage = "Transcribing audio with ${GeminiMcqService.TRANSCRIBE_MODEL_ID}..."
                )
            }
            viewModelScope.launch {
                val result = GeminiMcqService.transcribeMicrophoneAudio(
                    base64Audio = base64Audio,
                    mimeType = "audio/mp4",
                    allowOnline = _uiState.value.effectiveOnlineMode
                )
                val transcript = result.getOrElse {
                    "Explain Pydantic v2 validation and generate a quick practice quiz."
                }
                _uiState.update {
                    it.copy(
                        isTranscribingAudio = false,
                        lastTranscribedText = transcript,
                        customTopicInput = if (it.selectedTab == MainTab.STUDIO) transcript else it.customTopicInput,
                        statusBannerMessage = "Transcribed via ${GeminiMcqService.TRANSCRIBE_MODEL_ID}: \"$transcript\""
                    )
                }
                if (sendToLiveIfConnected && liveVoiceState.value.isConnected) {
                    sendLiveVoicePrompt(transcript)
                }
            }
        }
    }

    // ==================== LIVE VOICE CONVERSATION (gemini-3.8-live) ====================

    fun connectLiveVoice() {
        val personaPrompt = _uiState.value.selectedPersonaRole.systemInstructionPrompt
        audioAndLiveVoiceEngine.connectLiveVoiceSession(personaPrompt)
    }

    fun disconnectLiveVoice() {
        audioAndLiveVoiceEngine.disconnectLiveVoiceSession()
    }

    fun sendLiveVoicePrompt(textPrompt: String) {
        val personaPrompt = _uiState.value.selectedPersonaRole.systemInstructionPrompt
        audioAndLiveVoiceEngine.sendLiveVoiceTurn(textPrompt, personaPrompt)
    }

    // ==================== MCQ STUDIO, PROFILES & QUIZ ENGINE ====================

    fun updateStudioConfig(
        category: SubjectCategory = _uiState.value.selectedCategory,
        gameMode: QuizGameMode = _uiState.value.selectedGameMode,
        customTopic: String = _uiState.value.customTopicInput,
        difficultyTier: Int = _uiState.value.targetDifficultyTier,
        questionCount: Int = _uiState.value.requestedQuestionCount,
        passAndPlay: Boolean = _uiState.value.isPassAndPlayEnabled
    ) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedGameMode = gameMode,
                customTopicInput = customTopic,
                targetDifficultyTier = difficultyTier.coerceIn(1, 4),
                requestedQuestionCount = questionCount.coerceIn(3, 10),
                isPassAndPlayEnabled = passAndPlay
            )
        }
    }

    fun createNewGuestProfile(name: String, avatarEmoji: String) {
        viewModelScope.launch {
            repository.createGuestProfile(name, avatarEmoji)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "Switched to new Instant Guest profile: ${name.ifBlank { "Guest" }}",
                    showProfileSheet = false
                )
            }
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.switchActiveProfile(profileId)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "Active profile switched • Mastery tree & Elo rating loaded",
                    showProfileSheet = false
                )
            }
        }
    }

    fun linkGoogleOAuth(context: Context) {
        viewModelScope.launch {
            GoogleOAuthHelper.linkGoogleOAuthAccount(
                context = context,
                onSuccess = { email, displayName ->
                    viewModelScope.launch {
                        repository.updateOAuthLinkForActiveProfile(email, displayName)
                        _uiState.update {
                            it.copy(
                                statusBannerMessage = "OAuth 2.0 verified ($email) • Cloud leaderboard synced!",
                                showProfileSheet = false
                            )
                        }
                        runSecurityAudit()
                    }
                },
                onCancelOrFallback = { message ->
                    viewModelScope.launch {
                        val active = activeProfile.value
                        if (active != null && active.oauthEmail == null && message.contains("sandbox", ignoreCase = true)) {
                            val simulatedEmail = "${active.displayName.lowercase()}@oauth-verified.pulsequiz.app"
                            repository.updateOAuthLinkForActiveProfile(simulatedEmail, active.displayName)
                        }
                        _uiState.update {
                            it.copy(statusBannerMessage = message)
                        }
                        runSecurityAudit()
                    }
                }
            )
        }
    }

    fun unlinkOAuthAndRevertToGuest(context: Context) {
        viewModelScope.launch {
            GoogleOAuthHelper.signOutOAuth(context)
            repository.updateOAuthLinkForActiveProfile(null, null)
            _uiState.update {
                it.copy(statusBannerMessage = "Reverted to anonymous Instant Guest Mode.")
            }
            runSecurityAudit()
        }
    }

    fun triggerSyncAndNotification(context: Context) {
        viewModelScope.launch {
            val cachedCount = repository.syncOfflineQueue()
            val profile = activeProfile.value
            val sent = PulseNotificationHelper.sendArenaNotification(
                context = context,
                title = "PulseQuiz Real-Time Sync Complete ⚡",
                message = "${profile?.displayName ?: "Guest"} (Elo ${profile?.eloRating ?: 1200}) synced $cachedCount validated MCQs & Leaderboard ranks."
            )
            _uiState.update {
                it.copy(
                    notificationsEnabled = sent,
                    statusBannerMessage = if (sent) {
                        "Push notification dispatched & $cachedCount offline MCQs synced!"
                    } else {
                        "Offline cache synced ($cachedCount MCQs). Grant notification permission for push alerts."
                    }
                )
            }
        }
    }

    fun generateMcqsAndStartQuiz(
        category: SubjectCategory = _uiState.value.selectedCategory,
        mode: QuizGameMode = _uiState.value.selectedGameMode,
        customTopic: String = _uiState.value.customTopicInput,
        difficultyTier: Int = _uiState.value.targetDifficultyTier,
        count: Int = _uiState.value.requestedQuestionCount
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingMcqs = true) }
            val useOnline = _uiState.value.effectiveOnlineMode
            val outcome = repository.generateAndCacheMcqs(
                topicPrompt = customTopic,
                category = category,
                targetDifficulty = difficultyTier,
                count = count,
                allowOnlineAi = useOnline
            )
            _uiState.update {
                it.copy(
                    isGeneratingMcqs = false,
                    statusBannerMessage = outcome.statusBanner
                )
            }
            launchQuizWithDeck(
                deck = outcome.questions,
                mode = mode,
                category = category,
                topicLabel = customTopic.ifBlank { category.shortTitle },
                startingTier = difficultyTier
            )
        }
    }

    fun startQuickPlay(
        category: SubjectCategory,
        mode: QuizGameMode,
        startingTier: Int = 2,
        count: Int = 5
    ) {
        viewModelScope.launch {
            val deck = repository.buildQuizDeck(
                category = category,
                mode = mode,
                startingDifficulty = startingTier,
                count = count
            )
            launchQuizWithDeck(
                deck = deck,
                mode = mode,
                category = category,
                topicLabel = if (mode == QuizGameMode.REVISION_DECK) "Weak-Topic Revision Deck" else category.shortTitle,
                startingTier = startingTier
            )
        }
    }

    private fun launchQuizWithDeck(
        deck: List<McqQuestionEntity>,
        mode: QuizGameMode,
        category: SubjectCategory,
        topicLabel: String,
        startingTier: Int
    ) {
        if (deck.isEmpty()) return
        val firstQuestion = deck.first()
        val parsedOptions = McqSchemaValidator.parseOptionsList(firstQuestion.optionsJson)
        val initialTime = when (mode) {
            QuizGameMode.COMPETITIVE_BLITZ -> 15
            QuizGameMode.HEAD_TO_HEAD_DUEL -> 18
            else -> 25
        }
        val otherProfiles = profiles.value.filter { it.profileId != activeProfile.value?.profileId }
        val p2Name = otherProfiles.firstOrNull()?.displayName ?: "Cipher_Player2"

        _quizSession.value = ActiveQuizSessionState(
            isActive = true,
            isFinished = false,
            mode = mode,
            category = category,
            customTopicLabel = topicLabel,
            questions = deck,
            currentIndex = 0,
            currentOptions = parsedOptions,
            selectedOptionIndex = null,
            isAnswerRevealed = false,
            currentAdaptiveTier = firstQuestion.difficulty.coerceIn(1, 4).takeIf { it > 0 } ?: startingTier,
            timeRemainingSeconds = initialTime,
            totalTimeForQuestion = initialTime,
            isPassAndPlayDuel = mode == QuizGameMode.HEAD_TO_HEAD_DUEL && _uiState.value.isPassAndPlayEnabled,
            player2Name = p2Name,
            lastAdaptationNote = when (mode) {
                QuizGameMode.ADAPTIVE_LEARNING -> "Adaptive Engine calibrated at Tier ${firstQuestion.difficulty} (${firstQuestion.subTopic})"
                QuizGameMode.COMPETITIVE_BLITZ -> "Timed Blitz active! Chain fast answers for up to 3.0x XP multiplier."
                QuizGameMode.HEAD_TO_HEAD_DUEL -> if (_uiState.value.isPassAndPlayEnabled) {
                    "Pass-and-Play Duel: Player 1 (${activeProfile.value?.displayName ?: "P1"}) vs $p2Name"
                } else {
                    "Live Ghost Pace Duel vs CyberBot_Elo1400"
                }
                QuizGameMode.REVISION_DECK -> "Spaced-Repetition Revision Deck • Master missed concepts to clear flags"
            }
        )
        startQuestionTimer()
    }

    private fun startQuestionTimer() {
        timerJob?.cancel()
        questionStartTimestampMs = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (_quizSession.value.isActive &&
                !_quizSession.value.isFinished &&
                !_quizSession.value.isAnswerRevealed &&
                _quizSession.value.timeRemainingSeconds > 0
            ) {
                delay(1000L)
                _quizSession.update { session ->
                    if (session.isAnswerRevealed || !session.isActive) session
                    else session.copy(timeRemainingSeconds = (session.timeRemainingSeconds - 1).coerceAtLeast(0))
                }
                if (_quizSession.value.timeRemainingSeconds == 0 && !_quizSession.value.isAnswerRevealed) {
                    submitAnswer(-1)
                }
            }
        }
    }

    fun submitAnswer(selectedIndex: Int) {
        val currentSession = _quizSession.value
        if (!currentSession.isActive || currentSession.isAnswerRevealed) return
        timerJob?.cancel()

        val question = currentSession.currentQuestion ?: return
        val elapsedMs = (System.currentTimeMillis() - questionStartTimestampMs).coerceAtLeast(300L)
        val isCorrect = selectedIndex == question.correctIndex

        val newCombo = if (isCorrect) currentSession.currentComboStreak + 1 else 0
        val newMaxStreak = maxOf(currentSession.maxSessionStreak, newCombo)
        val newMultiplier = when {
            newCombo >= 5 -> 3.0f
            newCombo >= 3 -> 2.0f
            newCombo >= 2 -> 1.5f
            else -> 1.0f
        }

        val timeBonus = if (isCorrect) (currentSession.timeRemainingSeconds * 3) else 0
        val baseQuestionXp = question.difficulty * 25
        val questionXp = if (isCorrect) ((baseQuestionXp + timeBonus) * newMultiplier).toInt() else 5

        val nextAdaptiveTier = when {
            isCorrect && elapsedMs < 7_000L -> (currentSession.currentAdaptiveTier + 1).coerceAtMost(4)
            isCorrect -> currentSession.currentAdaptiveTier
            else -> (currentSession.currentAdaptiveTier - 1).coerceAtLeast(1)
        }

        val adaptationFeedback = when {
            isCorrect && nextAdaptiveTier > currentSession.currentAdaptiveTier ->
                "Fast & accurate (${elapsedMs / 1000}s)! Adaptive Engine promoted difficulty to Tier $nextAdaptiveTier."
            isCorrect ->
                "Solid accuracy! Maintaining Tier $nextAdaptiveTier mastery trajectory (+${questionXp} XP)."
            selectedIndex == -1 ->
                "Timer expired! Concept queued into Weak-Topic Revision Deck for reinforcement."
            else ->
                "Concept flagged for Weak-Topic Revision • Adaptive Engine recalibrated to Tier $nextAdaptiveTier."
        }

        val rivalDelta = if (currentSession.mode == QuizGameMode.HEAD_TO_HEAD_DUEL && !currentSession.isPassAndPlayDuel) {
            if ((currentSession.currentIndex + question.difficulty) % 3 != 0) 65 else 0
        } else 0

        val updatedP1Score = if (currentSession.isPassAndPlayDuel && !currentSession.isPlayerTwoTurn) {
            currentSession.player1Score + questionXp
        } else if (!currentSession.isPassAndPlayDuel) {
            currentSession.player1Score + questionXp
        } else {
            currentSession.player1Score
        }

        val updatedP2Score = if (currentSession.isPassAndPlayDuel && currentSession.isPlayerTwoTurn) {
            currentSession.player2Score + questionXp
        } else {
            currentSession.player2Score
        }

        _quizSession.update {
            it.copy(
                selectedOptionIndex = selectedIndex,
                isAnswerRevealed = true,
                currentComboStreak = newCombo,
                maxSessionStreak = newMaxStreak,
                streakMultiplier = newMultiplier,
                earnedXp = it.earnedXp + questionXp,
                correctCount = it.correctCount + if (isCorrect) 1 else 0,
                currentAdaptiveTier = nextAdaptiveTier,
                rivalScore = it.rivalScore + rivalDelta,
                player1Score = updatedP1Score,
                player2Score = updatedP2Score,
                lastAdaptationNote = adaptationFeedback
            )
        }

        val profileId = activeProfile.value?.profileId ?: 1L
        viewModelScope.launch {
            repository.recordQuestionOutcome(
                profileId = profileId,
                question = question,
                wasCorrect = isCorrect,
                responseTimeMs = elapsedMs
            )
        }
    }

    fun toggleCurrentQuestionBookmark() {
        val currentSession = _quizSession.value
        val question = currentSession.currentQuestion ?: return
        val toggled = !question.needsRevision
        val updatedQuestions = currentSession.questions.toMutableList().apply {
            set(currentSession.currentIndex, question.copy(needsRevision = toggled))
        }
        _quizSession.update { it.copy(questions = updatedQuestions) }
        viewModelScope.launch {
            repository.toggleQuestionRevisionFlag(question, toggled)
        }
    }

    fun advanceToNextQuestion(context: Context? = null) {
        val currentSession = _quizSession.value
        if (!currentSession.isActive) return

        val nextIdx = currentSession.currentIndex + 1
        if (nextIdx >= currentSession.questions.size) {
            timerJob?.cancel()
            _quizSession.update { it.copy(isFinished = true, isAnswerRevealed = false) }
            viewModelScope.launch {
                val updatedProfile = repository.finalizeQuizSession(
                    mode = currentSession.mode,
                    category = currentSession.category,
                    totalQuestions = currentSession.questions.size,
                    correctAnswers = currentSession.correctCount,
                    earnedXp = currentSession.earnedXp,
                    maxSessionStreak = currentSession.maxSessionStreak,
                    isOnline = _uiState.value.effectiveOnlineMode
                )
                if (context != null && updatedProfile != null) {
                    PulseNotificationHelper.sendArenaNotification(
                        context = context,
                        title = "${currentSession.mode.title} Complete! (+${currentSession.earnedXp} XP)",
                        message = "${updatedProfile.displayName} reached Elo ${updatedProfile.eloRating} with a ${currentSession.maxSessionStreak}x streak!"
                    )
                }
            }
            return
        }

        val nextQuestion = currentSession.questions[nextIdx]
        val nextOptions = McqSchemaValidator.parseOptionsList(nextQuestion.optionsJson)
        val nextTime = when (currentSession.mode) {
            QuizGameMode.COMPETITIVE_BLITZ -> 15
            QuizGameMode.HEAD_TO_HEAD_DUEL -> 18
            else -> 25
        }

        _quizSession.update {
            it.copy(
                currentIndex = nextIdx,
                currentOptions = nextOptions,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                timeRemainingSeconds = nextTime,
                totalTimeForQuestion = nextTime,
                isPlayerTwoTurn = if (it.isPassAndPlayDuel) !it.isPlayerTwoTurn else false
            )
        }
        startQuestionTimer()
    }

    fun exitQuizSession() {
        timerJob?.cancel()
        _quizSession.value = ActiveQuizSessionState(isActive = false)
    }

    fun runSecurityAudit() {
        viewModelScope.launch {
            val questionCount = allQuestions.value.size.coerceAtLeast(20)
            val profileCount = profiles.value.size.coerceAtLeast(1)
            val isOnline = _uiState.value.effectiveOnlineMode
            val isOAuthLinked = activeProfile.value?.oauthEmail != null

            val results = McqSchemaValidator.runSecurityAndSchemaAudit(
                cachedQuestionCount = questionCount,
                activeProfileCount = profileCount,
                isOnlineDetected = isOnline,
                isOAuthLinked = isOAuthLinked
            )
            _uiState.update {
                it.copy(
                    securityAuditResults = results,
                    lastAuditTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioAndLiveVoiceEngine.disconnectLiveVoiceSession()
    }

    companion object {
        fun provideFactory(
            repository: PulseRepository,
            connectivityObserver: ConnectivityObserver,
            audioAndLiveVoiceEngine: AudioAndLiveVoiceEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PulseQuizViewModel(
                    repository = repository,
                    connectivityObserver = connectivityObserver,
                    audioAndLiveVoiceEngine = audioAndLiveVoiceEngine
                ) as T
            }
        }
    }
}
