package com.example.peruchocourierapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val BlueDark = Color(0xFF0D3280)
private val BluePrimary = Color(0xFF1A4FBF)
private val BlueMid = Color(0xFF2D6BE4)
private val RedPrimary = Color(0xFFE02020)
private val GrayBg = Color(0xFFF4F6FB)
private val GrayBorder = Color(0xFFE8ECF4)
private val GrayText = Color(0xFF6B7A99)
private val GrayPlaceholder = Color(0xFFB0BAD0)
private val DarkText = Color(0xFF1A2340)

@Composable
fun CompletarPerfilGoogleScreen(
    navController: NavController,
    emailParam: String
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    var dniFrontUri by remember { mutableStateOf<Uri?>(null) }
    var dniBackUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val frontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        dniFrontUri = uri
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        dniBackUri = uri
    }

    fun uriToPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(
                partName,
                "$partName.jpg",
                requestBody
            )
        } catch (e: Exception) {
            null
        }
    }

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
                .height(220.dp)
                .background(
                    Brush.linearGradient(
                        listOf(BlueDark, BluePrimary, BlueMid)
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_perucho2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Completa tu perfil",
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Necesitamos estos datos para activar tu cuenta",
                color = GrayText,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            GoogleProfileTextField(
                label = "Correo Google",
                value = emailParam,
                onValueChange = {},
                placeholder = "",
                keyboardType = KeyboardType.Email,
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Outlined.CheckCircle, null, tint = BluePrimary)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "DNI",
                value = dni,
                onValueChange = {
                    if (it.length <= 8 && it.all { c -> c.isDigit() }) {
                        dni = it
                    }
                },
                placeholder = "Ingresa tu DNI",
                keyboardType = KeyboardType.Number,
                leadingIcon = {
                    Icon(Icons.Outlined.Badge, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "Celular",
                value = telefono,
                onValueChange = {
                    if (it.length <= 9 && it.all { c -> c.isDigit() }) {
                        telefono = it
                    }
                },
                placeholder = "Ingresa tu celular",
                keyboardType = KeyboardType.Phone,
                leadingIcon = {
                    Icon(Icons.Outlined.Phone, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "Dirección DNI",
                value = direccion,
                onValueChange = { direccion = it },
                placeholder = "Dirección según DNI",
                keyboardType = KeyboardType.Text,
                leadingIcon = {
                    Icon(Icons.Outlined.Home, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "Provincia",
                value = provincia,
                onValueChange = { provincia = it },
                placeholder = "Provincia según DNI",
                keyboardType = KeyboardType.Text,
                leadingIcon = {
                    Icon(Icons.Outlined.Home, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            UploadButton(
                text = if (dniFrontUri == null) "Subir DNI frontal" else "DNI frontal cargado",
                selected = dniFrontUri != null,
                onClick = { frontLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            UploadButton(
                text = if (dniBackUri == null) "Subir DNI reverso" else "DNI reverso cargado",
                selected = dniBackUri != null,
                onClick = { backLauncher.launch("image/*") }
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    color = RedPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    when {
                        dni.length != 8 -> {
                            errorMessage = "El DNI debe tener exactamente 8 dígitos"
                        }

                        telefono.length != 9 -> {
                            errorMessage = "El celular debe tener exactamente 9 dígitos"
                        }

                        direccion.trim().isEmpty() -> {
                            errorMessage = "Ingresa tu dirección"
                        }

                        provincia.trim().isEmpty() -> {
                            errorMessage = "Ingresa tu provincia"
                        }

                        dniFrontUri == null || dniBackUri == null -> {
                            errorMessage = "Debes subir foto frontal y reverso del DNI"
                        }

                        else -> {
                            errorMessage = ""
                            isLoading = true

                            val dniFront = uriToPart(dniFrontUri!!, "dni_front")
                            val dniBack = uriToPart(dniBackUri!!, "dni_back")

                            if (dniFront == null || dniBack == null) {
                                isLoading = false
                                errorMessage = "No se pudo leer la imagen del DNI"
                                return@Button
                            }

                            RetrofitClient.instance.completeGoogleProfile(
                                email = emailParam.toRequestBody("text/plain".toMediaTypeOrNull()),
                                dni = dni.toRequestBody("text/plain".toMediaTypeOrNull()),
                                phone = telefono.toRequestBody("text/plain".toMediaTypeOrNull()),
                                dniDireccion = direccion.toRequestBody("text/plain".toMediaTypeOrNull()),
                                dniProvincia = provincia.toRequestBody("text/plain".toMediaTypeOrNull()),
                                dniFront = dniFront,
                                dniBack = dniBack
                            ).enqueue(object : Callback<BasicResponse> {
                                override fun onResponse(
                                    call: Call<BasicResponse>,
                                    response: Response<BasicResponse>
                                ) {
                                    isLoading = false

                                    val result = response.body()

                                    if (response.isSuccessful && result?.success == true) {
                                        sessionManager.saveUserSession(
                                            name = sessionManager.getUserName() ?: "",
                                            email = emailParam,
                                            phone = telefono,
                                            role = "cliente",
                                            dni = dni
                                        )

                                        navController.navigate(
                                            "verify_sms/$telefono/${sessionManager.getUserName() ?: ""}/$emailParam/$dni"
                                        ) {
                                            popUpTo("completar_perfil_google/$emailParam") {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = result?.message ?: "No se pudo completar el perfil"
                                    }
                                }

                                override fun onFailure(
                                    call: Call<BasicResponse>,
                                    t: Throwable
                                ) {
                                    isLoading = false
                                    errorMessage = "Error: ${t.message}"
                                }
                            })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedPrimary,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Guardando..." else "Completar perfil",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun GoogleProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = GrayText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = leadingIcon,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GrayPlaceholder,
                    fontSize = 14.sp
                )
            },
            textStyle = TextStyle(
                color = DarkText,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrayBg,
                unfocusedContainerColor = GrayBg,
                disabledContainerColor = GrayBg,
                focusedIndicatorColor = GrayBorder,
                unfocusedIndicatorColor = GrayBorder,
                disabledIndicatorColor = GrayBorder,
                cursorColor = BluePrimary,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                disabledTextColor = GrayText
            )
        )
    }
}

@Composable
private fun UploadButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            2.dp,
            if (selected) BluePrimary else GrayBorder
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Color(0xFFE8EFFE) else Color.White,
            contentColor = if (selected) BluePrimary else DarkText
        )
    ) {
        Icon(Icons.Outlined.UploadFile, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}