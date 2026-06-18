package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager

private val BluePrimary = Color(0xFF1A4FBF)
private val BlueLight = Color(0xFFE8EFFE)
private val RedPrimary = Color(0xFFE02020)
private val RedLight = Color(0xFFFFF0F0)
private val DarkText = Color(0xFF1A2340)
private val GrayText = Color(0xFF6B7A99)
private val GrayBorder = Color(0xFFE8ECF4)

@Composable
fun RoleSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val role = sessionManager.getUserRole() ?: "cliente"

    LaunchedEffect(role) {
        if (role != "repartidor") {
            navController.navigate("client_lobby") {
                popUpTo("role_selection") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (role != "repartidor") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BluePrimary)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AsyncImage(
            model = R.drawable.fondo_roles,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(230.dp))

            Text(
                text = "Elige cómo deseas ingresar",
                color = DarkText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tu cuenta tiene acceso como cliente y repartidor",
                color = GrayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(26.dp))

            RoleCard(
                title = "Entrar como cliente",
                subtitle = "Realiza pedidos y revisa tus seguimientos",
                iconBackground = BlueLight,
                selected = true,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(30.dp)
                    )
                },
                onClick = {
                    navController.navigate("client_lobby") {
                        popUpTo("role_selection") { inclusive = true }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleCard(
                title = "Entrar como repartidor",
                subtitle = "Acepta pedidos y gestiona tus entregas",
                iconBackground = RedLight,
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DeliveryDining,
                        contentDescription = null,
                        tint = RedPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                },
                onClick = {
                    navController.navigate("driver_lobby") {
                        popUpTo("role_selection") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    iconBackground: Color,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) BluePrimary else GrayBorder
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) BlueLight.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.95f),
            contentColor = DarkText
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(
                        color = iconBackground,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    color = DarkText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = GrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = if (selected) BluePrimary else Color(0xFFB0BAD0),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}