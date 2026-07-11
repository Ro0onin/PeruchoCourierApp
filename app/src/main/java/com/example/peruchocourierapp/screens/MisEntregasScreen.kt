package com.example.peruchocourierapp.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.EntregaItem
import com.example.peruchocourierapp.models.GetDriverHistoryResponse
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

private data class DriverColors(
    val background: Color,
    val card: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val divider: Color,
    val bottomBar: Color,
    val bottomText: Color,
    val successBg: Color,
    val successText: Color,
    val personBg: Color,
    val bcpBg: Color,
    val bcpText: Color,
    val error: Color
)

@Composable
private fun driverColors(): DriverColors {
    val dark = ThemeManager.isDarkMode.value
    return if (dark) {
        DriverColors(
            background = Color(0xFF0F172A),
            card = Color(0xFF111827),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            divider = Color(0xFF1F2937),
            bottomBar = Color(0xFF111827),
            bottomText = Color(0xFF94A3B8),
            successBg = Color(0xFF14532D),
            successText = Color(0xFFDCFCE7),
            personBg = Color(0xFF172554),
            bcpBg = Color(0xFF451A03),
            bcpText = Color(0xFFFBBF24),
            error = Color(0xFFFFB4B4)
        )
    } else {
        DriverColors(
            background = DriverBg,
            card = Color.White,
            border = DriverBorder,
            text = DriverText,
            muted = DriverMuted,
            divider = DriverBg,
            bottomBar = Color.White,
            bottomText = Color(0xFFB0BAD0),
            successBg = Color(0xFFD1FAE5),
            successText = Color(0xFF065F46),
            personBg = Color(0xFFE8EFFE),
            bcpBg = Color(0xFFFFF4E8),
            bcpText = Color(0xFFD97706),
            error = DriverRed
        )
    }
}

@Composable
fun MisEntregasScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = driverColors()

    var entregas by remember { mutableStateOf<List<EntregaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val totalGanado = entregas.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
    val totalKm = entregas.sumOf { it.distanciaKm.toDoubleOrNull() ?: 0.0 }

    fun cargarHistorial() {
        val driverEmail = sessionManager.getUserEmail()

        if (driverEmail.isNullOrBlank()) {
            isLoading = false
            errorMessage = "No se encontró la sesión del repartidor"
            return
        }

        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getDriverDeliveryHistory(driverEmail)
            .enqueue(object : Callback<GetDriverHistoryResponse> {
                override fun onResponse(
                    call: Call<GetDriverHistoryResponse>,
                    response: Response<GetDriverHistoryResponse>
                ) {
                    isLoading = false

                    if (response.isSuccessful && response.body()?.success == true) {
                        entregas = response.body()?.orders ?: emptyList()
                    } else {
                        errorMessage = response.body()?.message ?: "No se pudo cargar el historial"
                    }
                }

                override fun onFailure(call: Call<GetDriverHistoryResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    LaunchedEffect(Unit) {
        cargarHistorial()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        HeaderMisEntregas(
            onBack = { navController.popBackStack() }
        )

        EstadisticasEntregas(
            colors = colors,
            entregas = entregas.size,
            ganado = totalGanado,
            kilometros = totalKm
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
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

                errorMessage.isNotEmpty() -> {
                    item {
                        Text(
                            text = errorMessage,
                            color = colors.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }

                entregas.isEmpty() -> {
                    item {
                        Text(
                            text = "Aún no tienes entregas realizadas",
                            color = colors.muted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }

                else -> {
                    items(entregas) { entrega ->
                        EntregaHistorialCardRediseñada(
                            colors = colors,
                            entrega = entrega
                        )
                    }
                }
            }
        }

        Text(
            text = "MIS ENTREGAS",
            color = colors.bottomText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bottomBar)
                .padding(vertical = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeaderMisEntregas(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                if (ThemeManager.isDarkMode.value) {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF020617),
                            Color(0xFF0F172A),
                            Color(0xFF1E293B)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            DriverBlueDark,
                            DriverBlue,
                            DriverBlueMid
                        )
                    )
                }
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .align(Alignment.TopEnd)
                .offset(x = 34.dp, y = (-34).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 34.dp),
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
                text = "Mis entregas",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EstadisticasEntregas(
    colors: DriverColors,
    entregas: Int,
    ganado: Double,
    kilometros: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBox(
            colors = colors,
            value = entregas.toString(),
            label = "Este mes",
            color = DriverBlue,
            modifier = Modifier.weight(1f)
        )

        StatBox(
            colors = colors,
            value = "S/ ${String.format("%.0f", ganado)}",
            label = "Ganado",
            color = Color(0xFF059669),
            modifier = Modifier.weight(1f)
        )

        StatBox(
            colors = colors,
            value = String.format("%.1f", kilometros),
            label = "Km totales",
            color = Color(0xFFF97316),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBox(
    colors: DriverColors,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, colors.border),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label.uppercase(),
                color = colors.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun EntregaHistorialCardRediseñada(
    colors: DriverColors,
    entrega: EntregaItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, colors.border),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.personBg),
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
                    EntregaRouteRow(
                        colors = colors,
                        color = DriverGreen,
                        text = entrega.pickupAddress,
                        strong = true
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 4.5.dp, top = 3.dp, bottom = 3.dp)
                            .width(1.5.dp)
                            .height(12.dp)
                            .background(colors.border)
                    )

                    EntregaRouteRow(
                        colors = colors,
                        color = DriverRed,
                        text = entrega.dropoffAddress,
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
                    text = "S/ ${entrega.total}",
                    color = DriverBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                MetaDotEntrega(colors)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.muted,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = "${entrega.distanciaKm} km",
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                MetaDotEntrega(colors)

                PagoEntregaBadge(
                    colors = colors,
                    payment = entrega.metodoPago
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(colors.successBg)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Entregado",
                        color = colors.successText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = entrega.fechaEntrega,
                    color = colors.bottomText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EntregaRouteRow(
    colors: DriverColors,
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
private fun MetaDotEntrega(
    colors: DriverColors
) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(colors.bottomText)
    )
}

@Composable
private fun PagoEntregaBadge(
    colors: DriverColors,
    payment: String
) {
    val isBcp = payment.equals("BCP", ignoreCase = true)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isBcp) colors.bcpBg else colors.personBg)
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