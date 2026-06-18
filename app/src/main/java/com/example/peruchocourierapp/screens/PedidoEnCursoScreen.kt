package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.utils.obtenerRuta
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
private val DriverBlue = Color(0xFF1A4FBF)
private val DriverBg = Color(0xFFF4F6FB)
private val DriverText = Color(0xFF1A2340)
private val DriverMuted = Color(0xFF6B7A99)
private val DriverBorder = Color(0xFFE8ECF4)
private val DriverGreen = Color(0xFF22C55E)
private val DriverRed = Color(0xFFE02020)

private fun normalizarEstadoPedido(estado: String?): String {
    return when ((estado ?: "").trim().lowercase()) {
        "recogiendo" -> "recogido"
        "en_transito" -> "en_camino"
        else -> (estado ?: "").trim().lowercase()
    }
}

@Composable
fun PedidoEnCursoScreen(
    navController: NavController,
    driverEmailParam: String = ""
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val driverEmail = remember(driverEmailParam) {
        driverEmailParam.ifBlank {
            sessionManager.getUserEmail()?.trim().orEmpty()
        }
    }

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var estadoPendiente by remember { mutableStateOf("") }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPositionState = rememberCameraPositionState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    fun cargarPedidoActivo() {
        if (driverEmail.isBlank()) {
            isLoading = false
            activeOrder = null
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            return
        }

        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getActiveOrder(driverEmail)
            .enqueue(object : Callback<ActiveOrderResponse> {
                override fun onResponse(
                    call: Call<ActiveOrderResponse>,
                    response: Response<ActiveOrderResponse>
                ) {
                    isLoading = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true && result.order != null) {
                        activeOrder = result.order
                        errorMessage = ""
                    } else {
                        activeOrder = null
                        errorMessage = result?.message ?: "No hay pedido activo"
                    }
                }

                override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                    isLoading = false
                    activeOrder = null
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    fun actualizarEstado(nuevoEstado: String) {
        val orderId = activeOrder?.id ?: return

        if (driverEmail.isBlank()) {
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            return
        }

        if (isUpdating) return

        isUpdating = true

        RetrofitClient.instance.updateOrderStatus(
            orderId = orderId,
            driverEmail = driverEmail,
            estado = nuevoEstado
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                isUpdating = false
                val result = response.body()

                if (response.isSuccessful && result?.success == true) {
                    errorMessage = ""

                    if (nuevoEstado == "entregado") {
                        navController.navigate("driver_lobby") {
                            popUpTo("pedido_en_curso") { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        cargarPedidoActivo()
                    }
                } else {
                    errorMessage = result?.message ?: "No se pudo actualizar el estado"
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                isUpdating = false
                errorMessage = "Error de conexión: ${t.message}"
            }
        })
    }

    fun obtenerUbicacionYEnviar() {
        if (!hasLocationPermission) return

        val orderId = activeOrder?.id ?: return
        if (driverEmail.isBlank()) return

        try {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(locationRequest, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude

                        RetrofitClient.instance.updateDriverLocation(
                            orderId = orderId,
                            driverEmail = driverEmail,
                            lat = currentLat.toString(),
                            lng = currentLng.toString()
                        ).enqueue(object : Callback<BasicResponse> {
                            override fun onResponse(
                                call: Call<BasicResponse>,
                                response: Response<BasicResponse>
                            ) {}

                            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {}
                        })
                    }
                }
        } catch (e: SecurityException) {
            errorMessage = "Permiso de ubicación requerido"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        cargarPedidoActivo()
    }

    LaunchedEffect(driverEmail) {
        while (driverEmail.isNotBlank()) {
            delay(30000)
            cargarPedidoActivo()
        }
    }

    LaunchedEffect(activeOrder?.id, hasLocationPermission, activeOrder?.estado) {
        while (
            activeOrder?.id != null &&
            normalizarEstadoPedido(activeOrder?.estado) != "entregado"
        ) {
            obtenerUbicacionYEnviar()
            delay(5000)
        }
    }

    LaunchedEffect(currentLat, currentLng, activeOrder?.id, activeOrder?.estado) {
        val estado = normalizarEstadoPedido(activeOrder?.estado)

        val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
        val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
        val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
        val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

        val origin = if (currentLat != 0.0 && currentLng != 0.0) {
            "$currentLat,$currentLng"
        } else {
            null
        }

        val destination = when (estado) {
            "asignado" -> {
                if (pickupLat != null && pickupLng != null) "$pickupLat,$pickupLng" else null
            }

            "recogido", "en_camino" -> {
                if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
            }

            else -> null
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

    var centeredOnce by remember { mutableStateOf(false) }

    LaunchedEffect(currentLat, currentLng) {
        if (!centeredOnce && currentLat != 0.0 && currentLng != 0.0) {
            centeredOnce = true
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLat, currentLng),
                    16f
                )
            )
        }
    }

    val estadoActual = normalizarEstadoPedido(activeOrder?.estado)

    val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
    val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
    val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
    val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

    val pickupPoint =
        if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null

    val dropPoint =
        if (dropLat != null && dropLng != null) LatLng(dropLat, dropLng) else null

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.56f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (pickupPoint != null && estadoActual == "asignado") {
                    Marker(
                        state = MarkerState(pickupPoint),
                        title = "Recojo"
                    )
                }

                if (dropPoint != null && estadoActual != "entregado") {
                    Marker(
                        state = MarkerState(dropPoint),
                        title = "Entrega"
                    )
                }

                if (currentLat != 0.0 && currentLng != 0.0) {
                    Marker(
                        state = MarkerState(LatLng(currentLat, currentLng)),
                        title = "Repartidor"
                    )
                }

                if (ruta.isNotEmpty()) {
                    Polyline(
                        points = ruta,
                        color = Color(0xFF00B7FF),
                        width = 10f
                    )
                } else if (pickupPoint != null && dropPoint != null) {
                    Polyline(
                        points = listOf(pickupPoint, dropPoint),
                        color = Color(0xFF00B7FF),
                        width = 8f
                    )
                }
            }

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = DriverBlue
                )
            }

            IconButton(
                onClick = {
                    if (currentLat != 0.0 && currentLng != 0.0) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(currentLat, currentLng),
                                17f
                            )
                        )
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación",
                    tint = DriverBlue
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.44f)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            colors = CardDefaults.cardColors(containerColor = DriverBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = DriverBlue,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Pedido en curso",
                        color = DriverText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DriverBlue)
                        }
                    }

                    errorMessage.isNotEmpty() -> {
                        Text(
                            text = errorMessage,
                            color = DriverRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    activeOrder != null -> {
                        PedidoEnCursoContenidoRedisenado(
                            activeOrder = activeOrder!!,
                            isUpdating = isUpdating,
                            onVerDetalles = { showDetailsDialog = true },
                            onConfirmarEstado = { estado ->
                                estadoPendiente = estado
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar cambio") },
            text = {
                Text("¿Seguro que deseas marcar este pedido como ${estadoPendiente.replace("_", " ")}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        actualizarEstado(estadoPendiente)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Sí, confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDetailsDialog && activeOrder != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Detalles del pedido") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetalleTexto("Recojo", activeOrder!!.pickup_address ?: "-")
                    DetalleTexto("Entrega", activeOrder!!.dropoff_address ?: "-")
                    DetalleTexto("Descripción", activeOrder!!.descripcion ?: "-")
                    DetalleTexto("Categoría", activeOrder!!.categoria ?: "-")
                    DetalleTexto("Tamaño del paquete", activeOrder!!.tamano_paquete ?: "-")
                    DetalleTexto("Distancia", "${activeOrder!!.distancia_km ?: "-"} km")
                    DetalleTexto("Método de pago", activeOrder!!.metodo_pago ?: "-")
                    DetalleTexto("Total", "S/ ${activeOrder!!.total ?: "-"}")
                    DetalleTexto("Comentarios repartidor", activeOrder!!.comentarios_repartidor ?: "-")
                    val fotoUrl = obtenerUrlFotoPaquete(activeOrder!!.foto_paquete)

                    if (fotoUrl != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Foto del paquete",
                            fontSize = 12.sp,
                            color = DriverMuted,
                            fontWeight = FontWeight.Bold
                        )

                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto del paquete",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    DetalleTexto("Teléfono remitente", activeOrder!!.telefono_remitente ?: "-")
                    DetalleTexto("Teléfono destinatario", activeOrder!!.telefono_destinatario ?: "-")
                    DetalleTexto("Estado", normalizarEstadoPedido(activeOrder!!.estado).replace("_", " "))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun PedidoEnCursoContenidoRedisenado(
    activeOrder: Order,
    isUpdating: Boolean,
    onVerDetalles: () -> Unit,
    onConfirmarEstado: (String) -> Unit
) {
    val estado = normalizarEstadoPedido(activeOrder.estado)

    RouteLinePedido(
        pickup = activeOrder.pickup_address ?: "-",
        dropoff = activeOrder.dropoff_address ?: "-"
    )

    InfoBoxPedido(
        metodoPago = activeOrder.metodo_pago ?: "-",
        total = activeOrder.total ?: "-",
        distanciaKm = activeOrder.distancia_km ?: "-"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalles() }
    ) {
        Text(
            text = "DETALLES DE LA SOLICITUD",
            color = DriverMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = activeOrder.descripcion ?: "Sin detalles",
                color = DriverMuted,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    EstadoBadgePedido(estado)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onConfirmarEstado("recogido") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "asignado",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DriverBlue,
                disabledContainerColor = Color(0xFFE5E7EB),
                disabledContentColor = Color(0xFF9CA3AF)
            )
        ) {
            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar recogido",
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = { onConfirmarEstado("en_camino") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "recogido",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE8EFFE),
                contentColor = DriverBlue,
                disabledContainerColor = Color(0xFFE5E7EB),
                disabledContentColor = Color(0xFF9CA3AF)
            )
        ) {
            Icon(Icons.Default.PedalBike, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar en camino",
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = { onConfirmarEstado("entregado") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "en_camino",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF059669),
                disabledContainerColor = Color(0xFFE5E7EB),
                disabledContentColor = Color(0xFF9CA3AF)
            )
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar entregado",
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun RouteLinePedido(
    pickup: String,
    dropoff: String
) {
    Column {
        RouteRowPedido(DriverGreen, pickup)

        Box(
            modifier = Modifier
                .padding(start = 4.5.dp, top = 3.dp, bottom = 3.dp)
                .width(1.5.dp)
                .height(14.dp)
                .background(DriverBorder)
        )

        RouteRowPedido(DriverRed, dropoff)
    }
}

@Composable
private fun RouteRowPedido(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = DriverText,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoBoxPedido(
    metodoPago: String,
    total: String,
    distanciaKm: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRowPedido("Método de pago", metodoPago, DriverBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DriverBorder)
        )

        InfoRowPedido("Distancia", "$distanciaKm km", DriverBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DriverBorder)
        )

        InfoRowPedido("Recargo total", "S/ $total", DriverText)
    }
}

@Composable
private fun InfoRowPedido(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = DriverMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EstadoBadgePedido(estado: String) {
    val estadoLower = normalizarEstadoPedido(estado)

    val badgeColor = when (estadoLower) {
        "asignado" -> Color(0xFFD1FAE5)
        "recogido" -> Color(0xFFFFF4E8)
        "en_camino" -> Color(0xFFE8EFFE)
        "entregado" -> Color(0xFFD1FAE5)
        else -> Color(0xFFD1FAE5)
    }

    val textColor = when (estadoLower) {
        "asignado" -> Color(0xFF065F46)
        "recogido" -> Color(0xFFD97706)
        "en_camino" -> DriverBlue
        "entregado" -> Color(0xFF059669)
        else -> Color(0xFF065F46)
    }

    val texto = when (estadoLower) {
        "en_camino" -> "En camino"
        else -> estadoLower.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "ESTADO:",
            color = DriverMuted,
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
                text = texto,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun DetalleTexto(
    titulo: String,
    valor: String
) {
    Column {
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = DriverMuted,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = valor,
            fontSize = 14.sp,
            color = DriverText,
            fontWeight = FontWeight.SemiBold
        )
    }
}
private fun obtenerUrlFotoPaquete(foto: String?): String? {

    if (foto.isNullOrBlank()) return null

    return when {
        foto.startsWith("http") ->
            foto

        foto.startsWith("uploads/") ->
            "https://peruchocourier.com/perucho_api/$foto"

        else ->
            "https://peruchocourier.com/perucho_api/uploads/paquetes/$foto"
    }
}