package com.example.peruchocourierapp.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.DriverDashboardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.scale
import com.example.peruchocourierapp.theme.ThemeManager
private val RedCard = Color(0xFFFF1C24)
private val Red = Color(0xFFE02020)
private val Green = Color(0xFF22C55E)
private val Blue = Color(0xFF1E4FC7)

@Composable
fun DriverLobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val background = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textDark = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val textSoft = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    val userName = sessionManager.getUserName()?.takeIf { it.isNotBlank() } ?: "Usuario"
    val driverEmail = sessionManager.getUserEmail()?.trim().orEmpty()
    val initials = getInitials(userName)

    var gananciasHoy by remember { mutableStateOf("0.00") }
    var entregasHoy by remember { mutableStateOf("0") }
    var activos by remember { mutableStateOf("0") }
    var disponibles by remember { mutableStateOf("0") }
    var entregados by remember { mutableStateOf("0") }

    LaunchedEffect(driverEmail) {
        if (driverEmail.isBlank()) return@LaunchedEffect

        RetrofitClient.instance.getDriverDashboardStats(driverEmail)
            .enqueue(object : Callback<DriverDashboardResponse> {
                override fun onResponse(
                    call: Call<DriverDashboardResponse>,
                    response: Response<DriverDashboardResponse>
                ) {
                    val result = response.body()
                    if (response.isSuccessful && result?.success == true) {
                        activos = result.activos.toString()
                        disponibles = result.disponibles.toString()
                        entregados = result.entregados.toString()
                        entregasHoy = result.entregas_hoy.toString()
                        gananciasHoy = "%.2f".format(result.ganancias_hoy)
                    }
                }

                override fun onFailure(call: Call<DriverDashboardResponse>, t: Throwable) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_perucho2),
                contentDescription = "Perucho Courier",
                modifier = Modifier
                    .width(260.dp)
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(RedCard),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hola de nuevo",
            color = textMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = userName,
                color = textDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )

            DriverThemeToggle()
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DriverStat(activos, "Activos", Modifier.weight(1f), Color(0xFF579DFF), cardBg, cardBorder, textMuted)
            DriverStat(disponibles, "Disponibles", Modifier.weight(1f), Color(0xFFFF6B7A), cardBg, cardBorder, textMuted)
            DriverStat(entregados, "Entregados", Modifier.weight(1f), Green, cardBg, cardBorder, textMuted)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DriverCard(
                title = "Pedidos\ndisponibles",
                subtitle = "Acepta y gana",
                icon = Icons.Outlined.Inbox,
                iconColor = Blue,
                modifier = Modifier.weight(1f),
                badge = if (disponibles != "0") disponibles else null,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textDark = textDark,
                textMuted = textMuted
            ) {
                navController.navigate("pedidos_disponibles")
            }

            DriverCard(
                title = "Pedido\nactivo",
                subtitle = "Ver en curso",
                icon = Icons.Outlined.DeliveryDining,
                iconColor = RedCard,
                modifier = Modifier.weight(1f),
                cardBg = cardBg,
                cardBorder = cardBorder,
                textDark = textDark,
                textMuted = textMuted
            ) {
                abrirPedidoActivoConNavegacion(
                    context = context,
                    navController = navController,
                    driverEmail = driverEmail
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DriverCard(
                title = "Mis entregas",
                subtitle = "Historial",
                icon = Icons.Outlined.Assignment,
                iconColor = Blue,
                modifier = Modifier.weight(1f),
                cardBg = cardBg,
                cardBorder = cardBorder,
                textDark = textDark,
                textMuted = textMuted
            ) {
                navController.navigate("mis_entregas")
            }

            DriverCard(
                title = "Mi perfil",
                subtitle = "Datos",
                icon = Icons.Outlined.AccountCircle,
                iconColor = Blue,
                modifier = Modifier.weight(1f),
                cardBg = cardBg,
                cardBorder = cardBorder,
                textDark = textDark,
                textMuted = textMuted
            ) {
                navController.navigate("perfil_repartidor")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ganancias hoy",
                    color = textMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "S/ $gananciasHoy",
                    color = Green,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "$entregasHoy entrega${if (entregasHoy == "1") "" else "s"}",
                    color = textSoft,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "💰",
                fontSize = 38.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .clickable {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                tint = Red
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Cerrar sesión",
                color = Red,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "HOME REPARTIDOR",
            color = textSoft,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

private fun abrirPedidoActivoConNavegacion(
    context: Context,
    navController: NavController,
    driverEmail: String
) {
    if (driverEmail.isBlank()) return

    RetrofitClient.instance.getActiveOrder(driverEmail)
        .enqueue(object : Callback<ActiveOrderResponse> {
            override fun onResponse(
                call: Call<ActiveOrderResponse>,
                response: Response<ActiveOrderResponse>
            ) {
                val result = response.body()
                val order = result?.order

                if (response.isSuccessful && result?.success == true && order != null) {
                    val estado = normalizarEstadoLobby(order.estado)

                    val latLng = when (estado) {
                        "asignado", "recogiendo", "recogido" -> {
                            val lat = order.pickup_lat?.toDoubleOrNull()
                            val lng = order.pickup_lng?.toDoubleOrNull()
                            if (lat != null && lng != null) Pair(lat, lng) else null
                        }

                        "en_camino", "en_transito" -> {
                            val lat = order.dropoff_lat?.toDoubleOrNull()
                            val lng = order.dropoff_lng?.toDoubleOrNull()
                            if (lat != null && lng != null) Pair(lat, lng) else null
                        }

                        else -> null
                    }

                    if (latLng != null) {
                        abrirNavegacion(context, latLng.first, latLng.second)
                    }
                }

                navController.navigate("pedido_en_curso/${Uri.encode(driverEmail)}")
            }

            override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                navController.navigate("pedido_en_curso/${Uri.encode(driverEmail)}")
            }
        })
}

private fun abrirNavegacion(
    context: Context,
    lat: Double,
    lng: Double
) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
    val intent = Intent(Intent.ACTION_VIEW, uri)

    val chooser = Intent.createChooser(
        intent,
        "Abrir con"
    )

    context.startActivity(chooser)
}

private fun normalizarEstadoLobby(estado: String?): String {
    return when ((estado ?: "").trim().lowercase()) {
        "recogiendo" -> "recogido"
        "en_transito" -> "en_camino"
        else -> (estado ?: "").trim().lowercase()
    }
}

@Composable
private fun DriverStat(
    number: String,
    label: String,
    modifier: Modifier,
    numberColor: Color,
    cardBg: Color,
    cardBorder: Color,
    textMuted: Color
) {
    Column(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            color = numberColor,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )

        Text(
            text = label,
            color = textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DriverCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier,
    iconColor: Color = Color(0xFF1E4FC7),
    badge: String? = null,
    cardBg: Color,
    cardBorder: Color,
    textDark: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(142.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(38.dp)
            )

            Column {
                Text(
                    text = title,
                    color = textDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 13.sp
                )
            }
        }

        if (!badge.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50.dp))
                    .background(RedCard)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "US"
    }
}
@Composable
private fun DriverThemeToggle() {

    val context = LocalContext.current
    val dark = ThemeManager.isDarkMode.value

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Outlined.LightMode,
            contentDescription = null,
            tint = if (!dark)
                Color(0xFFFFD54F)
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f),
            modifier = Modifier.size(18.dp)
        )

        Switch(
            checked = dark,
            onCheckedChange = {
                ThemeManager.setDark(context, it)
            },
            modifier = Modifier.scale(0.78f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF1E40AF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )

        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = null,
            tint = if (dark)
                Color(0xFF93C5FD)
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.40f),
            modifier = Modifier.size(18.dp)
        )
    }
}