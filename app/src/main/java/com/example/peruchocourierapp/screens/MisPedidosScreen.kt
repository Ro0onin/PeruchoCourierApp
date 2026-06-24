package com.example.peruchocourierapp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.launch
import java.text.Normalizer
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
private val PcBlue = Color(0xFF1A4FBF)
private val PcBlueDark = Color(0xFF0B2E78)
private val PcBlueMid = Color(0xFF2D6BE4)
private val PcBlueLight = Color(0xFFEAF2FF)
private val PcRed = Color(0xFFFF1C24)
private val PcRedSoft = Color(0xFFFFE8EA)
private val Bg = Color(0xFFF5F7FB)
private val CardBorder = Color(0xFFE7ECF5)
private val Dark = Color(0xFF111827)
private val Muted = Color(0xFF667294)
private val SoftText = Color(0xFFA0A8BC)
private val SuccessBg = Color(0xFFD1FAE5)
private val SuccessText = Color(0xFF067647)
private val WarningBg = Color(0xFFFFF3CD)
private val WarningText = Color(0xFF856404)
private val DangerBg = Color(0xFFFEE2E2)
private val DangerText = Color(0xFF991B1B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(navController: NavController) {
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
        else -> orders
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .navigationBarsPadding()
    ) {
        HeaderMisPedidos(
            totalPedidos = orders.size,
            onBack = { navController.popBackStack() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChipButton("Todos", selectedFilter) { selectedFilter = "Todos" }
            FilterChipButton("Nacionales", selectedFilter) { selectedFilter = "Nacionales" }
            FilterChipButton("Internacionales", selectedFilter) { selectedFilter = "Internacionales" }
            FilterChipButton("Entregados", selectedFilter) { selectedFilter = "Entregados" }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando pedidos...", color = Muted, fontWeight = FontWeight.Bold)
                }
            }

            errorMessage.isNotBlank() -> {
                Text(
                    text = errorMessage,
                    color = PcRed,
                    modifier = Modifier.padding(18.dp),
                    fontWeight = FontWeight.Bold
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredOrders) { order ->
                        PedidoPremiumCard(
                            order = order,
                            onClick = { selectedOrder = order }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PERUCHO COURIER",
                            color = SoftText.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
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

    if (selectedOrder != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedOrder = null },
            containerColor = Bg
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
                    color = Dark
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
                        color = Muted,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "¿Deseas continuar con la cancelación?",
                        color = Dark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
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
            containerColor = Color.White
        )
    }
}

@Composable
private fun HeaderMisPedidos(
    totalPedidos: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .background(
                Brush.linearGradient(
                    listOf(PcBlueDark, PcBlue, PcBlueMid)
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Volver",
                    modifier = Modifier.size(23.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mis pedidos",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "$totalPedidos envío${if (totalPedidos == 1) "" else "s"} registrados",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected == text) PcBlue else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected == text) Color.White else Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PedidoPremiumCard(
    order: Order,
    onClick: () -> Unit
) {
    val isNacional = order.tipo_envio == "nacional" || order.tipo_envio.isNullOrBlank()
    val estado = normalizarEstado(order.estado)
    val totalText = if (isNacional) "S/ ${order.total ?: "-"}" else "$${order.total ?: "-"}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_perucho2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.12f
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isNacional) PcBlueLight else PcRedSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isNacional) Icons.Outlined.Inventory2 else Icons.Outlined.Language,
                            contentDescription = null,
                            tint = if (isNacional) PcBlue else PcRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pedido #${order.id ?: 0}",
                            color = Dark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = formatFecha(order.created_at),
                            color = SoftText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        tint = SoftText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(estado)
                    TypeBadge(if (isNacional) "Nacional" else "Internacional")
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isNacional) {
                    RouteMiniLine(
                        pickup = order.pickup_address ?: order.origen ?: "-",
                        dropoff = order.dropoff_address ?: order.destino ?: "-"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoLine(
                        icon = Icons.Outlined.Inventory2,
                        text = "${order.categoria ?: "-"} · ${order.tamano_paquete ?: "-"}"
                    )
                } else {
                    InfoLine(
                        icon = Icons.Outlined.Language,
                        text = "Compra en ${order.web_compra ?: "-"}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoLine(
                        icon = Icons.Outlined.QrCode2,
                        text = "Tracking: ${order.tracking ?: "-"}"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL",
                            color = SoftText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )

                        Text(
                            text = totalText,
                            color = PcBlueDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(PcBlueLight)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = order.metodo_pago ?: "Pago",
                            color = PcBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMiniLine(
    pickup: String,
    dropoff: String
) {
    Column {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Recojo", color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(
                    text = pickup,
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(PcRed),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Entrega", color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(
                    text = dropoff,
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoLine(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PcBlue,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusBadge(estado: String) {
    val bg: Color
    val fg: Color
    val label: String

    when (estado) {
        "pendiente_pago" -> {
            bg = WarningBg
            fg = WarningText
            label = "Pendiente pago"
        }

        "pendiente_revision" -> {
            bg = PcBlueLight
            fg = PcBlue
            label = "En revisión"
        }

        "esperando_almacen" -> {
            bg = PcBlueLight
            fg = PcBlue
            label = "Esperando almacén"
        }

        "en_almacen" -> {
            bg = PcBlueLight
            fg = PcBlue
            label = "En almacén"
        }

        "en_transito", "asignado", "transito", "recogido", "recogiendo", "en_camino" -> {
            bg = PcBlueLight
            fg = PcBlue
            label = "En tránsito"
        }

        "listo_entrega" -> {
            bg = SuccessBg
            fg = SuccessText
            label = "Listo entrega"
        }

        "entregado" -> {
            bg = SuccessBg
            fg = SuccessText
            label = "Entregado"
        }

        "cancelado", "cancelado_cliente" -> {
            bg = DangerBg
            fg = DangerText
            label = if (estado == "cancelado_cliente") "Cancelado por cliente" else "Cancelado"
        }

        else -> {
            bg = Bg
            fg = Muted
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
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun TypeBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(PcBlue)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EmptyOrdersBox(selectedFilter: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(PcBlueLight),
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
                    color = Dark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "No encontramos pedidos en la categoría $selectedFilter.",
                    color = Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
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
    val context = LocalContext.current
    val isNacional = order.tipo_envio == "nacional" || order.tipo_envio.isNullOrBlank()
    val estado = normalizarEstado(order.estado)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .verticalScroll(rememberScrollState())
            .background(Bg)
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
                    color = Color(0xFF22C55E)
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
                        "Vehículo" to (order.tipo_vehiculo ?: "-"),
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
                Text("Rastrear pedido", fontWeight = FontWeight.Black)
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
                Text("Cancelar pedido", fontWeight = FontWeight.Black)
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
            Text("Contactar soporte", fontWeight = FontWeight.Black)
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
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(PcBlueDark, PcBlue, PcBlueMid)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Pedido #${order.id ?: 0}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatFecha(order.created_at),
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(estado)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.20f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isNacional) "Nacional" else "Internacional",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

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
                            value = order.tipo_vehiculo ?: "-",
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Bg)
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
            color = Muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )

        Text(
            text = value,
            color = Dark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
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
    val steps = if (isNacional) {
        listOf(
            "Pedido creado" to true,
            "Pago confirmado" to (estado != "pendiente_pago"),
            "Recojo asignado" to (estado in listOf("asignado", "recogiendo", "recogido", "en_camino", "entregado")),
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
                    Icon(
                        imageVector = if (item.second) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (item.second) PcBlue else SoftText,
                        modifier = Modifier.size(21.dp)
                    )

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(if (item.second) PcBlue else CardBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.first,
                    color = if (item.second) Dark else Muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PcBlueLight),
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
                    color = Dark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
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
                        .background(Color.White)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(CardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = label.uppercase(),
                color = Muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Text(
                text = value,
                color = Dark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Bg)
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = Muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = Dark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
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
                .background(PcBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Call, null, tint = PcBlue, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(phone, color = Dark, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label.uppercase(),
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            color = Dark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color = CardBorder,
        modifier = Modifier.padding(vertical = 10.dp)
    )
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
        "pendiente_pago" -> "Pendiente pago"
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
