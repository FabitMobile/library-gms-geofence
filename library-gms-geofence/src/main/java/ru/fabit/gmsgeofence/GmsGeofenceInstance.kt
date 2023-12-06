package ru.fabit.gmsgeofence

import ru.fabit.gmsgeofence.entity.GmsGeofenceConfig

object GmsGeofenceInstance {
    var eventHandler: GmsGeofenceEventHandler? = null
    var recoveryManager: RecoveryGeofenceManager? = null
    var locationServiceManager: LocationServiceManager? = null
    var errorHandler: ErrorHandler? = null
    var config: GmsGeofenceConfig = GmsGeofenceConfig()
    fun getInstance(
        eventHandler: GmsGeofenceEventHandler,
        recoveryManager: RecoveryGeofenceManager,
        config: GmsGeofenceConfig,
        locationServiceManager: LocationServiceManager,
        errorHandler: ErrorHandler
    ) {
        this.eventHandler = eventHandler
        this.recoveryManager = recoveryManager
        this.config = config
        this.locationServiceManager = locationServiceManager
        this.errorHandler = errorHandler
    }
}