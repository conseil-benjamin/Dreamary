package com.example.dreamary.models.entities

data class Share(
    var users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
)
