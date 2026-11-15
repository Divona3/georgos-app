package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.FarmerPassportEntity
import com.example.data.local.LandPassportEntity
import com.example.domain.AgroAnalyticsEngine
import com.example.ui.GrievousUiState
import com.example.ui.components.PassportBadgeLogo
import com.example.ui.components.VoiceInputTextField
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.WaterTeal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PassportsScreen(
    state: GrievousUiState,
    onSaveFarmer: (FarmerPassportEntity) -> Unit,
    onRenewPassport: (String, Double, String, String) -> Unit,
    onSelectLand: (Long) -> Unit,
    onSaveLand: (LandPassportEntity) -> Unit,
    onLogDispatch: (String, String) -> Unit,
    onSpeakText: (String) -> Unit
) {
    val context = LocalContext.current
    val farmer = state.farmer
    var showEditFarmerDialog by remember { mutableStateOf(false) }
    var showRenewDialog by remember { mutableStateOf(false) }
    var editingLand by remember { mutableStateOf<LandPassportEntity?>(null) }
    var showNewLandDialog by remember { mutableStateOf(false) }

    val farmerPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && farmer != null) {
            onSaveFarmer(farmer.copy(photoUri = uri.toString()))
        }
    }

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
                    PassportBadgeLogo(size = 28.dp, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = state.strings.farmerPassportTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (farmer != null) {
                item {
                    FarmerPassportCard(
                        farmer = farmer,
                        state = state,
                        onPickPhoto = {
                            farmerPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onEdit = { showEditFarmerDialog = true },
                        onRenew = { showRenewDialog = true }
                    )
                }

                item {
                    OfflineDispatchBridgeCard(
                        farmer = farmer,
                        state = state,
                        onSendSms = { message ->
                            onLogDispatch("SMS", message)
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${farmer.phoneNumber}")
                                putExtra("sms_body", message)
                            }
                            runCatching { context.startActivity(smsIntent) }
                        },
                        onVoiceCall = { message ->
                            onLogDispatch("VOICE_CALL", message)
                            onSpeakText(message)
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${farmer.phoneNumber}")
                            }
                            runCatching { context.startActivity(dialIntent) }
                        },
                        onReadScript = { message -> onSpeakText(message) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.strings.landPassportTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    FilledTonalButton(
                        onClick = { showNewLandDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_land_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(state.strings.addLand)
                    }
                }
            }

            items(state.lands, key = { it.id }) { land ->
                LandPassportCard(
                    land = land,
                    onSelect = { onSelectLand(land.id) },
                    onEdit = { editingLand = land }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showEditFarmerDialog && farmer != null) {
        EditFarmerPassportDialog(
            farmer = farmer,
            languageCode = state.languageCode,
            onDismiss = { showEditFarmerDialog = false },
            onSave = {
                onSaveFarmer(it)
                showEditFarmerDialog = false
            }
        )
    }

    if (showRenewDialog && farmer != null) {
        RenewPassportDialog(
            farmer = farmer,
            languageCode = state.languageCode,
            onDismiss = { showRenewDialog = false },
            onRenew = { crop, acres, rotation, note ->
                onRenewPassport(crop, acres, rotation, note)
                showRenewDialog = false
            }
        )
    }

    if (editingLand != null || showNewLandDialog) {
        EditLandPassportDialog(
            initialLand = editingLand,
            languageCode = state.languageCode,
            onDismiss = {
                editingLand = null
                showNewLandDialog = false
            },
            onSave = {
                onSaveLand(it)
                editingLand = null
                showNewLandDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FarmerPassportCard(
    farmer: FarmerPassportEntity,
    state: GrievousUiState,
    onPickPhoto: () -> Unit,
    onEdit: () -> Unit,
    onRenew: () -> Unit
) {
    val isOffline = farmer.connectionType == "OFFLINE" || !farmer.hasSmartphone
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ForestGreen, EarthBrown)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(modifier = Modifier.clickable { onPickPhoto() }) {
                        if (farmer.photoUri.isNotBlank()) {
                            AsyncImage(
                                model = farmer.photoUri,
                                contentDescription = "Farmer photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_farmer_avatar),
                                contentDescription = "Farmer photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = OffWhite,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Change farmer photo",
                                tint = ForestGreen,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = farmer.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = OffWhite
                        )
                        Text(
                            text = "${farmer.farmerIdCode} • ${farmer.district}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OffWhite.copy(alpha = 0.88f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PassportPill(
                                text = if (isOffline) state.strings.offlineFeaturePhone else state.strings.onlineFarmer,
                                containerColor = if (isOffline) AmberHarvest else OffWhite.copy(alpha = 0.22f)
                            )
                            PassportPill(
                                text = if (farmer.hasSmartphone) state.strings.smartphoneYes else state.strings.smartphoneNo,
                                containerColor = OffWhite.copy(alpha = 0.2f)
                            )
                            PassportPill(
                                text = "Cycle #${farmer.cycleNumber}",
                                containerColor = OffWhite.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PassportStat("Experience", "${farmer.experienceYears} yrs")
                    PassportStat("Cultivated", "${farmer.cultivatedAcres} ac")
                    PassportStat("Main Crop", farmer.mainCrop)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Rotation Strategy",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = farmer.rotationStrategy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Government Schemes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    farmer.governmentSchemes.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        .forEach { scheme ->
                            AssistChip(
                                onClick = {},
                                label = { Text(scheme, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("edit_farmer_passport_button")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(state.strings.editPassport)
                    }
                    Button(
                        onClick = onRenew,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("renew_passport_button")
                    ) {
                        Icon(Icons.Rounded.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(state.strings.renewPassport)
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineDispatchBridgeCard(
    farmer: FarmerPassportEntity,
    state: GrievousUiState,
    onSendSms: (String) -> Unit,
    onVoiceCall: (String) -> Unit,
    onReadScript: (String) -> Unit
) {
    val dispatchMessage = remember(farmer, state.selectedLand, state.insight) {
        AgroAnalyticsEngine.buildOfflineDispatchMessage(farmer, state.selectedLand, state.insight)
    }
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = state.strings.offlineDispatchTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "Schedule: ${farmer.dispatchCadence} • Phone: ${farmer.phoneNumber} • Lang: ${farmer.preferredLanguage.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = dispatchMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSendSms(dispatchMessage) },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("send_offline_sms_button")
                ) {
                    Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(state.strings.sendSmsUpdate)
                }
                OutlinedButton(
                    onClick = { onVoiceCall(dispatchMessage) },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("voice_call_dispatch_button")
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(state.strings.startVoiceCall)
                }
            }
            if (state.dispatchLogs.isNotEmpty()) {
                val latest = state.dispatchLogs.first()
                Text(
                    text = "Last bridge dispatch: ${latest.channel} (${latest.cadence}) -> ${latest.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                TextButton(onClick = { onReadScript(dispatchMessage) }) {
                    Text("Preview Voice Call Script Aloud")
                }
            }
        }
    }
}

@Composable
private fun LandPassportCard(
    land: LandPassportEntity,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    val selectedBorder = if (land.isSelected) BorderStroke(2.dp, ForestGreen)
    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (land.isSelected) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = selectedBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("land_card_${land.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            ) {
                if (land.photoUri.isNotBlank()) {
                    AsyncImage(
                        model = land.photoUri,
                        contentDescription = land.landName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_land_parcel),
                        contentDescription = land.landName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (land.isSelected) {
                    Surface(
                        color = ForestGreen,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = OffWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selected Active Plot", style = MaterialTheme.typography.labelSmall, color = OffWhite)
                        }
                    }
                }
                Surface(
                    color = OffWhite.copy(alpha = 0.92f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit ${land.landName}",
                        tint = EarthBrown,
                        modifier = Modifier
                            .clickable { onEdit() }
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(land.landName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${land.areaAcres} acres", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                }
                Text(
                    text = "${land.soilType} • ${land.locationLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = WaterTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Water: ${land.waterSources}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "History: ${land.agriculturalHistory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Land Health: ${land.landHealthScore}/100", style = MaterialTheme.typography.labelLarge)
                    Text(land.monitoringStatus, style = MaterialTheme.typography.labelSmall, color = ForestGreen)
                }
                LinearProgressIndicator(
                    progress = { (land.landHealthScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ForestGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PassportPill(text: String, containerColor: androidx.compose.ui.graphics.Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(50)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = OffWhite,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PassportStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EditFarmerPassportDialog(
    farmer: FarmerPassportEntity,
    languageCode: String,
    onDismiss: () -> Unit,
    onSave: (FarmerPassportEntity) -> Unit
) {
    var name by remember { mutableStateOf(farmer.name) }
    var farmerCode by remember { mutableStateOf(farmer.farmerIdCode) }
    var district by remember { mutableStateOf(farmer.district) }
    var hasSmartphone by remember { mutableStateOf(farmer.hasSmartphone) }
    var isOfflineType by remember { mutableStateOf(farmer.connectionType == "OFFLINE") }
    var expText by remember { mutableStateOf(farmer.experienceYears.toString()) }
    var acresText by remember { mutableStateOf(farmer.cultivatedAcres.toString()) }
    var mainCrop by remember { mutableStateOf(farmer.mainCrop) }
    var schemes by remember { mutableStateOf(farmer.governmentSchemes) }
    var rotation by remember { mutableStateOf(farmer.rotationStrategy) }
    var phone by remember { mutableStateOf(farmer.phoneNumber) }
    var cadence by remember { mutableStateOf(farmer.dispatchCadence) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Farmer Passport") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceInputTextField(name, { name = it }, "Farmer Name", languageCode)
                VoiceInputTextField(farmerCode, { farmerCode = it }, "Farmer ID", languageCode)
                VoiceInputTextField(district, { district = it }, "District", languageCode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Has Smartphone")
                    Switch(
                        checked = hasSmartphone,
                        onCheckedChange = {
                            hasSmartphone = it
                            if (!it) isOfflineType = true
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Offline Mode (SMS/Voice)")
                    Switch(checked = isOfflineType, onCheckedChange = { isOfflineType = it })
                }
                VoiceInputTextField(phone, { phone = it }, "Mobile Number (for SMS/Voice)", languageCode)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY").forEach { option ->
                        FilterChip(
                            selected = cadence == option,
                            onClick = { cadence = option },
                            label = { Text(option, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                VoiceInputTextField(expText, { expText = it }, "Experience (Years)", languageCode)
                VoiceInputTextField(acresText, { acresText = it }, "Cultivated Acres", languageCode)
                VoiceInputTextField(mainCrop, { mainCrop = it }, "Main Crop", languageCode)
                VoiceInputTextField(schemes, { schemes = it }, "Government Schemes", languageCode, singleLine = false, maxLines = 2)
                VoiceInputTextField(rotation, { rotation = it }, "Rotation Strategy", languageCode, singleLine = false, maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    farmer.copy(
                        name = name.ifBlank { farmer.name },
                        farmerIdCode = farmerCode.ifBlank { farmer.farmerIdCode },
                        district = district.ifBlank { farmer.district },
                        hasSmartphone = hasSmartphone,
                        connectionType = if (isOfflineType) "OFFLINE" else "ONLINE",
                        experienceYears = expText.toIntOrNull() ?: farmer.experienceYears,
                        cultivatedAcres = acresText.toDoubleOrNull() ?: farmer.cultivatedAcres,
                        mainCrop = mainCrop.ifBlank { farmer.mainCrop },
                        governmentSchemes = schemes,
                        rotationStrategy = rotation,
                        phoneNumber = phone.ifBlank { farmer.phoneNumber },
                        dispatchCadence = cadence
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RenewPassportDialog(
    farmer: FarmerPassportEntity,
    languageCode: String,
    onDismiss: () -> Unit,
    onRenew: (String, Double, String, String) -> Unit
) {
    var mainCrop by remember { mutableStateOf(farmer.mainCrop) }
    var acresText by remember { mutableStateOf(farmer.cultivatedAcres.toString()) }
    var rotation by remember { mutableStateOf(farmer.rotationStrategy) }
    var yieldNote by remember { mutableStateOf("2.3 t/ac harvested") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew Passport • Cycle #${farmer.cycleNumber + 1}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your profile (${farmer.name}, ${farmer.farmerIdCode}, ${farmer.district}) carries over automatically. Archive this harvest and set your next cycle (+40 Leaves).",
                    style = MaterialTheme.typography.bodySmall
                )
                VoiceInputTextField(yieldNote, { yieldNote = it }, "Completed Harvest Summary", languageCode)
                VoiceInputTextField(mainCrop, { mainCrop = it }, "Next Main Crop", languageCode)
                VoiceInputTextField(acresText, { acresText = it }, "Acres for Next Cycle", languageCode)
                VoiceInputTextField(rotation, { rotation = it }, "Next Rotation Strategy", languageCode, singleLine = false, maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                onRenew(
                    mainCrop.ifBlank { farmer.mainCrop },
                    acresText.toDoubleOrNull() ?: farmer.cultivatedAcres,
                    rotation.ifBlank { farmer.rotationStrategy },
                    yieldNote.ifBlank { "Completed" }
                )
            }) {
                Text("Renew & Earn +40 Leaves")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun EditLandPassportDialog(
    initialLand: LandPassportEntity?,
    languageCode: String,
    onDismiss: () -> Unit,
    onSave: (LandPassportEntity) -> Unit
) {
    val context = LocalContext.current
    var landName by remember { mutableStateOf(initialLand?.landName ?: "") }
    var areaText by remember { mutableStateOf((initialLand?.areaAcres ?: 3.5).toString()) }
    var soilType by remember { mutableStateOf(initialLand?.soilType ?: "Alluvial Loam") }
    var locationLabel by remember { mutableStateOf(initialLand?.locationLabel ?: "Nashik Sector") }
    var latText by remember { mutableStateOf((initialLand?.latitude ?: 19.9975).toString()) }
    var lonText by remember { mutableStateOf((initialLand?.longitude ?: 73.7898).toString()) }
    var waterSources by remember { mutableStateOf(initialLand?.waterSources ?: "Drip Canal + Borewell") }
    var history by remember { mutableStateOf(initialLand?.agriculturalHistory ?: "Cycle 1: Millet") }
    var photoUri by remember { mutableStateOf(initialLand?.photoUri ?: "") }

    val landPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLastCoarseCoordinates(context)?.let { (lat, lon) ->
                latText = String.format("%.4f", lat)
                lonText = String.format("%.4f", lon)
                locationLabel = "GPS • ${latText}°N, ${lonText}°E"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialLand == null) "Add Land Passport" else "Edit Land Passport") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        landPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoUri.isBlank()) "Choose Land Picture" else "Land Picture Selected")
                }
                VoiceInputTextField(landName, { landName = it }, "Land Plot Name", languageCode)
                VoiceInputTextField(areaText, { areaText = it }, "Land Area (Acres)", languageCode)
                VoiceInputTextField(soilType, { soilType = it }, "Soil Type", languageCode)
                VoiceInputTextField(locationLabel, { locationLabel = it }, "Location / Village", languageCode)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VoiceInputTextField(latText, { latText = it }, "Lat", languageCode, modifier = Modifier.weight(1f))
                    VoiceInputTextField(lonText, { lonText = it }, "Lon", languageCode, modifier = Modifier.weight(1f))
                }
                TextButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            fetchLastCoarseCoordinates(context)?.let { (lat, lon) ->
                                latText = String.format("%.4f", lat)
                                lonText = String.format("%.4f", lon)
                                locationLabel = "GPS • ${latText}°N, ${lonText}°E"
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    }
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-Fill Device Location")
                }
                VoiceInputTextField(waterSources, { waterSources = it }, "Water Sources", languageCode)
                VoiceInputTextField(history, { history = it }, "Agricultural History", languageCode, singleLine = false, maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    LandPassportEntity(
                        id = initialLand?.id ?: 0L,
                        landName = landName.ifBlank { "New Farm Plot" },
                        photoUri = photoUri,
                        areaAcres = areaText.toDoubleOrNull() ?: 3.0,
                        soilType = soilType.ifBlank { "Alluvial Loam" },
                        locationLabel = locationLabel.ifBlank { "District Plot" },
                        latitude = latText.toDoubleOrNull() ?: 19.9975,
                        longitude = lonText.toDoubleOrNull() ?: 73.7898,
                        waterSources = waterSources.ifBlank { "Rain-fed" },
                        agriculturalHistory = history.ifBlank { "New Parcel" },
                        landHealthScore = initialLand?.landHealthScore ?: 82,
                        soilMoisturePercent = initialLand?.soilMoisturePercent ?: 58,
                        monitoringStatus = initialLand?.monitoringStatus ?: "Telemetry Ready",
                        isSelected = initialLand?.isSelected ?: true
                    )
                )
            }) {
                Text("Save Land")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@SuppressLint("MissingPermission")
private fun fetchLastCoarseCoordinates(context: Context): Pair<Double, Double>? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    return loc?.let { it.latitude to it.longitude }
}
