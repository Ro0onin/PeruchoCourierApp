package com.example.peruchocourierapp.models

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int? = null,
    val phone: String? = null,
    val phone_twilio: String? = null,
    val requires_verification: Boolean? = false,
    val sms_status: String? = null,
    val sms_error: String? = null
)