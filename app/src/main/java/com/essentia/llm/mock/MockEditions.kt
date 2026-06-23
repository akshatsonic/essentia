package com.essentia.llm.mock

import com.essentia.data.model.Category
import com.essentia.data.model.DigestItem
import com.essentia.data.model.Edition
import com.essentia.data.model.EditionType

object MockEditions {

    private val now = System.currentTimeMillis()
    private val day = 1000L * 60 * 60 * 24

    // IMPORTANT: `titles` must be declared BEFORE `itemsForEdition` and `titleFor` because
    // Kotlin initializes object properties top-to-bottom. Previously this was declared
    // after `titleFor`, which caused the object initializer to NPE on `titles.size`.
    private val titles: List<String> = listOf(
        "Energy Sector Faces New Regulations Amid Climate Summit",
        "Tech Giants Announce Merger: Aiming to Set New Standard for AI Integration",
        "Healthcare Reforms Stir Debate: New Bill Passes First Reading",
        "Cryptocurrency Markets Rebound After Major Banks Signal Support",
        "Aerospace Industry Reveals Plans for Next-Gen Electric Aircraft",
        "Cybersecurity Concerns Rise as New Vulnerabilities Are Discovered",
        "Quantum Computing Milestone: Researchers Demonstrate Practical Application",
        "Space Exploration: International Crew Returns from Historic Mission"
    )

    private fun titleFor(edition: Edition, idx: Int): String =
        titles[Math.abs(edition.id.hashCode() + idx) % titles.size]

    private fun humanDate(edition: Edition): String {
        val fmt = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
        return fmt.format(java.util.Date(edition.createdAt))
    }

    val allEditions: List<Edition> = listOf(
        edition(daysAgo = 0, type = EditionType.DAILY),
        edition(daysAgo = 1, type = EditionType.DAILY),
        edition(daysAgo = 2, type = EditionType.WEEKEND),
        edition(daysAgo = 3, type = EditionType.WEEKEND),
        edition(daysAgo = 4, type = EditionType.DAILY),
        edition(daysAgo = 5, type = EditionType.DAILY),
        edition(daysAgo = 6, type = EditionType.DAILY),
        edition(daysAgo = 7, type = EditionType.DAILY)
    )

    val allItems: Map<String, List<DigestItem>> = allEditions.associate { ed ->
        ed.id to itemsForEdition(ed)
    }

    val savedForLater: List<DigestItem> = allEditions.flatMap { allItems[it.id].orEmpty() }
        .filter { it.id.hashCode().mod(3) == 0 }
        .map { it.copy(isArchived = true) }

    private fun edition(daysAgo: Int, type: EditionType): Edition {
        val id = "past-${daysAgo}"
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date(now - daysAgo * day))
        return Edition(
            id = id,
            date = date,
            type = type,
            digestIds = emptyList(),
            createdAt = now - daysAgo * day
        )
    }

    private fun itemsForEdition(edition: Edition): List<DigestItem> = listOf(
        DigestItem(
            id = "past-1-${edition.id}",
            topicId = "ai-agents",
            editionId = edition.id,
            title = titleFor(edition, 0),
            summary = "A detailed analysis of how distributed AI systems are reshaping the enterprise software stack, with implications for incumbent vendors and new entrants alike.",
            detailSummary = "Long-form detail summary for ${edition.date}. " +
                "The shift toward agentic systems is not merely a feature release — it represents a fundamental restructuring of how software is built, sold, and supported. " +
                "In conversations with engineering leaders at twenty Fortune 1000 companies this quarter, a consistent pattern emerged: teams that began with a single-agent prototype six months ago are now operating fleets of cooperating agents, each with its own memory, tools, and policy constraints. " +
                "The organizational implications are even more striking. The most advanced adopters have moved from the traditional 'feature team' model to a 'capability team' model, where teams own a coherent slice of agent behavior end-to-end. " +
                "This shift is not without risk. Several early adopters report difficulty managing the operational complexity of agent fleets, particularly around observability and rollback. The tools and practices that worked for stateless microservices do not always transfer cleanly.",
            pullQuote = "The shift toward agentic systems is not merely a feature release — it represents a fundamental restructuring of how software is built, sold, and supported.",
            category = Category.ANALYSIS,
            readTimeMinutes = 6,
            isArchived = false,
            byline = "By Elena Kovacs · ${humanDate(edition)}",
            sourceIds = listOf("hn", "reddit"),
            sourceUrls = listOf(
                "https://news.ycombinator.com/item?id=past1",
                "https://reddit.com/r/MachineLearning/comments/past1"
            ),
            topicTag = "AI Agents",
            timestamp = edition.createdAt,
            createdAt = edition.createdAt
        ),
        DigestItem(
            id = "past-2-${edition.id}",
            topicId = "tech",
            editionId = edition.id,
            title = titleFor(edition, 1),
            summary = "New regulations proposed this week would reshape the compliance landscape for technology companies operating in the European market.",
            detailSummary = "Detailed coverage of the new regulatory framework. " +
                "The proposed rules extend existing data protection requirements to cover AI-generated content, automated decision-making systems, and cross-border data flows. " +
                "Compliance costs are expected to increase by 15-20% for affected companies, though proponents argue the long-term benefits of regulatory clarity outweigh the near-term burden. " +
                "Industry response has been mixed, with larger incumbents expressing qualified support and smaller startups warning that compliance overhead could stifle innovation.",
            pullQuote = null,
            category = Category.BREAKING,
            readTimeMinutes = 4,
            isArchived = false,
            byline = "By Daniel Park · ${humanDate(edition)}",
            sourceIds = listOf("x", "reddit"),
            sourceUrls = listOf(
                "https://x.com/sample/past2",
                "https://reddit.com/r/technology/comments/past2"
            ),
            topicTag = "Tech Policy",
            timestamp = edition.createdAt + 1000L * 60 * 60 * 2,
            createdAt = edition.createdAt
        ),
        DigestItem(
            id = "past-3-${edition.id}",
            topicId = "opinion",
            editionId = edition.id,
            title = titleFor(edition, 2),
            summary = "A contrarian take on why the current wave of consolidation in the productivity software market may be good for users in the long run.",
            detailSummary = "Opinion piece. " +
                "Conventional wisdom holds that consolidation is bad for users: fewer competitors means less innovation and higher prices. But the current round of mergers in the productivity software space tells a more nuanced story. " +
                "In several cases, the acquired products have actually improved after acquisition — gaining reliability, security updates, and integrations they previously lacked. The trade-off is real, but it is not as one-sided as critics suggest. " +
                "What users should watch for is not the number of competitors, but the diversity of business models and the openness of the platforms that result.",
            pullQuote = "What users should watch for is not the number of competitors, but the diversity of business models.",
            category = Category.OPINION,
            readTimeMinutes = 5,
            isArchived = false,
            byline = "By Marcus Whitfield · ${humanDate(edition)}",
            sourceIds = listOf("reddit"),
            sourceUrls = listOf("https://reddit.com/r/productivity/comments/past3"),
            topicTag = "Software",
            timestamp = edition.createdAt + 1000L * 60 * 60 * 4,
            createdAt = edition.createdAt
        ),
        DigestItem(
            id = "past-4-${edition.id}",
            topicId = "world",
            editionId = edition.id,
            title = titleFor(edition, 3),
            summary = "Diplomatic negotiations yield a framework agreement after months of on-again, off-again talks.",
            detailSummary = "The agreement, signed in a ceremony attended by heads of state from seven nations, establishes a framework for cooperation on a range of issues including trade, climate, and security. " +
                "Implementation will begin in phases, with the first set of measures taking effect within 90 days. Observers note that while the agreement is ambitious in scope, the details of enforcement remain to be worked out. " +
                "Markets responded positively, with regional equity indexes gaining an average of 1.8% on the news.",
            pullQuote = null,
            category = Category.BREAKING,
            readTimeMinutes = 3,
            isArchived = false,
            byline = null,
            sourceIds = listOf("hn"),
            sourceUrls = listOf("https://news.ycombinator.com/item?id=past4"),
            topicTag = "World",
            timestamp = edition.createdAt + 1000L * 60 * 60 * 6,
            createdAt = edition.createdAt
        )
    )
}
