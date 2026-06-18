package com.example.peruchocourierapp.screens

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

@Composable
fun PedidosDisponiblesScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

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
            .background(DriverBg)
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
                            color = DriverRed,
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
                            color = DriverMuted,
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
            title = { Text("No se pudo aceptar") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Entendido")
                }
            }
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
                Brush.linearGradient(
                    listOf(DriverBlueDark, DriverBlue, DriverBlueMid)
                )
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
    order: Order,
    isAccepting: Boolean,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, DriverBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EFFE)),
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
                        color = DriverGreen,
                        text = order.pickup_address ?: "Recojo no disponible",
                        strong = true
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 4.5.dp, top = 3.dp, bottom = 3.dp)
                            .width(1.5.dp)
                            .height(12.dp)
                            .background(DriverBorder)
                    )

                    RouteRow(
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

                MetaDot()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DriverMuted,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = "${order.distancia_km ?: "-"} km",
                        color = DriverMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                MetaDot()

                PaymentBadge(order.metodo_pago ?: "-")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Producto a recoger",
                color = DriverMuted,
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
                        .background(Color(0xFFF4F6FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El cliente no adjuntó foto",
                        color = DriverMuted,
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
                    contentColor = Color.White
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
            color = DriverText,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = if (strong) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(Color(0xFFB0BAD0))
    )
}

@Composable
private fun PaymentBadge(payment: String) {
    val isBcp = payment.equals("BCP", ignoreCase = true)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isBcp) Color(0xFFFFF4E8) else Color(0xFFE8EFFE))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = payment,
            color = if (isBcp) Color(0xFFD97706) else DriverBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}