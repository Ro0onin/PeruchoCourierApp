package com.example.peruchocourierapp.models

data class GetDriverHistoryResponse(
    val success: Boolean,
    val message: String,
    val orders: List<EntregaItem>
)