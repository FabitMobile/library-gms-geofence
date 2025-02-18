package ru.fabit.gmsgeofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationAvailability

class UpdateLocationService : Service() {

    private lateinit var locationProvider: LocationProvider

    override fun onCreate() {
        super.onCreate()
        locationProvider = LocationProvider(applicationContext)
        startForeground()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (LocationAvailability.hasLocationAvailability(intent)) {
                val locationAvailability = LocationAvailability.extractLocationAvailability(intent)
                if (locationAvailability == null || !locationAvailability.isLocationAvailable) {
                    stop()
                }
            }
        }
        val isFinish = GmsGeofenceInstance.locationServiceManager?.isFinish() ?: false
        if (isFinish) stop()
        return START_NOT_STICKY
    }

    private fun stop() {
        locationProvider.unsubscribeBackgroundLocation()
        stopForeground(true)
        stopSelf()
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                System.currentTimeMillis().hashCode(), makeNotification(
                    this,
                    this::class.java.simpleName,
                    getString(R.string.geofence_foreground_notification_channel_name),
                    getString(R.string.geofence_foreground_notification_text_content),
                    getString(R.string.geofence_foreground_notification_title)
                ),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(
                System.currentTimeMillis().hashCode(), makeNotification(
                    this,
                    this::class.java.simpleName,
                    getString(R.string.geofence_foreground_notification_channel_name),
                    getString(R.string.geofence_foreground_notification_text_content),
                    getString(R.string.geofence_foreground_notification_title)
                )
            )
        }
    }

    private fun makeNotification(
        context: Context,
        serviceId: String,
        channelName: String,
        contentText: String,
        contentTitle: String? = null
    ): Notification {
        val channelId: String = createNotificationChannel(context, serviceId, channelName)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
        val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Notification.CATEGORY_NAVIGATION
        } else {
            Notification.CATEGORY_SERVICE
        }
        return notificationBuilder
            .setOngoing(true)
            .setSmallIcon(GmsGeofenceInstance.config.foregroundNotificationDrawableResId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(category)
            .build()
    }

    private fun createNotificationChannel(
        context: Context,
        serviceId: String,
        channelName: String
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan =
                NotificationChannel(serviceId, channelName, NotificationManager.IMPORTANCE_HIGH)
            chan.lightColor = Color.GREEN
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }
        return serviceId
    }
}