package com.example.peruchocourierapp.models

data class CallContactsResponse(
    val success: Boolean,
    val message: String? = null,
    val driver_phone: String? = null,
    val telefono_remitente: String? = null,
    val telefono_destinatario: String? = null
)