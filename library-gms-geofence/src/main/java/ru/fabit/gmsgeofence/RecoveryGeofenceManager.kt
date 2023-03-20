package ru.fabit.gmsgeofence

interface RecoveryGeofenceManager {
    suspend fun recovery(completeCallback: () -> Unit)
}