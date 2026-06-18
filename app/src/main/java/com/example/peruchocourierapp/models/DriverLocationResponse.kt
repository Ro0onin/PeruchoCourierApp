package com.example.peruchocourierapp.models

data class DriverLocationResponse(
    val success: Boolean,
    val driver_email: String? = null,
    val driver_lat: String? = null,
    val driver_lng: String? = null,
    val updated_at: String? = null,
    val estado: String? = null,
    val pickup_address: String? = null,
    val pickup_lat: String? = null,
    val pickup_lng: String? = null,
    val dropoff_address: String? = null,
    val dropoff_lat: String? = null,
    val dropoff_lng: String? = null,
    val metodo_pago: String? = null,
    val total: String? = null,
    val message: String? = null
)