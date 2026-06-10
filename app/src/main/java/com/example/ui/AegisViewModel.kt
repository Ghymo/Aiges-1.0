package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AegisRepository
import com.example.data.ChildTracker
import com.example.data.FamilyMember
import com.example.data.GeofenceRegion
import com.example.data.Language
import com.example.data.RealtimeLocation
import com.example.data.SafetyAlert
import com.example.data.UserProfile
import com.example.data.OutboundNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AegisScreen {
    object Splash : AegisScreen()
    object Welcome : AegisScreen()
    object SignUp : AegisScreen()
    data class CreatePIN(val name: String, val phone: String) : AegisScreen()
    data class ConfirmPIN(val name: String, val phone: String, val createdPin: String) : AegisScreen()
    object ProfileCompletion : AegisScreen()
    object SignIn : AegisScreen()
    
    // 4-Step Registration Flows for new tracking beacons
    object AddChildStep : AegisScreen() // Step 1 of 4
    data class ChoosePackageStep(val name: String, val age: String, val avatar: String) : AegisScreen() // Step 2 of 4
    data class PairDeviceStep(val name: String, val age: String, val avatar: String, val packageChosen: String) : AegisScreen() // Step 3 of 4
    object AddFamilyMemberStep : AegisScreen() // Step 4 of 4
    
    // Boundary Placement Screen
    object SetGeofenceSetup : AegisScreen()
    
    // Core Dashboard Container
    object MainAppContainer : AegisScreen()
}

class AegisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AegisRepository(application)

    // Current Navigation Route
    private val _currentScreen = MutableStateFlow<AegisScreen>(AegisScreen.Splash)
    val currentScreen: StateFlow<AegisScreen> = _currentScreen.asStateFlow()

    // Bottom Navigation Rail / Tab Index
    private val _activeTab = MutableStateFlow(0) // 0: Map, 1: Children, 2: Zones, 3: Family, 4: Settings
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Reactive database flows
    val currentUser = repository.currentUser
    val children = repository.children
    val geofences = repository.geofences
    val familyMembers = repository.familyMembers
    val alerts = repository.alerts
    val liveLocations = repository.liveLocations
    val activeLanguage = repository.activeLanguage
    val outboundNotifications = repository.outboundNotifications

    // Toggle and setting configurations of Settings Tab
    private val _zoneAlertsEnabled = MutableStateFlow(true)
    val zoneAlertsEnabled = _zoneAlertsEnabled.asStateFlow()

    private val _locationUpdatesEnabled = MutableStateFlow(true)
    val locationUpdatesEnabled = _locationUpdatesEnabled.asStateFlow()

    private val _smsBackupEnabled = MutableStateFlow(true)
    val smsBackupEnabled = _smsBackupEnabled.asStateFlow()

    private val _lowBatteryWarningEnabled = MutableStateFlow(true)
    val lowBatteryWarningEnabled = _lowBatteryWarningEnabled.asStateFlow()

    private val _fingerprintEnabled = MutableStateFlow(false)
    val fingerprintEnabled = _fingerprintEnabled.asStateFlow()

    // Autocomplete list of predefined major cities in Nigeria
    val nigerianCities = listOf(
        "Lagos", "Ibadan", "Abuja", "Kano", "Port Harcourt", 
        "Benin City", "Kaduna", "Enugu", "Onitsha", "Aba", 
        "Jos", "Ilorin", "Ogbomosho", "Zaria", "Warri"
    )

    // Temporary values for forms
    val selectedChildForGeofence = MutableStateFlow<ChildTracker?>(null)
    
    // Active alert focus for detailed modal overlay
    private val _focusedAlert = MutableStateFlow<SafetyAlert?>(null)
    val focusedAlert: StateFlow<SafetyAlert?> = _focusedAlert.asStateFlow()

    init {
        // Trigger Splash Timer transition
        viewModelScope.launch {
            delay(2500) // 2.5 seconds splash display
            val user = currentUser.value
            if (user == null) {
                _currentScreen.value = AegisScreen.Welcome
            } else if (!user.profileComplete) {
                _currentScreen.value = AegisScreen.ProfileCompletion
            } else {
                _currentScreen.value = AegisScreen.MainAppContainer
            }
        }

        // Periodically verify un-resolved alerts and overlay them modal-wise
        viewModelScope.launch {
            while (true) {
                delay(1200)
                val unResolved = alerts.value.firstOrNull { !it.isResolved }
                if (unResolved != null && _focusedAlert.value == null) {
                    _focusedAlert.value = unResolved
                    triggerDeviceVibration()
                }
            }
        }
    }

    // Navigation controllers
    fun navigateTo(screen: AegisScreen) {
        _currentScreen.value = screen
    }

    fun selectTab(index: Int) {
        _activeTab.value = index
    }

    // Biometric helper
    fun toggleBiometrics() {
        _fingerprintEnabled.value = !_fingerprintEnabled.value
    }

    // System configurations
    fun toggleNotificationOption(type: String) {
        when (type) {
            "zone" -> _zoneAlertsEnabled.value = !_zoneAlertsEnabled.value
            "location" -> _locationUpdatesEnabled.value = !_locationUpdatesEnabled.value
            "sms" -> _smsBackupEnabled.value = !_smsBackupEnabled.value
            "battery" -> _lowBatteryWarningEnabled.value = !_lowBatteryWarningEnabled.value
        }
    }

    // Profile Completion percentages calculation logic
    fun calculateProfileCompletionPercentage(user: UserProfile?): Int {
        if (user == null) return 0
        var completedCount = 0
        val totalFields = 6 // FullName, DOB, Gender, City, Role, Photo

        if (user.fullName.isNotBlank()) completedCount++
        if (user.dateOfBirth.isNotBlank()) completedCount++
        if (user.gender.isNotBlank()) completedCount++
        if (user.city.isNotBlank()) completedCount++
        if (user.role.isNotBlank()) completedCount++
        if (user.profilePhoto.isNotBlank()) completedCount++

        return (completedCount * 100) / totalFields
    }

    // Set User Language selection
    fun changeSystemLanguage(lang: Language) {
        repository.updateLanguage(lang.code)
    }

    // --- Authentication Actions ---
    fun onSignUpContinue(name: String, phone: String) {
        _currentScreen.value = AegisScreen.CreatePIN(name, phone)
    }

    fun onPINCreateComplete(name: String, phone: String, pin: String) {
        _currentScreen.value = AegisScreen.ConfirmPIN(name, phone, pin)
    }

    fun onPINConfirmSuccess(name: String, phone: String, pin: String) {
        viewModelScope.launch {
            val hashedPin = pin // Simulated Hash
            val success = repository.registerOrSignIn(phone, hashedPin)
            if (success) {
                val current = repository.currentUser.value
                if (current != null) {
                    repository.saveUserProfile(current.copy(fullName = name, profileComplete = false))
                }
                _currentScreen.value = AegisScreen.ProfileCompletion
            }
        }
    }

    fun onSignInComplete(phone: String, pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.registerOrSignIn(phone, pin)
            if (success) {
                val user = repository.currentUser.value
                if (user != null && !user.profileComplete) {
                    _currentScreen.value = AegisScreen.ProfileCompletion
                } else {
                    _currentScreen.value = AegisScreen.MainAppContainer
                }
                onSuccess()
            } else {
                onError()
                triggerDeviceVibration()
            }
        }
    }

    fun onBiometricLoginRequested(context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        // Biometric authentication logic - FIXED
        viewModelScope.launch {
            try {
                // Simulate fingerprint scan delay
                delay(1000)
                
                // Get stored credentials from secure storage
                val prefs = context.getSharedPreferences("aegis_secure_store", Context.MODE_PRIVATE)
                val cachedPhone = prefs.getString("user_phone", null)
                val cachedPin = prefs.getString("user_pin_hash", null)
                
                // Check if biometric credentials are available
                if (cachedPhone != null && cachedPin != null) {
                    val loginSuccess = repository.registerOrSignIn(cachedPhone, cachedPin)
                    if (loginSuccess) {
                        val user = repository.currentUser.value
                        if (user != null && !user.profileComplete) {
                            _currentScreen.value = AegisScreen.ProfileCompletion
                        } else {
                            _currentScreen.value = AegisScreen.MainAppContainer
                        }
                        onSuccess()
                    } else {
                        onError()
                    }
                } else {
                    // No biometric credentials stored
                    onError()
                }
            } catch (e: Exception) {
                android.util.Log.e("AegisBiometric", "Biometric login failed: ${e.message}")
                onError()
            }
        }
    }

    fun updateProfileInfo(
        dob: String, gender: String, city: String, role: String, photo: String,
        linkedin: String, github: String, website: String
    ) {
        val current = repository.currentUser.value ?: return
        val complete = dob.isNotBlank() && gender.isNotBlank() && city.isNotBlank() && role.isNotBlank() && photo.isNotBlank()
        val updatedProfile = current.copy(
            dateOfBirth = dob,
            gender = gender,
            city = city,
            role = role,
            profilePhoto = photo,
            linkedIn = linkedin,
            github = github,
            website = website,
            profileComplete = complete
        )
        repository.saveUserProfile(updatedProfile)
        
        // Done with profile, proceed to addchild
        _currentScreen.value = AegisScreen.AddChildStep
    }

    // --- Add Child Stepper Actions ---
    fun onAddChildDone(name: String, age: String, avatar: String) {
        _currentScreen.value = AegisScreen.ChoosePackageStep(name, age, avatar)
    }

    fun onPackageChosen(name: String, age: String, avatar: String, pkg: String) {
        _currentScreen.value = AegisScreen.PairDeviceStep(name, age, avatar, pkg)
    }

    fun onDevicePaired(name: String, age: String, avatar: String, pkg: String) {
        repository.addChild(name, age, avatar, pkg)
        _currentScreen.value = AegisScreen.AddFamilyMemberStep
    }

    fun onAddFamilyStepDone() {
        // If there are children, pre-select first child for geofence mapping setup
        selectedChildForGeofence.value = repository.children.value.lastOrNull()
        _currentScreen.value = AegisScreen.SetGeofenceSetup
    }

    fun saveGeofenceAndDone(name: String, lat: Double, lon: Double, radius: Double, from: String, to: String, days: List<String>, childId: String) {
        repository.addGeofence(name, lat, lon, radius, from, to, days, childId)
        _currentScreen.value = AegisScreen.MainAppContainer
    }

    // Standard CRUD actions during Main app run
    fun addMainGeofence(name: String, lat: Double, lon: Double, radius: Double, from: String, to: String, days: List<String>, childId: String) {
        repository.addGeofence(name, lat, lon, radius, from, to, days, childId)
    }

    fun addFamilyRegular(name: String, phone: String, email: String, relationship: String, accessLevel: String) {
        repository.addFamilyMember(name, phone, email, relationship, accessLevel)
    }

    fun launchDeviceEmail(recipient: String, subject: String, body: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("AegisEmail", "Failed to launch mail intent: ${e.message}")
        }
    }

    fun removeFamilyRegular(id: String) {
        repository.removeFamilyMember(id)
    }

    fun toggleGeofenceMain(id: String) {
        repository.toggleGeofence(id)
    }

    fun deleteGeofenceMain(id: String) {
        repository.deleteGeofence(id)
    }

    fun performSignOut() {
        repository.signOut()
        _currentScreen.value = AegisScreen.SignIn
    }

    // Resolve active overlay modal
    fun dismissFocusedAlert() {
        val alert = _focusedAlert.value
        if (alert != null) {
            repository.resolveAlert(alert.alertId)
        }
        _focusedAlert.value = null
    }

    fun escalateFocusedAlert() {
        val alert = _focusedAlert.value
        if (alert != null) {
            repository.escalateToAmber(alert.alertId)
        }
    }

    // --- Interactive Simulation / Demonstration controls ---
    fun simulateChildGPSDriftOut(childId: String) {
        val child = children.value.find { it.childId == childId } ?: return
        val activeFence = geofences.value.find { it.childId == childId || it.childId.isEmpty() }

        if (activeFence != null) {
            // Place coordinate 550 meters outside of the geofence center to trigger the alert
            val driftLat = activeFence.latitude + 0.007 // roughly 770m drift
            val driftLon = activeFence.longitude + 0.007
            repository.updateChildLocationForcefully(childId, driftLat, driftLon)
        } else {
            // If no geofence active, create some drift off Ibadan center
            repository.updateChildLocationForcefully(childId, 7.3910, 3.9610)
        }
    }

    fun triggerChildManualSOS(childId: String) {
        repository.triggerSOSAlert(childId)
    }

    fun triggerGPSHopNormal(childId: String) {
        // Move slightly but stay inside Ibadan center
        val randLat = 7.3775 + (Math.random() - 0.5) * 0.002
        val randLon = 3.9470 + (Math.random() - 0.5) * 0.002
        repository.updateChildLocationForcefully(childId, randLat, randLon)
    }

    fun updateChildGPSCoordinates(childId: String, lat: Double, lon: Double) {
        repository.updateChildLocationForcefully(childId, lat, lon)
    }

    private fun triggerDeviceVibration() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(400)
        } catch (e: Exception) {
            // Safe fallback on headless execution
        }
    }

    fun updateEmailSimulated(email: String) {
        val current = repository.currentUser.value ?: return
        val updatedProfile = current.copy(email = email)
        repository.saveUserProfile(updatedProfile)
    }

    fun updatePhoneSimulated(phone: String) {
        val current = repository.currentUser.value ?: return
        val updatedProfile = current.copy(phone = phone)
        repository.saveUserProfile(updatedProfile)
    }

    fun deleteAccountSimulated() {
        val current = repository.currentUser.value ?: return
        val emptyProfile = UserProfile()
        repository.saveUserProfile(emptyProfile)
    }
}
