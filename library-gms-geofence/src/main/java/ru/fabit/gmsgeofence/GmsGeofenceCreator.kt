package ru.fabit.gmsgeofence

import ru.fabit.gmsgeofence.entity.GmsGeofence

interface GmsGeofenceCreator {
    fun createGeofences(list: List<GmsGeofence>)
}