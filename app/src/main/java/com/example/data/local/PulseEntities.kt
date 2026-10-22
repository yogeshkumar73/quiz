package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_profiles")
data class GuestProfileEntity(
    @PrimaryKey(autoGenerate = true) val profileId: Long = 0,
    val displayName: String,
    val avatarEmoji: String,
    val roleTitle: String = "Pulse Challenger",
    val isGuest: Boolean = true,
    val oauthEmail: String? = null,
    val totalXp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val eloRating: Int = 1200,
    val quizzesCompleted: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val isActive: Boolean = false,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    val overallAccuracyPercent: Int
        get() = if (totalQuestionsAnswered == 0) 0
        else ((totalCorrectAnswers * 100f) / totalQuestionsAnswered).toInt().coerceIn(0, 100)
}

@Entity(tableName = "mcq_questions")
data class McqQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val subTopic: String,
    val difficulty: Int, // 1 = Foundation, 2 = Practitioner, 3 = Specialist, 4 = Master
    val questionText: String,
    val optionsJson: String, // JSON array of 4 validated strings
    val correctIndex: Int, // 0..3
    val explanation: String,
    val codeSnippet: String? = null,
    val sourceEngine: String, // "ONLINE_GEMINI" or "OFFLINE_LIGHTWEIGHT"
    val timesAnswered: Int = 0,
    val timesCorrect: Int = 0,
    val needsRevision: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_mastery")
data class TopicMasteryEntity(
    @PrimaryKey val masteryKey: String, // "${profileId}_${categoryId}_${subTopic}"
    val profileId: Long,
    val categoryId: String,
    val subTopic: String,
    val masteryScore: Int = 25, // 0..100
    val currentDifficultyTier: Int = 1, // 1..4
    val attemptsCount: Int = 0,
    val correctCount: Int = 0,
    val unlockedBadge: String = "Initiate",
    val lastPracticedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val entryId: Long = 0,
    val profileId: Long,
    val playerName: String,
    val avatarEmoji: String,
    val score: Int,
    val maxStreak: Int,
    val accuracyPercent: Int,
    val eloRating: Int,
    val gameMode: String, // "COMPETITIVE_BLITZ", "ADAPTIVE_LEARNING", "HEAD_TO_HEAD_DUEL"
    val categoryId: String,
    val isUserEntry: Boolean = false,
    val isSyncedToCloud: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
