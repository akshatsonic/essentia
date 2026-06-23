package com.essentia.llm.mock

import com.essentia.data.model.Category
import com.essentia.data.model.DigestItem
import com.essentia.data.model.Edition
import com.essentia.data.model.EditionType
import com.essentia.data.model.LlmDigestResponse
import com.essentia.llm.LlmProvider
import kotlinx.coroutines.delay

class MockLlmProvider : LlmProvider {
    override val id: String = "mock"

    override suspend fun generateDigest(
        topic: String,
        sinceMs: Long,
        enabledSources: Set<String>,
        xBearerToken: String?
    ): LlmDigestResponse {
        delay(800)
        val now = System.currentTimeMillis()
        val editionId = "edition-${now / 86400000}"
        val edition = Edition(
            id = editionId,
            date = isoDate(now),
            type = if (isWeekend(now)) EditionType.WEEKEND else EditionType.DAILY,
            digestIds = sampleItems(editionId, topic, now).map { it.id },
            createdAt = now
        )
        return LlmDigestResponse(edition = edition, items = sampleItems(editionId, topic, now))
    }

    private fun sampleItems(editionId: String, topic: String, now: Long): List<DigestItem> = listOf(
        DigestItem(
            id = stableId("global-markets-rally", topic),
            topicId = topic,
            editionId = editionId,
            title = "Global Markets Rally as Central Banks Signal Rate Pause",
            summary = "Investors breathed a sigh of relief on Tuesday following coordinated signals from major central banks that a pause in the rate-hike cycle is imminent, sparking a broad rally across equities and bonds.",
            detailSummary = "For the past decade, the central banks of the world's largest economies have pursued increasingly tight monetary policy in an attempt to combat post-pandemic inflation. On Tuesday, that posture shifted.\n\nIn a coordinated series of statements, the Federal Reserve, the European Central Bank, and the Bank of England signaled that the current rate-hike cycle has likely concluded, citing softening labor markets and easing consumer price growth. The S&P 500 surged 2.1% in afternoon trading, with the Nasdaq Composite gaining 2.8% and the Dow Jones Industrial Average adding 1.4%.\n\nBond markets reacted even more strongly. The yield on the 10-year U.S. Treasury note fell 18 basis points to 3.87%, its largest single-day drop in eight months. The 2-year yield, more sensitive to rate expectations, fell 22 basis points.\n\n\"This is the pivot the market has been waiting for,\" said Janet Alvarez, chief market strategist at Capital Research. \"The question now is whether this is a pause or the start of an easing cycle.\"\n\nNot all economists are convinced. Several noted that core inflation remains stubbornly above central bank targets, and that premature easing could risk reigniting price pressures. The Federal Reserve's preferred inflation gauge, the personal consumption expenditures price index, still shows core inflation running at 2.8% annually.\n\nEmerging markets, often sensitive to U.S. monetary policy shifts, saw broad-based gains. The MSCI Emerging Markets Index rose 3.2%, with notable strength in Brazilian and Indian equities.",
            pullQuote = "This is the pivot the market has been waiting for. The question now is whether this is a pause or the start of an easing cycle.",
            category = Category.BREAKING,
            readTimeMinutes = 4,
            isFeatured = true,
            isArchived = false,
            byline = "By Elena Kovacs · ${humanDate(now)}",
            sourceIds = listOf("reddit", "hn"),
            sourceUrls = listOf(
                "https://reddit.com/r/economics/comments/sample1",
                "https://news.ycombinator.com/item?id=sample1"
            ),
            topicTag = topic,
            timestamp = now - 1000L * 60 * 60 * 2,
            createdAt = now
        ),
        DigestItem(
            id = stableId("hidden-cost-urban-reforestation", topic),
            topicId = topic,
            editionId = editionId,
            title = "The Hidden Cost of Urban Reforestation Projects",
            summary = "Cities around the world are planting millions of trees to combat heat and pollution, but a new analysis reveals that poorly planned greening initiatives can deepen inequality and strain local budgets.",
            detailSummary = "Urban reforestation has long been championed as a low-cost, high-impact strategy for improving city livability. Trees cool streets, filter air, and provide habitat for urban wildlife. But a new study from the Lincoln Institute of Land Policy suggests that the benefits — and costs — are distributed unevenly across neighborhoods.\n\nThe study, which analyzed 12 major U.S. cities over the past decade, found that while canopy coverage in wealthy neighborhoods increased by an average of 8%, coverage in lower-income neighborhoods grew by less than 2%. The disparity is partly due to infrastructure: tree-planting programs require watering, pruning, and replacement, costs that wealthier districts are better equipped to absorb.\n\n\"We've created a situation where the places that need trees the most are getting the fewest,\" said Dr. Marcus Chen, the study's lead author. \"And it's not because of explicit policy — it's because the economics of tree maintenance are invisible to most planning budgets.\"\n\nThe analysis also found that poorly chosen species can worsen the problem. Fast-growing trees like the Callery pear, popular in many municipal programs, are now considered invasive in several states and require expensive removal programs.\n\nResearchers recommend a more deliberate approach: longer planning horizons, community-led species selection, and dedicated maintenance funding tied to each tree planted. Several cities, including Boston and Seattle, have begun piloting such models.",
            pullQuote = "We've created a situation where the places that need trees the most are getting the fewest.",
            category = Category.ANALYSIS,
            readTimeMinutes = 5,
            isArchived = false,
            byline = "By Daniel Park · ${humanDate(now)}",
            sourceIds = listOf("reddit"),
            sourceUrls = listOf("https://reddit.com/r/urbanplanning/comments/sample2"),
            topicTag = topic,
            timestamp = now - 1000L * 60 * 60 * 4,
            createdAt = now
        ),
        DigestItem(
            id = stableId("diplomatic-breakthrough-corridor", topic),
            topicId = topic,
            editionId = editionId,
            title = "Diplomatic Breakthrough Reached in Energy Corridor Dispute",
            summary = "After two years of stalled negotiations, three nations have signed a framework agreement to jointly develop a cross-border energy transmission corridor.",
            detailSummary = "Ministers from three nations signed a framework agreement in Geneva on Monday to jointly develop a 1,400-kilometer energy transmission corridor linking hydroelectric capacity in the north to industrial demand centers in the south. The deal, two years in the making, resolves a series of overlapping territorial claims that had frozen prior negotiations.\n\nThe corridor will transport a mix of hydroelectric and wind power, with a target capacity of 8 gigawatts — enough to power approximately 6 million homes. Construction is expected to begin in 2027, with the first phase operational by 2031.\n\nThe agreement includes provisions for revenue sharing, environmental impact mitigation, and a joint oversight body composed of representatives from all three signatory nations. Critics have noted, however, that the agreement is silent on labor standards and on the rights of indigenous communities along the route.",
            pullQuote = null,
            category = Category.BREAKING,
            readTimeMinutes = 3,
            isArchived = false,
            byline = null,
            sourceIds = listOf("hn", "x"),
            sourceUrls = listOf(
                "https://news.ycombinator.com/item?id=sample3",
                "https://x.com/sample3/status/123"
            ),
            topicTag = topic,
            timestamp = now - 1000L * 60 * 60 * 5,
            createdAt = now
        ),
        DigestItem(
            id = stableId("biotech-phase3", topic),
            topicId = topic,
            editionId = editionId,
            title = "Pioneering Biotech Firm Announces Phase 3 Success for Novel Therapy",
            summary = "A clinical-stage biotech company reported overwhelmingly positive Phase 3 trial results for a therapy targeting a previously treatment-resistant form of leukemia.",
            detailSummary = "Helix Therapeutics announced Tuesday that its lead candidate, HTX-204, met its primary endpoint in a Phase 3 clinical trial for relapsed acute myeloid leukemia. The therapy, a bispecific antibody, demonstrated a 67% overall response rate in patients who had failed at least two prior lines of treatment — nearly double the response rate of the current standard of care.\n\nThe trial enrolled 412 patients across 38 sites in 11 countries. Detailed results will be presented at the American Society of Hematology annual meeting in December.\n\nAnalysts were quick to note both the clinical significance and the commercial implications. Acute myeloid leukemia affects approximately 120,000 people globally each year, and the relapsed/refractory population represents the highest unmet need in the indication.\n\n\"This is the most convincing dataset I've seen in this space in over a decade,\" said Dr. Priya Ramaswamy, an oncologist not involved in the trial. \"If the safety profile holds up in regulatory review, this becomes the new backbone of therapy.\"\n\nHelix expects to file for FDA approval in the first half of next year.",
            pullQuote = "This is the most convincing dataset I've seen in this space in over a decade.",
            category = Category.BREAKING,
            readTimeMinutes = 4,
            isArchived = false,
            byline = "By Sarah Chen · ${humanDate(now)}",
            sourceIds = listOf("reddit"),
            sourceUrls = listOf("https://reddit.com/r/biotech/comments/sample4"),
            topicTag = topic,
            timestamp = now - 1000L * 60 * 60 * 8,
            createdAt = now
        ),
        DigestItem(
            id = stableId("return-to-office-mandates", topic),
            topicId = topic,
            editionId = editionId,
            title = "Why the Return to Office Mandates Are Quietly Failing",
            summary = "Despite high-profile mandates from Fortune 500 CEOs, the data tells a more complicated story: most companies are quietly walking back their in-office requirements.",
            detailSummary = "When major CEOs began announcing strict return-to-office mandates in early 2024, the consensus was that the great work-from-home experiment was over. Two years later, the data suggests a different story.\n\nA new analysis of 2,400 mid-to-large U.S. companies finds that only 14% have maintained strict 5-day in-office requirements, while 41% have either softened their mandates or quietly stopped enforcing them. The remaining 45% never imposed strict mandates in the first place.\n\nThe shift is driven by measurable outcomes. Voluntary attrition at strict 5-day companies is running 38% higher than at flexible-hybrid companies, even after controlling for industry and role. Employee Net Promoter Scores tell a similar story: strict-mandate companies score 22 points lower than their flexible peers.\n\n\"The companies that pushed hardest are now quietly walking it back,\" said Dr. Anita Desai, who led the analysis. \"But they don't want to make a big announcement, because that would imply the original mandate was a mistake.\"\n\nThe most common pattern: a 3-day minimum that gets flexibly enforced, often with team-level discretion. Productivity metrics, where they can be measured, show no clear advantage for either model.\n\nThe lesson, according to Desai, is that culture cannot be mandated. \"Companies that figured out how to build culture intentionally — through rituals, in-person offsites, and intentional collaboration days — are thriving regardless of their daily policy. The ones that issued mandates without that foundation are now paying the cost.\"",
            pullQuote = "Culture cannot be mandated. Companies that figured out how to build culture intentionally are thriving; the ones that issued mandates without that foundation are now paying the cost.",
            category = Category.OPINION,
            readTimeMinutes = 6,
            isArchived = false,
            byline = "By Marcus Whitfield · ${humanDate(now)}",
            sourceIds = listOf("reddit", "x"),
            sourceUrls = listOf(
                "https://reddit.com/r/remotework/comments/sample5",
                "https://x.com/sample5/status/456"
            ),
            topicTag = topic,
            timestamp = now - 1000L * 60 * 60 * 10,
            createdAt = now
        )
    )

    private fun stableId(seed: String, topic: String): String {
        val raw = "$seed|$topic"
        return "d_" + raw.hashCode().toUInt().toString(16)
    }

    private fun isoDate(ms: Long): String {
        val date = java.util.Date(ms)
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return fmt.format(date)
    }

    private fun humanDate(ms: Long): String {
        val date = java.util.Date(ms)
        val fmt = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
        return fmt.format(date)
    }

    private fun isWeekend(ms: Long): Boolean {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ms }
        val day = cal.get(java.util.Calendar.DAY_OF_WEEK)
        return day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY
    }
}
