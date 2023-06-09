package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    private val _logoutState = MutableLiveData<Boolean?>()
    val logoutState: LiveData<Boolean?> = _logoutState

    private val _showLoadingMutableLiveData = MutableLiveData<Boolean>()
    val showLoadingLiveData: LiveData<Boolean> = _showLoadingMutableLiveData

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            // interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll(
                        (result.data as List<ReminderDTO>).map { reminder ->
                            // map the reminder data from the DB to the be ready to be displayed on the UI
                            ReminderDataItem(
                                reminder.title,
                                reminder.description,
                                reminder.location,
                                reminder.latitude,
                                reminder.longitude,
                                reminder.id
                            )
                        }
                    )
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }

            // check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Deletes a reminder from the database using the given ID.
     */
    fun deleteReminder(id: String) {
        viewModelScope.launch {
            dataSource.deleteReminder(id)
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    fun logout(context: Context) {
        AuthUI.getInstance()
            .signOut(context)
            .addOnCompleteListener {
                _logoutState.value = !it.isSuccessful
            }
    }
}
