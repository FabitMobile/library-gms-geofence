package ru.fabit.gmsgeofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence

class GmsGeofenceEventWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            handle()
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            System.currentTimeMillis().hashCode(),
            makeNotification(
                context,
                this::class.java.simpleName,
                context.getString(GmsGeofenceInstance.config.foregroundNotificationChannelNameResId),
                context.getString(GmsGeofenceInstance.config.foregroundNotificationContentTextResId),
                context.getString(GmsGeofenceInstance.config.foregroundNotificationTitleResId)
            )
        )
    }

    private fun makeNotification(
        context: Context,
        serviceId: String,
        channelName: String,
        contentText: String,
        contentTitle: String? = null
    ): Notification {
        val channelId = createNotificationChannel(context, serviceId, channelName)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
        return notificationBuilder
            .setOngoing(true)
            .setSmallIcon(GmsGeofenceInstance.config.foregroundNotificationDrawableResId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel(
        context: Context,
        serviceId: String,
        channelName: String
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan =
                NotificationChannel(serviceId, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.GREEN
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }
        return serviceId
    }

    private fun handle() {
        val errorMessage = inputData.getString(GEOFENCE_ERROR_MESSAGE_KEY)
        val errorCode = inputData.getInt(GEOFENCE_ERROR_CODE_KEY, NO_VALUE)

        val geofenceTransition = inputData.getInt(GEOFENCE_TRANSITION_KEY, NO_VALUE)
        val geofenceIdsString = inputData.getString(TRIGGERING_GEOFENCE_IDS_KEY)
        val geofenceIds = geofenceIdsString?.split(",") ?: listOf()

        if (errorCode != NO_VALUE) {
            GmsGeofenceInstance.errorHandler?.handle(RuntimeException("geofencingEvent error $errorCode $errorMessage"))
        } else {
            val gmsGeofenceEventHandler = GmsGeofenceInstance.eventHandler
            if (gmsGeofenceEventHandler != null && geofenceIds.isNotEmpty()) {
                when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        gmsGeofenceEventHandler.onTransitionExit(geofenceIds)
                    }

                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        gmsGeofenceEventHandler.onTransitionEnter(geofenceIds)
                    }

                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        gmsGeofenceEventHandler.onTransitionDwell(geofenceIds)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "GmsGeofenceEventWorker"

        private const val NO_VALUE = 404

        const val GEOFENCE_TRANSITION_KEY = "GEOFENCE_TRANSITION_KEY"
        const val TRIGGERING_GEOFENCE_IDS_KEY = "TRIGGERING_GEOFENCE_IDS_KEY"

        const val GEOFENCE_ERROR_CODE_KEY = "GEOFENCE_ERROR_CODE_KEY"
        const val GEOFENCE_ERROR_MESSAGE_KEY = "GEOFENCE_ERROR_MESSAGE_KEY"
    }
}