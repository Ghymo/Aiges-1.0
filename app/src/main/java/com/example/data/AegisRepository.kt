package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.TranslationStore.t
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AegisRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("aegis_secure_store", Context.MODE_PRIVATE)

    // Reactive StateFlows for MVVM UI integration
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _children = MutableStateFlow<List<ChildTracker>>(emptyList())
    val children: StateFlow<List<ChildTracker>> = _children.asStateFlow()

    private val _geofences = MutableStateFlow<List<GeofenceRegion>>(emptyList())
    val geofences: StateFlow<List<GeofenceRegion>> = _geofences.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    private val _alerts = MutableStateFlow<List<SafetyAlert>>(emptyList())
    val alerts: StateFlow<List<SafetyAlert>> = _alerts.asStateFlow()

    // Holds simulated live coordinates for tracking map
    private val _liveLocations = MutableStateFlow<Map<String, RealtimeLocation>>(emptyMap())
    val liveLocations: StateFlow<Map<String, RealtimeLocation>> = _liveLocations.asStateFlow()

    // Keeps user's active selected language
    private val _activeLanguage = MutableStateFlow("en")
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    private val _outboundNotifications = MutableStateFlow<List<OutboundNotification>>(emptyList())
    val outboundNotifications: StateFlow<List<OutboundNotification>> = _outboundNotifications.asStateFlow()

    init {
        loadDataFromLocalCache()
    }

    // --- Core Authentication Simulations ---
    fun registerOrSignIn(phone: String, pinHash: String): Boolean {
        // Mock authentication check
        val existingPhone = prefs.getString("user_phone", "")
        val localPin = prefs.getString("user_pin_hash", "")

        if (existingPhone == phone) {
            if (localPin == pinHash) {
                // Return user success
                val user = UserProfile(
                    userId = "user_aegis_local",
                    fullName = prefs.getString("user_full_name", "") ?: "Premium Guardian",
                    phone = phone,
                    dateOfBirth = prefs.getString("user_dob", "") ?: "",
                    gender = prefs.getString("user_gender", "") ?: "",
                    city = prefs.getString("user_city", "") ?: "",
                    profilePhoto = prefs.getString("user_photo", "") ?: "",
                    linkedIn = prefs.getString("user_linkedin", "") ?: "",
                    github = prefs.getString("user_github", "") ?: "",
                    website = prefs.getString("user_website", "") ?: "",
                    role = prefs.getString("user_role", "") ?: "",
                    language = prefs.getString("user_language", "en") ?: "en",
                    profileComplete = prefs.getBoolean("user_complete", false)
                )
                _currentUser.value = user
                _activeLanguage.value = user.language
                return true
            }
            return false // PIN incorrect
        } else {
            // First time registration
            prefs.edit().apply {
                putString("user_phone", phone)
                putString("user_pin_hash", pinHash)
                putString("user_full_name", "")
                putBoolean("user_complete", false)
                apply()
            }
            val newUser = UserProfile(userId = "user_aegis_local", phone = phone, fcmToken = "token_local")
            _currentUser.value = newUser
            return true
        }
    }

    // Save Complete Profile Details
    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString("user_full_name", profile.fullName)
            putString("user_phone", profile.phone)
            putString("user_email", profile.email)
            putString("user_dob", profile.dateOfBirth)
            putString("user_gender", profile.gender)
            putString("user_city", profile.city)
            putString("user_photo", profile.profilePhoto)
            putString("user_linkedin", profile.linkedIn)
            putString("user_github", profile.github)
            putString("user_website", profile.website)
            putString("user_role", profile.role)
            putString("user_language", profile.language)
            putBoolean("user_complete", profile.profileComplete)
            apply()
        }
        _currentUser.value = profile
        _activeLanguage.value = profile.language
    }

    fun updateLanguage(langCode: String) {
        prefs.edit().putString("user_language", langCode).apply()
        _activeLanguage.value = langCode
        _currentUser.value = _currentUser.value?.copy(language = langCode)
    }

    fun signOut() {
        _currentUser.value = null
        // Clear active session flags, leaving registration info
    }

    // --- Child Tracker Management ---
    fun addChild(name: String, age: String, avatar: String, packageType: String) {
        val newChild = ChildTracker(
            childId = "child_" + UUID.randomUUID().toString().take(6),
            name = name,
            age = age,
            avatar = avatar,
            packageType = packageType,
            batteryLevel = (80..100).random(),
            isOnline = true,
            lastSeen = getFormattedTimeNow()
        )
        val updatedList = _children.value + newChild
        _children.value = updatedList
        saveChildrenToCache(updatedList)

        // Initialize child live location in Realtime Database simulator
        // Center on Ibadan initial coords with slight offset
        val offsetLat = (Math.random() - 0.5) * 0.005
        val offsetLon = (Math.random() - 0.5) * 0.005
        val location = RealtimeLocation(
            childId = newChild.childId,
            latitude = 7.3775 + offsetLat,
            longitude = 3.9470 + offsetLon,
            batteryLevel = newChild.batteryLevel
        )
        _liveLocations.value = _liveLocations.value + (newChild.childId to location)
    }

    // --- Geofence Management ---
    fun addGeofence(name: String, lat: Double, lon: Double, radius: Double, activeFrom: String, activeTo: String, activeDays: List<String>, childId: String) {
        val newFence = GeofenceRegion(
            geofenceId = "geo_" + UUID.randomUUID().toString().take(6),
            name = name,
            latitude = lat,
            longitude = lon,
            radius = radius,
            activeDays = activeDays,
            activeFrom = activeFrom,
            activeTo = activeTo,
            isActive = true,
            childId = childId
        )
        val updatedList = _geofences.value + newFence
        _geofences.value = updatedList
        saveGeofencesToCache(updatedList)
    }

    fun toggleGeofence(id: String) {
        val updatedList = _geofences.value.map {
            if (it.geofenceId == id) it.copy(isActive = !it.isActive) else it
        }
        _geofences.value = updatedList
        saveGeofencesToCache(updatedList)
    }

    fun deleteGeofence(id: String) {
        val updatedList = _geofences.value.filter { it.geofenceId != id }
        _geofences.value = updatedList
        saveGeofencesToCache(updatedList)
    }

    // --- Family Member Invitation ---
    fun addFamilyMember(name: String, phone: String, email: String, relationship: String, accessLevel: String) {
        val newMember = FamilyMember(
            memberId = "mem_" + UUID.randomUUID().toString().take(6),
            name = name,
            phone = phone,
            email = email,
            relationship = relationship,
            accessLevel = accessLevel,
            status = "pending" // Pending till they verify
        )
        val updatedList = _familyMembers.value + newMember
        _familyMembers.value = updatedList
        saveFamilyToCache(updatedList)

        // Simulate sending real SMS via Africa's Talking Fallback API
        simulateAfricasTalkingSMS(
            recipientPhone = phone,
            textMessage = "AEGIS ALERT: Hi $name, you are invited by ${_currentUser.value?.fullName ?: "your guardian"} to join their Aegis secure safety circle. Download app & sign up."
        )

        // Simulate sending real email invitation
        if (email.isNotBlank()) {
            simulateOutboundEmail(
                recipientEmail = email,
                subject = "Aegis Secure GPS Safety Circle Invitation",
                textMessage = "Hi $name,\n\nYou have been invited by ${_currentUser.value?.fullName ?: "your guardian"} to join their secure Aegis Safety Circle to monitor and safeguard your family.\n\nDownload the Aegis App and enter invite code '${newMember.memberId.uppercase(Locale.US)}' to secure your family grid.\n\nAegis Secure Engine"
            )
        }
    }

    fun removeFamilyMember(id: String) {
        val updatedList = _familyMembers.value.filter { it.memberId != id }
        _familyMembers.value = updatedList
        saveFamilyToCache(updatedList)
    }

    // --- Realtime Location Update & Haversine Geofencing Logic ---
    fun updateChildLocationForcefully(childId: String, lat: Double, lon: Double) {
        val location = RealtimeLocation(
            childId = childId,
            latitude = lat,
            longitude = lon,
            timestamp = System.currentTimeMillis(),
            batteryLevel = _children.value.find { it.childId == childId }?.batteryLevel ?: 90
        )
        _liveLocations.value = _liveLocations.value + (childId to location)

        // Run Geofencing logic evaluations
        evaluateGeofencesForChild(childId, lat, lon)
    }

    private fun evaluateGeofencesForChild(childId: String, currentLat: Double, currentLon: Double) {
        val child = _children.value.find { it.childId == childId } ?: return
        val activeFences = _geofences.value.filter { it.isActive && (it.childId == childId || it.childId.isEmpty()) }

        for (fence in activeFences) {
            val distance = calculateDistanceInMeters(currentLat, currentLon, fence.latitude, fence.longitude)
            Log.d("AegisGeofence", "Checking ${child.name} against ${fence.name}. Distance: $distance meters. Limit: ${fence.radius}")

            if (distance > fence.radius) {
                // Check active hours & active day constraints as requested
                if (isTimeWithinBounds(fence.activeFrom, fence.activeTo) && isTodayInDaysList(fence.activeDays)) {
                    // Trigger Alarm!
                    triggerBoundaryAlert(child, fence, currentLat, currentLon)
                }
            }
        }
    }

    private fun triggerBoundaryAlert(child: ChildTracker, fence: GeofenceRegion, lat: Double, lon: Double) {
        // Avoid duplicate active alarms
        val alreadyFired = _alerts.value.any { !it.isResolved && it.childId == child.childId && it.zoneName == fence.name }
        if (alreadyFired) return

        val alert = SafetyAlert(
            alertId = "alert_" + UUID.randomUUID().toString().take(6),
            type = "zone_exit",
            childId = child.childId,
            childName = child.name,
            childAvatar = child.avatar,
            zoneName = fence.name,
            latitude = lat,
            longitude = lon,
            isResolved = false,
            escalatedToAmber = false,
            timestamp = System.currentTimeMillis()
        )

        _alerts.value = listOf(alert) + _alerts.value // Display at top of list
        saveAlertsToCache(_alerts.value)

        // Send Push Warning & SMS Fallback to current user
        val mainParentPhone = _currentUser.value?.phone ?: "+2348000000000"
        val mainParentEmail = _currentUser.value?.email ?: ""
        simulateAfricasTalkingSMS(
            recipientPhone = mainParentPhone,
            textMessage = "AEGIS CRITICAL WARNING: ${child.name} has EXITED safety zone '${fence.name}'! Live monitor active."
        )
        if (mainParentEmail.isNotBlank()) {
            simulateOutboundEmail(
                recipientEmail = mainParentEmail,
                subject = "AEGIS ALERT: ${child.name} EXITED SAFE ZONE",
                textMessage = "Hi,\n\nThis is an urgent Aegis security dispatch. child ${child.name} has EXITED safety zone '${fence.name}'!\n\nLive tracking and geofencing sweep is currently active. Check the map position inside your Aegis app.\n\nCoordinates: Lat=$lat, Lon=$lon\n\nAegis Team"
            )
        }

        // Send to other full access family members
        _familyMembers.value.forEach { member ->
            simulateAfricasTalkingSMS(
                recipientPhone = member.phone,
                textMessage = "AEGIS CRITICAL: Pikin ${child.name} exit safe boundary '${fence.name}'. Tracking beacon has engaged."
            )
            if (member.email.isNotBlank()) {
                simulateOutboundEmail(
                    recipientEmail = member.email,
                    subject = "AEGIS ALERT: ${child.name} Exited Safe Zone",
                    textMessage = "Hi ${member.name},\n\nThis is an urgent Aegis safety circle warning. child ${child.name} has EXITED safety zone '${fence.name}'!\n\nLive tracking is currently active.\n\nCoordinates: Lat=$lat, Lon=$lon\n\nYour Safety, Our Guard,\nAegis secure engine"
                )
            }
        }
    }

    fun triggerSOSAlert(childId: String) {
        val child = _children.value.find { it.childId == childId } ?: return
        val liveLoc = _liveLocations.value[childId] ?: RealtimeLocation(childId = childId)

        val alert = SafetyAlert(
            alertId = "alert_" + UUID.randomUUID().toString().take(6),
            type = "sos",
            childId = child.childId,
            childName = child.name,
            childAvatar = child.avatar,
            zoneName = "EMERGENCY BEACON",
            latitude = liveLoc.latitude,
            longitude = liveLoc.longitude,
            isResolved = false,
            escalatedToAmber = false,
            timestamp = System.currentTimeMillis()
        )

        _alerts.value = listOf(alert) + _alerts.value
        saveAlertsToCache(_alerts.value)

        // Direct SMS & Email Fallback broadcasts to parent
        val mainPhone = _currentUser.value?.phone ?: "+2348000000000"
        val mainEmail = _currentUser.value?.email ?: ""
        simulateAfricasTalkingSMS(
            recipientPhone = mainPhone,
            textMessage = "AEGIS PANIC TRIGGERED: Child ${child.name} has initiated physical emergency beacon. Location: Ibadan Grid ${liveLoc.latitude}, ${liveLoc.longitude}."
        )
        if (mainEmail.isNotBlank()) {
            simulateOutboundEmail(
                recipientEmail = mainEmail,
                subject = "AEGIS PANIC: child ${child.name} Triggered SOS!",
                textMessage = "Hi,\n\nEMERGENCY EXTREME: child ${child.name} has triggered their Aegis panic SOS SOS device!\n\nLive coordinates are: Lat=${liveLoc.latitude}, Lon=${liveLoc.longitude}\n\nAct immediately!\nAegis Team"
            )
        }

        _familyMembers.value.forEach { member ->
            simulateAfricasTalkingSMS(
                recipientPhone = member.phone,
                textMessage = "AEGIS PANIC: Pikin ${child.name} press SOS! Verify position immediately."
            )
            if (member.email.isNotBlank()) {
                simulateOutboundEmail(
                    recipientEmail = member.email,
                    subject = "AEGIS CIRClE EMERGENCY: child ${child.name} SOS!",
                    textMessage = "Hi ${member.name},\n\nEMERGENCY: child ${child.name} has pressed their Aegis panic SOS button!\n\nVerify position immediately at: Lat=${liveLoc.latitude}, Lon=${liveLoc.longitude}\n\nAegis secure engine"
                )
            }
        }
    }

    fun resolveAlert(id: String) {
        _alerts.value = _alerts.value.map {
            if (it.alertId == id) it.copy(isResolved = true) else it
        }
        saveAlertsToCache(_alerts.value)
    }

    fun escalateToAmber(id: String) {
        _alerts.value = _alerts.value.map {
            if (it.alertId == id) it.copy(escalatedToAmber = true) else it
        }
        saveAlertsToCache(_alerts.value)

        val alert = _alerts.value.find { it.alertId == id } ?: return
        
        val mainPhone = _currentUser.value?.phone ?: "+2348000000000"
        val mainEmail = _currentUser.value?.email ?: ""
        
        // Send nationwide broadcast SMS warning
        simulateAfricasTalkingSMS(
            recipientPhone = mainPhone,
            textMessage = "AEGIS AMBER EXTREME: Amber Alert active for child ${alert.childName}. Local sector community network notified."
        )
        if (mainEmail.isNotBlank()) {
            simulateOutboundEmail(
                recipientEmail = mainEmail,
                subject = "AEGIS AMBER EXTREME DISPATCH: ${alert.childName}",
                textMessage = "Hi,\n\nAMBER ALERT DISPATCH ACTIVATED FOR ${alert.childName}!\n\nCommunity sector nodes and local security responders have been pinged.\n\nKeep lines open,\nAegis Team"
            )
        }
    }

    // --- Simulated Fallback SMS Gateway via Africa's Talking API ---
    private fun simulateAfricasTalkingSMS(recipientPhone: String, textMessage: String) {
        Log.i("AfricaTalkingSMS", "=== OUTBOUND SMS DISPATCH GATEWAY ===")
        Log.i("AfricaTalkingSMS", "To: $recipientPhone")
        Log.i("AfricaTalkingSMS", "Content: $textMessage")
        Log.i("AfricaTalkingSMS", "Routing fallback via low-connectivity GSM cellular cell grid")
        Log.i("AfricaTalkingSMS", "========================================")
        recordNotification(recipientPhone, "SMS", textMessage)
    }

    fun simulateOutboundEmail(recipientEmail: String, subject: String, textMessage: String) {
        Log.i("AegisEmailEngine", "=== OUTBOUND EMAIL DISPATCH GATEWAY ===")
        Log.i("AegisEmailEngine", "To: $recipientEmail")
        Log.i("AegisEmailEngine", "Subject: $subject")
        Log.i("AegisEmailEngine", "Content: $textMessage")
        Log.i("AegisEmailEngine", "========================================")
        recordNotification(recipientEmail, "EMAIL", "Subject: $subject\n\n$textMessage")
    }

    private fun recordNotification(recipient: String, channel: String, message: String) {
        val notif = OutboundNotification(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            recipient = recipient,
            channel = channel,
            content = message,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )
        val updated = listOf(notif) + _outboundNotifications.value
        _outboundNotifications.value = updated
        saveOutboundNotificationsToCache(updated)
    }

    private fun saveOutboundNotificationsToCache(list: List<OutboundNotification>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("recipient", item.recipient)
                    put("channel", item.channel)
                    put("content", item.content)
                    put("timestamp", item.timestamp)
                    put("status", item.status)
                }
                array.put(obj)
            }
            prefs.edit().putString("caches_outbound", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("AegisCache", "Failed to cache outbound notifications: ${e.message}")
        }
    }

    // --- Helper Math: Haversine distance formula ---
    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun isTimeWithinBounds(activeFrom: String, activeTo: String): Boolean {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val cal = Calendar.getInstance()
            val nowTimeStr = "${cal.get(Calendar.HOUR_OF_DAY)}:${cal.get(Calendar.MINUTE)}"
            val now = sdf.parse(nowTimeStr)
            val from = sdf.parse(activeFrom)
            val to = sdf.parse(activeTo)

            if (now != null && from != null && to != null) {
                return if (to.before(from)) {
                    // Over midnight span
                    now.after(from) || now.before(to)
                } else {
                    now.after(from) && now.before(to)
                }
            }
        } catch (e: Exception) {
            Log.e("AegisTime", "Error parsing time limits", e)
        }
        return true // default true on parse errors for protection
    }

    private fun isTodayInDaysList(days: List<String>): Boolean {
        val cal = Calendar.getInstance()
        val todayStr = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Mon"
        }
        return days.contains(todayStr)
    }

    private fun getFormattedTimeNow(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(System.currentTimeMillis())
    }

    // --- SharedPreferences Cache Store/Load Methods (Local AsyncStorage replacement) ---
    private fun loadDataFromLocalCache() {
        try {
            // Load custom active language
            _activeLanguage.value = prefs.getString("user_language", "en") ?: "en"

            // Load registered child models
            val childRaw = prefs.getString("caches_children", "[]") ?: "[]"
            val childArray = JSONArray(childRaw)
            val childList = mutableListOf<ChildTracker>()
            for (i in 0 until childArray.length()) {
                val obj = childArray.getJSONObject(i)
                childList.add(ChildTracker(
                    childId = obj.getString("childId"),
                    name = obj.getString("name"),
                    age = obj.getString("age"),
                    avatar = obj.getString("avatar"),
                    packageType = obj.getString("packageType"),
                    batteryLevel = obj.getInt("batteryLevel"),
                    isOnline = obj.getBoolean("isOnline"),
                    lastSeen = obj.getString("lastSeen")
                ))
            }
            _children.value = childList

            // Load geofences
            val geoRaw = prefs.getString("caches_geofences", "[]") ?: "[]"
            val geoArray = JSONArray(geoRaw)
            val geoList = mutableListOf<GeofenceRegion>()
            for (i in 0 until geoArray.length()) {
                val obj = geoArray.getJSONObject(i)
                val daysArr = obj.getJSONArray("activeDays")
                val daysList = mutableListOf<String>()
                for (j in 0 until daysArr.length()) {
                    daysList.add(daysArr.getString(j))
                }
                geoList.add(GeofenceRegion(
                    geofenceId = obj.getString("geofenceId"),
                    name = obj.getString("name"),
                    latitude = obj.getDouble("latitude"),
                    longitude = obj.getDouble("longitude"),
                    radius = obj.getDouble("radius"),
                    activeDays = daysList,
                    activeFrom = obj.getString("activeFrom"),
                    activeTo = obj.getString("activeTo"),
                    isActive = obj.getBoolean("isActive"),
                    childId = obj.getString("childId")
                ))
            }
            _geofences.value = geoList

            // Load profile session
            val cachedPhone = prefs.getString("user_phone", "")
            if (!cachedPhone.isNullOrEmpty()) {
                _currentUser.value = UserProfile(
                    userId = "user_aegis_local",
                    fullName = prefs.getString("user_full_name", "") ?: "",
                    phone = cachedPhone,
                    email = prefs.getString("user_email", "") ?: "",
                    dateOfBirth = prefs.getString("user_dob", "") ?: "",
                    gender = prefs.getString("user_gender", "") ?: "",
                    city = prefs.getString("user_city", "") ?: "",
                    profilePhoto = prefs.getString("user_photo", "") ?: "",
                    linkedIn = prefs.getString("user_linkedin", "") ?: "",
                    github = prefs.getString("user_github", "") ?: "",
                    website = prefs.getString("user_website", "") ?: "",
                    role = prefs.getString("user_role", "") ?: "",
                    language = _activeLanguage.value,
                    profileComplete = prefs.getBoolean("user_complete", false)
                )
            }

            // Load family circle
            val famRaw = prefs.getString("caches_family", "[]") ?: "[]"
            val famArray = JSONArray(famRaw)
            val famList = mutableListOf<FamilyMember>()
            for (i in 0 until famArray.length()) {
                val obj = famArray.getJSONObject(i)
                famList.add(FamilyMember(
                    memberId = obj.getString("memberId"),
                    name = obj.getString("name"),
                    phone = obj.getString("phone"),
                    email = obj.optString("email", ""),
                    relationship = obj.getString("relationship"),
                    accessLevel = obj.getString("accessLevel"),
                    status = obj.getString("status")
                ))
            }
            _familyMembers.value = famList

            // Load outbound notifications logs
            val outRaw = prefs.getString("caches_outbound", "[]") ?: "[]"
            val outArray = JSONArray(outRaw)
            val outList = mutableListOf<OutboundNotification>()
            for (i in 0 until outArray.length()) {
                val obj = outArray.getJSONObject(i)
                outList.add(OutboundNotification(
                    id = obj.getString("id"),
                    recipient = obj.getString("recipient"),
                    channel = obj.getString("channel"),
                    content = obj.getString("content"),
                    timestamp = obj.getLong("timestamp"),
                    status = obj.getString("status")
                ))
            }
            _outboundNotifications.value = outList

            // Load alerts
            val alertsRaw = prefs.getString("caches_alerts", "[]") ?: "[]"
            val alertsArray = JSONArray(alertsRaw)
            val alertsList = mutableListOf<SafetyAlert>()
            for (i in 0 until alertsArray.length()) {
                val obj = alertsArray.getJSONObject(i)
                alertsList.add(SafetyAlert(
                    alertId = obj.getString("alertId"),
                    type = obj.getString("type"),
                    childId = obj.getString("childId"),
                    childName = obj.getString("childName"),
                    childAvatar = obj.getString("childAvatar"),
                    zoneName = obj.getString("zoneName"),
                    latitude = obj.getDouble("latitude"),
                    longitude = obj.getDouble("longitude"),
                    isResolved = obj.getBoolean("isResolved"),
                    escalatedToAmber = obj.getBoolean("escalatedToAmber"),
                    timestamp = obj.getLong("timestamp")
                ))
            }
            _alerts.value = alertsList

            // Setup default live coordinates
            val liveMap = mutableMapOf<String, RealtimeLocation>()
            for (child in childList) {
                // Spawn within Ibadan grid
                liveMap[child.childId] = RealtimeLocation(
                    childId = child.childId,
                    latitude = 7.3775 + (Math.random() - 0.5) * 0.008,
                    longitude = 3.9470 + (Math.random() - 0.5) * 0.008,
                    batteryLevel = child.batteryLevel
                )
            }
            _liveLocations.value = liveMap

        } catch (e: Exception) {
            Log.e("AegisCache", "Failure reading local cache configs", e)
        }
    }

    private fun saveChildrenToCache(list: List<ChildTracker>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("childId", item.childId)
                put("name", item.name)
                put("age", item.age)
                put("avatar", item.avatar)
                put("packageType", item.packageType)
                put("batteryLevel", item.batteryLevel)
                put("isOnline", item.isOnline)
                put("lastSeen", item.lastSeen)
            }
            array.put(obj)
        }
        prefs.edit().putString("caches_children", array.toString()).apply()
    }

    private fun saveGeofencesToCache(list: List<GeofenceRegion>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("geofenceId", item.geofenceId)
                put("name", item.name)
                put("latitude", item.latitude)
                put("longitude", item.longitude)
                put("radius", item.radius)
                put("activeFrom", item.activeFrom)
                put("activeTo", item.activeTo)
                put("isActive", item.isActive)
                put("childId", item.childId)

                val daysArr = JSONArray()
                item.activeDays.forEach { daysArr.put(it) }
                put("activeDays", daysArr)
            }
            array.put(obj)
        }
        prefs.edit().putString("caches_geofences", array.toString()).apply()
    }

    private fun saveFamilyToCache(list: List<FamilyMember>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("memberId", item.memberId)
                put("name", item.name)
                put("phone", item.phone)
                put("email", item.email)
                put("relationship", item.relationship)
                put("accessLevel", item.accessLevel)
                put("status", item.status)
            }
            array.put(obj)
        }
        prefs.edit().putString("caches_family", array.toString()).apply()
    }

    private fun saveAlertsToCache(list: List<SafetyAlert>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("alertId", item.alertId)
                put("type", item.type)
                put("childId", item.childId)
                put("childName", item.childName)
                put("childAvatar", item.childAvatar)
                put("zoneName", item.zoneName)
                put("latitude", item.latitude)
                put("longitude", item.longitude)
                put("isResolved", item.isResolved)
                put("escalatedToAmber", item.escalatedToAmber)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("caches_alerts", array.toString()).apply()
    }
}
