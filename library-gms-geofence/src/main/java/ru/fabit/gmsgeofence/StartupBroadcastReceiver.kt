package ru.fabit.gmsgeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.multiprocess.RemoteWorkManager

class StartupBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val workManager = RemoteWorkManager.getInstance(context)
            val request = OneTimeWorkRequest.Builder(RecoveryGeofenceWorker::class.java)
                .addTag(RecoveryGeofenceWorker.TAG)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            workManager.enqueueUniqueWork(
                RecoveryGeofenceWorker.TAG,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}