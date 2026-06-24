package com.example.peruchocourierapp.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val ForgotBlueDark = Color(0xFF0D3280)
private val ForgotBlue = Color(0xFF1A4FBF)
private val ForgotBlueMid = Color(0xFF2D6BE4)
private val ForgotRed = Color(0xFFE02020)
private val ForgotBg = Color(0xFFF4F6FB)
private val ForgotBorder = Color(0xFFE8ECF4)
private val ForgotText = Color(0xFF1A2340)
private val ForgotMuted = Color(0xFF6B7A99)
private val ForgotPlaceholder = Color(0xFFB0BAD0)
private val ForgotSuccessBg = Color(0xFFD1FAE5)
private val ForgotSuccessText = Color(0xFF067647)

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.linearGradient(
                        listOf(ForgotBlueDark, ForgotBlue, ForgotBlueMid)
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = 12.dp)
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-45).dp, y = 45.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Image(
                painter = painterResource(id = R.drawable.logo_perucho2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recuperar contraseña",
                color = ForgotText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                color = ForgotMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "CORREO ELECTRÓNICO",
                color = ForgotMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            TextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    message = ""
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = ForgotPlaceholder
                    )
                },
                placeholder = {
                    Text(
                        text = "correo@ejemplo.com",
                        color = ForgotPlaceholder,
                        fontSize = 14.sp
                    )
                },
                textStyle = TextStyle(
                    color = ForgotText,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ForgotBg,
                    unfocusedContainerColor = ForgotBg,
                    disabledContainerColor = ForgotBg,
                    focusedIndicatorColor = ForgotBorder,
                    unfocusedIndicatorColor = ForgotBorder,
                    disabledIndicatorColor = ForgotBorder,
                    cursorColor = ForgotBlue,
                    focusedTextColor = ForgotText,
                    unfocusedTextColor = ForgotText
                )
            )

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSuccess) ForgotSuccessBg else Color(0xFFFFE2E2))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Outlined.MarkEmailRead else Icons.Outlined.Email,
                        contentDescription = null,
                        tint = if (isSuccess) ForgotSuccessText else ForgotRed,
                        modifier = Modifier.size(21.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = message,
                        color = if (isSuccess) ForgotSuccessText else ForgotRed,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim()

                    when {
                        cleanEmail.isBlank() -> {
                            isSuccess = false
                            message = "Ingresa tu correo electrónico"
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> {
                            isSuccess = false
                            message = "Correo electrónico inválido"
                        }

                        else -> {
                            isLoading = true
                            message = ""

                            RetrofitClient.instance.forgotPassword(cleanEmail)
                                .enqueue(object : Callback<BasicResponse> {
                                    override fun onResponse(
                                        call: Call<BasicResponse>,
                                        response: Response<BasicResponse>
                                    ) {
                                        isLoading = false

                                        val result = response.body()

                                        if (response.isSuccessful && result?.success == true) {
                                            isSuccess = true
                                            message = result.message
                                                ?: "Te enviamos un enlace para restablecer tu contraseña."
                                        } else {
                                            isSuccess = false
                                            message = result?.message
                                                ?: "No se pudo enviar el correo de recuperación."
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<BasicResponse>,
                                        t: Throwable
                                    ) {
                                        isLoading = false
                                        isSuccess = false
                                        message = "Error de conexión: ${t.message}"
                                    }
                                })
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForgotRed,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isLoading) "Enviando..." else "Enviar enlace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = ForgotBlue
                )
            ) {
                Text(
                    text = "Volver al inicio de sesión",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PERUCHO COURIER",
                color = ForgotPlaceholder,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}