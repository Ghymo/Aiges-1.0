package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationStore.t
import com.example.ui.AegisViewModel
import com.example.ui.theme.*
import java.util.*

@Composable
fun DangerAlertOverlayModal(lang: String, viewModel: AegisViewModel) {
    val activeAlert by viewModel.focusedAlert.collectAsState()
    val alertValue = activeAlert ?: return

    var isAmberModalActive by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "crimsonRadar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    if (isAmberModalActive) {
        AmberAlertOverlayModal(
            lang = lang,
            childName = alertValue.childName,
            onClose = { isAmberModalActive = false },
            onConfirmSent = {
                viewModel.escalateFocusedAlert()
                isAmberModalActive = false
                viewModel.dismissFocusedAlert()
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AlertOverlayRed)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Pulse Warning ring drawing in Canvas
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = DangerRed.copy(alpha = 1f - pulseScale),
                                radius = 80.dp.toPx() * pulseScale,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(DangerRed, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🚨",
                                fontSize = 42.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = t("alert_title", lang),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(alertValue.childAvatar, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${alertValue.childName} • ${t("danger", lang)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Coordinates display Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("GPS BOUNDARY ANOMALY", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Zone Name: ${alertValue.zoneName}\nLatitude: ${alertValue.latitude}\nLongitude: ${alertValue.longitude}",
                                color = PureWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dispatch State: GSM Low-Connectivity SMS fallback online",
                                color = SafeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Call to actions buttons rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Track Live Location red button
                    Button(
                        onClick = { viewModel.dismissFocusedAlert() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("alert_live", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }

                    // Call watch VoIP secondary button
                    Button(
                        onClick = { /* simulated proxy call warning SMS */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("alert_call", lang), color = PureWhite, fontSize = 14.sp)
                    }

                    // Escalate Amber Alert button (Orange)
                    Button(
                        onClick = { isAmberModalActive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("alert_escalate", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Resolve alarm button at bottom
                    TextButton(
                        onClick = { viewModel.dismissFocusedAlert() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("alert_dismiss", lang).uppercase(Locale.ROOT), color = PureWhite.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AmberAlertOverlayModal(
    lang: String,
    childName: String,
    onClose: () -> Unit,
    onConfirmSent: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // Step 1: Checklist. Step 2: Inputs. Step 3: Success.

    // Step 1 checklist requirements
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    var check4 by remember { mutableStateOf(false) }

    // Step 2 descriptive fields
    var clothing by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AMBER EMERGENCY SYSTEM",
                        color = Color(0xFFD35400),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    Text(
                        text = t("amber_checklist", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "National safety law requires verifying these details to avoid accidental broadcast penalties.",
                        color = TextColorDark.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val checks = listOf(
                        Triple("I verify that the physical child trace is not currently responsive.", check1) { check1 = !check1 },
                        Triple("I have contacted my family observers to verify child whereabouts.", check2) { check2 = !check2 },
                        Triple("I agree to submit coordinates data directly to safety services.", check3) { check3 = !check3 },
                        Triple("I understand that incorrect alert triggers carry community penalty points.", check4) { check4 = !check4 }
                    )

                    checks.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clickable { p.third() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = p.second,
                                onCheckedChange = { p.third() },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GoldAccent
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(p.first, color = PureWhite, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                } else if (step == 2) {
                    Text(
                        text = "BROADCAST BLUEPRINT DETAILS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = clothing,
                        onValueChange = { clothing = it },
                        label = { Text(t("step_clothing", lang)) },
                        placeholder = { Text("e.g. Red shirt, blue shorts, black shoes") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            focusedLabelColor = GoldAccent,
                            cursorColor = GoldAccent,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text(t("step_add", lang)) },
                        placeholder = { Text("e.g. Last seen near Ibadan sector mosque gate.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            focusedLabelColor = GoldAccent,
                            cursorColor = GoldAccent,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alert Preview Card
                    Text(t("amber_preview", lang), color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "AEGIS EMERGENCY: AMBER ALERT DISPATCH ACTIVATED FOR $childName!\n\nFEATURES: $clothing\nCONTEXT: $remarks\nFALLBACK SMS BROADCAST IS DISTRIBUTING TO NEIGHBOR VANS NOW.",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    // Success state walk-through steps
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = t("amber_success", lang),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = t("amber_next_steps", lang),
                                color = PureWhite.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 1) {
                    val enabled = check1 && check2 && check3 && check4
                    Button(
                        onClick = { step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400)),
                        enabled = enabled,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(t("continue", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.ExtraBold)
                    }
                } else if (step == 2) {
                    val enabled = clothing.isNotBlank() && remarks.isNotBlank()
                    Button(
                        onClick = { step = 3 },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        enabled = enabled,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(t("amber_send", lang).uppercase(Locale.ROOT), color = PureWhite, fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(
                        onClick = { onConfirmSent() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text("DISMISS ALARM", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
