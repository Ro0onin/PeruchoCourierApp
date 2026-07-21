package com.example.peruchocourierapp.screens

import androidx.compose.foundation.clickable
import com.google.android.gms.maps.model.LatLng


import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.theme.ThemeManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val DriverBlue = Color(0xFF1A4FBF)
private val DriverBlueDark = Color(0xFF0D3280)
private val DriverBlueMid = Color(0xFF2D6BE4)
private val DriverBg = Color(0xFFF4F6FB)
private val DriverText = Color(0xFF1A2340)
private val DriverMuted = Color(0xFF6B7A99)
private val DriverBorder = Color(0xFFE8ECF4)
private val DriverGreen = Color(0xFF22C55E)
private val DriverRed = Color(0xFFE02020)

private data class AvailableOrdersColors(
    val screenBg: Color,
    val cardBg: Color,
    val fieldBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val subtleText: Color,
    val iconSoftBg: Color,
    val emptyPhotoBg: Color,
    val paymentBg: Color,
    val bcpBg: Color,
    val bcpText: Color,
    val errorText: Color,
    val dialogBg: Color
)

@Composable
private fun availableOrdersColors(): AvailableOrdersColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        AvailableOrdersColors(
            screenBg = Color(0xFF0F172A),
            cardBg = Color(0xFF111827),
            fieldBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            subtleText = Color(0xFF94A3B8),
            iconSoftBg = Color(0xFF172554),
            emptyPhotoBg = Color(0xFF1F2937),
            paymentBg = Color(0xFF172554),
            bcpBg = Color(0xFF451A03),
            bcpText = Color(0xFFFBBF24),
            errorText = Color(0xFFFFB4B4),
            dialogBg = Color(0xFF111827)
        )
    } else {
        AvailableOrdersColors(
            screenBg = DriverBg,
            cardBg = Color.White,
            fieldBg = Color(0xFFF9FAFB),
            border = DriverBorder,
            text = DriverText,
            muted = DriverMuted,
            subtleText = Color(0xFFB0BAD0),
            iconSoftBg = Color(0xFFE8EFFE),
            emptyPhotoBg = Color(0xFFF4F6FB),
            paymentBg = Color(0xFFE8EFFE),
            bcpBg = Color(0xFFFFF4E8),
            bcpText = Color(0xFFD97706),
            errorText = DriverRed,
            dialogBg = Color.White
        )
    }
}

@Composable
fun PedidosDisponiblesScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = availableOrdersColors()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var acceptingOrderId by remember { mutableStateOf<Int?>(null) }

    fun cargarPedidos() {
        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getAvailableOrders()
            .enqueue(object : Callback<GetOrdersResponse> {
                override fun onResponse(
                    call: Call<GetOrdersResponse>,
                    response: Response<GetOrdersResponse>
                ) {
                    isLoading = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        orders = result.orders
                        errorMessage = ""
                    } else {
                        orders = emptyList()
                        errorMessage = result?.message ?: "No se pudieron cargar los pedidos"
                    }
                }

                override fun onFailure(call: Call<GetOrdersResponse>, t: Throwable) {
                    isLoading = false
                    orders = emptyList()
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    fun aceptarPedido(order: Order) {
        val driverEmail = sessionManager.getUserEmail()?.trim().orEmpty()
        val orderId = order.id

        if (driverEmail.isBlank()) {
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            showErrorDialog = true
            return
        }

        if (orderId == null) {
            errorMessage = "Pedido inválido"
            showErrorDialog = true
            return
        }

        acceptingOrderId = orderId
        errorMessage = ""

        RetrofitClient.instance.acceptOrder(orderId, driverEmail)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    acceptingOrderId = null
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        navController.navigate("pedido_en_curso/${Uri.encode(driverEmail)}")
                    } else {
                        errorMessage = result?.message ?: "No se pudo aceptar el pedido"
                        showErrorDialog = true
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    acceptingOrderId = null
                    errorMessage = "Error: ${t.message}"
                    showErrorDialog = true
                }
            })
    }

    LaunchedEffect(Unit) {
        cargarPedidos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
            .navigationBarsPadding()
    ) {
        HeaderPedidosDisponibles(
            count = orders.size,
            onBack = { navController.popBackStack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DriverBlue)
                        }
                    }
                }

                errorMessage.isNotBlank() && orders.isEmpty() -> {
                    item {
                        Text(
                            text = errorMessage,
                            color = colors.errorText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                        )
                    }
                }

                orders.isEmpty() -> {
                    item {
                        Text(
                            text = "No hay pedidos disponibles",
                            color = colors.muted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                        )
                    }
                }

                else -> {
                    items(orders) { order ->
                        PedidoDisponibleCard(
                            navController = navController,
                            colors = colors,
                            order = order,
                            isAccepting = acceptingOrderId == order.id,
                            onAccept = { aceptarPedido(order) }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { cargarPedidos() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, DriverBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DriverBlue
                            )
                        ) {
                            Text(
                                text = "Actualizar lista",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "No se pudo aceptar",
                    color = colors.text,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    color = colors.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Entendido")
                }
            },
            containerColor = colors.dialogBg
        )
    }
}

@Composable
private fun HeaderPedidosDisponibles(
    count: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                if (ThemeManager.isDarkMode.value) {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF111827),
                            Color(0xFF1E293B)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(DriverBlueDark, DriverBlue, DriverBlueMid)
                    )
                }
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Volver",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Pedidos disponibles",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(DriverRed)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$count nuevos",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PedidoDisponibleCard(
    navController: NavController,
    colors: AvailableOrdersColors,
    order: Order,
    isAccepting: Boolean,
    onAccept: () -> Unit
) {
    val context = LocalContext.current
    var showProductPreview by remember(order.id) {
        mutableStateOf(false)
    }

    val fotoUrl = remember(order.foto_paquete) {
        obtenerUrlFotoPaquete(order.foto_paquete)
    }

    val pickupPoint = remember(order.pickup_lat, order.pickup_lng) {
        val lat = order.pickup_lat?.toDoubleOrNull()
        val lng = order.pickup_lng?.toDoubleOrNull()

        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            LatLng(lat, lng)
        } else {
            null
        }
    }

    val dropoffPoint = remember(order.dropoff_lat, order.dropoff_lng) {
        val lat = order.dropoff_lat?.toDoubleOrNull()
        val lng = order.dropoff_lng?.toDoubleOrNull()

        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            LatLng(lat, lng)
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = tiempoRelativo(order.created_at),
                    color = colors.subtleText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            RouteRow(colors, DriverGreen, order.pickup_address ?: "Recojo no disponible", true)
            Box(
                modifier = Modifier
                    .padding(start = 4.5.dp, top = 3.dp, bottom = 3.dp)
                    .width(1.5.dp)
                    .height(18.dp)
                    .background(colors.border)
            )
            RouteRow(colors, DriverRed, order.dropoff_address ?: "Entrega no disponible", true)

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "S/ ${order.total ?: "-"}",
                    color = DriverGreen,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.LocationOn, null, tint = DriverRed, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${order.distancia_km ?: "-"} km",
                    color = colors.subtleText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.weight(1f))
                PaymentBadge(order.metodo_pago ?: "-")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val orderId = order.id ?: return@OutlinedButton

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.apply {
                            set("preview_order_id", orderId)
                            set("preview_total", order.total ?: "0.00")
                            set("preview_distance", order.distancia_km ?: "0")
                            set("preview_pickup_address", order.pickup_address ?: "")
                            set("preview_dropoff_address", order.dropoff_address ?: "")
                            set("preview_pickup_lat", pickupPoint?.latitude)
                            set("preview_pickup_lng", pickupPoint?.longitude)
                            set("preview_dropoff_lat", dropoffPoint?.latitude)
                            set("preview_dropoff_lng", dropoffPoint?.longitude)
                        }

                    navController.navigate("vista_ruta_pedido")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DriverBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Ver ruta antes de aceptar",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = fotoUrl != null) {
                        showProductPreview = true
                    },
                color = colors.fieldBg,
                shape = RoundedCornerShape(11.dp)
            ) {
                Row(
                    modifier = Modifier.padding(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fotoUrl != null) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto del producto",
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(9.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(colors.emptyPhotoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = colors.subtleText)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "PRODUCTO A RECOGER",
                            color = colors.subtleText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = buildString {
                                append(order.categoria ?: "Paquete")
                                if (!order.descripcion.isNullOrBlank()) {
                                    append(" · ")
                                    append(order.descripcion)
                                }
                            },
                            color = colors.muted,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = if (fotoUrl != null) "Ver" else "Sin foto",
                        color = if (fotoUrl != null) DriverBlue else colors.subtleText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onAccept,
                enabled = !isAccepting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DriverRed,
                    contentColor = Color.White,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.muted
                )
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    if (isAccepting) {
                        "Aceptando..."
                    } else {
                        "Aceptar ruta por S/ ${order.total ?: "-"}"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    if (showProductPreview && fotoUrl != null) {
        Dialog(
            onDismissRequest = {
                showProductPreview = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = "Imagen completa del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(
                            start = 14.dp,
                            end = 14.dp,
                            top = 68.dp,
                            bottom = 92.dp
                        )
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = {
                        showProductPreview = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 14.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar imagen",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 28.dp,
                            bottom = 18.dp
                        )
                ) {
                    Text(
                        text = "Producto a recoger",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildString {
                            append(order.categoria ?: "Paquete")

                            if (!order.descripcion.isNullOrBlank()) {
                                append(" · ")
                                append(order.descripcion)
                            }
                        },
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun tiempoRelativo(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val created = formatter.parse(createdAt) ?: return ""
        val minutes = ((System.currentTimeMillis() - created.time) / 60_000L).coerceAtLeast(0L)
        when {
            minutes < 1 -> "ahora"
            minutes < 60 -> "hace $minutes min"
            minutes < 1_440 -> "hace ${minutes / 60} h"
            else -> "hace ${minutes / 1_440} d"
        }
    } catch (_: Exception) {
        ""
    }
}

private fun obtenerUrlFotoPaquete(foto: String?): String? {
    if (foto.isNullOrBlank()) return null

    return when {
        foto.startsWith("http") -> foto
        foto.startsWith("uploads/") -> "https://peruchocourier.com/perucho_api/$foto"
        else -> "https://peruchocourier.com/perucho_api/uploads/paquetes/$foto"
    }
}

@Composable
private fun RouteRow(
    colors: AvailableOrdersColors,
    color: Color,
    text: String,
    strong: Boolean
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
            color = colors.text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = if (strong) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetaDot(
    colors: AvailableOrdersColors
) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(colors.subtleText)
    )
}

@Composable
private fun PaymentBadge(
    payment: String
) {
    val icon = when (payment.trim().uppercase()) {
        "YAPE" -> R.drawable.ic_yape
        "PLIN" -> R.drawable.ic_plin
        "EFECTIVO" -> R.drawable.ic_efectivo
        else -> null
    }

    if (icon != null) {
        Image(
            painter = painterResource(icon),
            contentDescription = payment,
            modifier = Modifier
                .height(30.dp)
                .wrapContentWidth(),
            contentScale = ContentScale.Fit
        )
    } else {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF1A4FBF).copy(alpha = 0.12f)
        ) {
            Text(
                text = payment,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color(0xFF1A4FBF),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
