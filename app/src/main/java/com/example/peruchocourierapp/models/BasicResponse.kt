package com.example.peruchocourierapp.models

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    val success: Boolean,
    val message: String,

    @SerializedName("envio_id")
    val orderId: Int? = null
)