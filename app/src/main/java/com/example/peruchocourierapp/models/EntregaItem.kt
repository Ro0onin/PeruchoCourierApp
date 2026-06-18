package com.example.peruchocourierapp.models

data class EntregaItem(
    val id: Int,
    val pickupAddress: String,
    val dropoffAddress: String,
    val total: String,
    val distanciaKm: String,
    val metodoPago: String,
    val fechaEntrega: String
)