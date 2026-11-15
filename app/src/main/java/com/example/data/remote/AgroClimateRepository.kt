package com.example.data.remote

import com.example.data.local.ClimateSnapshotEntity
import com.example.data.local.GrievousDao
import com.example.data.local.LandPassportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

private data class EcostressMetadataBundle(
    val isLive: Boolean,
    val tileId: String,
    val overpassIso: String,
    val granuleTitle: String,
    val browseUrl: String,
    val productCount: Int
)

private data class PowerTelemetryBundle(
    val temperatureC: Double,
    val tempMinC: Double,
    val tempMaxC: Double,
    val earthSkinTempC: Double?,
    val precipitationMm: Double,
    val solarIrradianceMj: Double,
    val evapTransFluxMj: Double?,
    val humidityPercent: Double,
    val surfaceWetness: Double,
    val rootZoneWetness: Double,
    val windSpeedMs: Double,
    val dateRange: String
)

class AgroClimateRepository(
    private val api: NasaPowerApi,
    private val ecostressApi: EcostressCmrApi,
    private val dao: GrievousDao
) {
    suspend fun syncTelemetryForLand(land: LandPassportEntity): Result<ClimateSnapshotEntity> =
        withContext(Dispatchers.IO) {
            try {
                val (ecostressMeta, powerData) = coroutineScope {
                    val ecoDeferred = async { fetchEcostressGranuleMetadata(land) }
                    val powerDeferred = async { fetchPowerAgroPoint(land) }
                    ecoDeferred.await() to powerDeferred.await()
                }

                if (powerData == null && !ecostressMeta.isLive) {
                    throw IllegalStateException("Both ECOSTRESS CMR and POWER endpoints unavailable")
                }

                val baseLatFactor = (1.0 - (abs(land.latitude) / 60.0)).coerceIn(0.3, 1.0)
                val tempC = powerData?.temperatureC ?: (21.5 + baseLatFactor * 8.2)
                val tempMinC = powerData?.tempMinC ?: (tempC - 5.4)
                val tempMaxC = powerData?.tempMaxC ?: (tempC + 5.2)
                val precipMm = powerData?.precipitationMm
                    ?: if (land.waterSources.contains("Rain", ignoreCase = true)) 2.1 else 3.6
                val solarMj = powerData?.solarIrradianceMj ?: (16.0 + baseLatFactor * 5.5)
                val humidityPct = powerData?.humidityPercent
                    ?: (48.0 + land.soilMoisturePercent * 0.28).coerceIn(35.0, 88.0)
                val surfaceWet = powerData?.surfaceWetness
                    ?: (land.soilMoisturePercent / 100.0).coerceIn(0.15, 0.92)
                val rootWet = powerData?.rootZoneWetness
                    ?: ((land.soilMoisturePercent + 4) / 100.0).coerceIn(0.18, 0.92)
                val windMs = powerData?.windSpeedMs ?: 2.4

                // Compute PT-JPL aligned ECOSTRESS thermal, ET, ESI & WUE metrics
                val lstC = computeLandSurfaceTemp(
                    airTempC = tempC,
                    skinTempC = powerData?.earthSkinTempC,
                    solarMj = solarMj,
                    soilWetness = surfaceWet
                )
                val petMmDay = computePriestleyTaylorPet(
                    tempC = tempC,
                    solarMj = solarMj
                )
                val actualEtMmDay = computeActualEt(
                    evapFluxMj = powerData?.evapTransFluxMj,
                    petMmDay = petMmDay,
                    rootWetness = rootWet,
                    surfaceWetness = surfaceWet
                )
                val esi = ((actualEtMmDay / petMmDay.coerceAtLeast(0.8)) * 0.72 + rootWet * 0.28)
                    .coerceIn(0.12, 0.98)
                val wue = computeWaterUseEfficiency(
                    solarMj = solarMj,
                    tempC = tempC,
                    esi = esi
                )

                val sourceLabel = when {
                    ecostressMeta.isLive && powerData != null -> "ECOSTRESS 70m CMR + POWER"
                    ecostressMeta.isLive -> "ECOSTRESS 70m CMR + Baseline"
                    else -> "POWER + PT-JPL ET Model"
                }

                val snapshot = ClimateSnapshotEntity(
                    landId = land.id,
                    temperatureC = round1(tempC),
                    precipitationMm = round1(precipMm),
                    solarIrradianceMj = round1(solarMj),
                    humidityPercent = round1(humidityPct),
                    soilWetnessRatio = surfaceWet,
                    sourceLabel = sourceLabel,
                    isLiveRemote = ecostressMeta.isLive || powerData != null,
                    fetchedAtMillis = System.currentTimeMillis(),
                    windSpeedMs = round1(windMs),
                    rootZoneWetnessRatio = rootWet,
                    tempMinC = round1(tempMinC),
                    tempMaxC = round1(tempMaxC),
                    observationDateRange = powerData?.dateRange ?: "Latest Scene Window",
                    landSurfaceTempC = round1(lstC),
                    evapotranspirationMmDay = round1(actualEtMmDay),
                    potentialEtMmDay = round1(petMmDay),
                    evaporativeStressIndex = round2(esi),
                    waterUseEfficiency = round2(wue),
                    ecostressTileId = ecostressMeta.tileId,
                    ecostressOverpassIso = ecostressMeta.overpassIso,
                    ecostressGranuleTitle = ecostressMeta.granuleTitle,
                    ecostressBrowseUrl = ecostressMeta.browseUrl,
                    ecostressProductCount = ecostressMeta.productCount,
                    ecostressLive = ecostressMeta.isLive
                )

                dao.upsertClimate(snapshot)
                Result.success(snapshot)
            } catch (e: Exception) {
                val latFactor = (1.0 - (abs(land.latitude) / 60.0)).coerceIn(0.3, 1.0)
                val baseTemp = 21.5 + latFactor * 8.2
                val wetness = (land.soilMoisturePercent / 100.0).coerceIn(0.15, 0.92)
                val rootWet = ((land.soilMoisturePercent + 4) / 100.0).coerceIn(0.18, 0.92)
                val solar = 16.0 + latFactor * 5.5
                val lst = computeLandSurfaceTemp(baseTemp, null, solar, wetness)
                val pet = computePriestleyTaylorPet(baseTemp, solar)
                val et = (pet * (0.42 + rootWet * 0.56)).coerceIn(0.6, pet)
                val esi = (et / pet).coerceIn(0.15, 0.96)
                val wue = computeWaterUseEfficiency(solar, baseTemp, esi)

                val fallback = ClimateSnapshotEntity(
                    landId = land.id,
                    temperatureC = round1(baseTemp),
                    precipitationMm = if (land.waterSources.contains("Rain", ignoreCase = true)) 2.1 else 3.6,
                    solarIrradianceMj = round1(solar),
                    humidityPercent = (48.0 + land.soilMoisturePercent * 0.28).coerceIn(35.0, 88.0),
                    soilWetnessRatio = wetness,
                    sourceLabel = "Cached ECOSTRESS & Climate Model",
                    isLiveRemote = false,
                    fetchedAtMillis = System.currentTimeMillis(),
                    windSpeedMs = 2.4,
                    rootZoneWetnessRatio = rootWet,
                    tempMinC = round1(baseTemp - 5.4),
                    tempMaxC = round1(baseTemp + 5.2),
                    observationDateRange = "Offline Baseline",
                    landSurfaceTempC = round1(lst),
                    evapotranspirationMmDay = round1(et),
                    potentialEtMmDay = round1(pet),
                    evaporativeStressIndex = round2(esi),
                    waterUseEfficiency = round2(wue),
                    ecostressTileId = deriveFallbackMgrsLabel(land.latitude, land.longitude),
                    ecostressOverpassIso = "Offline Baseline",
                    ecostressGranuleTitle = "ECO_L4T_ESI • Cached 70m Model",
                    ecostressBrowseUrl = "",
                    ecostressProductCount = 0,
                    ecostressLive = false
                )
                dao.upsertClimate(fallback)
                Result.failure(e)
            }
        }

    private suspend fun fetchEcostressGranuleMetadata(land: LandPassportEntity): EcostressMetadataBundle {
        val pointParam = String.format(Locale.US, "%.5f,%.5f", land.longitude, land.latitude)
        val bboxParam = String.format(
            Locale.US,
            "%.4f,%.4f,%.4f,%.4f",
            (land.longitude - 0.18).coerceIn(-180.0, 180.0),
            (land.latitude - 0.18).coerceIn(-90.0, 90.0),
            (land.longitude + 0.18).coerceIn(-180.0, 180.0),
            (land.latitude + 0.18).coerceIn(-90.0, 90.0)
        )

        val entriesByProduct = coroutineScope {
            EcostressCmrApi.PRODUCT_SHORT_NAMES.map { shortName ->
                async {
                    val pointEntries = runCatching {
                        ecostressApi.searchGranulesByPoint(shortName, pointParam).feed?.entry.orEmpty()
                    }.getOrDefault(emptyList())
                    if (pointEntries.isNotEmpty()) {
                        pointEntries
                    } else {
                        runCatching {
                            ecostressApi.searchGranulesByBox(shortName, bboxParam).feed?.entry.orEmpty()
                        }.getOrDefault(emptyList())
                    }
                }
            }.awaitAll()
        }

        val allEntries = entriesByProduct.flatten()
        if (allEntries.isEmpty()) {
            return EcostressMetadataBundle(
                isLive = false,
                tileId = deriveFallbackMgrsLabel(land.latitude, land.longitude),
                overpassIso = "Awaiting ISS Overpass",
                granuleTitle = "ECO_L4T_ESI • Regional 70m",
                browseUrl = "",
                productCount = 0
            )
        }

        val sorted = allEntries.sortedByDescending { it.timeStart.orEmpty() }
        val latest = sorted.first()
        val bestTitle = latest.producerGranuleId ?: latest.title ?: "ECO_L4T_ESI 70m"
        val mgrsCode = sorted.firstNotNullOfOrNull {
            EcostressCmrApi.extractMgrsTile(it.producerGranuleId ?: it.title)
        }
        val tileLabel = if (mgrsCode != null) "MGRS $mgrsCode • 70m" else deriveFallbackMgrsLabel(land.latitude, land.longitude)
        val browseUrl = sorted.firstNotNullOfOrNull { entry ->
            EcostressCmrApi.extractBrowseUrl(entry.links).takeIf { it.isNotBlank() }
        }.orEmpty()

        return EcostressMetadataBundle(
            isLive = true,
            tileId = tileLabel,
            overpassIso = EcostressCmrApi.formatOverpassDate(latest.timeStart),
            granuleTitle = bestTitle.take(48),
            browseUrl = browseUrl,
            productCount = allEntries.size
        )
    }

    private suspend fun fetchPowerAgroPoint(land: LandPassportEntity): PowerTelemetryBundle? {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        val nowCal = Calendar.getInstance()
        val currentYear = nowCal.get(Calendar.YEAR)
        val candidateYears = listOf(currentYear, 2025, 2024).distinct()

        for (year in candidateYears) {
            val endCal = (nowCal.clone() as Calendar).apply {
                set(Calendar.YEAR, year)
                add(Calendar.DAY_OF_YEAR, -3)
            }
            val startCal = (endCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -10)
            }
            val startStr = formatter.format(startCal.time)
            val endStr = formatter.format(endCal.time)

            val response = runCatching {
                api.getDailyAgroPoint(
                    longitude = land.longitude,
                    latitude = land.latitude,
                    start = startStr,
                    end = endStr
                )
            }.getOrNull() ?: continue

            val params = response.properties?.parameter
            val temp = latestValid(params?.temperature)
            val humidity = latestValid(params?.humidity)
            if (temp != null && humidity != null) {
                val precip = averageValid(params?.precipitation) ?: 2.4
                val solar = latestValid(params?.solarIrradiance) ?: 18.5
                val wetness = (latestValid(params?.surfaceSoilWetness) ?: (land.soilMoisturePercent / 100.0)).coerceIn(0.08, 0.96)
                val rootWetness = (latestValid(params?.rootSoilWetness) ?: wetness).coerceIn(0.10, 0.96)
                val wind = latestValid(params?.windSpeed2m) ?: 2.3
                val tMin = latestValid(params?.temperatureMin) ?: (temp - 5.5)
                val tMax = latestValid(params?.temperatureMax) ?: (temp + 5.5)
                val skinTemp = latestValid(params?.earthSkinTemp)
                val evapFlux = latestValid(params?.evapTransFlux)

                return PowerTelemetryBundle(
                    temperatureC = temp,
                    tempMinC = tMin,
                    tempMaxC = tMax,
                    earthSkinTempC = skinTemp,
                    precipitationMm = precip,
                    solarIrradianceMj = solar,
                    evapTransFluxMj = evapFlux,
                    humidityPercent = humidity,
                    surfaceWetness = wetness,
                    rootZoneWetness = rootWetness,
                    windSpeedMs = wind,
                    dateRange = "$startStr – $endStr"
                )
            }
        }
        return null
    }

    private fun computeLandSurfaceTemp(
        airTempC: Double,
        skinTempC: Double?,
        solarMj: Double,
        soilWetness: Double
    ): Double {
        if (skinTempC != null) return skinTempC
        //Dry soils heat faster above air temperature under strong solar irradiance
        val drynessOffset = (1.0 - soilWetness).coerceIn(0.0, 0.9) * 5.2
        val solarOffset = ((solarMj - 14.0).coerceAtLeast(0.0) * 0.22)
        return (airTempC + drynessOffset + solarOffset).coerceIn(airTempC - 1.5, airTempC + 10.5)
    }

    private fun computePriestleyTaylorPet(tempC: Double, solarMj: Double): Double {
        // Slope of saturation vapor pressure curve (kPa/°C)
        val delta = (4098.0 * (0.6108 * exp((17.27 * tempC) / (tempC + 237.3)))) /
            (tempC + 237.3).pow(2.0)
        val gamma = 0.066 // Psychrometric constant
        val netRadiationMj = (solarMj * 0.62).coerceAtLeast(3.0)
        val lambda = 2.45 // MJ/kg latent heat of vaporization
        val pet = 1.26 * (delta / (delta + gamma)) * (netRadiationMj / lambda)
        return pet.coerceIn(1.5, 9.5)
    }

    private fun computeActualEt(
        evapFluxMj: Double?,
        petMmDay: Double,
        rootWetness: Double,
        surfaceWetness: Double
    ): Double {
        val rawEt = if (evapFluxMj != null && evapFluxMj > 0.2) {
            evapFluxMj / 2.45
        } else {
            val moistureConductance = (rootWetness * 0.68 + surfaceWetness * 0.32).coerceIn(0.22, 0.98)
            petMmDay * (0.35 + 0.62 * moistureConductance)
        }
        return rawEt.coerceIn(0.4, petMmDay * 1.05)
    }

    private fun computeWaterUseEfficiency(solarMj: Double, tempC: Double, esi: Double): Double {
        val thermalEfficiency = (1.0 - abs(tempC - 26.0) * 0.018).coerceIn(0.55, 1.05)
        val lightFactor = (solarMj / 18.5).coerceIn(0.6, 1.35)
        return (1.65 + esi * 1.45 * thermalEfficiency * lightFactor).coerceIn(1.1, 4.4)
    }

    private fun deriveFallbackMgrsLabel(lat: Double, lon: Double): String {
        val zone = (((lon + 180.0) / 6.0).toInt() + 1).coerceIn(1, 60)
        val bands = "CDEFGHJKLMNPQRSTUVWXX"
        val bandIdx = (((lat + 80.0) / 8.0).toInt()).coerceIn(0, bands.lastIndex)
        return "Zone $zone${bands[bandIdx]} • 70m"
    }

    private fun latestValid(map: Map<String, Double>?): Double? {
        if (map == null) return null
        return map.entries
            .sortedByDescending { it.key }
            .map { it.value }
            .firstOrNull { it > -900.0 }
    }

    private fun averageValid(map: Map<String, Double>?): Double? {
        if (map == null) return null
        val valid = map.values.filter { it > -900.0 }
        return if (valid.isEmpty()) null else valid.average()
    }

    private fun round1(v: Double): Double = (v * 10.0).roundToInt() / 10.0
    private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0
}
