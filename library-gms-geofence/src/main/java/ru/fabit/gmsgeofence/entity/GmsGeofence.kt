package ru.fabit.gmsgeofence.entity

data class GmsGeofence(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val durationMillis: Long
)
