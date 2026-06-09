package com.example.data

data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val city: String = "",
    val profilePhoto: String = "", // Holds local file URI or avatar selection ID
    val linkedIn: String = "",
    val github: String = "",
    val website: String = "",
    val role: String = "",
    val language: String = "en",
    val fcmToken: String = "token_fcm_aegis_local",
    val profileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChildTracker(
    val childId: String = "",
    val name: String = "",
    val age: String = "",
    val avatar: String = "👦", // Emoji characters
    val packageType: String = "", // Aegis Watch, Aegis Patch, Aegis Complete
    val batteryLevel: Int = 88,
    val isOnline: Boolean = true,
    val lastSeen: String = "" // formatted timestamp
)

data class GeofenceRegion(
    val geofenceId: String = "",
    val name: String = "",
    val latitude: Double = 7.3775, // Default Ibadan Center
    val longitude: Double = 3.9470,
    val radius: Double = 300.0, // in meters
    val activeDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val activeFrom: String = "08:00",
    val activeTo: String = "17:00",
    val isActive: Boolean = true,
    val childId: String = ""
)

data class FamilyMember(
    val memberId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val relationship: String = "", // Mother, Father, Aunt, etc.
    val accessLevel: String = "View Only", // View Only, Full Access
    val status: String = "active" // active, pending
)

data class SafetyAlert(
    val alertId: String = "",
    val type: String = "zone_exit", // zone_exit, sos
    val childId: String = "",
    val childName: String = "",
    val childAvatar: String = "👦",
    val zoneName: String = "",
    val latitude: Double = 7.3775,
    val longitude: Double = 3.9470,
    val isResolved: Boolean = false,
    val escalatedToAmber: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class RealtimeLocation(
    val childId: String = "",
    val latitude: Double = 7.3775,
    val longitude: Double = 3.9470,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int = 88
)

data class OutboundNotification(
    val id: String = "",
    val recipient: String = "", // Phone or Email address
    val channel: String = "",   // "SMS" or "EMAIL"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT" // "SENT", "DELIVERED", "QUEUED"
)
