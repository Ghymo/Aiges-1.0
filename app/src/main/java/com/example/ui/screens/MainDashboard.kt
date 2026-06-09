package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.data.RealtimeLocation
import com.example.data.ChildTracker
import com.example.data.Language
import com.example.data.TranslationStore.t
import com.example.ui.AegisScreen
import com.example.ui.AegisViewModel
import com.example.ui.theme.*
import java.util.*

@Composable
fun MainAppContainer(
    lang: String,
    viewModel: AegisViewModel,
    onNavigateAddChildWizard: () -> Unit,
    onNavigateEditProfile: () -> Unit
) {
    val activeTab by viewModel.activeTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkNavy,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                val tabs = listOf(
                    Triple(Icons.Default.Map, t("home_tab", lang), 0),
                    Triple(Icons.Default.ChildCare, t("children_tab", lang), 1),
                    Triple(Icons.Default.Shield, t("zones_tab", lang), 2),
                    Triple(Icons.Default.People, t("family_tab", lang), 3),
                    Triple(Icons.Default.Settings, t("settings_tab", lang), 4)
                )

                tabs.forEach { item ->
                    val selected = activeTab == item.third
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(item.third) },
                        icon = { Icon(imageVector = item.first, contentDescription = item.second, tint = if (selected) GoldAccent else PureWhite.copy(alpha = 0.5f)) },
                        label = { Text(item.second, fontSize = 9.sp, color = if (selected) GoldAccent else PureWhite.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkSurface
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeTab) {
                0 -> HomeScreenTab(lang, viewModel)
                1 -> ChildrenScreenTab(lang, viewModel, onNavigateAddChildWizard)
                2 -> GeofencesScreenTab(lang, viewModel, onNavigateAddChildWizard)
                3 -> FamilyScreenTab(lang, viewModel)
                4 -> SettingsScreenTab(lang, viewModel, onNavigateEditProfile)
            }
        }
    }
}

@Composable
fun HomeScreenTab(lang: String, viewModel: AegisViewModel) {
    val children by viewModel.children.collectAsState()
    val geofences by viewModel.geofences.collectAsState()
    val liveLocations by viewModel.liveLocations.collectAsState()
    val curUser by viewModel.currentUser.collectAsState()

    var selectedChildId by remember { mutableStateOf("") }
    
    // Set first child as selected initially if none chosen
    LaunchedEffect(children) {
        if (selectedChildId.isEmpty() && children.isNotEmpty()) {
            selectedChildId = children.first().childId
        }
    }

    val childFocused = children.find { it.childId == selectedChildId }
    val matchingLiveCoords = liveLocations[selectedChildId]

    // Animation configuration for pulses
    val infiniteTransition = rememberInfiniteTransition(label = "markerPulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2000), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // MAP Canvas visual elements representation representing Ibadan City, Nigeria
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Map background style
            drawRect(color = Color(0xFFE2E7ED))

            // Render active geofence constraints (Gold Dashed circles)
            geofences.forEach { fence ->
                if (fence.isActive && (fence.childId == selectedChildId || fence.childId.isEmpty())) {
                    // Coordinates projection scaling representation centered
                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.1f),
                        radius = fence.radius.toFloat() * 0.45f,
                        center = center
                    )
                    drawCircle(
                        color = GoldAccent,
                        radius = fence.radius.toFloat() * 0.45f,
                        center = center,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )
                }
            }

            // Radar drift beacon animation
            drawCircle(
                color = SafeGreen.copy(alpha = 0.15f),
                radius = 70.dp.toPx() * pulseSize,
                center = center
            )
        }

        // 1. Pulses Emoji markers overlays
        if (childFocused != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Safe status tag
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = t("safe", lang),
                            color = PureWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(PureWhite, shape = CircleShape)
                            .border(3.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(childFocused.avatar, fontSize = 28.sp)
                    }
                }
            }
        }

        // 2. Floating Top Greeting Bar with Bell Notification
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldAccent, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(curUser?.profilePhoto ?: "👦", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val greeting = when {
                            hour < 12 -> "Good morning"
                            hour < 17 -> "Good afternoon"
                            else -> "Good evening"
                        }
                        Text(greeting, fontSize = 11.sp, color = PureWhite.copy(alpha = 0.6f))
                        Text(curUser?.fullName?.split(" ")?.firstOrNull() ?: "Guardian", fontSize = 14.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Bell overlay selector buttons
            Box {
                FloatingActionButton(
                    onClick = { /* Simulated history log open */ },
                    containerColor = PureWhite,
                    contentColor = DarkNavy,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "alerts")
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(DangerRed, shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
        }

        // 3. Float red SOS Button on Map
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 220.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Drift Forcers (Demo triggers)
            if (childFocused != null) {
                FloatingActionButton(
                    onClick = { viewModel.simulateChildGPSDriftOut(childFocused.childId) },
                    containerColor = GoldAccent,
                    contentColor = DarkNavy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Exit Boundary", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            FloatingActionButton(
                onClick = { 
                    if (childFocused != null) {
                        viewModel.triggerChildManualSOS(childFocused.childId)
                    }
                },
                containerColor = DangerRed,
                contentColor = PureWhite,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .border(3.dp, PureWhite, CircleShape)
            ) {
                Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
            }
        }

        // 4. Horizontal switcher slides deck
        if (children.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 144.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(children) { ch ->
                        val currentActive = ch.childId == selectedChildId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentActive) DarkNavy else PureWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clickable { selectedChildId = ch.childId }
                                .border(
                                    width = 1.5.dp,
                                    color = if (currentActive) GoldAccent else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(ch.avatar, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ch.name, color = if (currentActive) PureWhite else DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. Drawer/Bottom sheet details Card
        if (childFocused != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(childFocused.avatar, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(childFocused.name, fontWeight = FontWeight.Black, color = DarkNavy, fontSize = 16.sp)
                                Text("${childFocused.packageType} • Age ${childFocused.age}", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.5f))
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = t("safe", lang),
                                color = SafeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${t("bottom_sheet_battery", lang)}: ${childFocused.batteryLevel}%",
                            fontSize = 11.sp,
                            color = DarkNavy.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${t("bottom_sheet_last_seen", lang)}: ${childFocused.lastSeen}",
                            fontSize = 11.sp,
                            color = DarkNavy.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            // Emptystate overlay banner
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(t("empty_children", lang), color = DarkNavy, textAlign = TextAlign.Center, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ChildrenScreenTab(lang: String, viewModel: AegisViewModel, onNavigateAddChild: () -> Unit) {
    val itemsList by viewModel.children.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // Custom Dashboard Header toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(t("children_tab", lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkNavy)
                Text("Manage and query telemetry receivers", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.5f))
            }

            IconButton(onClick = onNavigateAddChild) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add", tint = GoldAccent, modifier = Modifier.size(36.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (itemsList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No active child monitors registered yet.",
                                color = DarkNavy,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the plus icon above to define a telemetry child node, pair a wristwear, or configure Software-Only GPS link.",
                                color = DarkNavy.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(itemsList) { child ->
                    val statusColor = if (child.isOnline) SafeGreen else DangerRed
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(width = 1.dp, color = DarkNavy.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(DarkNavy.copy(alpha = 0.05f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(child.avatar, fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(child.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                                        Text("Age ${child.age} • ${child.packageType}", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.5f))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(statusColor, shape = CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (child.isOnline) "ONLINE" else "OFFLINE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = statusColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Diagnostic mini columns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("BATTERY LEVEL", fontSize = 9.sp, color = DarkNavy.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${child.batteryLevel}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavy)
                                    }
                                }

                                Column {
                                    Text("LAST SYNCED", fontSize = 9.sp, color = DarkNavy.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                    Text(child.lastSeen, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavy)
                                }

                                Column {
                                    Text("FALLBACK MODE", fontSize = 9.sp, color = DarkNavy.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                    Text("AT_GSM_ACTIVE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldAccent)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ElevatedButton(
                                    onClick = { viewModel.triggerGPSHopNormal(child.childId) },
                                    colors = ButtonDefaults.elevatedButtonColors(containerColor = DarkNavy),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Track Now", color = PureWhite, fontSize = 11.sp)
                                }

                                ElevatedButton(
                                    onClick = { viewModel.triggerChildManualSOS(child.childId) },
                                    colors = ButtonDefaults.elevatedButtonColors(containerColor = DangerRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Force SOS", color = PureWhite, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Always display the AEGIS active safety alerts suite hub below
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AegisSafetySuiteHub(lang, viewModel)
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun GeofencesScreenTab(lang: String, viewModel: AegisViewModel, onNavigateAddChild: () -> Unit) {
    val arrayData by viewModel.geofences.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(t("zones_tab", lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkNavy)
                Text("Predefined geographical borders", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.5f))
            }

            IconButton(onClick = onNavigateAddChild) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = GoldAccent, modifier = Modifier.size(36.dp))
            }
        }

        if (arrayData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = t("empty_zones", lang),
                        color = DarkNavy.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(arrayData) { fence ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(fence.name, fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 16.sp)
                                        Text("Limits Radial: ${fence.radius.toInt()}m", color = DarkNavy.copy(alpha = 0.4f), fontSize = 11.sp)
                                    }
                                }

                                Switch(
                                    checked = fence.isActive,
                                    onCheckedChange = { viewModel.toggleGeofenceMain(fence.geofenceId) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = SafeGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    fence.activeDays.forEach { d ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DarkNavy.copy(alpha = 0.05f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(d, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ACTIVE MINUTES", fontSize = 8.sp, color = DarkNavy.copy(alpha = 0.4f))
                                    Text("${fence.activeFrom} - ${fence.activeTo}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DarkNavy)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                OutlinedButton(
                                    onClick = { viewModel.deleteGeofenceMain(fence.geofenceId) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete Zone", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyScreenTab(lang: String, viewModel: AegisViewModel) {
    val itemsList by viewModel.familyMembers.collectAsState()
    val curUser by viewModel.currentUser.collectAsState()

    var inviteName by remember { mutableStateOf("") }
    var invitePhone by remember { mutableStateOf("+234") }
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRelation by remember { mutableStateOf("Mother") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(t("family_tab", lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkNavy)
            Text("Authorize trusted peer observers", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.5f))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Guardian Chief Card First
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(GoldAccent, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(curUser?.profilePhoto ?: "💂", fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(curUser?.fullName ?: "Chief Guardian", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 18.sp)
                            Text(t("owner", lang), color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(curUser?.phone ?: "", color = PureWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Loop items
            if (itemsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t("empty_family", lang), color = DarkNavy.copy(alpha = 0.5f), textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                }
            } else {
                items(itemsList) { member ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(DarkNavy.copy(alpha = 0.05f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (member.relationship) {
                                            "Mother" -> "👩"
                                            "Father" -> "👨"
                                            "Grandmother" -> "👵"
                                            "Grandfather" -> "👴"
                                            else -> "👤"
                                        },
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, color = DarkNavy)
                                    Text("${member.relationship} • Access ${member.accessLevel}", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.4f))
                                }
                            }

                            // invite badge status active vs pending
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (member.status == "active") SafeGreen.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (member.status == "active") t("active", lang) else t("pending", lang),
                                        color = if (member.status == "active") SafeGreen else GoldAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = t("remove", lang),
                                    fontSize = 10.sp,
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.removeFamilyRegular(member.memberId) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick add loops section at bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(t("invite", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inviteName,
                            onValueChange = { inviteName = it },
                            label = { Text("Peer obv Name") },
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
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = invitePhone,
                            onValueChange = { if (it.startsWith("+234")) invitePhone = it },
                            label = { Text(t("phone_number", lang)) },
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
                         Spacer(modifier = Modifier.height(10.dp))

                         OutlinedTextField(
                            value = inviteEmail,
                            onValueChange = { inviteEmail = it },
                            label = { Text("Email Address (Optional)") },
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
                         Spacer(modifier = Modifier.height(14.dp))

                         Button(
                             onClick = {
                                 if (inviteName.isNotBlank() && invitePhone.length >= 13) {
                                     viewModel.addFamilyRegular(inviteName, invitePhone, inviteEmail, inviteRelation, "Full Access")
                                     inviteName = ""
                                     invitePhone = "+234"
                                     inviteEmail = ""
                                 }
                             },
                             colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                             shape = RoundedCornerShape(10.dp),
                             modifier = Modifier.fillMaxWidth()
                          ) {
                            Text(t("invite", lang), color = PureWhite, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenTab(lang: String, viewModel: AegisViewModel, onNavigateEditProfile: () -> Unit) {
    val curUser by viewModel.currentUser.collectAsState()
    val completion = viewModel.calculateProfileCompletionPercentage(curUser)

    val listLanguages = listOf(
        Language.ENGLISH,
        Language.YORUBA,
        Language.IGBO,
        Language.HAUSA,
        Language.PIDGIN
    )

    var showSignoutConfirm by remember { mutableStateOf(false) }
    var ipProtectionEnabled by remember { mutableStateOf(true) }
    var twoStepEnabled by remember { mutableStateOf(false) }
    var selectedServiceDialog by remember { mutableStateOf<String?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var passkeyInput by remember { mutableStateOf("") }

    var accountExpanded by remember { mutableStateOf(false) }
    var privacyExpanded by remember { mutableStateOf(false) }
    var supportExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(t("settings_tab", lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = DarkNavy)
            Spacer(modifier = Modifier.height(16.dp))

            // Profile info Summary
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(GoldAccent, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(curUser?.profilePhoto ?: "💂", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(curUser?.fullName ?: "Guardian", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkNavy)
                            Text(curUser?.phone ?: "", color = DarkNavy.copy(alpha = 0.5f), fontSize = 13.sp)
                            Text("${curUser?.role?.uppercase(Locale.ROOT)} (${curUser?.city})", fontWeight = FontWeight.Black, color = GoldAccent, fontSize = 10.sp)
                        }
                    }

                    // Edit details navigate link
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateEditProfile() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("edit_profile", lang), color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }

                    // Completion percentage badge shown if profile is not 100 percent complete
                    if (completion < 100) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DangerRed.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${t("percentage_complete", lang)}: $completion% complete", color = DangerRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Operational Language selection
            Text(t("language_selection", lang), fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listLanguages) { l ->
                    val active = l.code == lang
                    FilterChip(
                        selected = active,
                        onClick = { viewModel.changeSystemLanguage(l) },
                        label = { Text(l.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications toggles row
            Text(t("settings_notifications", lang), fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val notifyConfig = listOf(
                        Triple(t("alert_zone", lang), viewModel.zoneAlertsEnabled.collectAsState().value, "zone"),
                        Triple(t("alert_loc", lang), viewModel.locationUpdatesEnabled.collectAsState().value, "location"),
                        Triple(t("alert_sms", lang), viewModel.smsBackupEnabled.collectAsState().value, "sms"),
                        Triple(t("alert_battery", lang), viewModel.lowBatteryWarningEnabled.collectAsState().value, "battery")
                    )

                    notifyConfig.forEachIndexed { i, config ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(config.first, fontSize = 13.sp, color = DarkNavy)
                            Switch(
                                checked = config.second,
                                onCheckedChange = { viewModel.toggleNotificationOption(config.third) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SafeGreen
                                )
                            )
                        }
                        if (i < notifyConfig.size - 1) Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed Informative Aegis Library
            Text(t("help_header", lang), fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HOW AEGIS SECURE NETWORK WORKS",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aegis secures children in vulnerable areas by layering decoy hardware, secure localized offline links, and dynamic safety barriers.",
                        color = DarkNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val guides = listOf(
                        "1. Deep Redundancy & Decoy Protections" to "Children carry standard everyday wearables (watches, belts, backpacks) configured as active tracking items. A tiny secondary RF tracking node is concealed inside the child's clothing/lining. If primary decoy items are removed or lost, the secondary passive sensor continues emitting signal handshakes securely.",
                        "2. Africa's Talking Offline SMS Gateway" to "Our network relies on robust SMS backup loops. When mobile carrier data (2G/3G/4G) is jammed, compromised, or off-grid, Aegis pings direct numerical geo-coordinates automatically to neighborhood watch nodes via Africa's Talking SMS network channels.",
                        "3. Boundary Guard Patrol (Dynamic Geofences)" to "Guardians define geo-fencable safe sectors (e.g., School yards, homes, activity hubs) directly on their control deck. If a child crosses these marked perimeter borders without authorization, localized audio alarms and phone warnings dispatch to registered guardians instantly.",
                        "4. Simulated Decoy Sirens & Call Deflection" to "In extreme crisis scenarios, guardians can trigger high-decibel tracker alarms or incoming decoy phone calls on the child's wearable. This diverts hostile attention, acts as an active deterrent, and signals immediate public help in crowds."
                    )
                    guides.forEachIndexed { idx, pair ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(pair.first, fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(pair.second, color = DarkNavy.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        if (idx < guides.size - 1) Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ACCOUNT SECURITY & PROTOCOLS (Collapsible Accordion) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { accountExpanded = !accountExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SECURE ACCOUNT PANEL", fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
                                Text("Manage passkey, email, and 2FA credentials", fontSize = 10.sp, color = DarkNavy.copy(alpha = 0.5f))
                            }
                        }
                        Icon(
                            imageVector = if (accountExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Section",
                            tint = DarkNavy.copy(alpha = 0.6f)
                        )
                    }

                    AnimatedVisibility(visible = accountExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Fingerprint,
                                title = "Passkey Setup",
                                subtitle = "Enable passwordless biometric secure node entry",
                                onClick = { selectedServiceDialog = "passkey" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Email,
                                title = "Secure Recovery Email",
                                subtitle = if (curUser?.email.isNullOrBlank()) "guardian@aegis.com" else curUser?.email,
                                onClick = { selectedServiceDialog = "email" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Lock,
                                title = "Two-Step Verification",
                                subtitle = "Validate safety changes over dual backup channels",
                                trailing = {
                                    Switch(
                                        checked = twoStepEnabled,
                                        onCheckedChange = { twoStepEnabled = it },
                                        colors = SwitchDefaults.colors(checkedTrackColor = SafeGreen)
                                    )
                                },
                                onClick = { twoStepEnabled = !twoStepEnabled }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Phone,
                                title = "Change Phone Number",
                                subtitle = if (curUser?.phone.isNullOrBlank()) t("profile_completed_title", lang) else curUser?.phone,
                                onClick = { selectedServiceDialog = "phone" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.List,
                                title = "Request Account Info",
                                subtitle = "Request complete telemetry data package logs",
                                onClick = { selectedServiceDialog = "request_info" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Delete,
                                title = "Delete Permanent Account",
                                subtitle = "Decommission and purge database keys",
                                onClick = { selectedServiceDialog = "delete_account" }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PRIVACY & ENCRYPTION (Collapsible Accordion) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { privacyExpanded = !privacyExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("PRIVACY & NETWORKING", fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
                                Text("IP masking and coordinate shielding", fontSize = 10.sp, color = DarkNavy.copy(alpha = 0.5f))
                            }
                        }
                        Icon(
                            imageVector = if (privacyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Section",
                            tint = DarkNavy.copy(alpha = 0.6f)
                        )
                    }

                    AnimatedVisibility(visible = privacyExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Shield,
                                title = "Protect IP Address",
                                subtitle = "Route location beacons via secure proxy onions",
                                trailing = {
                                    Switch(
                                        checked = ipProtectionEnabled,
                                        onCheckedChange = { ipProtectionEnabled = it },
                                        colors = SwitchDefaults.colors(checkedTrackColor = SafeGreen)
                                    )
                                },
                                onClick = { ipProtectionEnabled = !ipProtectionEnabled }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Info,
                                title = "Privacy Policy",
                                subtitle = "Learn how your offline coordinates are shielded",
                                onClick = { selectedServiceDialog = "privacy_policy" }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SUPPORT & FEEDBACK (Collapsible Accordion) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { supportExpanded = !supportExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SUPPORT & DOCUMENTATION", fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
                                Text("FAQs, technical feedback logs, and terms", fontSize = 10.sp, color = DarkNavy.copy(alpha = 0.5f))
                            }
                        }
                        Icon(
                            imageVector = if (supportExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Section",
                            tint = DarkNavy.copy(alpha = 0.6f)
                        )
                    }

                    AnimatedVisibility(visible = supportExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.HelpCenter,
                                title = "Help Center Support FAQs",
                                subtitle = "Calibrate networks, sensors, and GPS zones",
                                onClick = { selectedServiceDialog = "helpcenter" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Feedback,
                                title = "Send Technical Feedback",
                                subtitle = "Submit bugs, patches, or system suggestions",
                                onClick = { selectedServiceDialog = "feedback" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Terminal,
                                title = "Channel Telemetry Reports",
                                subtitle = "Monitor SMS, satellite, and LTE handshakes",
                                onClick = { selectedServiceDialog = "channel_reports" }
                            )
                            Divider()
                            SettingsRow(
                                icon = Icons.Default.Info,
                                title = "Terms of Service Agreement",
                                subtitle = "Liability limits and defensive guidelines",
                                onClick = { selectedServiceDialog = "terms" }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- INVESTOR DEMO & SYSTEM DATABASE PORTAL ---
            Text("INVESTOR DEMO & REGISTERED DATABASE", fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.5.dp, GoldAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF2ECC71), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SANDBOX SQL ENGINE ONLINE", color = Color(0xFF2ECC71), fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                        Text("ACTIVE DEMO", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 8.sp, modifier = Modifier.background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aegis security databases reside locally in SQLite sandbox layers (SharedPreferences state) inside this APK. This matches the exact database tables synced dynamically with active SMS nodes during cellular live sweeping.",
                        color = DarkNavy.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Expansible subtab selector
                    var demoSubTab by remember { mutableStateOf(0) } // 0: Local Account DB, 1: Active GPS Beacons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightBackground)
                            .padding(2.dp)
                    ) {
                        listOf("Parent SQL DB", "GPS Joystick").forEachIndexed { idx, label ->
                            val active = demoSubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) DarkNavy else Color.Transparent)
                                    .clickable { demoSubTab = idx }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) PureWhite else DarkNavy.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (demoSubTab == 0) {
                        // Parents SQL Database table simulator
                        Column {
                            Text("query: SELECT * FROM aegis_parents_registry", color = DarkNavy.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Show registered users
                            val parentsTable = listOf(
                                Triple(curUser?.fullName ?: "Guardian-Demo", curUser?.phone ?: "No phone registered", curUser?.city ?: "Ibadan (Host)"),
                                Triple("Olowo Ibrahim", "+234 803 948 5739", "Lagos (Subscriber)"),
                                Triple("Chinedu Okafor", "+234 812 394 8572", "Port Harcourt (Subscriber)"),
                                Triple("Aminu Yusuf", "+234 905 382 9471", "Abuja (Subscriber)")
                            )

                            parentsTable.forEachIndexed { index, row ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = LightBackground.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(row.first, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkNavy)
                                            Text(if (index == 0) "🟢 HOST ADMIN" else "🔹 SUBSCRIBER", color = if (index == 0) Color(0xFF2ECC71) else GoldAccent, fontWeight = FontWeight.Black, fontSize = 8.sp)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Contact: ${row.second}", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.7f))
                                            Text(row.third, fontSize = 10.sp, color = DarkNavy.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Beacons Joystick Sliders
                        val activeKids by viewModel.children.collectAsState()
                        val liveCoords by viewModel.liveLocations.collectAsState()

                        if (activeKids.isEmpty()) {
                            Text(
                                "No registered child beacons found. Go to children tab to link a smart watch, adhesive patch, or simulated phone beacon.",
                                color = DangerRed,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        } else {
                            Column {
                                activeKids.forEach { kid ->
                                    val loc = liveCoords[kid.childId] ?: RealtimeLocation(childId = kid.childId)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LightBackground.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(kid.avatar, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(kid.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavy)
                                                }
                                                Text(
                                                    text = kid.packageType.uppercase(Locale.ROOT),
                                                    color = GoldAccent,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 8.sp,
                                                    modifier = Modifier.background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Live Coordinates:\nLat: ${String.format(Locale.US, "%.5f", loc.latitude)}, Lon: ${String.format(Locale.US, "%.5f", loc.longitude)}", fontSize = 10.sp, color = DarkNavy.copy(alpha = 0.6f), lineHeight = 14.sp)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Simulation Quick Action buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.updateChildGPSCoordinates(kid.childId, 7.3775, 3.9470) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp)
                                                ) {
                                                    Text("Safe Center", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { 
                                                        // Slide 900 meters outside geofence boundary to trigger alarm instantly!
                                                        viewModel.simulateChildGPSDriftOut(kid.childId)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1.5f).height(32.dp)
                                                ) {
                                                    Text("Simulate Exit", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { viewModel.triggerChildManualSOS(kid.childId) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp)
                                                ) {
                                                    Text("Sim SOS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Absolute sign out triggers
            Button(
                onClick = { showSignoutConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("sign_out", lang).uppercase(Locale.ROOT), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AEGIS — v1.2.4-Naija-Active\nAnzen for every family",
                fontSize = 11.sp,
                color = DarkNavy.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (selectedServiceDialog != null) {
        AlertDialog(
            onDismissRequest = { selectedServiceDialog = null },
            confirmButton = {
                TextButton(onClick = { selectedServiceDialog = null }) {
                    Text("DONE", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                val titleString = when (selectedServiceDialog) {
                    "passkey" -> "Configure Biometric Passkey"
                    "email" -> "Recovery Email Address"
                    "phone" -> "Configure SIM phone link"
                    "request_info" -> "Request Complete Account Logs"
                    "delete_account" -> "⚠️ CRITICAL SYSTEM ACTION"
                    "helpcenter" -> "Aegis Help Center Support"
                    "feedback" -> "Send System Feedback"
                    "channel_reports" -> "Channel Telemetry Monitoring"
                    "terms" -> "Aegis Terms of Service"
                    "privacy_policy" -> "Aegis Privacy Policy Protocol"
                    else -> "Settings Details"
                }
                Text(titleString, fontWeight = FontWeight.Black, color = DarkNavy, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (selectedServiceDialog) {
                        "passkey" -> {
                            Text("Passkeys allow passwordless, encrypted biometric login using your phone's secure enclave (Face or Fingerprint scanner).", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 16.sp)
                            OutlinedTextField(
                                value = passkeyInput,
                                onValueChange = { passkeyInput = it },
                                label = { Text("Passkey Nickname") },
                                placeholder = { Text("e.g. Master Device Passkey") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkNavy, unfocusedTextColor = DarkNavy),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { selectedServiceDialog = null },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Bind Biometric Passkey Keys", color = PureWhite, fontSize = 12.sp)
                            }
                        }
                        "email" -> {
                            Text("Your secure email receives critical daily telemetry summaries and failover alert dispatches if networks are disrupted.", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 16.sp)
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkNavy, unfocusedTextColor = DarkNavy),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { 
                                    viewModel.updateEmailSimulated(emailInput)
                                    selectedServiceDialog = null 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Secure Email", color = PureWhite, fontSize = 12.sp)
                            }
                        }
                        "phone" -> {
                            Text("This node SIM number is used by Aegis algorithms to synchronize offline geographical telemetry coordinates over Africa's Talking.", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 16.sp)
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("SIM Phone Number") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkNavy, unfocusedTextColor = DarkNavy),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { 
                                    viewModel.updatePhoneSimulated(phoneInput)
                                    selectedServiceDialog = null 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save New Tracking SIM", color = PureWhite, fontSize = 12.sp)
                            }
                        }
                        "request_info" -> {
                            Text("Aegis will compile and package all secure node telemetry, including your calibrated safe zones, paired tracker IDs, and broadcast histories.", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 16.sp)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📄 Generating package: It will be encrypted and dispatched to your email address dynamically within 5-10 minutes.", fontSize = 11.sp, color = DarkNavy, modifier = Modifier.padding(10.dp), lineHeight = 15.sp)
                            }
                        }
                        "delete_account" -> {
                            Text("WARNING: Deleting your account will completely purge child telemetry profiles, paired hardware nodes, geofence coordinates, and clear regional watch databases immediately.", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                            Button(
                                onClick = { 
                                    viewModel.deleteAccountSimulated()
                                    selectedServiceDialog = null 
                                    showSignoutConfirm = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Permanently Erase System Keys Now", color = PureWhite, fontSize = 12.sp)
                            }
                        }
                        "helpcenter" -> {
                            Text("Browse real solutions to typical tracking scenarios:", fontSize = 12.sp, color = DarkNavy, fontWeight = FontWeight.Bold)
                            val faqs = listOf(
                                "How do I avoid false drift alerts?" to "Ensure safe-zone radiuses are set above 150 meters so atmospheric cell mast hop doesn't trigger anomalous exits.",
                                "Battery is draining fast?" to "Increase the telemetry check-in interval to 'Hourly' inside notifications settings above to conserve hardware sensors.",
                                "Device offline warning?" to "Simulate cellular beacons or configure Africa's Talking offline backup loops to handle off-grid environments."
                            )
                            faqs.forEach { faq ->
                                Column {
                                    Text("• " + faq.first, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DarkNavy)
                                    Text(faq.second, fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                        "feedback" -> {
                            Text("We constantly refine Aegis defenses using suggestions from field operators. Submit raw suggestion telemetry:", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f))
                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                label = { Text("Suggestions / Technical Logs") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkNavy, unfocusedTextColor = DarkNavy),
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )
                            Button(
                                onClick = { 
                                    feedbackText = ""
                                    selectedServiceDialog = null 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Transmit Encrypted Feedback", color = PureWhite, fontSize = 12.sp)
                            }
                        }
                        "channel_reports" -> {
                            Text("Realtime status check across tracking socket streams:", fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f))
                            val logs = listOf(
                                "[LOG 10:12:05] ACTIVE: GPS node handshake approved (1.2m accuracy).",
                                "[LOG 10:30:11] SAT-COM: Handshake ping success code 300.",
                                "[LOG 10:44:02] INTEGRITY: Fallback GSM alerts channel fully operational. 0 active sweeps."
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    logs.forEach { log ->
                                        Text(log, fontSize = 9.sp, color = DarkNavy, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, lineHeight = 12.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }
                        "terms" -> {
                            Text("Aegis defensive software, decoy trackers, and SMS alert channels serve as preventive and educational safety tools. Parents and guardians must collaborate proactively with local safety patrols and tactical groups during real threats.", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 15.sp)
                        }
                        "privacy_policy" -> {
                            Text("We do not store trace records of location coordinates unless you explicitly initiate an Amber/Critical sweep event. Local geofence perimeters are fully encrypted on client hardware database vaults.", fontSize = 11.sp, color = DarkNavy.copy(alpha = 0.7f), lineHeight = 15.sp)
                        }
                    }
                }
            },
            containerColor = PureWhite
        )
    }

    if (showSignoutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignoutConfirm = false },
            title = { Text(t("confirm_signout", lang)) },
            text = { Text(t("confirm_text", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignoutConfirm = false
                        viewModel.performSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(t("sign_out", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignoutConfirm = false }) {
                    Text(t("cancel", lang))
                }
            },
            containerColor = PureWhite
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 13.sp)
            if (subtitle != null) {
                Text(subtitle, color = DarkNavy.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = DarkNavy.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================
// AEGIS PREVENTATIVE SAFETY & ALERTS HUB
// ==========================================
@Composable
fun AegisSafetySuiteHub(lang: String, viewModel: AegisViewModel) {
    var expandedFeature by remember { mutableStateOf<String?>(null) }

    // State parameters for simulation
    var showCircleToast by remember { mutableStateOf(false) }
    var circleAlertActive by remember { mutableStateOf(false) }
    var circleLog by remember { mutableStateOf("Ready to transmit secure circle beacon.") }

    var timeIntervalSlider by remember { mutableStateOf(15f) } // 15 mins, 30 mins, 60 mins
    var sightingText by remember { mutableStateOf("") }
    var submittedSightingLog by remember { mutableStateOf("") }

    var routineLearningEnabled by remember { mutableStateOf(true) }
    var mockSightingReport by remember { mutableStateOf("No local bystander dispatches recorded.") }

    var showPosterModal by remember { mutableStateOf(false) }
    var dossierStatusLog by remember { mutableStateOf("Ready to assemble case dossier.") }

    val activeChildren by viewModel.children.collectAsState()
    val targetChildName = if (activeChildren.isNotEmpty()) activeChildren[0].name else "Aegis Child"
    val targetChildAvatar = if (activeChildren.isNotEmpty()) activeChildren[0].avatar else "👦"

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AEGIS ALERT & SAFETY SUITE",
                        color = PureWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Prevents risk and triggers multi-channel emergency networks",
                        color = PureWhite.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Trusted Circle Alert (Tap to reveal details and simulate)
            AegisSafetyItemCard(
                title = "1. Trusted Circle Direct Alert",
                subtitle = "Notify relatives & teachers dynamically",
                icon = Icons.Default.People,
                isExpanded = expandedFeature == "trusted_circle",
                onClick = { expandedFeature = if (expandedFeature == "trusted_circle") null else "trusted_circle" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Automatically pings child coordinates, photos, maps, and dial handles to registered immediate guardian devices when reported at-risk.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TRUSTED ENCLAVE CONTACTS:",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    val circleContacts = listOf(
                        "👨 Father (Active SMS Hub)",
                        "👩 Mother (Primary Responder)",
                        "👵 Grandmother (Secondary Relay)",
                        "👨‍🏫 School Head Teacher (Perimeter Guard)"
                    )
                    CircleContactList(contacts = circleContacts)

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            circleAlertActive = true
                            circleLog = "Alert dispatched! Contacts: 4 notified. Mother (RESPONDING): 'Scanning zone!' Tracker live GPS link synced."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (circleAlertActive) DangerRed else GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = if (circleAlertActive) PureWhite else DarkNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (circleAlertActive) "CRITICAL ALERT ACTIVE — VIEW LIVE FEED" else "TEST TRUSTED CIRCLE EMERGENCY PING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (circleAlertActive) PureWhite else DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CONSOLE: $circleLog",
                            color = if (circleAlertActive) GoldAccent else PureWhite.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Community Alert Network
            AegisSafetyItemCard(
                title = "2. Community Alert Network (NFC & GPS)",
                subtitle = "Scalable local radius broadcast filters",
                icon = Icons.Default.ShareLocation,
                isExpanded = expandedFeature == "community_network",
                onClick = { expandedFeature = if (expandedFeature == "community_network") null else "community_network" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Launches neighborhood-wide alerts inside a dynamic radius. The radius automatically scales based on duration offline.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Radius scale slider
                    val radiusKm = when {
                        timeIntervalSlider <= 15 -> 2
                        timeIntervalSlider <= 40 -> 5
                        else -> 10
                    }
                    Text(
                        text = "DURATION OFFLINE: ${timeIntervalSlider.toInt()} MINS • BROADCAST RADIUS: $radiusKm KM",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Slider(
                        value = timeIntervalSlider,
                        onValueChange = { timeIntervalSlider = it },
                        valueRange = 15f..60f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = PureWhite.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SIMULATE LOCAL BYSTANDER SIGHTING REPORT:",
                        color = PureWhite.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = sightingText,
                        onValueChange = { sightingText = it },
                        placeholder = { Text("Describe location, clothing, or sightings...", color = PureWhite.copy(alpha = 0.3f), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = PureWhite.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (sightingText.isNotBlank()) {
                                submittedSightingLog = "[VERIFIED SIGHTING] ${sightingText.trim()}. Radar alerted."
                                sightingText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SUBMIT COMMUNITY SIGHTING", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    }

                    if (submittedSightingLog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = submittedSightingLog,
                                color = SafeGreen,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Geofence Breach
            AegisSafetyItemCard(
                title = "3. Geofence Breach Proactive Guard",
                subtitle = "Exit, delayed arrival, unauthorized areas",
                icon = Icons.Default.MapsHomeWork,
                isExpanded = expandedFeature == "geofence_breach",
                onClick = { expandedFeature = if (expandedFeature == "geofence_breach") null else "geofence_breach" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Configures dynamic geofencing boundaries. LearnJourney AI models the child's typical weekly paths and school routines to trigger early warnings when deviations occur.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Routine Learning AI Model", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = routineLearningEnabled,
                            onCheckedChange = { routineLearningEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = SafeGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "VIRTUAL FENCE BOUNDARY STATES:",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    val fenceStates = listOf(
                        "🏫 Primary School Yard: Arrival (Active) • Delayed Arrival Alert (Set for 13:45)",
                        "🏡 Sweet Home Perimeter: Exit Alert (Active) • Arrival (Alert Silent)",
                        "🕌 Local Mosque Sector: Boundary Exit (Active)"
                    )
                    Column {
                        fenceStates.forEach { fs ->
                            Text("📍 $fs", color = PureWhite.copy(alpha = 0.7f), fontSize = 10.sp, lineHeight = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Nearby bystander assistance
            AegisSafetyItemCard(
                title = "4. Nearby Bystander Alert Pings",
                subtitle = "Alert vetted Aegis rescuers safely",
                icon = Icons.Default.Sos,
                isExpanded = expandedFeature == "nearby_assistance",
                onClick = { expandedFeature = if (expandedFeature == "nearby_assistance") null else "nearby_assistance" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Enables emergency pings to secure bystander units in direct line-of-sight. Crucially, your parent/guardian personal details will be fully masked unless you explicitly grant security authorization.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            mockSightingReport = "[PINGING] Broad-network SOS packets transmitted to 8 vetted Aegis defenders. Masked tracking tunnel active."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PING SECURE BYSTANDER FORCE", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mockSightingReport,
                            color = PureWhite.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. AI Risk Detection
            AegisSafetyItemCard(
                title = "5. AI-Powered Danger & Risk Engine",
                subtitle = "Analyzes unusual telemetry speeds & shutoffs",
                icon = Icons.Default.Memory,
                isExpanded = expandedFeature == "ai_risk_detection",
                onClick = { expandedFeature = if (expandedFeature == "ai_risk_detection") null else "ai_risk_detection" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Automated background sweeps scan physical tracking indices for anomalous patterns: suspicious high-speed movement, unauthorized route deviation, and sudden node shutdown coordinates.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "REALTIME CORES & THREAT SUMMARY:",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    val riskLogs = listOf(
                        "🛰️ ROUTE DRIFT CORES: Nominal (0m offset limits)",
                        "🔋 WEARABLE ACCELEROMETER: Walking speed (1.4 m/s)",
                        "🔌 HARDWARE JAMMER DETECTION: No jamming pings detected",
                        "📲 SUDDEN SHUTOFF SECURE HANDSHAKE: Active socket checked"
                    )
                    Column {
                        riskLogs.forEach { log ->
                            Text(log, color = PureWhite.copy(alpha = 0.7f), fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Police Escalation Pack
            AegisSafetyItemCard(
                title = "6. Police Escalation Package",
                subtitle = "One-tap PDF, printable poster & history zip",
                icon = Icons.Default.LocalPolice,
                isExpanded = expandedFeature == "police_escalation",
                onClick = { expandedFeature = if (expandedFeature == "police_escalation") null else "police_escalation" }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Auto-compiles vital demographics: child name, age, physical indicators, last-known map coordinate plots, and historic trackings for immediate handoff.",
                        color = PureWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPosterModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("GENERATE POSTER", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                dossierStatusLog = "[SUCCESS] Compiled encrypted ZIP export. File saved inside your Downloads: Aegis_Escalation_${targetChildName}_Kit.pdf"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("EXPORT CASE PDF", fontSize = 10.sp, color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dossierStatusLog,
                            color = PureWhite.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showPosterModal) {
        AlertDialog(
            onDismissRequest = { showPosterModal = false },
            title = {
                Text(
                    text = "🚨 URGENT: COMS STATE CODES EXPORTED",
                    fontWeight = FontWeight.Black,
                    color = DangerRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, DangerRed)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "MISSING CHILD",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = DangerRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(DarkNavy.copy(alpha = 0.1f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(targetChildAvatar, fontSize = 64.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = targetChildName.uppercase(Locale.getDefault()),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LAST KNOWN GEO: 6.5244° N, 3.3792° E\nTIME OF ALERT: UTC Real-time",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "IF FOUND, INITIATE DIRECT CONTACT WITH PRIMARY RESPONDER CHANNELS IMMEDIATELY. VERIFIED SECURE COORDINATE ENCRYPTED WITH AEGIS INTEGRITY METRICS.",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = DarkNavy.copy(alpha = 0.6f),
                        lineHeight = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPosterModal = false }) {
                    Text("CLOSE POSTER PREVIEW", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PureWhite
        )
    }
}

@Composable
fun AegisSafetyItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = subtitle,
                            color = PureWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PureWhite.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Divider(color = PureWhite.copy(alpha = 0.1f))
                content()
            }
        }
    }
}

@Composable
fun CircleContactList(contacts: List<String>) {
    Column {
        contacts.forEach { contact ->
            Text(
                text = contact,
                color = PureWhite.copy(alpha = 0.8f),
                fontSize = 10.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
