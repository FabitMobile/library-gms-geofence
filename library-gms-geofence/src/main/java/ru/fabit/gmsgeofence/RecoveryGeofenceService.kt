package ru.fabit.gmsgeofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class RecoveryGeofenceService : Service() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val recoveryManager = GmsGeofenceInstance.recoveryManager
        job?.cancel()
        try {
            job = scope.launch {
                    recoveryManager?.recovery {
                        stopForeground(true)
                        stopSelf()
                    }
                }
        } catch (t: Throwable) {
            if (t !is CancellationException) {
                throw t
            }
        }
        return START_NOT_STICKY
    }

    private fun startForeground() {
        startForeground(
            System.currentTimeMillis().hashCode(), makeNotification(
                this,
                this::class.java.simpleName,
                getString(GmsGeofenceInstance.config.recoveryForegroundNotificationChannelNameResId),
                getString(GmsGeofenceInstance.config.recoveryForegroundNotificationContentTextResId),
                getString(GmsGeofenceInstance.config.recoveryForegroundNotificationTitleResId)
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
        var channelId = ""
        channelId = createNotificationChannel(context, serviceId, channelName)
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
        val chan = NotificationChannel(serviceId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return serviceId
    }
}