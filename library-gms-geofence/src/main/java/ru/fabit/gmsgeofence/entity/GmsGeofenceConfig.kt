package ru.fabit.gmsgeofence.entity

data class GmsGeofenceConfig(
    val foregroundNotificationChannelNameResId: Int = -1,
    val foregroundNotificationTitleResId: Int = -1,
    val foregroundNotificationContentTextResId: Int = -1,
    val foregroundNotificationDrawableResId: Int = -1,
    val recoveryForegroundNotificationChannelNameResId: Int = -1,
    val recoveryForegroundNotificationTitleResId: Int = -1,
    val recoveryForegroundNotificationContentTextResId: Int = -1,
    val recoveryForegroundNotificationDrawableResId: Int = -1,
    val eventExitEnabled: Boolean = false,
    val eventEnterEnabled: Boolean = false,
    val eventDwellEnabled: Boolean = false,
    val loiteringDelay: Int = 0,
    val updateLocationInterval: Long = 0
)
