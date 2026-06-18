package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.DriverLocationResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.utils.obtenerRuta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val TrackBlue = Color(0xFF1A4FBF)
private val TrackBg = Color(0xFFF4F6FB)
private val TrackText = Color(0xFF1A2340)
private val TrackMuted = Color(0xFF6B7A99)
private val TrackBorder = Color(0xFFE8ECF4)
private val TrackGreen = Color(0xFF22C55E)
private val TrackRed = Color(0xFFE02020)

@Composable
fun SeguimientoPedidoScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var driverLat by remember { mutableDoubleStateOf(0.0) }
    var driverLng by remember { mutableDoubleStateOf(0.0) }
    var currentStatus by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState()

    fun cargarPedidoActivoCliente() {
        val userEmail = sessionManager.getUserEmail()

        if (userEmail.isNullOrEmpty()) {
            errorMessage = "No se encontró la sesión del cliente"
            isLoading = false
            return
        }

        RetrofitClient.instance.getMyActiveOrder(userEmail)
            .enqueue(object : Callback<ActiveOrderResponse> {
                override fun onResponse(
                    call: Call<ActiveOrderResponse>,
                    response: Response<ActiveOrderResponse>
                ) {
                    isLoading = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true && result.order != null) {
                        activeOrder = result.order
                        currentStatus = result.order.estado ?: ""
                        errorMessage = ""
                    } else {
                        activeOrder = null
                        errorMessage = result?.message ?: "No hay pedido activo"
                    }
                }

                override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    fun cargarUbicacionRepartidor(orderId: Int) {
        RetrofitClient.instance.getDriverLocation(orderId)
            .enqueue(object : Callback<DriverLocationResponse> {
                override fun onResponse(
                    call: Call<DriverLocationResponse>,
                    response: Response<DriverLocationResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        driverLat = result.driver_lat?.toDoubleOrNull() ?: 0.0
                        driverLng = result.driver_lng?.toDoubleOrNull() ?: 0.0
                        currentStatus = result.estado ?: currentStatus
                    }
                }

                override fun onFailure(call: Call<DriverLocationResponse>, t: Throwable) {}
            })
    }

    LaunchedEffect(Unit) {
        cargarPedidoActivoCliente()
    }

    LaunchedEffect(activeOrder?.id, currentStatus) {
        while (
            activeOrder?.id != null &&
            normalizarEstadoSeguimiento(currentStatus) != "entregado"
        ) {
            activeOrder?.id?.let { cargarUbicacionRepartidor(it) }
            delay(2000)
        }
    }

    LaunchedEffect(driverLat, driverLng, activeOrder?.id, currentStatus) {
        val estado = normalizarEstadoSeguimiento(currentStatus)

        val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
        val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
        val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
        val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

        val origin = when {
            driverLat != 0.0 && driverLng != 0.0 -> "$driverLat,$driverLng"
            pickupLat != null && pickupLng != null -> "$pickupLat,$pickupLng"
            else -> null
        }

        val destination = when (estado) {
            "asignado" -> {
                if (pickupLat != null && pickupLng != null) "$pickupLat,$pickupLng" else null
            }
            "recogido", "en_camino" -> {
                if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
            }
            else -> {
                if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
            }
        }

        ruta = if (origin != null && destination != null) {
            withContext(Dispatchers.IO) {
                obtenerRuta(
                    origin = origin,
                    destination = destination
                )
            }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(driverLat, driverLng, activeOrder?.pickup_lat, activeOrder?.pickup_lng) {
        when {
            driverLat != 0.0 && driverLng != 0.0 -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(driverLat, driverLng),
                        16f
                    )
                )
            }

            activeOrder?.pickup_lat?.toDoubleOrNull() != null &&
                    activeOrder?.pickup_lng?.toDoubleOrNull() != null -> {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            activeOrder!!.pickup_lat!!.toDouble(),
                            activeOrder!!.pickup_lng!!.toDouble()
                        ),
                        15f
                    )
                )
            }
        }
    }

    val estadoActual = normalizarEstadoSeguimiento(currentStatus)

    val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
    val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
    val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
    val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

    val pickupPoint =
        if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null

    val dropPoint =
        if (dropLat != null && dropLng != null) LatLng(dropLat, dropLng) else null

    val driverPoint =
        if (driverLat != 0.0 && driverLng != 0.0) LatLng(driverLat, driverLng) else null

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            if (pickupPoint != null && estadoActual == "asignado") {
                Marker(
                    state = MarkerState(position = pickupPoint),
                    title = "Recojo"
                )
            }

            if (dropPoint != null && estadoActual != "entregado") {
                Marker(
                    state = MarkerState(position = dropPoint),
                    title = "Entrega"
                )
            }

            if (driverPoint != null) {
                Marker(
                    state = MarkerState(position = driverPoint),
                    title = "Repartidor"
                )
            }

            val fallbackPoints = when {
                driverPoint != null && estadoActual == "asignado" && pickupPoint != null -> {
                    listOf(driverPoint, pickupPoint)
                }

                driverPoint != null && dropPoint != null -> {
                    listOf(driverPoint, dropPoint)
                }

                pickupPoint != null && dropPoint != null -> {
                    listOf(pickupPoint, dropPoint)
                }

                else -> emptyList()
            }

            if (ruta.isNotEmpty()) {
                Polyline(
                    points = ruta,
                    color = TrackBlue,
                    width = 10f
                )
            } else if (fallbackPoints.size == 2) {
                Polyline(
                    points = fallbackPoints,
                    color = TrackBlue,
                    width = 8f
                )
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TrackBlue
            )
        }

        IconButton(
            onClick = {
                driverPoint?.let {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(it, 16f)
                    )
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar repartidor",
                tint = TrackBlue
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(
                containerColor = TrackBg.copy(alpha = 0.98f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = TrackBlue,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Seguimiento del pedido",
                        color = TrackText,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                when {
                    isLoading -> {
                        Text(
                            text = "Cargando...",
                            color = TrackMuted,
                            fontSize = 16.sp
                        )
                    }

                    errorMessage.isNotEmpty() -> {
                        Text(
                            text = errorMessage,
                            color = TrackRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    activeOrder != null -> {
                        RouteInfoRowSeguimiento(
                            text = activeOrder?.pickup_address ?: "-",
                            markerColor = TrackGreen
                        )

                        RouteInfoRowSeguimiento(
                            text = activeOrder?.dropoff_address ?: "-",
                            markerColor = TrackRed
                        )

                        InfoPanelSeguimiento(
                            metodoPago = activeOrder?.metodo_pago ?: "-",
                            distancia = activeOrder?.distancia_km ?: "-",
                            total = activeOrder?.total ?: "-"
                        )

                        EstadoSeguimientoBadge(currentStatus)

                        Text(
                            text = if (driverPoint != null) {
                                "Seguimiento en vivo activo"
                            } else {
                                "Esperando ubicación del repartidor"
                            },
                            color = if (driverPoint != null) TrackBlue else TrackMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteInfoRowSeguimiento(
    text: String,
    markerColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = markerColor,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = TrackText,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoPanelSeguimiento(
    metodoPago: String,
    distancia: String,
    total: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRowSeguimiento("Método de pago", metodoPago, TrackBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TrackBorder)
        )

        InfoRowSeguimiento("Distancia", "$distancia km", TrackBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TrackBorder)
        )

        InfoRowSeguimiento("Total", "S/ $total", TrackText)
    }
}

@Composable
private fun InfoRowSeguimiento(
    leftLabel: String,
    rightValue: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leftLabel,
            color = TrackMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = rightValue,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EstadoSeguimientoBadge(status: String) {
    val estado = normalizarEstadoSeguimiento(status)

    val badgeColor = when (estado) {
        "asignado" -> Color(0xFFD1FAE5)
        "recogido" -> Color(0xFFFFF4E8)
        "en_camino" -> Color(0xFFE8EFFE)
        "entregado" -> Color(0xFFD1FAE5)
        "pendiente_pago" -> Color(0xFFFFF4E8)
        else -> Color(0xFFE8EFFE)
    }

    val textColor = when (estado) {
        "asignado" -> Color(0xFF065F46)
        "recogido" -> Color(0xFFD97706)
        "en_camino" -> TrackBlue
        "entregado" -> Color(0xFF059669)
        "pendiente_pago" -> Color(0xFFD97706)
        else -> TrackBlue
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "ESTADO:",
            color = TrackMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(badgeColor)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = textoEstadoSeguimiento(estado),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun normalizarEstadoSeguimiento(status: String?): String {
    return when ((status ?: "").trim().lowercase()) {
        "recogiendo" -> "recogido"
        "en_transito" -> "en_camino"
        else -> (status ?: "").trim().lowercase()
    }
}

private fun textoEstadoSeguimiento(status: String): String {
    return when (normalizarEstadoSeguimiento(status)) {
        "buscando" -> "Buscando repartidor"
        "asignado" -> "Repartidor asignado"
        "recogido" -> "Pedido recogido"
        "en_camino" -> "En camino"
        "entregado" -> "Entregado"
        "pendiente_pago" -> "Pendiente de pago"
        else -> status.ifBlank { "Sin estado" }
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }
}