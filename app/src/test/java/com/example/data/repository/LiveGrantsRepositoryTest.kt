package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TraceLocalDatabase
import com.example.data.remote.GrantsFetchOpportunityRequest
import com.example.data.remote.GrantsFetchOpportunityResponse
import com.example.data.remote.GrantsGovApiService
import com.example.data.remote.GrantsOppHit
import com.example.data.remote.GrantsOpportunityDetailData
import com.example.data.remote.GrantsSearchData
import com.example.data.remote.GrantsSearchRequest
import com.example.data.remote.GrantsSearchResponse
import com.example.data.remote.GrantsSynopsis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LiveGrantsRepositoryTest {

    private lateinit var database: TraceLocalDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TraceLocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncLiveGrants_cachesMappedOpportunitiesAndDetailSynopsis() = runTest {
        val fakeApi = object : GrantsGovApiService {
            override suspend fun searchOpportunities(request: GrantsSearchRequest): GrantsSearchResponse {
                return GrantsSearchResponse(
                    errorcode = 0,
                    msg = "Webservice Succeeds",
                    data = GrantsSearchData(
                        hitCount = 2,
                        oppHits = listOf(
                            GrantsOppHit(
                                id = "350123",
                                number = "USDA-NIFA-2026-01",
                                title = "Smallholder Climate-Smart Agriculture Research Fellowship",
                                agencyCode = "USDA-NIFA",
                                agencyName = "National Institute of Food and Agriculture",
                                openDate = "11/01/2026",
                                closeDate = "01/15/2027",
                                oppStatus = "posted",
                                docType = "synopsis",
                                alnist = listOf("10.310")
                            ),
                            GrantsOppHit(
                                id = "350124",
                                number = "EPA-CHALLENGE-2026",
                                title = "Community Plastic Waste Innovation Challenge",
                                agencyCode = "EPA",
                                agencyName = "Environmental Protection Agency",
                                openDate = "11/10/2026",
                                closeDate = "",
                                oppStatus = "forecasted",
                                docType = "synopsis",
                                alnist = listOf("66.511")
                            )
                        )
                    )
                )
            }

            override suspend fun fetchOpportunityDetail(
                request: GrantsFetchOpportunityRequest
            ): GrantsFetchOpportunityResponse {
                return if (request.opportunityId == 350123L) {
                    GrantsFetchOpportunityResponse(
                        errorcode = 0,
                        data = GrantsOpportunityDetailData(
                            id = 350123L,
                            opportunityNumber = "USDA-NIFA-2026-01",
                            opportunityTitle = "Smallholder Climate-Smart Agriculture Research Fellowship",
                            owningAgencyCode = "USDA-NIFA",
                            synopsis = GrantsSynopsis(
                                agencyName = "National Institute of Food and Agriculture",
                                agencyCode = "USDA-NIFA",
                                synopsisDesc = "<p>Supports <b>post-harvest</b> and climate resilience research.</p>",
                                responseDateDesc = "01/15/2027",
                                postingDate = "11/01/2026",
                                awardFloorFormatted = "$50,000",
                                awardCeilingFormatted = "$250,000"
                            )
                        )
                    )
                } else {
                    GrantsFetchOpportunityResponse(errorcode = 0, data = null)
                }
            }
        }

        val repo = LiveGrantsRepository(fakeApi, database.cachedGrantDao())
        assertTrue(repo.isCacheStale())

        val result = repo.syncLiveGrants()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        assertFalse(repo.isCacheStale())

        val cached = repo.observeLiveGrants().first()
        assertEquals(2, cached.size)

        val fellowship = cached.first { it.id == "grantsgov_350123" }
        assertEquals("Fellowship", fellowship.category)
        assertEquals("$50,000 – $250,000", fellowship.fundingAmount)
        assertEquals("Supports post-harvest and climate resilience research.", fellowship.description)
        assertEquals("https://www.grants.gov/search-results-detail/350123", fellowship.applicationUrl)
        assertTrue(fellowship.isLiveFeed)

        val challenge = cached.first { it.id == "grantsgov_350124" }
        assertEquals("Innovation Challenge", challenge.category)
        assertEquals("Forecasted (Opening Soon)", challenge.deadline)
    }
}
