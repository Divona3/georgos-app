package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Unauthenticated NASA Earthdata CMR endpoint querying ECOSTRESS Collection 2
 * 70m gridded/tiled products (https://ecostress.jpl.nasa.gov/):
 * - ECO_L2T_LSTE: Land Surface Temperature & Emissivity
 * - ECO_L3T_JET: Evapotranspiration PT-JPL
 * - ECO_L4T_ESI: Evaporative Stress Index
 * - ECO_L4T_WUE: Water Use Efficiency
 */
interface EcostressCmrApi {

    @GET("search/granules.json")
    suspend fun searchGranulesByPoint(
        @Query("short_name") shortName: String,
        @Query("point") pointLonLat: String,
        @Query("sort_key") sortKey: String = "-start_date",
        @Query("page_size") pageSize: Int = 4
    ): CmrGranulesResponse

    @GET("search/granules.json")
    suspend fun searchGranulesByBox(
        @Query("short_name") shortName: String,
        @Query("bounding_box") boundingBox: String,
        @Query("sort_key") sortKey: String = "-start_date",
        @Query("page_size") pageSize: Int = 4
    ): CmrGranulesResponse

    companion object {
        private const val BASE_URL = "https://cmr.earthdata.nasa.gov/"

        val PRODUCT_SHORT_NAMES = listOf(
            "ECO_L4T_ESI",
            "ECO_L3T_JET",
            "ECO_L2T_LSTE",
            "ECO_L4T_WUE"
        )

        private val MGRS_REGEX = Regex("""_(\d{2}[A-Z]{3})_""")

        fun extractMgrsTile(granuleTitle: String?): String? {
            if (granuleTitle.isNullOrBlank()) return null
            return MGRS_REGEX.find(granuleTitle)?.groupValues?.getOrNull(1)
        }

        fun extractBrowseUrl(links: List<CmrGranuleLink>?): String {
            if (links.isNullOrEmpty()) return ""
            val iconLink = links.firstOrNull { link ->
                val rel = link.rel.orEmpty()
                val href = link.href.orEmpty()
                (rel.contains("#icon", ignoreCase = true) || rel.contains("browse", ignoreCase = true)) &&
                    href.startsWith("https://")
            }
            return iconLink?.href.orEmpty()
        }

        fun formatOverpassDate(isoTimestamp: String?): String {
            if (isoTimestamp.isNullOrBlank()) return "Awaiting ISS Overpass"
            return if (isoTimestamp.length >= 16) {
                isoTimestamp.substring(0, 10) + " " + isoTimestamp.substring(11, 16) + " UTC"
            } else {
                isoTimestamp
            }
        }

        fun create(): EcostressCmrApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(EcostressCmrApi::class.java)
        }
    }
}

@JsonClass(generateAdapter = true)
data class CmrGranulesResponse(
    val feed: CmrFeed? = null
)

@JsonClass(generateAdapter = true)
data class CmrFeed(
    val updated: String? = null,
    val entry: List<CmrGranuleEntry>? = null
)

@JsonClass(generateAdapter = true)
data class CmrGranuleEntry(
    val id: String? = null,
    val title: String? = null,
    @Json(name = "producer_granule_id") val producerGranuleId: String? = null,
    @Json(name = "time_start") val timeStart: String? = null,
    @Json(name = "time_end") val timeEnd: String? = null,
    @Json(name = "dataset_id") val datasetId: String? = null,
    @Json(name = "data_center") val dataCenter: String? = null,
    val boxes: List<String>? = null,
    val links: List<CmrGranuleLink>? = null
)

@JsonClass(generateAdapter = true)
data class CmrGranuleLink(
    val rel: String? = null,
    val type: String? = null,
    val href: String? = null
)
