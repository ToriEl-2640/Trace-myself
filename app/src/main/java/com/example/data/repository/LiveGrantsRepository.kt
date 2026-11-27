package com.example.data.repository

import android.content.Context
import androidx.core.text.HtmlCompat
import com.example.data.local.CachedGrantDao
import com.example.data.local.CachedGrantEntity
import com.example.data.local.TraceLocalDatabase
import com.example.data.model.CatalogOpportunity
import com.example.data.remote.GrantsFetchOpportunityRequest
import com.example.data.remote.GrantsGovApiService
import com.example.data.remote.GrantsOppHit
import com.example.data.remote.GrantsSearchRequest
import com.example.data.remote.GrantsSynopsis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LiveGrantsRepository(
    private val apiService: GrantsGovApiService,
    private val grantDao: CachedGrantDao
) {
    constructor(context: Context) : this(
        apiService = GrantsGovApiService.create(),
        grantDao = TraceLocalDatabase.getInstance(context).cachedGrantDao()
    )

    fun observeLiveGrants(): Flow<List<CatalogOpportunity>> =
        grantDao.observeAllLiveGrants().map { entities ->
            entities.map { it.toCatalogOpportunity() }
        }

    fun observeLastSyncMillis(): Flow<Long?> = grantDao.observeLastSyncMillis()

    suspend fun isCacheStale(maxAgeMillis: Long = SIX_HOURS_MILLIS): Boolean {
        val lastSync = grantDao.getLastSyncMillis() ?: return true
        return (System.currentTimeMillis() - lastSync) > maxAgeMillis
    }

    suspend fun syncLiveGrants(keyword: String = ""): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchOpportunities(
                GrantsSearchRequest(
                    rows = 20,
                    keyword = keyword.trim(),
                    oppStatuses = "posted|forecasted",
                    sortBy = "openDate|desc"
                )
            )

            if (response.errorcode != 0) {
                return@withContext Result.failure(
                    IllegalStateException(response.msg ?: "Grants.gov API returned error ${response.errorcode}")
                )
            }

            val hits = response.data?.oppHits.orEmpty().filter { !it.id.isNullOrBlank() && !it.title.isNullOrBlank() }
            val now = System.currentTimeMillis()

            // Enrich the top 6 hits in parallel with award ceiling and synopsis text
            val enrichedDetails: Map<String, GrantsSynopsis?> = coroutineScope {
                hits.take(6).map { hit ->
                    async {
                        val numericId = hit.id?.toLongOrNull()
                        if (numericId != null) {
                            try {
                                val detailResp = apiService.fetchOpportunityDetail(
                                    GrantsFetchOpportunityRequest(opportunityId = numericId)
                                )
                                hit.id!! to detailResp.data?.synopsis
                            } catch (ce: CancellationException) {
                                throw ce
                            } catch (_: Exception) {
                                hit.id!! to null
                            }
                        } else {
                            (hit.id ?: "") to null
                        }
                    }
                }.awaitAll().toMap()
            }

            val entities = hits.map { hit ->
                val rawId = hit.id.orEmpty()
                mapHitToEntity(hit, enrichedDetails[rawId], now)
            }

            if (entities.isNotEmpty() || keyword.isBlank()) {
                grantDao.replaceAllLiveGrants(entities)
            }

            Result.success(entities.size)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLiveOpportunityDetail(opportunityId: String): Result<CatalogOpportunity?> =
        withContext(Dispatchers.IO) {
            try {
                val rawNumericId = opportunityId.removePrefix("grantsgov_").toLongOrNull()
                    ?: return@withContext Result.success(null)

                val resp = apiService.fetchOpportunityDetail(
                    GrantsFetchOpportunityRequest(opportunityId = rawNumericId)
                )
                val data = resp.data ?: return@withContext Result.success(null)
                val synopsis = data.synopsis

                val mapped = CatalogOpportunity(
                    id = "grantsgov_$rawNumericId",
                    title = data.opportunityTitle?.trim().orEmpty().ifBlank { "Grants.gov Opportunity #$rawNumericId" },
                    organization = synopsis?.agencyName?.trim().orEmpty()
                        .ifBlank { data.owningAgencyCode ?: "Federal Granting Agency" },
                    fundingAmount = formatAwardRange(synopsis),
                    deadline = synopsis?.responseDateDesc?.trim().orEmpty().ifBlank { "Check Official Portal" },
                    category = classifyCategory(data.opportunityTitle.orEmpty()),
                    focusArea = listOfNotNull(
                        data.opportunityNumber?.takeIf { it.isNotBlank() },
                        synopsis?.agencyCode?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank { "Public Research Grant" },
                    eligibleRegions = "Check Eligibility in Official Solicitation",
                    description = cleanHtml(synopsis?.synopsisDesc).ifBlank {
                        "Official Grants.gov Notice (${data.opportunityNumber.orEmpty()}). Open the portal link below for full eligibility, funding guidelines, and submission package."
                    },
                    applicationUrl = "https://www.grants.gov/search-results-detail/$rawNumericId",
                    isLiveFeed = true,
                    openDate = synopsis?.postingDate.orEmpty(),
                    opportunityNumber = data.opportunityNumber.orEmpty()
                )

                // Update cached record with full detail
                grantDao.upsertGrant(
                    CachedGrantEntity(
                        id = mapped.id,
                        title = mapped.title,
                        organization = mapped.organization,
                        fundingAmount = mapped.fundingAmount,
                        deadline = mapped.deadline,
                        category = mapped.category,
                        focusArea = mapped.focusArea,
                        eligibleRegions = mapped.eligibleRegions,
                        description = mapped.description,
                        applicationUrl = mapped.applicationUrl,
                        isLiveFeed = true,
                        openDate = mapped.openDate,
                        opportunityNumber = mapped.opportunityNumber,
                        fetchedAtMillis = System.currentTimeMillis()
                    )
                )

                Result.success(mapped)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun mapHitToEntity(
        hit: GrantsOppHit,
        synopsis: GrantsSynopsis?,
        fetchedAtMillis: Long
    ): CachedGrantEntity {
        val rawId = hit.id.orEmpty()
        val title = hit.title.orEmpty().trim()
        val agencyName = synopsis?.agencyName?.takeIf { it.isNotBlank() }
            ?: hit.agencyName?.takeIf { it.isNotBlank() }
            ?: hit.agencyCode?.takeIf { it.isNotBlank() }
            ?: "Federal Agency"
        val oppNum = hit.number.orEmpty().trim()
        val statusLabel = hit.oppStatus.orEmpty().replaceFirstChar { it.uppercase() }.ifBlank { "Posted" }
        val openDate = hit.openDate.orEmpty().trim()
        val closeDate = hit.closeDate.orEmpty().trim()

        val fundingDisplay = formatAwardRange(synopsis).let { range ->
            if (range != "See Notice") range
            else if (statusLabel.equals("Forecasted", ignoreCase = true)) "Forecasted Award"
            else "Open ($statusLabel)"
        }

        val deadlineDisplay = closeDate.ifBlank {
            if (statusLabel.equals("Forecasted", ignoreCase = true)) "Forecasted (Opening Soon)"
            else "Rolling / See Portal"
        }

        val cleanedSynopsis = cleanHtml(synopsis?.synopsisDesc)
        val fallbackDesc = buildString {
            append("Live $statusLabel grant opportunity")
            if (oppNum.isNotBlank()) append(" (#$oppNum)")
            append(" from $agencyName.")
            if (openDate.isNotBlank()) append(" Posted on $openDate.")
            if (!hit.alnist.isNullOrEmpty()) append(" Assistance Listing: ${hit.alnist.joinToString(", ")}.")
        }

        return CachedGrantEntity(
            id = "grantsgov_$rawId",
            title = title,
            organization = agencyName,
            fundingAmount = fundingDisplay,
            deadline = deadlineDisplay,
            category = classifyCategory(title),
            focusArea = listOfNotNull(
                oppNum.takeIf { it.isNotBlank() },
                hit.agencyCode?.takeIf { it.isNotBlank() }
            ).joinToString(" • ").ifBlank { "Research & Innovation" },
            eligibleRegions = "U.S. & Eligible International Partners",
            description = cleanedSynopsis.ifBlank { fallbackDesc },
            applicationUrl = "https://www.grants.gov/search-results-detail/$rawId",
            isLiveFeed = true,
            openDate = openDate,
            opportunityNumber = oppNum,
            fetchedAtMillis = fetchedAtMillis
        )
    }

    private fun formatAwardRange(synopsis: GrantsSynopsis?): String {
        if (synopsis == null) return "See Notice"
        val floor = synopsis.awardFloorFormatted?.takeIf { it.isNotBlank() }
            ?: synopsis.awardFloor?.takeIf { it.isNotBlank() && it != "0" }?.let { "$$it" }
        val ceiling = synopsis.awardCeilingFormatted?.takeIf { it.isNotBlank() }
            ?: synopsis.awardCeiling?.takeIf { it.isNotBlank() && it != "0" }?.let { "$$it" }

        return when {
            floor != null && ceiling != null && floor != ceiling -> "$floor – $ceiling"
            ceiling != null -> "Up to $ceiling"
            floor != null -> "From $floor"
            else -> "See Notice"
        }
    }

    private fun classifyCategory(title: String): String {
        val lower = title.lowercase()
        return when {
            lower.contains("fellow") || lower.contains("postdoc") || lower.contains("scholar") || lower.contains("training") -> "Fellowship"
            lower.contains("challenge") || lower.contains("prize") || lower.contains("competition") -> "Innovation Challenge"
            lower.contains("residen") || lower.contains("visiting") -> "Research Residency"
            else -> "Grant"
        }
    }

    private fun cleanHtml(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    companion object {
        const val SIX_HOURS_MILLIS = 6L * 60L * 60L * 1000L
    }
}
