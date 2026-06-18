package com.example.peruchocourierapp.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.DriverLocationResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.utils.obtenerRuta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val TrackBlue = Color(0xFF1A4FBF)
private val TrackNegro = Color(0xFF1A1A1A)
private val TrackBg = Color(0xFFFFFFFF)
private val TrackText = Color(0xFF1A2340)
private val TrackMuted = Color(0xFF888888)
private val TrackBorder = Color(0xFFE8ECF4)
private val TrackGrisF = Color(0xFFF5F5F5)
private val TrackRed = Color(0xFFE02020)

private val LIMA_CENTER = LatLng(-12.0464, -77.0428)

@Composable
fun SeguimientoPedidoClienteScreen(
    navController: NavController,
    orderIdParam: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var driverLat by remember { mutableDoubleStateOf(0.0) }
    var driverLng by remember { mutableDoubleStateOf(0.0) }
    var currentStatus by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var centeredOnce by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LIMA_CENTER, 14f)
    }

    val driverMarkerState = rememberMarkerState(position = LIMA_CENTER)
    val pickupMarkerState = rememberMarkerState(position = LIMA_CENTER)
    val dropMarkerState = rememberMarkerState(position = LIMA_CENTER)

    fun cargarPedidoActivo() {
        val userEmail = sessionManager.getUserEmail()

        if (userEmail.isNullOrEmpty()) {
            errorMessage = "No se encontró la sesión"
            isLoading = false
            return
        }

        RetrofitClient.instance.getOrderTracking(orderIdParam, userEmail)
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

                        val pickupLat = result.order.pickup_lat?.toDoubleOrNull()
                        val pickupLng = result.order.pickup_lng?.toDoubleOrNull()

                        if (pickupLat != null && pickupLng != null) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(pickupLat, pickupLng),
                                    15f
                                )
                            )
                        }
                    } else {
                        activeOrder = null
                        errorMessage = result?.message ?: "No tienes pedidos activos en este momento"
                    }
                }

                override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Sin conexión. Intenta de nuevo."
                }
            })
    }

    fun actualizarUbicacionRepartidor(orderId: Int) {
        RetrofitClient.instance.getDriverLocation(orderId)
            .enqueue(object : Callback<DriverLocationResponse> {
                override fun onResponse(
                    call: Call<DriverLocationResponse>,
                    response: Response<DriverLocationResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        val lat = result.driver_lat?.toDoubleOrNull() ?: 0.0
                        val lng = result.driver_lng?.toDoubleOrNull() ?: 0.0

                        if (lat != 0.0 && lng != 0.0) {
                            driverLat = lat
                            driverLng = lng

                            if (!centeredOnce) {
                                centeredOnce = true
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        16f
                                    )
                                )
                            }
                        }

                        currentStatus = result.estado ?: currentStatus
                    }
                }

                override fun onFailure(call: Call<DriverLocationResponse>, t: Throwable) {}
            })
    }

    LaunchedEffect(Unit) {
        cargarPedidoActivo()
    }

    LaunchedEffect(activeOrder?.id) {
        val orderId = activeOrder?.id ?: return@LaunchedEffect

        while (true) {
            if (normalizarEstado(currentStatus) == "entregado") break

            actualizarUbicacionRepartidor(orderId)
            delay(3_000)
        }
    }

    LaunchedEffect(activeOrder?.id, currentStatus, driverLat, driverLng)  {
        val estado = normalizarEstado(currentStatus)

        val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
        val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
        val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
        val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

        val origin = if (driverLat != 0.0 && driverLng != 0.0) {
            "$driverLat,$driverLng"
        } else if (pickupLat != null && pickupLng != null) {
            "$pickupLat,$pickupLng"
        } else {
            null
        }

        val destination = when (estado) {
            "asignado" -> if (pickupLat != null && pickupLng != null) "$pickupLat,$pickupLng" else null
            "recogido", "en_camino" -> if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
            "pendiente_pago" -> if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
            else -> if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
        }

        if (origin != null && destination != null) {
            ruta = withContext(Dispatchers.IO) {
                obtenerRuta(origin = origin, destination = destination)
            }
        }
    }

    val estado = normalizarEstado(currentStatus)

    val pickupPoint = activeOrder?.pickup_lat?.toDoubleOrNull()?.let { lat ->
        activeOrder?.pickup_lng?.toDoubleOrNull()?.let { lng ->
            LatLng(lat, lng)
        }
    }

    val dropPoint = activeOrder?.dropoff_lat?.toDoubleOrNull()?.let { lat ->
        activeOrder?.dropoff_lng?.toDoubleOrNull()?.let { lng ->
            LatLng(lat, lng)
        }
    }

    val driverPoint = if (driverLat != 0.0 && driverLng != 0.0) {
        LatLng(driverLat, driverLng)
    } else {
        null
    }

    LaunchedEffect(driverPoint) {
        driverPoint?.let {
            driverMarkerState.position = it
        }
    }

    LaunchedEffect(pickupPoint) {
        pickupPoint?.let {
            pickupMarkerState.position = it
        }
    }

    LaunchedEffect(dropPoint) {
        dropPoint?.let {
            dropMarkerState.position = it
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            if (pickupPoint != null) {
                Marker(
                    state = pickupMarkerState,
                    title = "Punto de recojo",
                    icon = bitmapDescriptorFromDrawableSafe(
                        context,
                        R.drawable.ic_pin_recojo,
                        90,
                        90
                    ),
                    anchor = Offset(0.5f, 1.0f)
                )
            }

            if (dropPoint != null && estado != "entregado") {
                Marker(
                    state = dropMarkerState,
                    title = "Punto de entrega",
                    icon = bitmapDescriptorFromDrawableSafe(
                        context,
                        R.drawable.ic_pin_entrega,
                        90,
                        90
                    ),
                    anchor = Offset(0.5f, 1.0f)
                )
            }

            if (driverPoint != null) {
                Marker(
                    state = driverMarkerState,
                    title = "Repartidor",
                    snippet = textoEstado(estado),
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }

            if (ruta.size >= 2) {
                Polyline(
                    points = ruta,
                    color = TrackBlue,
                    width = 10f
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 52.dp, start = 16.dp)
                .size(44.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TrackNegro
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 52.dp, end = 16.dp)
                .size(44.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    val target = driverPoint ?: pickupPoint ?: LIMA_CENTER
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(target, 16f)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar",
                    tint = TrackBlue
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = TrackBg,
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TrackBorder)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = TrackBlue,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Seguimiento del pedido",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrackText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    isLoading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = TrackBlue
                            )

                            Text(
                                text = "Buscando tu pedido activo...",
                                color = TrackMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    errorMessage.isNotEmpty() -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF0F0)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = TrackRed,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column {
                                    Text(
                                        text = "Sin pedido activo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TrackRed
                                    )

                                    Text(
                                        text = errorMessage,
                                        fontSize = 12.sp,
                                        color = TrackMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                cargarPedidoActivo()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrackNegro
                            )
                        ) {
                            Text(
                                text = "Reintentar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    activeOrder != null -> {
                        EstadoBadge(estado = currentStatus)

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = TrackGrisF,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DireccionRow(
                                    texto = activeOrder?.pickup_address ?: "-",
                                    label = "Recojo",
                                    icon = R.drawable.ic_pin_recojo
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .width(1.5.dp)
                                        .height(12.dp)
                                        .background(TrackBorder)
                                )

                                DireccionRow(
                                    texto = activeOrder?.dropoff_address ?: "-",
                                    label = "Entrega",
                                    icon = R.drawable.ic_pin_entrega
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InfoChip(
                                label = "Pago",
                                value = activeOrder?.metodo_pago ?: "-",
                                modifier = Modifier.weight(1f)
                            )

                            InfoChip(
                                label = "Distancia",
                                value = "${activeOrder?.distancia_km ?: "-"} km",
                                modifier = Modifier.weight(1f)
                            )

                            InfoChip(
                                label = "Total",
                                value = "S/ ${activeOrder?.total ?: "-"}",
                                valueColor = TrackBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedVisibility(visible = driverPoint != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8EFFE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 10.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(TrackBlue)
                                    )

                                    Text(
                                        text = "Seguimiento en vivo activo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TrackBlue
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = "Actualiza cada 3 seg",
                                        fontSize = 11.sp,
                                        color = TrackMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DireccionRow(
    texto: String,
    label: String,
    icon: Int
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(34.dp)
        )

        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TrackMuted
            )

            Text(
                text = texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrackText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TrackText
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = TrackGrisF,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TrackMuted,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val norm = normalizarEstado(estado)

    val bgColor = when (norm) {
        "asignado" -> Color(0xFFE8EFFE)
        "recogido" -> Color(0xFFFFF4E8)
        "en_camino" -> Color(0xFFDCFCE7)
        "entregado" -> Color(0xFFD1FAE5)
        "pendiente_pago" -> Color(0xFFFFF4E8)
        "buscando" -> Color(0xFFF5F5F5)
        else -> Color(0xFFF5F5F5)
    }

    val fgColor = when (norm) {
        "asignado" -> TrackBlue
        "recogido" -> Color(0xFFD97706)
        "en_camino" -> Color(0xFF16A34A)
        "entregado" -> Color(0xFF059669)
        "pendiente_pago" -> Color(0xFFD97706)
        else -> TrackMuted
    }

    val icono = when (norm) {
        "asignado" -> "🏍️ "
        "recogido" -> "📦 "
        "en_camino" -> "🚀 "
        "entregado" -> "✅ "
        "pendiente_pago" -> "💳 "
        "buscando" -> "🔍 "
        else -> "📋 "
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = "$icono${textoEstado(norm)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fgColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

private fun normalizarEstado(status: String?): String =
    when ((status ?: "").trim().lowercase()) {
        "recogiendo" -> "recogido"
        "en_transito" -> "en_camino"
        else -> (status ?: "").trim().lowercase()
    }

private fun textoEstado(status: String): String =
    when (normalizarEstado(status)) {
        "buscando" -> "Buscando repartidor"
        "asignado" -> "Repartidor asignado"
        "recogido" -> "Pedido recogido"
        "en_camino" -> "En camino a entrega"
        "entregado" -> "¡Entregado!"
        "pendiente_pago" -> "Pendiente de pago"
        else -> status.ifBlank { "Sin estado" }
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

private fun bitmapDescriptorFromDrawableSafe(
    context: android.content.Context,
    drawableId: Int,
    width: Int,
    height: Int
): BitmapDescriptor {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}