package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarSlotDao {
    @Query("SELECT * FROM calendar_slots ORDER BY dayOfWeek ASC, startHour ASC, startMin ASC")
    fun getAllSlots(): Flow<List<CalendarSlotEntity>>

    @Query("SELECT * FROM calendar_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startHour ASC")
    fun getSlotsForDay(dayOfWeek: Int): Flow<List<CalendarSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<CalendarSlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: CalendarSlotEntity): Long

    @Query("DELETE FROM calendar_slots WHERE id = :id")
    suspend fun deleteSlotById(id: Long)

    @Query("DELETE FROM calendar_slots")
    suspend fun clearAllSlots()
}
