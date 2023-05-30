package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repo: ReminderDataSource
    private lateinit var context: Application

    @get:Rule
    val instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    private val reminderItemForTesting: ReminderDTO by lazy { ReminderDTO("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762) }

    @Before
    fun init() {
        // Stop the original app koin
        stopKoin()
        context = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }

            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> {
                RemindersLocalRepository(get())
            }

            single {
                LocalDB.createRemindersDao(context)
            }
        }

        // Declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        // Get our real repository
        repo = get()

        // Clear the data to start fresh
        runBlocking {
            repo.deleteAllReminders()
        }
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun onClickAddReminderFAB_navigateToSaveReminderScreen() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_LocationReminder)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun remindersList_DisplayScreen(): Unit = runBlocking {
        repo.saveReminder(reminderItemForTesting)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_LocationReminder)
        Espresso.onView(withId(R.id.reminderssRecyclerView))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(withText(reminderItemForTesting.title))))
    }

    @Test
    fun checkDisplay_notFoundDataToDisplayed(): Unit = runBlocking {
        repo.saveReminder(reminderItemForTesting)
        repo.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_LocationReminder)
        Espresso
            .onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
