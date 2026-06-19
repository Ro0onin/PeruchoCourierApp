package com.example.peruchocourierapp.models

data class ChatMessage(
    val id: Int,
    val order_id: Int,
    val sender_email: String,
    val receiver_email: String,
    val mensaje: String,
    val leido: Int,
    val created_at: String
)

data class GetChatMessagesResponse(
    val success: Boolean,
    val messages: List<ChatMessage> = emptyList(),
    val message: String? = null
)