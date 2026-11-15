package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SatelliteAlt
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.GrievousUiState
import com.example.ui.components.DashboardTelemetryLogo
import com.example.ui.theme.AlertTerracotta
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.WaterTeal
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    state: GrievousUiState,
    onSelectLand: (Long) -> Unit,
    onRefreshTelemetry: () -> Unit
) {
    val insight = state.insight
    val climate = state.selectedClimate
    val land = state.selectedLand

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardTelemetryLogo(size = 28.dp, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = state.strings.navDashboard,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    FilledTonalButton(
                        onClick = onRefreshTelemetry,
                        enabled = !state.isSyncingTelemetry,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sync_telemetry_button")
                    ) {
                        if (state.isSyncingTelemetry) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(state.strings.refreshSatellite)
                    }
                }
            }

            if (state.lands.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.lands.forEach { parcel ->
                            FilterChip(
                                selected = parcel.id == land?.id,
                                onClick = { onSelectLand(parcel.id) },
                                label = { Text(parcel.landName) }
                            )
                        }
                    }
                }
            }

            // Live ECOSTRESS + POWER Agro-Climatology Telemetry Hero Card (zero NASA/JPL brand logo)
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(ForestGreen, EarthBrown)))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = land?.landName ?: state.strings.activeLandLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = OffWhite
                                )
                                Text(
                                    text = "Source: ${climate?.sourceLabel ?: "ECOSTRESS + POWER"} • ${climate?.observationDateRange ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OffWhite.copy(alpha = 0.84f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                color = OffWhite.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (climate?.isLiveRemote == true) "LIVE TELEMETRY" else "CACHED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OffWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryMetric("Surface LST", "${String.format("%.1f", insight.landSurfaceTempC)}°C")
                            TelemetryMetric("Actual ET", "${String.format("%.1f", insight.evapotranspirationMmDay)} mm/d")
                            TelemetryMetric("Stress ESI", String.format("%.2f", insight.evaporativeStressIndex))
                            TelemetryMetric("WUE", "${String.format("%.1f", insight.waterUseEfficiency)} g/kg")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryMetric("Air T2M", "${(climate?.temperatureC ?: 27.0).roundToInt()}°C")
                            TelemetryMetric("Rain", "${String.format("%.1f", climate?.precipitationMm ?: 3.2)} mm")
                            TelemetryMetric("Root Zone", "${((climate?.rootZoneWetnessRatio ?: 0.64) * 100).roundToInt()}%")
                            TelemetryMetric("Humidity", "${(climate?.humidityPercent ?: 60.0).roundToInt()}%")
                        }
                    }
                }
            }

            // ECOSTRESS 70m Thermal, Evapotranspiration & Plant Stress Card (no brand logo)
            item {
                val esi = insight.evaporativeStressIndex.toFloat().coerceIn(0f, 1f)
                val esiColor = when {
                    esi < 0.45f -> AlertTerracotta
                    esi < 0.70f -> AmberHarvest
                    else -> MeadowGreen
                }

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ecostress_telemetry_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Rounded.Thermostat,
                                    contentDescription = null,
                                    tint = esiColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "70m Thermal & ET (ECOSTRESS)",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${climate?.ecostressTileId ?: "MGRS 43QCB • 70m"} • ${climate?.ecostressOverpassIso ?: "Baseline"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                color = esiColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (climate?.ecostressLive == true) {
                                        "${climate.ecostressProductCount} Scenes"
                                    } else {
                                        "PT-JPL 70m"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = esiColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!climate?.ecostressBrowseUrl.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = climate?.ecostressBrowseUrl,
                                    contentDescription = "ECOSTRESS 70m satellite thermal and stress tile preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.SatelliteAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Latest LP DAAC Acquisition",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = climate?.ecostressGranuleTitle ?: "ECO_L4T_ESI 70m",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evaporative Stress Index (L4T ESI)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = insight.ecostressStressBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = esiColor
                                )
                            }
                            LinearProgressIndicator(
                                progress = { esi },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = esiColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EcostressMiniPill(
                                label = "L2T Land Temp",
                                value = "${String.format("%.1f", insight.landSurfaceTempC)}°C",
                                detail = "+${String.format("%.1f", insight.thermalCanopyDeltaC)}° vs air",
                                modifier = Modifier.weight(1f)
                            )
                            EcostressMiniPill(
                                label = "L3T ET / PET",
                                value = "${String.format("%.1f", insight.evapotranspirationMmDay)}/${String.format("%.1f", insight.potentialEtMmDay)}",
                                detail = "mm/day flux",
                                modifier = Modifier.weight(1f)
                            )
                            EcostressMiniPill(
                                label = "L4T Water Eff.",
                                value = String.format("%.2f", insight.waterUseEfficiency),
                                detail = "g C / kg H2O",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 1. Crop Prices & Field Value Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.strings.cropPricesTitle, style = MaterialTheme.typography.titleMedium)
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                val sign = if (insight.sevenDayTrendPct >= 0) "+" else ""
                                Text(
                                    text = "$sign${insight.sevenDayTrendPct}% 7d",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${insight.cropName}: $${insight.pricePerTonUsd}/ton",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Est. plot revenue: $${insight.estimatedRevenueUsd}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            PriceSparkline(
                                points = insight.sparklinePoints,
                                modifier = Modifier
                                    .width(108.dp)
                                    .height(44.dp)
                            )
                        }
                    }
                }
            }

            // 2. Best Harvesting Times & Field Shipping
            item {
                DashboardInfoCard(
                    icon = Icons.Rounded.Agriculture,
                    iconTint = AmberHarvest,
                    title = state.strings.harvestTimingTitle,
                    badge = "Ship in ${insight.shippingReadinessDays}d",
                    body = insight.bestHarvestWindow,
                    footerIcon = Icons.Rounded.LocalShipping,
                    footerText = "Field shipping pre-book window ready • Humidity-checked dispatch"
                )
            }

            // 3. Storage Recommendations
            item {
                DashboardInfoCard(
                    icon = Icons.Rounded.Inventory2,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = state.strings.storageAdviceTitle,
                    badge = insight.storageRiskLevel,
                    body = insight.storageRecommendation
                )
            }

            // 4. Water Availability in Area
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = WaterTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.strings.waterAvailabilityTitle, style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = "${insight.waterAvailabilityPct}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = WaterTeal
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (insight.waterAvailabilityPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = WaterTeal,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(insight.waterStatusLabel, style = MaterialTheme.typography.labelLarge, color = WaterTeal)
                        Text(insight.irrigationAdvice, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 5. Best Nearby Markets
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.strings.nearbyMarketsTitle, style = MaterialTheme.typography.titleMedium)
                        }
                        insight.nearbyMarkets.forEachIndexed { idx, market ->
                            if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(market.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${market.distanceKm} km away • ${market.demandBadge}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        market.shippingNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$${market.offeredPricePerTonUsd}/t",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TelemetryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OffWhite.copy(alpha = 0.78f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OffWhite)
    }
}

@Composable
private fun PriceSparkline(points: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val path = Path()
        val stepX = size.width / (points.size - 1)
        points.forEachIndexed { idx, v ->
            val x = idx * stepX
            val y = size.height - (v.coerceIn(0f, 1f) * size.height)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = MeadowGreen,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
        val lastX = (points.size - 1) * stepX
        val lastY = size.height - (points.last().coerceIn(0f, 1f) * size.height)
        drawCircle(color = AmberHarvest, radius = 8f, center = Offset(lastX, lastY))
    }
}

@Composable
private fun DashboardInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    badge: String,
    body: String,
    footerIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    footerText: String? = null
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(body, style = MaterialTheme.typography.bodyMedium)
            if (footerIcon != null && footerText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(footerIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(footerText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun EcostressMiniPill(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
