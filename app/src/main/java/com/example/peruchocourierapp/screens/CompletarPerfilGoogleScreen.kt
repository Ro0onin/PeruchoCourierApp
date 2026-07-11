package com.example.peruchocourierapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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

private data class GoogleProfileColors(
    val screenBg: Color,
    val cardBg: Color,
    val fieldBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val placeholder: Color,
    val disabledText: Color,
    val uploadSelectedBg: Color,
    val uploadDefaultBg: Color,
    val errorBg: Color,
    val errorText: Color,
    val shadowColor: Color
)

@Composable
private fun googleProfileColors(): GoogleProfileColors {
    val dark = isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.2f

    return if (dark) {
        GoogleProfileColors(
            screenBg = Color(0xFF0F172A),
            cardBg = Color(0xFF111827),
            fieldBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            placeholder = Color(0xFF94A3B8),
            disabledText = Color(0xFF94A3B8),
            uploadSelectedBg = Color(0xFF172554),
            uploadDefaultBg = Color(0xFF111827),
            errorBg = Color(0xFF3F1717),
            errorText = Color(0xFFFFB4B4),
            shadowColor = Color.Black.copy(alpha = 0.35f)
        )
    } else {
        GoogleProfileColors(
            screenBg = Color.White,
            cardBg = Color.White,
            fieldBg = Color(0xFFF4F6FB),
            border = Color(0xFFE8ECF4),
            text = Color(0xFF1A2340),
            muted = Color(0xFF6B7A99),
            placeholder = Color(0xFFB0BAD0),
            disabledText = Color(0xFF6B7A99),
            uploadSelectedBg = Color(0xFFE8EFFE),
            uploadDefaultBg = Color.White,
            errorBg = Color(0xFFFFEEEE),
            errorText = RedPrimary,
            shadowColor = Color.Black.copy(alpha = 0.12f)
        )
    }
}

@Composable
fun CompletarPerfilGoogleScreen(
    navController: NavController,
    emailParam: String
) {
    val context = LocalContext.current
    val colors = googleProfileColors()

    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    var dniFrontUri by remember { mutableStateOf<Uri?>(null) }
    var dniBackUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val nombreGoogle = remember(emailParam) {
        emailParam.substringBefore("@").replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

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
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
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
                .background(colors.screenBg)
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Completa tu perfil",
                color = colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Necesitamos estos datos para activar tu cuenta",
                color = colors.muted,
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
                colors = colors,
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
                colors = colors,
                leadingIcon = {
                    Icon(Icons.Outlined.Badge, null, tint = colors.placeholder)
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
                colors = colors,
                leadingIcon = {
                    Icon(Icons.Outlined.Phone, null, tint = colors.placeholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "Dirección DNI",
                value = direccion,
                onValueChange = { direccion = it },
                placeholder = "Dirección según DNI",
                keyboardType = KeyboardType.Text,
                colors = colors,
                leadingIcon = {
                    Icon(Icons.Outlined.Home, null, tint = colors.placeholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleProfileTextField(
                label = "Provincia",
                value = provincia,
                onValueChange = { provincia = it },
                placeholder = "Provincia según DNI",
                keyboardType = KeyboardType.Text,
                colors = colors,
                leadingIcon = {
                    Icon(Icons.Outlined.Home, null, tint = colors.placeholder)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            UploadButton(
                text = if (dniFrontUri == null) "Subir DNI frontal" else "DNI frontal cargado",
                selected = dniFrontUri != null,
                colors = colors,
                onClick = { frontLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            UploadButton(
                text = if (dniBackUri == null) "Subir DNI reverso" else "DNI reverso cargado",
                selected = dniBackUri != null,
                colors = colors,
                onClick = { backLauncher.launch("image/*") }
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.errorBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = colors.errorText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
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

                        !telefono.startsWith("9") -> {
                            errorMessage = "El celular debe iniciar con 9"
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
                                        navController.navigate(
                                            "verify_sms/${Uri.encode(telefono)}/${Uri.encode(nombreGoogle)}/${Uri.encode(emailParam)}/${Uri.encode(dni)}"
                                        ) {
                                            popUpTo("completar_perfil_google/${Uri.encode(emailParam)}") {
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
                    contentColor = Color.White,
                    disabledContainerColor = RedPrimary.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

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
    colors: GoogleProfileColors,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = colors.muted,
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
                    color = colors.placeholder,
                    fontSize = 14.sp
                )
            },
            textStyle = TextStyle(
                color = colors.text,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.fieldBg,
                unfocusedContainerColor = colors.fieldBg,
                disabledContainerColor = colors.fieldBg,
                focusedIndicatorColor = colors.border,
                unfocusedIndicatorColor = colors.border,
                disabledIndicatorColor = colors.border,
                cursorColor = BluePrimary,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                disabledTextColor = colors.disabledText,
                focusedPlaceholderColor = colors.placeholder,
                unfocusedPlaceholderColor = colors.placeholder,
                disabledPlaceholderColor = colors.placeholder
            )
        )
    }
}

@Composable
private fun UploadButton(
    text: String,
    selected: Boolean,
    colors: GoogleProfileColors,
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
            if (selected) BluePrimary else colors.border
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) colors.uploadSelectedBg else colors.uploadDefaultBg,
            contentColor = if (selected) BluePrimary else colors.text
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
