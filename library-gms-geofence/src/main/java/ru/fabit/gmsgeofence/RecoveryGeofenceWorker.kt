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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecoveryGeofenceWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val TAG = "RecoveryGeofenceWorker"
    }

    override suspend fun doWork(): Result {
        val recoveryManager = GmsGeofenceInstance.recoveryManager
        return try {
            withContext(Dispatchers.IO) {
                recoveryManager?.recovery {}
            }
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
                appContext,
                this::class.java.simpleName,
                appContext.getString(R.string.geofence_foreground_notification_channel_name),
                appContext.getString(R.string.recovery_geofence_foreground_notification_text_content),
                appContext.getString(R.string.recovery_geofence_foreground_notification_title)
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
            .setSmallIcon(GmsGeofenceInstance.config.recoveryForegroundNotificationDrawableResId)
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
}