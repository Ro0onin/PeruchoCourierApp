package com.example.peruchocourierapp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@Composable
fun PerfilClienteScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

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
                Icon(Icons.Outlined.ShoppingBag, null, tint = Blue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Cliente", color = GrayText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProfileSection("Información personal", Icons.Outlined.Person) {
                LockedProfileField("Nombre completo", name, Icons.Outlined.Person)
                LockedProfileField("DNI / Documento", dni, Icons.Outlined.Badge)
                LockedProfileField("Correo electrónico", email.ifBlank { "-" }, Icons.Outlined.Email)
                LockedProfileField("Teléfono", phone, Icons.Outlined.Phone)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSection("Dirección de entrega", Icons.Outlined.LocationOn) {
                EditableAddressField("País", pais, Icons.Outlined.Public) {
                    pais = it
                    successMessage = ""
                }

                EditableAddressField("Ciudad", ciudad, Icons.Outlined.LocationCity) {
                    ciudad = it
                    successMessage = ""
                }

                EditableAddressField("Dirección", direccion, Icons.Outlined.Home) {
                    direccion = it
                    successMessage = ""
                }

                EditableAddressField("Apartamento / Referencia", apartamento, Icons.Outlined.Apartment) {
                    apartamento = it
                    successMessage = ""
                }

                EditableAddressField("Provincia", provincia, Icons.Outlined.Map) {
                    provincia = it
                    successMessage = ""
                }

                EditableAddressField("Código postal", codigoPostal, Icons.Outlined.MarkunreadMailbox) {
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

            WarningBox()

            Spacer(modifier = Modifier.height(12.dp))

            SupportAction {
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
                    color = Green,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PERFIL DE USUARIO",
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
private fun ProfileSection(
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
                    Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp))
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
private fun EditableAddressField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label.uppercase(),
        color = GrayText,
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
            ProfileTag("Editar", false)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = Blue,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
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
        Icon(icon, null, tint = GrayLight, modifier = Modifier.size(21.dp))

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

        ProfileTag("Fijo", true)
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ProfileTag(text: String, locked: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (locked) GrayBorder else BlueLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                Icons.Outlined.Lock,
                null,
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
private fun WarningBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(YellowBg)
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
            color = YellowText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun SupportAction(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
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
                color = Dark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Ayuda con tus datos o pedidos",
                color = GrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Icon(Icons.Outlined.ChevronRight, null, tint = GrayText)
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