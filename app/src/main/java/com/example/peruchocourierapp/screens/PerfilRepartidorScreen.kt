package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
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

@Composable
fun PerfilRepartidorScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

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
            .background(GrayBg)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .background(Brush.linearGradient(listOf(BlueDark, Blue, BlueMid)))
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
                color = Dark,
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
                    color = GrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProfileSectionRepartidor(
                title = "Información personal",
                icon = Icons.Outlined.Person
            ) {
                LockedProfileFieldRepartidor("Nombre completo", name, Icons.Outlined.Person)
                LockedProfileFieldRepartidor("DNI / Documento", dni, Icons.Outlined.Badge)
                LockedProfileFieldRepartidor("Correo electrónico", email, Icons.Outlined.Email)
                LockedProfileFieldRepartidor("Teléfono", phone, Icons.Outlined.Phone)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionRepartidor(
                title = "Operación",
                icon = Icons.Outlined.LocalShipping
            ) {
                InfoRowRepartidor("Cuenta", "Repartidor")
                InfoRowRepartidor("Pedidos activos", "Revisar pedido en curso")
                InfoRowRepartidor("Historial", "Disponible en Mis entregas")
            }

            Spacer(modifier = Modifier.height(12.dp))

            WarningBoxRepartidor()

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
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Red
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
                color = GrayLight,
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
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrayBorder)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlueLight),
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
                    color = Dark,
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
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Text(
        text = label.uppercase(),
        color = GrayText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(GrayBg)
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GrayLight,
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = value,
            color = Dark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        ProfileTagRepartidor("Fijo", true)
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ProfileTagRepartidor(
    text: String,
    locked: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (locked) GrayBorder else BlueLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(11.dp)
            )

            Spacer(modifier = Modifier.width(3.dp))
        }

        Text(
            text = text,
            color = if (locked) GrayText else Blue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun WarningBoxRepartidor() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(YellowBg)
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = "Para cambiar tus datos personales, solicítalo con administración por WhatsApp.",
            color = YellowText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun InfoRowRepartidor(
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
            color = GrayText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Dark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}