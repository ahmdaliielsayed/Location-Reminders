package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.asReminderDTOMutableList
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.fakeReminderData
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModelForTesting: RemindersListViewModel

    // Use a fake repository to be injected into the viewModel
    private lateinit var fakeDataSourceForTesting: FakeDataSource
    private lateinit var fakeList: MutableList<ReminderDTO>

    // Placeholder for empty test parameters.
    private val nothing: Unit = Unit

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRuleTest = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        fakeDataSourceForTesting = FakeDataSource()
        remindersListViewModelForTesting = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSourceForTesting)
        fakeList = fakeReminderData.asReminderDTOMutableList()
    }

    @Test
    fun `nothing loadReminders showLoading`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        nothing
        pauseDispatcher()

        // When
        remindersListViewModelForTesting.loadReminders()

        // Then
        MatcherAssert.assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        resumeDispatcher()
        MatcherAssert.assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun `reminderList loadReminders checkListNotEmpty`() = runBlockingTest {
        // Given
        fakeList.forEach {
            fakeDataSourceForTesting.saveReminder(it)
        }

        // When
        remindersListViewModelForTesting.loadReminders()

        // Then
        val isNotEmptyList = fakeDataSourceForTesting.getCountList() >= 1
        MatcherAssert.assertThat(isNotEmptyList, CoreMatchers.`is`(true))
    }

    @Test
    fun `emptyReminderList loadReminders checkListEmpty`() = runBlockingTest {
        // Given
        fakeDataSourceForTesting.deleteAllReminders()

        // When
        remindersListViewModelForTesting.loadReminders()

        // Then
        MatcherAssert.assertThat(remindersListViewModelForTesting.showNoData.getOrAwaitValue(), CoreMatchers.`is`(true))
    }

    @Test
    fun `ReminderList deleteReminder checkListEmpty`() = runBlockingTest {
        // Given
        fakeDataSourceForTesting.saveReminder(fakeList[0])

        // When
        fakeDataSourceForTesting.deleteReminder(fakeList[0].id)

        // Then
        remindersListViewModelForTesting.loadReminders()
        val isEmptyList = fakeDataSourceForTesting.getCountList() == 0
        MatcherAssert.assertThat(isEmptyList, CoreMatchers.`is`(true))
    }

    @Test
    fun `reminderError loadReminders show error`() {
        // Given
        fakeDataSourceForTesting.setShouldReturnError(true)

        // When
        remindersListViewModelForTesting.loadReminders()

        // Then
        val reminderException = Exception("Reminder Exception!").toString()
        MatcherAssert.assertThat(remindersListViewModelForTesting.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`(reminderException))
    }
}
