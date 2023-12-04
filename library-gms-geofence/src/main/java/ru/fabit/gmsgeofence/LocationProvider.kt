package ru.fabit.gmsgeofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

class LocationProvider(private val context: Context) {

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        val intent = Intent(context, UpdateLocationService::class.java)
        return PendingIntent.getForegroundService(
            context,
            UpdateLocationService::class.java.simpleName.hashCode(),
            intent,
            flag
        )
    }

    @SuppressLint("MissingPermission")
    fun subscribeBackgroundLocation(expirationDuration: Long, updateLocationInterval: Long) {
        val backgroundLocationRequest = LocationRequest
            .create()
            .apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = updateLocationInterval
                fastestInterval = updateLocationInterval
                setExpirationDuration(expirationDuration + TimeUnit.MINUTES.toMillis(1))
                isWaitForAccurateLocation = true
            }
        fusedLocationProviderClient.requestLocationUpdates(
            backgroundLocationRequest,
            getPendingIntent()
        )
    }

    fun unsubscribeBackgroundLocation() {
        if (GmsGeofenceInstance.config.updateLocationInterval != 0L) {
            fusedLocationProviderClient.removeLocationUpdates(getPendingIntent())
        }
    }
}