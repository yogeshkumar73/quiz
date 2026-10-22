# PulseQuiz: Gamified Adaptive & Competitive MCQ Platform

PulseQuiz is a responsive, offline-first Android learning and trivia platform that combines structured schema-validated AI question generation with an embedded lightweight offline MCQ engine, adaptive mastery progression, and competitive speed-blitz leaderboards—accessible immediately via zero-login guest profiles with optional Google OAuth 2.0 cloud synchronization.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Scope & Architectural Decisions from Phase 1**
> - **Confirmed Subject Domains**: Pre-loaded offline & online MCQ generation across **Computer Science, Programming & Tech**, **General Knowledge, Science & Math**, **Competitive Exam Prep & Aptitude**, and **Language Learning & Vocabulary**, alongside custom user-entered topic prompts.
> - **Confirmed Gamification & Learning Scope**: Full support for **Timed Blitz Rounds** (streak multipliers & XP levels), **Adaptive Mastery Tree** (skill badges & accuracy tracking), **Head-to-Head Challenge Rooms & Daily Leaderboard Tournaments**, and **Detailed Post-Quiz Explanations with Weak-Topic Revision Decks**.
> - **Recommended Default — Instant Guest Profiles + Optional Google OAuth 2.0 Sync**: Because the prompt requests both instant anonymous guest usability without login and OAuth 2.0 real-time syncing, the app launches directly into an **Instant Guest Profile** (supporting multiple local profiles and pass-and-play rooms on-device with zero login friction) while providing an optional **Google Sign-In (OAuth 2.0 via Credential Manager)** toggle in the Profile & Sync hub for cloud leaderboard synchronization.

---

## 1. Overview & Core Concept

- **What It Does**: Generates curriculum-grade Multiple Choice Questions (MCQs) on any topic using strict Pydantic-style JSON schema validation—seamlessly switching between cloud Gemini AI generation when online and an on-device lightweight deterministic template/concept engine when offline—and delivers them through two distinct modes: **Adaptive Learning** and **Competitive Challenge**.
- **Target Audience / Persona**: Students, developers, exam candidates, and trivia enthusiasts who want instant, zero-friction practice across varying network conditions with rewarding progression and competitive benchmarking.
- **Key Value**: Eliminates both login walls and connectivity dead-ends—users can jump straight into a personalized adaptive study session or a high-stakes timed blitz duel whether they are on high-speed Wi-Fi or completely offline on a commute.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Zero-Friction Instant Launch & Multi-Profile Switcher**:
   - Opens directly into the **Arena Dashboard** with an auto-provisioned Guest Profile (customizable avatar badge, callsign, XP bar, and active streak).
   - A quick-switch pill in the top bar lets multiple users on the same device switch guest profiles or link a Google account via OAuth 2.0.
   - A live **Connectivity & Engine Pill** (`Online AI Mode` vs. `Offline Lightweight Engine`) automatically reflects real-time `ConnectivityManager` status and allows manual override for testing offline mode at any time.
2. **MCQ Studio (Hybrid Online / Offline Generator)**:
   - Users pick from the 4 core domain tracks (**CS & Tech**, **Science & Math**, **Aptitude & Exams**, **Languages & Vocab**) or type any custom topic prompt, target difficulty (`Adaptive`, `Easy`, `Medium`, `Hard`), and question count.
   - **Online Mode**: Streams schema-enforced structured JSON questions via `gemini-3.5-flash`, automatically caching every generated deck into the local Room SQLite database for future offline play.
   - **Offline Mode**: Uses the on-device **Lightweight Concept & Template Synthesizer** combined with the local Room question bank to instantaneously generate fresh, non-repeating MCQs with zero network latency.
3. **Mode A — Adaptive Learning & Mastery Flow**:
   - Dynamically adjusts question difficulty after every answer based on rolling accuracy and response time (promoting from *Foundation* → *Practitioner* → *Specialist* → *Master*).
   - Displays immediate conceptual feedback, deep step-by-step explanations, and confidence tagging.
   - Missed or slow-answered questions are automatically routed into the user's **Weak-Topic Revision Deck** and visualized on an interactive **Adaptive Mastery Tree**.
4. **Mode B — Competitive Challenge & Head-to-Head Rooms**:
   - **Daily Blitz Tournament**: 60-second or 10-question rapid-fire rounds with visual countdown rings, combo streak multipliers (`1.5x`, `2x`, `3x`), and time-bonus XP.
   - **Head-to-Head Challenge Room**: Supports instant Room Code challenges, ghost-rival pace duels (racing against real/seeded rival split times per question), and local Pass-and-Play 2-player duels.
5. **Real-Time Leaderboards, Notifications & Security Audit Drawer**:
   - Live leaderboard tabs filtering by **Daily Tournament**, **All-Time XP**, and **Subject Mastery**, highlighting the active user's rank delta and sync status.
   - Built-in **Push Notification Center** using Android `NotificationManager` (`POST_NOTIFICATIONS` runtime flow) for streak alerts, daily tournament resets, and offline-to-online sync completions.
   - **Security & Schema Diagnostics Panel**: Inspects strict schema validation rules, input sanitization checks, OAuth token status, and offline cache integrity.

### Visual Identity & Theme

- **Aesthetic Direction**: **Neo-Arcade Editorial Studio** — High-contrast deep obsidian surfaces paired with electric lime, cyber cyan, and warm amber streak accents. Clean geometric cards with subtle radial atmosphere gradients (`Modifier.drawBehind`) and tactile spring animations.
- **Color Palette & Mood**:
  - `--background`: `#0B0F17` (Deep Obsidian) / Light mode `#F6F8FC` (Crisp Alabaster)
  - `--surface`: `#131B2E` (Elevated Slate Navy) with `#1E2942` subtle 1dp borders
  - `--primary`: `#10B981` (Electric Emerald — Growth, Mastery & Correct states)
  - `--secondary`: `#38BDF8` (Pulse Cyan — Adaptive AI & Online Sync indicators)
  - `--accent`: `#F59E0B` (Blitz Amber — Streak Multipliers, Trophies & Timers)
  - `--destructive`: `#F43F5E` (Crimson Coral — Missed questions & Weak-Topic alerts)
- **Typography & Hierarchy**:
  - **Display / Headers & Score Counters**: `Space Grotesk` (bundled locally via `font-util`) — bold, geometric, tabular-friendly numerals for countdown timers, XP counters, and rank badges.
  - **Body & Question Stems**: `Plus Jakarta Sans` (bundled locally via `font-util`) — high legibility for dense technical code snippets, math formulas, and multi-line MCQ options.
  - **Code & Schema Badges**: `JetBrains Mono` (bundled locally via `font-util`) — for programming MCQs and diagnostic metrics.
- **Component Styling & Layout**:
  - **Adaptive Layout**: Uses Material 3 adaptive patterns (`BoxWithConstraints` supporting both compact handheld portrait and wide tablet split-pane layouts).
  - **Edge-to-Edge Safe Areas**: Full-bleed `enableEdgeToEdge()` canvas with `WindowInsets.safeDrawing` top bar padding and `.navigationBarsPadding()` on bottom navigation and quiz action bars.
- **Interactive Feedback & Motion**:
  - Spring-animated (`spring(dampingRatio = 0.75f)`) option selection cards, animated circular timer progress rings, floating `+XP` combo popups, and instant ripple feedback on all 48dp+ touch targets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid Pydantic-Style Schema Engine (Cloud Gemini + On-Device Lightweight Synthesizer)**
  - *Chosen Approach*: Enforce a strict, deterministic Kotlin Serialization / JSON Schema contract (mirroring Pydantic's `BaseModel` validation with field constraints, option count checks, index bounds validation, and explanation completeness) across both the `gemini-3.5-flash` REST pipeline and the on-device rule-based MCQ synthesizer.
  - *Why*: Guarantees that every generated question—whether from the cloud LLM or the offline engine—has valid options, a verified answer index, difficulty metadata, and a rich explanation before ever reaching the UI or database.
  - *Alternatives Considered*: Unstructured text parsing from LLMs (rejected due to fragile regex failures) or bundling a multi-gigabyte local LLM weight file (rejected because it bloats APK size and causes OOM crashes on standard devices).
- **Decision 2: Offline-First Multi-User Guest Architecture with Optional OAuth 2.0 Sync**
  - *Chosen Approach*: Use **Room SQLite** as the single source of truth for instant anonymous guest sessions, multi-profile switching, offline MCQ caching, mastery trees, and sync queues, paired with Jetpack `CredentialManager` Google OAuth 2.0 sign-in for optional identity binding and cloud sync.
  - *Why*: Directly satisfies "no need to login and focus instead on anonymous guest access for instant usability" while preserving multi-user profiles, offline caching, and secure OAuth 2.0 capability for users who choose to connect.
  - *Alternatives Considered*: Hard login gate before app entry (rejected because it violates the user's explicit request for instant anonymous guest usability).
- **Decision 3: Adaptive Elo-Inspired Mastery Rating**
  - *Chosen Approach*: Track a per-category and global **Skill Rating (SR)** alongside question difficulty tiers (`1` to `4`). Correct fast answers boost SR and unlock harder questions; wrong answers trigger immediate remedial explanations and enqueue the concept into the Weak-Topic Revision Deck.
  - *Why*: Makes "Adaptive Learning" measurably responsive within 2–3 questions rather than feeling like a static quiz.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                     JETPACK COMPOSE UI LAYER (MVVM)                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐  │
│  │  Arena Home &    │  │ Adaptive Mastery │  │  Competitive Blitz &     │  │
│  │  MCQ Studio Hub  │  │ Tree & Revision  │  │  Head-to-Head Duel Room  │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────────┬─────────────┘  │
│           │                     │                         │                │
│  ┌────────┴─────────────────────┴─────────────────────────┴─────────────┐  │
│  │       Live Leaderboards, Profile Switcher & Security Audit Sheet     │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                STATE & DOMAIN LAYER (PulseQuizViewModel)                   │
│  • Reactive StateFlow<PulseQuizUiState> (Active Profile, Mode, Connectivity│
│    Status, Active Quiz Session, Timer & Streak Multiplier, Leaderboard)    │
│  • Pydantic-Style Schema Validator & Sanitizer (McqSchemaValidator)        │
│  • Adaptive Elo Engine (Difficulty Scaling & Weak-Topic Deck Router)       │
└───────────────┬───────────────────────────────────────────┬────────────────┘
                ▼                                           ▼
┌───────────────────────────────────────┐   ┌────────────────────────────────┐
│   HYBRID MCQ GENERATION ENGINE        │   │  OFFLINE-FIRST REPOSITORY &    │
│  ┌─────────────────────────────────┐  │   │  REAL-TIME SYNC ENGINE         │
│  │ Online: Gemini 3.5 Flash REST   │  │   │  • Room SQLite Database:       │
│  │ Structured JSON Schema Output   │  │   │    - GuestProfileEntity        │
│  └────────────────┬────────────────┘  │   │    - McqQuestionEntity (Cache) │
│                   │ Auto-Fallback     │   │    - QuizAttemptEntity         │
│  ┌────────────────▼────────────────┐  │   │    - LeaderboardEntryEntity    │
│  │ Offline: Lightweight Rule-Based │  │   │    - TopicMasteryEntity        │
│  │ Concept & Template Synthesizer  │  │   │  • Network Connectivity Flow   │
│  │ (CS, Science, Aptitude, Lang)   │  │   │  • OAuth 2.0 CredentialManager │
│  └─────────────────────────────────┘  │   │  • Local Push Notifications    │
└───────────────────────────────────────┘   └────────────────────────────────┘
```

### Data Model & State

1. **`GuestProfileEntity` (Multi-User Support & OAuth Link)**:
   - Stores `profileId`, `displayName`, `avatarEmoji`, `isGuest`, `oauthEmail` (nullable), `totalXp`, `level`, `currentStreak`, `bestStreak`, `eloRating`, and `isActive`.
2. **`McqQuestionEntity` (Pydantic-Validated Schema & Offline Cache)**:
   - Stores `id`, `category` (`CS_TECH`, `SCIENCE_MATH`, `EXAM_APTITUDE`, `LANGUAGE_VOCAB`, `CUSTOM`), `subTopic`, `difficulty` (`1..4`), `questionText`, `optionsJson` (4 validated choices), `correctIndex` (`0..3`), `explanation`, `sourceMode` (`ONLINE_GEMINI` or `OFFLINE_ENGINE`), `timesAnswered`, `timesCorrect`, and `needsRevision` boolean flag.
3. **`TopicMasteryEntity` (Adaptive Learning Tree)**:
   - Tracks per-category and per-subtopic `masteryScore` (`0..100`), `currentDifficultyTier`, `accuracyRate`, `unlockedBadges`, and `lastPracticedAt`.
4. **`LeaderboardEntryEntity` & `ChallengeRoomState` (Competitive & Sync Queue)**:
   - Stores `entryId`, `profileId`, `playerName`, `avatar`, `score`, `maxStreak`, `accuracyPercent`, `mode` (`DAILY_BLITZ`, `HEAD_TO_HEAD`, `ADAPTIVE_MASTERY`), `isSynced`, and `timestamp`.

### Interactive Component & State Mapping

- **Network & Mode Switcher (`ConnectivityBanner`)**: Observes Android `ConnectivityManager.NetworkCallback` in real time and exposes a manual `Force Offline / Online Mode` toggle so users can test seamless offline fallback at any time.
- **MCQ Generator Studio (`GeneratorBottomSheet` / `StudioCard`)**: Validates topic input, invokes `HybridMcqGenerator`, runs `McqSchemaValidator` (rejecting or repairing malformed items), persists new questions to Room, and launches the selected quiz mode.
- **Active Quiz Engine (`QuizSessionScreen`)**:
  - In **Adaptive Mode**: Evaluates answer accuracy + response latency to compute the next question's difficulty tier (`1..4`), displays an expandable explanation card, and offers a one-tap *"Add to Revision Deck"* toggle.
  - In **Competitive Blitz / Head-to-Head Mode**: Runs a 100ms-precision coroutine countdown timer, calculates combo multipliers (`1.0x` to `3.0x`), updates a live rival comparison bar (or Pass-and-Play Player 2 turn), and posts final scores to the real-time Leaderboard & Notification service.
- **Security & Schema Test Suite (`SecurityAuditSheet`)**: Interactive diagnostic runner allowing the user to execute live self-tests on Pydantic schema validation, XSS/injection sanitization, OAuth 2.0 credential state, and Room offline cache resilience directly inside the UI.
