package ru.fabit.gmsgeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class StartupBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, RecoveryGeofenceService::class.java)
        context?.let { ContextCompat.startForegroundService(context, serviceIntent) }
    }
}