package com.example.data.repository

import android.util.Log
import com.example.data.model.FieldEvidence
import com.example.data.model.ProblemInvestigation
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val TAG = "AiResearchRepository"

sealed interface AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>
    data class Error(val message: String) : AiResult<Nothing>
}

class AiResearchRepository {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 40
            }
        )
    }

    suspend fun simplifyResearchPaper(denseText: String): AiResult<String> {
        val prompt = """
            You are TRACE AI, a research translation specialist whose mission is to make scientific papers accessible to ordinary people, community investigators, and independent innovators.
            
            Analyze the following research excerpt or paper abstract:
            \"\"\"
            $denseText
            \"\"\"
            
            Provide a clean, structured breakdown in plain, accessible language:
            1. Core Finding in 1 Sentence (The Bottom Line)
            2. Plain English Explanation (No jargon; explain like you're talking to a smart neighbor)
            3. Real-World Practical Implications (How this matters to businesses, farmers, or communities)
            4. Key Limitations or Caveats (What the paper did NOT prove or where it only applies)
            5. Potential Local Action (What an everyday innovator can do with this knowledge)
        """.trimIndent()

        return executePrompt(prompt)
    }

    suspend fun identifyResearchGaps(problemTitle: String, problemDescription: String, sector: String): AiResult<String> {
        val prompt = """
            You are TRACE AI, an expert research methodologist. An everyday researcher has observed the following real-world problem:
            
            Sector: $sector
            Problem Title: $problemTitle
            Problem Observation: $problemDescription
            
            Analyze this problem against existing global and local research:
            1. What has traditional academic research ALREADY explored about this?
            2. Identify 3 Critical Unaddressed Gaps (What researchers and governments have overlooked, failed to scale, or ignored at the grassroots level).
            3. Why Existing Interventions Failed (Economic friction, behavioral barriers, maintenance traps).
            4. 3 High-Impact Research Questions the user could investigate on the ground.
        """.trimIndent()

        return executePrompt(prompt)
    }

    suspend fun generateHypothesisAndMethodology(problemStatement: String): AiResult<String> {
        val prompt = """
            You are TRACE AI, formulating a rigorous yet accessible scientific research plan for an everyday problem investigator.
            
            Problem Context:
            \"\"\"
            $problemStatement
            \"\"\"
            
            Formulate:
            1. Working Hypothesis (A testable 'If... then...' statement grounded in real conditions)
            2. Independent & Dependent Variables (What the researcher changes vs what they measure)
            3. Step-by-Step Low-Cost Methodology (Practical field methods using mobile phones, local measurements, and simple surveys)
            4. Data Collection Checklist (5 quantitative and qualitative data points to record)
            5. Potential Bias & Confounding Factors to Watch Out For
        """.trimIndent()

        return executePrompt(prompt)
    }

    suspend fun generateFieldSurveyQuestions(problemTitle: String, targetAudience: String): AiResult<String> {
        val prompt = """
            You are TRACE AI, designing a field survey questionnaire for community researchers.
            
            Research Topic: $problemTitle
            Target Respondents: $targetAudience
            
            Create a targeted 8-question field survey:
            - 3 Quantitative Baseline Questions (Costs, percentages, frequency, hours lost)
            - 3 Qualitative Root Cause Questions (Daily pain points, workarounds, failed attempts)
            - 2 Willingness-to-Adopt / Willingness-to-Pay Questions (Testing solution viability)
            
            Include brief interviewer tips for overcoming response bias in community settings.
        """.trimIndent()

        return executePrompt(prompt)
    }

    suspend fun draftResearchExecutiveSummary(
        problem: ProblemInvestigation,
        evidenceList: List<FieldEvidence>
    ): AiResult<String> {
        val evidenceText = if (evidenceList.isEmpty()) {
            problem.evidenceNotes.ifBlank { "No specific evidence points logged." }
        } else {
            evidenceList.joinToString("\n") { "- [${it.evidenceType.uppercase()}] ${it.title}: ${it.content} (Value: ${it.dataValue}, Loc: ${it.locationTag})" }
        }

        val prompt = """
            You are TRACE AI, compiling a comprehensive, professional Research & Discovery Report from field investigation notes.
            
            Project: ${problem.title}
            Sector: ${problem.sector}
            Location: ${problem.location}
            Problem Statement: ${problem.problemStatement}
            Existing Literature: ${problem.existingResearch}
            Unaddressed Gaps: ${problem.unaddressedGaps}
            Field Evidence Collected:
            $evidenceText
            Proposed Solutions: ${problem.proposedSolutions}
            
            Generate a publication-grade Structured Research Brief:
            1. Executive Summary & Problem Scope (Quantifying the scale and human/economic toll)
            2. Evidence Synthesis (Connecting field observations with structural barriers)
            3. The Core Innovation & Solution Architecture (Why this approach succeeds where others failed)
            4. Economic & Business Viability (Unit economics, market size, sustainability)
            5. Grant & Collaboration Pitch (A concise 3-paragraph pitch for fellowships, donors, and angel investors)
        """.trimIndent()

        return executePrompt(prompt)
    }

    suspend fun streamResearchChat(
        systemContext: String,
        userQuery: String,
        onChunk: (String) -> Unit
    ): AiResult<String> {
        val fullPrompt = if (systemContext.isNotBlank()) {
            "System Context: $systemContext\n\nUser Question: $userQuery"
        } else {
            userQuery
        }

        val accumulated = StringBuilder()
        return try {
            withTimeout(60_000L) {
                model.generateContentStream(fullPrompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    accumulated.append(chunkText)
                    onChunk(accumulated.toString())
                }
            }
            if (accumulated.isNotBlank()) {
                AiResult.Success(accumulated.toString())
            } else {
                AiResult.Error("AI returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming generation timed out", e)
            AiResult.Error("Generation timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation failed", e)
            AiResult.Error(e.localizedMessage ?: "AI generation failed.")
        }
    }

    private suspend fun executePrompt(prompt: String): AiResult<String> {
        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                AiResult.Success(text)
            } else {
                AiResult.Error("AI returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Generation timed out", e)
            AiResult.Error("AI request timed out after 60 seconds. Please check your connection and retry.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            AiResult.Error(e.localizedMessage ?: "AI generation encountered an unexpected error.")
        }
    }
}
