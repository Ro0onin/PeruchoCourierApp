package com.example.peruchocourierapp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.ProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.peruchocourierapp.theme.ThemeManager
import androidx.compose.material.icons.outlined.DarkMode
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
private val Green = Color(0xFF10B981)

private data class PerfilClienteColors(
    val background: Color,
    val card: Color,
    val field: Color,
    val fieldEditable: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val lightText: Color,
    val iconBg: Color,
    val tagLockedBg: Color,
    val tagEditBg: Color,
    val warningBg: Color,
    val warningText: Color,
    val success: Color,
    val supportCard: Color
)

@Composable
private fun perfilClienteColors(): PerfilClienteColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        PerfilClienteColors(
            background = Color(0xFF0F172A),
            card = Color(0xFF111827),
            field = Color(0xFF1F2937),
            fieldEditable = Color(0xFF111827),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            lightText = Color(0xFF94A3B8),
            iconBg = Color(0xFF172554),
            tagLockedBg = Color(0xFF1F2937),
            tagEditBg = Color(0xFF172554),
            warningBg = Color(0xFF422006),
            warningText = Color(0xFFFDE68A),
            success = Color(0xFF34D399),
            supportCard = Color(0xFF111827)
        )
    } else {
        PerfilClienteColors(
            background = GrayBg,
            card = Color.White,
            field = GrayBg,
            fieldEditable = Color.White,
            border = GrayBorder,
            text = Dark,
            muted = GrayText,
            lightText = GrayLight,
            iconBg = BlueLight,
            tagLockedBg = GrayBorder,
            tagEditBg = BlueLight,
            warningBg = YellowBg,
            warningText = YellowText,
            success = Green,
            supportCard = Color.White
        )
    }
}

@Composable
fun PerfilClienteScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = perfilClienteColors()

    var name by remember { mutableStateOf(sessionManager.getUserName() ?: "Cliente") }
    val email = sessionManager.getUserEmail() ?: ""
    val phone = sessionManager.getUserPhone() ?: "-"
    val dni = sessionManager.getUserDni() ?: "-"

    var pais by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf("") }
    var isSavingAddress by remember { mutableStateOf(false) }

    val initials = name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "C" }

    LaunchedEffect(Unit) {
        if (email.isNotBlank()) {
            RetrofitClient.instance.getProfile(email)
                .enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        val user = response.body()?.user

                        if (response.isSuccessful && response.body()?.success == true && user != null) {
                            name = user.name ?: name
                            pais = user.pais.orEmpty()
                            ciudad = user.ciudad.orEmpty()
                            direccion = user.direccion.orEmpty()
                            apartamento = user.apartamento.orEmpty()
                            provincia = user.provincia.orEmpty()
                            codigoPostal = user.codigo_postal.orEmpty()
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        successMessage = "Error cargando perfil"
                    }
                })
        }
    }

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
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                Icon(Icons.Outlined.ShoppingBag, null, tint = Blue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Cliente", color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProfileSection(
                title = "Información personal",
                icon = Icons.Outlined.Person,
                colors = colors
            ) {
                LockedProfileField("Nombre completo", name, Icons.Outlined.Person, colors)
                LockedProfileField("DNI / Documento", dni, Icons.Outlined.Badge, colors)
                LockedProfileField("Correo electrónico", email.ifBlank { "-" }, Icons.Outlined.Email, colors)
                LockedProfileField("Teléfono", phone, Icons.Outlined.Phone, colors)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSection(
                title = "Dirección de entrega",
                icon = Icons.Outlined.LocationOn,
                colors = colors
            ) {
                EditableAddressField("País", pais, Icons.Outlined.Public, colors) {
                    pais = it
                    successMessage = ""
                }

                EditableAddressField("Ciudad", ciudad, Icons.Outlined.LocationCity, colors) {
                    ciudad = it
                    successMessage = ""
                }

                EditableAddressField("Dirección", direccion, Icons.Outlined.Home, colors) {
                    direccion = it
                    successMessage = ""
                }

                EditableAddressField("Apartamento / Referencia", apartamento, Icons.Outlined.Apartment, colors) {
                    apartamento = it
                    successMessage = ""
                }

                EditableAddressField("Provincia", provincia, Icons.Outlined.Map, colors) {
                    provincia = it
                    successMessage = ""
                }

                EditableAddressField("Código postal", codigoPostal, Icons.Outlined.MarkunreadMailbox, colors) {
                    codigoPostal = it
                    successMessage = ""
                }

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            successMessage = "No se encontró el email del usuario"
                            return@Button
                        }

                        isSavingAddress = true

                        RetrofitClient.instance.updateProfileAddress(
                            email = email,
                            pais = pais,
                            direccion = direccion,
                            apartamento = apartamento,
                            ciudad = ciudad,
                            provincia = provincia,
                            codigoPostal = codigoPostal
                        ).enqueue(object : Callback<BasicResponse> {
                            override fun onResponse(
                                call: Call<BasicResponse>,
                                response: Response<BasicResponse>
                            ) {
                                isSavingAddress = false
                                successMessage = response.body()?.message ?: "Dirección actualizada"
                            }

                            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                                isSavingAddress = false
                                successMessage = "Error al guardar dirección"
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text(
                        text = if (isSavingAddress) "Guardando..." else "Guardar dirección",
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            WarningBox(colors)

            Spacer(modifier = Modifier.height(12.dp))

            SupportAction(colors) {
                abrirWhatsApp(
                    context = context,
                    numero = "51967929967",
                    mensaje = "Hola, necesito soporte con mi perfil de Perucho Courier."
                )
            }


            if (successMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = successMessage,
                    color = colors.success,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PERFIL DE USUARIO",
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
private fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: PerfilClienteColors,
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
                        .background(colors.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp))
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
private fun EditableAddressField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: PerfilClienteColors,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label.uppercase(),
        color = colors.muted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = {
            Icon(icon, null, tint = Blue)
        },
        trailingIcon = {
            ProfileTag("Editar", false, colors)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = Blue,
            focusedContainerColor = colors.fieldEditable,
            unfocusedContainerColor = colors.fieldEditable,
            disabledContainerColor = colors.fieldEditable,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            disabledTextColor = colors.text,
            cursorColor = Blue,
            focusedLabelColor = colors.muted,
            unfocusedLabelColor = colors.muted
        ),
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun LockedProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: PerfilClienteColors
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
            .background(colors.field)
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.lightText, modifier = Modifier.size(21.dp))

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

        ProfileTag("Fijo", true, colors)
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ProfileTag(
    text: String,
    locked: Boolean,
    colors: PerfilClienteColors
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (locked) colors.tagLockedBg else colors.tagEditBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                Icons.Outlined.Lock,
                null,
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
private fun WarningBox(colors: PerfilClienteColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colors.warningBg)
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = "Solo puedes editar tu dirección de entrega. Para cambiar DNI, teléfono o correo, solicítalo por WhatsApp.",
            color = colors.warningText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun SupportAction(
    colors: PerfilClienteColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(colors.supportCard)
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.SupportAgent,
            null,
            tint = Green,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Contactar soporte",
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Ayuda con tus datos o pedidos",
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Icon(Icons.Outlined.ChevronRight, null, tint = colors.muted)
    }
}


private fun abrirWhatsApp(
    context: android.content.Context,
    numero: String,
    mensaje: String
) {
    val url = "https://wa.me/$numero?text=${Uri.encode(mensaje)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
