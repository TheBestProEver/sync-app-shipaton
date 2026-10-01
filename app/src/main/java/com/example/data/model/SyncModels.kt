package com.example.data.model

enum class SpaceType {
    COMMUNITY,
    WORKPLACE
}

enum class StatusFamily {
    OPEN,       // Lit amber window, counts as free now
    FOCUSED,    // Blinds drawn, not free
    BUSY,       // Dark wall, not free
    AWAY,       // Unlit dusk frame
    GHOST,      // Dashed dusk frame
    IN_OFFICE   // 12% moonlight, office day views
}

data class WindowTone(
    val name: String,
    val hex: String
)

data class UserProfile(
    val id: String = "user_arnav",
    val displayName: String = "Arnav",
    val email: String = "arnav@uchicago.edu",
    val windowTone: String = "Rose",
    val birthYear: Int = 2007,
    val timezone: String = "America/Chicago",
    val workingHours: String = "9:00 to 5:30",
    val verifiedDomain: String = "uchicago.edu",
    val isPro: Boolean = false,
    val proUntil: Long? = null,
    val ghostUntil: Long? = null,
    val quietHoursStart: String = "23:00",
    val quietHoursEnd: String = "08:00"
)

data class SyncSpace(
    val id: String,
    val type: SpaceType,
    val name: String,
    val city: String? = null,
    val emailDomain: String? = null,
    val statusWordsTemplate: String = "Campus",
    val places: List<String> = emptyList(),
    // Current user's status in this space
    val currentStatusLabel: String = "",
    val currentStatusFamily: StatusFamily = StatusFamily.AWAY,
    val currentStatusPlace: String = "",
    val currentStatusExpiresAt: Long = 0L,
    val shareLevel: String = "Titles", // Titles, Busy, None
    // Workplace attributes
    val seatLimit: Int = 100,
    val seatsUsed: Int = 100,
    val planTier: String = "Business 100",
    val planRenewsDate: String = "Sep 29, 2027",
    val isFocusHoursActive: Boolean = false,
    val focusHoursBannerText: String = "7 in focus until 11:00",
    val isMixerEnabled: Boolean = true,
    val inviteCode: String = "ENG101"
)

data class CircleMember(
    val id: String,
    val name: String,
    val initials: String,
    val tone: String,
    val family: StatusFamily,
    val statusLabel: String,
    val place: String = "",
    val expiresAt: Long = 0L,
    val sharedCircles: List<String> = emptyList(),
    val department: String? = null,
    val timezone: String = "America/Chicago",
    val localTimeStr: String = ""
) {
    fun computedLocalTime(): String {
        return try {
            val zoneId = java.time.ZoneId.of(timezone)
            val zonedDateTime = java.time.Instant.now().atZone(zoneId)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
            zonedDateTime.format(formatter).lowercase()
        } catch (e: Exception) {
            localTimeStr.ifBlank { "" }
        }
    }
}

data class SyncCircle(
    val id: String,
    val spaceId: String,
    val name: String,
    val kind: String, // squad, club, course, house, department, project
    val memberCount: Int,
    val freeCount: Int,
    val isOwner: Boolean = false,
    val visibility: String = "private", // private, public
    val category: String? = null,
    val joinCode: String = "QK7M9P",
    val ritualText: String? = null,
    val members: List<CircleMember> = emptyList()
)

data class SyncPulse(
    val id: String,
    val spaceId: String,
    val circleId: String,
    val circleName: String,
    val creatorId: String,
    val creatorName: String,
    val title: String,
    val place: String,
    val startsAt: Long,
    val durationMin: Int = 60,
    val quorum: Int = 3,
    val cap: Int? = null,
    val decideBy: Long = 0L,
    val state: String = "OPEN", // OPEN, CONFIRMED, FULL, CANCELLED, EXPIRED
    val isUserIn: Boolean = false,
    val isUserCant: Boolean = false,
    val membersIn: List<CircleMember> = emptyList(),
    val waitlist: List<CircleMember> = emptyList(),
    val note: String? = null,
    val isRitual: Boolean = false
)

data class ScheduleBlock(
    val id: String,
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int,
    val title: String,
    val kind: String // class, work, practice, meeting, other
)

data class OfficeDayPlan(
    val dayOfWeek: Int, // 1 = Mon .. 5 = Fri
    val weekIndex: Int, // 0 = This week, 1 = Next week
    val status: String // IN_OFFICE, REMOTE, NOT_PLANNED
)

data class DiscoverCircle(
    val id: String,
    val name: String,
    val category: String,
    val city: String,
    val neighborhood: String,
    val memberCount: Int,
    val ritual: String?,
    val isRequested: Boolean = false
)

data class MixerMatch(
    val id: String,
    val title: String,
    val slot: String,
    val members: List<String>,
    val isUserIn: Boolean = false,
    val isUserCant: Boolean = false
)

data class WorkplaceInsight(
    val dayOfWeek: String,
    val attendancePercent: Int
)
