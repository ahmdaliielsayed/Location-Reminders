package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.data.local.asReminderDTOMutableList
import com.udacity.project4.locationreminders.data.local.fakeReminderData
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
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

// END TO END test to black box test the app
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private lateinit var fakeList: MutableList<ReminderDTO>

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get: Rule
    var intentRule = ActivityScenarioRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin() // stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        // declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        // Get our real repository
        repository = get()

        // clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        // initialize fake data list
        fakeList = fakeReminderData.asReminderDTOMutableList()
    }

    @Test
    fun saveReminder_displaySnackBar_TitleError() {
        Espresso
            .onView(ViewMatchers.withId(R.id.addReminderFAB))
            .perform(ViewActions.click())
        Espresso
            .onView(ViewMatchers.withId(R.id.saveReminder))
            .perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        Espresso
            .onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun saveReminder_displaySnackBar_LocationError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso
            .onView(ViewMatchers.withId(R.id.addReminderFAB))
            .perform(ViewActions.click())
        Espresso
            .onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        Espresso.closeSoftKeyboard()
        Espresso
            .onView(ViewMatchers.withId(R.id.saveReminder))
            .perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_select_location)
        Espresso
            .onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun saveReminder_showToastMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso
            .onView(ViewMatchers.withId(R.id.addReminderFAB))
            .perform(ViewActions.click())
        Espresso
            .onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        Espresso.closeSoftKeyboard()
        Espresso
            .onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"))
        Espresso.closeSoftKeyboard()

        Espresso
            .onView(ViewMatchers.withId(R.id.selectLocation))
            .perform(ViewActions.click())
        Espresso
            .onView(ViewMatchers.withId(R.id.mapFragment))
            .perform(ViewActions.longClick())

        Espresso
            .onView(ViewMatchers.withId(R.id.save_btn))
            .perform(ViewActions.click())
        Espresso
            .onView(ViewMatchers.withId(R.id.saveReminder))
            .perform(ViewActions.click())

        Espresso
            .onView(ViewMatchers.withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(Matchers.not(CoreMatchers.`is`(getActivity(activityScenario).window.decorView))))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
