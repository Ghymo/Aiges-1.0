package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AegisScreen
import com.example.ui.AegisViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val aegisViewModel: AegisViewModel = viewModel()
                        AegisNavigationRouter(viewModel = aegisViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AegisNavigationRouter(viewModel: AegisViewModel) {
    val screen by viewModel.currentScreen.collectAsState()
    val lang by viewModel.activeLanguage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val active = screen) {
            is AegisScreen.Splash -> AegisSplashScreen(lang)
            is AegisScreen.Welcome -> WelcomeOnboardingScreen(
                lang = lang,
                onNavigateSignUp = { viewModel.navigateTo(AegisScreen.SignUp) },
                onNavigateSignIn = { viewModel.navigateTo(AegisScreen.SignIn) }
            )
            is AegisScreen.SignUp -> SignUpScreen(
                lang = lang,
                onContinue = { name, phone -> viewModel.onSignUpContinue(name, phone) },
                onBack = { viewModel.navigateTo(AegisScreen.Welcome) }
            )
            is AegisScreen.CreatePIN -> PinKeypadScreen(
                lang = lang,
                titleKey = "create_pin",
                descKey = "enter_pin",
                pinLength = 4,
                onComplete = { pin -> viewModel.onPINCreateComplete(active.name, active.phone, pin) },
                showBackButton = true,
                onBack = { viewModel.navigateTo(AegisScreen.SignUp) }
            )
            is AegisScreen.ConfirmPIN -> PinKeypadScreen(
                lang = lang,
                titleKey = "confirm_pin",
                descKey = "confirm_pin_desc",
                pinLength = 4,
                onComplete = { pin ->
                    if (pin == active.createdPin) {
                        viewModel.onPINConfirmSuccess(active.name, active.phone, pin)
                    } else {
                        // Wrong PIN: force back to Create PIN to restart
                        viewModel.navigateTo(AegisScreen.CreatePIN(active.name, active.phone))
                    }
                },
                showBackButton = true,
                onBack = { viewModel.navigateTo(AegisScreen.CreatePIN(active.name, active.phone)) }
            )
            is AegisScreen.ProfileCompletion -> ProfileCompletionScreen(
                lang = lang,
                viewModel = viewModel
            )
            is AegisScreen.SignIn -> SignInScreen(
                lang = lang,
                viewModel = viewModel,
                onSuccess = { viewModel.navigateTo(AegisScreen.MainAppContainer) }
            )
            is AegisScreen.AddChildStep -> AddChildStepScreen(
                lang = lang,
                onDone = { name, age, av -> viewModel.onAddChildDone(name, age, av) }
            )
            is AegisScreen.ChoosePackageStep -> ChoosePackageStepScreen(
                lang = lang,
                childName = active.name,
                onBack = { viewModel.navigateTo(AegisScreen.AddChildStep) },
                onPackageSelected = { pkg -> viewModel.onPackageChosen(active.name, active.age, active.avatar, pkg) }
            )
            is AegisScreen.PairDeviceStep -> PairDeviceStepScreen(
                lang = lang,
                packageChosen = active.packageChosen,
                onBack = { viewModel.navigateTo(AegisScreen.AddChildStep) },
                onComplete = { viewModel.onDevicePaired(active.name, active.age, active.avatar, active.packageChosen) }
            )
            is AegisScreen.AddFamilyMemberStep -> AddFamilyMemberStepScreen(
                lang = lang,
                viewModel = viewModel,
                onComplete = { viewModel.onAddFamilyStepDone() }
            )
            is AegisScreen.SetGeofenceSetup -> SetGeofenceScreen(
                lang = lang,
                viewModel = viewModel,
                onComplete = { viewModel.navigateTo(AegisScreen.MainAppContainer) }
            )
            is AegisScreen.MainAppContainer -> MainAppContainer(
                lang = lang,
                viewModel = viewModel,
                onNavigateAddChildWizard = { viewModel.navigateTo(AegisScreen.AddChildStep) },
                onNavigateEditProfile = { viewModel.navigateTo(AegisScreen.ProfileCompletion) }
            )
        }

        // Instantly overlay critical modal alerts if active geofence breaches are detected
        val activeAlert by viewModel.focusedAlert.collectAsState()
        if (activeAlert != null) {
            DangerAlertOverlayModal(lang = lang, viewModel = viewModel)
        }
    }
}
