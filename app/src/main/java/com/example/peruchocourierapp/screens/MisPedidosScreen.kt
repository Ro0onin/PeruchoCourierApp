package com.example.peruchocourierapp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.theme.ThemeManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.Normalizer

private val PcInk = Color(0xFF111A33)
private val PcMuted = Color(0xFF6B7590)
private val PcLine = Color(0xFFE7E9F1)
private val PcRed = Color(0xFFE42328)
private val PcRedDark = Color(0xFFB81419)
private val PcRedSoft = Color(0xFFFDECEC)
private val PcBlue = Color(0xFF1E4FD8)
private val PcBlueTint = Color(0xFFEAF0FE)
private val PcGreen = Color(0xFF12805A)
private val PcGreenTint = Color(0xFFE5F6EE)
private val Bg = Color(0xFFF4F5F9)
private val WarningBg = Color(0xFFFFF3CD)
private val WarningText = Color(0xFF856404)
private val DangerBg = Color(0xFFFEE2E2)
private val DangerText = Color(0xFF991B1B)

private val InterFont = FontFamily.SansSerif

private data class MisPedidosColors(
    val bg: Color,
    val card: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val textSoft: Color,
    val placeholder: Color,
    val blueTint: Color,
    val greenTint: Color,
    val dangerBg: Color,
    val dangerText: Color,
    val warningBg: Color,
    val warningText: Color,
    val sheetBg: Color,
    val watermark: Color
)

@Composable
private fun misPedidosColors(): MisPedidosColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        MisPedidosColors(
            bg = Color(0xFF0F172A),
            card = Color(0xFF111827),
            ink = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            line = Color(0xFF334155),
            textSoft = Color(0xFFE2E8F0),
            placeholder = Color(0xFF94A3B8),
            blueTint = Color(0xFF172554),
            greenTint = Color(0xFF14532D),
            dangerBg = Color(0xFF3F1717),
            dangerText = Color(0xFFFFB4B4),
            warningBg = Color(0xFF422006),
            warningText = Color(0xFFFDE68A),
            sheetBg = Color(0xFF0F172A),
            watermark = Color.White.copy(alpha = 0.035f)
        )
    } else {
        MisPedidosColors(
            bg = Bg,
            card = Color.White,
            ink = PcInk,
            muted = PcMuted,
            line = PcLine,
            textSoft = Color(0xFF3A4260),
            placeholder = Color(0xFFC3CADD),
            blueTint = PcBlueTint,
            greenTint = PcGreenTint,
            dangerBg = DangerBg,
            dangerText = DangerText,
            warningBg = WarningBg,
            warningText = WarningText,
            sheetBg = Bg,
            watermark = PcInk.copy(alpha = 0.035f)
        )
    }
}

private val MonoFont = FontFamily.Monospace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(navController: NavController) {
    val colors = misPedidosColors()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var isCancelling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun cargarPedidos() {
        val userEmail = sessionManager.getUserEmail()

        if (userEmail.isNullOrEmpty()) {
            errorMessage = "No se encontró la sesión del usuario"
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getOrders(userEmail)
            .enqueue(object : Callback<GetOrdersResponse> {
                override fun onResponse(
                    call: Call<GetOrdersResponse>,
                    response: Response<GetOrdersResponse>
                ) {
                    isLoading = false

                    if (response.isSuccessful && response.body()?.success == true) {
                        orders = response.body()?.orders.orEmpty()
                    } else {
                        errorMessage = "No se pudieron cargar los pedidos"
                    }
                }

                override fun onFailure(call: Call<GetOrdersResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    LaunchedEffect(Unit) {
        cargarPedidos()
    }

    val filteredOrders = when (selectedFilter) {
        "Nacionales" -> orders.filter { it.tipo_envio == "nacional" || it.tipo_envio.isNullOrBlank() }
        "Internacionales" -> orders.filter { it.tipo_envio == "internacional" }
        "Entregados" -> orders.filter { normalizarEstado(it.estado) == "entregado" }
        "En curso" -> orders.filter {
            normalizarEstado(it.estado) in listOf(
                "asignado",
                "recogiendo",
                "recogido",
                "en_camino",
                "en_transito",
                "transito"
            )
        }
        else -> orders
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .navigationBarsPadding()
    ) {
        HeaderMisPedidos(
            totalPedidos = orders.size,
            onBack = { navController.popBackStack() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (ThemeManager.isDarkMode.value) Color(0xFF020617) else PcInk)
                .horizontalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton("Todos", selectedFilter) { selectedFilter = "Todos" }
            FilterChipButton("Nacionales", selectedFilter) { selectedFilter = "Nacionales" }
            FilterChipButton("Internacionales", selectedFilter) { selectedFilter = "Internacionales" }
            FilterChipButton("En curso", selectedFilter) { selectedFilter = "En curso" }
            FilterChipButton("Entregados", selectedFilter) { selectedFilter = "Entregados" }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            Text(
                text = "PERUCHO COURIER",
                color = colors.watermark,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = InterFont,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(-25f)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cargando pedidos...",
                            color = colors.muted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFont
                        )
                    }
                }

                errorMessage.isNotBlank() -> {
                    Text(
                        text = errorMessage,
                        color = PcRed,
                        modifier = Modifier.padding(18.dp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }

                filteredOrders.isEmpty() -> {
                    EmptyOrdersBox(selectedFilter = selectedFilter)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredOrders) { order ->
                            PedidoTicketCard(
                                order = order,
                                onClick = { selectedOrder = order }
                            )
                        }

                        item {
                            Text(
                                text = "PERUCHO COURIER",
                                color = colors.placeholder.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = InterFont,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedOrder != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedOrder = null },
            containerColor = colors.bg
        ) {
            PedidoDetalleSheet(
                order = selectedOrder!!,
                onClose = { selectedOrder = null },
                onTrack = {
                    val id = selectedOrder?.id ?: 0
                    selectedOrder = null
                    if (id > 0) navController.navigate("seguimiento_cliente/$id")
                },
                onSupport = {
                    val numero = "51967929967"
                    val mensaje = Uri.encode("Hola Perucho Courier, necesito ayuda con mi pedido #${selectedOrder?.id ?: ""}.")
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numero?text=$mensaje"))
                    )
                },
                onCancel = {
                    orderToCancel = selectedOrder
                    showCancelDialog = true
                }
            )
        }
    }

    if (showCancelDialog && orderToCancel != null) {
        val pedido = orderToCancel!!
        val estadoPedido = normalizarEstado(pedido.estado)
        val penalidad = calcularPenalidadCancelacion(estadoPedido)

        AlertDialog(
            onDismissRequest = {
                if (!isCancelling) {
                    showCancelDialog = false
                    orderToCancel = null
                }
            },
            title = {
                Text(
                    text = "Cancelar pedido #${pedido.id ?: 0}",
                    fontWeight = FontWeight.Black,
                    color = colors.ink,
                    fontFamily = InterFont
                )
            },
            text = {
                Column {
                    Text(
                        text = if (penalidad > 0.0) {
                            "Este pedido ya inició su proceso. Se aplicará una penalidad operativa de S/ ${"%.2f".format(penalidad)}."
                        } else {
                            "Este pedido aún puede cancelarse sin penalidad."
                        },
                        color = colors.muted,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        fontFamily = InterFont
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "¿Deseas continuar con la cancelación?",
                        color = colors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        val userEmail = sessionManager.getUserEmail()
                        val pedidoId = pedido.id ?: 0

                        if (userEmail.isNullOrBlank() || pedidoId <= 0) {
                            showCancelDialog = false
                            orderToCancel = null
                            errorMessage = "No se pudo cancelar el pedido"
                            return@TextButton
                        }

                        scope.launch {
                            try {
                                isCancelling = true

                                val response: BasicResponse = RetrofitClient.instance.cancelOrder(
                                    envioId = pedidoId,
                                    userEmail = userEmail,
                                    motivo = "Cancelado por el cliente desde la app"
                                )

                                isCancelling = false
                                showCancelDialog = false
                                orderToCancel = null
                                selectedOrder = null

                                if (response.success) {
                                    cargarPedidos()
                                } else {
                                    errorMessage = response.message ?: "No se pudo cancelar el pedido"
                                }
                            } catch (e: Exception) {
                                isCancelling = false
                                showCancelDialog = false
                                orderToCancel = null
                                errorMessage = "Error al cancelar: ${e.message}"
                            }
                        }
                    }
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PcRed
                        )
                    } else {
                        Text("Sí, cancelar", color = PcRed, fontWeight = FontWeight.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        showCancelDialog = false
                        orderToCancel = null
                    }
                ) {
                    Text("No")
                }
            },
            containerColor = colors.card
        )
    }
}

@Composable
private fun HeaderMisPedidos(
    totalPedidos: Int,
    onBack: () -> Unit
) {
    val colors = misPedidosColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (ThemeManager.isDarkMode.value) Color(0xFF020617) else PcInk)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Mis pedidos",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Text(
                        text = "$totalPedidos envío${if (totalPedidos == 1) "" else "s"} registrados",
                        color = Color(0xFFAEB6D4),
                        fontSize = 13.sp,
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
                        listOf(PcRed, PcBlue)
                    )
                )
        )
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: String,
    onClick: () -> Unit
) {
    val colors = misPedidosColors()
    val isSelected = selected == text

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) PcInk else Color(0xFFC7CDE4),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFont
        )
    }
}

@Composable
private fun PedidoTicketCard(
    order: Order,
    onClick: () -> Unit
) {
    val colors = misPedidosColors()
    val isNacional = order.tipo_envio == "nacional" || order.tipo_envio.isNullOrBlank()
    val estado = normalizarEstado(order.estado)
    val totalText = if (isNacional) "S/ ${order.total ?: "-"}" else "$${order.total ?: "-"}"
    val isDelivered = estado == "entregado"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center)
                    .rotate(-18f)
                    .alpha(0.15f),
                contentScale = ContentScale.Fit
            )

            Column {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pedido #${order.id ?: 0}",
                                color = colors.ink,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = InterFont
                            )

                            Text(
                                text = "${formatFecha(order.created_at)} · ${if (isNacional) "Nacional" else "Internacional"}",
                                color = colors.muted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFont
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            tint = colors.placeholder,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    TicketStamp(
                        estado = estado,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 18.dp)
                            .rotate(-6f)
                    )
                }

                if (isNacional) {
                    RouteTicketLine(
                        pickup = order.pickup_address ?: order.origen ?: "-",
                        dropoff = order.dropoff_address ?: order.destino ?: "-"
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TicketInfoRow(
                            icon = Icons.Outlined.Inventory2,
                            text = "${order.categoria ?: "-"} · ${order.tamano_paquete ?: "-"}"
                        )

                        TicketInfoRow(
                            icon = Icons.Outlined.TwoWheeler,
                            text = vehiculoLegible(order)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TicketInfoRow(
                            icon = Icons.Outlined.Language,
                            text = "Compra en ${order.web_compra ?: "-"}"
                        )

                        TicketInfoRow(
                            icon = Icons.Outlined.QrCode2,
                            text = "Tracking: ${order.tracking ?: "-"}"
                        )
                    }
                }

                DashedDividerWithCuts()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL",
                            color = colors.muted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFont,
                            letterSpacing = 0.6.sp
                        )

                        Text(
                            text = totalText,
                            color = colors.ink,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MonoFont
                        )
                    }

                    when (order.metodo_pago?.lowercase()) {

                        "yape" -> {
                            Image(
                                painter = painterResource(R.drawable.ic_yape2),
                                contentDescription = "Yape",
                                modifier = Modifier
                                    .height(34.dp)
                                    .width(34.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        "plin" -> {
                            Image(
                                painter = painterResource(R.drawable.ic_plin),
                                contentDescription = "Plin",
                                modifier = Modifier
                                    .height(34.dp)
                                    .width(34.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        else -> {
                            Text(
                                text = order.metodo_pago ?: "Pago",
                                color = PcBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketStamp(
    estado: String,
    modifier: Modifier = Modifier
) {
    val colors = misPedidosColors()
    val color = when (estado) {
        "entregado" -> PcGreen
        "cancelado", "cancelado_cliente" -> PcRed
        else -> PcBlue
    }

    Box(
        modifier = modifier
            .border(2.dp, color, RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = estadoLegible(estado).uppercase(),
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
private fun RouteTicketLine(
    pickup: String,
    dropoff: String
) {
    val colors = misPedidosColors()
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .border(2.dp, PcGreen, CircleShape)
                        .background(colors.card)
                )

                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(28.dp)
                        .background(colors.line)
                        .padding(vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
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
                    color = colors.ink,
                    fontSize = 13.sp,
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
                    .size(11.dp)
                    .clip(CircleShape)
                    .border(2.dp, PcRed, CircleShape)
                    .background(colors.card)
            )

            Spacer(modifier = Modifier.width(10.dp))

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
                    color = colors.ink,
                    fontSize = 13.sp,
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
private fun TicketInfoRow(
    icon: ImageVector,
    text: String
) {
    val colors = misPedidosColors()
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PcBlue,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = colors.textSoft,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = InterFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashedDividerWithCuts() {
    val colors = misPedidosColors()
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
                color = colors.line,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
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
                .background(colors.bg)
        )

        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 8.dp)
                .clip(CircleShape)
                .background(colors.bg)
        )
    }
}

@Composable
private fun EmptyOrdersBox(selectedFilter: String) {
    val colors = misPedidosColors()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.line)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(colors.blueTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = PcBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No tienes pedidos",
                    color = colors.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont
                )

                Text(
                    text = "No encontramos pedidos en la categoría $selectedFilter.",
                    color = colors.muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = InterFont
                )
            }
        }
    }
}

@Composable
private fun PedidoDetalleSheet(
    order: Order,
    onClose: () -> Unit,
    onTrack: () -> Unit,
    onSupport: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = misPedidosColors()
    val context = LocalContext.current
    val isNacional = order.tipo_envio == "nacional" || order.tipo_envio.isNullOrBlank()
    val estado = normalizarEstado(order.estado)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .verticalScroll(rememberScrollState())
            .background(colors.bg)
            .padding(16.dp)
    ) {
        DetailHeroCard(order = order, isNacional = isNacional, estado = estado)

        Spacer(modifier = Modifier.height(12.dp))

        TrackingTimeline(
            estado = estado,
            isNacional = isNacional
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isNacional) {
            DetailSection(
                title = "Ruta de entrega",
                icon = Icons.Outlined.Route
            ) {
                TimelineAddress(
                    label = "Recojo",
                    value = order.pickup_address ?: order.origen ?: "-",
                    color = PcGreen
                )

                TimelineAddress(
                    label = "Entrega",
                    value = order.dropoff_address ?: order.destino ?: "-",
                    color = PcRed,
                    isLast = true
                )
            }

            DetailSection(
                title = "Detalles del paquete",
                icon = Icons.Outlined.Inventory2
            ) {
                DetailGrid(
                    items = listOf(
                        "Categoría" to (order.categoria ?: "-"),
                        "Tamaño" to (order.tamano_paquete ?: "-"),
                        "Vehículo" to vehiculoLegible(order),
                        "Distancia" to "${order.distancia_km ?: "-"} km"
                    )
                )

                DetailLine("Descripción", order.descripcion ?: "-")
                DetailLine("Comentario repartidor", order.comentarios_repartidor ?: "-")
            }

            DetailSection(
                title = "Contactos",
                icon = Icons.Outlined.AccountCircle
            ) {
                ContactRow(
                    label = "Remitente",
                    phone = order.telefono_remitente ?: "-",
                    onClick = { callPhone(context, order.telefono_remitente) }
                )

                DetailDivider()

                ContactRow(
                    label = "Destinatario",
                    phone = order.telefono_destinatario ?: "-",
                    onClick = { callPhone(context, order.telefono_destinatario) }
                )
            }
        } else {
            DetailSection(
                title = "Compra internacional",
                icon = Icons.Outlined.Language
            ) {
                DetailGrid(
                    items = listOf(
                        "Web" to (order.web_compra ?: "-"),
                        "Tracking" to (order.tracking ?: "-"),
                        "Peso" to "${order.peso_estimado ?: "-"} kg",
                        "Llegada" to (order.fecha_llegada ?: "-")
                    )
                )

                DetailLine("Productos", order.productos ?: "-")
                DetailLine("Precio compra", "$${order.precio_compra ?: "-"}")
                DetailLine("Factura PDF", order.factura_pdf ?: "-")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isNacional && estado != "entregado" && estado != "cancelado" && estado != "cancelado_cliente") {
            Button(
                onClick = onTrack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PcBlue)
            ) {
                Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rastrear pedido", fontWeight = FontWeight.Black, fontFamily = InterFont)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (puedeCancelarPedido(estado)) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PcRed)
            ) {
                Icon(Icons.Outlined.Circle, null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar pedido", fontWeight = FontWeight.Black, fontFamily = InterFont)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = onSupport,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PcRed)
        ) {
            Icon(Icons.Outlined.HeadsetMic, null, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contactar soporte", fontWeight = FontWeight.Black, fontFamily = InterFont)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailHeroCard(
    order: Order,
    isNacional: Boolean,
    estado: String
) {
    val colors = misPedidosColors()
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (ThemeManager.isDarkMode.value) Color(0xFF020617) else PcInk)
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Pedido #${order.id ?: 0}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatFecha(order.created_at),
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(estado)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.28f),
                                    RoundedCornerShape(50.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isNacional) "Nacional" else "Internacional",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = InterFont
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(listOf(PcRed, PcBlue))
                    )
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat(
                        icon = Icons.Outlined.Payments,
                        label = "Total",
                        value = if (isNacional) "S/ ${order.total ?: "-"}" else "$${order.total ?: "-"}",
                        modifier = Modifier.weight(1f)
                    )

                    MiniStat(
                        icon = Icons.Outlined.CreditCard,
                        label = "Pago",
                        value = order.metodo_pago ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isNacional) {
                        MiniStat(
                            icon = Icons.Outlined.Straighten,
                            label = "Distancia",
                            value = "${order.distancia_km ?: "-"} km",
                            modifier = Modifier.weight(1f)
                        )

                        MiniStat(
                            icon = Icons.Outlined.TwoWheeler,
                            label = "Vehículo",
                            value = vehiculoLegible(order),
                            modifier = Modifier.weight(1f)
                        )

                        MiniStat(
                            icon = Icons.Outlined.Inventory2,
                            label = "Paquete",
                            value = order.tamano_paquete ?: "-",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MiniStat(
                            icon = Icons.Outlined.QrCode2,
                            label = "Tracking",
                            value = order.tracking ?: "-",
                            modifier = Modifier.weight(1f)
                        )

                        MiniStat(
                            icon = Icons.Outlined.Inventory2,
                            label = "Peso",
                            value = "${order.peso_estimado ?: "-"} kg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = misPedidosColors()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bg)
            .padding(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PcBlue,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label.uppercase(),
            color = colors.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            maxLines = 1
        )

        Text(
            text = value,
            color = colors.ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrackingTimeline(
    estado: String,
    isNacional: Boolean
) {
    val colors = misPedidosColors()
    val steps = if (isNacional) {
        listOf(
            "Pedido creado" to true,
            "Esperando repartidor" to (estado in listOf(
                "pendiente_pago",
                "esperando_repartidor",
                "asignado",
                "recogiendo",
                "recogido",
                "en_camino",
                "entregado"
            )),
            "Repartidor asignado" to (estado in listOf(
                "asignado",
                "recogiendo",
                "recogido",
                "en_camino",
                "entregado"
            )),
            "Recogiendo paquete" to (estado in listOf(
                "recogiendo",
                "recogido",
                "en_camino",
                "entregado"
            )),
            "En camino" to (estado in listOf("en_camino", "entregado")),
            "Entregado" to (estado == "entregado")
        )
    } else {
        listOf(
            "Pedido internacional registrado" to true,
            "En revisión" to (estado in listOf(
                "en_revision", "esperando_almacen", "recibido_en_almacen",
                "en_consolidacion", "despachado", "transito_internacional",
                "llego_a_peru", "desaduanaje", "pago_de_impuestos",
                "liberado_por_aduanas", "en_distribucion", "en_ruta", "entregado"
            )),
            "Esperando almacén" to (estado in listOf(
                "esperando_almacen", "recibido_en_almacen", "en_consolidacion",
                "despachado", "transito_internacional", "llego_a_peru",
                "desaduanaje", "pago_de_impuestos", "liberado_por_aduanas",
                "en_distribucion", "en_ruta", "entregado"
            )),
            "Recibido en almacén" to (estado in listOf(
                "recibido_en_almacen", "en_consolidacion", "despachado",
                "transito_internacional", "llego_a_peru", "desaduanaje",
                "pago_de_impuestos", "liberado_por_aduanas", "en_distribucion",
                "en_ruta", "entregado"
            )),
            "En consolidación" to (estado in listOf(
                "en_consolidacion", "despachado", "transito_internacional",
                "llego_a_peru", "desaduanaje", "pago_de_impuestos",
                "liberado_por_aduanas", "en_distribucion", "en_ruta", "entregado"
            )),
            "Despachado" to (estado in listOf(
                "despachado", "transito_internacional", "llego_a_peru",
                "desaduanaje", "pago_de_impuestos", "liberado_por_aduanas",
                "en_distribucion", "en_ruta", "entregado"
            )),
            "Tránsito internacional" to (estado in listOf(
                "transito_internacional", "llego_a_peru", "desaduanaje",
                "pago_de_impuestos", "liberado_por_aduanas", "en_distribucion",
                "en_ruta", "entregado"
            )),
            "Llegó a Perú" to (estado in listOf(
                "llego_a_peru", "desaduanaje", "pago_de_impuestos",
                "liberado_por_aduanas", "en_distribucion", "en_ruta", "entregado"
            )),
            "Desaduanaje" to (estado in listOf(
                "desaduanaje", "pago_de_impuestos", "liberado_por_aduanas",
                "en_distribucion", "en_ruta", "entregado"
            )),
            "Pago de impuestos" to (estado in listOf(
                "pago_de_impuestos", "liberado_por_aduanas",
                "en_distribucion", "en_ruta", "entregado"
            )),
            "Liberado por aduanas" to (estado in listOf(
                "liberado_por_aduanas", "en_distribucion", "en_ruta", "entregado"
            )),
            "En distribución" to (estado in listOf(
                "en_distribucion", "en_ruta", "entregado"
            )),
            "En ruta" to (estado in listOf("en_ruta", "entregado")),
            "Entregado" to (estado == "entregado")
        )
    }

    DetailSection(
        title = if (isNacional) "Seguimiento" else "Seguimiento internacional",
        icon = Icons.Outlined.Schedule
    ) {
        steps.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index == steps.lastIndex && item.second -> PcGreen
                                    item.second -> PcInk
                                    else -> Color.White
                                }
                            )
                            .border(
                                2.dp,
                                when {
                                    index == steps.lastIndex && item.second -> PcGreen
                                    item.second -> PcInk
                                    else -> colors.line
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.second) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(28.dp)
                                .background(if (item.second) PcBlue else colors.line)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.first,
                    color = if (item.second) PcInk else PcMuted,
                    fontSize = 14.sp,
                    fontWeight = if (index == steps.lastIndex && item.second) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontFamily = InterFont,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = misPedidosColors()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.blueTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PcBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title.uppercase(),
                    color = colors.muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont,
                    letterSpacing = 0.4.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun TimelineAddress(
    label: String,
    value: String,
    color: Color,
    isLast: Boolean = false
) {
    val colors = misPedidosColors()
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(colors.card)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(colors.line)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = label.uppercase(),
                color = colors.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont,
                letterSpacing = 0.5.sp
            )

            Text(
                text = value,
                color = colors.ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFont,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    val colors = misPedidosColors()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    MiniInfoBox(
                        label = item.first,
                        value = item.second,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun MiniInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = misPedidosColors()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bg)
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = colors.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = colors.ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContactRow(
    label: String,
    phone: String,
    onClick: () -> Unit
) {
    val colors = misPedidosColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.blueTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Call, null, tint = PcBlue, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label.uppercase(),
                color = colors.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont
            )

            Text(
                phone,
                color = colors.ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont
            )
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    val colors = misPedidosColors()
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label.uppercase(),
            color = colors.muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            color = colors.ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFont,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DetailDivider() {
    val colors = misPedidosColors()
    HorizontalDivider(
        color = colors.line,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun StatusBadge(estado: String) {
    val colors = misPedidosColors()
    val bg: Color
    val fg: Color
    val label: String

    when (estado) {
        "pendiente_pago" -> {
            bg = colors.blueTint
            fg = PcBlue
            label = "Esperando repartidor"
        }

        "pendiente_revision", "esperando_almacen", "en_almacen" -> {
            bg = colors.blueTint
            fg = PcBlue
            label = estadoLegible(estado)
        }

        "en_transito", "asignado", "transito", "recogido", "recogiendo", "en_camino" -> {
            bg = colors.blueTint
            fg = PcBlue
            label = "En tránsito"
        }

        "listo_entrega", "entregado" -> {
            bg = colors.greenTint
            fg = PcGreen
            label = if (estado == "entregado") "Entregado" else "Listo entrega"
        }

        "cancelado", "cancelado_cliente" -> {
            bg = colors.dangerBg
            fg = colors.dangerText
            label = if (estado == "cancelado_cliente") "Cancelado por cliente" else "Cancelado"
        }

        else -> {
            bg = colors.bg
            fg = PcMuted
            label = estadoLegible(estado)
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont
        )
    }
}

private fun vehiculoLegible(order: Order): String {
    val vehiculo = order.tipo_vehiculo.orEmpty().lowercase().trim()
    val tarifa = order.tarifa_motorizado.orEmpty().lowercase().trim()

    return when {
        vehiculo == "motorizado" && tarifa == "plana" -> "Motorizado Tarifa Plana"
        vehiculo == "motorizado" && tarifa == "estandar" -> "Motorizado Tarifa Estándar"
        vehiculo == "motorizado" -> "Motorizado"
        vehiculo == "van" || vehiculo == "van / minivan" || vehiculo == "minivan" -> "Van / Minivan"
        else -> order.tipo_vehiculo ?: "-"
    }
}

private fun callPhone(
    context: android.content.Context,
    phone: String?
) {
    if (!phone.isNullOrBlank() && phone != "-") {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        )
    }
}

private fun normalizarEstado(estado: String?): String {
    val sinAcentos = Normalizer.normalize(estado.orEmpty(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")

    return sinAcentos
        .lowercase()
        .trim()
        .replace(" ", "_")
        .replace("-", "_")
}

private fun puedeCancelarPedido(estado: String): Boolean {
    return estado !in listOf(
        "entregado",
        "finalizado",
        "cancelado",
        "cancelado_cliente"
    )
}

private fun calcularPenalidadCancelacion(estado: String): Double {
    return when (estado) {
        "asignado", "recogiendo", "recogido" -> 2.90
        "en_camino", "en_transito", "transito" -> 5.00
        else -> 0.00
    }
}

private fun estadoLegible(estado: String?): String {
    val limpio = normalizarEstado(estado)

    return when (limpio) {
        "pendiente_pago" -> "Esperando repartidor"
        "pendiente_revision" -> "En revisión"
        "esperando_almacen" -> "Esperando almacén"
        "en_almacen" -> "En almacén"
        "en_transito", "transito", "asignado", "recogido", "recogiendo", "en_camino" -> "En tránsito"
        "listo_entrega" -> "Listo para entrega"
        "entregado" -> "Entregado"
        "cancelado" -> "Cancelado"
        "cancelado_cliente" -> "Cancelado por cliente"
        "en_revision" -> "En revisión"
        "recibido_en_almacen" -> "Recibido en almacén"
        "en_consolidacion" -> "En consolidación"
        "despachado" -> "Despachado"
        "transito_internacional" -> "Tránsito internacional"
        "llego_a_peru" -> "Llegó a Perú"
        "desaduanaje" -> "Desaduanaje"
        "pago_de_impuestos" -> "Pago de impuestos"
        "liberado_por_aduanas" -> "Liberado por aduanas"
        "en_distribucion" -> "En distribución"
        "en_ruta" -> "En ruta"
        else -> limpio
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
            .ifBlank { "Pendiente" }
    }
}

private fun formatFecha(fecha: String?): String {
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