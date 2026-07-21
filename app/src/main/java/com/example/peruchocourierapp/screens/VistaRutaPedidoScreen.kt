package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val PreviewBlue = Color(0xFF1A4FBF)
private val PreviewGreen = Color(0xFF22C55E)
private val PreviewRed = Color(0xFFE02020)

private data class RoutePreviewColors(
    val sheetBg: Color,
    val cardBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val handle: Color,
    val floatingBg: Color,
    val floatingIcon: Color,
    val disabledBg: Color,
    val disabledText: Color
)

@Composable
private fun routePreviewColors(): RoutePreviewColors {
    return if (ThemeManager.isDarkMode.value) {
        RoutePreviewColors(
            sheetBg = Color(0xFF111827),
            cardBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            handle = Color(0xFF475569),
            floatingBg = Color(0xFF111827).copy(alpha = 0.96f),
            floatingIcon = Color(0xFFF8FAFC),
            disabledBg = Color(0xFF475569),
            disabledText = Color(0xFFCBD5E1)
        )
    } else {
        RoutePreviewColors(
            sheetBg = Color.White,
            cardBg = Color(0xFFF7F7F7),
            border = Color(0xFFE5E7EB),
            text = Color(0xFF111A33),
            muted = Color(0xFF6B7590),
            handle = Color(0xFFE0E0E0),
            floatingBg = Color.White.copy(alpha = 0.96f),
            floatingIcon = Color(0xFF111A33),
            disabledBg = Color(0xFFBDBDBD),
            disabledText = Color.White
        )
    }
}

@Composable
fun VistaRutaPedidoScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = routePreviewColors()
    val sessionManager = remember { SessionManager(context) }

    val sourceEntry = navController.previousBackStackEntry
    val stateHandle = sourceEntry?.savedStateHandle

    val orderId = stateHandle?.get<Int>("preview_order_id") ?: 0
    val total = stateHandle?.get<String>("preview_total") ?: "0.00"
    val distance = stateHandle?.get<String>("preview_distance") ?: "0"
    val pickupAddress = stateHandle?.get<String>("preview_pickup_address").orEmpty()
    val dropoffAddress = stateHandle?.get<String>("preview_dropoff_address").orEmpty()

    val pickupLat = stateHandle?.get<Double?>("preview_pickup_lat")
    val pickupLng = stateHandle?.get<Double?>("preview_pickup_lng")
    val dropoffLat = stateHandle?.get<Double?>("preview_dropoff_lat")
    val dropoffLng = stateHandle?.get<Double?>("preview_dropoff_lng")

    val pickupPoint = if (
        pickupLat != null && pickupLng != null &&
        pickupLat != 0.0 && pickupLng != 0.0
    ) {
        LatLng(pickupLat, pickupLng)
    } else {
        null
    }

    val dropoffPoint = if (
        dropoffLat != null && dropoffLng != null &&
        dropoffLat != 0.0 && dropoffLng != 0.0
    ) {
        LatLng(dropoffLat, dropoffLng)
    } else {
        null
    }

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

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var driverPoint by remember { mutableStateOf<LatLng?>(null) }
    var routeToPickup by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeToDropoff by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var driverToPickupKm by remember { mutableStateOf<Double?>(null) }
    var driverToPickupMin by remember { mutableStateOf<Int?>(null) }
    var pickupToDropoffMin by remember { mutableStateOf<Int?>(null) }
    var isLoadingRoute by remember { mutableStateOf(true) }
    var routeError by remember { mutableStateOf("") }
    var isAccepting by remember { mutableStateOf(false) }
    var acceptError by remember { mutableStateOf("") }

    val initialPoint = pickupPoint ?: dropoffPoint ?: LatLng(-12.0464, -77.0428)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPoint, 13f)
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
    }

    LaunchedEffect(hasLocationPermission, pickupPoint, dropoffPoint) {
        if (pickupPoint == null || dropoffPoint == null) {
            isLoadingRoute = false
            routeError = "El pedido no tiene coordenadas válidas para mostrar la ruta."
            return@LaunchedEffect
        }

        isLoadingRoute = true
        routeError = ""

        try {
            if (hasLocationPermission) {
                val lastLocation = fusedLocationClient.lastLocation.await()
                driverPoint = lastLocation?.let {
                    LatLng(it.latitude, it.longitude)
                }
            }

            val secondLeg = withContext(Dispatchers.IO) {
                obtenerRutaCompleta(
                    origin = "${pickupPoint.latitude},${pickupPoint.longitude}",
                    destination = "${dropoffPoint.latitude},${dropoffPoint.longitude}"
                )
            }

            routeToDropoff = secondLeg.puntos
            pickupToDropoffMin = secondLeg.duracionMin

            driverPoint?.let { driver ->
                val firstLeg = withContext(Dispatchers.IO) {
                    obtenerRutaCompleta(
                        origin = "${driver.latitude},${driver.longitude}",
                        destination = "${pickupPoint.latitude},${pickupPoint.longitude}"
                    )
                }

                routeToPickup = firstLeg.puntos
                driverToPickupKm = firstLeg.distanciaKm
                driverToPickupMin = firstLeg.duracionMin
            }

            val boundsBuilder = LatLngBounds.Builder()
            listOfNotNull(driverPoint, pickupPoint, dropoffPoint).forEach {
                boundsBuilder.include(it)
            }

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    110
                )
            )
        } catch (e: Exception) {
            routeError = "No se pudo calcular la ruta: ${e.message ?: "error desconocido"}"
        } finally {
            isLoadingRoute = false
        }
    }

    fun aceptarPedido() {
        val driverEmail = sessionManager.getUserEmail()?.trim().orEmpty()

        if (orderId <= 0) {
            acceptError = "Pedido inválido"
            return
        }

        if (driverEmail.isBlank()) {
            acceptError = "No se encontró la sesión del repartidor"
            return
        }

        isAccepting = true
        acceptError = ""

        RetrofitClient.instance.acceptOrder(orderId, driverEmail)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    isAccepting = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        navController.navigate(
                            "pedido_en_curso/${Uri.encode(driverEmail)}"
                        ) {
                            popUpTo("pedidos_disponibles") {
                                inclusive = false
                            }
                        }
                    } else {
                        acceptError = result?.message ?: "No se pudo aceptar el pedido"
                    }
                }

                override fun onFailure(
                    call: Call<BasicResponse>,
                    t: Throwable
                ) {
                    isAccepting = false
                    acceptError = "Error de conexión: ${t.message}"
                }
            })
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true
            )
        ) {
            MapEffect(ThemeManager.isDarkMode.value) { map ->
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

            driverPoint?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Tu ubicación",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }

            pickupPoint?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Punto de recojo",
                    icon = bitmapDescriptorFromDrawableRoute(
                        context,
                        R.drawable.ic_pin_recojo,
                        100,
                        100
                    ),
                    anchor = Offset(0.5f, 1f)
                )
            }

            dropoffPoint?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Punto de entrega",
                    icon = bitmapDescriptorFromDrawableRoute(
                        context,
                        R.drawable.ic_pin_entrega,
                        100,
                        100
                    ),
                    anchor = Offset(0.5f, 1f)
                )
            }

            if (routeToPickup.size >= 2) {
                Polyline(
                    points = routeToPickup,
                    color = PreviewBlue,
                    width = 9f
                )
            }

            if (routeToDropoff.size >= 2) {
                Polyline(
                    points = routeToDropoff,
                    color = PreviewGreen,
                    width = 9f
                )
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 10.dp, start = 14.dp)
                .size(48.dp)
                .shadow(7.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.floatingBg)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = colors.floatingIcon
            )
        }

        IconButton(
            onClick = {
                val target = driverPoint ?: pickupPoint ?: initialPoint

                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(target, 16f)
                    )
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 10.dp, end = 14.dp)
                .size(48.dp)
                .shadow(7.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.floatingBg)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar",
                tint = PreviewBlue
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colors.sheetBg
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(colors.handle)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Revisa la ruta",
                    color = colors.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Tu ubicación → recojo → entrega",
                    color = colors.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                RouteAddressRow(
                    colors = colors,
                    label = "Recojo",
                    value = pickupAddress.ifBlank { "Punto de recojo" },
                    dotColor = PreviewGreen,
                    showLine = true
                )

                RouteAddressRow(
                    colors = colors,
                    label = "Entrega",
                    value = dropoffAddress.ifBlank { "Punto de entrega" },
                    dotColor = PreviewRed,
                    showLine = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBg,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RouteStat(
                            label = "A recojo",
                            value = when {
                                isLoadingRoute -> "Calculando"
                                driverToPickupKm != null && driverToPickupMin != null ->
                                    "${"%.2f".format(driverToPickupKm)} km · ${driverToPickupMin} min"
                                else -> "Sin ubicación"
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(colors.border)
                        )

                        RouteStat(
                            label = "Viaje",
                            value = "$distance km · ${pickupToDropoffMin ?: "-"} min",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (routeError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = routeError,
                        color = PreviewRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (acceptError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = acceptError,
                        color = PreviewRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { aceptarPedido() },
                    enabled = !isAccepting && orderId > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PreviewRed,
                        contentColor = Color.White,
                        disabledContainerColor = colors.disabledBg,
                        disabledContentColor = colors.disabledText
                    )
                ) {
                    if (isAccepting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(21.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Text(
                            text = "Aceptando pedido...",
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Aceptar pedido por S/ $total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteAddressRow(
    colors: RoutePreviewColors,
    label: String,
    value: String,
    dotColor: Color,
    showLine: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.sheetBg)
                    .border(2.dp, dotColor, CircleShape)
            )

            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(30.dp)
                        .background(colors.border)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (showLine) 10.dp else 0.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = colors.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )

            Text(
                text = value,
                color = colors.text,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RowScope.RouteStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            color = if (ThemeManager.isDarkMode.value) {
                Color(0xFF94A3B8)
            } else {
                Color(0xFF6B7590)
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = if (ThemeManager.isDarkMode.value) {
                Color.White
            } else {
                Color(0xFF111A33)
            },
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun bitmapDescriptorFromDrawableRoute(
    context: android.content.Context,
    drawableId: Int,
    width: Int,
    height: Int
): BitmapDescriptor {
    return try {
        val drawable = ContextCompat.getDrawable(
            context,
            drawableId
        ) ?: return BitmapDescriptorFactory.defaultMarker()

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
    } catch (_: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}