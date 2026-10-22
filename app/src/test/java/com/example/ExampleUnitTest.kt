package com.example

import com.example.data.generator.ChatModelTier
import com.example.data.generator.GeminiMcqService
import com.example.data.generator.OfflineLightweightMcqEngine
import com.example.data.generator.TutorPersonaRole
import com.example.domain.McqSchemaValidator
import com.example.model.PydanticMcqItem
import com.example.model.SubjectCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun pydanticSchemaValidator_rejectsMalformedAndNormalizesValidItems() {
        val malformed = PydanticMcqItem(
            question = "Short",
            options = listOf("OnlyOne"),
            correctIndex = 99,
            difficulty = 8,
            explanation = ""
        )
        assertNull(McqSchemaValidator.validateAndNormalize(malformed))

        val valid = PydanticMcqItem(
            question = "What is the primary purpose of Pydantic BaseModel validation?",
            options = listOf(
                "Runtime data parsing and strict schema enforcement",
                "Hardware GPU fan control",
                "CSS stylesheet minification",
                "SMTP email relay routing"
            ),
            correctIndex = 0,
            difficulty = 3,
            subTopic = "Python & Pydantic",
            explanation = "Pydantic enforces strict runtime schema invariants."
        )
        val normalized = McqSchemaValidator.validateAndNormalize(valid)
        assertNotNull(normalized)
        assertEquals(4, normalized?.options?.size)
        assertEquals(0, normalized?.correctIndex)
    }

    @Test
    fun securitySanitizer_blocksXssAndSqlInjectionPayloads() {
        val xssInput = "<script>alert('hack')</script>What is OAuth 2.0 PKCE?"
        val sanitized = McqSchemaValidator.sanitizeInput(xssInput)
        assertFalse(sanitized.contains("<script>", ignoreCase = true))
        assertTrue(sanitized.contains("What is OAuth 2.0 PKCE?"))

        assertFalse(McqSchemaValidator.isTopicPromptSafe("SELECT * FROM users; DROP TABLE mcqs;--"))
        assertTrue(McqSchemaValidator.isTopicPromptSafe("Kotlin Coroutines & Flow"))
    }

    @Test
    fun offlineLightweightEngine_synthesizesValidatedQuestionsAcrossCategories() {
        val seeds = OfflineLightweightMcqEngine.getSeedQuestionsEntities()
        assertTrue(seeds.size >= 16)

        val customBatch = OfflineLightweightMcqEngine.synthesizeOfflineBatch(
            topicPrompt = "Distributed Consensus & Raft",
            category = SubjectCategory.CS_TECH,
            targetDifficulty = 3,
            count = 4
        )
        assertEquals(4, customBatch.size)
        assertTrue(customBatch.all { it.options.size == 4 && it.correctIndex in 0..3 })
    }

    @Test
    fun geminiModelsAndMultiTurnChat_supportsFastGeneralComplexAndTranscribeModels() = runBlocking {
        assertEquals("gemini-3.1-flash-lite", ChatModelTier.FAST_LITE.modelId)
        assertEquals("gemini-3.8-flash", ChatModelTier.GENERAL_FLASH.modelId)
        assertEquals("gemini-4-argon", ChatModelTier.COMPLEX_ARGON.modelId)
        assertEquals("gemini-3.5-transcribe", GeminiMcqService.TRANSCRIBE_MODEL_ID)
        assertEquals("gemini-3.8-live", GeminiMcqService.LIVE_VOICE_MODEL_ID)

        val reply = GeminiMcqService.sendMultiTurnChatMessage(
            history = emptyList(),
            newUserMessage = "Explain Pydantic v2 validation",
            modelTier = ChatModelTier.COMPLEX_ARGON,
            personaRole = TutorPersonaRole.SCHEMA_ARCHITECT,
            allowOnline = false
        )
        assertTrue(reply.text.contains("Pydantic"))
        assertFalse(reply.isUser)
    }
}
