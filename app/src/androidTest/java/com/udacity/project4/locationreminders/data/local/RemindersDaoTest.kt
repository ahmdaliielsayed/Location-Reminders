package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Unit test the DAO
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private lateinit var reminderDao: RemindersDao
    private lateinit var fakeList: MutableList<ReminderDTO>
    private val nothing: Unit = Unit

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderDao = database.reminderDao()
        fakeList = fakeReminderData.asReminderDTOMutableList()
    }

    @Test
    fun nothing_getCount_checkCountListIsZero() = runBlockingTest {
        // Given
        nothing

        // When
        val countList = reminderDao.getCountList()

        // Then
        ViewMatchers.assertThat(countList, CoreMatchers.`is`(0))
    }

    @Test
    fun reminder_saveReminder_checkCountListIsChanged() = runBlockingTest {
        // Given
        val reminder = fakeList[0]
        val oldCount = reminderDao.getCountList()

        // When
        reminderDao.saveReminder(reminder)

        // Then
        val newCount = reminderDao.getCountList()
        ViewMatchers.assertThat(oldCount, CoreMatchers.`is`(0))
        ViewMatchers.assertThat(newCount, CoreMatchers.`is`(1))
    }

    @Test
    fun reminder_deleteReminderById_checkCountListIsChanged() = runBlockingTest {
        // Given
        val reminder = fakeList[0]
        reminderDao.saveReminder(reminder)
        val oldCount = reminderDao.getCountList()

        // When
        reminderDao.deleteReminder(reminder.id)

        // Then
        val newCount = reminderDao.getCountList()
        ViewMatchers.assertThat(oldCount, CoreMatchers.`is`(1))
        ViewMatchers.assertThat(newCount, CoreMatchers.`is`(0))
    }

    @Test
    fun remindersList_deleteAlReminders_checkCountListIsZero() = runBlockingTest {
        // Given
        fakeList.forEach { reminderDao.saveReminder(it) }
        val oldCount = reminderDao.getCountList()

        // When
        reminderDao.deleteAllReminders()

        // Then
        val newCount = reminderDao.getCountList()
        ViewMatchers.assertThat(oldCount, CoreMatchers.`is`(9))
        ViewMatchers.assertThat(newCount, CoreMatchers.`is`(0))
    }

    @Test
    fun reminder_getReminderById_ReminderIsValid() = runBlockingTest {
        // Given
        reminderDao.saveReminder(fakeList[0])

        // When
        val retrievedReminder = reminderDao.getReminderById(fakeList[0].id)

        // Then
        ViewMatchers.assertThat(retrievedReminder as ReminderDTO, IsNull.notNullValue())
        ViewMatchers.assertThat(retrievedReminder.id, CoreMatchers.`is`(fakeList[0].id))
        ViewMatchers.assertThat(retrievedReminder.title, CoreMatchers.`is`(fakeList[0].title))
        ViewMatchers.assertThat(retrievedReminder.description, CoreMatchers.`is`(fakeList[0].description))
        ViewMatchers.assertThat(retrievedReminder.location, CoreMatchers.`is`(fakeList[0].location))
        ViewMatchers.assertThat(retrievedReminder.latitude, CoreMatchers.`is`(fakeList[0].latitude))
        ViewMatchers.assertThat(retrievedReminder.longitude, CoreMatchers.`is`(fakeList[0].longitude))
    }

    @Test
    fun reminders_getReminders_validateReminders() = runBlockingTest {
        // Given
        fakeList.forEach { reminderDao.saveReminder(it) }

        // When
        val retrievedRemindersFromRoom = reminderDao.getReminders()

        // Then
        val listValidated = retrievedRemindersFromRoom.containsAll(fakeList)
        ViewMatchers.assertThat(listValidated, CoreMatchers.`is`(true))
    }

    @Test
    fun reminder_saveReminder_confirmReminderUpdated() = runBlockingTest {
        // Given
        fakeList.forEach { reminderDao.saveReminder(it) }
        val reminderToEdit = reminderDao.getReminders()[1]
        val newDescription = "description 2 edited"

        // When
        reminderToEdit.description = newDescription
        reminderDao.saveReminder(reminderToEdit)

        // Then
        val updatedReminder = reminderDao.getReminderById(reminderToEdit.id)
        ViewMatchers.assertThat(updatedReminder?.description, CoreMatchers.`is`(newDescription))
    }

    @After
    fun closeDb() = database.close()
}
