package ru.fabit.gmsgeofence

interface ErrorHandler {
    fun handle(throwable: Throwable)
}