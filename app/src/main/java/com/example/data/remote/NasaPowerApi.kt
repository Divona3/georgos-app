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

interface NasaPowerApi {
    @GET("api/temporal/daily/point")
    suspend fun getDailyAgroPoint(
        @Query("parameters") parameters: String = "T2M,T2M_MAX,T2M_MIN,TS,PRECTOTCORR,ALLSKY_SFC_SW_DWN,EVPTRNS,RH2M,GWETTOP,GWETROOT,WS2M",
        @Query("community") community: String = "AG",
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("format") format: String = "JSON"
    ): PowerPointResponse

    companion object {
        private const val BASE_URL = "https://power.larc.nasa.gov/"

        fun create(): NasaPowerApi {
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
                .create(NasaPowerApi::class.java)
        }
    }
}

@JsonClass(generateAdapter = true)
data class PowerPointResponse(
    val properties: PowerProperties? = null
)

@JsonClass(generateAdapter = true)
data class PowerProperties(
    val parameter: PowerParameters? = null
)

@JsonClass(generateAdapter = true)
data class PowerParameters(
    @Json(name = "T2M") val temperature: Map<String, Double>? = null,
    @Json(name = "T2M_MAX") val temperatureMax: Map<String, Double>? = null,
    @Json(name = "T2M_MIN") val temperatureMin: Map<String, Double>? = null,
    @Json(name = "TS") val earthSkinTemp: Map<String, Double>? = null,
    @Json(name = "PRECTOTCORR") val precipitation: Map<String, Double>? = null,
    @Json(name = "ALLSKY_SFC_SW_DWN") val solarIrradiance: Map<String, Double>? = null,
    @Json(name = "EVPTRNS") val evapTransFlux: Map<String, Double>? = null,
    @Json(name = "RH2M") val humidity: Map<String, Double>? = null,
    @Json(name = "GWETTOP") val surfaceSoilWetness: Map<String, Double>? = null,
    @Json(name = "GWETROOT") val rootSoilWetness: Map<String, Double>? = null,
    @Json(name = "WS2M") val windSpeed2m: Map<String, Double>? = null
)
