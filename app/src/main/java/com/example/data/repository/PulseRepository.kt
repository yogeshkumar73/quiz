package com.example.data.repository

import com.example.data.generator.GeminiMcqService
import com.example.data.generator.OfflineLightweightMcqEngine
import com.example.data.local.GuestProfileEntity
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.McqQuestionEntity
import com.example.data.local.PulseDao
import com.example.data.local.TopicMasteryEntity
import com.example.domain.McqSchemaValidator
import com.example.model.DifficultyTier
import com.example.model.QuizGameMode
import com.example.model.SubjectCategory
import kotlinx.coroutines.flow.Flow

data class GenerationOutcome(
    val questions: List<McqQuestionEntity>,
    val usedOnlineAi: Boolean,
    val statusBanner: String
)

class PulseRepository(private val dao: PulseDao) {

    val allProfiles: Flow<List<GuestProfileEntity>> = dao.observeAllProfiles()
    val activeProfile: Flow<GuestProfileEntity?> = dao.observeActiveProfile()
    val allQuestions: Flow<List<McqQuestionEntity>> = dao.observeAllQuestions()
    val revisionQuestions: Flow<List<McqQuestionEntity>> = dao.observeRevisionQuestions()
    val leaderboard: Flow<List<LeaderboardEntryEntity>> = dao.observeLeaderboard()

    fun observeMasteryForProfile(profileId: Long): Flow<List<TopicMasteryEntity>> =
        dao.observeMasteryByProfile(profileId)

    suspend fun ensureSeeded() {
        val profileCount = dao.getProfileCount()
        val primaryProfileId = if (profileCount == 0) {
            val id1 = dao.insertProfile(
                GuestProfileEntity(
                    displayName = "Nova_Guest",
                    avatarEmoji = "⚡",
                    roleTitle = "Adaptive Architect",
                    isGuest = true,
                    totalXp = 1480,
                    level = 4,
                    currentStreak = 5,
                    bestStreak = 9,
                    eloRating = 1340,
                    quizzesCompleted = 7,
                    totalQuestionsAnswered = 42,
                    totalCorrectAnswers = 35,
                    isActive = true
                )
            )
            dao.insertProfile(
                GuestProfileEntity(
                    displayName = "Cipher_Player2",
                    avatarEmoji = "🦊",
                    roleTitle = "Blitz Challenger",
                    isGuest = true,
                    totalXp = 920,
                    level = 3,
                    currentStreak = 3,
                    bestStreak = 6,
                    eloRating = 1245,
                    quizzesCompleted = 4,
                    totalQuestionsAnswered = 25,
                    totalCorrectAnswers = 19,
                    isActive = false
                )
            )
            dao.insertProfile(
                GuestProfileEntity(
                    displayName = "Vortex_Scholar",
                    avatarEmoji = "🚀",
                    roleTitle = "Schema Specialist",
                    isGuest = true,
                    totalXp = 640,
                    level = 2,
                    currentStreak = 2,
                    bestStreak = 4,
                    eloRating = 1190,
                    quizzesCompleted = 3,
                    totalQuestionsAnswered = 18,
                    totalCorrectAnswers = 14,
                    isActive = false
                )
            )
            id1
        } else {
            dao.getActiveProfileOnce()?.profileId ?: 1L
        }

        if (dao.getQuestionCount() == 0) {
            val seedQuestions = OfflineLightweightMcqEngine.getSeedQuestionsEntities()
            dao.insertQuestions(seedQuestions)
            seedInitialMasteryForProfile(primaryProfileId)
        }

        if (dao.getLeaderboardCount() == 0) {
            val initialEntries = listOf(
                LeaderboardEntryEntity(
                    profileId = 901L,
                    playerName = "Aether_Prime",
                    avatarEmoji = "👑",
                    score = 2840,
                    maxStreak = 14,
                    accuracyPercent = 96,
                    eloRating = 1620,
                    gameMode = QuizGameMode.COMPETITIVE_BLITZ.id,
                    categoryId = SubjectCategory.CS_TECH.id,
                    isUserEntry = false,
                    isSyncedToCloud = true
                ),
                LeaderboardEntryEntity(
                    profileId = 902L,
                    playerName = "Pydantic_Wizard",
                    avatarEmoji = "🐍",
                    score = 2490,
                    maxStreak = 11,
                    accuracyPercent = 92,
                    eloRating = 1540,
                    gameMode = QuizGameMode.COMPETITIVE_BLITZ.id,
                    categoryId = SubjectCategory.CS_TECH.id,
                    isUserEntry = false,
                    isSyncedToCloud = true
                ),
                LeaderboardEntryEntity(
                    profileId = 903L,
                    playerName = "Quantum_Lyra",
                    avatarEmoji = "🧬",
                    score = 2180,
                    maxStreak = 9,
                    accuracyPercent = 90,
                    eloRating = 1475,
                    gameMode = QuizGameMode.ADAPTIVE_LEARNING.id,
                    categoryId = SubjectCategory.SCIENCE_MATH.id,
                    isUserEntry = false,
                    isSyncedToCloud = true
                ),
                LeaderboardEntryEntity(
                    profileId = primaryProfileId,
                    playerName = "Nova_Guest",
                    avatarEmoji = "⚡",
                    score = 1890,
                    maxStreak = 8,
                    accuracyPercent = 88,
                    eloRating = 1340,
                    gameMode = QuizGameMode.COMPETITIVE_BLITZ.id,
                    categoryId = SubjectCategory.CS_TECH.id,
                    isUserEntry = true,
                    isSyncedToCloud = true
                ),
                LeaderboardEntryEntity(
                    profileId = 904L,
                    playerName = "Lexicon_Sage",
                    avatarEmoji = "📚",
                    score = 1710,
                    maxStreak = 7,
                    accuracyPercent = 85,
                    eloRating = 1290,
                    gameMode = QuizGameMode.HEAD_TO_HEAD_DUEL.id,
                    categoryId = SubjectCategory.LANGUAGE_VOCAB.id,
                    isUserEntry = false,
                    isSyncedToCloud = true
                ),
                LeaderboardEntryEntity(
                    profileId = 905L,
                    playerName = "Aptitude_Titan",
                    avatarEmoji = "🎯",
                    score = 1540,
                    maxStreak = 6,
                    accuracyPercent = 82,
                    eloRating = 1230,
                    gameMode = QuizGameMode.COMPETITIVE_BLITZ.id,
                    categoryId = SubjectCategory.EXAM_APTITUDE.id,
                    isUserEntry = false,
                    isSyncedToCloud = true
                )
            )
            dao.insertLeaderboardEntries(initialEntries)
        }
    }

    suspend fun seedInitialMasteryForProfile(profileId: Long) {
        val defaultCategories = listOf(
            SubjectCategory.CS_TECH,
            SubjectCategory.SCIENCE_MATH,
            SubjectCategory.EXAM_APTITUDE,
            SubjectCategory.LANGUAGE_VOCAB
        )
        val nodes = mutableListOf<TopicMasteryEntity>()
        defaultCategories.forEachIndexed { catIdx, category ->
            category.defaultSubTopics.take(3).forEachIndexed { subIdx, subTopic ->
                val initialScore = (42 + (catIdx * 9 + subIdx * 11) % 45).coerceIn(25, 85)
                val tier = when {
                    initialScore >= 80 -> 4
                    initialScore >= 60 -> 3
                    initialScore >= 40 -> 2
                    else -> 1
                }
                nodes += TopicMasteryEntity(
                    masteryKey = "${profileId}_${category.id}_$subTopic",
                    profileId = profileId,
                    categoryId = category.id,
                    subTopic = subTopic,
                    masteryScore = initialScore,
                    currentDifficultyTier = tier,
                    attemptsCount = 6 + subIdx * 2,
                    correctCount = 4 + subIdx * 2,
                    unlockedBadge = DifficultyTier.fromLevel(tier).badgeName
                )
            }
        }
        dao.upsertMasteryNodes(nodes)
    }

    suspend fun createGuestProfile(name: String, emoji: String): Long {
        val cleanName = McqSchemaValidator.sanitizeInput(name, 28).ifBlank { "Guest_Challenger" }
        val newProfile = GuestProfileEntity(
            displayName = cleanName,
            avatarEmoji = emoji.ifBlank { "🎮" },
            roleTitle = "Instant Guest",
            isGuest = true,
            totalXp = 100,
            level = 1,
            currentStreak = 1,
            bestStreak = 1,
            eloRating = 1200,
            isActive = true
        )
        val newId = dao.insertProfile(newProfile)
        dao.setActiveProfile(newId)
        seedInitialMasteryForProfile(newId)
        return newId
    }

    suspend fun switchActiveProfile(profileId: Long) {
        dao.setActiveProfile(profileId)
    }

    suspend fun updateOAuthLinkForActiveProfile(email: String?, displayName: String?) {
        val active = dao.getActiveProfileOnce() ?: return
        val updated = active.copy(
            oauthEmail = email,
            isGuest = email == null,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: active.displayName,
            roleTitle = if (email != null) "OAuth 2.0 Verified" else "Instant Guest",
            lastSyncedAt = System.currentTimeMillis()
        )
        dao.updateProfile(updated)
        if (email != null) {
            dao.markAllLeaderboardEntriesSynced()
        }
    }

    suspend fun syncOfflineQueue(): Int {
        val active = dao.getActiveProfileOnce()
        if (active != null) {
            dao.updateProfile(active.copy(lastSyncedAt = System.currentTimeMillis()))
        }
        dao.markAllLeaderboardEntriesSynced()
        return dao.getQuestionCount()
    }

    /**
     * Generates and validates MCQs using either Online Gemini 3.5 Flash REST API (with Pydantic JSON schema)
     * or the On-Device Lightweight Synthesizer when offline, caching all generated items in Room.
     */
    suspend fun generateAndCacheMcqs(
        topicPrompt: String,
        category: SubjectCategory,
        targetDifficulty: Int,
        count: Int,
        allowOnlineAi: Boolean
    ): GenerationOutcome {
        val effectiveTopic = topicPrompt.trim().ifBlank {
            category.defaultSubTopics.random()
        }

        if (allowOnlineAi && GeminiMcqService.isApiKeyConfigured()) {
            val aiResult = GeminiMcqService.generateValidatedMcqs(
                topic = effectiveTopic,
                categoryTitle = category.title,
                targetDifficulty = targetDifficulty,
                count = count
            )
            val validItems = aiResult.getOrNull()
            if (!validItems.isNullOrEmpty()) {
                val entities = validItems.map { item ->
                    McqQuestionEntity(
                        categoryId = category.id,
                        subTopic = item.subTopic,
                        difficulty = item.difficulty,
                        questionText = item.question,
                        optionsJson = McqSchemaValidator.encodeOptionsList(item.options),
                        correctIndex = item.correctIndex,
                        explanation = item.explanation,
                        codeSnippet = item.codeSnippet,
                        sourceEngine = "ONLINE_GEMINI"
                    )
                }
                dao.insertQuestions(entities)
                val persisted = dao.getQuestionsByCategory(category.id, count)
                return GenerationOutcome(
                    questions = persisted.ifEmpty { entities },
                    usedOnlineAi = true,
                    statusBanner = "Generated ${entities.size} Pydantic-validated MCQs via Online Gemini 3.5 Flash & cached to Room."
                )
            }
        }

        // Seamless Offline Lightweight Engine synthesis
        val synthesized = OfflineLightweightMcqEngine.synthesizeOfflineBatch(
            topicPrompt = effectiveTopic,
            category = category,
            targetDifficulty = targetDifficulty,
            count = count
        )
        val entities = synthesized.map { item ->
            McqQuestionEntity(
                categoryId = category.id,
                subTopic = item.subTopic,
                difficulty = item.difficulty,
                questionText = item.question,
                optionsJson = McqSchemaValidator.encodeOptionsList(item.options),
                correctIndex = item.correctIndex,
                explanation = item.explanation,
                codeSnippet = item.codeSnippet,
                sourceEngine = "OFFLINE_LIGHTWEIGHT"
            )
        }
        dao.insertQuestions(entities)
        val combinedDeck = (entities + dao.getQuestionsByCategory(category.id, count))
            .distinctBy { it.questionText }
            .take(count)

        val reason = if (!allowOnlineAi) {
            "Synthesized ${combinedDeck.size} validated MCQs via On-Device Lightweight Offline Engine (0ms network latency)."
        } else {
            "Synthesized ${combinedDeck.size} validated MCQs via Offline Lightweight Engine & local Room SQLite cache."
        }
        return GenerationOutcome(
            questions = combinedDeck,
            usedOnlineAi = false,
            statusBanner = reason
        )
    }

    suspend fun buildQuizDeck(
        category: SubjectCategory,
        mode: QuizGameMode,
        startingDifficulty: Int,
        count: Int
    ): List<McqQuestionEntity> {
        if (mode == QuizGameMode.REVISION_DECK) {
            val revision = dao.getRevisionQuestionsOnce(count)
            if (revision.isNotEmpty()) return revision
        }

        val adaptiveMatches = dao.getAdaptiveQuestions(category.id, startingDifficulty, count)
        if (adaptiveMatches.size >= count) {
            return adaptiveMatches
        }

        val categoryQuestions = dao.getQuestionsByCategory(category.id, count)
        if (categoryQuestions.size >= count) {
            return categoryQuestions
        }

        // Synthesize additional offline items if custom category is sparse
        val extra = generateAndCacheMcqs(
            topicPrompt = category.shortTitle,
            category = category,
            targetDifficulty = startingDifficulty,
            count = count,
            allowOnlineAi = false
        )
        return (categoryQuestions + extra.questions).distinctBy { it.questionText }.take(count)
    }

    suspend fun toggleQuestionRevisionFlag(question: McqQuestionEntity, needsRevision: Boolean) {
        dao.updateQuestion(question.copy(needsRevision = needsRevision))
    }

    suspend fun recordQuestionOutcome(
        profileId: Long,
        question: McqQuestionEntity,
        wasCorrect: Boolean,
        responseTimeMs: Long
    ) {
        val updatedQuestion = question.copy(
            timesAnswered = question.timesAnswered + 1,
            timesCorrect = question.timesCorrect + if (wasCorrect) 1 else 0,
            needsRevision = if (!wasCorrect) true else question.needsRevision && responseTimeMs > 12_000L
        )
        dao.updateQuestion(updatedQuestion)

        val key = "${profileId}_${question.categoryId}_${question.subTopic}"
        val existingNode = dao.getMasteryNode(key)
        val oldScore = existingNode?.masteryScore ?: 35
        val delta = when {
            wasCorrect && responseTimeMs < 6_000L -> +12
            wasCorrect -> +8
            else -> -6
        }
        val newScore = (oldScore + delta).coerceIn(5, 100)
        val newTier = when {
            newScore >= 80 -> 4
            newScore >= 60 -> 3
            newScore >= 35 -> 2
            else -> 1
        }
        val updatedNode = TopicMasteryEntity(
            masteryKey = key,
            profileId = profileId,
            categoryId = question.categoryId,
            subTopic = question.subTopic,
            masteryScore = newScore,
            currentDifficultyTier = newTier,
            attemptsCount = (existingNode?.attemptsCount ?: 0) + 1,
            correctCount = (existingNode?.correctCount ?: 0) + if (wasCorrect) 1 else 0,
            unlockedBadge = DifficultyTier.fromLevel(newTier).badgeName,
            lastPracticedAt = System.currentTimeMillis()
        )
        dao.upsertMasteryNode(updatedNode)
    }

    suspend fun finalizeQuizSession(
        mode: QuizGameMode,
        category: SubjectCategory,
        totalQuestions: Int,
        correctAnswers: Int,
        earnedXp: Int,
        maxSessionStreak: Int,
        isOnline: Boolean
    ): GuestProfileEntity? {
        val active = dao.getActiveProfileOnce() ?: return null
        val accuracyPercent = if (totalQuestions > 0) {
            ((correctAnswers * 100f) / totalQuestions).toInt().coerceIn(0, 100)
        } else 0

        val eloDelta = ((accuracyPercent - 65) * 0.6f).toInt() + (maxSessionStreak * 4)
        val newElo = (active.eloRating + eloDelta).coerceIn(800, 2800)
        val newTotalXp = active.totalXp + earnedXp
        val newLevel = (1 + (newTotalXp / 400)).coerceAtLeast(1)
        val newStreak = if (accuracyPercent >= 50) active.currentStreak + 1 else 1
        val newBestStreak = maxOf(active.bestStreak, newStreak, maxSessionStreak)

        val updatedProfile = active.copy(
            totalXp = newTotalXp,
            level = newLevel,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            eloRating = newElo,
            quizzesCompleted = active.quizzesCompleted + 1,
            totalQuestionsAnswered = active.totalQuestionsAnswered + totalQuestions,
            totalCorrectAnswers = active.totalCorrectAnswers + correctAnswers,
            lastSyncedAt = System.currentTimeMillis()
        )
        dao.updateProfile(updatedProfile)

        val leaderboardScore = earnedXp + (accuracyPercent * 8) + (maxSessionStreak * 45)
        dao.insertLeaderboardEntry(
            LeaderboardEntryEntity(
                profileId = updatedProfile.profileId,
                playerName = updatedProfile.displayName,
                avatarEmoji = updatedProfile.avatarEmoji,
                score = leaderboardScore,
                maxStreak = maxSessionStreak,
                accuracyPercent = accuracyPercent,
                eloRating = newElo,
                gameMode = mode.id,
                categoryId = category.id,
                isUserEntry = true,
                isSyncedToCloud = isOnline
            )
        )
        return updatedProfile
    }
}
