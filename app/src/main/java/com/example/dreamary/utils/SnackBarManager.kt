package com.example.dreamary.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.dreamary.R

object SnackbarManager {
    private val snackbarChannel = Channel<SnackbarMessage>(Channel.BUFFERED)
    val snackbarMessages = snackbarChannel.receiveAsFlow()

    suspend fun showMessage(message: String, type: SnackbarType, actionLabel: String? = null) {
        snackbarChannel.send(SnackbarMessage(message, type, actionLabel))
    }
}

data class SnackbarMessage(val message: String, val type: SnackbarType, val actionLabel: String? = null)

enum class SnackbarType(val iconResId: Int) {
    SUCCESS(R.drawable.success),
    ERROR(R.drawable.error),
    WARNING(R.drawable.warning),
    INFO(R.drawable.information)
}
