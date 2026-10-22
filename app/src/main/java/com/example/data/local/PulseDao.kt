package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseDao {

    // --- Guest & Multi-User Profiles ---
    @Query("SELECT * FROM guest_profiles ORDER BY isActive DESC, totalXp DESC")
    fun observeAllProfiles(): Flow<List<GuestProfileEntity>>

    @Query("SELECT * FROM guest_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActiveProfile(): Flow<GuestProfileEntity?>

    @Query("SELECT * FROM guest_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): GuestProfileEntity?

    @Query("SELECT COUNT(*) FROM guest_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GuestProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: GuestProfileEntity)

    @Query("UPDATE guest_profiles SET isActive = CASE WHEN profileId = :targetId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(targetId: Long)

    // --- MCQ Question Bank & Offline Cache ---
    @Query("SELECT * FROM mcq_questions ORDER BY createdAt DESC")
    fun observeAllQuestions(): Flow<List<McqQuestionEntity>>

    @Query("SELECT * FROM mcq_questions WHERE needsRevision = 1 ORDER BY createdAt DESC")
    fun observeRevisionQuestions(): Flow<List<McqQuestionEntity>>

    @Query("SELECT COUNT(*) FROM mcq_questions")
    suspend fun getQuestionCount(): Int

    @Query("SELECT * FROM mcq_questions WHERE categoryId = :categoryId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByCategory(categoryId: String, limit: Int): List<McqQuestionEntity>

    @Query("SELECT * FROM mcq_questions WHERE categoryId = :categoryId AND difficulty = :difficulty ORDER BY timesAnswered ASC, RANDOM() LIMIT :limit")
    suspend fun getAdaptiveQuestions(categoryId: String, difficulty: Int, limit: Int): List<McqQuestionEntity>

    @Query("SELECT * FROM mcq_questions WHERE needsRevision = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRevisionQuestionsOnce(limit: Int): List<McqQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<McqQuestionEntity>)

    @Update
    suspend fun updateQuestion(question: McqQuestionEntity)

    // --- Adaptive Topic Mastery Tree ---
    @Query("SELECT * FROM topic_mastery WHERE profileId = :profileId ORDER BY masteryScore DESC")
    fun observeMasteryByProfile(profileId: Long): Flow<List<TopicMasteryEntity>>

    @Query("SELECT * FROM topic_mastery WHERE masteryKey = :key LIMIT 1")
    suspend fun getMasteryNode(key: String): TopicMasteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMasteryNode(node: TopicMasteryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMasteryNodes(nodes: List<TopicMasteryEntity>)

    // --- Real-Time & Offline Cached Leaderboards ---
    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC, accuracyPercent DESC LIMIT 40")
    fun observeLeaderboard(): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT COUNT(*) FROM leaderboard_entries")
    suspend fun getLeaderboardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntryEntity>)

    @Query("UPDATE leaderboard_entries SET isSyncedToCloud = 1 WHERE isSyncedToCloud = 0")
    suspend fun markAllLeaderboardEntriesSynced()
}
