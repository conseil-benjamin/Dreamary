package com.example.dreamary.models.states

import java.security.Timestamp

sealed class DreamResponse {
    object Loading : DreamResponse()
    data class Success(
        val dreamId: String? = null,
        val timestamp: Timestamp? = null
    ) : DreamResponse()
    data class Error(
        val message: String,
        val code: Int? = null
    ) : DreamResponse()
}