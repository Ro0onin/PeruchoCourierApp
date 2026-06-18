/*package com.example.peruchocourierapp.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
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

private val Blue = Color(0xFF1A4FBF)
private val BlueDark = Color(0xFF0D3280)
private val BlueLight = Color(0xFFE8EFFE)
private val GrayBg = Color(0xFFF4F6FB)
private val GrayBorder = Color(0xFFE8ECF4)
private val GrayText = Color(0xFF6B7A99)
private val GrayLight = Color(0xFFB0BAD0)
private val Dark = Color(0xFF1A2340)
private val Red = Color(0xFFE02020)
private val RedLight = Color(0xFFFFF0F0)

private fun String.toCrearRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEnvioScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    var selectedType by rememberSaveable { mutableStateOf("internacional") }

    var descripcion by remember { mutableStateOf("") }
    var telefonoRemitente by remember { mutableStateOf("") }
    var telefonoDestinatario by remember { mutableStateOf("") }
    var tamanoPaquete by remember { mutableStateOf("") }
    var tipoVehiculo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var comentariosRepartidor by remember { mutableStateOf("") }
    var metodoPagoNacional by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val pickupAddress = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("pickup_address", "")
        ?.collectAsState()

    val pickupLat = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("pickup_lat", 0.0)
        ?.collectAsState()

    val pickupLng = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("pickup_lng", 0.0)
        ?.collectAsState()

    val dropoffAddress = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("dropoff_address", "")
        ?.collectAsState()

    val dropoffLat = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("dropoff_lat", 0.0)
        ?.collectAsState()

    val dropoffLng = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("dropoff_lng", 0.0)
        ?.collectAsState()

    val distanciaKm = if (
        (pickupLat?.value ?: 0.0) != 0.0 &&
        (pickupLng?.value ?: 0.0) != 0.0 &&
        (dropoffLat?.value ?: 0.0) != 0.0 &&
        (dropoffLng?.value ?: 0.0) != 0.0
    ) {
        calcularDistanciaCrearPedidoKm(
            pickupLat?.value ?: 0.0,
            pickupLng?.value ?: 0.0,
            dropoffLat?.value ?: 0.0,
            dropoffLng?.value ?: 0.0
        )
    } else {
        0.0
    }

    val totalNacional = calcularTarifaCrearPedido(
        distanciaKm = distanciaKm,
        tamano = tamanoPaquete,
        tipoVehiculo = tipoVehiculo
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(BlueDark, Blue)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Crear pedido",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeTab(
                    title = "Internacional",
                    icon = Icons.Outlined.Language,
                    selected = selectedType == "internacional",
                    modifier = Modifier.weight(1f)
                ) {
                    selectedType = "internacional"
                    errorMessage = ""
                }

                TypeTab(
                    title = "Nacional",
                    icon = Icons.Outlined.Map,
                    selected = selectedType == "nacional",
                    modifier = Modifier.weight(1f)
                ) {
                    selectedType = "nacional"
                    errorMessage = ""
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedType == "internacional") {
                PedidoInternacionalFormContent(navController)
            } else {
                FormCard(
                    title = "RECOJO Y ENTREGA",
                    icon = Icons.Outlined.LocationOn
                ) {
                    ModernInput(
                        label = "ORIGEN",
                        value = pickupAddress?.value ?: "",
                        placeholder = "Toca para seleccionar",
                        readOnly = true,
                        onClick = { navController.navigate("map_picker/pickup") },
                        onValueChange = {}
                    )

                    ModernInput(
                        label = "DESTINO",
                        value = dropoffAddress?.value ?: "",
                        placeholder = "Toca para seleccionar",
                        readOnly = true,
                        onClick = { navController.navigate("map_picker/dropoff") },
                        onValueChange = {}
                    )

                    ModernInput(
                        label = "DESCRIPCIÓN",
                        value = descripcion,
                        placeholder = "Descripción del paquete",
                        onValueChange = { descripcion = it }
                    )

                    ModernInput(
                        label = "TELÉFONO REMITENTE",
                        value = telefonoRemitente,
                        placeholder = "999999999",
                        keyboardType = KeyboardType.Phone,
                        onValueChange = { telefonoRemitente = it }
                    )

                    ModernInput(
                        label = "TELÉFONO DESTINATARIO",
                        value = telefonoDestinatario,
                        placeholder = "999999999",
                        keyboardType = KeyboardType.Phone,
                        onValueChange = { telefonoDestinatario = it }
                    )

                    ModernDropdown(
                        label = "TAMAÑO DEL PAQUETE",
                        value = tamanoPaquete,
                        placeholder = "Selecciona tamaño",
                        options = listOf("Pequeño", "Mediano", "Grande"),
                        onSelect = { tamanoPaquete = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ModernDropdown(
                        label = "TIPO DE VEHÍCULO",
                        value = tipoVehiculo,
                        placeholder = "Selecciona vehículo",
                        options = listOf(
                            "Motorizado",
                            "Auto / Sedan",
                            "Van / Minivan",
                            "Camión"
                        ),
                        onSelect = { tipoVehiculo = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ModernDropdown(
                        label = "CATEGORÍA",
                        value = categoria,
                        placeholder = "Selecciona categoría",
                        options = listOf("Alimentos", "Ropa", "Documentos", "Electrónicos", "Otros"),
                        onSelect = { categoria = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ModernDropdown(
                        label = "MÉTODO DE PAGO",
                        value = metodoPagoNacional,
                        placeholder = "Selecciona método de pago",
                        options = listOf("Yape", "Plin", "Efectivo", "BCP"),
                        onSelect = { metodoPagoNacional = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ModernInput(
                        label = "COMENTARIOS PARA EL REPARTIDOR",
                        value = comentariosRepartidor,
                        placeholder = "Ej: llamar al llegar, entregar en recepción...",
                        onValueChange = { comentariosRepartidor = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                PriceBox(
                    if (distanciaKm > 0.0) {
                        "S/ ${"%.2f".format(totalNacional)}"
                    } else {
                        "Según distancia"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        val userEmail = sessionManager.getUserEmail()

                        val pAddress = pickupAddress?.value.orEmpty()
                        val pLat = pickupLat?.value ?: 0.0
                        val pLng = pickupLng?.value ?: 0.0

                        val dAddress = dropoffAddress?.value.orEmpty()
                        val dLat = dropoffLat?.value ?: 0.0
                        val dLng = dropoffLng?.value ?: 0.0

                        when {
                            userEmail.isNullOrBlank() -> {
                                errorMessage = "Sesión inválida"
                            }

                            pAddress.isBlank() || pLat == 0.0 || pLng == 0.0 -> {
                                errorMessage = "Selecciona el origen en el mapa"
                            }

                            dAddress.isBlank() || dLat == 0.0 || dLng == 0.0 -> {
                                errorMessage = "Selecciona el destino en el mapa"
                            }

                            descripcion.isBlank() -> {
                                errorMessage = "Completa la descripción"
                            }

                            telefonoRemitente.isBlank() -> {
                                errorMessage = "Completa el teléfono del remitente"
                            }

                            telefonoDestinatario.isBlank() -> {
                                errorMessage = "Completa el teléfono del destinatario"
                            }

                            tamanoPaquete.isBlank() -> {
                                errorMessage = "Completa el tamaño del paquete"
                            }

                            tipoVehiculo.isBlank() -> {
                                errorMessage = "Selecciona el tipo de vehículo"
                            }

                            categoria.isBlank() -> {
                                errorMessage = "Selecciona una categoría"
                            }

                            metodoPagoNacional.isBlank() -> {
                                errorMessage = "Selecciona un método de pago"
                            }

                            else -> {
                                isLoading = true
                                errorMessage = ""

                                RetrofitClient.instance.createNationalOrder(
                                    userEmail = userEmail,
                                    tipoEnvio = "nacional",
                                    origen = pAddress,
                                    destino = dAddress,
                                    pickupAddress = pAddress,
                                    pickupLat = pLat.toString(),
                                    pickupLng = pLng.toString(),
                                    dropoffAddress = dAddress,
                                    dropoffLat = dLat.toString(),
                                    dropoffLng = dLng.toString(),
                                    telefonoRemitente = telefonoRemitente,
                                    telefonoDestinatario = telefonoDestinatario,
                                    descripcion = descripcion,
                                    tamanoPaquete = tamanoPaquete,
                                    tipoVehiculo = normalizarTipoVehiculo(tipoVehiculo),
                                    categoria = categoria,
                                    comentariosRepartidor = comentariosRepartidor,
                                    metodoPago = metodoPagoNacional,
                                    distanciaKm = "%.2f".format(distanciaKm),
                                    total = "%.2f".format(totalNacional)
                                ).enqueue(object : Callback<BasicResponse> {
                                    override fun onResponse(
                                        call: Call<BasicResponse>,
                                        response: Response<BasicResponse>
                                    ) {
                                        isLoading = false

                                        val result = response.body()

                                        if (response.isSuccessful && result?.success == true) {
                                            navController.navigate("mis_pedidos") {
                                                popUpTo("client_lobby") {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            errorMessage =
                                                result?.message ?: "No se pudo crear el pedido"
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<BasicResponse>,
                                        t: Throwable
                                    ) {
                                        isLoading = false
                                        errorMessage = "Error de conexión: ${t.message}"
                                    }
                                })
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Icon(Icons.Outlined.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Creando..." else "Confirmar pedido",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PedidoInternacionalFormContent(navController: NavController) {
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

    IntlInfoBanner()

    Spacer(modifier = Modifier.height(12.dp))

    IntlSectionCard(
        title = "Tienda / Producto",
        icon = Icons.Outlined.Storefront,
        iconBg = BlueLight,
        iconTint = Blue
    ) {
        IntlInput(
            label = "WEB DE COMPRA",
            value = webCompra,
            placeholder = "ej: amazon.com",
            icon = Icons.Outlined.Language,
            onValueChange = { webCompra = it }
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

    Spacer(modifier = Modifier.height(12.dp))

    IntlSectionCard(
        title = "Seguimiento",
        icon = Icons.Outlined.LocalShipping,
        iconBg = RedLight,
        iconTint = Red
    ) {
        IntlInput(
            label = "NÚMERO DE TRACKING",
            value = tracking,
            placeholder = "Ej: 1Z999AA10123456784",
            icon = Icons.Outlined.QrCode2,
            onValueChange = { tracking = it }
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

    Spacer(modifier = Modifier.height(12.dp))

    IntlTotalCard(totalEstimado)

    Spacer(modifier = Modifier.height(12.dp))

    IntlSectionCard(
        title = "Pago",
        icon = Icons.Outlined.CreditCard,
        iconBg = BlueLight,
        iconTint = Blue
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

    Spacer(modifier = Modifier.height(12.dp))

    PdfSelectorRow(
        pdfNombre = pdfNombre,
        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (errorMessage.isNotBlank()) {
        Text(
            text = errorMessage,
            color = if (errorMessage.contains("✅")) Color(0xFF2E7D32) else Red,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))
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
                            email.toCrearRequestBody(),
                            "internacional".toCrearRequestBody(),
                            webCompra.toCrearRequestBody(),
                            productos.toCrearRequestBody(),
                            precioCompra.toCrearRequestBody(),
                            tracking.toCrearRequestBody(),
                            fechaSeleccionada.toCrearRequestBody(),
                            pesoEstimado.toCrearRequestBody(),
                            metodoPago.toCrearRequestBody(),
                            "%.2f".format(totalEstimado).toCrearRequestBody(),
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
            containerColor = Red,
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

    Spacer(modifier = Modifier.height(18.dp))

    Text(
        text = "PEDIDO INTERNACIONAL",
        color = GrayLight,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.7.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(18.dp))

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
}

@Composable
private fun TypeTab(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Blue else Color.White)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else GrayText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = if (selected) Color.White else GrayText,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = GrayText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.7.sp
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun FormCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Blue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        content()
    }
}

@Composable
private fun ModernInput(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        color = GrayText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.7.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        enabled = onClick == null,
        placeholder = {
            Text(placeholder, color = GrayText)
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            disabledContainerColor = GrayBg,
            focusedContainerColor = GrayBg,
            unfocusedContainerColor = GrayBg,
            disabledIndicatorColor = GrayBorder,
            focusedIndicatorColor = GrayBorder,
            unfocusedIndicatorColor = GrayBorder,
            disabledTextColor = Dark,
            disabledPlaceholderColor = GrayText,
            cursorColor = Blue,
            focusedTextColor = Dark,
            unfocusedTextColor = Dark
        )
    )

    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun PriceBox(total: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE8EFFE))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total estimado",
            color = Blue,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = total,
            color = BlueDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDropdown(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = label,
        color = GrayText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.7.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GrayText
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GrayText
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrayBg,
                unfocusedContainerColor = GrayBg,
                focusedIndicatorColor = GrayBorder,
                unfocusedIndicatorColor = GrayBorder,
                cursorColor = Blue,
                focusedTextColor = Dark,
                unfocusedTextColor = Dark
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Dark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun IntlInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(BlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Blue,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Tarifa: $8.5 por kilo. Recibirás la dirección de nuestro almacén y asesoramiento personalizado.",
            color = GrayText,
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
                color = Dark,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = GrayLight,
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
        color = GrayText,
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
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = GrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GrayLight
            )
        },
        placeholder = {
            Text(
                text = "Seleccionar fecha",
                color = GrayLight,
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
        color = GrayText,
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
                tint = GrayLight,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GrayLight
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = GrayLight,
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
private fun IntlTotalCard(total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Blue)
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

        Text(
            text = "Peso × $8.5/kg\nSe actualiza al ingresar peso",
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 15.sp
        )
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
            tint = Red,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Factura PDF",
                color = Dark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = if (pdfNombre == "Ningún archivo seleccionado") {
                    "Opcional · Adjuntar comprobante"
                } else {
                    pdfNombre
                },
                color = GrayText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(9.dp),
            border = BorderStroke(1.5.dp, Blue),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Seleccionar",
                color = Blue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun intlFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = GrayBg,
    unfocusedContainerColor = GrayBg,
    disabledContainerColor = GrayBg,
    focusedBorderColor = GrayBorder,
    unfocusedBorderColor = GrayBorder,
    disabledBorderColor = GrayBorder,
    cursorColor = Blue,
    focusedTextColor = Dark,
    unfocusedTextColor = Dark,
    disabledTextColor = Dark,
    focusedPlaceholderColor = GrayLight,
    unfocusedPlaceholderColor = GrayLight,
    disabledPlaceholderColor = GrayLight
)


private fun normalizarTipoVehiculo(tipo: String): String {
    return when (tipo) {
        "Motorizado" -> "motorizado"
        "Auto / Sedan" -> "auto"
        "Van / Minivan" -> "van"
        "Camión" -> "camion"
        else -> "motorizado"
    }
}

private fun calcularTarifaCrearPedido(
    distanciaKm: Double,
    tamano: String,
    tipoVehiculo: String
): Double {
    val baseVehiculo = when (tipoVehiculo) {
        "Motorizado" -> 9.0
        "Auto / Sedan" -> 15.0
        "Van / Minivan" -> 35.0
        "Camión" -> 80.0
        else -> 0.0
    }

    val extraPorKm = when (tipoVehiculo) {
        "Motorizado" -> 1.0
        "Auto / Sedan" -> 1.6
        "Van / Minivan" -> 3.0
        "Camión" -> 5.0
        else -> 0.0
    }

    val extraTamano = when (tamano) {
        "Pequeño" -> 0.0
        "Mediano" -> 3.0
        "Grande" -> 6.0
        else -> 0.0
    }

    return if (distanciaKm <= 0.0 || tipoVehiculo.isBlank()) {
        0.0
    } else if (distanciaKm <= 5.0) {
        baseVehiculo + extraTamano
    } else {
        baseVehiculo + ((distanciaKm - 5.0) * extraPorKm) + extraTamano
    }
}

private fun calcularDistanciaCrearPedidoKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val radioTierra = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) *
            kotlin.math.sin(dLon / 2)

    val c = 2 * kotlin.math.atan2(
        kotlin.math.sqrt(a),
        kotlin.math.sqrt(1 - a)
    )

    return radioTierra * c
}*/