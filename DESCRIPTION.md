# Project descriptions

Drop-in descriptions for various places. Each is self-contained.

---

## GitHub repo "About" (max 350 chars, but ~120 is the sweet spot)

```
A personal Android app that turns Reddit, Hacker News, and X.com into one
calm, AI-edited daily edition. You bring the LLM key; the app handles
the scraping, clustering, and editorial layout.
```

```
A daily news digest app for Android. AI-edited, editorial typography,
finite reading. The LLM is the data adaptor — no per-source scrapers.
```

```
Your daily digest, across the sources you care about. Android, KMP-ready,
AI-edited, editorial typography. Bring your own OpenAI-compatible API key.
```

---

## GitHub repo "Description" (one-liner, ~80 chars)

```
Daily AI-edited digest from Reddit, HN, and X.com. Editorial typography.
Android, KMP-ready, BYO LLM key.
```

```
One calm newspaper per day, distilled from the sources you choose.
```

```
A daily newspaper for the rest of the internet, distilled by an LLM.
```

---

## Twitter / X / social post

```
A daily newspaper for the rest of the internet. Distilled from Reddit,
HN, and X by an LLM you control. Editorial typography, no infinite scroll,
no engagement metrics — just one calm edition per day.
```

```
Essentia: a daily digest app where the LLM IS the data adaptor. No
per-source scrapers, no engagement tricks. Just a calm newspaper.
```

```
Built a daily news app where the LLM is the data scraper. Reddit + HN + X
→ one editorial per day, capped at 10 stories. Editorial typography,
"finite reading for mindful minds."
```

---

## Product Hunt / Hacker News "Show HN" style

**Title:** Show HN: Essentia – A daily newspaper distilled from Reddit, HN, and X

**Body:**
```
I built a personal Android app that turns Reddit, Hacker News, and X.com
into one calm, AI-edited daily edition. The interesting design choice:
the LLM is the data adaptor. Instead of writing per-source scrapers, I
give the model a `fetch_url` tool and let it call the public APIs on
my behalf. Two calls per topic: one to gather, one to cluster into JSON.

Editorial typography (serif headlines, sans body), one edition per day
capped at 10 stories, save-for-later but no other social features. The
point is to be the opposite of a notification feed.

Written in Kotlin + Compose, with a shared module that's KMP-ready for
iOS. Bring your own API key — works with OpenRouter (free tier), OpenAI,
Groq, Together, LM Studio, anything that speaks the OpenAI
chat-completions protocol.

Repo + screenshots in the README. The crash logger + diagnostics screen
are fun if you want to read about how I'm dogfooding the app to find
its own bugs.
```

---

## Project one-liner (Twitter bio, conference talk abstract)

```
Essentia — a daily newspaper for the rest of the internet, distilled by
an LLM from the sources you choose. Editorial typography, no infinite
scroll, no engagement metrics. Android, KMP-ready. MIT.
```

---

## README blurb (already in the README but here for copy-paste)

> Essentia is a personal Android app that turns a handful of public news
> sources (Reddit, Hacker News, and X.com) into a single, deduplicated,
> AI-edited daily edition — like a calm newspaper for the rest of the
> internet. You pick the topics you care about. The app pulls raw posts
> from each enabled source, hands them to a language model of your
> choice, and the LLM clusters the same-event stories, writes a card
> summary plus a long-form body, picks a category, estimates read-time,
> and produces a single editorial per cluster. You read. The app
> disappears.

---

## Why it exists (mission statement, 2 sentences)

> Most news apps treat you as a feed to be optimized for engagement.
> Essentia treats you as a reader with a finite amount of attention.

---

## Tagline (max ~6 words)

```
Your daily digest, distilled.
```

```
A calm newspaper for the rest of the internet.
```

```
Finite reading for mindful minds.
```

```
One edition a day. No infinite scroll.
```

```
The LLM is the data adaptor.
```
