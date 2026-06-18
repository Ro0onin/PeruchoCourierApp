package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val TrackBlue = Color(0xFF1A4FBF)
private val TrackBlueDark = Color(0xFF0D3280)
private val TrackRed = Color(0xFFE02020)
private val TrackBg = Color(0xFFF4F6FB)
private val TrackCard = Color.White
private val TrackText = Color(0xFF1A2340)
private val TrackMuted = Color(0xFF6B7A99)
private val TrackBorder = Color(0xFFE8ECF4)
private val TrackGreen = Color(0xFF22C55E)
private val TrackOrange = Color(0xFFF59E0B)

@Composable
fun SeleccionarPedidoSeguimientoScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val userEmail = sessionManager.getUserEmail()

        if (userEmail.isNullOrBlank()) {
            isLoading = false
            errorMessage = "No se encontró la sesión del cliente"
            return@LaunchedEffect
        }

        RetrofitClient.instance.getOrders(userEmail)
            .enqueue(object : Callback<GetOrdersResponse> {
                override fun onResponse(
                    call: Call<GetOrdersResponse>,
                    response: Response<GetOrdersResponse>
                ) {
                    isLoading = false

                    if (response.isSuccessful && response.body()?.success == true) {
                        orders = response.body()?.orders
                            .orEmpty()
                            .filter { order ->
                                val estado = normalizarEstadoTrack(order.estado)

                                val esActivo = estado !in listOf(
                                    "entregado",
                                    "cancelado"
                                )

                                val esNacional = order.tipo_envio == "nacional" ||
                                        order.tipo_envio.isNullOrBlank()

                                esActivo && esNacional
                            }
                    } else {
                        errorMessage = "No se pudieron cargar tus pedidos"
                    }
                }

                override fun onFailure(call: Call<GetOrdersResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrackBg)
    ) {
        HeaderSeleccionSeguimiento(
            onBack = { navController.popBackStack() }
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TrackBlue)
                }
            }

            errorMessage.isNotBlank() -> {
                EmptyTrackState(
                    title = "No se pudo cargar",
                    message = errorMessage
                )
            }

            orders.isEmpty() -> {
                EmptyTrackState(
                    title = "No tienes pedidos para rastrear",
                    message = "Cuando tengas un pedido nacional activo, aparecerá aquí."
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Selecciona el pedido que deseas seguir en tiempo real.",
                            color = TrackMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(orders) { order ->
                        PedidoSeguimientoCard(
                            order = order,
                            onClick = {
                                val orderId = order.id ?: 0
                                if (orderId > 0) {
                                    navController.navigate("seguimiento_cliente/$orderId")
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSeleccionSeguimiento(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(TrackBlueDark, TrackBlue)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rastrear pedido",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Seguimiento en tiempo real",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PedidoSeguimientoCard(
    order: Order,
    onClick: () -> Unit
) {
    val estado = normalizarEstadoTrack(order.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TrackCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TrackBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(TrackBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = TrackBlue,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido #${order.id ?: 0}",
                        color = TrackText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = formatFechaTrack(order.created_at),
                        color = TrackMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                EstadoTrackBadge(estado)
            }

            Spacer(modifier = Modifier.height(14.dp))

            RoutePreviewLine(
                label = "Recojo",
                value = order.pickup_address ?: order.origen ?: "-",
                color = TrackGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoutePreviewLine(
                label = "Entrega",
                value = order.dropoff_address ?: order.destino ?: "-",
                color = TrackRed
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniTrackInfo(
                    label = "Total",
                    value = "S/ ${order.total ?: "-"}",
                    modifier = Modifier.weight(1f)
                )

                MiniTrackInfo(
                    label = "Pago",
                    value = order.metodo_pago ?: "-",
                    modifier = Modifier.weight(1f)
                )

                MiniTrackInfo(
                    label = "Distancia",
                    value = "${order.distancia_km ?: "-"} km",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrackBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Ver seguimiento",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun RoutePreviewLine(
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label.uppercase(),
                color = TrackMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )

            Text(
                text = value,
                color = TrackText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniTrackInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(TrackBg)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            color = TrackMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )

        Text(
            text = value,
            color = TrackText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EstadoTrackBadge(
    estado: String
) {
    val bg: Color
    val fg: Color
    val text: String

    when (estado) {
        "pendiente_pago" -> {
            bg = Color(0xFFFFF4E8)
            fg = TrackOrange
            text = "Pendiente"
        }

        "buscando" -> {
            bg = Color(0xFFE8EFFE)
            fg = TrackBlue
            text = "Buscando"
        }

        "asignado" -> {
            bg = Color(0xFFE8EFFE)
            fg = TrackBlue
            text = "Asignado"
        }

        "recogido" -> {
            bg = Color(0xFFFFF4E8)
            fg = TrackOrange
            text = "Recogido"
        }

        "en_camino", "en_transito" -> {
            bg = Color(0xFFD1FAE5)
            fg = Color(0xFF059669)
            text = "En camino"
        }

        else -> {
            bg = TrackBg
            fg = TrackMuted
            text = estado.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EmptyTrackState(
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(TrackBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = TrackBlue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = TrackText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = TrackMuted,
                fontSize = 13.sp
            )
        }
    }
}

private fun normalizarEstadoTrack(estado: String?): String {
    return estado
        ?.trim()
        ?.lowercase()
        ?.replace(" ", "_")
        ?.replace("-", "_")
        .orEmpty()
}

private fun formatFechaTrack(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "-"

    return try {
        val parts = fecha.substringBefore(" ").split("-")
        val year = parts[0]
        val month = parts[1]
        val day = parts[2]

        val monthName = when (month) {
            "01" -> "Ene"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Abr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Ago"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dic"
            else -> month
        }

        "$day $monthName $year"
    } catch (_: Exception) {
        fecha
    }
}