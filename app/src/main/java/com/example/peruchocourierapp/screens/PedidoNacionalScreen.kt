package com.example.peruchocourierapp.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.theme.ThemeManager
import com.example.peruchocourierapp.utils.obtenerRutaCompleta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

private val Red = Color(0xFFE02020)
private val Green = Color(0xFF22C55E)
private val Blue = Color(0xFF1A4FBF)

private val IsDarkMode: Boolean
    @Composable get() = ThemeManager.isDarkMode.value

private val ScreenBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)

private val CardBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF111827) else Color(0xFFFFFFFF)

private val Dark: Color
    @Composable get() = if (IsDarkMode) Color(0xFFF8FAFC) else Color(0xFF1A1A1A)

private val Muted: Color
    @Composable get() = if (IsDarkMode) Color(0xFFCBD5E1) else Color(0xFF888888)

private val LightBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF1F2937) else Color(0xFFF5F5F5)

private val Border: Color
    @Composable get() = if (IsDarkMode) Color(0xFF334155) else Color(0xFFE8E8E8)

private val SoftBlueBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF172554) else Color(0xFFE8EFFE)

private val SoftRedBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF3F1717) else Color(0xFFFFEAEA)

private val SoftGreenBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF14532D) else Color(0xFFEAFBF0)

private val DisabledBg: Color
    @Composable get() = if (IsDarkMode) Color(0xFF374151) else Color(0xFFE8E8E8)

private val DisabledText: Color
    @Composable get() = if (IsDarkMode) Color(0xFF94A3B8) else Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoNacionalScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)

    var pickupAddress by remember { mutableStateOf("") }
    var dropoffAddress by remember { mutableStateOf("") }

    var pickupLat by remember { mutableDoubleStateOf(0.0) }
    var pickupLng by remember { mutableDoubleStateOf(0.0) }
    var dropoffLat by remember { mutableDoubleStateOf(0.0) }
    var dropoffLng by remember { mutableDoubleStateOf(0.0) }

    var descripcion by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Documentos") }
    var comentarioRepartidor by remember { mutableStateOf("") }

    var selectedVehicle by remember { mutableStateOf("Motorizado") }
    var metodoPago by remember { mutableStateOf("Yape") }
    var pesoKg by remember { mutableStateOf("") }
    var cantidadBultos by remember { mutableStateOf("1") }
    var fotoPaqueteUri by remember { mutableStateOf<Uri?>(null) }

    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distanciaKm by remember { mutableDoubleStateOf(0.0) }
    var duracionMin by remember { mutableStateOf(0) }

    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var showQueEnviarasPopup by remember { mutableStateOf(false) }
    var showContactoPopup by remember { mutableStateOf(false) }
    var showFotoPopup by remember { mutableStateOf(false) }
    var showComentarioPopup by remember { mutableStateOf(false) }
    var showMotorizadoTarifasPopup by remember { mutableStateOf(false) }

    var tarifaMotorizado by remember { mutableStateOf("plana") }
    var destinatarioPaga by remember { mutableStateOf(false) }

    val pesoDouble = pesoKg.replace(",", ".").toDoubleOrNull()

    LaunchedEffect(pesoDouble, selectedVehicle) {
        if (selectedVehicle == "Motorizado" && pesoDouble != null) {
            tarifaMotorizado =
                if (pesoDouble > 2.0) {
                    "estandar"
                } else {
                    "plana"
                }

            errorMessage =
                if (pesoDouble > 2.0) {
                    "Se cambió automáticamente a Tarifa Estándar porque supera los 2 kg"
                } else {
                    ""
                }
        }

        if (
            selectedVehicle == "Van / Minivan" &&
            pesoDouble != null &&
            pesoDouble > 800
        ) {
            errorMessage = "Van / Minivan solo permite paquetes hasta 800 kg"
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoPaqueteUri = uri
    }

    val currentSavedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val previousSavedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(currentSavedStateHandle, previousSavedStateHandle) {
        val pickupAddressSaved =
            currentSavedStateHandle?.get<String>("pickup_address")
                ?: previousSavedStateHandle?.get<String>("pickup_address")

        val pickupLatSaved =
            currentSavedStateHandle?.get<Double>("pickup_lat")
                ?: previousSavedStateHandle?.get<Double>("pickup_lat")

        val pickupLngSaved =
            currentSavedStateHandle?.get<Double>("pickup_lng")
                ?: previousSavedStateHandle?.get<Double>("pickup_lng")

        val dropoffAddressSaved =
            currentSavedStateHandle?.get<String>("dropoff_address")
                ?: previousSavedStateHandle?.get<String>("dropoff_address")

        val dropoffLatSaved =
            currentSavedStateHandle?.get<Double>("dropoff_lat")
                ?: previousSavedStateHandle?.get<Double>("dropoff_lat")

        val dropoffLngSaved =
            currentSavedStateHandle?.get<Double>("dropoff_lng")
                ?: previousSavedStateHandle?.get<Double>("dropoff_lng")

        pickupAddressSaved?.let { pickupAddress = it }
        pickupLatSaved?.let { pickupLat = it }
        pickupLngSaved?.let { pickupLng = it }

        dropoffAddressSaved?.let { dropoffAddress = it }
        dropoffLatSaved?.let { dropoffLat = it }
        dropoffLngSaved?.let { dropoffLng = it }
    }

    LaunchedEffect(pickupLat, pickupLng, dropoffLat, dropoffLng) {
        val target = when {
            pickupLat != 0.0 && pickupLng != 0.0 &&
                    dropoffLat != 0.0 && dropoffLng != 0.0 -> {
                LatLng(
                    (pickupLat + dropoffLat) / 2,
                    (pickupLng + dropoffLng) / 2
                )
            }

            pickupLat != 0.0 && pickupLng != 0.0 -> {
                LatLng(pickupLat, pickupLng)
            }

            else -> LatLng(-12.0464, -77.0428)
        }

        val zoom = if (
            pickupLat != 0.0 && pickupLng != 0.0 &&
            dropoffLat != 0.0 && dropoffLng != 0.0
        ) {
            11.2f
        } else {
            13.8f
        }

        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(target, zoom)
        )

        if (
            pickupLat != 0.0 && pickupLng != 0.0 &&
            dropoffLat != 0.0 && dropoffLng != 0.0
        ) {
            val resultado = withContext(Dispatchers.IO) {
                obtenerRutaCompleta(
                    origin = "$pickupLat,$pickupLng",
                    destination = "$dropoffLat,$dropoffLng"
                )
            }

            val results = FloatArray(1)

            Location.distanceBetween(
                pickupLat,
                pickupLng,
                dropoffLat,
                dropoffLng,
                results
            )

            val distanciaRectaKm = results[0] / 1000.0

            val distanciaFinal = if (
                resultado.distanciaKm > distanciaRectaKm * 3 &&
                distanciaRectaKm < 5
            ) {
                distanciaRectaKm * 1.35
            } else {
                resultado.distanciaKm
            }

            ruta = resultado.puntos
            distanciaKm = distanciaFinal
            duracionMin = resultado.duracionMin
        }
    }

    val totalSeleccionado = calcularPrecioVehiculo(
        vehiculo = selectedVehicle,
        distanciaKm = distanciaKm,
        tarifaMotorizado = tarifaMotorizado,
        pesoKg = pesoDouble
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PedidoTopBar(
            onBack = { navController.popBackStack() }
        )

        MiniMapPedido(
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            ruta = ruta,
            cameraPositionState = cameraPositionState
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            RoutePillsPedido(
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                onPickupClick = { navController.navigate("map_picker/pickup") },
                onDropoffClick = { navController.navigate("map_picker/dropoff") }
            )

            Text(
                text = "ELIGE TU VEHÍCULO",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

            val bloqueaVan = pesoDouble != null && pesoDouble > 800.0

            VehicleCard(
                icon = R.drawable.motorizado,
                name = if (tarifaMotorizado == "plana") {
                    "Motorizado Tarifa Plana"
                } else {
                    "Motorizado Tarifa Estándar"
                },
                desc = "Paquetes pequeños hasta 2kg o un poco más. Rápido en tráfico.",
                price = calcularPrecioVehiculo(
                    vehiculo = "Motorizado",
                    distanciaKm = distanciaKm,
                    tarifaMotorizado = tarifaMotorizado,
                    pesoKg = pesoDouble
                ),
                selected = selectedVehicle == "Motorizado",
                recommended = true,
                enabled = true,
                disabledReason = null,
                showInfoButton = true,
                onInfoClick = { showMotorizadoTarifasPopup = true },
                onClick = { selectedVehicle = "Motorizado" }
            )

            VehicleCard(
                icon = R.drawable.trailer,
                name = "Van / Minivan",
                desc = "Hasta 800 kg. Mudanzas, carga y paquetes voluminosos.",
                price = calcularPrecioVehiculo("Van / Minivan", distanciaKm),
                selected = selectedVehicle == "Van / Minivan" && !bloqueaVan,
                enabled = !bloqueaVan,
                disabledReason = "No disponible: supera los 800 kg permitidos.",
                onClick = { selectedVehicle = "Van / Minivan" }
            )

            ZoneNote()

            if (distanciaKm > 0.0) {
                Text(
                    text = "${"%.2f".format(distanciaKm)} km · aprox. $duracionMin min",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            ConfirmBar(
                total = totalSeleccionado,
                cantidadBultos = cantidadBultos,
                isSubmitting = isSubmitting,
                destinatarioPaga = destinatarioPaga,
                metodoPago = metodoPago,
                descripcion = descripcion,
                categoria = itemCategory,
                pesoKg = pesoKg,
                senderPhone = senderPhone,
                receiverPhone = receiverPhone,
                fotoPaqueteUri = fotoPaqueteUri,
                onPesoChange = { valor ->
                    pesoKg = valor.filter { caracter ->
                        caracter.isDigit() || caracter == '.' || caracter == ','
                    }
                },
                onCantidadBultosChange = { valor ->
                    cantidadBultos = valor.filter { it.isDigit() }
                },
                onEditDetails = {
                    showQueEnviarasPopup = true
                },
                onDestinatarioPagaChange = { destinatarioPaga = it },
                onMetodoPagoChange = { metodoPago = it },
                onConfirm = {
                    val pesoValidado = pesoKg.replace(",", ".").toDoubleOrNull()
                    val bultosValidados = cantidadBultos.toIntOrNull()

                    when {
                        pickupAddress.isBlank() || dropoffAddress.isBlank() -> {
                            errorMessage = "Selecciona punto de recojo y entrega"
                        }

                        descripcion.isBlank() -> {
                            errorMessage = ""
                            showQueEnviarasPopup = true
                        }

                        pesoValidado == null || pesoValidado <= 0.0 -> {
                            errorMessage = "Ingresa un peso aproximado válido"
                        }

                        bultosValidados == null || bultosValidados <= 0 -> {
                            errorMessage = "Ingresa una cantidad válida de paquetes"
                        }

                        senderPhone.isBlank() || receiverPhone.isBlank() -> {
                            errorMessage = ""
                            showContactoPopup = true
                        }
                        fotoPaqueteUri == null -> {
                            errorMessage = ""
                            showFotoPopup = true
                        }
                        else -> {
                            val userEmail = sessionManager.getUserEmail()

                            if (userEmail.isNullOrBlank()) {
                                errorMessage = "Inicia sesión para crear un pedido"
                                return@ConfirmBar
                            }

                            try {
                                val fotoFile = uriToFile(context, fotoPaqueteUri!!)
                                val fotoRequest = fotoFile.asRequestBody("image/*".toMediaTypeOrNull())
                                val fotoPart = MultipartBody.Part.createFormData(
                                    "foto_paquete",
                                    fotoFile.name,
                                    fotoRequest
                                )

                                isSubmitting = true
                                errorMessage = ""

                                RetrofitClient.instance.createNationalOrder(
                                    userEmail = textPart(userEmail),
                                    tipoEnvio = textPart("nacional"),
                                    origen = textPart(pickupAddress),
                                    destino = textPart(dropoffAddress),
                                    pickupAddress = textPart(pickupAddress),
                                    pickupLat = textPart(pickupLat.toString()),
                                    pickupLng = textPart(pickupLng.toString()),
                                    dropoffAddress = textPart(dropoffAddress),
                                    dropoffLat = textPart(dropoffLat.toString()),
                                    dropoffLng = textPart(dropoffLng.toString()),
                                    telefonoRemitente = textPart(senderPhone),
                                    telefonoDestinatario = textPart(receiverPhone),
                                    descripcion = textPart(descripcion),
                                    categoria = textPart(itemCategory),
                                    comentariosRepartidor = textPart(comentarioRepartidor),
                                    cantidadBultos = textPart(cantidadBultos),
                                    pesoKg = textPart("%.2f".format(pesoValidado)),
                                    tipoVehiculo = textPart(
                                        when (selectedVehicle) {
                                            "Motorizado" -> "motorizado"
                                            "Van / Minivan" -> "van"
                                            else -> "motorizado"
                                        }
                                    ),
                                    tarifaMotorizado = textPart(
                                        if (selectedVehicle == "Motorizado") tarifaMotorizado else ""
                                    ),
                                    destinatarioPaga = textPart(
                                        if (destinatarioPaga) "1" else "0"
                                    ),
                                    metodoPago = textPart(metodoPago),
                                    distanciaKm = textPart("%.2f".format(distanciaKm)),
                                    total = textPart("%.2f".format(totalSeleccionado)),
                                    fotoPaquete = fotoPart
                                ).enqueue(object : Callback<BasicResponse> {
                                    override fun onResponse(
                                        call: Call<BasicResponse>,
                                        response: Response<BasicResponse>
                                    ) {
                                        isSubmitting = false
                                        val result = response.body()

                                        if (response.isSuccessful && result?.success == true) {
                                            navController.navigate("mis_pedidos") {
                                                popUpTo("client_lobby") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            errorMessage = result?.message ?: "No se pudo crear el pedido"
                                        }
                                    }

                                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                                        isSubmitting = false
                                        errorMessage = "Error: ${t.message}"
                                    }
                                })
                            } catch (e: Exception) {
                                isSubmitting = false
                                errorMessage = "No se pudo preparar la foto: ${e.message}"
                            }
                        }
                    }
                }
            )
        }
    }

    if (showQueEnviarasPopup) {
        QueEnviarasPopup(
            descripcion = descripcion,
            categoria = itemCategory,
            onDescripcionChange = { descripcion = it },
            onCategoriaChange = { itemCategory = it },
            onDismiss = { showQueEnviarasPopup = false },
            onContinue = {
                errorMessage = ""
                showQueEnviarasPopup = false
                showContactoPopup = true
            }
        )
    }
    if (showContactoPopup) {
        ContactoPedidoPopup(
            senderPhone = senderPhone,
            receiverPhone = receiverPhone,
            onSenderPhoneChange = { senderPhone = it },
            onReceiverPhoneChange = { receiverPhone = it },
            onDismiss = { showContactoPopup = false },
            onContinue = {
                errorMessage = ""
                showContactoPopup = false
                showFotoPopup = true
            }
        )
    }
    if (showFotoPopup) {
        FotoPaquetePopup(
            fotoPaqueteUri = fotoPaqueteUri,
            onSelectPhoto = {
                imagePickerLauncher.launch("image/*")
            },
            onDismiss = { showFotoPopup = false },
            onContinue = {
                errorMessage = ""
                showFotoPopup = false
                showComentarioPopup = true
            }
        )
    }
    if (showComentarioPopup) {
        ComentarioRepartidorPopup(
            comentario = comentarioRepartidor,
            onComentarioChange = { comentarioRepartidor = it },
            onDismiss = { showComentarioPopup = false },
            onSkip = {
                comentarioRepartidor = ""
                showComentarioPopup = false
            },
            onFinish = {
                errorMessage = ""
                showComentarioPopup = false
            }
        )
    }

    if (showMotorizadoTarifasPopup) {
        MotorizadoTarifasPopup(
            selectedTarifa = tarifaMotorizado,
            pesoKg = pesoDouble,
            onSelectTarifa = {
                if (it == "plana" && (pesoDouble ?: 0.0) > 2) {
                    tarifaMotorizado = "estandar"
                    errorMessage = "La Tarifa Plana no está disponible para paquetes mayores a 2kg"
                } else {
                    tarifaMotorizado = it
                    errorMessage = ""
                }
            },
            onDismiss = { showMotorizadoTarifasPopup = false }
        )
    }
}

@Composable
private fun PedidoTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(LightBg)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Dark, modifier = Modifier.size(19.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Pedido Nacional",
            color = Dark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.weight(1f))

        StepPills()
    }
}

@Composable
private fun StepPills() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.width(20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Dark))
        Box(Modifier.width(20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Dark))
        Box(Modifier.width(20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Red))
        Box(Modifier.width(20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Border))
    }
}

@Composable
private fun MiniMapPedido(
    pickupLat: Double,
    pickupLng: Double,
    dropoffLat: Double,
    dropoffLng: Double,
    ruta: List<LatLng>,
    cameraPositionState: CameraPositionState,
    animationDurationMs: Int = 2600
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(ruta) {
        if (ruta.size >= 2) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = if (ruta.size > 120) 3400 else animationDurationMs,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            progress.snapTo(0f)
        }
    }

    val puntosVisibles = remember(ruta, progress.value) {
        if (ruta.size < 2) {
            emptyList()
        } else {
            val total = ruta.size
            val posicion = (progress.value * (total - 1)).coerceIn(0f, (total - 1).toFloat())
            val index = posicion.toInt().coerceIn(0, total - 1)
            val fraccion = posicion - index
            val base = ruta.take(index + 1).toMutableList()

            if (index < total - 1) {
                val desde = ruta[index]
                val hasta = ruta[index + 1]

                val puntoInterpolado = LatLng(
                    desde.latitude + (hasta.latitude - desde.latitude) * fraccion,
                    desde.longitude + (hasta.longitude - desde.longitude) * fraccion
                )

                base.add(puntoInterpolado)
            }

            base
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            if (pickupLat != 0.0 && pickupLng != 0.0) {
                Marker(
                    state = MarkerState(LatLng(pickupLat, pickupLng)),
                    title = "Recojo",
                    icon = bitmapDescriptorFromDrawableSafe(
                        context,
                        R.drawable.ic_pin_recojo,
                        120,
                        120
                    )
                )
            }
            MapEffect(
                ThemeManager.isDarkMode.value
            ) { map ->

                if (ThemeManager.isDarkMode.value) {

                    map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style_dark
                        )
                    )

                } else {

                    map.setMapStyle(null)

                }
            }

            if (
                dropoffLat != 0.0 &&
                dropoffLng != 0.0 &&
                (ruta.isEmpty() || progress.value >= 0.92f)
            ) {
                Marker(
                    state = MarkerState(LatLng(dropoffLat, dropoffLng)),
                    title = "Entrega",
                    icon = bitmapDescriptorFromDrawableSafe(
                        context,
                        R.drawable.ic_pin_entrega,
                        120,
                        120
                    )
                )
            }

            if (puntosVisibles.size >= 2) {
                Polyline(
                    points = puntosVisibles,
                    color = Red.copy(alpha = 0.22f),
                    width = 18f,
                    zIndex = 0f
                )

                Polyline(
                    points = puntosVisibles,
                    color = Red,
                    width = 8f,
                    zIndex = 1f
                )

                Polyline(
                    points = puntosVisibles,
                    color = Color.White.copy(alpha = 0.45f),
                    width = 3f,
                    zIndex = 2f
                )
            }
        }
    }
}

@Composable
private fun RoutePillsPedido(
    pickupAddress: String,
    dropoffAddress: String,
    onPickupClick: () -> Unit,
    onDropoffClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RoutePill(
            color = Green,
            text = if (pickupAddress.isBlank()) "Seleccionar punto de recojo" else pickupAddress,
            onClick = onPickupClick
        )

        RoutePill(
            color = Red,
            text = if (dropoffAddress.isBlank()) "Seleccionar punto de entrega" else dropoffAddress,
            onClick = onDropoffClick
        )
    }
}

@Composable
private fun RoutePill(
    color: Color,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Dark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun VehicleCard(
    icon: Int,
    name: String,
    desc: String,
    price: Double,
    selected: Boolean,
    recommended: Boolean = false,
    enabled: Boolean = true,
    disabledReason: String? = null,
    showInfoButton: Boolean = false,
    onInfoClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.38f

    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    !enabled -> LightBg
                    selected -> SoftBlueBg
                    else -> CardBg
                }
            )
            .border(
                width = if (selected && enabled) 2.dp else 1.dp,
                color = when {
                    !enabled -> Color(0xFFE0E0E0)
                    selected -> Blue
                    else -> Border
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = name,
                modifier = Modifier.size(56.dp),
                alpha = alpha
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name,
                    color = Dark.copy(alpha = alpha),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (showInfoButton && enabled && onInfoClick != null) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Blue)
                            .clickable { onInfoClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Ver tarifas",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (!enabled && !disabledReason.isNullOrBlank()) disabledReason else desc,
                color = if (enabled) Muted else Red,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = if (enabled) FontWeight.Normal else FontWeight.Bold
            )

            if (enabled && name.contains("Motorizado")) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (name.contains("Plana")) {
                        "S/10 fijo hasta 2kg"
                    } else {
                        "S/10 + km extra según distancia"
                    },
                    color = Green,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            if (recommended && enabled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Red)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Popular",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            if (!enabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Red,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "S/ ${"%.0f".format(price)}",
                    color = Dark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Lima · Callao",
                    color = Muted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun QueEnviarasPopup(
    descripcion: String,
    categoria: String,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    val categorias = listOf(
        Triple("Documentos", "📄", Blue),
        Triple("Ropa", "👕", Red),
        Triple("Tecnología", "💻", Blue),
        Triple("Alimentos", "🍴", Color(0xFFF97316)),
        Triple("Medicinas", "🧴", Green),
        Triple("Accesorios", "🛍️", Color(0xFF9333EA)),
        Triple("Otros", "•••", Muted)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DisabledText)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "¿Qué enviarás?",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Text(
                    text = "Describe brevemente el contenido del paquete.",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    placeholder = {
                        Text(
                            text = "Ej. Documentos SUNAT",
                            color = Muted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = false,
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Categoría",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Spacer(modifier = Modifier.height(10.dp))

                categorias.forEach { item ->
                    val nombre = item.first
                    val emoji = item.second
                    val color = item.third
                    val selected = categoria == nombre

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selected) SoftBlueBg
                                else CardBg
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Blue else Border,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onCategoriaChange(nombre) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = nombre,
                            color = Dark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )

                        RadioButton(
                            selected = selected,
                            onClick = { onCategoriaChange(nombre) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Blue,
                                unselectedColor = DisabledText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onContinue,
                    enabled = descripcion.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledBg,
                        disabledContentColor = DisabledText
                    )
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        color = Red,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun MotorizadoTarifasPopup(
    selectedTarifa: String,
    pesoKg: Double?,
    onSelectTarifa: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val puedeUsarTarifaPlana = (pesoKg ?: 0.0) <= 2.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(22.dp),
        confirmButton = {},
        title = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.motorizado),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Tarifas del motorizado",
                        color = Dark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(LightBg)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Muted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (puedeUsarTarifaPlana) {
                        "Elige la tarifa que más te conviene para este envío."
                    } else {
                        "La tarifa plana no está disponible porque supera los 2kg."
                    },
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                MotorizadoTarifaCard(
                    title = "Tarifa Plana",
                    tag = if (puedeUsarTarifaPlana) "Precio fijo" else "No disponible",
                    price = "S/ 10",
                    subtitle = if (puedeUsarTarifaPlana) "precio único" else "máx. 2kg",
                    selected = selectedTarifa == "plana" && puedeUsarTarifaPlana,
                    enabled = puedeUsarTarifaPlana,
                    onClick = {
                        if (puedeUsarTarifaPlana) {
                            onSelectTarifa("plana")
                            onDismiss()
                        }
                    },
                    items = if (puedeUsarTarifaPlana) {
                        listOf(
                            "Ideal para paquetes pequeños y livianos",
                            "Hasta 2 kg con precio fijo de S/10",
                            "No aumenta por distancia si no supera 2 kg"
                        )
                    } else {
                        listOf(
                            "No disponible para paquetes mayores a 2 kg",
                            "Selecciona Tarifa Estándar para continuar",
                            "Disponible solo hasta 2 kg"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MotorizadoTarifaCard(
                    title = "Tarifa Estándar",
                    tag = "Más capacidad",
                    price = "S/ 10",
                    subtitle = "+ S/1 por km extra",
                    selected = selectedTarifa == "estandar",
                    enabled = true,
                    onClick = {
                        onSelectTarifa("estandar")
                        onDismiss()
                    },
                    items = listOf(
                        "Para paquetes de 2kg a más",
                        "Precio base de S/10",
                        "+ S/1 por km extra después de los 5 km"
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Entendido",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        color = Blue,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

@Composable
private fun MotorizadoTarifaCard(
    title: String,
    tag: String,
    price: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    items: List<String>
) {
    val isDark = IsDarkMode
    val alpha = if (enabled) 1f else 0.45f

    val selectedBackground = if (isDark) {
        Color(0xFF5A171B)
    } else {
        Color(0xFFFFEAEA)
    }

    val selectedBorder = if (isDark) {
        Color(0xFFFF5A5F)
    } else {
        Red
    }

    val selectedTitleColor = if (isDark) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF7F1D1D)
    }

    val selectedBodyColor = if (isDark) {
        Color(0xFFFFE4E6)
    } else {
        Color(0xFF5A3336)
    }

    val selectedSecondaryColor = if (isDark) {
        Color(0xFFFECACA)
    } else {
        Color(0xFF7F1D1D)
    }

    val unselectedTitleColor = Dark
    val unselectedBodyColor = if (isDark) {
        Color(0xFFE2E8F0)
    } else {
        Muted
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                onClick()
            },
        color = when {
            !enabled -> LightBg
            selected -> selectedBackground
            else -> CardBg
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = when {
                !enabled -> Border.copy(alpha = 0.55f)
                selected -> selectedBorder
                else -> Border
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(15.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected && enabled) {
                            Red
                        } else {
                            if (isDark) Color(0xFF111827) else CardBg
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (selected && enabled) {
                            selectedBorder
                        } else {
                            Border
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selected && enabled -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    !enabled -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Muted.copy(alpha = alpha),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .padding(end = 26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected -> selectedTitleColor
                                else -> unselectedTitleColor
                            },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = tag.uppercase(),
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected && isDark -> Color(0xFF60A5FA)
                                else -> Blue
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = price,
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected -> selectedTitleColor
                                else -> unselectedTitleColor
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = subtitle,
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected -> selectedSecondaryColor
                                else -> unselectedBodyColor
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(modifier = Modifier.height(9.dp))

                items.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "–",
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected -> selectedBorder
                                else -> Muted
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = item,
                            color = when {
                                !enabled -> Muted.copy(alpha = alpha)
                                selected -> selectedBodyColor
                                else -> unselectedTitleColor
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 19.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneNote() {
    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8F0))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFFF97316),
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Los precios varían según distancia, peso y tipo de vehículo.",
            color = Color(0xFF92400E),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ConfirmBar(
    total: Double,
    cantidadBultos: String,
    isSubmitting: Boolean,
    destinatarioPaga: Boolean,
    metodoPago: String,
    descripcion: String,
    categoria: String,
    pesoKg: String,
    senderPhone: String,
    receiverPhone: String,
    fotoPaqueteUri: Uri?,
    onPesoChange: (String) -> Unit,
    onCantidadBultosChange: (String) -> Unit,
    onEditDetails: () -> Unit,
    onDestinatarioPagaChange: (Boolean) -> Unit,
    onMetodoPagoChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {

        Text(
            text = "Peso y cantidad",
            color = Dark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LightBg,
            border = BorderStroke(1.5.dp, Border)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = pesoKg,
                    onValueChange = { value ->
                        onPesoChange(
                            value.filter { character ->
                                character.isDigit() || character == '.' || character == ','
                            }
                        )
                    },
                    label = { Text("Peso aproximado") },
                    placeholder = { Text("Ej. 2") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        focusedLabelColor = Blue,
                        unfocusedLabelColor = Muted,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cantidadBultos,
                    onValueChange = { value ->
                        onCantidadBultosChange(value.filter { it.isDigit() })
                    },
                    label = { Text("Cantidad de paquetes") },
                    placeholder = { Text("Ej. 1") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        focusedLabelColor = Blue,
                        unfocusedLabelColor = Muted,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF8E8))
                        .border(
                            1.dp,
                            Color(0xFFFFD166),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {

                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {

                        Text(
                            text = "Peso referencial",
                            color = Color(0xFF92400E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "El peso ingresado es aproximado y será verificado por el repartidor durante el recojo. Si el paquete supera los 2 kg, el pedido cambiará automáticamente a Tarifa Estándar y el costo será recalculado.",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (
            descripcion.isNotBlank() ||
            pesoKg.isNotBlank() ||
            senderPhone.isNotBlank() ||
            receiverPhone.isNotBlank() ||
            fotoPaqueteUri != null
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditDetails() },
                shape = RoundedCornerShape(18.dp),
                color = LightBg,
                border = BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Datos del pedido",
                            color = Dark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Modificar",
                            color = Blue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Blue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (descripcion.isNotBlank()) {
                        Text(
                            text = "📦 $categoria · $descripcion",
                            color = Dark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (pesoKg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "⚖ $pesoKg kg · $cantidadBultos paquete(s)",
                            color = Dark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (senderPhone.isNotBlank() && receiverPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "📞 Remitente: $senderPhone · Destinatario: $receiverPhone",
                            color = Dark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (fotoPaqueteUri != null) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "📷 Foto del paquete agregada",
                            color = Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDestinatarioPagaChange(!destinatarioPaga) },
            shape = RoundedCornerShape(16.dp),
            color = if (destinatarioPaga) SoftBlueBg else LightBg,
            border = BorderStroke(
                1.5.dp,
                if (destinatarioPaga) Blue else Border
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¿El destinatario paga?",
                        color = Dark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = if (destinatarioPaga)
                            "Sí, pagará al recibir el pedido."
                        else
                            "No, pagarás tú al confirmar.",
                        color = Muted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Switch(
                    checked = destinatarioPaga,
                    onCheckedChange = onDestinatarioPagaChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Blue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = DisabledText,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Método de pago",
            color = Dark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentButton(
                icon = R.drawable.ic_yape,
                metodo = "Yape",
                seleccionado = metodoPago == "Yape"
            ) {
                onMetodoPagoChange("Yape")
            }

            PaymentButton(
                icon = R.drawable.ic_plin,
                metodo = "Plin",
                seleccionado = metodoPago == "Plin"
            ) {
                onMetodoPagoChange("Plin")
            }

            PaymentButton(
                icon = R.drawable.ic_efectivo,
                metodo = "Efectivo",
                seleccionado = metodoPago == "Efectivo"
            ) {
                onMetodoPagoChange("Efectivo")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total estimado",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "S/ ${"%.2f".format(total)}",
                color = Dark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Paquetes",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = cantidadBultos.ifBlank { "1" },
                color = Dark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red,
                contentColor = Color.White,
                disabledContainerColor = DisabledBg,
                disabledContentColor = DisabledText
            )
        ) {
            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isSubmitting) "Confirmando..." else "Confirmar pedido",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun LightField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    )
}

private fun calcularPrecioVehiculo(
    vehiculo: String,
    distanciaKm: Double,
    tarifaMotorizado: String = "plana",
    pesoKg: Double? = null
): Double {
    return when (vehiculo) {
        "Motorizado" -> {
            if (tarifaMotorizado == "plana") {
                10.0
            } else {
                val kmExtra = (distanciaKm - 5.0).coerceAtLeast(0.0)
                10.0 + kmExtra
            }
        }

        "Van / Minivan" -> {
            if (distanciaKm <= 15.0) {
                35.0
            } else {
                35.0 + ((distanciaKm - 15.0) * 3.0)
            }
        }

        else -> 10.0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun textPart(value: String): RequestBody {
    return value.toRequestBody("text/plain".toMediaTypeOrNull())
}

private fun uriToFile(
    context: android.content.Context,
    uri: Uri
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("No se pudo abrir la imagen")

    val file = File(
        context.cacheDir,
        "foto_paquete_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use { output ->
        inputStream.copyTo(output)
    }

    inputStream.close()

    return file
}

private fun bitmapDescriptorFromDrawableSafe(
    context: android.content.Context,
    drawableId: Int,
    width: Int,
    height: Int
): BitmapDescriptor {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}

@Composable
private fun RowScope.PaymentButton(
    icon: Int,
    metodo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (seleccionado) Dark else LightBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = metodo,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun DestinatarioPagaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        color = if (checked) SoftBlueBg else LightBg,
        border = BorderStroke(
            width = 2.dp,
            color = if (checked) Blue else Border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¿El destinatario paga?",
                    color = Dark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (checked) {
                        "Sí, el destinatario pagará al recibir el pedido."
                    } else {
                        "No, pagará la persona que realiza el pedido."
                    },
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Blue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = DisabledText,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
@Composable
private fun ContactoPedidoPopup(
    senderPhone: String,
    receiverPhone: String,
    onSenderPhoneChange: (String) -> Unit,
    onReceiverPhoneChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DisabledText)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Datos de contacto",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Text(
                    text = "Ingresa los teléfonos para coordinar el recojo y la entrega.",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = senderPhone,
                    onValueChange = {
                        onSenderPhoneChange(
                            it.filter { c -> c.isDigit() }.take(9)
                        )
                    },
                    label = { Text("Teléfono del remitente") },
                    placeholder = { Text("Ej. 987654321") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Blue
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        cursorColor = Blue
                    )

                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = receiverPhone,
                    onValueChange = {
                        onReceiverPhoneChange(
                            it.filter { c -> c.isDigit() }.take(9)
                        )
                    },
                    label = { Text("Teléfono del destinatario") },
                    placeholder = { Text("Ej. 912345678") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Green
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF8F0))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Estos números se usarán solo para coordinar el servicio del pedido.",
                        color = Color(0xFF92400E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onContinue,
                    enabled = senderPhone.length == 9 && receiverPhone.length == 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledBg,
                        disabledContentColor = DisabledText
                    )
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        color = Red,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
@Composable
private fun FotoPaquetePopup(
    fotoPaqueteUri: Uri?,
    onSelectPhoto: () -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DisabledText)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Foto del paquete",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Text(
                    text = "Agrega una foto para que el repartidor identifique mejor el paquete.",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (fotoPaqueteUri != null) Color(0xFFEAFBF0)
                            else Color(0xFFF5F5F5)
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (fotoPaqueteUri != null) Green else Border,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { onSelectPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (fotoPaqueteUri != null) Green.copy(alpha = 0.12f)
                                    else Blue.copy(alpha = 0.10f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (fotoPaqueteUri != null)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = if (fotoPaqueteUri != null) Green else Blue,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (fotoPaqueteUri != null)
                                "Foto agregada correctamente"
                            else
                                "Agregar foto del paquete",
                            color = Dark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = if (fotoPaqueteUri != null)
                                "Puedes cambiarla si deseas"
                            else
                                "Toca aquí para seleccionar una imagen",
                            color = Muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF8F0))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "La foto ayuda a evitar confusiones durante el recojo y la entrega.",
                        color = Color(0xFF92400E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onContinue,
                    enabled = fotoPaqueteUri != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        disabledContainerColor = DisabledText
                    )
                ) {
                    Text(
                        text = "Continuar",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        color = Red,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
@Composable
private fun ComentarioRepartidorPopup(
    comentario: String,
    onComentarioChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DisabledText)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Comentario para el repartidor",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Text(
                    text = "Puedes agregar una indicación especial. Este paso es opcional.",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = comentario,
                    onValueChange = onComentarioChange,
                    placeholder = {
                        Text(
                            text = "Ej. Llamar al llegar, preguntar por recepción, tocar el timbre...",
                            color = Muted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Blue
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    singleLine = false,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Border,
                        focusedTextColor = Dark,
                        unfocusedTextColor = Dark,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftBlueBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Este mensaje ayudará al repartidor a coordinar mejor el recojo o la entrega.",
                        color = Color(0xFF123B8F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onFinish,
                    enabled = comentario.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledBg,
                        disabledContentColor = DisabledText
                    )
                ) {
                    Text(
                        text = "Finalizar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Omitir comentario",
                        color = Blue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        color = Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}