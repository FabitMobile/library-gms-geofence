package ru.fabit.gmsgeofence

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import ru.fabit.gmsgeofence.entity.GmsGeofence

class GmsGeofenceCreatorImpl(private val context: Context) : GmsGeofenceCreator {

    private val locationProvider = LocationProvider(context)

    private var mGeofencePendingIntent: PendingIntent? = null

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(
            context
        )
    }

    override fun createGeofences(list: List<GmsGeofence>) {
        removeGeofences()
        if (list.isNotEmpty()) {
            addGeofences(list)
                ?.addOnFailureListener {
                    GmsGeofenceInstance.errorHandler?.handle(it)
                }
            if (GmsGeofenceInstance.config.updateLocationInterval != 0L) {
                val expirationMillis = list.maxBy { it.durationMillis }.durationMillis
                locationProvider.subscribeBackgroundLocation(
                    expirationMillis,
                    GmsGeofenceInstance.config.updateLocationInterval
                )
            }
        } else {
            val serviceIntent =
                Intent(context.applicationContext, UpdateLocationService::class.java)
            locationProvider.unsubscribeBackgroundLocation()
            context.stopService(serviceIntent)
        }
    }

    private fun createGeofence(gmsGeofence: GmsGeofence) =
        Geofence.Builder()
            .setRequestId(gmsGeofence.id)
            .setCircularRegion(
                gmsGeofence.latitude,
                gmsGeofence.longitude,
                gmsGeofence.radius
            )
            .setExpirationDuration(gmsGeofence.durationMillis)
            .setTransitionTypes(getTransitionTypes())
            .setLoiteringDelay(GmsGeofenceInstance.config.loiteringDelay)
            .build()

    private fun getGeofencingRequest(geofences: List<Geofence>): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.addGeofences(geofences)
        return builder.build()
    }

    private fun getTransitionTypes(): Int {
        var transitionTypes = 0
        if (GmsGeofenceInstance.config.eventEnterEnabled) transitionTypes =
            transitionTypes or Geofence.GEOFENCE_TRANSITION_ENTER
        if (GmsGeofenceInstance.config.eventExitEnabled) transitionTypes =
            transitionTypes or Geofence.GEOFENCE_TRANSITION_EXIT
        if (GmsGeofenceInstance.config.eventDwellEnabled) transitionTypes =
            transitionTypes or Geofence.GEOFENCE_TRANSITION_DWELL
        return transitionTypes
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        mGeofencePendingIntent?.let {
            return it
        } ?: run {
            val intent = Intent(context, GmsGeofenceBroadcastReceiver::class.java)
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            mGeofencePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                flag
            )
            return mGeofencePendingIntent!!
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences(list: List<GmsGeofence>): Task<Void>? {
        if (!checkPermissions()) {
            return null
        }
        val geofences: MutableList<Geofence> = arrayListOf()
        list.forEach {
            if (checkRegion(it))
                geofences.add(createGeofence(it))
        }
        return geofencingClient.addGeofences(
            getGeofencingRequest(geofences),
            getGeofencePendingIntent()
        )
    }

    private fun checkRegion(geofence: GmsGeofence) =
        geofence.radius > 0 && geofence.latitude in latitudeRange && geofence.longitude in longitudeRange

    private fun removeGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED && checkBackgroundLocationPermissionGranted()
    }

    private fun checkBackgroundLocationPermissionGranted(): Boolean {
        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val latitudeRange = -90f..90f
        private val longitudeRange = -180f..180f
    }
}