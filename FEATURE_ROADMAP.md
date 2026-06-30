# DefineEasy Feature Roadmap

> **Goal:** Move from 0 users to product-market fit by building growth loops, retention mechanics, and market expansion features.

---

## Phase 1: Growth Engine (Week 1)

### 1.1 Shareable Word Cards
**Priority:** CRITICAL — #1 viral loop for Indian users who share study material on WhatsApp.

**What:** Turn any word into a beautiful, branded image card (word + phonetic + definition + part-of-speech + app branding) that users can save and share.

**Why:** A user sharing one card on a WhatsApp study group reaches 50–200 people. Each card is a billboard with a download link.

**Technical Plan:**
- Create `WordCardRenderer.kt` — a Compose-based composable rendered off-screen to a `Bitmap`
- Card design: gradient background (matching theme), large word typography, phonetic, first definition, "DefineEasy" watermark + download URL
- Add "Share Card" button in `WordDetailScreen` toolbar
- Use `Bitmap.createBitmap()` + `Canvas` to render composable, then `Intent.ACTION_SEND` with `image/*` MIME type
- Add share options: WhatsApp, Instagram, Save to Gallery

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/components/WordCardRenderer.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/screens/WordDetailScreen.kt` (ADD share card action)
- `app/src/main/res/values/strings.xml` (ADD share card strings)

---

### 1.2 Text Share → Define (Receive Intent)
**Priority:** HIGH — Captures users mid-reading.

**What:** When a user shares text from another app (browser, Kindle, PDF reader) to DefineEasy, auto-define the text and offer "Add to Review".

**Why:** Massive acquisition channel. Users discover your app while reading — the moment they need a definition most.

**Technical Plan:**
- Add `intent-filter` in `AndroidManifest.xml` for `ACTION_SEND` with `text/plain`
- Create `ReceiveShareActivity.kt` — thin launcher that extracts shared text, finds the first word (or entire phrase), and navigates to `WordDetailScreen`
- Handle edge cases: multiple words, URLs, long text (truncate or show "no definition found")
- Deep link directly to word detail with the shared word as the search query

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/ReceiveShareActivity.kt` (NEW)
- `app/src/main/AndroidManifest.xml` (ADD intent-filter)
- `app/src/main/java/com/rudra/defineeasy/navigation/DefineEasyDestinations.kt` (ADD share-receive route if needed)

---

### 1.3 Streak System
**Priority:** HIGH — #1 retention mechanic (Duolingo proved this).

**What:** Visible streak counter showing consecutive days of review. Streak freeze (1 free skip/day). Milestone badges (7, 30, 100 days).

**Why:** Once a user has a 5-day streak, they open daily. Streak loss is the #1 reason for churn — freeze prevents it.

**Technical Plan:**
- `StreakTracker.kt` — DataStore-backed service tracking:
  - `currentStreak: Int`
  - `lastReviewDate: LocalDate?`
  - `freezeCount: Int`
  - `longestStreak: Int`
- Logic: On each review completion, check if `lastReviewDate == today - 1`. If yes, increment streak. If no, check freeze. If no freeze, reset to 1.
- Display streak pill in `SearchScreen` hero section (replaces or augments current streak placeholder)
- Add streak freeze in Settings
- Add milestone achievement toasts (7, 30, 100 days)

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/preferences/StreakPreferences.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/domain/StreakTracker.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/screens/SearchScreen.kt` (MODIFY streak display)
- `app/src/main/java/com/rudra/defineeasy/settings/SettingsScreen.kt` (MODIFY — add streak freeze info)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/screens/ReviewScreen.kt` (MODIFY — update streak on review complete)
- `app/src/main/res/values/strings.xml` (ADD streak strings)

---

## Phase 2: Retention & Market Expansion (Week 2)

### 2.1 Daily Word Quiz
**Priority:** MEDIUM — Gives daily reason to open even without due words.

**What:** 5-question multiple-choice quiz. Mix of due review words + new words from collections. Score tracking, shareable results.

**Why:** Review only works when you have due words. Quiz works every day. Shareable results = viral.

**Technical Plan:**
- `QuizGenerator.kt` — selects 5 words: 2 from due review, 2 from recent searches, 1 random from collections
- For each word, generate 4 options: correct definition + 3 random definitions from other words
- `QuizScreen.kt` — card-based UI, one question at a time, swipe or tap to answer
- Score screen: X/5 correct, "Share Score" button (generates shareable image or text)
- Store daily quiz history in Room (`QuizHistoryEntity`)

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/feature_quiz/` (NEW package)
  - `QuizScreen.kt`
  - `QuizViewModel.kt`
  - `QuizState.kt`
  - `QuizGenerator.kt`
- `app/src/main/java/com/rudra/defineeasy/navigation/DefineEasyDestinations.kt` (ADD Quiz route)
- `app/src/main/java/com/rudra/defineeasy/navigation/DefineEasyNavGraph.kt` (ADD Quiz composable)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/data/local/entity/QuizHistoryEntity.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/data/local/WordInfoDatabase.kt` (ADD entity + migration)

---

### 2.2 More Exam Collections
**Priority:** MEDIUM — Each collection = new user segment + SEO keyword.

**What:** Add JSON asset files for: GRE (500 words), SSC CGL (300 words), IELTS (200 words), TOEFL (200 words), GATE (150 words).

**Why:** Low effort (JSON files), massive reach. GRE alone has 300K+ Indian test-takers/year.

**Technical Plan:**
- Create `collection_gre.json`, `collection_ssc.json`, `collection_ielts.json`, `collection_toefl.json`, `collection_gate.json` in `assets/`
- Follow existing `CollectionWordDto` schema
- Add collection IDs to `CollectionModels.kt`
- Add UI metadata (title, description, icon, gradient) to `CollectionUiMetadata.kt`
- No code changes needed for the loading pipeline — it already reads from assets by ID

**Files to create/modify:**
- `app/src/main/assets/collection_gre.json` (NEW)
- `app/src/main/assets/collection_ssc.json` (NEW)
- `app/src/main/assets/collection_ielts.json` (NEW)
- `app/src/main/assets/collection_toefl.json` (NEW)
- `app/src/main/assets/collection_gate.json` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/domain/model/CollectionModels.kt` (MODIFY — add IDs)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/CollectionUiMetadata.kt` (MODIFY — add metadata)
- `app/src/main/res/values/strings.xml` (ADD collection titles/descriptions)

---

### 2.3 Offline Word Packs
**Priority:** MEDIUM — Critical for Indian users with spotty internet.

**What:** Pre-cached word bundles (word + definition + phonetic + example) that can be downloaded and used fully offline.

**Why:** Indian students study during commutes (bus, train, metro) where internet is unreliable.

**Technical Plan:**
- Generate `wordpack_upsc.json`, `wordpack_cat.json` etc. — pre-fetched definitions from API
- Store in `assets/wordpacks/`
- On first app launch or manual download, load packs into Room database
- Add "Download Offline Pack" option in Settings
- Mark words with `source = "offline_pack"` for UI distinction

**Files to create/modify:**
- `app/src/main/assets/wordpacks/` (NEW directory + JSON files)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/data/local/WordPackDataSource.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/settings/SettingsScreen.kt` (MODIFY — add download button)
- `app/src/main/java/com/rudra/defineeasy/settings/SettingsViewModel.kt` (MODIFY — add download action)

---

### 2.4 Clipboard Detection
**Priority:** LOW (small effort, nice UX) — Frictionless lookup.

**What:** When app opens, detect if clipboard contains a single English word → show "Define [word]?" prompt.

**Why:** Zero-friction word lookup after copying from reading material.

**Technical Plan:**
- `ClipboardDetector.kt` — uses `ClipboardManager` to read clipboard on app focus
- Filter: must be a single word, 2–30 chars, alphabetic only
- Show a `Snackbar` or bottom sheet in `SearchScreen`: "Define '[word]'?" with "Search" and "Dismiss" buttons
- Respect clipboard privacy — only read on explicit app open, don't store

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/core/util/ClipboardDetector.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/screens/SearchScreen.kt` (MODIFY — show prompt)
- `app/src/main/res/values/strings.xml` (ADD clipboard strings)

---

## Phase 3: Polish & Trust (Week 3)

### 3.1 Rate the App Prompt
**Priority:** HIGH for ASO — Critical with 0 users.

**What:** Show in-app review prompt after user completes 10 reviews OR hits a 3-day streak.

**Why:** Play Store algorithm favors apps with recent positive reviews. Need seed reviews to rank.

**Technical Plan:**
- Use `AppReviewManager` from Google Play Core library
- Trigger conditions: `reviewCount >= 10` OR `streak >= 3`
- Track prompt shown count — max 2 prompts total
- Store state in DataStore (`ReviewPromptPreferences`)

**Files to create/modify:**
- `app/build.gradle.kts` (ADD play-core dependency)
- `app/src/main/java/com/rudra/defineeasy/preferences/ReviewPromptPreferences.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/DictionaryApp.kt` (MODIFY — check and trigger on app launch)

---

### 3.2 App Shortcuts
**Priority:** LOW — Modern Android expectation.

**What:** Long-press app icon → "Search Word", "Word of the Day", "Start Review".

**Why:** Reduces friction to core actions. Looks polished.

**Technical Plan:**
- Add `shortcuts.xml` in `res/xml/`
- Define 3 static shortcuts: search, wotd, review
- Each shortcut launches `MainActivity` with appropriate intent extra
- `MainActivity` handles extras in `onCreate` and navigates accordingly

**Files to create/modify:**
- `app/src/main/res/xml/shortcuts.xml` (NEW)
- `app/src/main/AndroidManifest.xml` (ADD meta-data reference)
- `app/src/main/java/com/rudra/defineeasy/MainActivity.kt` (MODIFY — handle shortcut extras)
- `app/src/main/res/values/strings.xml` (ADD shortcut labels)

---

### 3.3 Export/Import Collections
**Priority:** LOW — Network effect for study groups.

**What:** Export custom collections as shareable JSON files. Import collections from shared files.

**Why:** Teachers share word lists with students. Study groups share collections. Viral loop.

**Technical Plan:**
- Export: serialize custom collection + words to JSON → `Intent.ACTION_SEND` or save to Downloads
- Import: `Intent.ACTION_OPEN_DOCUMENT` to pick JSON file → parse and insert into Room
- Add export/import buttons to `CollectionsScreen`

**Files to create/modify:**
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/data/collection/CollectionImportExport.kt` (NEW)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/screens/CollectionsScreen.kt` (MODIFY — add buttons)
- `app/src/main/java/com/rudra/defineeasy/feature_dictionary/presentation/CollectionsViewModel.kt` (MODIFY — add import/export actions)

---

## Execution Order

```
Day 1: Shareable Word Cards (1.1) + Text Share → Define (1.2)
Day 2: Streak System (1.3)
Day 3: Daily Word Quiz (2.1)
Day 4: More Exam Collections (2.2) — GRE + SSC + IELTS
Day 5: Offline Word Packs (2.3) + Clipboard Detection (2.4)
Day 6: Rate the App Prompt (3.1) + App Shortcuts (3.2)
Day 7: Export/Import Collections (3.3) + Polish + Bug Fixes
```

---

## Dependencies to Add

```kotlin
// build.gradle.kts
implementation("com.google.android.play:review:2.0.2")              // Phase 3.1
implementation("com.google.android.play:review-ktx:2.0.2")          // Phase 3.1
```

---

## Success Metrics

| Metric | Target (30 days) | Target (90 days) |
|--------|-----------------|-----------------|
| Daily Active Users | 50 | 500 |
| 7-Day Retention | 25% | 40% |
| Avg Session Duration | 3 min | 5 min |
| Words Shared per User | 0.5/day | 1.5/day |
| Play Store Rating | 4.2+ (10+ reviews) | 4.5+ (50+ reviews) |

---

## Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Word card rendering is slow | Use `ComposeView` off-screen rendering, cache bitmaps, show loading state |
| Streak feels punitive | Add freeze mechanic, show streak loss prevention dialog |
| Quiz questions too easy/hard | Use SM-2 easinessFactor to calibrate difficulty |
| Clipboard detection feels invasive | Only trigger on explicit app open, show clear prompt, never auto-search |
| More collections dilute brand | Keep UPSC/CAT as featured; new collections in "More" section |
