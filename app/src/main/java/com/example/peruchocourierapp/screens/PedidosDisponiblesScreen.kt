package com.example.peruchocourierapp.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
                    .background(Color.White.copy(alpha = 0.15f))
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
    colors: AvailableOrdersColors,
    order: Order,
    isAccepting: Boolean,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, colors.border),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.iconSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DriverBlue,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    RouteRow(
                        colors = colors,
                        color = DriverGreen,
                        text = order.pickup_address ?: "Recojo no disponible",
                        strong = true
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 4.5.dp, top = 3.dp, bottom = 3.dp)
                            .width(1.5.dp)
                            .height(12.dp)
                            .background(colors.border)
                    )

                    RouteRow(
                        colors = colors,
                        color = DriverRed,
                        text = order.dropoff_address ?: "Entrega no disponible",
                        strong = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = "S/ ${order.total ?: "-"}",
                    color = DriverBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                MetaDot(colors)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.muted,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = "${order.distancia_km ?: "-"} km",
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                MetaDot(colors)

                PaymentBadge(
                    colors = colors,
                    payment = order.metodo_pago ?: "-"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Producto a recoger",
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val fotoUrl = obtenerUrlFotoPaquete(order.foto_paquete)

            if (fotoUrl != null) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = "Foto del paquete",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.emptyPhotoBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El cliente no adjuntó foto",
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onAccept,
                enabled = !isAccepting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DriverBlue,
                    contentColor = Color.White,
                    disabledContainerColor = colors.border,
                    disabledContentColor = colors.muted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isAccepting) "Aceptando..." else "Aceptar pedido",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
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
    colors: AvailableOrdersColors,
    payment: String
) {
    val isBcp = payment.equals("BCP", ignoreCase = true)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isBcp) colors.bcpBg else colors.paymentBg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = payment,
            color = if (isBcp) colors.bcpText else DriverBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
