package com.example.peruchocourierapp.models

data class ProfileResponse(
    val success: Boolean,
    val message: String?,
    val user: UserProfile?
)