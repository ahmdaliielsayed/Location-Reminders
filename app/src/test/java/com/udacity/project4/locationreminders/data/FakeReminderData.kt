package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.io.Serializable
import java.util.*

/**
 * A data class for fake reminders.
 */
data class ReminderTestItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Serializable

val fakeReminderData: MutableList<ReminderTestItem> = mutableListOf(
    ReminderTestItem("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.2", "description 2", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.3", "description 3", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.4", "description 4", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.5", "description 5", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.6", "description 6", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.7", "description 7", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.8", "description 8", "City ", 30.043457431, 31.2765762),
    ReminderTestItem("Task NO.9", "description 9", "City ", 30.043457431, 31.2765762)
)

fun MutableList<ReminderTestItem>.asReminderDTOMutableList(): MutableList<ReminderDTO> {
    return map { reminder ->
        ReminderDTO(
            title = reminder.title,
            description = reminder.description,
            location = reminder.location,
            latitude = reminder.latitude,
            longitude = reminder.longitude
        )
    }.toMutableList()
}

fun MutableList<ReminderTestItem>.asReminderDataItemMutableList(): MutableList<ReminderDataItem> {
    return map { reminder ->
        ReminderDataItem(
            title = reminder.title,
            description = reminder.description,
            location = reminder.location,
            latitude = reminder.latitude,
            longitude = reminder.longitude
        )
    }.toMutableList()
}
