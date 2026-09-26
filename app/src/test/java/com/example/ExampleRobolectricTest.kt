package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CalendarSlotDao
import com.example.data.local.CalendarSlotEntity
import com.example.data.local.SyncDatabase
import com.example.data.model.SpaceType
import com.example.data.repository.SyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: SyncDatabase
    private lateinit var calendarSlotDao: CalendarSlotDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        calendarSlotDao = db.calendarSlotDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SYNC", appName)
    }

    @Test
    fun `insert and retrieve calendar slots from Room`() = runBlocking {
        val slot = CalendarSlotEntity(
            eventTitle = "Dentist Appointment",
            dayOfWeek = 2,
            startHour = 9,
            startMin = 0,
            endHour = 10,
            endMin = 0,
            startTimeMs = 1000L,
            endTimeMs = 2000L,
            calendarAccount = "test@uchicago.edu"
        )
        calendarSlotDao.insertSlot(slot)

        val slots = calendarSlotDao.getAllSlots().first()
        assertEquals(1, slots.size)
        assertEquals("Dentist Appointment", slots[0].eventTitle)
        assertEquals(2, slots[0].dayOfWeek)
    }

    @Test
    fun `space switching between community and workplace`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SyncRepository(context)

        val initialSpaceId = repo.currentSpaceId.value
        assertEquals("uchicago", initialSpaceId)

        val initialSpace = repo.spaces.value.first { it.id == initialSpaceId }
        assertEquals(SpaceType.COMMUNITY, initialSpace.type)

        // Switch to Workplace
        repo.selectSpace("kestrel_labs")
        val switchedSpace = repo.spaces.value.first { it.id == repo.currentSpaceId.value }
        assertEquals("kestrel_labs", switchedSpace.id)
        assertEquals(SpaceType.WORKPLACE, switchedSpace.type)
        assertEquals(100, switchedSpace.seatLimit)
        assertEquals("Business 100", switchedSpace.planTier)
    }
}

