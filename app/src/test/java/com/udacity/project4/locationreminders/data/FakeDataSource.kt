package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/**
 * A fake data source that acts as a test double to the local data source.
 * Use FakeDataSource that acts as a test double to the LocalDataSource
 */
class FakeDataSource : ReminderDataSource {

    private var fakeList: MutableList<ReminderDTO> = mutableListOf()
    private var shouldReturnError: Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(Exception("Reminder Exception!").toString())
        }
        return Result.Success(ArrayList(fakeList))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error(Exception("Reminder Exception!").toString())
        }
        fakeList.let { fakeList ->
            for (reminder in fakeList) {
                if (reminder.id == id) {
                    return Result.Success(reminder)
                }
            }
        }
        return Result.Error(Exception("Reminder not found!").toString())
    }

    override suspend fun deleteAllReminders() {
        fakeList.clear()
    }

    override suspend fun deleteReminder(id: String) {
        fakeList.let { reminders ->
            reminders.removeIf { reminder -> (reminder.id == id) }
        }
    }

    override suspend fun getCountList(): Int {
        return fakeList.size
    }

    /**
     * Uses the given [Boolean] to specify if an error should be returned.
     */
    fun setShouldReturnError(returnError: Boolean) {
        shouldReturnError = returnError
    }
}
