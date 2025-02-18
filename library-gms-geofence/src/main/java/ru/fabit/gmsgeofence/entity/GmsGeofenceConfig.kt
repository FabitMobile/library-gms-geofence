package ru.fabit.gmsgeofence.entity

import ru.fabit.gmsgeofence.R

data class GmsGeofenceConfig(
    val foregroundNotificationDrawableResId: Int = R.drawable.location,
    val recoveryForegroundNotificationDrawableResId: Int = R.drawable.location,
    val eventExitEnabled: Boolean = false,
    val eventEnterEnabled: Boolean = false,
    val eventDwellEnabled: Boolean = false,
    val loiteringDelay: Int = 0,
    val updateLocationInterval: Long = 0
)