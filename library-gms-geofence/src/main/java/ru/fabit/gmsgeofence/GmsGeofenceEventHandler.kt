package ru.fabit.gmsgeofence

interface GmsGeofenceEventHandler {
    fun onTransitionExit(ids: List<String>) {}
    fun onTransitionEnter(ids: List<String>) {}
    fun onTransitionDwell(ids: List<String>) {}
}