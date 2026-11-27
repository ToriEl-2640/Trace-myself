package com.example.data.model

data class ResearchTopic(
    val id: String,
    val title: String,
    val category: TopicCategory,
    val sector: String,
    val region: String,
    val urgencyLevel: String, // High, Medium, Emerging
    val overview: String,
    val keyQuestions: List<String>,
    val relevanceToIndustry: String,
    val relatedDisciplines: List<String>,
    val potentialImpact: String
)

enum class TopicCategory(val displayName: String) {
    COMMUNITY_CHALLENGE("Everyday Challenge"),
    EMERGING_INDUSTRY("Emerging Industry"),
    UNANSWERED_QUESTION("Unanswered Question"),
    FINANCE_BLOCKCHAIN("Finance & Blockchain")
}

data class CatalogOpportunity(
    val id: String,
    val title: String,
    val organization: String,
    val fundingAmount: String,
    val deadline: String,
    val category: String,
    val focusArea: String,
    val eligibleRegions: String,
    val description: String,
    val applicationUrl: String,
    val isLiveFeed: Boolean = false,
    val openDate: String = "",
    val opportunityNumber: String = ""
)

data class MetricDataPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float = 0f,
    val category: String = "",
    val unit: String = ""
)

data class DatasetAnalysis(
    val title: String,
    val subtitle: String,
    val description: String,
    val insightSummary: String,
    val dataPoints: List<MetricDataPoint>,
    val keyTakeaway: String
)
