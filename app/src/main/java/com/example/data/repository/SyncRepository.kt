package com.example.data.repository

import android.content.Context
import com.example.data.local.CalendarSlotDao
import com.example.data.local.CalendarSlotEntity
import com.example.data.local.SyncDatabase
import com.example.data.model.*
import com.example.service.CalendarSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SyncRepository(
    context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val db: SyncDatabase? = context?.let { SyncDatabase.getInstance(it) }
    private val calendarDao: CalendarSlotDao? = db?.calendarSlotDao()
    val calendarSyncService: CalendarSyncService? = calendarDao?.let { CalendarSyncService(it) }

    val cachedCalendarSlots: Flow<List<CalendarSlotEntity>> =
        calendarDao?.getAllSlots() ?: emptyFlow()

    private val _calendarSyncStatus = MutableStateFlow("Ready to sync device calendar")
    val calendarSyncStatus: StateFlow<String> = _calendarSyncStatus.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "user_arnav",
            displayName = "Arnav",
            windowTone = "Rose",
            birthYear = 2007,
            timezone = "America/Chicago",
            workingHours = "9:00 to 5:30",
            verifiedDomain = "uchicago.edu",
            isPro = false,
            proUntil = null
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _spaces = MutableStateFlow<List<SyncSpace>>(emptyList())
    val spaces: StateFlow<List<SyncSpace>> = _spaces.asStateFlow()

    private val _currentSpaceId = MutableStateFlow("uchicago")
    val currentSpaceId: StateFlow<String> = _currentSpaceId.asStateFlow()

    private val _circles = MutableStateFlow<List<SyncCircle>>(emptyList())
    val circles: StateFlow<List<SyncCircle>> = _circles.asStateFlow()

    private val _pulses = MutableStateFlow<List<SyncPulse>>(emptyList())
    val pulses: StateFlow<List<SyncPulse>> = _pulses.asStateFlow()

    private val _scheduleBlocks = MutableStateFlow<List<ScheduleBlock>>(emptyList())
    val scheduleBlocks: StateFlow<List<ScheduleBlock>> = _scheduleBlocks.asStateFlow()

    private val _officeDays = MutableStateFlow<List<OfficeDayPlan>>(emptyList())
    val officeDays: StateFlow<List<OfficeDayPlan>> = _officeDays.asStateFlow()

    private val _discoverCircles = MutableStateFlow<List<DiscoverCircle>>(emptyList())
    val discoverCircles: StateFlow<List<DiscoverCircle>> = _discoverCircles.asStateFlow()

    private val _mixerMatch = MutableStateFlow<MixerMatch?>(null)
    val mixerMatch: StateFlow<MixerMatch?> = _mixerMatch.asStateFlow()

    init {
        loadSeedData()
        if (context != null) {
            // Auto sync sample calendar slots initially into Room
            scope.launch {
                calendarSyncService?.syncCalendarEvents(context)
            }
        }
    }

    private fun loadSeedData() {
        val nowMs = System.currentTimeMillis()
        val oneHourLater = nowMs + 3600 * 1000
        val twoHoursLater = nowMs + 7200 * 1000

        // 1. Initial Spaces (Community vs Workplace)
        _spaces.value = listOf(
            SyncSpace(
                id = "uchicago",
                type = SpaceType.COMMUNITY,
                name = "UChicago",
                city = "Chicago",
                emailDomain = "uchicago.edu",
                statusWordsTemplate = "Campus",
                places = listOf(
                    "Joseph Regenstein Library (the Reg)",
                    "Joe and Rika Mansueto Library",
                    "Harper Memorial Library",
                    "Bartlett dining commons",
                    "Cathey dining commons",
                    "Woodlawn dining commons",
                    "Hallowed Grounds in Reynolds Club",
                    "Ex Libris café in the Reg",
                    "Reynolds Club",
                    "Ratner Athletics Center",
                    "Promontory Point",
                    "Max Palevsky"
                ),
                currentStatusLabel = "Free for food",
                currentStatusFamily = StatusFamily.OPEN,
                currentStatusPlace = "Bartlett",
                currentStatusExpiresAt = oneHourLater + 2520000L, // ~1h 42m
                shareLevel = "Titles"
            ),
            SyncSpace(
                id = "hyde_park_runners",
                type = SpaceType.COMMUNITY,
                name = "Hyde Park Runners",
                city = "Chicago",
                statusWordsTemplate = "Club",
                places = listOf(
                    "Promontory Point",
                    "Lakefront Trail",
                    "Woodlawn & 57th",
                    "Jackson Park"
                ),
                currentStatusLabel = "Free after 6",
                currentStatusFamily = StatusFamily.OPEN,
                currentStatusPlace = "Promontory Point",
                currentStatusExpiresAt = nowMs + 18000 * 1000,
                shareLevel = "Titles"
            ),
            SyncSpace(
                id = "kestrel_labs",
                type = SpaceType.WORKPLACE,
                name = "Kestrel Labs",
                city = "Chicago",
                emailDomain = "kestrellabs.com",
                statusWordsTemplate = "Workplace",
                places = listOf(
                    "Building 2, 4th Floor",
                    "Main Cafeteria",
                    "Design Studio",
                    "Platform Pod"
                ),
                currentStatusLabel = "Heads down",
                currentStatusFamily = StatusFamily.FOCUSED,
                currentStatusPlace = "Platform Pod",
                currentStatusExpiresAt = nowMs + 3600 * 1000,
                shareLevel = "Titles",
                seatLimit = 100,
                seatsUsed = 10,
                planTier = "Business 100",
                planRenewsDate = "Sep 29, 2027",
                isFocusHoursActive = true,
                focusHoursBannerText = "7 in focus until 11:00",
                isMixerEnabled = true,
                inviteCode = "ENG101"
            )
        )

        // 2. Members for circles
        val memberMaya = CircleMember("m1", "Maya Chen", "MC", "Rose", StatusFamily.OPEN, "Free for food", "Bartlett", oneHourLater, listOf("Physics '29", "UC Quantum"), timezone = "America/Chicago")
        val memberTomas = CircleMember("m2", "Tomás Rivera", "TR", "Sage", StatusFamily.OPEN, "Free for coffee", "Ex Libris", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberKai = CircleMember("m3", "Kai Patel", "KP", "Sky", StatusFamily.OPEN, "Free now", "Reg 3rd floor", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberLiam = CircleMember("m4", "Liam O'Connor", "LO", "Teal", StatusFamily.OPEN, "Up for lunch", "Bartlett", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberJordan = CircleMember("m5", "Jordan Smith", "JS", "Moss", StatusFamily.OPEN, "Free until 2:00", "Mansueto", twoHoursLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberElena = CircleMember("m6", "Elena Rostova", "ER", "Lavender", StatusFamily.FOCUSED, "Studying alone", "The Reg", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberAlex = CircleMember("m7", "Alex Reed", "AR", "Stone", StatusFamily.FOCUSED, "Recharging", "Dorm", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberSam = CircleMember("m8", "Sam Wilson", "SW", "Coral", StatusFamily.BUSY, "In class until 1:15", "Kersten", oneHourLater, listOf("Physics '29"), timezone = "America/Chicago")
        val memberChris = CircleMember("m9", "Chris Vance", "CV", "Sky", StatusFamily.AWAY, "Out", "", 0L, listOf("Physics '29"), timezone = "America/Chicago")

        // Workplace Platform Team members with dynamic timezones (computes real local times)
        val workDavid = CircleMember("w1", "David Kim", "DK", "Sky", StatusFamily.FOCUSED, "Heads down until 11", "Desk 4A", oneHourLater, listOf("Platform"), "Platform", timezone = "America/Chicago")
        val workLena = CircleMember("w2", "Lena Vance", "LV", "Teal", StatusFamily.OPEN, "Open to chat", "Design Lounge", oneHourLater, listOf("Design"), "Design", timezone = "America/Chicago")
        val workOmar = CircleMember("w3", "Omar Haddad", "OH", "Lavender", StatusFamily.OPEN, "Free for coffee", "Café 2", oneHourLater, listOf("Sales"), "Sales", timezone = "America/Chicago")
        val workPriya = CircleMember("w4", "Priya Sharma", "PS", "Rose", StatusFamily.FOCUSED, "Heads down until 11", "Desk 4B", oneHourLater, listOf("Platform"), "Platform", timezone = "America/Chicago")
        val workMarcus = CircleMember("w5", "Marcus Brody", "MB", "Sage", StatusFamily.BUSY, "In a meeting until 10:30", "Room 401", oneHourLater, listOf("Platform"), "Platform", timezone = "America/Chicago")
        val workZoe = CircleMember("w6", "Zoe Alverez", "ZA", "Moss", StatusFamily.FOCUSED, "Heads down until 11", "Remote", oneHourLater, listOf("Platform"), "Platform", timezone = "America/New_York")
        val workBrian = CircleMember("w7", "Brian Hughes", "BH", "Stone", StatusFamily.FOCUSED, "Heads down until 11", "Desk 4C", oneHourLater, listOf("Platform"), "Platform", timezone = "America/Chicago")
        val workAisha = CircleMember("w8", "Aisha Khan", "AK", "Coral", StatusFamily.OPEN, "Free for lunch", "Cafeteria", twoHoursLater, listOf("Platform"), "Platform", timezone = "Europe/London")
        val workLeo = CircleMember("w9", "Leo Vance", "LV", "Sky", StatusFamily.BUSY, "On a call", "Phone Booth", oneHourLater, listOf("Platform"), "Platform", timezone = "America/Denver")

        // Hyde Park Runners (12 members initially)
        val runnerMembers = (1..12).map { index ->
            val tones = listOf("Sky", "Sage", "Rose", "Lavender", "Teal", "Moss", "Stone", "Coral")
            CircleMember(
                id = "runner_$index",
                name = listOf("Priya", "Nate", "Claire", "Amir", "Danielle", "Dev", "Hannah", "Julian", "Sara", "Tom", "Mina", "Lucas")[index - 1],
                initials = "R$index",
                tone = tones[index % tones.size],
                family = if (index <= 5) StatusFamily.OPEN else StatusFamily.BUSY,
                statusLabel = if (index <= 5) "Up for a run" else "At practice",
                place = "Promontory Point"
            )
        }

        // 3. Initial Circles
        _circles.value = listOf(
            SyncCircle(
                id = "physics_29",
                spaceId = "uchicago",
                name = "Physics '29",
                kind = "squad",
                memberCount = 9,
                freeCount = 5,
                isOwner = false,
                visibility = "private",
                joinCode = "PX29Q8",
                members = listOf(memberMaya, memberTomas, memberKai, memberLiam, memberJordan, memberElena, memberAlex, memberSam, memberChris)
            ),
            SyncCircle(
                id = "uc_quantum",
                spaceId = "uchicago",
                name = "UC Quantum",
                kind = "club",
                memberCount = 8,
                freeCount = 1,
                isOwner = false,
                visibility = "private",
                joinCode = "QNT841",
                members = listOf(
                    CircleMember("q1", "Sophia Li", "SL", "Teal", StatusFamily.OPEN, "Lab break", "ERC"),
                    CircleMember("q2", "Wei Zhang", "WZ", "Sky", StatusFamily.FOCUSED, "Studying alone", "Reg"),
                    CircleMember("q3", "Dev Anand", "DA", "Sage", StatusFamily.BUSY, "In lecture", "Kersten"),
                    CircleMember("q4", "Maya Chen", "MC", "Rose", StatusFamily.OPEN, "Free for food", "Bartlett")
                )
            ),
            SyncCircle(
                id = "max_p_house",
                spaceId = "uchicago",
                name = "Max P house",
                kind = "house",
                memberCount = 6,
                freeCount = 3,
                isOwner = false,
                visibility = "private",
                joinCode = "MXP339",
                members = listOf(
                    CircleMember("h1", "Chloe Ross", "CR", "Lavender", StatusFamily.OPEN, "Free for lunch", "Bartlett"),
                    CircleMember("h2", "Noah Green", "NG", "Sky", StatusFamily.OPEN, "Lounge chill", "Max P"),
                    CircleMember("h3", "Ben Cooper", "BC", "Sage", StatusFamily.OPEN, "Break", "Quad"),
                    CircleMember("h4", "Sarah Bell", "SB", "Rose", StatusFamily.BUSY, "In class", "Cobb")
                )
            ),
            SyncCircle(
                id = "dawn_crew",
                spaceId = "hyde_park_runners",
                name = "Dawn crew",
                kind = "club",
                memberCount = 12,
                freeCount = 5,
                isOwner = true,
                visibility = "public",
                category = "Running",
                joinCode = "RUN6AM",
                ritualText = "Usually Thursdays at 7",
                members = runnerMembers
            ),
            SyncCircle(
                id = "platform_team",
                spaceId = "kestrel_labs",
                name = "Platform",
                kind = "department",
                memberCount = 9,
                freeCount = 3,
                isOwner = true,
                visibility = "private",
                joinCode = "PLT991",
                members = listOf(workDavid, workLena, workOmar, workPriya, workMarcus, workZoe, workBrian, workAisha, workLeo)
            ),
            SyncCircle(
                id = "eng_team",
                spaceId = "kestrel_labs",
                name = "Engineering",
                kind = "department",
                memberCount = 45,
                freeCount = 14,
                isOwner = false,
                visibility = "private",
                joinCode = "ENG101",
                members = listOf(workDavid, workPriya, workBrian, workZoe)
            )
        )

        // 4. Initial Pulses
        _pulses.value = listOf(
            SyncPulse(
                id = "pulse_lunch_bartlett",
                spaceId = "uchicago",
                circleId = "physics_29",
                circleName = "Physics '29",
                creatorId = "m1",
                creatorName = "Maya Chen",
                title = "Lunch at 12:30",
                place = "Bartlett",
                startsAt = nowMs + 1800 * 1000,
                durationMin = 60,
                quorum = 4,
                cap = 6,
                decideBy = nowMs + 1200 * 1000,
                state = "OPEN",
                isUserIn = true,
                membersIn = listOf(memberMaya, memberLiam, memberTomas),
                waitlist = emptyList()
            ),
            SyncPulse(
                id = "pulse_quantum_study",
                spaceId = "uchicago",
                circleId = "uc_quantum",
                circleName = "UC Quantum",
                creatorId = "q1",
                creatorName = "Sophia Li",
                title = "Problem Set Sprint at 4:00",
                place = "Reg 3rd floor",
                startsAt = nowMs + 7200 * 1000,
                durationMin = 90,
                quorum = 3,
                cap = null,
                decideBy = nowMs + 5400 * 1000,
                state = "OPEN",
                isUserIn = false,
                membersIn = listOf(CircleMember("q1", "Sophia Li", "SL", "Teal", StatusFamily.OPEN, "Lab break", "ERC"))
            )
        )

        // 5. Initial Schedule Blocks
        _scheduleBlocks.value = listOf(
            ScheduleBlock("sb1", 1, 9, 30, 11, 20, "PHYS 22100 Physics II", "class"),
            ScheduleBlock("sb2", 1, 13, 30, 15, 0, "MATH 20300 Linear Algebra", "class"),
            ScheduleBlock("sb3", 2, 10, 0, 11, 30, "CMSC 15200 Intro to CS", "class"),
            ScheduleBlock("sb4", 2, 14, 0, 17, 0, "Quantum Computing Lab", "practice"),
            ScheduleBlock("sb5", 3, 9, 30, 11, 20, "PHYS 22100 Physics II", "class"),
            ScheduleBlock("sb6", 3, 13, 30, 15, 0, "MATH 20300 Linear Algebra", "class"),
            ScheduleBlock("sb7", 4, 10, 0, 11, 30, "CMSC 15200 Intro to CS", "class"),
            ScheduleBlock("sb8", 5, 9, 30, 11, 20, "PHYS 22100 Physics II", "class"),
            ScheduleBlock("sb9", 5, 14, 0, 16, 0, "Study Session at Reg", "other")
        )

        // 6. Office Days
        _officeDays.value = listOf(
            OfficeDayPlan(1, 0, "REMOTE"),
            OfficeDayPlan(2, 0, "IN_OFFICE"),
            OfficeDayPlan(3, 0, "IN_OFFICE"),
            OfficeDayPlan(4, 0, "IN_OFFICE"),
            OfficeDayPlan(5, 0, "REMOTE"),
            OfficeDayPlan(1, 1, "NOT_PLANNED"),
            OfficeDayPlan(2, 1, "NOT_PLANNED"),
            OfficeDayPlan(3, 1, "NOT_PLANNED"),
            OfficeDayPlan(4, 1, "NOT_PLANNED"),
            OfficeDayPlan(5, 1, "NOT_PLANNED")
        )

        // 7. Discover Circles
        _discoverCircles.value = listOf(
            DiscoverCircle("d1", "Hyde Park Runners", "Running", "Chicago", "Promontory Point", 12, "Usually Thursdays at 7"),
            DiscoverCircle("d2", "Lisbon Polyglots", "Language exchange", "Lisbon", "Bairro Alto", 34, "Tuesdays at 6:30"),
            DiscoverCircle("d3", "Mumbai Sunday Cyclists", "Cycling", "Mumbai", "Marine Drive", 52, "Sundays at 6:00 am"),
            DiscoverCircle("d4", "London Mile End Climbing", "Climbing", "London", "Mile End", 29, "Wednesdays at 7:00"),
            DiscoverCircle("d5", "Chicago Tech Founders Coffee", "New in town", "Chicago", "Fulton Market", 19, "Fridays at 8:30 am")
        )

        // 8. Mixer Match
        _mixerMatch.value = MixerMatch(
            id = "mixer_w1",
            title = "Coffee with Lena (Design) and Omar (Sales)",
            slot = "Thu 2:30",
            members = listOf("Lena Vance", "Omar Haddad", "Arnav"),
            isUserIn = false
        )
    }

    // Space switching
    fun selectSpace(spaceId: String) {
        _currentSpaceId.value = spaceId
    }

    fun toggleFocusHours(spaceId: String) {
        _spaces.update { list ->
            list.map { s ->
                if (s.id == spaceId) {
                    val nextActive = !s.isFocusHoursActive
                    s.copy(
                        isFocusHoursActive = nextActive,
                        focusHoursBannerText = if (nextActive) "7 in focus until 11:00" else "Focus hours off"
                    )
                } else s
            }
        }
    }

    fun syncCalendar(context: Context) {
        scope.launch {
            if (calendarSyncService != null) {
                _calendarSyncStatus.value = "Syncing device calendar..."
                val result = calendarSyncService.syncCalendarEvents(context)
                if (result.isSuccess) {
                    _calendarSyncStatus.value = "Synced ${result.getOrNull()} events to Room database"
                } else {
                    _calendarSyncStatus.value = "Sync: ${result.exceptionOrNull()?.localizedMessage ?: "cached"}"
                }
            } else {
                _calendarSyncStatus.value = "Room database cache active"
            }
        }
    }

    fun setStatus(spaceId: String, family: StatusFamily, label: String, note: String, place: String, durationMinutes: Int) {
        val nowMs = System.currentTimeMillis()
        val expiresAt = nowMs + durationMinutes * 60 * 1000L

        _spaces.update { list ->
            list.map { space ->
                if (space.id == spaceId) {
                    space.copy(
                        currentStatusFamily = family,
                        currentStatusLabel = label,
                        currentStatusPlace = place,
                        currentStatusExpiresAt = expiresAt
                    )
                } else space
            }
        }
    }

    fun endStatus(spaceId: String) {
        _spaces.update { list ->
            list.map { space ->
                if (space.id == spaceId) {
                    space.copy(
                        currentStatusFamily = StatusFamily.AWAY,
                        currentStatusLabel = "",
                        currentStatusPlace = "",
                        currentStatusExpiresAt = 0L
                    )
                } else space
            }
        }
    }

    fun setGhostMode(durationMinutes: Int) {
        val expiresAt = if (durationMinutes > 0) System.currentTimeMillis() + durationMinutes * 60 * 1000L else null
        _userProfile.update { it.copy(ghostUntil = expiresAt) }
    }

    fun respondToPulse(pulseId: String, inPulse: Boolean) {
        val target = _pulses.value.firstOrNull { it.id == pulseId } ?: return
        // Tapping In or Can't on a cancelled or expired pulse must do nothing
        if (target.state.equals("CANCELLED", ignoreCase = true) || target.state.equals("EXPIRED", ignoreCase = true)) {
            return
        }

        val currentUser = _userProfile.value
        val userMember = CircleMember(
            id = currentUser.id,
            name = currentUser.displayName,
            initials = currentUser.displayName.take(2).uppercase(),
            tone = currentUser.windowTone,
            family = StatusFamily.OPEN,
            statusLabel = "In"
        )

        _pulses.update { list ->
            list.map { pulse ->
                if (pulse.id == pulseId) {
                    if (pulse.state.equals("CANCELLED", ignoreCase = true) || pulse.state.equals("EXPIRED", ignoreCase = true)) {
                        return@map pulse
                    }

                    val updatedInList = pulse.membersIn.toMutableList()
                    val updatedWaitlist = pulse.waitlist.toMutableList()

                    updatedInList.removeAll { it.id == currentUser.id }
                    updatedWaitlist.removeAll { it.id == currentUser.id }

                    if (inPulse) {
                        if (pulse.cap != null && updatedInList.size >= pulse.cap) {
                            updatedWaitlist.add(userMember)
                        } else {
                            updatedInList.add(userMember)
                        }
                    }

                    val newCount = updatedInList.size
                    // A confirmed pulse stays confirmed if someone drops out after it confirms
                    val wasConfirmed = pulse.state == "CONFIRMED"
                    val newState = if (wasConfirmed || newCount >= pulse.quorum) "CONFIRMED" else "OPEN"

                    pulse.copy(
                        isUserIn = inPulse,
                        isUserCant = !inPulse,
                        membersIn = updatedInList,
                        waitlist = updatedWaitlist,
                        state = newState
                    )
                } else pulse
            }
        }
    }

    fun createPulse(
        spaceId: String,
        circleId: String,
        circleName: String,
        title: String,
        place: String,
        startsAt: Long,
        durationMin: Int,
        quorum: Int,
        cap: Int?,
        decideBy: Long,
        note: String? = null
    ): SyncPulse {
        val currentUser = _userProfile.value
        val userMember = CircleMember(
            id = currentUser.id,
            name = currentUser.displayName,
            initials = currentUser.displayName.take(2).uppercase(),
            tone = currentUser.windowTone,
            family = StatusFamily.OPEN,
            statusLabel = "Creator"
        )

        val newPulse = SyncPulse(
            id = "pulse_${System.currentTimeMillis()}",
            spaceId = spaceId,
            circleId = circleId,
            circleName = circleName,
            creatorId = currentUser.id,
            creatorName = currentUser.displayName,
            title = title,
            place = place,
            startsAt = startsAt,
            durationMin = durationMin,
            quorum = quorum,
            cap = cap,
            decideBy = decideBy,
            state = if (quorum <= 1) "CONFIRMED" else "OPEN",
            isUserIn = true,
            membersIn = listOf(userMember),
            note = note
        )

        _pulses.update { listOf(newPulse) + it }
        return newPulse
    }

    fun cancelPulse(pulseId: String) {
        _pulses.update { list ->
            list.map {
                if (it.id == pulseId) it.copy(state = "CANCELLED") else it
            }
        }
    }

    fun addScheduleBlock(dayOfWeek: Int, startHour: Int, startMin: Int, endHour: Int, endMin: Int, title: String, kind: String) {
        val newBlock = ScheduleBlock(
            id = "sb_${System.currentTimeMillis()}",
            dayOfWeek = dayOfWeek,
            startHour = startHour,
            startMin = startMin,
            endHour = endHour,
            endMin = endMin,
            title = title,
            kind = kind
        )
        _scheduleBlocks.update { it + newBlock }
    }

    fun deleteScheduleBlock(id: String) {
        _scheduleBlocks.update { it.filterNot { b -> b.id == id } }
    }

    fun setOfficeDay(dayOfWeek: Int, weekIndex: Int, status: String) {
        _officeDays.update { list ->
            val mutable = list.toMutableList()
            val idx = mutable.indexOfFirst { it.dayOfWeek == dayOfWeek && it.weekIndex == weekIndex }
            if (idx >= 0) {
                mutable[idx] = mutable[idx].copy(status = status)
            } else {
                mutable.add(OfficeDayPlan(dayOfWeek, weekIndex, status))
            }
            mutable
        }
    }

    fun respondMixer(inMatch: Boolean) {
        _mixerMatch.update { it?.copy(isUserIn = inMatch, isUserCant = !inMatch) }
    }

    fun runMixerNow() {
        _mixerMatch.value = MixerMatch(
            id = "mixer_${System.currentTimeMillis()}",
            title = "Coffee with David (Platform) and Zoe (Design)",
            slot = "Tomorrow 3:00",
            members = listOf("David Kim", "Zoe Alverez", "Arnav"),
            isUserIn = false
        )
    }

    fun setPro(pro: Boolean) {
        val oneYearLater = System.currentTimeMillis() + 365L * 24 * 3600 * 1000
        _userProfile.update { it.copy(isPro = pro, proUntil = if (pro) oneYearLater else null) }
    }

    fun upgradeToPro() {
        setPro(true)
    }

    fun upgradeWorkplaceSeats(newSeatLimit: Int, newTier: String) {
        _spaces.update { list ->
            list.map { space ->
                if (space.id == "kestrel_labs") {
                    space.copy(seatLimit = newSeatLimit, planTier = newTier)
                } else space
            }
        }
    }

    fun createCircle(spaceId: String, name: String, kind: String): Boolean {
        val isPro = _userProfile.value.isPro
        val ownedCount = _circles.value.count { it.isOwner }
        if (!isPro && ownedCount >= 2) {
            return false
        }

        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { alphabet.random() }.joinToString("")

        val newCircle = SyncCircle(
            id = "circle_${System.currentTimeMillis()}",
            spaceId = spaceId,
            name = name.trim().ifBlank { "New Circle" },
            kind = kind.lowercase(),
            memberCount = 1,
            freeCount = 1,
            isOwner = true,
            visibility = "private",
            joinCode = code,
            members = listOf(
                CircleMember(
                    id = "m_me",
                    name = _userProfile.value.displayName,
                    initials = _userProfile.value.displayName.take(2).uppercase(),
                    tone = _userProfile.value.windowTone,
                    family = StatusFamily.OPEN,
                    statusLabel = "Available",
                    place = ""
                )
            )
        )

        _circles.update { it + newCircle }
        return true
    }

    fun joinCircleByCode(code: String): SyncCircle? {
        val found = _circles.value.firstOrNull { it.joinCode.equals(code.trim(), ignoreCase = true) }
        return found
    }

    fun requestDiscoverCircle(circleId: String) {
        _discoverCircles.update { list ->
            list.map {
                if (it.id == circleId) it.copy(isRequested = true) else it
            }
        }
    }

    fun addMemberToCircle(circleId: String, memberName: String): Boolean {
        val circle = _circles.value.firstOrNull { it.id == circleId } ?: return false
        val isPro = _userProfile.value.isPro

        if (circle.memberCount >= 12 && !isPro) {
            return false
        }

        val newMember = CircleMember(
            id = "m_${System.currentTimeMillis()}",
            name = memberName,
            initials = memberName.take(2).uppercase(),
            tone = "Sky",
            family = StatusFamily.OPEN,
            statusLabel = "Free now",
            place = ""
        )

        _circles.update { list ->
            list.map { c ->
                if (c.id == circleId) {
                    c.copy(
                        memberCount = c.memberCount + 1,
                        freeCount = c.freeCount + 1,
                        members = c.members + newMember
                    )
                } else c
            }
        }
        return true
    }

    fun createSpace(name: String, type: SpaceType): SyncSpace {
        val cleanName = name.trim().ifBlank { "New Space" }
        val id = "space_${System.currentTimeMillis()}"
        val newSpace = SyncSpace(
            id = id,
            type = type,
            name = cleanName,
            statusWordsTemplate = if (type == SpaceType.WORKPLACE) "Workplace" else "Community",
            places = if (type == SpaceType.WORKPLACE) listOf("Office", "Remote", "Floor 3", "Cafeteria") else listOf("Library", "Cafe", "Quad", "Lounge"),
            currentStatusLabel = "Available",
            currentStatusFamily = StatusFamily.OPEN,
            seatLimit = if (type == SpaceType.WORKPLACE) 25 else 100,
            seatsUsed = 1,
            planTier = if (type == SpaceType.WORKPLACE) "Team 25" else "Free",
            inviteCode = "SPC${(100..999).random()}"
        )
        _spaces.update { it + newSpace }
        _currentSpaceId.value = id
        return newSpace
    }

    fun updateProfile(displayName: String, email: String, tone: String, timezone: String, workingHours: String) {
        val domain = email.substringAfter("@", "uchicago.edu").ifBlank { "uchicago.edu" }
        _userProfile.update {
            it.copy(
                displayName = displayName,
                email = email,
                verifiedDomain = domain,
                windowTone = tone,
                timezone = timezone,
                workingHours = workingHours
            )
        }
    }
}
