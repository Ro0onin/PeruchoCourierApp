package com.example.peruchocourierapp.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.theme.ThemeManager
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
private val IntlRed = Color(0xFFE02020)

private val IsDarkMode: Boolean
    @Composable get() = ThemeManager.isDarkMode.value

private val IntlBlueLight: Color
    @Composable get() = if (IsDarkMode) Color(0xFF172554) else Color(0xFFE8EFFE)

private val IntlRedLight: Color
    @Composable get() = if (IsDarkMode) Color(0xFF3F1717) else Color(0xFFFFF0F0)

private val IntlGrayBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6FB)

private val IntlCard: Color
    @Composable get() = if (IsDarkMode) Color(0xFF111827) else Color.White

private val IntlFieldBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF1F2937) else Color(0xFFF4F6FB)

private val IntlGrayBorder: Color
    @Composable get() = if (IsDarkMode) Color(0xFF334155) else Color(0xFFE8ECF4)

private val IntlGrayText: Color
    @Composable get() = if (IsDarkMode) Color(0xFFCBD5E1) else Color(0xFF6B7A99)

private val IntlGrayLight: Color
    @Composable get() = if (IsDarkMode) Color(0xFF94A3B8) else Color(0xFFB0BAD0)

private val IntlDark: Color
    @Composable get() = if (IsDarkMode) Color(0xFFF8FAFC) else Color(0xFF1A2340)

private val IntlInfoBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF172554) else Color(0xFFF2F6FF)

private val IntlSuccessText: Color
    @Composable get() = if (IsDarkMode) Color(0xFFDCFCE7) else Color(0xFF065F46)

fun String.toPlainRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoInternacionalScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val precioPorKg = 8.5

    var pasoActual by rememberSaveable { mutableIntStateOf(1) }

    var webCompra by rememberSaveable { mutableStateOf("") }
    var productos by rememberSaveable { mutableStateOf("") }
    var precioCompra by rememberSaveable { mutableStateOf("") }
    var tracking by rememberSaveable { mutableStateOf("") }
    var fechaSeleccionada by rememberSaveable { mutableStateOf("") }
    var pesoEstimado by rememberSaveable { mutableStateOf("") }
    var metodoPago by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfNombre by rememberSaveable { mutableStateOf("Ningún archivo seleccionado") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showAduanaDialog by remember { mutableStateOf(false) }
    var showTrackingHelp by remember { mutableStateOf(false) }

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

    fun validarPaso1(): Boolean {
        errorMessage = when {
            webCompra.isBlank() || productos.isBlank() || precioCompra.isBlank() ->
                "Completa la web, el producto y el precio."

            !esWebCompraValida(webCompra) ->
                "La web de compra debe terminar en .com. Ejemplo: amazon.com"

            productos.trim().length < 3 ->
                "Ingresa un nombre de producto válido."

            !esDecimalValido(precioCompra) ||
                    precioCompra.toDoubleOrNull() == null ||
                    precioCompra.toDouble() <= 0.0 ->
                "Ingresa un precio válido mayor a 0. Ejemplo: 25.99"

            else -> ""
        }
        return errorMessage.isBlank()
    }

    fun validarPaso2(): Boolean {
        errorMessage = when {
            tracking.isBlank() || fechaSeleccionada.isBlank() || pesoEstimado.isBlank() ->
                "Completa el tracking, la fecha y el peso estimado."

            tracking.length < 6 ->
                "Ingresa un número de tracking válido."

            !esDecimalValido(pesoEstimado) ||
                    pesoEstimado.toDoubleOrNull() == null ||
                    pesoEstimado.toDouble() <= 0.0 ->
                "Ingresa un peso válido mayor a 0. Ejemplo: 1.50"

            else -> ""
        }
        return errorMessage.isBlank()
    }

    fun enviarPedido() {
        errorMessage = when {
            metodoPago.isBlank() -> "Selecciona un método de pago."
            pdfUri == null -> "Adjunta la factura PDF."
            else -> ""
        }
        if (errorMessage.isNotBlank()) return

        val email = sessionManager.getUserEmail()
        if (email.isNullOrEmpty()) {
            errorMessage = "Sesión inválida."
            return
        }

        isSubmitting = true

        try {
            val input = context.contentResolver.openInputStream(pdfUri!!)
            val file = File.createTempFile("factura_", ".pdf", context.cacheDir)
            val output = FileOutputStream(file)

            input?.copyTo(output)
            input?.close()
            output.close()

            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val pdfPart = MultipartBody.Part.createFormData(
                "factura_pdf",
                file.name,
                requestFile
            )

            // Se conserva exactamente la misma llamada a Retrofit y los mismos campos del PHP.
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
                "%.2f".format(Locale.US, totalEstimado).toPlainRequestBody(),
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
                                popUpTo("client_lobby") { inclusive = false }
                                launchSingleTop = true
                            }
                        } else {
                            errorMessage = result?.message ?: "Respuesta vacía del servidor."
                        }
                    } else {
                        errorMessage =
                            "HTTP ${response.code()}: ${response.errorBody()?.string()}"
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    isSubmitting = false
                    errorMessage = "Error: ${t.message}"
                }
            })
        } catch (e: Exception) {
            isSubmitting = false
            errorMessage = "Error al procesar el PDF."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IntlGrayBg)
            .navigationBarsPadding()
    ) {
        IntlTopBar(navController)
        IntlStepIndicator(currentStep = pasoActual)

        AnimatedContent(
            targetState = pasoActual,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                if (targetState > initialState) {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(280)
                    ) togetherWith slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(280)
                    )
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(280)
                    ) togetherWith slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(280)
                    )
                }
            },
            label = "pedido_internacional_steps"
        ) { step ->
            when (step) {
                1 -> PasoProducto(
                    webCompra = webCompra,
                    productos = productos,
                    precioCompra = precioCompra,
                    errorMessage = errorMessage,
                    onWebChange = {
                        webCompra = it.lowercase().replace(" ", "")
                        errorMessage = ""
                    },
                    onProductoChange = {
                        productos = it
                        errorMessage = ""
                    },
                    onPrecioChange = {
                        if (it.all { c -> c.isDigit() || c == '.' }) {
                            precioCompra = it
                            errorMessage = ""
                        }
                    },
                    onContinuar = {
                        if (validarPaso1()) pasoActual = 2
                    }
                )

                2 -> PasoSeguimiento(
                    tracking = tracking,
                    fechaSeleccionada = fechaSeleccionada,
                    pesoEstimado = pesoEstimado,
                    totalEstimado = totalEstimado,
                    errorMessage = errorMessage,
                    onTrackingChange = {
                        tracking = it.uppercase().replace(" ", "")
                        errorMessage = ""
                    },
                    onFechaClick = { showDatePicker = true },
                    onPesoChange = {
                        if (it.all { c -> c.isDigit() || c == '.' }) {
                            pesoEstimado = it
                            errorMessage = ""
                        }
                    },
                    onHelpClick = { showTrackingHelp = true },
                    onInfoClick = { showAduanaDialog = true },
                    onAtras = {
                        errorMessage = ""
                        pasoActual = 1
                    },
                    onContinuar = {
                        if (validarPaso2()) pasoActual = 3
                    }
                )

                else -> PasoPago(
                    webCompra = webCompra,
                    productos = productos,
                    precioCompra = precioCompra,
                    tracking = tracking,
                    fechaSeleccionada = fechaSeleccionada,
                    pesoEstimado = pesoEstimado,
                    totalEstimado = totalEstimado,
                    metodoPago = metodoPago,
                    pdfNombre = pdfNombre,
                    errorMessage = errorMessage,
                    isSubmitting = isSubmitting,
                    onMetodoChange = {
                        metodoPago = it
                        errorMessage = ""
                    },
                    onPdfClick = {
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    },
                    onAtras = {
                        errorMessage = ""
                        pasoActual = 2
                    },
                    onEnviar = { enviarPedido() }
                )
            }
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
                            errorMessage = ""
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar") }
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
                        Icons.Outlined.PriorityHigh,
                        contentDescription = null,
                        tint = IntlRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text("Aviso de Aduana", color = IntlDark, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Team Perucho Courier te informa: si tu compra supera los $200 dólares, la aduana puede aplicar un impuesto aproximado del 25% sobre el valor declarado. Te recomendamos revisar el monto de tu compra antes de registrar tu pedido.",
                    color = IntlGrayText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAduanaDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IntlBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = IntlCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showTrackingHelp) {
        TrackingHelpDialog(onDismiss = { showTrackingHelp = false })
    }
}

@Composable
private fun PasoProducto(
    webCompra: String,
    productos: String,
    precioCompra: String,
    errorMessage: String,
    onWebChange: (String) -> Unit,
    onProductoChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onContinuar: () -> Unit
) {
    StepContainer {
        InfoBanner()

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
                onValueChange = onWebChange
            )

            IntlInput(
                label = "PRODUCTO",
                value = productos,
                placeholder = "Nombre del producto",
                icon = Icons.Outlined.Inventory2,
                onValueChange = onProductoChange
            )

            IntlInput(
                label = "PRECIO DE COMPRA",
                value = precioCompra,
                placeholder = "0.00",
                icon = Icons.Outlined.AttachMoney,
                keyboardType = KeyboardType.Decimal,
                onValueChange = onPrecioChange
            )
        }

        StepError(errorMessage)

        PrimaryStepButton(
            text = "Continuar",
            icon = Icons.Outlined.ArrowForward,
            onClick = onContinuar
        )
    }
}

@Composable
private fun PasoSeguimiento(
    tracking: String,
    fechaSeleccionada: String,
    pesoEstimado: String,
    totalEstimado: Double,
    errorMessage: String,
    onTrackingChange: (String) -> Unit,
    onFechaClick: () -> Unit,
    onPesoChange: (String) -> Unit,
    onHelpClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAtras: () -> Unit,
    onContinuar: () -> Unit
) {
    StepContainer {
        IntlSectionCard(
            title = "Seguimiento",
            icon = Icons.Outlined.LocalShipping,
            iconBg = IntlRedLight,
            iconTint = IntlRed
        ) {
            TrackingInputCard(
                tracking = tracking,
                onTrackingChange = onTrackingChange,
                onHelpClick = onHelpClick
            )

            TrackingInfoHintCard(onClick = onHelpClick)
            Spacer(modifier = Modifier.height(10.dp))

            IntlDateInput(
                label = "FECHA ESTIMADA DE LLEGADA",
                value = fechaSeleccionada,
                onClick = onFechaClick
            )

            IntlInput(
                label = "PESO ESTIMADO (KG)",
                value = pesoEstimado,
                placeholder = "0.00 kg",
                icon = Icons.Outlined.Scale,
                keyboardType = KeyboardType.Decimal,
                onValueChange = onPesoChange
            )
        }

        IntlTotalCard(total = totalEstimado, onInfoClick = onInfoClick)
        StepError(errorMessage)

        StepNavigationButtons(
            onBack = onAtras,
            onNext = onContinuar
        )
    }
}

@Composable
private fun PasoPago(
    webCompra: String,
    productos: String,
    precioCompra: String,
    tracking: String,
    fechaSeleccionada: String,
    pesoEstimado: String,
    totalEstimado: Double,
    metodoPago: String,
    pdfNombre: String,
    errorMessage: String,
    isSubmitting: Boolean,
    onMetodoChange: (String) -> Unit,
    onPdfClick: () -> Unit,
    onAtras: () -> Unit,
    onEnviar: () -> Unit
) {
    StepContainer {
        IntlSectionCard(
            title = "Método de pago",
            icon = Icons.Outlined.CreditCard,
            iconBg = IntlBlueLight,
            iconTint = IntlBlue
        ) {
            PaymentMethodCards(
                selectedMethod = metodoPago,
                onSelected = onMetodoChange
            )
        }

        PdfSelectorRow(
            pdfNombre = pdfNombre,
            onClick = onPdfClick
        )

        OrderSummaryCard(
            webCompra = normalizarWebCompra(webCompra),
            producto = productos,
            precioCompra = precioCompra,
            tracking = tracking,
            fecha = fechaSeleccionada,
            peso = pesoEstimado,
            metodoPago = metodoPago,
            total = totalEstimado
        )

        StepError(errorMessage)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onAtras,
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, IntlBlue)
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = IntlBlue)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Atrás", color = IntlBlue, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = onEnviar,
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1.45f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IntlRed)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviando...", fontWeight = FontWeight.Black)
                } else {
                    Icon(Icons.Outlined.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar pedido", fontWeight = FontWeight.Black)
                }
            }
        }

        Text(
            text = "PEDIDO INTERNACIONAL",
            color = IntlGrayLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.7.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StepContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun IntlStepIndicator(currentStep: Int) {
    val steps = listOf("Producto", "Tracking", "Pago")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntlCard)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, title ->
                val number = index + 1
                val completed = number < currentStep
                val active = number == currentStep
                val circleColor = when {
                    completed || active -> IntlBlue
                    else -> IntlGrayBorder
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(70.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(circleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                number.toString(),
                                color = if (active) Color.White else IntlGrayText,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        title,
                        color = if (active || completed) IntlBlue else IntlGrayLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                }

                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentStep > number) IntlBlue else IntlGrayBorder
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCards(
    selectedMethod: String,
    onSelected: (String) -> Unit
) {
    val methods = listOf(
        Triple("Yape", R.drawable.ic_yape, Color(0xFF6F2DBD)),
        Triple("Plin", R.drawable.ic_plin, Color(0xFF00B5E2)),
        Triple("BCP", R.drawable.ic_bcp, Color(0xFF0033A0)),
        Triple("Efectivo", R.drawable.ic_efectivo, Color(0xFF666666))
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        methods.forEach { (nombre, imagen, color) ->

            val seleccionado = nombre == selectedMethod

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelected(nombre)
                    },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    if (seleccionado) 2.dp else 1.dp,
                    if (seleccionado) IntlBlue else IntlGrayBorder
                ),
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (seleccionado)
                            IntlBlueLight
                        else
                            IntlFieldBg
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(imagen),
                        contentDescription = nombre,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = IntlDark
                        )

                        Text(
                            when(nombre){

                                "Yape" ->
                                    "Pago inmediato"

                                "Plin" ->
                                    "Transferencia"

                                "BCP" ->
                                    "Depósito o transferencia"

                                "Interbank" ->
                                    "Depósito o transferencia"

                                else ->
                                    "Pago contra entrega"
                            },
                            color = IntlGrayText,
                            fontSize = 12.sp
                        )
                    }

                    RadioButton(
                        selected = seleccionado,
                        onClick = {
                            onSelected(nombre)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = color
                        )
                    )
                }
            }
        }
    }
}
@Composable
private fun OrderSummaryCard(
    webCompra: String,
    producto: String,
    precioCompra: String,
    tracking: String,
    fecha: String,
    peso: String,
    metodoPago: String,
    total: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IntlCard)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = IntlBlue)
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                "RESUMEN DEL PEDIDO",
                color = IntlDark,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SummaryRow("Web", webCompra)
        SummaryRow("Producto", producto)
        SummaryRow("Precio de compra", "$${precioCompra.ifBlank { "0.00" }}")
        SummaryRow("Tracking", tracking)
        SummaryRow("Fecha estimada", fecha)
        SummaryRow("Peso estimado", "${peso.ifBlank { "0.00" }} kg")
        SummaryRow("Método de pago", metodoPago.ifBlank { "Sin seleccionar" })

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = IntlGrayBorder
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "TOTAL ESTIMADO",
                color = IntlGrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "$${"%.2f".format(Locale.US, total)}",
                color = IntlBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            color = IntlGrayText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            value,
            color = IntlDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun StepError(message: String) {
    if (message.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(IntlRedLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = IntlRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                message,
                color = IntlRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PrimaryStepButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = IntlBlue)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(icon, contentDescription = null)
    }
}

@Composable
private fun StepNavigationButtons(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, IntlBlue)
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = IntlBlue)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Atrás", color = IntlBlue, fontWeight = FontWeight.Black)
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1.25f)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IntlBlue)
        ) {
            Text("Continuar", fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun IntlTopBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(Brush.horizontalGradient(listOf(IntlBlueDark, IntlBlue)))
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
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Pedido Internacional",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "USA / China → Perú",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                Icons.Outlined.Language,
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
            .clip(RoundedCornerShape(12.dp))
            .background(IntlCard)
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
                Icons.Outlined.Info,
                contentDescription = null,
                tint = IntlBlue,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            "Tarifa: $8.5 por kilo. Recibirás la dirección de nuestro almacén y asesoramiento personalizado.",
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
            .background(IntlCard)
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
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                title.uppercase(),
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
private fun TrackingInputCard(
    tracking: String,
    onTrackingChange: (String) -> Unit,
    onHelpClick: () -> Unit
) {
    Text(
        "NÚMERO DE TRACKING",
        color = IntlGrayText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    OutlinedTextField(
        value = tracking,
        onValueChange = onTrackingChange,
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Outlined.QrCode2, null, tint = IntlGrayLight, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(IntlBlueLight)
                    .clickable(onClick = onHelpClick),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = IntlBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        },
        placeholder = {
            Text(
                "Ej: 9400111899223754906185",
                color = IntlGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = intlFieldColors()
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun TrackingInfoHintCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IntlInfoBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(IntlBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "¿Dónde encuentro mi tracking number?",
                color = IntlBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                "Presiona el signo de interrogación para ver ejemplos de Amazon y eBay.",
                color = IntlGrayText,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TrackingHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IntlCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "¿Dónde encuentro mi Tracking Number?",
                            color = IntlDark,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Ejemplos de Amazon y eBay",
                            color = IntlGrayText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Cerrar", tint = IntlDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Image(
                    painter = painterResource(id = R.drawable.tracking_help),
                    contentDescription = "Ejemplo de ubicación de tracking number",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IntlRedLight)
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = IntlRed,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            "Importante",
                            color = IntlRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Copia y pega el número de seguimiento exactamente como aparece en Amazon o eBay para un mejor rastreo de tu pedido.",
                            color = IntlDark,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IntlBlue)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Black)
                }
            }
        }
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
        label,
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
            Icon(icon, null, tint = IntlGrayLight, modifier = Modifier.size(20.dp))
        },
        placeholder = {
            Text(
                placeholder,
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
        label,
        color = IntlGrayText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Outlined.CalendarMonth, null, tint = IntlGrayLight)
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, tint = IntlGrayLight)
            },
            placeholder = {
                Text(
                    "Seleccionar fecha",
                    color = IntlGrayLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = intlFieldColors()
        )

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
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
                "TOTAL ESTIMADO",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                "$${"%.2f".format(Locale.US, total)}",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Peso × $8.5/kg\nSe actualiza al ingresar peso",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onInfoClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.PriorityHigh,
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
            .background(IntlCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.PictureAsPdf,
            contentDescription = null,
            tint = IntlRed,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Comprobante de compra",
                color = IntlDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                if (pdfNombre == "Ningún archivo seleccionado") {
                    "Obligatorio · Adjuntar comprobante PDF"
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
                if (pdfNombre == "Ningún archivo seleccionado") "Seleccionar" else "Cambiar",
                color = IntlBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun intlFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = IntlFieldBg,
    unfocusedContainerColor = IntlFieldBg,
    disabledContainerColor = IntlFieldBg,
    focusedBorderColor = IntlBlue,
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
    return web.matches(Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.com$"))
}

private fun esDecimalValido(input: String): Boolean {
    return input.matches(Regex("^\\d+(\\.\\d{1,2})?$"))
}