package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.theme.ThemeManager

private val Blue = Color(0xFF1A4FBF)
private val BlueDark = Color(0xFF0D3280)
private val BlueMid = Color(0xFF2D6BE4)
private val BlueLight = Color(0xFFE8EFFE)
private val Red = Color(0xFFE02020)
private val GrayBg = Color(0xFFF4F6FB)
private val GrayBorder = Color(0xFFE8ECF4)
private val GrayText = Color(0xFF6B7A99)
private val GrayLight = Color(0xFFB0BAD0)
private val Dark = Color(0xFF1A2340)
private val YellowBg = Color(0xFFFEF3C7)
private val YellowText = Color(0xFF92400E)

private data class PerfilRepartidorColors(
    val background: Color,
    val card: Color,
    val fieldBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val lightText: Color,
    val blueLight: Color,
    val tagLockedBg: Color,
    val warningBg: Color,
    val warningText: Color,
    val warningIcon: Color,
    val logoutBorder: Color
)

@Composable
private fun perfilRepartidorColors(): PerfilRepartidorColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        PerfilRepartidorColors(
            background = Color(0xFF0F172A),
            card = Color(0xFF111827),
            fieldBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            lightText = Color(0xFF94A3B8),
            blueLight = Color(0xFF172554),
            tagLockedBg = Color(0xFF334155),
            warningBg = Color(0xFF451A03),
            warningText = Color(0xFFFDE68A),
            warningIcon = Color(0xFFFBBF24),
            logoutBorder = Color(0xFF7F1D1D)
        )
    } else {
        PerfilRepartidorColors(
            background = GrayBg,
            card = Color.White,
            fieldBg = GrayBg,
            border = GrayBorder,
            text = Dark,
            muted = GrayText,
            lightText = GrayLight,
            blueLight = BlueLight,
            tagLockedBg = GrayBorder,
            warningBg = YellowBg,
            warningText = YellowText,
            warningIcon = Color(0xFFD97706),
            logoutBorder = GrayBorder
        )
    }
}

@Composable
fun PerfilRepartidorScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = perfilRepartidorColors()

    val name = sessionManager.getUserName() ?: "Repartidor"
    val email = sessionManager.getUserEmail() ?: "-"
    val phone = sessionManager.getUserPhone() ?: "-"
    val dni = sessionManager.getUserDni() ?: "-"

    val initials = name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "R" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
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
                            listOf(BlueDark, Blue, BlueMid)
                        )
                    }
                )
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Text(
                text = "Mi Perfil",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .offset(y = (-24).dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = name,
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeliveryDining,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Repartidor",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProfileSectionRepartidor(
                colors = colors,
                title = "Información personal",
                icon = Icons.Outlined.Person
            ) {
                LockedProfileFieldRepartidor(colors, "Nombre completo", name, Icons.Outlined.Person)
                LockedProfileFieldRepartidor(colors, "DNI / Documento", dni, Icons.Outlined.Badge)
                LockedProfileFieldRepartidor(colors, "Correo electrónico", email, Icons.Outlined.Email)
                LockedProfileFieldRepartidor(colors, "Teléfono", phone, Icons.Outlined.Phone)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionRepartidor(
                colors = colors,
                title = "Operación",
                icon = Icons.Outlined.LocalShipping
            ) {
                InfoRowRepartidor(colors, "Cuenta", "Repartidor")
                InfoRowRepartidor(colors, "Pedidos activos", "Revisar pedido en curso")
                InfoRowRepartidor(colors, "Historial", "Disponible en Mis entregas")
            }

            Spacer(modifier = Modifier.height(12.dp))

            WarningBoxRepartidor(colors)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    sessionManager.clearSession()

                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(15.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.logoutBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Red,
                    containerColor = colors.card
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = Red
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cerrar sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PERFIL DE REPARTIDOR",
                color = colors.lightText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ProfileSectionRepartidor(
    colors: PerfilRepartidorColors,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.blueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title.uppercase(),
                    color = colors.text,
                    fontSize = 12.sp,
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
private fun LockedProfileFieldRepartidor(
    colors: PerfilRepartidorColors,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Text(
        text = label.uppercase(),
        color = colors.muted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colors.fieldBg)
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.lightText,
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = value,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        ProfileTagRepartidor(colors, "Fijo", true)
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ProfileTagRepartidor(
    colors: PerfilRepartidorColors,
    text: String,
    locked: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (locked) colors.tagLockedBg else colors.blueLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(11.dp)
            )

            Spacer(modifier = Modifier.width(3.dp))
        }

        Text(
            text = text,
            color = if (locked) colors.muted else Blue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun WarningBoxRepartidor(
    colors: PerfilRepartidorColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colors.warningBg)
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = colors.warningIcon,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = "Para cambiar tus datos personales, solicítalo con administración por WhatsApp.",
            color = colors.warningText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun InfoRowRepartidor(
    colors: PerfilRepartidorColors,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = colors.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}
