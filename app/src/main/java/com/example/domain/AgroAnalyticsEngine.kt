package com.example.domain

import com.example.data.local.ClimateSnapshotEntity
import com.example.data.local.FarmerPassportEntity
import com.example.data.local.LandPassportEntity
import com.example.data.local.LandSimulationEntity
import kotlin.math.abs
import kotlin.math.roundToInt

data class CropMarketInsight(
    val cropName: String,
    val pricePerTonUsd: Int,
    val sevenDayTrendPct: Double,
    val sparklinePoints: List<Float>,
    val estimatedRevenueUsd: Int,
    val bestHarvestWindow: String,
    val shippingReadinessDays: Int,
    val storageRecommendation: String,
    val storageRiskLevel: String,
    val nearbyMarkets: List<NearbyMarket>,
    val waterAvailabilityPct: Int,
    val waterStatusLabel: String,
    val irrigationAdvice: String,
    val landSurfaceTempC: Double = 30.2,
    val thermalCanopyDeltaC: Double = 3.2,
    val evapotranspirationMmDay: Double = 3.6,
    val potentialEtMmDay: Double = 4.8,
    val evaporativeStressIndex: Double = 0.75,
    val waterUseEfficiency: Double = 2.35,
    val ecostressStressBadge: String = "Healthy Transpiration"
)

data class NearbyMarket(
    val name: String,
    val distanceKm: Int,
    val offeredPricePerTonUsd: Int,
    val demandBadge: String,
    val shippingNote: String
)

data class RotationRecommendation(
    val id: String,
    val sequenceTitle: String,
    val wheelSegments: List<String>,
    val matchScorePct: Int,
    val nitrogenRecoveryPct: Int,
    val waterEfficiencyPct: Int,
    val profitLiftPct: Int,
    val rationale: String,
    val bestSeason: String
)

object AgroAnalyticsEngine {

    private val baseCropPrices = mapOf(
        "Paddy Rice" to 420,
        "Wheat" to 360,
        "Maize" to 310,
        "Soybean" to 540,
        "Chickpea" to 690,
        "Pearl Millet" to 345,
        "Cotton" to 780,
        "Mustard" to 615,
        "Pigeon Pea" to 720,
        "Groundnut" to 670
    )

    fun buildDashboardInsight(
        farmer: FarmerPassportEntity?,
        land: LandPassportEntity?,
        climate: ClimateSnapshotEntity?
    ): CropMarketInsight {
        val crop = farmer?.mainCrop?.ifBlank { "Paddy Rice" } ?: "Paddy Rice"
        val acres = land?.areaAcres ?: farmer?.cultivatedAcres ?: 4.0
        val temp = climate?.temperatureC ?: 27.0
        val precip = climate?.precipitationMm ?: 3.0
        val humidity = climate?.humidityPercent ?: 60.0
        val wetness = climate?.soilWetnessRatio ?: ((land?.soilMoisturePercent ?: 60) / 100.0)
        val lstC = climate?.landSurfaceTempC ?: (temp + 3.1)
        val thermalDelta = ((lstC - temp) * 10.0).roundToInt() / 10.0
        val actualEt = climate?.evapotranspirationMmDay ?: 3.6
        val pet = climate?.potentialEtMmDay ?: 4.8
        val esi = (climate?.evaporativeStressIndex ?: ((wetness * 0.85).coerceIn(0.20, 0.96))).coerceIn(0.10, 0.98)
        val wue = climate?.waterUseEfficiency ?: 2.35
        val district = farmer?.district?.substringBefore(",")?.trim()?.ifBlank { "District" } ?: "District"

        val basePrice = baseCropPrices.entries
            .firstOrNull { crop.contains(it.key, ignoreCase = true) }?.value ?: 450

        val soilBonus = when {
            land?.soilType?.contains("Alluvial", true) == true -> 1.12
            land?.soilType?.contains("Black", true) == true -> 1.10
            land?.soilType?.contains("Loam", true) == true -> 1.06
            else -> 0.96
        }
        val esiYieldModifier = (0.82 + esi * 0.24).coerceIn(0.75, 1.08)
        val yieldPerAcreTons = (1.85 * soilBonus * (0.72 + wetness * 0.36) * esiYieldModifier).coerceIn(1.0, 3.3)
        val totalRevenue = (acres * yieldPerAcreTons * basePrice).roundToInt()

        val trendPct = (((temp - 24.0) * 0.5) + (esi * 3.8) - 1.3)
            .coerceIn(-4.5, 8.4)
        val spark = listOf(0.55f, 0.62f, 0.58f, 0.70f, 0.76f, 0.72f, (0.76f + (trendPct / 30.0).toFloat()).coerceIn(0.35f, 0.98f))

        val shippingDays = when {
            precip > 6.0 -> 5
            wetness > 0.74 -> 4
            lstC > 35.0 || esi < 0.45 -> 2
            temp in 22.0..31.0 -> 2
            else -> 3
        }
        val harvestWindow = when {
            precip > 6.0 -> "Wait 4–5 dry solar days before mechanized harvesting & field dispatch"
            lstC >= 35.0 || thermalDelta >= 4.5 ->
                "Dawn harvest (05:30–09:00) advised • High 70m canopy thermal stress (${lstC.roundToInt()}°C LST) before crate dispatch"
            temp > 33.0 -> "Early-morning harvest (05:30–09:30) to avoid grain shattering before shipping"
            else -> "Optimal 7-day harvest window • Dispatch crates within $shippingDays days"
        }

        val storageRisk = when {
            humidity >= 75.0 -> "High Moisture Risk"
            humidity >= 60.0 -> "Moderate Humidity Watch"
            else -> "Low Spoilage Risk"
        }
        val storageAdvice = when {
            humidity >= 75.0 -> "Target grain moisture <=13%. Use hermetic PICS bags + elevated wooden pallets with cross-ventilation."
            humidity >= 60.0 -> "Sun-dry for 6 hours before bagging. Maintain 14% moisture and weekly aeration checks."
            else -> "Dry ambient conditions (${humidity.roundToInt()}% RH) ideal for ventilated jute/hermetic silos."
        }

        val waterPct = ((wetness * 42.0) + (esi * 40.0) + (precip.coerceAtMost(10.0) * 1.8) +
            if (land?.waterSources?.contains("Drip", true) == true || land?.waterSources?.contains("Canal", true) == true) 12.0 else 4.0)
            .roundToInt().coerceIn(18, 98)

        val ecostressStressBadge = when {
            esi < 0.45 -> "High Water Stress (ESI ${String.format("%.2f", esi)})"
            esi < 0.70 -> "Moderate Stress Watch (ESI ${String.format("%.2f", esi)})"
            else -> "Healthy Transpiration (ESI ${String.format("%.2f", esi)})"
        }

        val etDeficitMm = (pet - actualEt).coerceAtLeast(0.0)
        val waterStatus = when {
            esi < 0.45 -> "Flash-Drought Stress Alert (ESI ${String.format("%.2f", esi)})"
            waterPct >= 75 && esi >= 0.70 -> "Surplus Moisture & Healthy Canopy Cooling"
            waterPct >= 50 -> "Balanced Soil Moisture & ET Reserve"
            else -> "Deficit Alert • Drip & Mulch Critical"
        }
        val irrigationAdvice = when {
            esi < 0.45 -> "ECOSTRESS ESI (${String.format("%.2f", esi)}) shows canopy stomatal closure (ET ${actualEt} vs ${pet} mm/d). Irrigate ~${String.format("%.1f", (etDeficitMm * 1.2).coerceAtLeast(2.0))} mm/d via evening drip + straw mulch."
            waterPct >= 75 && esi >= 0.75 -> "Pause flood irrigation for 5 days; open surface drainage furrows. Evapotranspiration is balanced at ${actualEt} mm/day."
            waterPct >= 50 -> "Schedule light evening drip cycles to replenish ${actualEt} mm/day crop evapotranspiration (${String.format("%.1f", wue)} WUE)."
            else -> "Switch immediately to deficit drip irrigation + organic straw mulch to cut ${pet} mm/day evaporative loss by 32%."
        }

        val markets = listOf(
            NearbyMarket(
                name = "$district Agro-Export Hub",
                distanceKm = 14,
                offeredPricePerTonUsd = (basePrice * 1.09).roundToInt(),
                demandBadge = "High Export Demand",
                shippingNote = "Direct cold-crate dock • Instant weighbridge settlement"
            ),
            NearbyMarket(
                name = "Regional Cooperative Mandi",
                distanceKm = 8,
                offeredPricePerTonUsd = (basePrice * 1.03).roundToInt(),
                demandBadge = "Stable Daily Volume",
                shippingNote = "Low transport cost • Zero middleman fee with Passport ID"
            ),
            NearbyMarket(
                name = "Valley Cold-Chain Processing Yard",
                distanceKm = 27,
                offeredPricePerTonUsd = (basePrice * 1.14).roundToInt(),
                demandBadge = "Premium Grade-A Bonus",
                shippingNote = "Requires <=13.5% moisture certificate for premium field shipping"
            )
        )

        return CropMarketInsight(
            cropName = crop,
            pricePerTonUsd = basePrice,
            sevenDayTrendPct = (trendPct * 10.0).roundToInt() / 10.0,
            sparklinePoints = spark,
            estimatedRevenueUsd = totalRevenue,
            bestHarvestWindow = harvestWindow,
            shippingReadinessDays = shippingDays,
            storageRecommendation = storageAdvice,
            storageRiskLevel = storageRisk,
            nearbyMarkets = markets,
            waterAvailabilityPct = waterPct,
            waterStatusLabel = waterStatus,
            irrigationAdvice = irrigationAdvice,
            landSurfaceTempC = lstC,
            thermalCanopyDeltaC = thermalDelta,
            evapotranspirationMmDay = actualEt,
            potentialEtMmDay = pet,
            evaporativeStressIndex = esi,
            waterUseEfficiency = wue,
            ecostressStressBadge = ecostressStressBadge
        )
    }

    fun simulateVirtualLand(
        landId: Long,
        plotLabel: String,
        cropName: String,
        areaAcres: Double,
        slopeDegrees: Double,
        soilType: String,
        waterScore: Int,
        fertilizerIntensity: Int,
        shippingLeadDays: Int,
        climate: ClimateSnapshotEntity?
    ): LandSimulationEntity {
        val soilFit = when {
            soilType.contains("Alluvial", true) -> 92
            soilType.contains("Black", true) && (cropName.contains("Cotton", true) || cropName.contains("Chickpea", true) || cropName.contains("Soybean", true)) -> 94
            soilType.contains("Red", true) && (cropName.contains("Millet", true) || cropName.contains("Groundnut", true) || cropName.contains("Pigeon", true)) -> 90
            soilType.contains("Loam", true) -> 87
            else -> 76
        }

        val slopePenalty = (abs(slopeDegrees - 2.5) * 1.9).coerceAtMost(22.0)
        val fertBalancePenalty = abs(fertilizerIntensity - 68) * 0.25
        val shippingPenalty = ((shippingLeadDays - 2).coerceAtLeast(0)) * 2.8
        val climateTemp = climate?.landSurfaceTempC ?: climate?.temperatureC ?: 29.0
        val thermalFit = (100.0 - abs(climateTemp - 28.5) * 2.0).coerceIn(52.0, 98.0)
        val esi = (climate?.evaporativeStressIndex ?: 0.72).coerceIn(0.15, 0.98)
        val esiBonus = ((esi - 0.55) * 18.0).coerceIn(-8.0, 8.0)

        val successRate = (
            (soilFit * 0.34) +
                (waterScore * 0.30) +
                (thermalFit * 0.24) +
                ((100.0 - fertBalancePenalty) * 0.12) +
                esiBonus -
                slopePenalty -
                shippingPenalty
            ).roundToInt().coerceIn(30, 97)

        val price = baseCropPrices.entries.firstOrNull { cropName.contains(it.key, true) }?.value ?: 460
        val yieldPerAcre = (1.35 + (successRate / 100.0) * 1.65)
        val totalYield = ((areaAcres * yieldPerAcre) * 10.0).roundToInt() / 10.0
        val gross = totalYield * price
        val inputCost = (areaAcres * (145.0 + fertilizerIntensity * 1.4)).roundToInt()
        val netProfit = (gross - inputCost).roundToInt().coerceAtLeast(180)
        val unadaptedBaselineLoss = (netProfit * (1.0 - successRate / 120.0) * 0.72 + areaAcres * 55).roundToInt()

        val riskSummary = buildString {
            append(
                if (waterScore < 45 || esi < 0.45) "Drought / ESI stress risk (${String.format("%.2f", esi)})"
                else if (waterScore > 88 && slopeDegrees < 1.5) "Waterlogging risk"
                else "Balanced hydrology (LST ${climateTemp.roundToInt()}°C)"
            )
            append(" • ")
            append(if (slopeDegrees > 8.0) "Terrace bunding advised for ${slopeDegrees.roundToInt()}° slope" else "Low topsoil erosion")
            append(" • ")
            append(if (shippingLeadDays <= 2) "Optimal ${shippingLeadDays}d field-shipping freshness" else "Pre-cool crates for ${shippingLeadDays}d transit")
        }

        return LandSimulationEntity(
            landId = landId,
            plotLabel = plotLabel,
            cropName = cropName,
            areaAcres = areaAcres,
            slopeDegrees = slopeDegrees,
            soilType = soilType,
            waterScore = waterScore,
            fertilizerIntensity = fertilizerIntensity,
            shippingLeadDays = shippingLeadDays,
            successRatePercent = successRate,
            projectedYieldTons = totalYield,
            estimatedProfitUsd = netProfit,
            lossAvoidedUsd = unadaptedBaselineLoss,
            riskSummary = riskSummary
        )
    }

    fun recommendRotations(
        season: String,
        monthName: String,
        farmer: FarmerPassportEntity?,
        land: LandPassportEntity?,
        climate: ClimateSnapshotEntity?
    ): List<RotationRecommendation> {
        val soil = land?.soilType ?: "Alluvial Loam"
        val wetness = climate?.soilWetnessRatio ?: ((land?.soilMoisturePercent ?: 60) / 100.0)
        val temp = climate?.temperatureC ?: 27.0
        val lstC = climate?.landSurfaceTempC ?: (temp + 3.0)
        val esi = climate?.evaporativeStressIndex ?: 0.72
        val et = climate?.evapotranspirationMmDay ?: 3.6
        val waterSource = land?.waterSources ?: "Drip Canal"
        val currentCrop = farmer?.mainCrop ?: "Paddy Rice"

        val monsoonBonus = if (season.contains("Monsoon", true) || season.contains("Kharif", true) || wetness > 0.58) 6 else 0
        val winterBonus = if (season.contains("Winter", true) || season.contains("Rabi", true) || temp < 24.5) 7 else 0
        val summerBonus = if (season.contains("Summer", true) || season.contains("Zaid", true) || lstC > 33.0) 8 else 0

        val strategies = listOf(
            RotationRecommendation(
                id = "cereal_legume_cycle",
                sequenceTitle = "$currentCrop -> Chickpea -> Sesbania Cover -> Pearl Millet",
                wheelSegments = listOf(currentCrop, "Chickpea", "Sesbania Cover", "Pearl Millet"),
                matchScorePct = (86 + monsoonBonus + if (soil.contains("Loam", true)) 5 else 2).coerceIn(72, 98),
                nitrogenRecoveryPct = 42,
                waterEfficiencyPct = 88,
                profitLiftPct = 29,
                rationale = "Tailored for $monthName ($season) on $soil (ET $et mm/d). Chickpea fixes nitrogen after $currentCrop while Sesbania shields canopy moisture.",
                bestSeason = season
            ),
            RotationRecommendation(
                id = "oilseed_pulse_cycle",
                sequenceTitle = "Soybean -> Mustard -> Green Gram -> Maize",
                wheelSegments = listOf("Soybean", "Mustard", "Green Gram", "Maize"),
                matchScorePct = (82 + winterBonus + if (waterSource.contains("Drip", true)) 6 else 2).coerceIn(70, 96),
                nitrogenRecoveryPct = 36,
                waterEfficiencyPct = 84,
                profitLiftPct = 34,
                rationale = "High-margin oilseed & pulse rotation for ${lstC.roundToInt()}°C surface temp. Breaks stem-borer cycles and lifts WUE.",
                bestSeason = season
            ),
            RotationRecommendation(
                id = "drought_resilient_cycle",
                sequenceTitle = "Pearl Millet -> Pigeon Pea -> Groundnut -> Sorghum Cover",
                wheelSegments = listOf("Pearl Millet", "Pigeon Pea", "Groundnut", "Sorghum Cover"),
                matchScorePct = (79 + summerBonus + if (wetness < 0.55 || esi < 0.62) 12 else 3).coerceIn(68, 97),
                nitrogenRecoveryPct = 38,
                waterEfficiencyPct = 94,
                profitLiftPct = 25,
                rationale = "Low-water 70m ESI resilience plan (ESI ${String.format("%.2f", esi)}): deep-rooted Pigeon Pea & Groundnut survive heat spikes at ${(wetness * 100).roundToInt()}% moisture.",
                bestSeason = season
            )
        )
        return strategies.sortedByDescending { it.matchScorePct }
    }

    fun buildOfflineDispatchMessage(
        farmer: FarmerPassportEntity,
        land: LandPassportEntity?,
        insight: CropMarketInsight
    ): String {
        val landName = land?.landName ?: farmer.district
        val crop = farmer.mainCrop
        val cadenceTag = farmer.dispatchCadence.lowercase().replaceFirstChar { it.uppercase() }
        return "[Georgos $cadenceTag Field Update - ${farmer.preferredLanguage.uppercase()}] " +
            "Farmer ${farmer.name} (${farmer.farmerIdCode}) | Plot: $landName | " +
            "Crop: $crop | Best Price: $${insight.nearbyMarkets.firstOrNull()?.offeredPricePerTonUsd ?: insight.pricePerTonUsd}/t | " +
            "Water: ${insight.waterAvailabilityPct}% | LST ${insight.landSurfaceTempC}°C, ET ${insight.evapotranspirationMmDay}mm/d, ESI ${String.format("%.2f", insight.evaporativeStressIndex)} | " +
            "Action: ${insight.bestHarvestWindow}. Storage: ${insight.storageRiskLevel}."
    }
}
