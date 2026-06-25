package com.example.peruchocourierapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val PNegro = Color(0xFF1A1A1A)
private val PRojo = Color(0xFFE02020)
private val PAzul = Color(0xFF1A4FBF)
private val PTextoSub = Color(0xFF888888)

@Composable
fun VerifySmsScreen(
    navController: NavController,
    phoneParam: String,
    nameParam: String,
    emailParam: String,
    dniParam: String
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var initialCodeSent by remember { mutableStateOf(false) }

    val cleanPhone = remember(phoneParam) {
        phoneParam.replace(" ", "").trim()
    }

    val phoneForTwilio = remember(cleanPhone) {
        if (cleanPhone.startsWith("+51")) cleanPhone else "+51$cleanPhone"
    }

    val isSuccess = message.contains("correctamente", ignoreCase = true)

    fun enviarCodigoSms(esReenvio: Boolean = false) {
        if (phoneForTwilio.isBlank()) {
            message = "Número de teléfono inválido"
            return
        }

        if (esReenvio) {
            isResending = true
        }

        message = if (esReenvio) "" else "Enviando código SMS..."

        RetrofitClient.instance.sendSmsCode(phoneForTwilio)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (esReenvio) {
                        isResending = false
                    }

                    val result = response.body()

                    message = if (response.isSuccessful && result?.success == true) {
                        if (esReenvio) {
                            "Código reenviado correctamente"
                        } else {
                            "Código enviado correctamente"
                        }
                    } else {
                        result?.message ?: "No se pudo enviar el código"
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    if (esReenvio) {
                        isResending = false
                    }

                    message = "Error: ${t.message}"
                }
            })
    }

    LaunchedEffect(phoneForTwilio) {
        if (!initialCodeSent) {
            initialCodeSent = true
            enviarCodigoSms(esReenvio = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Outlined.Sms,
            contentDescription = null,
            tint = PAzul,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Verifica tu número",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = PNegro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa el código SMS enviado a:\n$phoneForTwilio",
            fontSize = 14.sp,
            color = PTextoSub,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    code = it
                }
            },
            placeholder = { Text("Código de 6 dígitos") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFFF0F0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    color = if (isSuccess) Color(0xFF16A34A) else PRojo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        Button(
            onClick = {
                if (code.length != 6) {
                    message = "Ingresa un código válido de 6 dígitos"
                    return@Button
                }

                isLoading = true
                message = ""

                RetrofitClient.instance.verifySmsCode(phoneForTwilio, code)
                    .enqueue(object : Callback<BasicResponse> {
                        override fun onResponse(
                            call: Call<BasicResponse>,
                            response: Response<BasicResponse>
                        ) {
                            isLoading = false
                            val result = response.body()

                            if (response.isSuccessful && result?.success == true) {
                                sessionManager.saveUserSession(
                                    name = nameParam,
                                    email = emailParam,
                                    phone = phoneForTwilio,
                                    role = "cliente",
                                    dni = dniParam
                                )

                                navController.navigate("client_lobby") {
                                    popUpTo("verify_sms/{phone}/{name}/{email}/{dni}") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                message = result?.message ?: "Código incorrecto"
                            }
                        }

                        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                            isLoading = false
                            message = "Error: ${t.message}"
                        }
                    })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PNegro,
                contentColor = Color.White
            ),
            enabled = !isLoading && !isResending
        ) {
            Text(
                text = if (isLoading) "Verificando..." else "Verificar código",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            enabled = !isResending && !isLoading,
            onClick = {
                enviarCodigoSms(esReenvio = true)
            }
        ) {
            Text(
                text = if (isResending) "Reenviando..." else "Reenviar código",
                color = PAzul,
                fontWeight = FontWeight.Bold
            )
        }
    }
}