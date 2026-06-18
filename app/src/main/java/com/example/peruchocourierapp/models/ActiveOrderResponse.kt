package com.example.peruchocourierapp.models

data class ActiveOrderResponse(
    val success: Boolean,
    val order: Order? = null,
    val message: String? = null
)