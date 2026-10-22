package com.example.model

import kotlinx.serialization.Serializable

enum class SubjectCategory(
    val id: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val badgeIcon: String,
    val defaultSubTopics: List<String>
) {
    CS_TECH(
        id = "CS_TECH",
        title = "Computer Science, Programming & Tech",
        shortTitle = "CS & Tech",
        subtitle = "Algorithms, Kotlin, Python/Pydantic, System Design & AI",
        badgeIcon = "💻",
        defaultSubTopics = listOf(
            "Algorithms & Data Structures",
            "Kotlin & Android Architecture",
            "Python & Pydantic Schema Design",
            "Distributed Systems & Cloud",
            "Cybersecurity & OAuth 2.0"
        )
    ),
    SCIENCE_MATH(
        id = "SCIENCE_MATH",
        title = "General Knowledge, Science & Math",
        shortTitle = "Science & Math",
        subtitle = "Physics, Calculus, Discrete Math, Astronomy & Biology",
        badgeIcon = "🧬",
        defaultSubTopics = listOf(
            "Physics & Electromagnetism",
            "Probability & Linear Algebra",
            "Molecular Biology & Genetics",
            "Astrophysics & Earth Science",
            "World History & Innovations"
        )
    ),
    EXAM_APTITUDE(
        id = "EXAM_APTITUDE",
        title = "Competitive Exam Prep & Aptitude",
        shortTitle = "Aptitude & Exams",
        subtitle = "Quantitative Reasoning, Logical Puzzles & Data Interpretation",
        badgeIcon = "🎯",
        defaultSubTopics = listOf(
            "Quantitative Arithmetic",
            "Logical Deduction & Syllogisms",
            "Permutations & Number Series",
            "Critical Reasoning & Analytics",
            "Speed Time & Work Problems"
        )
    ),
    LANGUAGE_VOCAB(
        id = "LANGUAGE_VOCAB",
        title = "Language Learning & Vocabulary",
        shortTitle = "Languages & Vocab",
        subtitle = "GRE/SAT Etymology, Idioms, Grammar Precision & Semantics",
        badgeIcon = "📚",
        defaultSubTopics = listOf(
            "High-Impact Academic Vocabulary",
            "Latin & Greek Root Etymology",
            "Syntactic Precision & Grammar",
            "Contextual Nuance & Antonyms",
            "Rhetorical Devices & Idioms"
        )
    ),
    CUSTOM(
        id = "CUSTOM",
        title = "Custom Topic Synthesis",
        shortTitle = "Custom Topic",
        subtitle = "On-demand schema-validated MCQs for any custom subject",
        badgeIcon = "⚡",
        defaultSubTopics = listOf("Custom User Prompt")
    );

    companion object {
        fun fromId(id: String): SubjectCategory =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: CS_TECH
    }
}

enum class DifficultyTier(
    val level: Int,
    val label: String,
    val xpReward: Int,
    val timeSeconds: Int,
    val badgeName: String
) {
    FOUNDATION(1, "Foundation", 25, 25, "Initiate"),
    PRACTITIONER(2, "Practitioner", 45, 20, "Specialist"),
    SPECIALIST(3, "Specialist", 75, 18, "Architect"),
    MASTER(4, "Master", 110, 15, "Grandmaster");

    companion object {
        fun fromLevel(level: Int): DifficultyTier =
            entries.find { it.level == level.coerceIn(1, 4) } ?: PRACTITIONER
    }
}

enum class QuizGameMode(
    val id: String,
    val title: String,
    val description: String,
    val badge: String
) {
    ADAPTIVE_LEARNING(
        id = "ADAPTIVE_LEARNING",
        title = "Adaptive Mastery",
        description = "Dynamic Elo difficulty scaling + deep conceptual explanations",
        badge = "🧠 Adaptive"
    ),
    COMPETITIVE_BLITZ(
        id = "COMPETITIVE_BLITZ",
        title = "Competitive Blitz",
        description = "Timed rapid-fire rounds with 3x combo multipliers & live leaderboard",
        badge = "⚡ Timed Blitz"
    ),
    HEAD_TO_HEAD_DUEL(
        id = "HEAD_TO_HEAD_DUEL",
        title = "Head-to-Head Room",
        description = "Duel a live ghost rival or local Player 2 in Pass-and-Play arena",
        badge = "⚔️ H2H Duel"
    ),
    REVISION_DECK(
        id = "REVISION_DECK",
        title = "Weak-Topic Revision",
        description = "Targeted spaced-repetition practice on previously missed concepts",
        badge = "🛠️ Revision"
    )
}

enum class EngineConnectivityMode {
    AUTO_DETECT,
    FORCE_ONLINE_AI,
    FORCE_OFFLINE_LIGHTWEIGHT
}

@Serializable
data class PydanticMcqItem(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val difficulty: Int = 2,
    val subTopic: String = "Core Concept",
    val explanation: String,
    val codeSnippet: String? = null
)

@Serializable
data class PydanticMcqBatch(
    val topic: String,
    val schemaVersion: String = "pydantic-v2-mcq-1.0",
    val items: List<PydanticMcqItem>
)

data class ValidationCheckResult(
    val checkId: String,
    val title: String,
    val category: String,
    val passed: Boolean,
    val details: String,
    val latencyMs: Long
)
