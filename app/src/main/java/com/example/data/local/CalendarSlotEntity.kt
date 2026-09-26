package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_slots")
data class CalendarSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventTitle: String,
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val isAllDay: Boolean = false,
    val calendarAccount: String = "Google Calendar",
    val syncedAtMs: Long = System.currentTimeMillis()
)
