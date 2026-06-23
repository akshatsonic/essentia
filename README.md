# Essentia

> Your daily digest, across the sources you care about.

Essentia is a personal Android app that turns a handful of public news sources (Reddit, Hacker News, and X.com) into a single, deduplicated, AI-edited daily edition — like a calm newspaper for the rest of the internet.

You pick the topics you care about. The app pulls raw posts from each enabled source, hands them to a language model of your choice, and the LLM clusters the same-event stories, writes a card summary plus a long-form body, picks a category, estimates read-time, and produces a single editorial per cluster. You read. The app disappears.

---

## Why it exists

Most news apps treat you as a feed to be optimized for engagement. Essentia treats you as a reader with a finite amount of attention. The whole UX leans into that:

- One edition per day, capped at 10 stories
- Editorial typography (serif headlines, sans body), not a notification feed
- "Finite reading for mindful minds" is the tagline for a reason
- Save-for-later is the only social feature
- No infinite scroll, no engagement metrics, no streaks

---

## What works today (v0.2.0)

- **Welcome screen** on first launch with a 4-step onboarding (LLM → Topic → Sources → Done)
- **Real LLM integration** — not a mock. You bring your own API key for any OpenAI-compatible service (OpenRouter, OpenAI, Groq, Anthropic via a proxy, Together, etc.) and the app runs the actual digest
- **Two-call digest flow**:
  1. The LLM is given a `fetch_url` tool and pulls raw JSON from Reddit, Hacker News, and X.com on its own
  2. The LLM is asked to produce strict JSON matching the digest schema, with one item marked featured, each cluster classified as BREAKING / ANALYSIS / OPINION
- **Settings screen** is stateful and persists (LLM provider, model, API key, sources, X bearer token)
- **API key is stored encrypted** via `EncryptedSharedPreferences` (Android Keystore-backed AES-256-GCM)
- **Crash logger** writes every uncaught exception to a file in `filesDir/crash_logs/` and a Diagnostics screen lets you view, share, or clear them
- **Real loading + error states** — Today shows a pulsing accent dot while the digest is building, an explicit "No digest yet" empty state, and a dedicated error state with the actual error message
- **Smooth transitions** — slide-in-from-right for pushes, crossfade for tab switches, staggered card entry, shimmer skeletons in the loading screen

## What's intentionally not done yet

- **No images yet.** The hero placeholders are gradient blocks with the word "ESSENTIA" faded behind. v2 will scrape the source's `og:image` (recommended) or call an image model.
- **No schema validation** of the LLM's JSON. The current code relies on `response_format: json_object` and the prompt. v2 will add a real `SchemaValidator` with field-level checks + retry on parse failure.
- **No WorkManager** scheduled daily digest yet. For now you tap "Run Digest Now" in Settings. v2 will schedule it.
- **No iOS** yet. The architecture is KMP-ready (the `llm/`, `data/`, and `crash/` packages are pure Kotlin) but the UI is Compose-on-Android only.
- **No Anthropic native support.** Anthropic uses a different request shape (separate `system` field, no `response_format`). The current `OpenAiCompatibleProvider` works with OpenAI, OpenRouter, Groq, Together, LM Studio, etc. — anything that speaks the OpenAI chat-completions protocol.

---

## Architecture

```
essentia/
├── data/
│   ├── model/                # Pure-Kotlin domain types
│   │   └── Models.kt         #   DigestItem, Edition, Source, Category,
│   │                         #   LlmDigestResponse (the JSON contract)
│   │
│   ├── prefs/                # Persistence
│   │   ├── UserPreferences.kt    # DataStore (non-sensitive prefs)
│   │   └── SecureStore.kt        # EncryptedSharedPreferences (API keys)
│   │
│   └── repository/
│       ├── DigestRepository.kt   # In-memory state of today's digest
│       └── SettingsRepository.kt # Combines UserPreferences + SecureStore
│                                 #   into a single Flow<AppSettings>
│
├── llm/                      # LLM layer (pure Kotlin, no Android)
│   ├── LlmProvider.kt         # Sealed interface + ToolDefinition + LlmResult
│   ├── DigestEngine.kt        # Orchestrates per-topic generateDigest();
│   │                         #   publishes StateFlow<DigestState>
│   ├── openai/                # Real OpenAI-compatible provider
│   │   ├── OpenAiCompatibleProvider.kt
│   │   ├── HttpFetcher.kt     # Ktor-backed URL fetch (used by fetch_url)
│   │   └── ToolRegistry.kt    # The fetch_url tool definition
│   └── mock/
│       ├── MockLlmProvider.kt # Canned data, used when no API key is set
│       └── MockEditions.kt    # Backfill of past editions for the
│                             #   Editions tab
│
├── crash/
│   └── CrashLogger.kt         # Thread.setDefaultUncaughtExceptionHandler
│                              #   + CoroutineExceptionHandler + file writer
│
└── ui/                       # Compose UI (Android-only, will become
    ├── theme/                #   the per-platform UI layer under KMP)
    ├── components/           # Shared editorial primitives
    ├── navigation/           # NavHost + transitions
    ├── onboarding/           # 4-step first-run flow
    ├── today/                # Home feed (Today tab)
    ├── editions/             # Past editions + per-edition list
    ├── archive/              # Saved-for-later (Archive tab)
    ├── detail/               # Long-form article view
    ├── settings/             # LLM provider, sources, maintenance
    ├── topics/               # Subscribed topics list
    ├── diagnostics/          # Crash logs viewer
    └── loading/              # "Fetching your stories" splash
```

The `llm/`, `data/`, and `crash/` packages are pure Kotlin and ready to drop into a KMP `shared/` module. The `ui/` package is Android-only Compose — it would become `androidApp/` in a KMP setup with a sibling `iosApp/` (SwiftUI).

---

## The two-call LLM flow in detail

This is the core of the product. It replaces a wall of source-specific scrapers with one LLM that knows the public APIs.

### Call 1 — Scrape

```
System: "You are a research assistant. Your only job is to gather raw
search hits from a small set of public APIs. You have one tool:
fetch_url."

User:   "Topic: 'AI agents'. Window: hits newer than <3 days ago>.
        Fetch raw data from these endpoints (call fetch_url for each):
        - Reddit: https://www.reddit.com/r/redditdev/new.json?...
        - Hacker News Algolia: https://hn.algolia.com/api/v1/search?...
        - X.com (if enabled): https://api.twitter.com/2/tweets/..."

Tools:  fetch_url(url: string) → returns raw response body
```

The LLM autonomously calls `fetch_url` for each enabled source, with up to 6 rounds of follow-ups. We aggregate the raw responses into one big text blob.

### Call 2 — Cluster

```
System: "You are an editorial assistant. You receive raw search hits
        and produce a deduplicated, clustered daily digest in strict
        JSON. Output this shape exactly: { edition: {...}, items: [...] }.
        Cluster same-event stories. Cap at 10. Mark exactly one featured.
        Classify as BREAKING/ANALYSIS/OPINION. Estimate read-time from
        word count. Synthetic bylines OK. No empty items."

User:   "Topic: 'AI agents'

        Raw hits:
        ---
        { Reddit .data.children: [...] }
        { HN hits: [...] }"
```

With `response_format: { type: "json_object" }` set in the request body. The app parses the response with `kotlinx.serialization` into `LlmDigestResponse`, validates the structure, and pushes it to the repository for the UI.

The LLM is the data adaptor. The app's only contract with Reddit/HN/X is "here are the URLs; here's the JSON shape I want back." If a new source is added, the prompt changes — no new scraper code.

---

## Running the app

### Prerequisites

- Android Studio (Quail 2026.1+) or a recent Android SDK
- JDK 17+
- An Android emulator or a physical device on API 26+

### Build

```bash
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install on a device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### First launch

1. Open the app. You land on the Welcome screen.
2. Tap **Get started**.
3. Pick a provider (we recommend **OpenRouter** — it has a free tier; sign up at [openrouter.ai](https://openrouter.ai) and create an API key).
4. Paste your API key, tap **Verify Connection**. You should see "✓ Connection verified".
5. Type a topic you want to read about, e.g. *"AI agents"*.
6. Enable at least one source (Reddit and Hacker News are recommended; both are free; X.com requires a bearer token).
7. Tap **Finalize Connections**.
8. You land on **Today**, which shows "No digest yet — Open Settings → Maintenance → Run Digest Now".
9. Tap the gear icon (top right of Today), scroll to **Maintenance**, tap **Run Digest Now**.
10. Within ~10-30 seconds, your real AI-edited digest appears.

### Other providers

The app speaks the **OpenAI chat completions protocol**, so it works with anything that implements it:

| Provider | Base URL | Free tier? | Example model |
|---|---|---|---|
| **OpenRouter** (default) | `https://openrouter.ai/api/v1` | Yes | `meta-llama/llama-3.3-70b-instruct:free` |
| OpenAI | `https://api.openai.com/v1` | No | `gpt-4o-mini` |
| Groq | `https://api.groq.com/openai/v1` | Yes | `llama-3.1-8b-instant` |
| Together AI | `https://api.together.xyz/v1` | Yes (credits) | `meta-llama/Llama-3-70b-chat-hf` |
| LM Studio (local) | `http://localhost:1234/v1` | N/A | any local model |

To use a non-default provider: edit the **Model** field in Settings, and (for the most flexibility, v2) the **Base URL** field.

---

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin 2.0.21 | KMP-ready, mature for Android |
| UI | Jetpack Compose + Material 3 | Less boilerplate, animations are first-class |
| Navigation | androidx.navigation:navigation-compose 2.8.4 | Type-safe routes, easy transitions |
| Async | Kotlin Coroutines + StateFlow | The lingua franca of Android async |
| HTTP | Ktor client (OkHttp engine) | KMP-friendly, no OkHttp boilerplate |
| Serialization | kotlinx.serialization | Auto-derives from data classes |
| Preferences | androidx.datastore:datastore-preferences 1.1.1 | Type-safe, async, modern |
| Secrets | androidx.security:security-crypto 1.1.0-alpha06 | EncryptedSharedPreferences (Keystore-backed) |
| Min SDK | 26 | EncryptedSharedPreferences requires API 23+ |
| Build | Gradle 8.10.2 + AGP 8.5.2 | Latest stable at the time of writing |

---

## Why "essentia"?

It's Latin for *essence* — the part of something that makes it what it is. The app is the essence of your daily news, distilled.

---

## License

MIT. Use it, fork it, learn from it.
