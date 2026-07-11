package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
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
import com.example.peruchocourierapp.theme.ThemeManager
import com.example.peruchocourierapp.utils.obtenerRutaCompleta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
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

private data class SeguimientoColors(
    val sheetBg: Color,
    val cardBg: Color,
    val chipBg: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val mapButtonBg: Color,
    val errorBg: Color,
    val assignedBg: Color,
    val pickedBg: Color,
    val onWayBg: Color,
    val deliveredBg: Color,
    val pendingBg: Color,
    val assignedText: Color,
    val pickedText: Color,
    val onWayText: Color,
    val deliveredText: Color,
    val pendingText: Color
)

@Composable
private fun seguimientoColors(): SeguimientoColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        SeguimientoColors(
            sheetBg = Color(0xFF0F172A).copy(alpha = 0.98f),
            cardBg = Color(0xFF111827),
            chipBg = Color(0xFF1F2937),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            border = Color(0xFF334155),
            mapButtonBg = Color(0xFF111827).copy(alpha = 0.95f),
            errorBg = Color(0xFF450A0A),
            assignedBg = Color(0xFF172554),
            pickedBg = Color(0xFF451A03),
            onWayBg = Color(0xFF172554),
            deliveredBg = Color(0xFF14532D),
            pendingBg = Color(0xFF451A03),
            assignedText = Color(0xFF93C5FD),
            pickedText = Color(0xFFFBBF24),
            onWayText = Color(0xFF93C5FD),
            deliveredText = Color(0xFFDCFCE7),
            pendingText = Color(0xFFFBBF24)
        )
    } else {
        SeguimientoColors(
            sheetBg = TrackBg.copy(alpha = 0.98f),
            cardBg = Color.White,
            chipBg = Color.White,
            text = TrackText,
            muted = TrackMuted,
            border = TrackBorder,
            mapButtonBg = Color.White.copy(alpha = 0.95f),
            errorBg = Color(0xFFFFF0F0),
            assignedBg = Color(0xFFD1FAE5),
            pickedBg = Color(0xFFFFF4E8),
            onWayBg = Color(0xFFE8EFFE),
            deliveredBg = Color(0xFFD1FAE5),
            pendingBg = Color(0xFFFFF4E8),
            assignedText = Color(0xFF065F46),
            pickedText = Color(0xFFD97706),
            onWayText = TrackBlue,
            deliveredText = Color(0xFF059669),
            pendingText = Color(0xFFD97706)
        )
    }
}

@Composable
fun SeguimientoPedidoScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = seguimientoColors()

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var driverLat by remember { mutableDoubleStateOf(0.0) }
    var driverLng by remember { mutableDoubleStateOf(0.0) }
    var currentStatus by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var duracionMin by remember { mutableStateOf(0) }

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

        if (origin != null && destination != null) {
            val resultado = withContext(Dispatchers.IO) {
                obtenerRutaCompleta(
                    origin = origin,
                    destination = destination
                )
            }

            ruta = resultado.puntos
            duracionMin = resultado.duracionMin
        } else {
            ruta = emptyList()
            duracionMin = 0
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
            MapEffect(
                ThemeManager.isDarkMode.value
            ) { map ->

                if (ThemeManager.isDarkMode.value) {

                    map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            com.example.peruchocourierapp.R.raw.map_style_dark
                        )
                    )

                } else {

                    map.setMapStyle(null)

                }
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
                .background(colors.mapButtonBg)
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
                .background(colors.mapButtonBg)
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
                containerColor = colors.sheetBg
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
                        color = colors.text,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black
                    )

                }

                when {
                    isLoading -> {
                        Text(
                            text = "Cargando...",
                            color = colors.muted,
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
                            colors = colors,
                            text = activeOrder?.pickup_address ?: "-",
                            markerColor = TrackGreen
                        )

                        RouteInfoRowSeguimiento(
                            colors = colors,
                            text = activeOrder?.dropoff_address ?: "-",
                            markerColor = TrackRed
                        )

                        InfoPanelSeguimiento(
                            colors = colors,
                            metodoPago = activeOrder?.metodo_pago ?: "-",
                            distancia = activeOrder?.distancia_km ?: "-",
                            total = activeOrder?.total ?: "-",
                            duracionMin = duracionMin
                        )

                        EstadoSeguimientoBadge(currentStatus, colors)


                    }
                }
            }
        }
    }
}


@Composable
private fun RouteInfoRowSeguimiento(
    colors: SeguimientoColors,
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
            color = colors.text,
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
    colors: SeguimientoColors,
    metodoPago: String,
    distancia: String,
    total: String,
    duracionMin: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniInfoSeguimiento(
            colors = colors,
            label = "Pago",
            value = metodoPago,
            modifier = Modifier.weight(1f)
        )

        MiniInfoSeguimiento(
            colors = colors,
            label = "Distancia",
            value = "$distancia km",
            modifier = Modifier.weight(1f)
        )

        MiniInfoSeguimiento(
            colors = colors,
            label = "Total",
            value = "S/ $total",
            modifier = Modifier.weight(1f)
        )

        MiniInfoSeguimiento(
            colors = colors,
            label = "Tiempo",
            value = if (duracionMin > 0) "$duracionMin min" else "--",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniInfoSeguimiento(
    colors: SeguimientoColors,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(colors.chipBg, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = value,
            color = TrackBlue,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EstadoSeguimientoBadge(status: String, colors: SeguimientoColors) {
    val estado = normalizarEstadoSeguimiento(status)

    val badgeColor = when (estado) {
        "asignado" -> colors.assignedBg
        "recogido" -> colors.pickedBg
        "en_camino" -> colors.onWayBg
        "entregado" -> colors.deliveredBg
        "pendiente_pago" -> colors.pendingBg
        else -> colors.onWayBg
    }

    val textColor = when (estado) {
        "asignado" -> colors.assignedText
        "recogido" -> colors.pickedText
        "en_camino" -> colors.onWayText
        "entregado" -> colors.deliveredText
        "pendiente_pago" -> colors.pendingText
        else -> colors.onWayText
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "ESTADO:",
            color = colors.muted,
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
        "en_camino" -> "En camino a entrega"
        "entregado" -> "Entregado"
        "pendiente_pago" -> "Pendiente de pago"
        else -> status.ifBlank { "Sin estado" }
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }
}