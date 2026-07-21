package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.Order
import com.example.peruchocourierapp.services.LocationForegroundService
import com.example.peruchocourierapp.utils.obtenerRutaCompleta
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.peruchocourierapp.models.DriverLocationResponse
import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.content.Context
import androidx.core.content.FileProvider
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.theme.ThemeManager
import com.google.android.gms.maps.model.MapStyleOptions
import okhttp3.MultipartBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody


private val DriverBlue = Color(0xFF1A4FBF)
private val DriverBg = Color(0xFFF4F6FB)
private val DriverText = Color(0xFF1A2340)
private val DriverMuted = Color(0xFF6B7A99)
private val DriverBorder = Color(0xFFE8ECF4)
private val DriverGreen = Color(0xFF22C55E)
private val DriverRed = Color(0xFFE02020)
private val LimaDefault = LatLng(-12.0464, -77.0428)


private data class PedidoCursoColors(
    val background: Color,
    val bottomSheet: Color,
    val card: Color,
    val infoBox: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val mapLoadingBg: Color,
    val mapButtonBg: Color,
    val disabledBg: Color,
    val disabledText: Color,
    val blueSoft: Color,
    val greenSoft: Color,
    val greenText: Color,
    val orangeSoft: Color,
    val orangeText: Color,
    val completedMessageBg: Color,
    val completedMessageText: Color,
    val statCardBg: Color,
    val dialogBg: Color

)

@Composable
private fun pedidoCursoColors(): PedidoCursoColors {
    val dark = ThemeManager.isDarkMode.value


    return if (dark) {
        PedidoCursoColors(
            background = Color(0xFF0F172A),
            bottomSheet = Color(0xFF111827),
            card = Color(0xFF111827),
            infoBox = Color(0xFF1F2937),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            border = Color(0xFF334155),
            mapLoadingBg = Color(0xFF0F172A),
            mapButtonBg = Color(0xFF111827).copy(alpha = 0.92f),
            disabledBg = Color(0xFF334155),
            disabledText = Color(0xFF94A3B8),
            blueSoft = Color(0xFF172554),
            greenSoft = Color(0xFF14532D),
            greenText = Color(0xFFDCFCE7),
            orangeSoft = Color(0xFF451A03),
            orangeText = Color(0xFFFBBF24),
            completedMessageBg = Color(0xFF052E1C),
            completedMessageText = Color(0xFFDCFCE7),
            statCardBg = Color(0xFF1F2937),
            dialogBg = Color(0xFF111827)
        )
    } else {
        PedidoCursoColors(
            background = DriverBg,
            bottomSheet = DriverBg,
            card = Color.White,
            infoBox = Color.White,
            text = DriverText,
            muted = DriverMuted,
            border = DriverBorder,
            mapLoadingBg = Color.White,
            mapButtonBg = Color.White.copy(alpha = 0.92f),
            disabledBg = Color(0xFFE5E7EB),
            disabledText = Color(0xFF9CA3AF),
            blueSoft = Color(0xFFE8EFFE),
            greenSoft = Color(0xFFD1FAE5),
            greenText = Color(0xFF065F46),
            orangeSoft = Color(0xFFFFF4E8),
            orangeText = Color(0xFFD97706),
            completedMessageBg = Color(0xFFF0FFF4),
            completedMessageText = Color(0xFF065F46),
            statCardBg = Color(0xFFF9FAFB),
            dialogBg = Color.White
        )
    }

}

private fun normalizarEstadoPedido(estado: String?): String {
    return when ((estado ?: "").trim().lowercase()) {
        "recogiendo" -> "recogido"
        "en_transito" -> "en_camino"
        else -> (estado ?: "").trim().lowercase()
    }
}

private fun interpolarLatLng(
    inicio: LatLng,
    fin: LatLng,
    fraccion: Float
): LatLng {
    val lat = inicio.latitude + (fin.latitude - inicio.latitude) * fraccion
    val lng = inicio.longitude + (fin.longitude - inicio.longitude) * fraccion
    return LatLng(lat, lng)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoEnCursoScreen(
    navController: NavController,
    driverEmailParam: String = ""
) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val colors = pedidoCursoColors()

    val driverEmail = remember(driverEmailParam) {
        driverEmailParam.ifBlank {
            sessionManager.getUserEmail()?.trim().orEmpty()
        }
    }

    var activeOrder by remember { mutableStateOf<Order?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var duracionMin by remember { mutableStateOf(0) }

    var showPedidoCompletadoDialog by remember { mutableStateOf(false) }
    var gananciaPedido by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var estadoPendiente by remember { mutableStateOf("") }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LimaDefault, 14f)
    }

    val driverMarkerState = rememberMarkerState(position = LimaDefault)
    var driverMarkerReady by remember { mutableStateOf(false) }
    var followDriver by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    /*
     * GPS local en vivo para que el repartidor vea su cursor/marcador
     * moverse fluido mientras conduce. Esto NO depende del backend.
     */
    DisposableEffect(
        hasLocationPermission,
        activeOrder?.id,
        activeOrder?.estado
    ) {
        val estado = normalizarEstadoPedido(activeOrder?.estado)

        if (
            !hasLocationPermission ||
            activeOrder?.id == null ||
            estado == "entregado"
        ) {
            onDispose { }
        } else {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            )
                .setMinUpdateIntervalMillis(500L)
                .setMinUpdateDistanceMeters(8f)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return

                    currentLat = location.latitude
                    currentLng = location.longitude
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                errorMessage = "Permiso de ubicación requerido"
            }

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
    /*
     * Fallback: solo consulta la última ubicación guardada en servidor
     * si todavía no tenemos GPS local. Para el panel del repartidor,
     * el movimiento fluido debe salir del GPS del celular, no del servidor.
     */
    LaunchedEffect(activeOrder?.id, activeOrder?.estado) {
        while (
            activeOrder?.id != null &&
            normalizarEstadoPedido(activeOrder?.estado) != "entregado" &&
            (currentLat == 0.0 || currentLng == 0.0)
        ) {
            val orderId = activeOrder?.id ?: break

            RetrofitClient.instance.getDriverLocation(orderId)
                .enqueue(object : Callback<DriverLocationResponse> {
                    override fun onResponse(
                        call: Call<DriverLocationResponse>,
                        response: Response<DriverLocationResponse>
                    ) {
                        val result = response.body()

                        if (response.isSuccessful && result?.success == true) {
                            val lat = result.driver_lat?.toDoubleOrNull()
                            val lng = result.driver_lng?.toDoubleOrNull()

                            if (lat != null && lng != null) {
                                currentLat = lat
                                currentLng = lng
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<DriverLocationResponse>,
                        t: Throwable
                    ) {}
                })

            delay(5000)
        }
    }

    fun iniciarServicioUbicacion(orderId: Int, tiempoEstimadoMin: Int) {
        if (orderId <= 0 || driverEmail.isBlank()) return

        val serviceIntent = Intent(context, LocationForegroundService::class.java).apply {
            putExtra("order_id", orderId)
            putExtra("driver_email", driverEmail)
            putExtra("tiempo_estimado_min", tiempoEstimadoMin)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun detenerServicioUbicacion() {
        context.stopService(
            Intent(context, LocationForegroundService::class.java)
        )
    }

    fun cargarPedidoActivo() {
        if (driverEmail.isBlank()) {
            isLoading = false
            activeOrder = null
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            return
        }

        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getActiveOrder(driverEmail)
            .enqueue(object : Callback<ActiveOrderResponse> {
                override fun onResponse(
                    call: Call<ActiveOrderResponse>,
                    response: Response<ActiveOrderResponse>
                ) {
                    isLoading = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true && result.order != null) {
                        activeOrder = result.order
                        errorMessage = ""
                    } else {
                        activeOrder = null
                        errorMessage = result?.message ?: "No hay pedido activo"
                    }
                }

                override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {
                    isLoading = false
                    activeOrder = null
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }
    var fotoEntregaFile by remember { mutableStateOf<File?>(null) }
    var fotoEntregaUri by remember { mutableStateOf<Uri?>(null) }

    fun crearArchivoFotoEntrega(): Pair<File, Uri> {
        val file = File(
            context.cacheDir,
            "entrega_${System.currentTimeMillis()}.jpg"
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Pair(file, uri)
    }

    fun subirFotoYEntregar(file: File) {
        val orderId = activeOrder?.id ?: return

        isUpdating = true

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        val fotoPart = MultipartBody.Part.createFormData(
            "foto_entrega",
            file.name,
            requestFile
        )

        RetrofitClient.instance.uploadDeliveryPhoto(
            orderId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            driverEmail.toRequestBody("text/plain".toMediaTypeOrNull()),
            fotoPart
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                isUpdating = false

                val result = response.body()

                if (response.isSuccessful && result?.success == true) {
                    detenerServicioUbicacion()
                    gananciaPedido = activeOrder?.total ?: "0.00"
                    showPedidoCompletadoDialog = true
                } else {
                    errorMessage = result?.message ?: "No se pudo subir la foto de entrega"
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                isUpdating = false
                errorMessage = "Error subiendo foto: ${t.message}"
            }
        })
    }

    val cameraEntregaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = fotoEntregaFile

        if (success && file != null && file.exists() && file.length() > 0L) {
            subirFotoYEntregar(file)
        } else {
            fotoEntregaFile = null
            fotoEntregaUri = null
            errorMessage = "Debes tomar una foto para completar la entrega"
        }
    }

    fun abrirCamaraEntrega() {
        val orderId = activeOrder?.id

        if (orderId == null || orderId <= 0) {
            errorMessage = "Pedido inválido"
            return
        }

        if (driverEmail.isBlank()) {
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            return
        }

        if (isUpdating) return

        val (file, uri) = crearArchivoFotoEntrega()
        fotoEntregaFile = file
        fotoEntregaUri = uri
        cameraEntregaLauncher.launch(uri)
    }

    fun actualizarEstado(nuevoEstado: String) {
        val orderId = activeOrder?.id ?: return

        if (driverEmail.isBlank()) {
            errorMessage = "Sesión inválida. Cierra sesión e inicia nuevamente."
            return
        }

        if (isUpdating) return

        isUpdating = true

        RetrofitClient.instance.updateOrderStatus(
            orderId = orderId,
            driverEmail = driverEmail,
            estado = nuevoEstado
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                isUpdating = false
                val result = response.body()

                if (response.isSuccessful && result?.success == true) {
                    errorMessage = ""

                    if (nuevoEstado == "entregado") {
                        detenerServicioUbicacion()

                        gananciaPedido = activeOrder?.total ?: "0.00"
                        showPedidoCompletadoDialog = true
                    } else {
                        cargarPedidoActivo()
                    }
                } else {
                    errorMessage = result?.message ?: "No se pudo actualizar el estado"
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                isUpdating = false
                errorMessage = "Error de conexión: ${t.message}"
            }
        })
    }

    fun obtenerUbicacionInicialMapa() {
        if (!hasLocationPermission) return

        try {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(locationRequest, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude
                    }
                }
        } catch (e: SecurityException) {
            errorMessage = "Permiso de ubicación requerido"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        cargarPedidoActivo()
        obtenerUbicacionInicialMapa()
    }

    LaunchedEffect(
        activeOrder?.id,
        activeOrder?.estado,
        driverEmail,
        hasLocationPermission,
        hasNotificationPermission,
        duracionMin
    ) {
        val order = activeOrder
        val estado = normalizarEstadoPedido(order?.estado)

        if (
            order?.id != null &&
            driverEmail.isNotBlank() &&
            hasLocationPermission &&
            hasNotificationPermission &&
            estado != "entregado"
        ) {
            iniciarServicioUbicacion(
                order.id,
                if (duracionMin > 0) duracionMin else 9999
            )
        }

        if (estado == "entregado" || order == null) {
            detenerServicioUbicacion()
        }
    }

    /*
     * Recalcula la ruta cada cierto tiempo, no en cada frame de GPS.
     * Si recalculas en cada movimiento del repartidor, el mapa puede sentirse pesado.
     */
    LaunchedEffect(activeOrder?.id, activeOrder?.estado) {
        while (
            activeOrder?.id != null &&
            normalizarEstadoPedido(activeOrder?.estado) != "entregado"
        ) {
            val estado = normalizarEstadoPedido(activeOrder?.estado)

            val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
            val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
            val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
            val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

            val origin = if (currentLat != 0.0 && currentLng != 0.0) {
                "$currentLat,$currentLng"
            } else {
                null
            }

            val destination = when (estado) {
                "asignado" -> {
                    if (pickupLat != null && pickupLng != null) "$pickupLat,$pickupLng" else null
                }

                "recogido", "en_camino" -> {
                    if (dropLat != null && dropLng != null) "$dropLat,$dropLng" else null
                }

                else -> null
            }

            if (origin != null && destination != null) {
                val resultado = withContext(Dispatchers.IO) {
                    obtenerRutaCompleta(
                        origin = origin,
                        destination = destination
                    )
                }

                ruta = resultado.puntos
                duracionMin = resultado.duracionMin
            } else {
                ruta = emptyList()
                duracionMin = 0
            }

            delay(15000)
        }
    }

    var centeredOnce by remember { mutableStateOf(false) }

    /*
     * Animación del marcador del repartidor.
     * En vez de saltar de un punto a otro, interpola varios puntos
     * para que parezca que el cursor está manejando en vivo.
     */
    LaunchedEffect(currentLat, currentLng) {
        if (currentLat == 0.0 || currentLng == 0.0) return@LaunchedEffect

        val nuevaPosicion = LatLng(currentLat, currentLng)

        if (!driverMarkerReady) {
            driverMarkerReady = true
            driverMarkerState.position = nuevaPosicion

            if (!centeredOnce) {
                centeredOnce = true
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(nuevaPosicion, 17f)
                )
            }

            return@LaunchedEffect
        }

        val posicionInicial = driverMarkerState.position

        val distanciaMetros = FloatArray(1)
        android.location.Location.distanceBetween(
            posicionInicial.latitude,
            posicionInicial.longitude,
            nuevaPosicion.latitude,
            nuevaPosicion.longitude,
            distanciaMetros
        )

        if (distanciaMetros[0] < 8f) {
            return@LaunchedEffect
        }

        val frames = 30
        val duracionMs = 700L
        val frameDelay = duracionMs / frames

        repeat(frames) { index ->
            val fraccion = (index + 1).toFloat() / frames.toFloat()

            driverMarkerState.position = interpolarLatLng(
                inicio = posicionInicial,
                fin = nuevaPosicion,
                fraccion = fraccion
            )

            delay(frameDelay)
        }

        driverMarkerState.position = nuevaPosicion
    }

    val estadoActual = normalizarEstadoPedido(activeOrder?.estado)

    val pickupLat = activeOrder?.pickup_lat?.toDoubleOrNull()
    val pickupLng = activeOrder?.pickup_lng?.toDoubleOrNull()
    val dropLat = activeOrder?.dropoff_lat?.toDoubleOrNull()
    val dropLng = activeOrder?.dropoff_lng?.toDoubleOrNull()

    val pickupPoint =
        if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null

    val dropPoint =
        if (dropLat != null && dropLng != null) LatLng(dropLat, dropLng) else null

    LaunchedEffect(activeOrder?.id, pickupPoint, dropPoint, currentLat, currentLng) {
        if (currentLat == 0.0 && currentLng == 0.0) {
            val target = when {
                pickupPoint != null && dropPoint != null -> LatLng(
                    (pickupPoint.latitude + dropPoint.latitude) / 2,
                    (pickupPoint.longitude + dropPoint.longitude) / 2
                )
                pickupPoint != null -> pickupPoint
                dropPoint != null -> dropPoint
                else -> null
            }

            if (target != null) {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(target, 13.5f)
                )
            }
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 145.dp,
        sheetContainerColor = colors.bottomSheet,
        sheetShadowElevation = 18.dp,
        sheetShape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        ),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 145.dp, max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = DriverBlue,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Pedido en curso",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DriverBlue)
                        }
                    }

                    errorMessage.isNotEmpty() -> {
                        Text(
                            text = errorMessage,
                            color = DriverRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    activeOrder != null -> {
                        PedidoEnCursoContenidoRedisenado(
                            navController = navController,
                            activeOrder = activeOrder!!,
                            isUpdating = isUpdating,
                            duracionMin = duracionMin,
                            onVerDetalles = { showDetailsDialog = true },
                            onConfirmarEstado = { estado ->
                                estadoPendiente = estado
                                showConfirmDialog = true
                            },
                            onMarcarEntregado = {
                                abrirCamaraEntrega()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val puedeMostrarMapa = currentLat != 0.0 && currentLng != 0.0 || pickupPoint != null || dropPoint != null

            if (!puedeMostrarMapa) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.mapLoadingBg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DriverBlue)
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    if (pickupPoint != null && estadoActual == "asignado") {
                        Marker(
                            state = MarkerState(pickupPoint),
                            title = "Recojo",
                            icon = bitmapDescriptorFromDrawableSafe(
                                context,
                                R.drawable.ic_pin_recojo,
                                100,
                                100
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
                                    com.example.peruchocourierapp.R.raw.map_style_dark
                                )
                            )

                        } else {

                            map.setMapStyle(null)

                        }
                    }

                    if (dropPoint != null && estadoActual != "entregado") {
                        Marker(
                            state = MarkerState(dropPoint),
                            title = "Entrega",
                            icon = bitmapDescriptorFromDrawableSafe(
                                context,
                                R.drawable.ic_pin_entrega,
                                100,
                                100
                            )
                        )
                    }

                    if (driverMarkerReady) {
                        Marker(
                            state = driverMarkerState,
                            title = "Repartidor"
                        )
                    }

                    if (ruta.isNotEmpty()) {
                        Polyline(
                            points = ruta,
                            color = Color(0xFF00B7FF),
                            width = 10f
                        )
                    }
                }

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.mapButtonBg)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = DriverBlue
                    )
                }

                IconButton(
                    onClick = {
                        followDriver = true

                        if (currentLat != 0.0 && currentLng != 0.0) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(currentLat, currentLng),
                                    17f
                                )
                            )
                        } else {
                            obtenerUbicacionInicialMapa()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(colors.mapButtonBg)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = DriverBlue
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar cambio") },
            text = {
                Text("¿Seguro que deseas marcar este pedido como ${estadoPendiente.replace("_", " ")}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        actualizarEstado(estadoPendiente)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Sí, confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDetailsDialog && activeOrder != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Detalles del pedido") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetalleTexto("Recojo", activeOrder!!.pickup_address ?: "-")
                    DetalleTexto("Entrega", activeOrder!!.dropoff_address ?: "-")
                    DetalleTexto("Descripción", activeOrder!!.descripcion ?: "-")
                    DetalleTexto("Categoría", activeOrder!!.categoria ?: "-")
                    DetalleTexto("Distancia", "${activeOrder!!.distancia_km ?: "-"} km")
                    DetalleTexto("Método de pago", activeOrder!!.metodo_pago ?: "-")
                    DetalleTexto("Total", "S/ ${activeOrder!!.total ?: "-"}")
                    DetalleTexto("Comentarios repartidor", activeOrder!!.comentarios_repartidor ?: "-")
                    val fotoUrl = obtenerUrlFotoPaquete(activeOrder!!.foto_paquete)

                    if (fotoUrl != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Foto del paquete",
                            fontSize = 12.sp,
                            color = colors.muted,
                            fontWeight = FontWeight.Bold
                        )

                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto del paquete",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    DetalleTexto("Teléfono remitente", activeOrder!!.telefono_remitente ?: "-")
                    DetalleTexto("Teléfono destinatario", activeOrder!!.telefono_destinatario ?: "-")
                    DetalleTexto("Estado", normalizarEstadoPedido(activeOrder!!.estado).replace("_", " "))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverBlue)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showPedidoCompletadoDialog) {

        Dialog(
            onDismissRequest = {
                showPedidoCompletadoDialog = false
            }
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.dialogBg
                )
            ) {

                Column {

                    // HEADER VERDE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF059669),
                                        Color(0xFF16A34A),
                                        Color(0xFF22C55E)
                                    )
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.20f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "✓",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "¡Pedido completado! 🎉",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                "Buen trabajo",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            StatCard(
                                value = "S/ ${gananciaPedido.ifBlank { "0.00" }}",
                                label = "Ganado",
                                valueColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                value = activeOrder?.distancia_km ?: "--",
                                label = "Distancia",
                                valueColor = colors.text,
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                value = "${duracionMin} min",
                                label = "Tiempo",
                                valueColor = colors.text,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = colors.completedMessageBg
                            )
                        ) {
                            Text(
                                text = "Excelente trabajo. El pedido fue entregado correctamente.",
                                modifier = Modifier.padding(14.dp),
                                color = colors.completedMessageText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showPedidoCompletadoDialog = false
                                navController.navigate("mis_entregas")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            Text("Ver mis ganancias")
                        }

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                showPedidoCompletadoDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoEnCursoContenidoRedisenado(
    navController: NavController,
    activeOrder: Order,
    isUpdating: Boolean,
    duracionMin: Int,
    onVerDetalles: () -> Unit,
    onConfirmarEstado: (String) -> Unit,
    onMarcarEntregado: () -> Unit
) {
    val colors = pedidoCursoColors()
    val estado = normalizarEstadoPedido(activeOrder.estado)

    RouteLinePedido(
        pickup = activeOrder.pickup_address ?: "-",
        dropoff = activeOrder.dropoff_address ?: "-"
    )

    InfoBoxPedido(
        metodoPago = activeOrder.metodo_pago ?: "-",
        total = activeOrder.total ?: "-",
        distanciaKm = activeOrder.distancia_km ?: "-"
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (duracionMin > 0) "Tiempo estimado: $duracionMin min" else "Tiempo estimado: calculando...",
        color = DriverBlue,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalles() }
    ) {
        Text(
            text = "DETALLES DE LA SOLICITUD",
            color = colors.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.infoBox, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = activeOrder.descripcion ?: "Sin detalles",
                color = colors.muted,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Button(
        onClick = {
            val clienteEmail = activeOrder.user_email ?: return@Button
            val emailEncoded = Uri.encode(clienteEmail)

            navController.navigate(
                "chat_pedido/${activeOrder.id}/$emailEncoded"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DriverBlue
        )
    ) {
        Text(
            text = "💬 Chat con cliente",
            fontWeight = FontWeight.Bold
        )
    }

    EstadoBadgePedido(estado)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onConfirmarEstado("recogido") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "asignado",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DriverBlue,
                contentColor = Color.White,
                disabledContainerColor = colors.disabledBg,
                disabledContentColor = colors.disabledText
            )
        ) {
            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar recogido",
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = { onConfirmarEstado("en_camino") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "recogido",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DriverBlue,
                contentColor = Color.White,
                disabledContainerColor = colors.disabledBg,
                disabledContentColor = colors.disabledText
            )
        ) {
            Icon(Icons.Default.PedalBike, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar en camino",
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = onMarcarEntregado,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isUpdating && estado == "en_camino",
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                disabledContainerColor = colors.disabledBg,
                disabledContentColor = colors.disabledText
            )
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isUpdating) "Actualizando..." else "Marcar entregado",
                fontWeight = FontWeight.Black
            )
        }

    }
}


@Composable
private fun RouteLinePedido(
    pickup: String,
    dropoff: String
) {
    val colors = pedidoCursoColors()
    Column {
        RouteRowPedido(DriverGreen, pickup)

        Box(
            modifier = Modifier
                .padding(start = 10.dp, top = 3.dp, bottom = 3.dp)
                .width(1.5.dp)
                .height(14.dp)
                .background(colors.border)
        )

        RouteRowPedido(DriverRed, dropoff)
    }
}

@Composable
private fun RouteRowPedido(
    color: Color,
    text: String
) {
    val colors = pedidoCursoColors()
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (color == DriverGreen) {
                Icons.Default.LocationOn
            } else {
                Icons.Default.Place
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoBoxPedido(
    metodoPago: String,
    total: String,
    distanciaKm: String
) {
    val colors = pedidoCursoColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.infoBox, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRowPedido("Método de pago", metodoPago, DriverBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )

        InfoRowPedido("Distancia", "$distanciaKm km", DriverBlue)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )

        InfoRowPedido("Recargo total", "S/ $total", colors.text)
    }
}

@Composable
private fun InfoRowPedido(
    label: String,
    value: String,
    valueColor: Color
) {
    val colors = pedidoCursoColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EstadoBadgePedido(estado: String) {
    val colors = pedidoCursoColors()
    val estadoLower = normalizarEstadoPedido(estado)

    val badgeColor = when (estadoLower) {
        "asignado" -> colors.greenSoft
        "recogido" -> colors.orangeSoft
        "en_camino" -> colors.blueSoft
        "entregado" -> colors.greenSoft
        else -> colors.greenSoft
    }

    val textColor = when (estadoLower) {
        "asignado" -> colors.greenText
        "recogido" -> colors.orangeText
        "en_camino" -> DriverBlue
        "entregado" -> Color(0xFF059669)
        else -> colors.greenText
    }

    val texto = when (estadoLower) {
        "en_camino" -> "En camino"
        else -> estadoLower.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "ESTADO:",
            color = colors.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(badgeColor)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = texto,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun DetalleTexto(
    titulo: String,
    valor: String
) {
    val colors = pedidoCursoColors()
    Column {
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = colors.muted,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = valor,
            fontSize = 14.sp,
            color = colors.text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun obtenerUrlFotoPaquete(foto: String?): String? {
    if (foto.isNullOrBlank()) return null

    return when {
        foto.startsWith("http") ->
            foto

        foto.startsWith("uploads/") ->
            "https://peruchocourier.com/perucho_api/$foto"

        else ->
            "https://peruchocourier.com/perucho_api/uploads/paquetes/$foto"
    }
}
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val colors = pedidoCursoColors()

    val finalValueColor = valueColor ?: colors.text

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.statCardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                color = finalValueColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = label,
                color = colors.muted,
                fontSize = 11.sp
            )
        }
    }
}
private fun bitmapDescriptorFromDrawableSafe(
    context: Context,
    drawableId: Int,
    width: Int,
    height: Int
): BitmapDescriptor {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}