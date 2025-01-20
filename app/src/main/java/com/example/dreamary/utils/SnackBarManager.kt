package com.example.dreamary.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SnackbarManager {
    private val snackbarChannel = Channel<SnackbarMessage>()
    val snackbarMessages = snackbarChannel.receiveAsFlow()

    suspend fun showMessage(message: String, iconResId: Int, actionLabel: String? = null) {
        snackbarChannel.send(SnackbarMessage(message, iconResId, actionLabel))
    }
}

data class SnackbarMessage(val message: String, val iconResId: Int, val actionLabel: String? = null)