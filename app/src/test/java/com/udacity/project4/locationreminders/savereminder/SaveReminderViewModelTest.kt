package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.asReminderDataItemMutableList
import com.udacity.project4.locationreminders.data.fakeReminderData
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Subject under test
    private lateinit var saveReminderViewModelForTesting: SaveReminderViewModel

    // Use a fake repository to be injected into the viewModel
    private lateinit var fakeDataSourceForTesting: FakeDataSource

    private lateinit var fakeList: MutableList<ReminderDataItem>
    private lateinit var reminderDataItemForTesting: ReminderDataItem
    private val nothing: Unit = Unit // Placeholder for empty test parameters.

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
        saveReminderViewModelForTesting = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSourceForTesting)
        fakeList = fakeReminderData.asReminderDataItemMutableList()
    }

    @Test
    fun `validateEnteredData NullTitle And Display SnackBar Error`() = runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[0]

        // When
        reminderDataItemForTesting.title = null

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting), CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun `validateEnteredData EmptyTitle And Display SnackBar Error`() = runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[1]

        // When
        reminderDataItemForTesting.title = ""

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting), CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun `validateEnteredData NullLocation And Display SnackBar Error`() = runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[2]

        // When
        reminderDataItemForTesting.location = null

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting), CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_select_location))
    }

    @Test
    fun `validateEnteredData EmptyLocation And Display SnackBar Error`() = runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[3]

        // When
        reminderDataItemForTesting.location = ""

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting), CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_select_location))
    }

    @Test
    fun `saveReminder Show Is Loading And Display`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[4]
        pauseDispatcher()

        // When
        saveReminderViewModelForTesting.saveReminder(reminderDataItemForTesting)

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        resumeDispatcher()
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun `saveReminder Show Toast`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[5]

        // When
        saveReminderViewModelForTesting.saveReminder(reminderDataItemForTesting)

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showToast.getOrAwaitValue(), CoreMatchers.`is`("Reminder Saved!"))
    }

    @Test
    fun `saveReminder Then NavigateCommandBack`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        reminderDataItemForTesting = fakeList[6]

        // When
        saveReminderViewModelForTesting.saveReminder(reminderDataItemForTesting)

        // Then
        MatcherAssert.assertThat(saveReminderViewModelForTesting.navigationCommand.getOrAwaitValue(), CoreMatchers.`is`(NavigationCommand.Back))
    }

    @Test
    fun `nothing onClear checkValuesCleared`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        nothing

        // When
        saveReminderViewModelForTesting.onClear()

        // Then
        assert(saveReminderViewModelForTesting.reminderTitle.getOrAwaitValue() == null)
        assert(saveReminderViewModelForTesting.reminderDescription.getOrAwaitValue() == null)
        assert(saveReminderViewModelForTesting.reminderSelectedLocationStr.getOrAwaitValue() == null)
        assert(saveReminderViewModelForTesting.selectedPOI.getOrAwaitValue() == null)
        assert(saveReminderViewModelForTesting.latitude.getOrAwaitValue() == null)
        assert(saveReminderViewModelForTesting.longitude.getOrAwaitValue() == null)
    }
}
