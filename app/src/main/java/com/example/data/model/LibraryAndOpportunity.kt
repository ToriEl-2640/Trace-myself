package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LibraryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val authors: String = "",
    val publicationYear: String = "",
    val source: String = "",
    val domain: String = "",
    val summary: String = "",
    val keyFindings: String = "",
    val simplifiedExplanation: String = "",
    val url: String = "",
    val isBookmarked: Boolean = false,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TrackedOpportunity(
    @DocumentId val id: String = "",
    val userId: String = "",
    val opportunityId: String = "",
    val title: String = "",
    val organization: String = "",
    val fundingAmount: String = "",
    val deadline: String = "",
    val category: String = "Grant", // Grant, Fellowship, Innovation Challenge, Research Residency
    val stage: String = "interested", // interested, preparing_proposal, submitted, awarded
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
