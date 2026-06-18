package com.example.peruchocourierapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage          // ✅ NUEVO
import com.example.peruchocourierapp.R

@Composable
fun SeleccionarTipoPedidoScreen(navController: NavController) {

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ CORREGIDO
        AsyncImage(
            model = R.drawable.fondo_tipo_pedido,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(315.dp))

            TipoPedidoButton(
                text = "Pedido Internacional",
                iconRes = R.drawable.ic_internacional,
                onClick = { navController.navigate("pedido_internacional") }
            )

            Spacer(modifier = Modifier.height(26.dp))

            TipoPedidoButton(
                text = "Pedido nacional",
                iconRes = R.drawable.ic_nacional,
                onClick = { navController.navigate("pedido_nacional") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1668AE)
                ),
                border = BorderStroke(width = 2.dp, color = Color(0xFF1668AE))
            ) {
                Text(text = "Volver", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TipoPedidoButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.70f)
            .height(56.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1668AE),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // ✅ Ícono pequeño — painterResource está bien
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}