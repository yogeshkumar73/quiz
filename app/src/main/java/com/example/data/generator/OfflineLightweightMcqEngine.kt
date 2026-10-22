package com.example.data.generator

import com.example.data.local.McqQuestionEntity
import com.example.domain.McqSchemaValidator
import com.example.model.PydanticMcqItem
import com.example.model.SubjectCategory

/**
 * Lightweight On-Device Deterministic & Template-Driven MCQ Engine.
 * Works 100% offline with zero network dependency, providing curated questions across
 * Computer Science & Tech, General Knowledge/Science/Math, Competitive Exam Aptitude,
 * and Language Learning & Vocabulary, plus dynamic concept synthesis for custom user prompts.
 */
object OfflineLightweightMcqEngine {

    fun getSeedQuestionsEntities(): List<McqQuestionEntity> {
        val rawPairs: List<Pair<SubjectCategory, PydanticMcqItem>> = buildList {
            // ==================== 1. COMPUTER SCIENCE, PROGRAMMING & TECH ====================
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "In Pydantic v2, which method is used to serialize a BaseModel instance into a Python dictionary?",
                    options = listOf("model_dump()", "to_dict_schema()", "serialize_json()", "export_fields()"),
                    correctIndex = 0,
                    difficulty = 1,
                    subTopic = "Python & Pydantic Schema Design",
                    explanation = "Pydantic v2 replaced v1's .dict() with .model_dump() for dictionary conversion and .model_dump_json() for direct JSON serialization in Rust (pydantic-core).",
                    codeSnippet = "user = UserSchema(id=42, role='admin')\npayload = user.model_dump()"
                )
            )
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "What is the time complexity of finding the shortest paths from a single source in a weighted graph with non-negative edges using Dijkstra's algorithm with a binary heap?",
                    options = listOf("O((V + E) log V)", "O(V * E)", "O(V^2 log E)", "O(E + V)"),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Algorithms & Data Structures",
                    explanation = "With a binary min-heap priority queue, extracting the minimum takes O(log V) across V vertices and relaxing E edges takes O(E log V), yielding O((V + E) log V)."
                )
            )
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "In Jetpack Compose & Kotlin Coroutines, why is collectAsStateWithLifecycle() preferred over collectAsState() in Android UI?",
                    options = listOf(
                        "It automatically stops collecting Upstream Flows when the lifecycle drops below a target state (e.g. STARTED), saving battery and CPU",
                        "It forces all database queries to execute synchronously on the Main UI thread",
                        "It bypasses ViewModel state preservation across configuration changes",
                        "It converts cold Flows into mutable shared singletons without a CoroutineScope"
                    ),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Kotlin & Android Architecture",
                    explanation = "collectAsStateWithLifecycle() is lifecycle-aware and cancels underlying Flow collection when the app goes into the background, preventing wasted resource usage."
                )
            )
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "In OAuth 2.0, what security vulnerability does the PKCE (Proof Key for Code Exchange) extension specifically mitigate for mobile and native apps?",
                    options = listOf(
                        "Authorization code interception attacks by malicious apps registered on the same custom URI scheme",
                        "SQL injection inside the resource server's relational user table",
                        "Cross-site scripting (XSS) inside static HTML documentation pages",
                        "DNS cache poisoning on the client's local Wi-Fi router"
                    ),
                    correctIndex = 0,
                    difficulty = 3,
                    subTopic = "Cybersecurity & OAuth 2.0",
                    explanation = "PKCE binds the authorization request to the token exchange using a dynamically generated code_verifier and hashed code_challenge, preventing intercepted auth codes from being redeemed."
                )
            )
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "According to the CAP theorem and PACELC extension, what trade-off does a distributed database make even during normal operation without network partitions?",
                    options = listOf(
                        "Latency (L) versus Consistency (C)",
                        "Disk capacity versus CPU clock frequency",
                        "Schema normalization versus primary key length",
                        "Thread pool size versus garbage collection pause time"
                    ),
                    correctIndex = 0,
                    difficulty = 4,
                    subTopic = "Distributed Systems & Cloud",
                    explanation = "PACELC states that if there is a Partition (P), a system trades off Availability (A) vs Consistency (C); Else (E), during normal operation, it trades off Latency (L) vs Consistency (C)."
                )
            )
            add(
                SubjectCategory.CS_TECH to PydanticMcqItem(
                    question = "When validating nested JSON payloads in Pydantic v2, which decorator runs validation logic after all individual fields have already been parsed and validated?",
                    options = listOf(
                        "@model_validator(mode='after')",
                        "@field_serializer(when_used='always')",
                        "@computed_field(cached=True)",
                        "@BeforeValidator"
                    ),
                    correctIndex = 0,
                    difficulty = 3,
                    subTopic = "Python & Pydantic Schema Design",
                    explanation = "@model_validator(mode='after') receives the validated model instance (`self`), making it ideal for cross-field invariants like verifying `correctIndex < len(options)`.",
                    codeSnippet = "@model_validator(mode='after')\ndef check_bounds(self) -> Self:\n    assert 0 <= self.correct_index < len(self.options)\n    return self"
                )
            )

            // ==================== 2. GENERAL KNOWLEDGE, SCIENCE & MATH ====================
            add(
                SubjectCategory.SCIENCE_MATH to PydanticMcqItem(
                    question = "Which Maxwell equation mathematically expresses that there are no magnetic monopoles in classical electromagnetism?",
                    options = listOf(
                        "∇ · B = 0 (Gauss's Law for Magnetism)",
                        "∇ · E = ρ / ε₀ (Gauss's Law for Electricity)",
                        "∇ × E = -∂B/∂t (Faraday's Law of Induction)",
                        "F = q(E + v × B) (Lorentz Force Law)"
                    ),
                    correctIndex = 0,
                    difficulty = 3,
                    subTopic = "Physics & Electromagnetism",
                    explanation = "The divergence of the magnetic field B is always zero (∇ · B = 0), meaning magnetic field lines always form closed loops rather than originating at isolated point charges."
                )
            )
            add(
                SubjectCategory.SCIENCE_MATH to PydanticMcqItem(
                    question = "If two fair six-sided dice are rolled simultaneously, what is the exact probability that the sum of the upward faces equals 8?",
                    options = listOf("5 / 36", "1 / 6", "4 / 36", "7 / 36"),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Probability & Linear Algebra",
                    explanation = "There are 36 equally likely outcomes. The pairs summing to 8 are (2,6), (3,5), (4,4), (5,3), and (6,2)—which is 5 favorable outcomes out of 36."
                )
            )
            add(
                SubjectCategory.SCIENCE_MATH to PydanticMcqItem(
                    question = "In eukaryotic cells, which enzyme complex is directly responsible for synthesizing ATP driven by a proton gradient across the inner mitochondrial membrane?",
                    options = listOf("ATP Synthase (Complex V)", "RNA Polymerase II", "DNA Helicase", "Cytochrome c Oxidase"),
                    correctIndex = 0,
                    difficulty = 1,
                    subTopic = "Molecular Biology & Genetics",
                    explanation = "ATP Synthase uses the proton-motive force generated by the electron transport chain (chemiosmosis) to phosphorylate ADP into ATP."
                )
            )
            add(
                SubjectCategory.SCIENCE_MATH to PydanticMcqItem(
                    question = "For a 2×2 matrix A = [[4, 1], [2, 3]], what are the eigenvalues λ₁ and λ₂?",
                    options = listOf("λ = 5 and λ = 2", "λ = 4 and λ = 3", "λ = 6 and λ = 1", "λ = 7 and λ = 0"),
                    correctIndex = 0,
                    difficulty = 4,
                    subTopic = "Probability & Linear Algebra",
                    explanation = "The characteristic equation is det(A - λI) = (4-λ)(3-λ) - (1)(2) = λ² - 7λ + 10 = (λ - 5)(λ - 2) = 0, yielding eigenvalues 5 and 2.",
                    codeSnippet = "det([[4 - λ, 1], [2, 3 - λ]]) = λ² - 7λ + 10 = 0"
                )
            )
            add(
                SubjectCategory.SCIENCE_MATH to PydanticMcqItem(
                    question = "What astronomical boundary marks the maximum mass (~1.44 solar masses) a white dwarf star can sustain through electron degeneracy pressure before collapsing?",
                    options = listOf("Chandrasekhar Limit", "Schwarzschild Radius", "Roche Limit", "Eddington Luminosity"),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Astrophysics & Earth Science",
                    explanation = "Formulated by Subrahmanyan Chandrasekhar in 1930, stars exceeding ~1.44 solar masses overcome electron degeneracy pressure, leading to a Type Ia supernova or neutron star."
                )
            )

            // ==================== 3. COMPETITIVE EXAM PREP & APTITUDE ====================
            add(
                SubjectCategory.EXAM_APTITUDE to PydanticMcqItem(
                    question = "A train 180 meters long is running at a speed of 72 km/h. How many seconds will it take to completely cross a stationary platform 220 meters long?",
                    options = listOf("20 seconds", "15 seconds", "25 seconds", "18 seconds"),
                    correctIndex = 0,
                    difficulty = 1,
                    subTopic = "Speed Time & Work Problems",
                    explanation = "Convert speed: 72 km/h × (5/18) = 20 m/s. Total distance to cover = train length + platform length = 180m + 220m = 400m. Time = 400m / 20 m/s = 20 seconds."
                )
            )
            add(
                SubjectCategory.EXAM_APTITUDE to PydanticMcqItem(
                    question = "Find the next number in the logical progression: 3, 7, 15, 31, 63, ?",
                    options = listOf("127", "125", "119", "131"),
                    correctIndex = 0,
                    difficulty = 1,
                    subTopic = "Permutations & Number Series",
                    explanation = "Each term follows the pattern T(n) = 2 * T(n-1) + 1 (or 2^(n+1) - 1). Thus 63 × 2 + 1 = 127."
                )
            )
            add(
                SubjectCategory.EXAM_APTITUDE to PydanticMcqItem(
                    question = "Pipe A can fill a reservoir in 12 hours and Pipe B can fill it in 15 hours, while drain Pipe C can empty the full reservoir in 20 hours. If all three are opened together, how long until the tank is full?",
                    options = listOf("10 hours", "8 hours", "12 hours", "9.5 hours"),
                    correctIndex = 0,
                    difficulty = 3,
                    subTopic = "Speed Time & Work Problems",
                    explanation = "Take LCM(12, 15, 20) = 60 units. Rates: A = +5 u/hr, B = +4 u/hr, C = -3 u/hr. Net rate = 5 + 4 - 3 = 6 units/hr. Total time = 60 / 6 = 10 hours."
                )
            )
            add(
                SubjectCategory.EXAM_APTITUDE to PydanticMcqItem(
                    question = "In how many distinct ways can the letters of the word 'ALGORITHM' be arranged such that the three vowels (A, O, I) always remain together?",
                    options = listOf("30,240", "362,880", "5,040", "15,120"),
                    correctIndex = 0,
                    difficulty = 4,
                    subTopic = "Permutations & Number Series",
                    explanation = "'ALGORITHM' has 9 distinct letters: 6 consonants + 3 vowels (A, O, I). Treat the 3 vowels as 1 super-block: 6 + 1 = 7 entities arranged in 7! = 5,040 ways. The 3 vowels arrange internally in 3! = 6 ways. Total = 5,040 × 6 = 30,240."
                )
            )
            add(
                SubjectCategory.EXAM_APTITUDE to PydanticMcqItem(
                    question = "Statements: All compilers are parsers. Some parsers are optimizers. Which conclusion logically follows with 100% certainty?",
                    options = listOf(
                        "Some parsers are compilers",
                        "All optimizers are compilers",
                        "No compiler is an optimizer",
                        "All parsers are compilers"
                    ),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Logical Deduction & Syllogisms",
                    explanation = "Since 'All compilers are parsers' (Universal Affirmative A-proposition), its immediate converse 'Some parsers are compilers' (I-proposition) always holds true."
                )
            )

            // ==================== 4. LANGUAGE LEARNING & VOCABULARY ====================
            add(
                SubjectCategory.LANGUAGE_VOCAB to PydanticMcqItem(
                    question = "Choose the most accurate synonym for the academic adjective 'EPHEMERAL' in the sentence: 'Trend-driven social networks often enjoy only ephemeral popularity.'",
                    options = listOf("Transitory and short-lived", "Perpetual and entrenched", "Cacophonous and harsh", "Meticulous and exhaustive"),
                    correctIndex = 0,
                    difficulty = 1,
                    subTopic = "High-Impact Academic Vocabulary",
                    explanation = "Derived from Greek 'ephemeros' (lasting only a day), 'ephemeral' means fleeting, transitory, or short-lived."
                )
            )
            add(
                SubjectCategory.LANGUAGE_VOCAB to PydanticMcqItem(
                    question = "What does the Latin root 'LOQU / LOCUT' (found in eloquent, loquacious, circumlocution) signify?",
                    options = listOf("To speak or talk", "To carry or bear", "To write or engrave", "To see or observe"),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Latin & Greek Root Etymology",
                    explanation = "From Latin 'loqui' (to speak): 'loquacious' means talkative, 'eloquent' means speaking fluently, and 'circumlocution' means talking around a subject."
                )
            )
            add(
                SubjectCategory.LANGUAGE_VOCAB to PydanticMcqItem(
                    question = "Select the sentence that demonstrates impeccable parallel grammatical structure and modifier placement:",
                    options = listOf(
                        "The architecture balances offline resilience, low-latency caching, and strict schema validation.",
                        "The architecture balances offline resilience, caching with low latency, and to validate schemas strictly.",
                        "Walking into the server room, the cooling fans sounded unusually loud to the engineer.",
                        "Neither the API gateway nor the worker nodes was responding to health pings."
                    ),
                    correctIndex = 0,
                    difficulty = 3,
                    subTopic = "Syntactic Precision & Grammar",
                    explanation = "Option 1 coordinates three parallel noun phrases ('offline resilience', 'low-latency caching', 'strict schema validation'). Option 3 has a dangling participle, and Option 4 violates plural proximity agreement ('nodes were')."
                )
            )
            add(
                SubjectCategory.LANGUAGE_VOCAB to PydanticMcqItem(
                    question = "Which pair of words represents the sharpest semantic ANTONYMS?",
                    options = listOf("Laconic ↔ Garrulous", "Pragmatic ↔ Practical", "Mitigate ↔ Alleviate", "Lucid ↔ Pellucid"),
                    correctIndex = 0,
                    difficulty = 4,
                    subTopic = "Contextual Nuance & Antonyms",
                    explanation = "'Laconic' means using very few words (concise to the point of terseness), whereas 'garrulous' means excessively talkative—making them direct antonyms. The other pairs are synonyms."
                )
            )
            add(
                SubjectCategory.LANGUAGE_VOCAB to PydanticMcqItem(
                    question = "Identify the rhetorical figure of speech used in the phrase: 'The silence in the auditorium was deafening.'",
                    options = listOf("Oxymoron / Paradox", "Synecdoche", "Litotes", "Anaphora"),
                    correctIndex = 0,
                    difficulty = 2,
                    subTopic = "Rhetorical Devices & Idioms",
                    explanation = "Juxtaposing 'silence' with 'deafening' combines contradictory terms for dramatic emphasis, which defines an oxymoron/paradox."
                )
            )
        }

        return rawPairs.mapNotNull { (category, item) ->
            val validated = McqSchemaValidator.validateAndNormalize(item) ?: return@mapNotNull null
            McqQuestionEntity(
                categoryId = category.id,
                subTopic = validated.subTopic,
                difficulty = validated.difficulty,
                questionText = validated.question,
                optionsJson = McqSchemaValidator.encodeOptionsList(validated.options),
                correctIndex = validated.correctIndex,
                explanation = validated.explanation,
                codeSnippet = validated.codeSnippet,
                sourceEngine = "OFFLINE_LIGHTWEIGHT"
            )
        }
    }

    /**
     * Synthesizes fresh, schema-validated MCQs completely offline on-device for any custom topic or category
     * when the user has no internet connection or chooses Offline Lightweight Mode.
     */
    fun synthesizeOfflineBatch(
        topicPrompt: String,
        category: SubjectCategory,
        targetDifficulty: Int,
        count: Int
    ): List<PydanticMcqItem> {
        val cleanTopic = McqSchemaValidator.sanitizeInput(topicPrompt.ifBlank { category.shortTitle }, 70)
        val tier = targetDifficulty.coerceIn(1, 4)

        val templates = listOf(
            PydanticMcqItem(
                question = "When designing a resilient architecture or solution for '$cleanTopic', which principle guarantees deterministic behavior under edge-case inputs?",
                options = listOf(
                    "Enforcing strict input schema validation, explicit bounds checking, and idempotent state transitions",
                    "Relying on implicit runtime type coercion without boundary assertions",
                    "Ignoring malformed inputs and continuing execution with uninitialized state",
                    "Coupling presentation layers directly to raw unvalidated network streams"
                ),
                correctIndex = 0,
                difficulty = tier,
                subTopic = cleanTopic,
                explanation = "Across $cleanTopic and modern engineering, strict boundary validation (such as Pydantic schema contracts) and idempotent state transitions prevent cascading failures.",
                codeSnippet = "// Offline Synthesizer Schema Contract\nvalidate(topic = \"$cleanTopic\", tier = $tier)"
            ),
            PydanticMcqItem(
                question = "In an advanced evaluation of '$cleanTopic', what is the primary advantage of separating core domain invariants from external I/O dependencies?",
                options = listOf(
                    "It enables 100% deterministic offline execution, fast unit verification, and zero-latency local fallbacks",
                    "It increases network round-trip overhead for every local state read",
                    "It prevents caching layers from persisting validated domain models",
                    "It requires a constant active WebSocket connection before rendering UI"
                ),
                correctIndex = 0,
                difficulty = (tier % 4) + 1,
                subTopic = cleanTopic,
                explanation = "Decoupling core logic in '$cleanTopic' from network availability ensures seamless offline functionality and verifiable correctness."
            ),
            PydanticMcqItem(
                question = "Which metric best measures mastery progression when practicing '$cleanTopic' under adaptive difficulty scaling?",
                options = listOf(
                    "Rolling accuracy weighted by question difficulty tier and response latency (Elo Skill Rating)",
                    "Total time spent idle on the summary screen without answering items",
                    "Raw count of skipped questions in ungraded sessions",
                    "Static sequential ordering regardless of prior user accuracy"
                ),
                correctIndex = 0,
                difficulty = tier,
                subTopic = cleanTopic,
                explanation = "Adaptive learning systems combine item difficulty tier (1..4) with user accuracy and speed to dynamically calibrate challenge levels in '$cleanTopic'."
            ),
            PydanticMcqItem(
                question = "When optimizing performance and reliability in '$cleanTopic', how should transient failures or missing remote resources be handled?",
                options = listOf(
                    "Gracefully fall back to a verified local SQLite/Room cache and queue changes for background synchronization",
                    "Terminate the user session immediately and clear local progress",
                    "Block the main UI thread indefinitely until a remote server responds",
                    "Return unvalidated random placeholders without notifying the state layer"
                ),
                correctIndex = 0,
                difficulty = tier.coerceAtLeast(2),
                subTopic = cleanTopic,
                explanation = "Offline-first caching with automatic sync queuing provides uninterrupted user experience for '$cleanTopic' across both online and offline environments."
            ),
            PydanticMcqItem(
                question = "What distinguishes a Master-tier (Tier 4) analysis of '$cleanTopic' from a Foundation-tier (Tier 1) recall check?",
                options = listOf(
                    "Synthesizing multi-step trade-offs, edge cases, and quantitative constraints under time pressure",
                    "Memorizing a single isolated definition without contextual application",
                    "Selecting the longest text option without verifying underlying assumptions",
                    "Avoiding quantitative or structural verification of the problem statement"
                ),
                correctIndex = 0,
                difficulty = 4,
                subTopic = cleanTopic,
                explanation = "Higher difficulty tiers in '$cleanTopic' test multi-variable synthesis and edge-case reasoning rather than surface-level recall."
            )
        )

        return templates
            .shuffled()
            .take(count.coerceIn(1, templates.size))
            .mapNotNull { McqSchemaValidator.validateAndNormalize(it) }
    }
}
