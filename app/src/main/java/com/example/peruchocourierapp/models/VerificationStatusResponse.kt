package com.example.peruchocourierapp.models

data class VerificationStatusResponse(
    val success: Boolean,
    val phone_verified: Int,
    val identity_status: String
)