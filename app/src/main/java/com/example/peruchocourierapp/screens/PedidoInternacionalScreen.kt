package com.example.peruchocourierapp.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val IntlBlue = Color(0xFF1A4FBF)
private val IntlBlueDark = Color(0xFF0D3280)
private val IntlBlueLight = Color(0xFFE8EFFE)
private val IntlRed = Color(0xFFE02020)
private val IntlRedLight = Color(0xFFFFF0F0)
private val IntlGrayBg = Color(0xFFF4F6FB)
private val IntlGrayBorder = Color(0xFFE8ECF4)
private val IntlGrayText = Color(0xFF6B7A99)
private val IntlGrayLight = Color(0xFFB0BAD0)
private val IntlDark = Color(0xFF1A2340)

fun String.toPlainRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoInternacionalScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val precioPorKg = 8.5

    var webCompra by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf("") }
    var precioCompra by remember { mutableStateOf("") }
    var tracking by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var pesoEstimado by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var expandedPago by remember { mutableStateOf(false) }
    val metodosPago = listOf("Yape", "Plin", "BCP", "Interbank", "Efectivo")

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfNombre by remember { mutableStateOf("Ningún archivo seleccionado") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showAduanaDialog by remember { mutableStateOf(false) }

    val totalEstimado = (pesoEstimado.toDoubleOrNull() ?: 0.0) * precioPorKg

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri

            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex >= 0) {
                    pdfNombre = it.getString(nameIndex)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IntlGrayBg)
            .navigationBarsPadding()
    ) {
        IntlTopBar(navController)

        InfoBanner()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IntlSectionCard(
                title = "Tienda / Producto",
                icon = Icons.Outlined.Storefront,
                iconBg = IntlBlueLight,
                iconTint = IntlBlue
            ) {
                IntlInput(
                    label = "WEB DE COMPRA",
                    value = webCompra,
                    placeholder = "ej: amazon.com",
                    icon = Icons.Outlined.Language,
                    keyboardType = KeyboardType.Uri,
                    onValueChange = {
                        webCompra = it
                            .lowercase()
                            .replace(" ", "")
                    }
                )

                IntlInput(
                    label = "PRODUCTO",
                    value = productos,
                    placeholder = "Nombre del producto",
                    icon = Icons.Outlined.Inventory2,
                    onValueChange = { productos = it }
                )

                IntlInput(
                    label = "PRECIO DE COMPRA",
                    value = precioCompra,
                    placeholder = "0.00",
                    icon = Icons.Outlined.AttachMoney,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() || c == '.' }) precioCompra = it
                    }
                )
            }

            IntlSectionCard(
                title = "Seguimiento",
                icon = Icons.Outlined.LocalShipping,
                iconBg = IntlRedLight,
                iconTint = IntlRed
            ) {
                IntlInput(
                    label = "NÚMERO DE TRACKING",
                    value = tracking,
                    placeholder = "Ej: 1Z999AA10123456784",
                    icon = Icons.Outlined.QrCode2,
                    onValueChange = {
                        tracking = it
                            .uppercase()
                            .replace(" ", "")
                    }
                )

                IntlDateInput(
                    label = "FECHA ESTIMADA DE LLEGADA",
                    value = fechaSeleccionada,
                    onClick = { showDatePicker = true }
                )

                IntlInput(
                    label = "PESO ESTIMADO (KG)",
                    value = pesoEstimado,
                    placeholder = "0.00 kg",
                    icon = Icons.Outlined.Scale,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() || c == '.' }) pesoEstimado = it
                    }
                )
            }

            IntlTotalCard(
                total = totalEstimado,
                onInfoClick = { showAduanaDialog = true }
            )

            IntlSectionCard(
                title = "Pago",
                icon = Icons.Outlined.CreditCard,
                iconBg = IntlBlueLight,
                iconTint = IntlBlue
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IntlDropdownInput(
                        label = "MÉTODO DE PAGO",
                        value = metodoPago,
                        placeholder = "Seleccionar método",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        onClick = { expandedPago = true }
                    )

                    DropdownMenu(
                        expanded = expandedPago,
                        onDismissRequest = { expandedPago = false }
                    ) {
                        metodosPago.forEach { metodo ->
                            DropdownMenuItem(
                                text = { Text(metodo) },
                                onClick = {
                                    metodoPago = metodo
                                    expandedPago = false
                                }
                            )
                        }
                    }
                }
            }

            PdfSelectorRow(
                pdfNombre = pdfNombre,
                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
            )

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = if (errorMessage.contains("✅")) Color(0xFF2E7D32) else IntlRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        webCompra.isBlank() ||
                                productos.isBlank() ||
                                precioCompra.isBlank() ||
                                tracking.isBlank() ||
                                fechaSeleccionada.isBlank() ||
                                pesoEstimado.isBlank() ||
                                metodoPago.isBlank() -> {
                            errorMessage = "Completa todos los campos"
                        }

                        !esWebCompraValida(webCompra) -> {
                            errorMessage = "La web de compra debe terminar en .com. Ejemplo: amazon.com"
                        }

                        productos.trim().length < 3 -> {
                            errorMessage = "Ingresa un nombre de producto válido"
                        }

                        !esDecimalValido(precioCompra) ||
                                precioCompra.toDoubleOrNull() == null ||
                                precioCompra.toDouble() <= 0.0 -> {
                            errorMessage = "Ingresa un precio de compra válido mayor a 0. Ejemplo: 25.99"
                        }

                        tracking.length < 6 -> {
                            errorMessage = "Ingresa un número de tracking válido"
                        }

                        !esDecimalValido(pesoEstimado) ||
                                pesoEstimado.toDoubleOrNull() == null ||
                                pesoEstimado.toDouble() <= 0.0 -> {
                            errorMessage = "Ingresa un peso estimado válido mayor a 0. Ejemplo: 1.50"
                        }

                        pdfUri == null -> {
                            errorMessage = "Adjunta la factura PDF"
                        }

                        else -> {
                            val email = sessionManager.getUserEmail()

                            if (email.isNullOrEmpty()) {
                                errorMessage = "Sesión inválida"
                                return@Button
                            }

                            isSubmitting = true
                            errorMessage = ""

                            try {
                                val input = context.contentResolver.openInputStream(pdfUri!!)
                                val file = File.createTempFile("factura_", ".pdf", context.cacheDir)
                                val output = FileOutputStream(file)

                                input?.copyTo(output)
                                input?.close()
                                output.close()

                                val requestFile =
                                    file.asRequestBody("application/pdf".toMediaTypeOrNull())

                                val pdfPart = MultipartBody.Part.createFormData(
                                    "factura_pdf",
                                    file.name,
                                    requestFile
                                )

                                RetrofitClient.instance.createInternationalOrder(
                                    email.toPlainRequestBody(),
                                    "internacional".toPlainRequestBody(),
                                    normalizarWebCompra(webCompra).toPlainRequestBody(),
                                    productos.trim().toPlainRequestBody(),
                                    precioCompra.trim().toPlainRequestBody(),
                                    tracking.trim().toPlainRequestBody(),
                                    fechaSeleccionada.toPlainRequestBody(),
                                    pesoEstimado.trim().toPlainRequestBody(),
                                    metodoPago.toPlainRequestBody(),
                                    "%.2f".format(totalEstimado).toPlainRequestBody(),
                                    pdfPart
                                ).enqueue(object : Callback<BasicResponse> {
                                    override fun onResponse(
                                        call: Call<BasicResponse>,
                                        response: Response<BasicResponse>
                                    ) {
                                        isSubmitting = false

                                        if (response.isSuccessful) {
                                            val result = response.body()

                                            if (result?.success == true) {
                                                navController.navigate("mis_pedidos") {
                                                    popUpTo("client_lobby") {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                errorMessage =
                                                    result?.message ?: "Respuesta vacía del servidor"
                                            }
                                        } else {
                                            errorMessage =
                                                "HTTP ${response.code()}: ${response.errorBody()?.string()}"
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<BasicResponse>,
                                        t: Throwable
                                    ) {
                                        isSubmitting = false
                                        errorMessage = "Error: ${t.message}"
                                    }
                                })
                            } catch (e: Exception) {
                                isSubmitting = false
                                errorMessage = "Error al procesar PDF"
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntlRed,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitting) "Enviando..." else "Registrar pedido",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "PEDIDO INTERNACIONAL",
                color = IntlGrayLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.7.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            fechaSeleccionada = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAduanaDialog) {
        AlertDialog(
            onDismissRequest = { showAduanaDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(IntlRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PriorityHigh,
                        contentDescription = null,
                        tint = IntlRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Aviso de Aduana",
                    color = IntlDark,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Team Perucho Courier te informa: si tu compra supera los $200 dólares, la aduana puede aplicar un impuesto aproximado del 25% sobre el valor declarado. Te recomendamos revisar el monto de tu compra antes de registrar tu pedido.",
                    color = IntlGrayText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAduanaDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IntlBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun IntlTopBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(IntlBlueDark, IntlBlue)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 24.dp, y = (-18).dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.07f))
        )

        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pedido Internacional",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "USA → Perú",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(IntlBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = IntlBlue,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Tarifa: $8.5 por kilo. Recibirás la dirección de nuestro almacén y asesoramiento personalizado.",
            color = IntlGrayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun IntlSectionCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = title.uppercase(),
                color = IntlDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        content()
    }
}

@Composable
private fun IntlInput(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        color = IntlGrayText,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IntlGrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = IntlGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = intlFieldColors()
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun IntlDateInput(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = IntlGrayText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        enabled = false,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = IntlGrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = IntlGrayLight
            )
        },
        placeholder = {
            Text(
                text = "Seleccionar fecha",
                color = IntlGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = intlFieldColors()
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun IntlDropdownInput(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = IntlGrayText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IntlGrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = IntlGrayLight
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = IntlGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = intlFieldColors()
    )
}

@Composable
private fun IntlTotalCard(
    total: Double,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IntlBlue)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TOTAL ESTIMADO",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "$${"%.2f".format(total)}",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Peso × $8.5/kg\nSe actualiza al ingresar peso",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onInfoClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PriorityHigh,
                    contentDescription = "Información aduana",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun PdfSelectorRow(
    pdfNombre: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.PictureAsPdf,
            contentDescription = null,
            tint = IntlRed,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Comprobante de compra",
                color = IntlDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = if (pdfNombre == "Ningún archivo seleccionado") {
                    "Obligatorio · Adjuntar comprobante"
                } else {
                    pdfNombre
                },
                color = IntlGrayText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(9.dp),
            border = BorderStroke(1.5.dp, IntlBlue),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Seleccionar",
                color = IntlBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun intlFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = IntlGrayBg,
    unfocusedContainerColor = IntlGrayBg,
    disabledContainerColor = IntlGrayBg,
    focusedBorderColor = IntlGrayBorder,
    unfocusedBorderColor = IntlGrayBorder,
    disabledBorderColor = IntlGrayBorder,
    cursorColor = IntlBlue,
    focusedTextColor = IntlDark,
    unfocusedTextColor = IntlDark,
    disabledTextColor = IntlDark,
    focusedPlaceholderColor = IntlGrayLight,
    unfocusedPlaceholderColor = IntlGrayLight,
    disabledPlaceholderColor = IntlGrayLight
)

private fun normalizarWebCompra(input: String): String {
    return input
        .trim()
        .lowercase()
        .replace("https://", "")
        .replace("http://", "")
        .removePrefix("www.")
}

private fun esWebCompraValida(input: String): Boolean {
    val web = normalizarWebCompra(input)

    return web.matches(
        Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.com$")
    )
}

private fun esDecimalValido(input: String): Boolean {
    return input.matches(Regex("^\\d+(\\.\\d{1,2})?$"))
}