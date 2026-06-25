package com.example.peruchocourierapp.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import android.location.Location
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.utils.obtenerRutaCompleta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
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

private val Dark = Color(0xFF1A1A1A)
private val Red = Color(0xFFE02020)
private val Green = Color(0xFF22C55E)
private val Muted = Color(0xFF888888)
private val LightBg = Color(0xFFF5F5F5)
private val Border = Color(0xFFF0F0F0)
private val Blue = Color(0xFF1A4FBF)

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
    var packageSize by remember { mutableStateOf("Pequeño - hasta 5 kg") }
    var fotoPaqueteUri by remember { mutableStateOf<Uri?>(null) }

    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distanciaKm by remember { mutableDoubleStateOf(0.0) }
    var duracionMin by remember { mutableStateOf(0) }

    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var showMotorizadoTarifasPopup by remember { mutableStateOf(false) }
    var tarifaMotorizado by remember { mutableStateOf("plana") }
    var destinatarioPaga by remember { mutableStateOf(false) }

    val pesoDouble = pesoKg.replace(",", ".").toDoubleOrNull()

    LaunchedEffect(pesoDouble, selectedVehicle) {

        if (selectedVehicle == "Motorizado" && pesoDouble != null) {

            tarifaMotorizado =
                if (pesoDouble > 2.5) {
                    "estandar"
                } else {
                    "plana"
                }

            errorMessage =
                if (pesoDouble > 2.5) {
                    "Se cambió automáticamente a Tarifa Estándar porque supera los 2.5 kg"
                } else {
                    ""
                }
        }

        if (
            selectedVehicle == "Van / Minivan" &&
            pesoDouble != null &&
            pesoDouble > 100
        ) {
            errorMessage = "Van / Minivan solo permite paquetes hasta 100 kg"
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
            .background(Color.White)
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
                color = Color(0xFF999999),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

            val bloqueaVan = pesoDouble != null && pesoDouble > 100.0

            VehicleCard(
                icon = R.drawable.motorizado,
                name = if (tarifaMotorizado == "plana") {
                    "Motorizado Tarifa Plana"
                } else {
                    "Motorizado Tarifa Estándar"
                },
                desc = "Paquetes pequeños hasta 2.5 kg o un poco más. Rápido en tráfico.",
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
                desc = "Hasta 100 kg. Mudanzas pequeñas o carga voluminosa.",
                price = calcularPrecioVehiculo("Van / Minivan", distanciaKm),
                selected = selectedVehicle == "Van / Minivan" && !bloqueaVan,
                enabled = !bloqueaVan,
                disabledReason = "No disponible: supera los 100 kg permitidos.",
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
                onDetailsClick = { showDetailsSheet = true },
                onConfirm = {
                    val pesoValidado = pesoKg.replace(",", ".").toDoubleOrNull()
                    val bultosValidados = cantidadBultos.toIntOrNull()

                    when {
                        pickupAddress.isBlank() || dropoffAddress.isBlank() -> {
                            errorMessage = "Selecciona punto de recojo y entrega"
                        }

                        pickupLat == 0.0 || pickupLng == 0.0 || dropoffLat == 0.0 || dropoffLng == 0.0 -> {
                            errorMessage = "Selecciona ubicaciones válidas"
                        }

                        pesoValidado == null || pesoValidado <= 0.0 -> {
                            errorMessage = "Ingresa el peso aproximado del paquete"
                            showDetailsSheet = true
                        }

                        selectedVehicle == "Motorizado" && tarifaMotorizado == "plana" && pesoValidado > 2.5 -> {
                            tarifaMotorizado = "estandar"
                            errorMessage = "Se cambió automáticamente a Tarifa Estándar porque supera los 2.5 kg"
                        }

                        selectedVehicle == "Motorizado" && pesoValidado > 5.0 -> {
                            errorMessage = "El motorizado solo admite hasta 5 kg. Selecciona Van / Minivan."
                            showDetailsSheet = true
                        }

                        selectedVehicle == "Van / Minivan" && pesoValidado > 100.0 -> {
                            errorMessage = "Van / Minivan solo permite paquetes hasta 100 kg"
                            showDetailsSheet = true
                        }

                        descripcion.isBlank() -> {
                            errorMessage = "Agrega los detalles del pedido"
                            showDetailsSheet = true
                        }

                        fotoPaqueteUri == null -> {
                            errorMessage = "Agrega una foto del paquete"
                            showDetailsSheet = true
                        }

                        bultosValidados == null || bultosValidados <= 0 -> {
                            errorMessage = "Ingresa la cantidad de cajas o bultos"
                            showDetailsSheet = true
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
                                    tamanoPaquete = textPart(packageSize),
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

    if (showDetailsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailsSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Detalles de la solicitud",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Dark
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategorySelector(
                    selected = itemCategory,
                    onSelect = { itemCategory = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PackageSizeSelector(
                    selected = packageSize,
                    onSelect = { packageSize = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = pesoKg,
                    onValueChange = { pesoKg = it },
                    label = "Peso aproximado del paquete (kg)",
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = cantidadBultos,
                    onValueChange = { cantidadBultos = it.filter { c -> c.isDigit() } },
                    label = "Cantidad de cajas o bultos",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Dark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (fotoPaqueteUri == null) {
                            "Agregar foto del paquete"
                        } else {
                            "Cambiar foto del paquete"
                        }
                    )
                }

                if (fotoPaqueteUri != null) {
                    Text(
                        text = "Foto seleccionada correctamente",
                        color = Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = senderPhone,
                    onValueChange = { senderPhone = it },
                    label = "Teléfono del remitente",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = receiverPhone,
                    onValueChange = { receiverPhone = it },
                    label = "Teléfono del destinatario",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción del pedido",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                LightField(
                    value = comentarioRepartidor,
                    onValueChange = { comentarioRepartidor = it },
                    label = "Comentario para el repartidor",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                DestinatarioPagaSwitch(
                    checked = destinatarioPaga,
                    onCheckedChange = { destinatarioPaga = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Método de pago",
                    color = Dark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )


                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PaymentButton(
                        icon = R.drawable.ic_yape,
                        metodo = "Yape",
                        seleccionado = metodoPago == "Yape"
                    ) {
                        metodoPago = "Yape"
                    }

                    PaymentButton(
                        icon = R.drawable.ic_plin,
                        metodo = "Plin",
                        seleccionado = metodoPago == "Plin"
                    ) {
                        metodoPago = "Plin"
                    }

                    PaymentButton(
                        icon = R.drawable.ic_efectivo,
                        metodo = "Efectivo",
                        seleccionado = metodoPago == "Efectivo"
                    ) {
                        metodoPago = "Efectivo"
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showDetailsSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Dark),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Guardar detalles", fontSize = 17.sp)
                }
            }
        }
    }

    if (showMotorizadoTarifasPopup) {
        MotorizadoTarifasPopup(
            selectedTarifa = tarifaMotorizado,
            onSelectTarifa = { tarifaMotorizado = it },
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
                    !enabled -> Color(0xFFF3F3F3)
                    selected -> Color(0xFFEAF2FF)
                    else -> Color.White
                }
            )
            .border(
                width = if (selected && enabled) 2.dp else 1.dp,
                color = when {
                    !enabled -> Color(0xFFE0E0E0)
                    selected -> Blue
                    else -> Color(0xFFF0F0F0)
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
                        "S/10 fijo hasta 2.5 kg"
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
                    color = Color(0xFF999999),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun MotorizadoTarifasPopup(
    selectedTarifa: String,
    onSelectTarifa: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Dark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Entendido",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        },
        title = {
            Text(
                text = "Tarifas del Motorizado 🏍️",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Dark
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Elige la tarifa que más te conviene",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                MotorizadoTarifaCard(
                    title = "Motorizado Tarifa Plana",
                    tag = "PRECIO FIJO",
                    price = "S/ 10",
                    subtitle = "precio único",
                    background = Color(0xFFF0FFF4),
                    border = Green,
                    tagColor = Green,
                    selected = selectedTarifa == "plana",
                    onClick = {
                        onSelectTarifa("plana")
                        onDismiss()
                    },
                    items = listOf(
                        "Ideal para paquetes pequeños y livianos",
                        "Hasta 2.5 kg: precio fijo de S/10",
                        "Precio no aumenta por distancia mientras no supere 2.5 kg"
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                MotorizadoTarifaCard(
                    title = "Motorizado Tarifa Estándar",
                    tag = "MÁS CAPACIDAD",
                    price = "S/ 10",
                    subtitle = "+ S/1 por km",
                    background = Color(0xFFF0F4FF),
                    border = Blue,
                    tagColor = Blue,
                    selected = selectedTarifa == "estandar",
                    onClick = {
                        onSelectTarifa("estandar")
                        onDismiss()
                    },
                    items = listOf(
                        "Para paquetes mayores a 2.5 kg hasta 5 kg",
                        "Precio base S/10",
                        "+ S/1 por km extra después de los 15 km",
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFFFFF8F0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
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
                            text = "La tarifa se calcula automáticamente al seleccionar origen y destino.",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 15.sp
                        )
                    }
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
    background: Color,
    border: Color,
    tagColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    items: List<String>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (selected) background else Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (selected) 8.dp else 2.dp,
        border = BorderStroke(
            width = if (selected) 3.dp else 2.dp,
            color = border
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (selected) border else Color.White)
                        .border(2.dp, border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Dark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(tagColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = price,
                        color = Dark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = subtitle,
                        color = Muted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = border,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = item,
                        color = Color(0xFF444444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp
                    )
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
    onDetailsClick: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDetailsClick() }
                .clip(RoundedCornerShape(14.dp))
                .background(LightBg)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detalles de la solicitud",
                color = Muted,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(Icons.Default.KeyboardArrowRight, null, tint = Muted)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total estimado", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("S/ ${"%.2f".format(total)}", color = Dark, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cajas / bultos",
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

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Dark)
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
            val kmExtra = if (distanciaKm > 15.0) distanciaKm - 15.0 else 0.0

            if (tarifaMotorizado == "plana") {
                if ((pesoKg ?: 0.0) <= 2.5) {
                    10.0
                } else {
                    10.0 + kmExtra
                }
            } else {
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
@Composable
private fun CategorySelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val categorias = listOf(
        "Documentos",
        "Ropa",
        "Calzado",
        "Accesorios",
        "Electrónicos",
        "Cosméticos",
        "Medicinas",
        "Alimentos",
        "Otro"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría del pedido") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        onSelect(categoria)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageSizeSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val sizes = listOf(
        "Pequeño - hasta 5 kg",
        "Mediano - 5 a 20 kg",
        "Grande - 20 a 100 kg"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tamaño del paquete") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sizes.forEach { size ->
                DropdownMenuItem(
                    text = { Text(size) },
                    onClick = {
                        onSelect(size)
                        expanded = false
                    }
                )
            }
        }
    }
}

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
        color = if (checked) Color(0xFFEAF2FF) else LightBg,
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
                    uncheckedTrackColor = Color(0xFFBDBDBD),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}