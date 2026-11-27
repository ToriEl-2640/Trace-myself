package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GrantsSearchRequest(
    @Json(name = "rows") val rows: Int = 20,
    @Json(name = "keyword") val keyword: String = "",
    @Json(name = "oppStatuses") val oppStatuses: String = "posted|forecasted",
    @Json(name = "sortBy") val sortBy: String = "openDate|desc",
    @Json(name = "startRecordNum") val startRecordNum: Int = 0
)

@JsonClass(generateAdapter = true)
data class GrantsSearchResponse(
    @Json(name = "errorcode") val errorcode: Int = 0,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: GrantsSearchData? = null
)

@JsonClass(generateAdapter = true)
data class GrantsSearchData(
    @Json(name = "hitCount") val hitCount: Int = 0,
    @Json(name = "oppHits") val oppHits: List<GrantsOppHit> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GrantsOppHit(
    @Json(name = "id") val id: String? = null,
    @Json(name = "number") val number: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "agencyCode") val agencyCode: String? = null,
    @Json(name = "agencyName") val agencyName: String? = null,
    @Json(name = "openDate") val openDate: String? = null,
    @Json(name = "closeDate") val closeDate: String? = null,
    @Json(name = "oppStatus") val oppStatus: String? = null,
    @Json(name = "docType") val docType: String? = null,
    @Json(name = "alnist") val alnist: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GrantsFetchOpportunityRequest(
    @Json(name = "opportunityId") val opportunityId: Long
)

@JsonClass(generateAdapter = true)
data class GrantsFetchOpportunityResponse(
    @Json(name = "errorcode") val errorcode: Int = 0,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: GrantsOpportunityDetailData? = null
)

@JsonClass(generateAdapter = true)
data class GrantsOpportunityDetailData(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "opportunityNumber") val opportunityNumber: String? = null,
    @Json(name = "opportunityTitle") val opportunityTitle: String? = null,
    @Json(name = "owningAgencyCode") val owningAgencyCode: String? = null,
    @Json(name = "synopsis") val synopsis: GrantsSynopsis? = null
)

@JsonClass(generateAdapter = true)
data class GrantsSynopsis(
    @Json(name = "agencyName") val agencyName: String? = null,
    @Json(name = "agencyCode") val agencyCode: String? = null,
    @Json(name = "synopsisDesc") val synopsisDesc: String? = null,
    @Json(name = "responseDateDesc") val responseDateDesc: String? = null,
    @Json(name = "postingDate") val postingDate: String? = null,
    @Json(name = "awardCeiling") val awardCeiling: String? = null,
    @Json(name = "awardCeilingFormatted") val awardCeilingFormatted: String? = null,
    @Json(name = "awardFloor") val awardFloor: String? = null,
    @Json(name = "awardFloorFormatted") val awardFloorFormatted: String? = null,
    @Json(name = "agencyContactEmail") val agencyContactEmail: String? = null,
    @Json(name = "agencyContactName") val agencyContactName: String? = null
)

interface GrantsGovApiService {

    @POST("v1/api/search2")
    suspend fun searchOpportunities(
        @Body request: GrantsSearchRequest
    ): GrantsSearchResponse

    @POST("v1/api/fetchOpportunity")
    suspend fun fetchOpportunityDetail(
        @Body request: GrantsFetchOpportunityRequest
    ): GrantsFetchOpportunityResponse

    companion object {
        private const val BASE_URL = "https://api.grants.gov/"

        fun create(): GrantsGovApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GrantsGovApiService::class.java)
        }
    }
}
