package com.example.peruchocourierapp.models

data class DriverDashboardResponse(
    val success: Boolean,
    val activos: Int,
    val disponibles: Int,
    val entregados: Int,
    val entregas_hoy: Int,
    val ganancias_hoy: Double
)