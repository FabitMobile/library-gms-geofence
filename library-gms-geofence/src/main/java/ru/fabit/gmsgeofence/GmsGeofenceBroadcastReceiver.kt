package ru.fabit.gmsgeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.multiprocess.RemoteWorkManager
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import ru.fabit.gmsgeofence.GmsGeofenceEventWorker.Companion.GEOFENCE_ERROR_CODE_KEY
import ru.fabit.gmsgeofence.GmsGeofenceEventWorker.Companion.GEOFENCE_ERROR_MESSAGE_KEY
import ru.fabit.gmsgeofence.GmsGeofenceEventWorker.Companion.GEOFENCE_TRANSITION_KEY
import ru.fabit.gmsgeofence.GmsGeofenceEventWorker.Companion.TRIGGERING_GEOFENCE_IDS_KEY

class GmsGeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        geofencingEvent ?: return

        val inputData = if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)

            Data.Builder()
                .putInt(GEOFENCE_ERROR_CODE_KEY, geofencingEvent.errorCode)
                .putString(GEOFENCE_ERROR_MESSAGE_KEY, errorMessage)
                .build()
        } else {
            val geofenceTransition = geofencingEvent.geofenceTransition
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceIds = triggeringGeofences?.map { it.requestId } ?: listOf()

            Data.Builder()
                .putInt(GEOFENCE_TRANSITION_KEY, geofenceTransition)
                .putString(TRIGGERING_GEOFENCE_IDS_KEY, geofenceIds.joinToString(","))
                .build()
        }

        val workRequest = OneTimeWorkRequestBuilder<GmsGeofenceEventWorker>()
            .addTag(GmsGeofenceEventWorker.TAG)
            .setInputData(inputData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        RemoteWorkManager.getInstance(context).enqueueUniqueWork(
            RecoveryGeofenceWorker.TAG,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}