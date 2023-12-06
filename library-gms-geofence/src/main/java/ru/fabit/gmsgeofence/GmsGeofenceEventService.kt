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
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.lang.RuntimeException

class GmsGeofenceEventService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handle(intent)
        return START_NOT_STICKY
    }

    private fun startForeground() {
        startForeground(
            System.currentTimeMillis().hashCode(), makeNotification(
                this,
                this::class.java.simpleName,
                getString(GmsGeofenceInstance.config.foregroundNotificationChannelNameResId),
                getString(GmsGeofenceInstance.config.foregroundNotificationContentTextResId),
                getString(GmsGeofenceInstance.config.foregroundNotificationTitleResId)
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
        val chan = NotificationChannel(serviceId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return serviceId
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    private fun handle(intent: Intent?) {
        intent?.also {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent?.hasError() == true) {
                GmsGeofenceInstance.errorHandler?.handle(RuntimeException("geofencingEvent error ${geofencingEvent.errorCode}"))
                stop()
            } else {
                val geofenceTransition = geofencingEvent?.geofenceTransition
                val triggeringGeofences = geofencingEvent?.triggeringGeofences
                val gmsGeofenceEventHandler = GmsGeofenceInstance.eventHandler
                if (gmsGeofenceEventHandler != null) {
                    when (geofenceTransition) {
                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            triggeringGeofences?.also { geofences ->
                                gmsGeofenceEventHandler.onTransitionExit(geofences.map { it.requestId })
                                stop()
                            } ?: stop()
                        }
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            triggeringGeofences?.also { geofences ->
                                gmsGeofenceEventHandler.onTransitionEnter(geofences.map { it.requestId })
                                stop()
                            } ?: stop()
                        }
                        Geofence.GEOFENCE_TRANSITION_DWELL -> {
                            triggeringGeofences?.also { geofences ->
                                gmsGeofenceEventHandler.onTransitionDwell(geofences.map { it.requestId })
                                stop()
                            } ?: stop()
                        }
                        else -> stop()
                    }
                } else {
                    stop()
                }
            }
        }
    }
}