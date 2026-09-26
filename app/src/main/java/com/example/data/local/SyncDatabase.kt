package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalendarSlotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun calendarSlotDao(): CalendarSlotDao

    companion object {
        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getInstance(context: Context): SyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SyncDatabase::class.java,
                    "sync_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
