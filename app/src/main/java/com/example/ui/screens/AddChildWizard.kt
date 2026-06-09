package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationStore.t
import com.example.ui.AegisViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun AddChildStepScreen(lang: String, onDone: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("👦") }

    val avatars = listOf("👦", "👧", "🧒", "👶")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                // Progress Wizard Info
                Text("Step 1 of 4", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("add_child", lang),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("add_child_desc", lang),
                    fontSize = 14.sp,
                    color = DarkNavy.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(t("child_name", lang)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkNavy,
                        unfocusedTextColor = DarkNavy,
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = DarkNavy.copy(alpha = 0.4f),
                        focusedLabelColor = DarkNavy,
                        unfocusedLabelColor = DarkNavy.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text(t("child_age", lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkNavy,
                        unfocusedTextColor = DarkNavy,
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = DarkNavy.copy(alpha = 0.4f),
                        focusedLabelColor = DarkNavy,
                        unfocusedLabelColor = DarkNavy.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(t("avatar", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    avatars.forEach { av ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = if (selectedAvatar == av) GoldAccent else DarkNavy.copy(alpha = 0.05f),
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatar = av },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(av, fontSize = 28.sp)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && age.isNotBlank()) {
                        onDone(name, age, selectedAvatar)
                    }
                },
                enabled = name.isNotBlank() && age.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = t("continue", lang).uppercase(Locale.ROOT),
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ChoosePackageStepScreen(
    lang: String,
    childName: String,
    onBack: () -> Unit,
    onPackageSelected: (String) -> Unit
) {
    var selectedPkg by remember { mutableStateOf("Aegis Complete (Watch + Patch)") }

    val packages = listOf(
        Triple("Aegis Decoy Watch", "₦35,000", "Custom decoy tracker disguised as structural standard kid smart watch."),
        Triple("Aegis Mobile Patch", "₦45,000", "Adhesive hidden locator to tape securely inside footwear/lining."),
        Triple("Aegis Complete (Watch + Patch)", "₦72,000", "Highly recommended bundle. Extreme multi-redundant node coverage.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Step 2 of 4", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("choose_package", lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(16.dp))

                packages.forEach { pkg ->
                    val isSelected = selectedPkg == pkg.first
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkNavy else PureWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedPkg = pkg.first }
                            .border(
                                width = 2.dp,
                                color = if (isSelected) GoldAccent else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPkg = pkg.first },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GoldAccent,
                                    unselectedColor = DarkNavy.copy(alpha = 0.4f)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = pkg.first,
                                    color = if (isSelected) PureWhite else DarkNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pkg.third,
                                    color = if (isSelected) PureWhite.copy(alpha = 0.6f) else DarkNavy.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pkg.second,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Special Protection Undertaking Warning Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = t("undertaking_sign", lang),
                            fontSize = 11.sp,
                            color = DarkNavy,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(t("back", lang).uppercase(Locale.ROOT), color = DarkNavy)
                }

                Button(
                    onClick = { onPackageSelected(selectedPkg) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(
                        text = t("continue", lang).uppercase(Locale.ROOT),
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PairDeviceStepScreen(
    lang: String,
    packageChosen: String,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    // 0: QR Scanner, 1: BLE Scan, 2: Manual serial code
    var setupMode by remember { mutableStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var successPair by remember { mutableStateOf(false) }
    var manualSerialCode by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val scanLineTransition = rememberInfiniteTransition(label = "scannerLine")
    val laserOffset by scanLineTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanProgress = 0f
            while (scanProgress < 1.0f) {
                delay(100)
                scanProgress += 0.04f
            }
            isScanning = false
            successPair = true
            delay(1500)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Step 3 of 4", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("pair_device", lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aegis hardware trackers require Carton QR Scan or serial linking code.",
                    fontSize = 12.sp,
                    color = DarkNavy.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tab Select Bar for Setup modes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkNavy.copy(alpha = 0.05f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Pair(Icons.Default.PhotoCamera, "Carton QR"),
                        Pair(Icons.Default.Bluetooth, "BLE Search"),
                        Pair(Icons.Default.Keyboard, "Manual Code")
                    )
                    tabs.forEachIndexed { idx, pair ->
                        val active = setupMode == idx
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) DarkNavy else Color.Transparent)
                                .clickable {
                                    if (!isScanning && !successPair) {
                                        setupMode = idx
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = pair.first,
                                contentDescription = null,
                                tint = if (active) GoldAccent else DarkNavy.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pair.second,
                                color = if (active) PureWhite else DarkNavy.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Interactive mode viewport
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkNavy),
                    contentAlignment = Alignment.Center
                ) {
                    if (successPair) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LINKED & CONNECTED",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (isScanning) {
                        // Viewing animation corresponding to active mode
                        if (setupMode == 0) {
                            // Carton QR Scanner Viewfinder mockup
                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Scanning Laser Line
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val yPos = size.height * laserOffset
                                    drawLine(
                                        color = GoldAccent,
                                        start = androidx.compose.ui.geometry.Offset(x = 0f, y = yPos),
                                        end = androidx.compose.ui.geometry.Offset(x = size.width, y = yPos),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CropFree,
                                        contentDescription = null,
                                        tint = PureWhite.copy(alpha = 0.3f),
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Capturing carton QR...", color = PureWhite.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }
                        } else {
                            // BLE search radar
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = GoldAccent.copy(alpha = 1f - scanProgress),
                                        radius = 120.dp.toPx() * pulseScale,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                                Icon(
                                    imageVector = if (packageChosen.contains("Watch")) Icons.Default.Watch else Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    } else {
                        // Static States (Ready to trigger)
                        when (setupMode) {
                            0 -> { // QR Code carton guide
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Scan QR code printed inside the Aegis tracker's carton pack.",
                                        color = PureWhite.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            1 -> { // BLE tracker guide
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (packageChosen.contains("Watch")) Icons.Default.Watch else Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Search for local cellular/bluetooth pairing node of $packageChosen.",
                                        color = PureWhite.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            2 -> { // Manual entry form
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "ENTER 8-DIGIT SERIAL",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = manualSerialCode,
                                        onValueChange = {
                                            if (it.length <= 12) {
                                                manualSerialCode = it.uppercase(Locale.getDefault())
                                                manualError = ""
                                            }
                                        },
                                        placeholder = { Text("AEGIS-XXXX", color = PureWhite.copy(alpha = 0.4f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PureWhite,
                                            unfocusedTextColor = PureWhite,
                                            focusedBorderColor = GoldAccent,
                                            unfocusedBorderColor = PureWhite.copy(alpha = 0.4f),
                                            focusedLabelColor = GoldAccent,
                                            unfocusedLabelColor = PureWhite.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                    )
                                    if (manualError.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = manualError,
                                            color = DangerRed,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (successPair) t("pair_success", lang) else if (isScanning) {
                        if (setupMode == 0) "Reading Carton QR code... Hold steady" else "Searching bluetooth beacons..."
                    } else "Ready to sync $packageChosen",
                    color = if (successPair) SafeGreen else DarkNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isScanning && !successPair) {
                    Button(
                        onClick = {
                            if (setupMode == 2) {
                                if (manualSerialCode.trim().length < 6) {
                                    manualError = "Please enter a valid carton serial code"
                                } else {
                                    isScanning = true
                                }
                            } else {
                                isScanning = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = (if (setupMode == 0) "Start QR Scanner" else if (setupMode == 1) "Scan Bluetooth Target" else "Link Serial Code").uppercase(Locale.ROOT),
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isScanning) {
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        color = GoldAccent,
                        trackColor = DarkNavy.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onBack) {
                        Text(t("cancel", lang), color = DarkNavy.copy(alpha = 0.5f))
                    }
                    TextButton(onClick = onComplete) {
                        Text(t("skip", lang) + " >>", color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddFamilyMemberStepScreen(
    lang: String,
    viewModel: AegisViewModel,
    onComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+234") }
    var relation by remember { mutableStateOf("Mother") }
    var accessLevel by remember { mutableStateOf("View Only") }

    val relationships = listOf("Mother", "Father", "Grandmother", "Grandfather", "Aunt", "Uncle", "Sibling", "Other")

    val activeMembers by viewModel.familyMembers.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Step 4 of 4", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("add_family", lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Input form
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkNavy,
                        unfocusedTextColor = DarkNavy,
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = DarkNavy.copy(alpha = 0.4f),
                        focusedLabelColor = DarkNavy,
                        unfocusedLabelColor = DarkNavy.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.startsWith("+234")) phone = it },
                    label = { Text(t("phone_number", lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkNavy,
                        unfocusedTextColor = DarkNavy,
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = DarkNavy.copy(alpha = 0.4f),
                        focusedLabelColor = DarkNavy,
                        unfocusedLabelColor = DarkNavy.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // relationship row selector
                Text(t("family_role", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(relationships) { rel ->
                        val active = relation == rel
                        FilterChip(
                            selected = active,
                            onClick = { relation = rel },
                            label = { Text(rel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkNavy,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // access level choose chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("access_level", lang), fontWeight = FontWeight.Bold, color = DarkNavy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("View Only", "Full Access").forEach { acc ->
                            val active = accessLevel == acc
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) DarkNavy else DarkNavy.copy(alpha = 0.05f))
                                    .clickable { accessLevel = acc }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = t("access_" + acc.lowercase(Locale.ROOT).replace(" ", ""), lang), color = if (active) PureWhite else DarkNavy, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.length >= 13) {
                            viewModel.addFamilyRegular(name, phone, relation, accessLevel)
                            name = ""
                            phone = "+234"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    enabled = name.isNotBlank() && phone.length >= 13,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(t("add_member", lang), color = DarkNavy, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Chips list of active members
                Text("Cirlce Chips:", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeMembers) { member ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(DarkNavy)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${member.name} (${member.relationship})", color = PureWhite, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "delete",
                                    tint = GoldAccent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.removeFamilyRegular(member.memberId) }
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onComplete) {
                    Text(t("skip", lang), color = DarkNavy.copy(alpha = 0.5f))
                }
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(t("continue", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SetGeofenceScreen(
    lang: String,
    viewModel: AegisViewModel,
    onComplete: () -> Unit
) {
    var zoneName by remember { mutableStateOf("") }
    var selectedRadius by remember { mutableStateOf(300.0) }
    var activeFromHour by remember { mutableStateOf("08:00") }
    var activeToHour by remember { mutableStateOf("17:00") }
    var activeDays by remember { mutableStateOf(listOf("Mon", "Tue", "Wed", "Thu", "Fri")) }

    // Map Click simulation coords offsets
    var clickOffsetLat by remember { mutableStateOf(0.0) }
    var clickOffsetLon by remember { mutableStateOf(0.0) }

    val presets = listOf(
        PresetZone("preset_school", "School", 300.0),
        PresetZone("preset_home", "Home", 200.0),
        PresetZone("preset_mosque", "Mosque", 150.0),
        PresetZone("preset_market", "Market", 400.0)
    )

    val radii = listOf(100.0, 200.0, 300.0, 500.0, 800.0)
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Simulated interactive Map layout using beautiful Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        // Simulate dragging click coordinates offset
                        clickOffsetLat = (Math.random() - 0.5) * 0.002
                        clickOffsetLon = (Math.random() - 0.5) * 0.002
                    }
            ) {
                // Background grid representation representing Ibadan coordinates
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFFE5E9F0))
                    
                    // Grid lines
                    val cols = 8
                    val rows = 12
                    for (i in 0..cols) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(i * (size.width / cols), 0f),
                            end = androidx.compose.ui.geometry.Offset(i * (size.width / cols), size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    for (i in 0..rows) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(0f, i * (size.height / rows)),
                            end = androidx.compose.ui.geometry.Offset(size.width, i * (size.height / rows)),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Predefined Safe zone outline
                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.15f),
                        radius = selectedRadius.toFloat() * 0.4f,
                        center = center
                    )
                    drawCircle(
                        color = GoldAccent,
                        radius = selectedRadius.toFloat() * 0.4f,
                        center = center,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )
                    
                    // Center marker
                    drawCircle(
                        color = DarkNavy,
                        radius = 8.dp.toPx(),
                        center = center
                    )
                }

                // Interactive map tags overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkNavy),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = t("set_geofence", lang),
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Float "Center on me" target
                FloatingActionButton(
                    onClick = {
                        clickOffsetLat = 0.0
                        clickOffsetLon = 0.0
                    },
                    containerColor = PureWhite,
                    contentColor = DarkNavy,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "me")
                }
            }

            // Lower controls card
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = zoneName,
                            onValueChange = { zoneName = it },
                            label = { Text(t("zone_name", lang)) },
                            placeholder = { Text("e.g. Ibadan School") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Presets Row
                        Text(t("presets", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presets.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkNavy.copy(alpha = 0.05f))
                                        .clickable {
                                            zoneName = preset.defaultName
                                            selectedRadius = preset.defaultRad
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(t(preset.key, lang), fontSize = 11.sp, color = DarkNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Radius choose line
                        Text(t("radius", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            radii.forEach { rad ->
                                val active = selectedRadius == rad
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (active) GoldAccent else DarkNavy.copy(alpha = 0.05f))
                                        .clickable { selectedRadius = rad }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("${rad.toInt()}m", fontSize = 12.sp, color = if (active) DarkNavy else DarkNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hours range (Simulated dialog clock popup)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = activeFromHour,
                                onValueChange = {},
                                label = { Text("From") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val mTimePicker = TimePickerDialog(context, { _, hour, minute ->
                                            activeFromHour = String.format(Locale.US, "%02d:%02d", hour, minute)
                                        }, 8, 0, true)
                                        mTimePicker.show()
                                    }) {
                                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = activeToHour,
                                onValueChange = {},
                                label = { Text("To") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val mTimePicker = TimePickerDialog(context, { _, hour, minute ->
                                            activeToHour = String.format(Locale.US, "%02d:%02d", hour, minute)
                                        }, 17, 0, true)
                                        mTimePicker.show()
                                    }) {
                                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active threat days selection row
                        Text(t("active_days", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            daysOfWeek.forEach { d ->
                                val active = activeDays.contains(d)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (active) DarkNavy else DarkNavy.copy(alpha = 0.05f), shape = CircleShape)
                                        .clickable {
                                            activeDays = if (active) activeDays - d else activeDays + d
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(d.take(1), color = if (active) PureWhite else DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val resolvedName = if (zoneName.isNotBlank()) zoneName else "Ibadan Safe Area"
                                // Coordinates centering Ibadan index + offsets
                                val centerLat = 7.3775 + clickOffsetLat
                                val centerLon = 3.9470 + clickOffsetLon
                                val activeChildId = viewModel.selectedChildForGeofence.value?.childId ?: ""

                                viewModel.saveGeofenceAndDone(
                                    name = resolvedName,
                                    lat = centerLat,
                                    lon = centerLon,
                                    radius = selectedRadius,
                                    from = activeFromHour,
                                    to = activeToHour,
                                    days = activeDays,
                                    childId = activeChildId
                                )
                                onComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(t("save_zone", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

data class PresetZone(val key: String, val defaultName: String, val defaultRad: Double)
