package ru.fabit.gmsgeofence

import ru.fabit.gmsgeofence.entity.GmsGeofenceConfig

object GmsGeofenceInstance {
    var eventHandler: GmsGeofenceEventHandler? = null
    var recoveryManager: RecoveryGeofenceManager? = null
    var config: GmsGeofenceConfig = GmsGeofenceConfig()
    fun getInstance(eventHandler: GmsGeofenceEventHandler, recoveryManager: RecoveryGeofenceManager, config: GmsGeofenceConfig) {
        this.eventHandler = eventHandler
        this.recoveryManager = recoveryManager
        this.config = config
    }
}