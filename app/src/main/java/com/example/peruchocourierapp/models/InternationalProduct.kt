package com.example.peruchocourierapp.models

data class InternationalProduct(
    val id: Int? = null,
    val envio_id: Int? = null,
    val web_compra: String? = null,
    val producto: String? = null,
    val tracking: String? = null,
    val precio_compra: String? = null,
    val peso_estimado: String? = null,
    val comentario: String? = null,
    val created_at: String? = null,
    val factura_pdf: String?,
)