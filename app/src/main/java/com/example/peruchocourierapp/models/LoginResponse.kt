package com.example.peruchocourierapp.models

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val name: String?,
    val dni: String?,
    val email: String?,
    val phone: String?,
    val role: String?,
    val requires_verification: Boolean? = false,
    val phone_verified: Int? = null,
    val verification_pending: Int? = null
)