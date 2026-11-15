package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.GrievousPrizeCatalog
import com.example.ui.GrievousUiState
import com.example.ui.RewardPrize
import com.example.ui.components.LeavesLogo
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SproutGreen
import kotlin.math.roundToInt

@Composable
fun RewardsScreen(
    state: GrievousUiState,
    onUpdateVault: (Int) -> Unit,
    onRecordProfitMilestone: () -> Unit,
    onRedeemPrize: (RewardPrize) -> Unit
) {
    val spendable = (state.totalLeavesBalance - state.vaultSavedLeaves).coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LeavesLogo(size = 30.dp, primaryColor = ForestGreen, secondaryColor = SproutGreen)
                    Text(state.strings.leavesTitle, style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Leaves Hero Summary Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(ForestGreen, EarthBrown)))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Total Collected Leaves",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = OffWhite.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${state.totalLeavesBalance} Leaves",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = OffWhite.copy(alpha = 0.16f),
                                shape = CircleShape,
                                modifier = Modifier.size(58.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    LeavesLogo(size = 34.dp, primaryColor = OffWhite, secondaryColor = AmberHarvest)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Saved for Future", style = MaterialTheme.typography.labelSmall, color = OffWhite.copy(alpha = 0.78f))
                                Text("${state.vaultSavedLeaves} Leaves", style = MaterialTheme.typography.titleMedium, color = OffWhite, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available for Prizes", style = MaterialTheme.typography.labelSmall, color = OffWhite.copy(alpha = 0.78f))
                                Text("$spendable Leaves", style = MaterialTheme.typography.titleMedium, color = AmberHarvest, fontWeight = FontWeight.Bold)
                            }
                        }

                        FilledTonalButton(
                            onClick = onRecordProfitMilestone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("profit_milestone_button")
                        ) {
                            Icon(Icons.Rounded.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Claim Field-Shipping Profit Bonus (+50 Leaves)")
                        }
                    }
                }
            }

            // Future Savings Vault Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = EarthBrown)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.strings.futureVaultTitle, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            "Protect Leaves for next-season seed & fertilizer investments: ${state.vaultSavedLeaves} / ${state.totalLeavesBalance} Leaves",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = state.vaultSavedLeaves.toFloat(),
                            onValueChange = { onUpdateVault(it.roundToInt()) },
                            valueRange = 0f..state.totalLeavesBalance.coerceAtLeast(1).toFloat(),
                            enabled = state.totalLeavesBalance > 0
                        )
                    }
                }
            }

            // Milestone Prizes Catalog
            item {
                Text(state.strings.prizesTitle, style = MaterialTheme.typography.titleLarge)
            }

            items(GrievousPrizeCatalog, key = { it.code }) { prize ->
                val progress = (state.totalLeavesBalance.toFloat() / prize.costLeaves.toFloat()).coerceIn(0f, 1f)
                val canRedeem = spendable >= prize.costLeaves
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = AmberHarvest)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(prize.title, style = MaterialTheme.typography.titleMedium)
                                    Text(prize.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${prize.costLeaves} Leaves",
                                style = MaterialTheme.typography.labelLarge,
                                color = ForestGreen
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ForestGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onRedeemPrize(prize) },
                                enabled = canRedeem,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("redeem_prize_${prize.code}")
                            ) {
                                Text(if (canRedeem) state.strings.redeemPrize else "Need ${prize.costLeaves - spendable} spendable")
                            }
                        }
                    }
                }
            }

            if (state.redeemedPrizes.isNotEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Claimed Prizes", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                            state.redeemedPrizes.forEach { claimed ->
                                Text("• ${claimed.prizeTitle} (${claimed.leavesCost} Leaves)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Recent Leaf Ledger
            item {
                Text("Leaves Activity Ledger", style = MaterialTheme.typography.titleLarge)
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.leafTransactions.take(10).forEachIndexed { idx, tx ->
                            if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(tx.sourceType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (tx.leavesDelta >= 0) "+${tx.leavesDelta}" else "${tx.leavesDelta}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.leavesDelta >= 0) ForestGreen else MaterialTheme.colorScheme.error
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
