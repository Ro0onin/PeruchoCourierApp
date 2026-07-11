package com.example.peruchocourierapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.theme.ThemeManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Ink = Color(0xFF111A33)
private val Muted = Color(0xFF6B7590)
private val Line = Color(0xFFE7E9F1)
private val Red = Color(0xFFE42328)
private val RedDark = Color(0xFFB81419)
private val Blue = Color(0xFF1E4FD8)
private val BlueTint = Color(0xFFEAF0FE)
private val Green = Color(0xFF12805A)
private val Amber = Color(0xFF9A6A0C)
private val Bg = Color(0xFFF4F5F9)
private val HeaderDark = Color(0xFF111A33)

private val InterFont = FontFamily.SansSerif
private val MonoFont = FontFamily.Monospace

private data class TrackSelectColors(
    val bg: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val line: Color,
    val headerBg: Color,
    val headerMuted: Color,
    val blueTint: Color,
    val stampBg: Color,
    val dashLine: Color,
    val cutBg: Color
)

@Composable
private fun seleccionarTrackColors(): TrackSelectColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        TrackSelectColors(
            bg = Color(0xFF0F172A),
            card = Color(0xFF111827),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            line = Color(0xFF334155),
            headerBg = Color(0xFF111827),
            headerMuted = Color(0xFFCBD5E1),
            blueTint = Color(0xFF172554),
            stampBg = Color(0xFF111827).copy(alpha = 0.78f),
            dashLine = Color(0xFF475569),
            cutBg = Color(0xFF0F172A)
        )
    } else {
        TrackSelectColors(
            bg = Bg,
            card = Color.White,
            text = Ink,
            muted = Muted,
            line = Line,
            headerBg = HeaderDark,
            headerMuted = Color(0xFFAEB6D4),
            blueTint = BlueTint,
            stampBg = Color.White.copy(alpha = 0.55f),
            dashLine = Color(0xFFD6DAE6),
            cutBg = Bg
        )
    }
}

private val LocalTrackSelectColors = staticCompositionLocalOf {
    TrackSelectColors(
        bg = Bg,
        card = Color.White,
        text = Ink,
        muted = Muted,
        line = Line,
        headerBg = HeaderDark,
        headerMuted = Color(0xFFAEB6D4),
        blueTint = BlueTint,
        stampBg = Color.White.copy(alpha = 0.55f),
        dashLine = Color(0xFFD6DAE6),
        cutBg = Bg
    )
}

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
                                    "cancelado",
                                    "cancelado_cliente"
                                )

                                val esNacional =
                                    order.tipo_envio == "nacional" ||
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

    CompositionLocalProvider(LocalTrackSelectColors provides seleccionarTrackColors()) {
        val colors = LocalTrackSelectColors.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .navigationBarsPadding()
        ) {
            HeaderRastrearPedido(
                onBack = { navController.popBackStack() }
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Red)
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Selecciona el pedido que deseas seguir en tiempo real.",
                                color = colors.muted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFont,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    end = 4.dp,
                                    bottom = 2.dp
                                )
                            )
                        }

                        items(orders) { order ->
                            PedidoTrackTicket(
                                order = order,
                                onClick = {
                                    val orderId = order.id ?: 0
                                    if (orderId > 0) {
                                        navController.navigate("seguimiento_cliente/$orderId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRastrearPedido(
    onBack: () -> Unit
) {
    val colors = LocalTrackSelectColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.headerBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Rastrear pedido",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Text(
                        text = "Seguimiento en tiempo real",
                        color = colors.headerMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Red, Blue)
                    )
                )
        )
    }
}

@Composable
private fun PedidoTrackTicket(
    order: Order,
    onClick: () -> Unit
) {
    val colors = LocalTrackSelectColors.current

    val estado = normalizarEstadoTrack(order.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Image(
                painter = painterResource(R.drawable.logo_perucho2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .align(Alignment.TopCenter)
                    .offset(y = 60.dp)     // ajusta entre 40.dp y 70.dp
                    .rotate(-18f)
                    .alpha(0.22f),
                contentScale = ContentScale.Fit
            )
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.blueTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = Blue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Pedido #${order.id ?: 0}",
                                color = colors.text,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = InterFont
                            )

                            Text(
                                text = formatFechaTrack(order.created_at),
                                color = colors.muted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFont
                            )
                        }
                    }

                    StampEstadoTrack(estado)
                }

                RouteTicketTrack(
                    pickup = order.pickup_address ?: order.origen ?: "-",
                    dropoff = order.dropoff_address ?: order.destino ?: "-"
                )

                PerforatedDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrackStatCell(
                        label = "Total",
                        value = "S/ ${order.total ?: "-"}",
                        modifier = Modifier.weight(1f),
                        mono = true
                    )

                    VerticalDashedDivider()

                    TrackStatCell(
                        label = "Pago",
                        value = order.metodo_pago ?: "-",
                        modifier = Modifier.weight(1f),
                        valueColor = Blue
                    )

                    VerticalDashedDivider()

                    TrackStatCell(
                        label = "Distancia",
                        value = "${order.distancia_km ?: "-"} km",
                        modifier = Modifier.weight(1f),
                        mono = true
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ver seguimiento",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }
            }
        }
    }
}

@Composable
private fun StampEstadoTrack(
    estado: String
) {
    val colors = LocalTrackSelectColors.current

    val color = when (estado) {
        "pendiente_pago", "pendiente" -> Amber
        "entregado" -> Green
        "cancelado", "cancelado_cliente" -> Red
        else -> Blue
    }

    val text = when (estado) {
        "pendiente_pago" -> "Pendiente"
        "buscando" -> "Buscando"
        "asignado" -> "Asignado"
        "recogido", "recogiendo" -> "Recogido"
        "en_camino", "en_transito", "transito" -> "En camino"
        else -> estado.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    Box(
        modifier = Modifier
            .rotate(-4f)
            .border(2.dp, color, RoundedCornerShape(6.dp))
            .background(colors.stampBg, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = InterFont,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun RouteTicketTrack(
    pickup: String,
    dropoff: String
) {
    val colors = LocalTrackSelectColors.current

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .border(2.dp, Green, CircleShape)
                        .background(colors.card)
                )

                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(28.dp)
                        .background(colors.line)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = "RECOJO",
                    color = colors.muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFont,
                    letterSpacing = 0.4.sp
                )

                Text(
                    text = pickup,
                    color = colors.text,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFont,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .border(2.dp, Red, CircleShape)
                    .background(colors.card)
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ENTREGA",
                    color = colors.muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFont,
                    letterSpacing = 0.4.sp
                )

                Text(
                    text = dropoff,
                    color = colors.text,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFont,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PerforatedDivider() {
    val colors = LocalTrackSelectColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.Center)
        ) {
            drawLine(
                color = colors.dashLine,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
            )
        }

        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp)
                .clip(CircleShape)
                .background(colors.cutBg)
        )

        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 8.dp)
                .clip(CircleShape)
                .background(colors.cutBg)
        )
    }
}

@Composable
private fun TrackStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
    mono: Boolean = false
) {
    val colors = LocalTrackSelectColors.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            color = colors.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFont,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = valueColor ?: colors.text,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = if (mono) MonoFont else InterFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VerticalDashedDivider() {
    val colors = LocalTrackSelectColors.current

    Canvas(
        modifier = Modifier
            .height(34.dp)
            .width(1.dp)
    ) {
        drawLine(
            color = colors.dashLine,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
        )
    }
}

@Composable
private fun EmptyTrackState(
    title: String,
    message: String
) {
    val colors = LocalTrackSelectColors.current

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
                    .background(colors.blueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = colors.muted,
                fontSize = 13.sp,
                fontFamily = InterFont
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