package com.example.peruchocourierapp.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.peruchocourierapp.utils.obtenerRutaCompleta
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
import android.net.Uri
import android.util.Log
import com.example.peruchocourierapp.theme.ThemeManager
import com.google.android.gms.maps.model.MapStyleOptions

private val TrackBlue = Color(0xFF1A4FBF)
private val TrackRed = Color(0xFFE02020)

private data class TrackingColors(
    val screenBg: Color,
    val sheetBg: Color,
    val cardBg: Color,
    val chipBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val floatingBg: Color,
    val topIcon: Color,
    val errorBg: Color,
    val errorText: Color,
    val primaryButton: Color,
    val successBg: Color,
    val successText: Color,
    val warningBg: Color,
    val warningText: Color,
    val blueSoft: Color
)

@Composable
private fun trackingColors(): TrackingColors {
    val dark = isSystemInDarkTheme()

    return if (dark) {
        TrackingColors(
            screenBg = Color(0xFF0F172A),
            sheetBg = Color(0xFF111827),
            cardBg = Color(0xFF1F2937),
            chipBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            floatingBg = Color(0xFF111827).copy(alpha = 0.94f),
            topIcon = Color(0xFFF8FAFC),
            errorBg = Color(0xFF3F1717),
            errorText = Color(0xFFFFB4B4),
            primaryButton = Color(0xFFF8FAFC),
            successBg = Color(0xFF14532D),
            successText = Color(0xFFDCFCE7),
            warningBg = Color(0xFF451A03),
            warningText = Color(0xFFFBBF24),
            blueSoft = Color(0xFF172554)
        )
    } else {
        TrackingColors(
            screenBg = Color.White,
            sheetBg = Color.White,
            cardBg = Color.White,
            chipBg = Color(0xFFF5F5F5),
            border = Color(0xFFE8ECF4),
            text = Color(0xFF1A2340),
            muted = Color(0xFF888888),
            floatingBg = Color.White,
            topIcon = Color(0xFF1A1A1A),
            errorBg = Color(0xFFFFF0F0),
            errorText = TrackRed,
            primaryButton = Color(0xFF1A1A1A),
            successBg = Color(0xFFD1FAE5),
            successText = Color(0xFF059669),
            warningBg = Color(0xFFFFF4E8),
            warningText = Color(0xFFD97706),
            blueSoft = Color(0xFFE8EFFE)
        )
    }
}

private val LIMA_CENTER = LatLng(-12.0464, -77.0428)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoPedidoClienteScreen(
    navController: NavController,
    orderIdParam: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = trackingColors()

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var driverLat by remember { mutableDoubleStateOf(0.0) }
    var driverLng by remember { mutableDoubleStateOf(0.0) }
    var currentStatus by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var duracionMin by remember { mutableStateOf(0) }
    var centeredOnce by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LIMA_CENTER, 14f)
    }

    val driverMarkerState = rememberMarkerState(position = LIMA_CENTER)
    val pickupMarkerState = rememberMarkerState(position = LIMA_CENTER)
    val dropMarkerState = rememberMarkerState(position = LIMA_CENTER)

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

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
            delay(2_000)
        }
    }

    LaunchedEffect(activeOrder?.id, currentStatus, driverLat, driverLng) {
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
            val resultado = withContext(Dispatchers.IO) {
                obtenerRutaCompleta(
                    origin = origin,
                    destination = destination
                )
            }

            ruta = resultado.puntos
            duracionMin = resultado.duracionMin
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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 108.dp,
        sheetContainerColor = colors.sheetBg,
        sheetShadowElevation = 20.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(46.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(colors.border)
            )
        },
        sheetContent = {
            SeguimientoBottomSheetContent(
                colors = colors,
                navController = navController,
                isLoading = isLoading,
                errorMessage = errorMessage,
                activeOrder = activeOrder,
                currentStatus = currentStatus,
                duracionMin = duracionMin,
                driverPoint = driverPoint,
                onRetry = {
                    isLoading = true
                    cargarPedidoActivo()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                MapEffect(
                    ThemeManager.isDarkMode.value
                ) { map ->

                    if (ThemeManager.isDarkMode.value) {

                        map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                context,
                                R.raw.map_style_dark
                            )
                        )

                    } else {

                        map.setMapStyle(null)

                    }
                }
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
                    .background(colors.floatingBg)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.topIcon
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 52.dp, end = 16.dp)
                    .size(44.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colors.floatingBg)
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
        }
    }
}

@Composable
private fun SeguimientoBottomSheetContent(
    colors: TrackingColors,
    navController: NavController,
    isLoading: Boolean,
    errorMessage: String,
    activeOrder: Order?,
    currentStatus: String,
    driverPoint: LatLng?,
    onRetry: () -> Unit,
    duracionMin: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
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
                color = colors.text
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
                        color = colors.muted,
                        fontSize = 13.sp
                    )
                }
            }

            errorMessage.isNotEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.errorBg
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
                                color = colors.errorText
                            )

                            Text(
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = colors.muted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryButton,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color.White
                    )
                ) {
                    Text(
                        text = "Reintentar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            activeOrder != null -> {
                EstadoBadge(colors = colors, estado = currentStatus)

                Spacer(modifier = Modifier.height(12.dp))

                if (driverPoint != null && duracionMin in 0..10) {
                    LlegadaProximaCard(colors = colors, minutos = duracionMin)

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.chipBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DireccionRow(
                            colors = colors,
                            texto = activeOrder.pickup_address ?: "-",
                            label = "Recojo",
                            icon = R.drawable.ic_pin_recojo
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .width(1.5.dp)
                                .height(12.dp)
                                .background(colors.border)
                        )

                        DireccionRow(
                            colors = colors,
                            texto = activeOrder.dropoff_address ?: "-",
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
                        colors = colors,
                        label = "Pago",
                        value = activeOrder.metodo_pago ?: "-",
                        modifier = Modifier.weight(1f)
                    )

                    InfoChip(
                        colors = colors,
                        label = "Distancia",
                        value = "${activeOrder.distancia_km ?: "-"} km",
                        modifier = Modifier.weight(1f)
                    )

                    InfoChip(
                        colors = colors,
                        label = "Total",
                        value = "S/ ${activeOrder.total ?: "-"}",
                        valueColor = TrackBlue,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChip(
                        colors = colors,
                        label = "Tiempo",
                        value = "$duracionMin min",
                        valueColor = TrackBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        Log.d("CHAT_TEST", "Boton chat presionado")
                        Log.d("CHAT_DEBUG", "ORDER ID = ${activeOrder?.id}")
                        Log.d("CHAT_DEBUG", "DRIVER = ${activeOrder?.driver_email}")


                        val order = activeOrder ?: return@Button

                        val driverEmail =
                            order.driver_email ?: return@Button

                        navController.navigate(
                            "chat_pedido/${order.id}/${Uri.encode(driverEmail)}"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrackBlue
                    )
                ) {
                    Text(
                        text = "💬 Chat con repartidor",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LlegadaProximaCard(colors: TrackingColors, minutos: Int) {
    val titulo: String
    val texto: String
    val emoji: String
    val fondo: Color
    val colorTexto: Color

    when {
        minutos >= 6 -> {
            emoji = "🛵"
            titulo = "Tu repartidor está acercándose"
            texto = "Ya está en camino al punto de entrega."
            fondo = colors.blueSoft
            colorTexto = TrackBlue
        }

        minutos in 2..5 -> {
            emoji = "🏠"
            titulo = "¡Ya casi llega!"
            texto = "Tu repartidor está muy cerca de tu dirección."
            fondo = colors.successBg
            colorTexto = colors.successText
        }

        minutos == 1 -> {
            emoji = "🚪"
            titulo = "Prepárate para recibir tu pedido"
            texto = "El repartidor está a aproximadamente 1 minuto."
            fondo = colors.warningBg
            colorTexto = colors.warningText
        }

        else -> {
            emoji = "✅"
            titulo = "El repartidor llegó al destino"
            texto = "Revisa tu pedido y confirma la recepción."
            fondo = colors.successBg
            colorTexto = colors.successText
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = fondo,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colorTexto),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 21.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = colorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = texto,
                    color = colors.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 3.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.cardBg
                ) {
                    Text(
                        text = "⏱ Llegada estimada: $minutos min",
                        color = colorTexto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
@Composable
private fun DireccionRow(
    colors: TrackingColors,
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
                color = colors.muted
            )

            Text(
                text = texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(
    colors: TrackingColors,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.chipBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = colors.muted,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor ?: colors.text,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun EstadoBadge(colors: TrackingColors, estado: String) {
    val norm = normalizarEstado(estado)

    val bgColor = when (norm) {
        "asignado" -> colors.blueSoft
        "recogido" -> colors.warningBg
        "en_camino" -> colors.successBg
        "entregado" -> colors.successBg
        "pendiente_pago", "esperando_repartidor" -> colors.warningBg
        "buscando" -> colors.chipBg
        else -> colors.chipBg
    }

    val fgColor = when (norm) {
        "asignado" -> TrackBlue
        "recogido" -> colors.warningText
        "en_camino" -> colors.successText
        "entregado" -> colors.successText
        "pendiente_pago" -> colors.warningText
        else -> colors.muted
    }

    val icono = when (norm) {
        "asignado" -> "🏍️ "
        "recogido" -> "📦 "
        "en_camino" -> "🚀 "
        "entregado" -> "✅ "
        "pendiente_pago", "esperando_repartidor" -> "⏳ "
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
        "pendiente_pago", "esperando_repartidor" -> "Esperando repartidor"
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
