package ru.fabit.gmsgeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GmsGeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent =
            Intent(context?.applicationContext, GmsGeofenceEventService::class.java)
        intent?.let { serviceIntent.putExtras(intent) }
        context?.applicationContext?.startForegroundService(serviceIntent)
    }
}