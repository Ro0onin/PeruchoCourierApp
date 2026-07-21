package com.example.peruchocourierapp.screens

import android.content.Intent
import android.provider.OpenableColumns
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.outlined.Circle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
                        val nuevosPedidos = response.body()?.orders.orEmpty()
                        orders = nuevosPedidos

                        val pedidoSeleccionadoId = selectedOrder?.id
                        if (pedidoSeleccionadoId != null) {
                            selectedOrder = nuevosPedidos.firstOrNull {
                                it.id == pedidoSeleccionadoId
                            } ?: selectedOrder
                        }
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
                                onClick = { selectedOrder = order },
                                onPay = {
                                    solicitarPagoYabrirWhatsApp(
                                        context = context,
                                        order = order,
                                        userEmail = sessionManager.getUserEmail().orEmpty(),
                                        onUpdated = { cargarPedidos() }
                                    )
                                }
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
                onProductAdded = {
                    cargarPedidos()
                },
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
                onPay = {
                    selectedOrder?.let { pedido ->
                        solicitarPagoYabrirWhatsApp(
                            context = context,
                            order = pedido,
                            userEmail = sessionManager.getUserEmail().orEmpty(),
                            onUpdated = {
                                cargarPedidos()
                            }
                        )
                    }
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
    onClick: () -> Unit,
    onPay: () -> Unit
) {
    val colors = misPedidosColors()
    val isNacional =
        order.tipo_envio == "nacional" ||
                order.tipo_envio.isNullOrBlank()

    val estado = normalizarEstado(order.estado)
    val estadoNombre = order.estado_nombre
        ?.takeIf { it.isNotBlank() }
        ?: estadoLegible(estado)

    val descripcion = order.estado_descripcion
        ?.takeIf { it.isNotBlank() }
        ?: if (isNacional) {
            descripcionEstadoNacional(estado)
        } else {
            descripcionEstadoInternacional(estado)
        }

    val pasoVisual = if (isNacional) {
        "nacional"
    } else {
        resolverPasoVisualInternacional(
            pasoVisualApi = order.paso_visual,
            estado = estado
        )
    }

    val accent = colorPasoVisual(
        pasoVisual = pasoVisual,
        estado = estado
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = 0.30f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = 0.16f),
                                colors.card
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = iconoPasoVisual(
                        pasoVisual = pasoVisual,
                        estado = estado
                    ),
                    contentDescription = null,
                    tint = accent.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 24.dp, y = (-28).dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(0.84f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconoPasoVisual(
                                    pasoVisual = pasoVisual,
                                    estado = estado
                                ),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pedido #${order.id ?: 0}",
                                color = colors.ink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = InterFont
                            )

                            Text(
                                text = "${formatFecha(order.created_at)} · ${
                                    if (isNacional) "Nacional" else "Internacional"
                                }",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = estadoNombre,
                        color = accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = descripcion,
                        color = colors.textSoft,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = colors.line.copy(alpha = 0.75f)
            )

            if (isNacional) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    )
                ) {
                    RouteTicketLine(
                        pickup = order.pickup_address
                            ?: order.origen
                            ?: "-",
                        dropoff = order.dropoff_address
                            ?: order.destino
                            ?: "-"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TicketInfoRow(
                        icon = Icons.Outlined.TwoWheeler,
                        text = vehiculoLegible(order)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    TicketInfoRow(
                        icon = Icons.Outlined.Language,
                        text = "Compra en ${order.web_compra ?: "-"}"
                    )

                    TicketInfoRow(
                        icon = Icons.Outlined.QrCode2,
                        text = "Tracking: ${order.tracking ?: "-"}"
                    )

                    InternationalMiniProgress(
                        pasoVisual = pasoVisual,
                        estado = estado
                    )
                }
            }

            HorizontalDivider(
                color = colors.line.copy(alpha = 0.75f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL",
                        color = colors.muted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = InterFont,
                        letterSpacing = 0.6.sp
                    )

                    Text(
                        text = if (isNacional) {
                            "S/ ${order.total ?: "-"}"
                        } else {
                            "$${order.total ?: "-"}"
                        },
                        color = colors.ink,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MonoFont
                    )
                }

                PaymentLogoOrText(
                    metodoPago = order.metodo_pago
                )
            }

            if (!isNacional) {
                InternationalPaymentAction(
                    order.estadoPago,
                    onPay = onPay,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 14.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun InternationalPaymentAction(
    estadoPago: String?,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = misPedidosColors()
    val estado = normalizarEstadoPago(estadoPago)

    when (estado) {
        "pagado" -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.greenTint)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = PcGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "Pago confirmado",
                    color = PcGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont
                )
            }
        }

        "en_validacion" -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.warningBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = colors.warningText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                    Text(
                        text = "Pago en validación",
                        color = colors.warningText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = InterFont
                    )
                    Text(
                        text = "Nuestro equipo confirmará el pago.",
                        color = colors.warningText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )
                }
            }
        }

        else -> {
            Button(
                onClick = onPay,
                modifier = modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (estado == "rechazado") "Volver a pagar" else "Ir a pagar",
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont
                )
            }
        }
    }
}

@Composable
private fun PaymentLogoOrText(
    metodoPago: String?
) {
    when (metodoPago?.lowercase()) {
        "yape" -> {
            Image(
                painter = painterResource(R.drawable.ic_yape2),
                contentDescription = "Yape",
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
        }

        "plin" -> {
            Image(
                painter = painterResource(R.drawable.ic_plin),
                contentDescription = "Plin",
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
        }

        else -> {
            Text(
                text = metodoPago ?: "Pago",
                color = PcBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFont
            )
        }
    }
}

@Composable
private fun InternationalMiniProgress(
    pasoVisual: String,
    estado: String
) {
    val colors = misPedidosColors()
    val stages = listOf(
        "registro",
        "almacen",
        "despacho",
        "desaduanaje",
        "distribucion"
    )

    val currentIndex = when (pasoVisual) {
        "registro" -> 0
        "almacen" -> 1
        "despacho" -> 2
        "desaduanaje", "aduanas" -> 3
        "distribucion", "entrega", "entregado" -> 4
        else -> 0
    }

    val delivered = estado == "entregado" ||
            pasoVisual == "entregado"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            delivered -> PcGreen
                            index < currentIndex -> PcBlue
                            index == currentIndex -> PcRed
                            else -> colors.line
                        }
                    )
            )

            if (index != stages.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (delivered || index < currentIndex) {
                                PcBlue.copy(alpha = 0.65f)
                            } else {
                                colors.line
                            }
                        )
                )
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
    onProductAdded: () -> Unit,
    onClose: () -> Unit,
    onTrack: () -> Unit,
    onSupport: () -> Unit,
    onPay: () -> Unit,
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

        if (isNacional) {
            TrackingTimelineNacional(
                estado = estado
            )
        } else {
            InternationalTrackingSection(
                order = order,
                onProductAdded = onProductAdded
            )
        }

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
                InternationalPurchaseHeader(
                    number = 1,
                    principal = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailGrid(
                    items = listOf(
                        "Web" to (order.web_compra ?: "-"),
                        "Tracking" to (order.tracking ?: "-"),
                        "Peso" to "${order.peso_estimado ?: "-"} kg",
                        "Llegada" to (order.fecha_llegada ?: "-")
                    )
                )

                DetailLine("Producto", order.productos ?: "-")
                DetailLine("Precio compra", "$${order.precio_compra ?: "-"}")

                if (!order.factura_pdf.isNullOrBlank()) {
                    DetailLine("Factura PDF", order.factura_pdf)
                }

                order.productos_adicionales.forEachIndexed { index, producto ->
                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        color = colors.line
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    InternationalPurchaseHeader(
                        number = index + 2,
                        principal = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailGrid(
                        items = listOf(
                            "Web" to (producto.web_compra ?: "-"),
                            "Tracking" to (producto.tracking ?: "-"),
                            "Peso" to "${producto.peso_estimado ?: "-"} kg",
                            "Precio" to "$${producto.precio_compra ?: "-"}"
                        )
                    )

                    DetailLine(
                        "Producto",
                        producto.producto ?: "-"
                    )
                    DetailLine(
                        "Factura PDF",
                        producto.factura_pdf
                            ?.takeIf { it.isNotBlank() }
                            ?: "Sin factura adjunta"
                    )

                    if (!producto.comentario.isNullOrBlank()) {
                        DetailLine(
                            "Comentario",
                            producto.comentario
                        )
                    }

                    if (!producto.created_at.isNullOrBlank()) {
                        DetailLine(
                            "Agregado",
                            formatFecha(producto.created_at)
                        )
                    }
                }
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

        if (!isNacional) {
            InternationalPaymentAction(
                order.estadoPago,
                onPay = onPay,
                modifier = Modifier.fillMaxWidth()
            )

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
private fun InternationalPurchaseHeader(
    number: Int,
    principal: Boolean
) {
    val colors = misPedidosColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (principal) {
                    colors.blueTint
                } else {
                    colors.bg
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (principal) PcBlue
                    else Color(0xFFF59E0B)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "COMPRA $number",
                color = colors.ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont,
                letterSpacing = 0.4.sp
            )

            Text(
                text = if (principal) {
                    "Compra principal"
                } else {
                    "Producto agregado"
                },
                color = colors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = InterFont
            )
        }

        if (principal) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(PcBlue.copy(alpha = 0.14f))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "PRINCIPAL",
                    color = PcBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont
                )
            }
        }
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

private data class InternationalVisualStage(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private data class InternationalHistoryStep(
    val codes: Set<String>,
    val label: String
)

@Composable
private fun TrackingTimelineNacional(
    estado: String
) {
    val colors = misPedidosColors()
    val darkMode = ThemeManager.isDarkMode.value

    val completedCircle = if (darkMode) PcBlue else PcInk
    val completedText = if (darkMode) colors.ink else PcInk
    val pendingCircleBackground =
        if (darkMode) Color(0xFF1E293B) else Color.White
    val pendingCircleBorder =
        if (darkMode) Color(0xFF94A3B8) else colors.line
    val pendingText =
        if (darkMode) Color(0xFF94A3B8) else PcMuted
    val completedLine =
        if (darkMode) Color(0xFF3B82F6) else PcBlue

    val steps = listOf(
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
        "En camino" to (estado in listOf(
            "en_camino",
            "entregado"
        )),
        "Entregado" to (estado == "entregado")
    )

    DetailSection(
        title = "Seguimiento",
        icon = Icons.Outlined.Schedule
    ) {
        steps.forEachIndexed { index, item ->
            val completed = item.second
            val finalCompleted =
                index == steps.lastIndex && completed

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    finalCompleted -> PcGreen
                                    completed -> completedCircle
                                    else -> pendingCircleBackground
                                }
                            )
                            .border(
                                2.dp,
                                when {
                                    finalCompleted -> PcGreen
                                    completed -> completedCircle
                                    else -> pendingCircleBorder
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
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
                                .background(
                                    if (completed) completedLine
                                    else colors.line
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.first,
                    color = when {
                        finalCompleted -> PcGreen
                        completed -> completedText
                        else -> pendingText
                    },
                    fontSize = 14.sp,
                    fontWeight = when {
                        finalCompleted -> FontWeight.ExtraBold
                        completed -> FontWeight.Bold
                        else -> FontWeight.SemiBold
                    },
                    fontFamily = InterFont,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun InternationalTrackingSection(
    order: Order,
    onProductAdded: () -> Unit
) {
    val colors = misPedidosColors()
    val estado = normalizarEstado(order.estado)

    val pasoVisual = resolverPasoVisualInternacional(
        pasoVisualApi = order.paso_visual,
        estado = estado
    )

    val estadoNombre = order.estado_nombre
        ?.takeIf { it.isNotBlank() }
        ?: estadoLegible(estado)

    val descripcion = order.estado_descripcion
        ?.takeIf { it.isNotBlank() }
        ?: descripcionEstadoInternacional(estado)

    val stages = listOf(
        InternationalVisualStage(
            key = "registro",
            label = "Registro",
            icon = Icons.Outlined.Schedule
        ),
        InternationalVisualStage(
            key = "almacen",
            label = "Almacén",
            icon = Icons.Outlined.Inventory2
        ),
        InternationalVisualStage(
            key = "despacho",
            label = "Despacho",
            icon = Icons.Outlined.Language
        ),
        InternationalVisualStage(
            key = "desaduanaje",
            label = "Aduanas",
            icon = Icons.Outlined.QrCode2
        ),
        InternationalVisualStage(
            key = "distribucion",
            label = "Entrega",
            icon = Icons.Outlined.Route
        )
    )

    val currentIndex = when (pasoVisual) {
        "registro" -> 0
        "almacen" -> 1
        "despacho" -> 2
        "desaduanaje", "aduanas" -> 3
        "distribucion", "entrega", "entregado" -> 4
        else -> 0
    }

    val delivered =
        estado == "entregado" ||
                pasoVisual == "entregado"

    var showAddProductDialog by remember(order.id, estado) {
        mutableStateOf(false)
    }

    InternationalCurrentStatusCard(
        order = order,
        title = estadoNombre,
        description = descripcion,
        pasoVisual = pasoVisual,
        delivered = delivered
    )

    if (puedeAgregarProductoInternacional(estado)) {
        Spacer(modifier = Modifier.height(12.dp))

        AddInternationalProductCard(
            onClick = {
                showAddProductDialog = true
            }
        )
    }

    if (showAddProductDialog) {
        AddInternationalProductDialog(
            order = order,
            onDismiss = {
                showAddProductDialog = false
            },
            onSubmitted = {
                showAddProductDialog = false
                onProductAdded()
            }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = "PROGRESO DEL ENVÍO",
                color = colors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = InterFont,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                stages.forEachIndexed { index, stage ->
                    InternationalStageItem(
                        stage = stage,
                        completed = index < currentIndex || delivered,
                        current = index == currentIndex && !delivered,
                        delivered = delivered &&
                                index == stages.lastIndex,
                        modifier = Modifier.weight(1f)
                    )

                    if (index != stages.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(top = 18.dp)
                                .width(12.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (index < currentIndex || delivered) {
                                        PcBlue
                                    } else {
                                        colors.line
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress =
                if (delivered) 1f
                else (currentIndex + 1) /
                        stages.size.toFloat()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(colors.line)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PcRed, PcBlue)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = if (delivered) {
                    "Tu pedido fue entregado correctamente."
                } else {
                    "Etapa actual: ${stages[currentIndex].label}"
                },
                color = if (delivered) PcGreen else colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFont
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    InternationalHistoryCard(
        currentState = estado
    )
}

@Composable
private fun AddInternationalProductCard(
    onClick: () -> Unit
) {
    val colors = misPedidosColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFF59E0B).copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFF59E0B).copy(alpha = 0.14f),
                            colors.card
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¿Tienes otra compra?",
                        color = colors.ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Puedes agregar más productos mientras el envío siga abierto.",
                        color = colors.muted,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Agregar otro producto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = InterFont
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "Disponible hasta que el pedido cambie a Despachado.",
                color = colors.muted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFont,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddInternationalProductDialog(
    order: Order,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit
) {
    val colors = misPedidosColors()
    val context = LocalContext.current
    val sessionManager = remember {
        SessionManager(context)
    }
    val scope = rememberCoroutineScope()

    var webCompra by remember(order.id) {
        mutableStateOf("")
    }

    var nombreProducto by remember(order.id) {
        mutableStateOf("")
    }

    var tracking by remember(order.id) {
        mutableStateOf("")
    }

    var precio by remember(order.id) {
        mutableStateOf("")
    }

    var pesoEstimado by remember(order.id) {
        mutableStateOf("")
    }

    var comentario by remember(order.id) {
        mutableStateOf("")
    }

    var facturaUri by remember(order.id) {
        mutableStateOf<Uri?>(null)
    }

    var facturaNombre by remember(order.id) {
        mutableStateOf("")
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    val facturaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            facturaUri = uri
            facturaNombre = obtenerNombreArchivo(
                context = context,
                uri = uri
            )

            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Algunos proveedores no permiten permisos persistentes.
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        containerColor = colors.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Agregar producto",
                        color = colors.ink,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = InterFont
                    )

                    Text(
                        text = "Pedido internacional #${order.id ?: 0}",
                        color = colors.muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(
                    text = "La nueva compra será guardada y asociada directamente con este pedido.",
                    color = colors.textSoft,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFont
                )

                OutlinedTextField(
                    value = webCompra,
                    onValueChange = { webCompra = it },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tienda o página web") },
                    placeholder = {
                        Text("Amazon, Temu, AliExpress...")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = nombreProducto,
                    onValueChange = { nombreProducto = it },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Producto") },
                    placeholder = {
                        Text("Ejemplo: zapatillas, celular...")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = tracking,
                    onValueChange = { tracking = it },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tracking") },
                    placeholder = {
                        Text("Número de seguimiento")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        label = { Text("Precio USD") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = pesoEstimado,
                        onValueChange = { pesoEstimado = it },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        label = { Text("Peso kg") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Column {
                    Text(
                        text = "FACTURA",
                        color = colors.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = InterFont,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    OutlinedButton(
                        onClick = {
                            facturaLauncher.launch(
                                arrayOf("application/pdf")
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (facturaUri != null) {
                                PcGreen
                            } else {
                                colors.line
                            }
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (facturaUri != null) {
                                PcGreen
                            } else {
                                PcBlue
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (facturaUri != null) {
                                Icons.Outlined.Description
                            } else {
                                Icons.Outlined.UploadFile
                            },
                            contentDescription = null,
                            modifier = Modifier.size(21.dp)
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Text(
                            text = if (facturaNombre.isNotBlank()) {
                                facturaNombre
                            } else {
                                "Seleccionar factura"
                            },
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFont,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Formato permitido: PDF. Máximo 10 MB.",
                        color = colors.muted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont
                    )
                }

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp),
                    label = { Text("Comentario opcional") },
                    placeholder = {
                        Text("Color, talla, cantidad u otra indicación")
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.warningBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Solo puedes agregar productos mientras el pedido esté abierto y antes de ser despachado.",
                        color = colors.warningText,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val envioId = order.id ?: 0
                    val userEmail = sessionManager.getUserEmail()

                    if (envioId <= 0) {
                        Toast.makeText(
                            context,
                            "Pedido inválido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (userEmail.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "No se encontró la sesión",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (webCompra.isBlank()) {
                        Toast.makeText(
                            context,
                            "Ingresa la tienda o página web",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (nombreProducto.isBlank()) {
                        Toast.makeText(
                            context,
                            "Ingresa el nombre del producto",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (tracking.isBlank()) {
                        Toast.makeText(
                            context,
                            "Ingresa el tracking",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (facturaUri == null) {
                        Toast.makeText(
                            context,
                            "Selecciona la factura",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val precioNormalizado =
                        precio.trim().replace(",", ".")

                    val pesoNormalizado =
                        pesoEstimado.trim().replace(",", ".")

                    if (
                        precioNormalizado.isNotBlank() &&
                        precioNormalizado.toDoubleOrNull() == null
                    ) {
                        Toast.makeText(
                            context,
                            "El precio no es válido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (
                        pesoNormalizado.isNotBlank() &&
                        pesoNormalizado.toDoubleOrNull() == null
                    ) {
                        Toast.makeText(
                            context,
                            "El peso no es válido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    scope.launch {
                        try {
                            isSaving = true

                            val facturaPart = withContext(
                                Dispatchers.IO
                            ) {
                                crearFacturaMultipart(
                                    context = context,
                                    uri = facturaUri!!,
                                    nombreOriginal = facturaNombre
                                )
                            }

                            val textPlain =
                                "text/plain".toMediaTypeOrNull()

                            val response =
                                RetrofitClient.instance
                                    .addInternationalProduct(
                                        envioId = envioId
                                            .toString()
                                            .toRequestBody(textPlain),
                                        userEmail = userEmail
                                            .toRequestBody(textPlain),
                                        webCompra = webCompra
                                            .trim()
                                            .toRequestBody(textPlain),
                                        producto = nombreProducto
                                            .trim()
                                            .toRequestBody(textPlain),
                                        tracking = tracking
                                            .trim()
                                            .toRequestBody(textPlain),
                                        precioCompra = precioNormalizado
                                            .toRequestBody(textPlain),
                                        pesoEstimado = pesoNormalizado
                                            .toRequestBody(textPlain),
                                        comentario = comentario
                                            .trim()
                                            .toRequestBody(textPlain),
                                        facturaPdf = facturaPart
                                    )

                            isSaving = false

                            if (response.success) {
                                Toast.makeText(
                                    context,
                                    response.message
                                        ?: "Producto agregado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onSubmitted()
                            } else {
                                Toast.makeText(
                                    context,
                                    response.message
                                        ?: "No se pudo agregar el producto",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            isSaving = false

                            Toast.makeText(
                                context,
                                "Error al agregar: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color.White,
                    disabledContainerColor =
                        Color(0xFFF59E0B).copy(alpha = 0.55f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Agregar producto",
                        fontWeight = FontWeight.Black,
                        fontFamily = InterFont
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = colors.muted,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFont
                )
            }
        }
    )
}

private fun obtenerNombreArchivo(
    context: android.content.Context,
    uri: Uri
): String {
    var nombre = "factura"

    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        val index =
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (index >= 0 && cursor.moveToFirst()) {
            nombre = cursor.getString(index)
        }
    }

    return nombre
}

private fun crearFacturaMultipart(
    context: android.content.Context,
    uri: Uri,
    nombreOriginal: String
): MultipartBody.Part {
    val mimeType =
        context.contentResolver.getType(uri)
            ?: "application/octet-stream"

    val extension = when (mimeType) {
        "application/pdf" -> ".pdf"
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> ""
    }

    val nombreSeguro = nombreOriginal
        .ifBlank { "factura$extension" }
        .replace(
            Regex("[^A-Za-z0-9._-]"),
            "_"
        )

    val archivoTemporal = File(
        context.cacheDir,
        "${System.currentTimeMillis()}_$nombreSeguro"
    )

    context.contentResolver
        .openInputStream(uri)
        ?.use { input ->
            archivoTemporal.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        ?: throw IllegalStateException(
            "No se pudo leer la factura seleccionada"
        )

    val requestBody = archivoTemporal.asRequestBody(
        mimeType.toMediaTypeOrNull()
    )

    return MultipartBody.Part.createFormData(
        name = "factura_pdf",
        filename = nombreSeguro,
        body = requestBody
    )
}

@Composable
private fun InternationalCurrentStatusCard(
    order: Order,
    title: String,
    description: String,
    pasoVisual: String,
    delivered: Boolean
) {
    val colors = misPedidosColors()
    val estado = normalizarEstado(order.estado)

    val accent = colorPasoVisual(
        pasoVisual = pasoVisual,
        estado = estado
    )

    val icon = iconoPasoVisual(
        pasoVisual = pasoVisual,
        estado = estado
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = 0.36f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.17f),
                            colors.card
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(135.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-34).dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.86f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "ESTADO ACTUAL",
                            color = colors.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = InterFont,
                            letterSpacing = 0.6.sp
                        )

                        Text(
                            text = title,
                            color = colors.ink,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = InterFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = description,
                    color = colors.textSoft,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFont,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Actualizado: ${
                            formatFecha(
                                order.updated_at
                                    ?: order.created_at
                            )
                        }",
                        color = colors.muted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }
            }
        }
    }
}

@Composable
private fun InternationalStageItem(
    stage: InternationalVisualStage,
    completed: Boolean,
    current: Boolean,
    delivered: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = misPedidosColors()

    val background = when {
        delivered -> PcGreen
        current -> PcRed
        completed -> PcBlue
        else -> colors.bg
    }

    val borderColor = when {
        delivered -> PcGreen
        current -> PcRed
        completed -> PcBlue
        else -> colors.line
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    2.dp,
                    borderColor,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (completed || delivered) {
                    Icons.Outlined.Check
                } else {
                    stage.icon
                },
                contentDescription = null,
                tint = if (completed || current || delivered) {
                    Color.White
                } else {
                    colors.placeholder
                },
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = stage.label.uppercase(),
            color = when {
                delivered -> PcGreen
                current -> PcRed
                completed -> PcBlue
                else -> colors.placeholder
            },
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            fontFamily = InterFont,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun InternationalHistoryCard(
    currentState: String
) {
    val colors = misPedidosColors()
    var expanded by remember(currentState) {
        mutableStateOf(false)
    }

    val history = internationalHistorySteps()
    val currentIndex = history.indexOfFirst {
        currentState in it.codes
    }.let {
        if (it < 0) 0 else it
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.blueTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = PcBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HISTORIAL DEL PEDIDO",
                        color = colors.muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = InterFont,
                        letterSpacing = 0.4.sp
                    )

                    Text(
                        text = if (expanded) {
                            "Ocultar estados"
                        } else {
                            "Ver detalle completo"
                        },
                        color = colors.ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFont
                    )
                }

                Text(
                    text = if (expanded) "⌃" else "⌄",
                    color = PcBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                history.forEachIndexed { index, step ->
                    val completed =
                        index < currentIndex ||
                                currentState == "entregado"

                    val current =
                        index == currentIndex &&
                                currentState != "entregado"

                    val isLast =
                        index == history.lastIndex

                    InternationalHistoryRow(
                        label = step.label,
                        completed = completed,
                        current = current,
                        delivered = currentState == "entregado" &&
                                isLast,
                        showLine = !isLast
                    )
                }
            }
        }
    }
}

@Composable
private fun InternationalHistoryRow(
    label: String,
    completed: Boolean,
    current: Boolean,
    delivered: Boolean,
    showLine: Boolean
) {
    val colors = misPedidosColors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            delivered -> PcGreen
                            current -> PcRed
                            completed -> PcBlue
                            else -> colors.bg
                        }
                    )
                    .border(
                        2.dp,
                        when {
                            delivered -> PcGreen
                            current -> PcRed
                            completed -> PcBlue
                            else -> colors.line
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    completed || delivered -> {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    current -> {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(
                            if (completed) {
                                PcBlue.copy(alpha = 0.55f)
                            } else {
                                colors.line
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            color = when {
                delivered -> PcGreen
                current -> PcRed
                completed -> colors.ink
                else -> colors.placeholder
            },
            fontSize = 13.5.sp,
            fontWeight = when {
                current || delivered -> FontWeight.ExtraBold
                completed -> FontWeight.Bold
                else -> FontWeight.Medium
            },
            fontFamily = InterFont,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

private fun internationalHistorySteps():
        List<InternationalHistoryStep> {
    return listOf(
        InternationalHistoryStep(
            codes = setOf(
                "pendiente_revision",
                "pedido_registrado"
            ),
            label = "Pedido internacional registrado"
        ),
        InternationalHistoryStep(
            codes = setOf("en_revision"),
            label = "En revisión"
        ),
        InternationalHistoryStep(
            codes = setOf("esperando_almacen"),
            label = "Esperando llegada al almacén"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "recibido_almacen",
                "recibido_en_almacen",
                "en_almacen"
            ),
            label = "Recibido en almacén"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "consolidacion",
                "en_consolidacion"
            ),
            label = "En consolidación"
        ),
        InternationalHistoryStep(
            codes = setOf("despachado"),
            label = "Despachado desde origen"
        ),
        InternationalHistoryStep(
            codes = setOf("transito_internacional"),
            label = "En tránsito internacional"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "llegado_peru",
                "llego_a_peru"
            ),
            label = "Llegó al Perú"
        ),
        InternationalHistoryStep(
            codes = setOf("desaduanaje"),
            label = "En proceso de desaduanaje"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "esperando_pago_impuestos",
                "pago_de_impuestos"
            ),
            label = "Pago de impuestos, si corresponde"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "liberado_aduanas",
                "liberado_por_aduanas"
            ),
            label = "Liberado por aduanas"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "distribucion",
                "en_distribucion"
            ),
            label = "En distribución"
        ),
        InternationalHistoryStep(
            codes = setOf(
                "ruta_entrega",
                "en_ruta"
            ),
            label = "En ruta de entrega"
        ),
        InternationalHistoryStep(
            codes = setOf("entregado"),
            label = "Entregado"
        )
    )
}

private fun puedeAgregarProductoInternacional(
    estado: String
): Boolean {
    return estado in setOf(
        "esperando_almacen",
        "recibido_almacen",
        "recibido_en_almacen",
        "en_almacen",
        "consolidacion",
        "en_consolidacion"
    )
}

private fun resolverPasoVisualInternacional(
    pasoVisualApi: String?,
    estado: String
): String {
    val api = normalizarEstado(pasoVisualApi)

    if (api.isNotBlank()) {
        return when (api) {
            "aduanas" -> "desaduanaje"
            "entrega" -> "distribucion"
            else -> api
        }
    }

    return when (estado) {
        "pendiente_revision",
        "pedido_registrado",
        "en_revision" -> "registro"

        "esperando_almacen",
        "recibido_almacen",
        "recibido_en_almacen",
        "en_almacen",
        "consolidacion",
        "en_consolidacion" -> "almacen"

        "despachado",
        "transito_internacional" -> "despacho"

        "llegado_peru",
        "llego_a_peru",
        "desaduanaje",
        "esperando_pago_impuestos",
        "pago_de_impuestos",
        "liberado_aduanas",
        "liberado_por_aduanas" -> "desaduanaje"

        "distribucion",
        "en_distribucion",
        "ruta_entrega",
        "en_ruta" -> "distribucion"

        "entregado" -> "entregado"

        else -> "registro"
    }
}

private fun colorPasoVisual(
    pasoVisual: String,
    estado: String
): Color {
    return when {
        estado == "entregado" ||
                pasoVisual == "entregado" -> PcGreen

        estado == "cancelado" ||
                estado == "cancelado_cliente" -> PcRed

        pasoVisual == "registro" -> PcBlue
        pasoVisual == "almacen" -> Color(0xFFF59E0B)
        pasoVisual == "despacho" -> PcBlue
        pasoVisual == "desaduanaje" ||
                pasoVisual == "aduanas" -> Color(0xFF7C3AED)

        else -> PcRed
    }
}

private fun iconoPasoVisual(
    pasoVisual: String,
    estado: String
): ImageVector {
    return when {
        estado == "entregado" ||
                pasoVisual == "entregado" ->
            Icons.Outlined.CheckCircle

        pasoVisual == "registro" ->
            Icons.Outlined.Schedule

        pasoVisual == "almacen" ->
            Icons.Outlined.Inventory2

        pasoVisual == "despacho" ->
            Icons.Outlined.Language

        pasoVisual == "desaduanaje" ||
                pasoVisual == "aduanas" ->
            Icons.Outlined.QrCode2

        else ->
            Icons.Outlined.Route
    }
}

private fun descripcionEstadoInternacional(
    estado: String
): String {
    return when (estado) {
        "pendiente_revision" ->
            "Registramos tu solicitud internacional y será revisada por nuestro equipo."

        "en_revision" ->
            "Estamos validando los datos, productos y documentos de tu pedido."

        "esperando_almacen" ->
            "Estamos esperando que tu compra llegue a nuestro almacén de origen."

        "recibido_almacen",
        "recibido_en_almacen",
        "en_almacen" ->
            "Tu compra ya fue recibida y registrada en nuestro almacén."

        "consolidacion",
        "en_consolidacion" ->
            "Estamos agrupando tus compras antes del despacho hacia Perú."

        "despachado" ->
            "Tu carga salió de nuestro almacén y fue preparada para el transporte internacional."

        "transito_internacional" ->
            "Tu pedido está viajando hacia Perú."

        "llegado_peru",
        "llego_a_peru" ->
            "Tu pedido llegó al Perú y será procesado por aduanas."

        "desaduanaje" ->
            "Estamos realizando el proceso de revisión y liberación aduanera."

        "esperando_pago_impuestos",
        "pago_de_impuestos" ->
            "El pedido requiere el pago de impuestos para continuar con su liberación."

        "liberado_aduanas",
        "liberado_por_aduanas" ->
            "Tu pedido fue liberado y será trasladado a distribución."

        "distribucion",
        "en_distribucion" ->
            "Estamos preparando tu pedido para la entrega local."

        "ruta_entrega",
        "en_ruta" ->
            "Tu pedido está en ruta hacia la dirección de entrega."

        "entregado" ->
            "Tu pedido internacional fue entregado correctamente."

        "cancelado",
        "cancelado_cliente" ->
            "El pedido internacional fue cancelado."

        else ->
            "Tu pedido continúa avanzando dentro del proceso internacional."
    }
}

private fun descripcionEstadoNacional(
    estado: String
): String {
    return when (estado) {
        "pendiente_pago",
        "esperando_repartidor" ->
            "Estamos buscando un repartidor para tu pedido."

        "asignado" ->
            "Tu pedido ya tiene un repartidor asignado."

        "recogiendo" ->
            "El repartidor se dirige al punto de recojo."

        "recogido" ->
            "El paquete fue recogido correctamente."

        "en_camino",
        "en_transito" ->
            "Tu pedido está en camino hacia el destino."

        "entregado" ->
            "Tu pedido fue entregado correctamente."

        "cancelado",
        "cancelado_cliente" ->
            "Este pedido fue cancelado."

        else ->
            "Tu pedido está siendo procesado."
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

private fun normalizarEstadoPago(estadoPago: String?): String {
    return estadoPago
        ?.trim()
        ?.lowercase()
        ?.replace(" ", "_")
        ?.replace("-", "_")
        .orEmpty()
        .ifBlank { "pendiente" }
}

private fun solicitarPagoYabrirWhatsApp(
    context: android.content.Context,
    order: Order,
    userEmail: String,
    onUpdated: () -> Unit
) {
    val envioId = order.id ?: 0

    if (envioId <= 0 || userEmail.isBlank()) {
        Toast.makeText(
            context,
            "No se pudo identificar el pedido o la sesión",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    RetrofitClient.instance
        .solicitarPagoInternacional(
            envioId = envioId,
            userEmail = userEmail
        )
        .enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                val result = response.body()

                if (response.isSuccessful && result?.success == true) {
                    onUpdated()
                    abrirWhatsAppPago(
                        context = context,
                        orderId = envioId,
                        total = order.total?.toString() ?: "0.00"
                    )
                } else {
                    Toast.makeText(
                        context,
                        result?.message ?: "No se pudo iniciar el pago",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<BasicResponse>,
                t: Throwable
            ) {
                Toast.makeText(
                    context,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
}

private fun abrirWhatsAppPago(
    context: android.content.Context,
    orderId: Int,
    total: String
) {
    val telefono = "51967929967"
    val mensaje = Uri.encode(
        """
        Hola Perucho Courier 👋

        Deseo realizar el pago de mi pedido internacional.

        Pedido: #$orderId
        Total: $$total

        Por favor, indíquenme los datos para realizar el pago.
        """.trimIndent()
    )

    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$telefono?text=$mensaje")
            )
        )
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "No se pudo abrir WhatsApp",
            Toast.LENGTH_SHORT
        ).show()
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