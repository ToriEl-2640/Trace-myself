package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ProblemInvestigation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val sector: String = "Agriculture & Food",
    val location: String = "",
    val status: String = "investigating", // draft, investigating, evidence_collected, solution_drafted, completed
    val problemStatement: String = "",
    val existingResearch: String = "",
    val unaddressedGaps: String = "",
    val evidenceNotes: String = "",
    val proposedSolutions: String = "",
    val opportunitySummary: String = "",
    val evidenceCount: Long = 0L,
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FieldEvidence(
    @DocumentId val id: String = "",
    val userId: String = "",
    val problemId: String = "",
    val title: String = "",
    val evidenceType: String = "observation", // observation, metric, interview, photo_note, cost_data
    val content: String = "",
    val dataValue: String = "",
    val locationTag: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
