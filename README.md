# gms-geofence - библиотека для управления геозонами, поставляемыми в com.google.android.gms:play-services-location

Библиотека включает в себя логику по добавлению, удалению
и восстановлению (например, после перезагрузки устройства) геозон

GmsGeofenceInstance - объект предназначен для определения параметров конфигурации библиотеки,
и может быть задействован без пользовательского интерфейса в сценариях восстановления геозон,
поэтому его применение должно происходить в саммой ранней точке работы приложения - Application.onCreate:

```kotlin
class App : Application() {
    override fun onCreate() {
        val gmsGeofenceEventHandler = GmsGeofenceEventHandlerImpl()
        val recoveryGeofenceManager = RecoveryGeofenceManagerImpl()
        val gmsGeofenceConfig = GmsGeofenceConfig()
        GmsGeofenceInstance.getInstance(gmsGeofenceEventHandler, recoveryGeofenceManager, gmsGeofenceConfig)
    }
}
```

GmsGeofenceEventHandler - интерфейс, определяющий логику обработки событий из геозон (въезд, выезд, нахождение)

RecoveryGeofenceManager - интерфейс, определяющий логику пересоздания геозон, если не реализовать
данный интерфейс, то при перезагрузке устройства, геозоны не будут пересозданы. Пример:

```kotlin
class GeofenceRepository {
    fun getGeofences(): List<GmsGeofence> {
        return listOf(
            GmsGeofence("0", 0.0, 0.0, 100f, 0),
            GmsGeofence("1", 0.0, 0.0, 100f, 0),
        )
    }
}

class RecoveryGeofenceManagerImpl(
    private val gmsGeofenceCreatorImpl: GmsGeofenceCreatorImpl,
    private val geofenceRepository: GeofenceRepository
) : RecoveryGeofenceManager {
    override suspend fun recovery(completeCallback: () -> Unit) {
        gmsGeofenceCreatorImpl.createGeofences(geofenceRepository.getGeofences())
        completeCallback()
    }
}
```

GmsGeofenceConfig - класс, определяющий настройки визуального отображения foreground уведомлений, а
также работоспособность событий доступных для регистрации и прослушивания от держателя геозон.

GmsGeofenceCreatorImpl - класс, помогающий создавать геозоны, принимает на вход список GmsGeofence объектов.

Библиотека требует доступ к фоновой геолокации приложения android.permission.ACCESS_BACKGROUND_LOCATION и
событиям при запуске устройства android.permission.RECEIVE_BOOT_COMPLETED

