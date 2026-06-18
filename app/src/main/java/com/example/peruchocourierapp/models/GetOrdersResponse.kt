package com.example.peruchocourierapp.models

data class GetOrdersResponse(
    val success: Boolean,
    val orders: List<Order> = emptyList(),
    val message: String? = null
)