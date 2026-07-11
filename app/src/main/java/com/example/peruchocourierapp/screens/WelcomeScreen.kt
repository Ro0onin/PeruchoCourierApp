package com.example.peruchocourierapp.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(navController: NavController) {

    val pages = listOf(
        WelcomePage(
            title = "PERUCHO.COURIER",
            description = "Envíos rápidos y seguros a todo el Perú,\ncon seguimiento en tiempo real.",
            imageRes = R.drawable.logo_perucho2
        ),
        WelcomePage(
            title = "Sigue tu pedido en vivo",
            description = "Mira en el mapa exactamente dónde está tu paquete, desde el recojo hasta la entrega.",
            imageRes = R.drawable.seguimiento_vivo
        ),
        WelcomePage(
            title = "Tarifas claras, sin sorpresas",
            description = "Conoce el precio antes de confirmar tu envío. Rápido, seguro y transparente.",
            imageRes = R.drawable.perucho_motorizado
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            val nextPage = (pagerState.currentPage + 1) % pages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1),
                        Color(0xFF1E4FD8),
                        Color(0xFF3B82F6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(70.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                WelcomeSlide(page = pages[page])
            }

            DotsIndicator(
                totalDots = pages.size,
                selectedIndex = pagerState.currentPage
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = {
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE42328),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Iniciar sesión",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("register") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Crear cuenta nueva",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Al continuar aceptas los Términos y la Política de privacidad.",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WelcomeSlide(page: WelcomePage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when (page.title) {
                        "PERUCHO.COURIER" -> 165.dp
                        "Sigue tu pedido en vivo" -> 260.dp
                        "Tarifas claras, sin sorpresas" -> 210.dp
                        else -> 190.dp
                    }
                ),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (page.title != "PERUCHO.COURIER") {
            Text(
                text = page.title,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        Text(
            text = page.description,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->

            val color by animateColorAsState(
                targetValue = if (index == selectedIndex) {
                    Color(0xFFE42328)
                } else {
                    Color.White.copy(alpha = 0.45f)
                },
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(7.dp)
                    .width(if (index == selectedIndex) 22.dp else 7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

data class WelcomePage(
    val title: String,
    val description: String,
    val imageRes: Int
)