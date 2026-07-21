package com.example.peruchocourierapp.models

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int? = null,
    val user_email: String? = null,
    val user_id: Int? = null,

    val tipo_envio: String? = null,

    // Pedido nacional / antiguo
    val origen: String? = null,
    val destino: String? = null,
    val descripcion: String? = null,
    val peso: String? = null,

    // Pedido nacional - mapa
    val pickup_address: String? = null,
    val pickup_lat: String? = null,
    val pickup_lng: String? = null,
    val dropoff_address: String? = null,
    val dropoff_lat: String? = null,
    val dropoff_lng: String? = null,

    // Pedido nacional - datos extra
    val telefono_remitente: String? = null,
    val telefono_destinatario: String? = null,
    val categoria: String? = null,
    val foto_paquete: String? = null,
    val comentarios_repartidor: String? = null,
    val tipo_vehiculo: String? = null,
    val distancia_km: String? = null,

    // Pedido internacional
    val web_compra: String? = null,
    val productos: String? = null,
    val precio_compra: String? = null,
    val tracking: String? = null,
    val fecha_llegada: String? = null,
    val peso_estimado: String? = null,
    val factura_pdf: String? = null,

    // Seguimiento visual
    val paso_visual: String? = null,
    val estado_nombre: String? = null,
    val estado_descripcion: String? = null,
    val permite_agregar_producto: Int? = 0,
    val productos_adicionales: List<InternationalProduct> = emptyList(),

    // Comunes
    val metodo_pago: String? = null,
    val total: String? = null,
    val estado: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,

    // Repartidor
    val driver_email: String? = null,
    val driver_id: String? = null,
    val driver_lat: String? = null,
    val driver_lng: String? = null,
    val driver_location_updated_at: String? = null,
    val tarifa_motorizado: String? = null,
    val destinatario_paga: Int? = 0,

    @SerializedName("estado_pago")
    val estadoPago: String? = null,

    @SerializedName("pago_metodo")
    val pagoMetodo: String? = null,

    @SerializedName("pago_observacion")
    val pagoObservacion: String? = null,

    @SerializedName("pago_solicitado_at")
    val pagoSolicitadoAt: String? = null,

    @SerializedName("pago_confirmado_at")
    val pagoConfirmadoAt: String? = null,

    @SerializedName("pago_confirmado_por")
    val pagoConfirmadoPor: String? = null,
)