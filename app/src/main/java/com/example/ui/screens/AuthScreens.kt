package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Language
import com.example.data.TranslationStore.t
import com.example.data.UserProfile
import com.example.ui.AegisScreen
import com.example.ui.AegisViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun AegisSplashScreen(lang: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing Gold Rings
        Canvas(modifier = Modifier.size(240.dp)) {
            drawCircle(
                color = GoldAccent.copy(alpha = 0.15f),
                radius = 120.dp.toPx() * scale,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.3f),
                radius = 90.dp.toPx() * scale,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield Icon
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "AEGIS",
                fontSize = 32.sp,
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Anzen for every family",
                fontSize = 14.sp,
                color = GoldAccent,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = GoldAccent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = t("splash_loading", lang),
                fontSize = 12.sp,
                color = TextColorDark.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun WelcomeOnboardingScreen(lang: String, onNavigateSignUp: () -> Unit, onNavigateSignIn: () -> Unit) {
    var activeSlide by remember { mutableStateOf(0) }
    
    val slides = listOf(
        Triple("🛡️", t("welcome_slide1_title", lang), t("welcome_slide1_desc", lang)),
        Triple("📌", t("welcome_slide2_title", lang), t("welcome_slide2_desc", lang)),
        Triple("🔒", t("welcome_slide3_title", lang), t("welcome_slide3_desc", lang))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Language Select Row at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Aegis Secure Gateway",
                    fontSize = 12.sp,
                    color = DarkNavy.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Slide Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(DarkNavy, shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slides[activeSlide].first,
                        fontSize = 64.sp
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = slides[activeSlide].second,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = slides[activeSlide].third,
                    fontSize = 14.sp,
                    color = DarkNavy.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Indicators & Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gold dots indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (activeSlide == index) 12.dp else 8.dp)
                                .background(
                                    color = if (activeSlide == index) GoldAccent else DarkNavy.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Interaction Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onNavigateSignIn() },
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = t("skip", lang).uppercase(Locale.ROOT),
                            color = DarkNavy.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (activeSlide < slides.size - 1) {
                                activeSlide++
                            } else {
                                onNavigateSignUp()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .width(160.dp)
                    ) {
                        Text(
                            text = (if (activeSlide == slides.size - 1) t("get_started", lang) else t("next", lang)).uppercase(Locale.ROOT),
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SignUpScreen(lang: String, onContinue: (String, String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+234") }
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = DarkNavy)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Aegis " + t("add_child", lang),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = t("welcome_slide1_desc", lang),
                    fontSize = 14.sp,
                    color = DarkNavy.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(t("full_name", lang)) },
                    singleLine = true,
                    isError = nameError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                if (nameError) {
                    Text(
                        text = t("name_validation_err", lang),
                        color = DangerRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.startsWith("+234")) {
                            phone = it
                        }
                        phoneError = false
                    },
                    label = { Text(t("phone_number", lang)) },
                    placeholder = { Text("+234XXXXXXXXXX") },
                    singleLine = true,
                    isError = phoneError,
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
                if (phoneError) {
                    Text(
                        text = t("phone_validation_err", lang),
                        color = DangerRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val isNameValid = name.trim().isNotBlank()
                    val isPhoneValid = phone.length >= 13 && phone.startsWith("+234")

                    if (!isNameValid) nameError = true
                    if (!isPhoneValid) phoneError = true

                    if (isNameValid && isPhoneValid) {
                        onContinue(name, phone)
                    }
                },
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
fun PinKeypadScreen(
    lang: String,
    titleKey: String,
    descKey: String,
    pinLength: Int,
    onComplete: (String) -> Unit,
    showBackButton: Boolean = true,
    onBack: () -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                if (showBackButton) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = PureWhite)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = t(titleKey, lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t(descKey, lang),
                    fontSize = 14.sp,
                    color = TextColorDark.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))

                // Progress indicators (Gold dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until pinLength) {
                        val active = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = if (active) GoldAccent else TextColorDark.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (char.isNotEmpty()) TextColorDark.copy(alpha = 0.05f) else Color.Transparent)
                                    .clickable(enabled = char.isNotEmpty()) {
                                        if (char == "⌫") {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        } else {
                                            if (pin.length < pinLength) {
                                                pin += char
                                                if (pin.length == pinLength) {
                                                    onComplete(pin)
                                                    pin = "" // Reset on complete
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (char == "⌫") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "delete",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = char,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (char == "0") GoldAccent else PureWhite
                                    )
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
fun SignInScreen(
    lang: String,
    viewModel: AegisViewModel,
    onSuccess: () -> Unit
) {
    var phoneStep by remember { mutableStateOf(true) }
    var inputPhone by remember { mutableStateOf("+234") }
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val isBiometricsEnabled by viewModel.fingerprintEnabled.collectAsState()
    val context = LocalContext.current

    if (phoneStep) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = t("signin_title", lang),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t("signin_desc", lang),
                        fontSize = 14.sp,
                        color = DarkNavy.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(48.dp))

                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = {
                            if (it.startsWith("+234")) {
                                inputPhone = it
                            }
                        },
                        label = { Text(t("phone_number", lang)) },
                        placeholder = { Text("+234XXXXXXXXXX") },
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
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = DangerRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Simulated Biometrics fingerprint Button
                    Button(
                        onClick = {
                            viewModel.onBiometricLoginRequested(
                                context = context,
                                onSuccess = { onSuccess() },
                                onError = { errorMessage = "Biometric mismatch or not configured yet." }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = DarkNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("signin_fingerprint", lang), color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        if (inputPhone.length >= 13 && inputPhone.startsWith("+234")) {
                            errorMessage = ""
                            phoneStep = false
                        } else {
                            errorMessage = t("phone_validation_err", lang)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = t("next", lang).uppercase(Locale.ROOT),
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    } else {
        // PIN Pad step
        PinKeypadScreen(
            lang = lang,
            titleKey = "enter_pin",
            descKey = "signin_desc",
            pinLength = 4,
            onComplete = { pin ->
                inputPin = pin
                viewModel.onSignInComplete(
                    phone = inputPhone,
                    pin = pin,
                    onSuccess = { onSuccess() },
                    onError = {
                        errorMessage = t("pin_mismatch", lang)
                        phoneStep = true // Force back to fix
                    }
                )
            },
            showBackButton = true,
            onBack = { phoneStep = true }
        )
    }
}

@Composable
fun ProfileCompletionScreen(lang: String, viewModel: AegisViewModel) {
    var dob by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Male") }
    var selectedRole by remember { mutableStateOf("Father") }
    var cityQuery by remember { mutableStateOf("") }
    var cityExpanded by remember { mutableStateOf(false) }
    var profilePhotoId by remember { mutableStateOf("👦") } // Simulated avatar string

    var linkedin by remember { mutableStateOf("") }
    var github by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    var showErrorMsg by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Calculates realtime completion rating
    val curUser = UserProfile(
        fullName = "Dummy",
        dateOfBirth = dob,
        gender = selectedGender,
        city = cityQuery,
        role = selectedRole,
        profilePhoto = profilePhotoId,
    )
    val percentage = viewModel.calculateProfileCompletionPercentage(curUser)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = t("profile_completed_title", lang),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "We need this information to sync your security node.",
                fontSize = 13.sp,
                color = DarkNavy.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Percentage Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("percentage_complete", lang), color = PureWhite, fontSize = 14.sp)
                        Text("$percentage%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        color = GoldAccent,
                        trackColor = PureWhite.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // circular picture upload avatar simulation
            Text(t("photo_upload", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatars = listOf("👨‍✈️", "👩‍⚕️", "👨‍🎨", "👩‍🌾", "🕵️‍♂️", "👵")
                avatars.forEach { av ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (profilePhotoId == av) GoldAccent else DarkNavy.copy(alpha = 0.05f),
                                shape = CircleShape
                            )
                            .clickable {
                                profilePhotoId = av
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(av, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DOB Calendar Popup Simulation
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text(t("dob", lang)) },
                placeholder = { Text("YYYY-MM-DD") },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    focusedBorderColor = DarkNavy,
                    unfocusedBorderColor = DarkNavy.copy(alpha = 0.4f),
                    focusedLabelColor = DarkNavy,
                    unfocusedLabelColor = DarkNavy.copy(alpha = 0.6f)
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance()
                        val mYear = calendar[Calendar.YEAR] - 30
                        val mMonth = calendar[Calendar.MONTH]
                        val mDay = calendar[Calendar.DAY_OF_MONTH]

                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                dob = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
                            }, mYear, mMonth, mDay
                        )
                        datePickerDialog.show()
                    }) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = DarkNavy)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender drop selector chips
            Text(t("gender", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val genders = listOf("Male", "Female", "Other")
                genders.forEach { gen ->
                    val selected = selectedGender == gen
                    FilterChip(
                        selected = selected,
                        onClick = { selectedGender = gen },
                        label = { Text(text = t(gen.lowercase(Locale.ROOT), lang)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role selector
            Text(t("role", lang), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val roles = listOf("Father", "Mother", "Guardian", "Grandparent", "Other")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roles) { r ->
                    val selected = selectedRole == r
                    FilterChip(
                        selected = selected,
                        onClick = { selectedRole = r },
                        label = { Text(text = t("role_" + r.lowercase(Locale.ROOT), lang)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nigerian City Autocomplete Field
            OutlinedTextField(
                value = cityQuery,
                onValueChange = {
                    cityQuery = it
                    cityExpanded = true
                },
                label = { Text(t("city", lang)) },
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
            if (cityExpanded && cityQuery.isNotEmpty()) {
                val filteredCities = viewModel.nigerianCities.filter {
                    it.contains(cityQuery, ignoreCase = true)
                }
                if (filteredCities.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column {
                            filteredCities.take(4).forEach { city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            cityQuery = city
                                            cityExpanded = false
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(city, color = DarkNavy)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showErrorMsg) {
                Text(
                    text = t("complete_profile_warning", lang),
                    color = DangerRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    if (dob.isBlank() || cityQuery.isBlank() || selectedGender.isBlank()) {
                        showErrorMsg = true
                    } else {
                        showErrorMsg = false
                        viewModel.updateProfileInfo(
                            dob = dob,
                            gender = selectedGender,
                            city = cityQuery,
                            role = selectedRole,
                            photo = profilePhotoId,
                            linkedin = "",
                            github = "",
                            website = ""
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = t("save_profile", lang).uppercase(Locale.ROOT),
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
