package com.example.peruchocourierapp.models

data class RouteDistanceResponse(
    val success: Boolean,
    val message: String? = null,
    val distance_km: Double? = null,
    val duration_min: Int? = null
)