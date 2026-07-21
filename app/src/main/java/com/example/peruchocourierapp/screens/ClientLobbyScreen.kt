package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.VerificationStatusResponse
import com.example.peruchocourierapp.theme.ThemeManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val BrandRed = Color(0xFFE42328)
private val BrandBlue = Color(0xFF1E4FD8)
private val DarkBlue = Color(0xFF0E3596)
private val DarkBlue2 = Color(0xFF1A4DCB)
private val SuccessGreen = Color(0xFF22C55E)

private enum class ClientTab {
    INICIO,
    HACER_PEDIDO,
    CUENTA
}

private data class ClientLobbyColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val softBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val subtleText: Color,
    val navBar: Color,
    val dialogSurface: Color,
    val blueTint: Color,
    val redTint: Color,
    val greenTint: Color,
    val arrow: Color
)

@Composable
private fun clientLobbyColors(): ClientLobbyColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return ClientLobbyColors(
        background = if (dark) Color(0xFF0F172A) else Color(0xFFF7F8FC),
        surface = if (dark) Color(0xFF111827) else Color.White,
        card = if (dark) Color(0xFF1E293B) else Color.White,
        softBg = if (dark) Color(0xFF1F2937) else Color(0xFFF1F3F7),
        border = if (dark) Color(0xFF334155) else Color(0xFFE5E7EB),
        text = if (dark) Color(0xFFF8FAFC) else Color(0xFF111827),
        muted = if (dark) Color(0xFFCBD5E1) else Color(0xFF6B7280),
        subtleText = if (dark) Color(0xFF94A3B8) else Color(0xFF9CA3AF),
        navBar = if (dark) Color(0xFF111827) else Color.White,
        dialogSurface = if (dark) Color(0xFF111827) else Color.White,
        blueTint = if (dark) Color(0xFF172554) else Color(0xFFEAF0FE),
        redTint = if (dark) Color(0xFF3F1717) else Color(0xFFFDECEC),
        greenTint = if (dark) Color(0xFF0F2F20) else Color(0xFFE5F6EE),
        arrow = if (dark) Color(0xFF64748B) else Color(0xFF9CA3AF)
    )
}

data class NearbyPlaceItem(
    val title: String,
    val subtitle: String,
    val distanceText: String,
    val lat: Double? = null,
    val lng: Double? = null
)

@Composable
fun ClientLobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = clientLobbyColors()
    val sessionManager = remember { SessionManager(context) }

    val isLoggedIn = sessionManager.isLoggedIn()
    val userName = sessionManager.getUserName()?.takeIf { it.isNotBlank() } ?: "Usuario"
    val userEmail = sessionManager.getUserEmail().orEmpty()
    val initials = getInitialsClient(userName)

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = ClientTab.values()[selectedTabIndex]

    var pendingOrdersCount by remember { mutableIntStateOf(0) }
    var pendingOrdersIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    var canCreateOrders by remember { mutableStateOf(false) }
    var verificationMessage by remember {
        mutableStateOf("Debes verificar tu identidad antes de realizar envíos")
    }

    var showNotificationRationale by remember { mutableStateOf(false) }
    var showLocationRationale by remember { mutableStateOf(false) }

    fun bloquearSiNoVerificado(onAllowed: () -> Unit) {
        if (!isLoggedIn) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para continuar",
                Toast.LENGTH_LONG
            ).show()

            navController.navigate("login")
            return
        }

        if (canCreateOrders) {
            onAllowed()
        }
    }

    LaunchedEffect(isLoggedIn, userEmail) {
        if (isLoggedIn && userEmail.isNotBlank()) {
            RetrofitClient.instance.getVerificationStatus(userEmail)
                .enqueue(object : Callback<VerificationStatusResponse> {
                    override fun onResponse(
                        call: Call<VerificationStatusResponse>,
                        response: Response<VerificationStatusResponse>
                    ) {
                        val result = response.body()

                        if (response.isSuccessful && result?.success == true) {
                            val smsOk = result.phone_verified == 1
                            val identityOk =
                                result.identity_status.lowercase().trim() == "aprobado"

                            canCreateOrders = smsOk && identityOk

                            verificationMessage = when {
                                !smsOk ->
                                    "Debes verificar tu número por SMS antes de realizar envíos"

                                !identityOk ->
                                    "Tu identidad aún no fue aprobada por administración"

                                else -> ""
                            }
                        } else {
                            canCreateOrders = false
                            verificationMessage = "No se pudo verificar tu cuenta"
                        }
                    }

                    override fun onFailure(
                        call: Call<VerificationStatusResponse>,
                        t: Throwable
                    ) {
                        canCreateOrders = false
                        verificationMessage = "No se pudo verificar tu cuenta"
                    }
                })
        }
    }

    LaunchedEffect(isLoggedIn, userEmail) {
        if (isLoggedIn && userEmail.isNotBlank()) {
            RetrofitClient.instance.getOrders(userEmail)
                .enqueue(object : Callback<GetOrdersResponse> {
                    override fun onResponse(
                        call: Call<GetOrdersResponse>,
                        response: Response<GetOrdersResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val activeOrders = response.body()
                                ?.orders
                                .orEmpty()
                                .filter { order ->
                                    val status = order.estado
                                        ?.lowercase()
                                        ?.trim()
                                        ?.replace(" ", "_")
                                        .orEmpty()

                                    status !in listOf(
                                        "entregado",
                                        "cancelado",
                                        "cancelado_cliente"
                                    )
                                }

                            pendingOrdersCount = activeOrders.size
                            pendingOrdersIds = activeOrders.mapNotNull { it.id }
                        }
                    }

                    override fun onFailure(
                        call: Call<GetOrdersResponse>,
                        t: Throwable
                    ) {
                        pendingOrdersCount = 0
                        pendingOrdersIds = emptyList()
                    }
                })
        }
    }

    var nearbyPlaces by remember { mutableStateOf<List<NearbyPlaceItem>>(emptyList()) }
    var placesMessage by remember { mutableStateOf("") }
    var isLoadingPlaces by remember { mutableStateOf(true) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted

        if (!granted) {
            isLoadingPlaces = false
            placesMessage = "Activa tu ubicación para ver lugares cercanos"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted

        if (!granted) {
            Toast.makeText(
                context,
                "Activa las notificaciones para recibir avisos de tus pedidos",
                Toast.LENGTH_LONG
            ).show()
        }

        if (!hasLocationPermission) {
            showLocationRationale = true
        }
    }

    LaunchedEffect(Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasNotificationPermission -> {
                showNotificationRationale = true
            }

            !hasLocationPermission -> {
                showLocationRationale = true
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect

        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )

            val mapsApiKey =
                appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()

            if (mapsApiKey.isNotBlank() && !Places.isInitialized()) {
                Places.initialize(context.applicationContext, mapsApiKey)
            }

            if (!Places.isInitialized()) {
                isLoadingPlaces = false
                placesMessage = "No se pudo iniciar Places API"
                return@LaunchedEffect
            }

            val request = FindCurrentPlaceRequest.newInstance(
                listOf(
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
            )

            Places.createClient(context)
                .findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    nearbyPlaces = response.placeLikelihoods
                        .take(5)
                        .mapIndexed { index, likelihood ->
                            val place = likelihood.place

                            NearbyPlaceItem(
                                title = place.name ?: "Lugar cercano",
                                subtitle = place.address ?: "Ubicación cercana",
                                distanceText = "${(index + 1) * 4} min",
                                lat = place.latLng?.latitude,
                                lng = place.latLng?.longitude
                            )
                        }

                    isLoadingPlaces = false
                    placesMessage =
                        if (nearbyPlaces.isEmpty()) "No se encontraron lugares cercanos" else ""
                }
                .addOnFailureListener { error ->
                    isLoadingPlaces = false
                    placesMessage = error.message ?: "No se pudieron cargar lugares cercanos"
                }
        } catch (_: Exception) {
            isLoadingPlaces = false
            placesMessage = "No se pudieron cargar lugares cercanos"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ClientBottomBar(
                colors = colors,
                selectedTab = selectedTab,
                onSelected = { tab ->
                    selectedTabIndex = tab.ordinal
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ClientTab.INICIO -> {
                    InicioTab(
                        colors = colors,
                        places = nearbyPlaces,
                        placesMessage = placesMessage,
                        isLoadingPlaces = isLoadingPlaces,
                        onNational = {
                            bloquearSiNoVerificado {
                                navController.navigate("pedido_nacional")
                            }
                        },
                        onInternational = {
                            bloquearSiNoVerificado {
                                navController.navigate("pedido_internacional")
                            }
                        },
                        onPlaceClick = { place ->
                            bloquearSiNoVerificado {
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("pickup_address", place.subtitle)

                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("pickup_lat", place.lat ?: 0.0)

                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("pickup_lng", place.lng ?: 0.0)

                                navController.navigate("pedido_nacional")
                            }
                        }
                    )
                }

                ClientTab.HACER_PEDIDO -> {
                    HacerPedidoTab(
                        colors = colors,
                        canCreateOrders = canCreateOrders,
                        verificationMessage = verificationMessage,
                        onNational = {
                            bloquearSiNoVerificado {
                                navController.navigate("pedido_nacional")
                            }
                        },
                        onInternational = {
                            bloquearSiNoVerificado {
                                navController.navigate("pedido_internacional")
                            }
                        }
                    )
                }

                ClientTab.CUENTA -> {
                    CuentaTab(
                        colors = colors,
                        context = context,
                        userName = userName,
                        userEmail = userEmail,
                        initials = initials,
                        pendingOrdersCount = pendingOrdersCount,
                        canCreateOrders = canCreateOrders,
                        onProfile = {
                            navController.navigate("perfil_cliente")
                        },
                        onOrders = {
                            navController.navigate("mis_pedidos")
                        },
                        onMakeOrder = {
                            selectedTabIndex = ClientTab.HACER_PEDIDO.ordinal
                        },
                        onTracking = {
                            if (pendingOrdersIds.size == 1) {
                                navController.navigate(
                                    "seguimiento_cliente/${pendingOrdersIds.first()}"
                                )
                            } else {
                                navController.navigate("seleccionar_pedido_seguimiento")
                            }
                        },
                        onSupport = {
                            openWhatsApp(context)
                        },
                        onLogout = {
                            sessionManager.clearSession()

                            navController.navigate("welcome") {
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            if (showNotificationRationale) {
                PermissionRationaleDialog(
                    colors = colors,
                    icon = Icons.Outlined.Notifications,
                    title = "Activa tus notificaciones",
                    description = "Te avisamos cuando tu repartidor sea asignado, recoja tu paquete o esté por llegar.",
                    confirmText = "Activar notificaciones",
                    onConfirm = {
                        showNotificationRationale = false

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    },
                    onDismiss = {
                        showNotificationRationale = false

                        if (!hasLocationPermission) {
                            showLocationRationale = true
                        }
                    }
                )
            }

            if (showLocationRationale) {
                PermissionRationaleDialog(
                    colors = colors,
                    icon = Icons.Outlined.LocationOn,
                    title = "Activa tu ubicación",
                    description = "La usamos para mostrar lugares cercanos y calcular la ruta de tus envíos.",
                    confirmText = "Activar ubicación",
                    onConfirm = {
                        showLocationRationale = false
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    },
                    onDismiss = {
                        showLocationRationale = false
                        isLoadingPlaces = false
                        placesMessage = "Activa tu ubicación para ver lugares cercanos"
                    }
                )
            }
        }
    }
}

@Composable
private fun ClientBottomBar(
    colors: ClientLobbyColors,
    selectedTab: ClientTab,
    onSelected: (ClientTab) -> Unit
) {
    NavigationBar(
        containerColor = colors.navBar,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        NavigationBarItem(
            selected = selectedTab == ClientTab.INICIO,
            onClick = { onSelected(ClientTab.INICIO) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == ClientTab.INICIO) {
                        Icons.Filled.Home
                    } else {
                        Icons.Outlined.Home
                    },
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") },
            colors = bottomItemColors(colors)
        )

        NavigationBarItem(
            selected = selectedTab == ClientTab.HACER_PEDIDO,
            onClick = { onSelected(ClientTab.HACER_PEDIDO) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = "Hacer pedido"
                )
            },
            label = { Text("Hacer Pedido") },
            colors = bottomItemColors(colors)
        )

        NavigationBarItem(
            selected = selectedTab == ClientTab.CUENTA,
            onClick = { onSelected(ClientTab.CUENTA) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Cuenta"
                )
            },
            label = { Text("Cuenta") },
            colors = bottomItemColors(colors)
        )
    }
}

@Composable
private fun bottomItemColors(colors: ClientLobbyColors) =
    NavigationBarItemDefaults.colors(
        selectedIconColor = BrandBlue,
        selectedTextColor = BrandBlue,
        indicatorColor = colors.blueTint,
        unselectedIconColor = colors.subtleText,
        unselectedTextColor = colors.subtleText
    )

@Composable
private fun InicioTab(
    colors: ClientLobbyColors,
    places: List<NearbyPlaceItem>,
    placesMessage: String,
    isLoadingPlaces: Boolean,
    onNational: () -> Unit,
    onInternational: () -> Unit,
    onPlaceClick: (NearbyPlaceItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HomeTopBar()

        ServiceGrid(
            onNational = onNational,
            onInternational = onInternational
        )

        Spacer(modifier = Modifier.height(10.dp))

        SearchDeliveryBar(
            colors = colors,
            onClick = onNational
        )

        Spacer(modifier = Modifier.height(8.dp))

        RecentPlacesList(
            colors = colors,
            places = places,
            message = placesMessage,
            isLoading = isLoadingPlaces,
            onPlaceClick = onPlaceClick
        )
    }
}

@Composable
private fun HomeTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .padding(horizontal = 18.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_perucho2),
            contentDescription = "Perucho Courier",
            modifier = Modifier
                .width(230.dp)
                .height(72.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 26.dp, y = (-4).dp)
                .graphicsLayer(
                    scaleX = 1.30f,
                    scaleY = 1.30f
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ServiceGrid(
    onNational: () -> Unit,
    onInternational: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ServiceCardSmall(
                title = "Envío Nacional",
                subtitle = "Lima y provincias",
                backgroundImage = R.drawable.caja_nacional,
                onClick = onNational
            )

            ServiceCardSmall(
                title = "Repartidor",
                subtitle = "Próximamente disponible",
                backgroundImage = R.drawable.moto,
                onClick = {
                    Toast.makeText(
                        context,
                        "La opción para convertirse en repartidor aún no está disponible.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        ServiceCardLarge(
            title = "Internacional",
            subtitle = "USA / China → Perú",
            price = "$8.5 / kg",
            backgroundImage = R.drawable.avion_internacional,
            onClick = onInternational,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ServiceCardSmall(
    title: String,
    subtitle: String,
    backgroundImage: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DarkBlue)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            contentScale = ContentScale.Crop,
            alpha = 0.86f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            DarkBlue.copy(alpha = 0.98f),
                            DarkBlue.copy(alpha = 0.72f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ServiceCardLarge(
    title: String,
    subtitle: String,
    price: String,
    backgroundImage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(234.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(DarkBlue2, DarkBlue)
                )
            )
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
                .padding(8.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.95f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = price,
                color = Color(0xFFFF3038),
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SearchDeliveryBar(
    colors: ClientLobbyColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(colors.card)
            .border(
                1.dp,
                BrandBlue.copy(alpha = 0.28f),
                RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "📍", fontSize = 22.sp)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "¿A dónde enviamos?",
            color = colors.muted,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BrandBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun RecentPlacesList(
    colors: ClientLobbyColors,
    places: List<NearbyPlaceItem>,
    message: String,
    isLoading: Boolean,
    onPlaceClick: (NearbyPlaceItem) -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandBlue)
            }
        }

        places.isEmpty() -> {
            RecentPlaceRow(
                colors = colors,
                title = message.ifBlank { "No se encontraron lugares cercanos" },
                subtitle = "Activa tu ubicación o vuelve a intentarlo",
                time = "",
                onClick = {}
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 16.dp
                )
            ) {
                items(places.size) { index ->
                    RecentPlaceRow(
                        colors = colors,
                        title = places[index].title,
                        subtitle = places[index].subtitle,
                        time = places[index].distanceText,
                        onClick = { onPlaceClick(places[index]) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPlaceRow(
    colors: ClientLobbyColors,
    title: String,
    subtitle: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.softBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = colors.text,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = colors.subtleText,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (time.isNotBlank()) {
            Text(
                text = time,
                color = colors.subtleText,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun HacerPedidoTab(
    colors: ClientLobbyColors,
    canCreateOrders: Boolean,
    verificationMessage: String,
    onNational: () -> Unit,
    onInternational: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background.copy(alpha = 0.78f),
                            colors.background.copy(alpha = 0.94f),
                            colors.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = 12.dp
                )
        ) {
            Text(
                text = "Hacer Pedido",
                color = colors.text,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "¿Qué tipo de pedido deseas realizar?",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Elige la opción que mejor se adapte a tus necesidades.",
                color = colors.subtleText,
                fontSize = 12.5.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (!canCreateOrders) {
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = BrandRed.copy(alpha = 0.48f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (
                            MaterialTheme.colorScheme.background.luminance() < 0.5f
                        ) {
                            Color(0xFF3F1717)
                        } else {
                            Color(0xFFFFEEEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(BrandRed.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verificación previa requerida",
                                color = colors.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = verificationMessage.ifBlank {
                                    "Tu identidad debe ser aprobada antes de crear pedidos."
                                },
                                color = colors.muted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FullScreenOrderPanel(
                modifier = Modifier.weight(1f),
                title = "Pedido Nacional",
                description = "Envíos dentro de Lima Metropolitana, Callao y provincias.",
                accentColor = BrandRed,
                startColor = Color(0xFF4A1114),
                endColor = Color(0xFF24080A),
                backgroundImage = R.drawable.envio_nacional_repartidor,
                enabled = canCreateOrders,
                onClick = onNational
            )

            Spacer(modifier = Modifier.height(12.dp))

            FullScreenOrderPanel(
                modifier = Modifier.weight(1f),
                title = "Pedido Internacional",
                description = "Traemos tus compras desde USA o China hacia Perú.",
                accentColor = BrandBlue,
                startColor = Color(0xFF123274),
                endColor = Color(0xFF081633),
                backgroundImage = R.drawable.avion_pedido,
                enabled = canCreateOrders,
                onClick = onInternational
            )
        }
    }
}

@Composable
private fun FullScreenOrderPanel(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    accentColor: Color,
    startColor: Color,
    endColor: Color,
    backgroundImage: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(startColor, endColor)
                )
            )
            .border(
                width = 1.4.dp,
                color = if (enabled) {
                    accentColor.copy(alpha = 0.72f)
                } else {
                    Color.Gray.copy(alpha = 0.45f)
                },
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
    ) {
        if (backgroundImage != null) {
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (enabled) 0.48f else 0.24f),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            endColor.copy(alpha = 0.35f),
                            endColor.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        if (!enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                if (enabled) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.62f),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Bloqueado",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (enabled) {
                    description
                } else {
                    "Disponible después de aprobar la verificación de identidad."
                },
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.84f)
            )
        }
    }
}

@Composable
private fun CuentaTab(
    colors: ClientLobbyColors,
    context: Context,
    userName: String,
    userEmail: String,
    initials: String,
    pendingOrdersCount: Int,
    canCreateOrders: Boolean,
    onProfile: () -> Unit,
    onOrders: () -> Unit,
    onMakeOrder: () -> Unit,
    onTracking: () -> Unit,
    onSupport: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item {
            AccountHeader(
                context = context,
                userName = userName,
                userEmail = userEmail,
                initials = initials
            )
        }

        item { AccountSectionTitle("MI CUENTA", colors) }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.AccountCircle,
                title = "Mi perfil",
                iconBackground = colors.softBg,
                iconColor = colors.text,
                onClick = onProfile
            )
        }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.ReceiptLong,
                title = "Mis pedidos",
                subtitle = "Ver historial completo",
                badge = pendingOrdersCount.takeIf { it > 0 }?.toString(),
                iconBackground = colors.redTint,
                iconColor = BrandRed,
                onClick = onOrders
            )
        }

        item { AccountSectionTitle("SERVICIOS", colors) }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.ShoppingBag,
                title = "Hacer Pedido",
                subtitle = "Elige nacional o internacional",
                iconBackground = colors.redTint,
                iconColor = BrandRed,
                onClick = onMakeOrder
            )
        }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.LocationOn,
                title = "Rastrear pedido",
                subtitle = "Seguimiento en tiempo real",
                iconBackground = colors.greenTint,
                iconColor = SuccessGreen,
                onClick = onTracking
            )
        }

        item { AccountSectionTitle("INFORMACIÓN", colors) }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.Description,
                title = "Términos y Condiciones",
                subtitle = "Condiciones de uso",
                iconBackground = colors.softBg,
                iconColor = colors.text,
                onClick = {
                    abrirPaginaWeb(
                        context = context,
                        url = "https://peruchocourier.com/terminos.php"
                    )
                }
            )
        }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.Security,
                title = "Política de Privacidad",
                subtitle = "Protección de datos",
                iconBackground = colors.softBg,
                iconColor = colors.text,
                onClick = {
                    abrirPaginaWeb(
                        context = context,
                        url = "https://peruchocourier.com/privacidad.php"
                    )
                }
            )
        }

        item {
            AccountMenuItem(
                colors = colors,
                icon = Icons.Outlined.HeadsetMic,
                title = "Soporte",
                subtitle = "WhatsApp Perucho Courier",
                iconBackground = colors.softBg,
                iconColor = colors.text,
                onClick = onSupport
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Cerrar sesión",
                    color = BrandRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun AccountHeader(
    context: Context,
    userName: String,
    userEmail: String,
    initials: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(BrandRed, BrandBlue)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = userEmail,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ThemeToggle(context)
        }
    }
}

@Composable
private fun AccountSectionTitle(
    title: String,
    colors: ClientLobbyColors
) {
    Text(
        text = title,
        color = colors.subtleText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 8.dp
        )
    )
}

@Composable
private fun AccountMenuItem(
    colors: ClientLobbyColors,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    iconBackground: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = colors.subtleText,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!badge.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrandRed)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.arrow,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ThemeToggle(context: Context) {
    val dark = ThemeManager.isDarkMode.value

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.LightMode,
            contentDescription = null,
            tint = if (!dark) {
                Color(0xFFFFD54F)
            } else {
                Color.White.copy(alpha = 0.45f)
            },
            modifier = Modifier.size(17.dp)
        )

        Switch(
            checked = dark,
            onCheckedChange = {
                ThemeManager.setDark(context, it)
            },
            modifier = Modifier.scale(0.75f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0F172A),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.35f),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )

        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = null,
            tint = if (dark) {
                Color(0xFFB3E5FC)
            } else {
                Color.White.copy(alpha = 0.45f)
            },
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    colors: ClientLobbyColors,
    icon: ImageVector,
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.dialogSurface)
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BrandRed, BrandBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                color = colors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = colors.muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRed
                )
            ) {
                Text(
                    text = confirmText,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ahora no",
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun openWhatsApp(context: Context) {
    val phone = "51967929967"
    val message = Uri.encode("Hola Perucho Courier, necesito soporte.")

    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$phone?text=$message")
            )
        )
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "No se pudo abrir WhatsApp",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun getInitialsClient(name: String): String {
    val parts = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 ->
            "${parts[0].first()}${parts[1].first()}".uppercase()

        parts.size == 1 ->
            parts[0].take(2).uppercase()

        else -> "US"
    }
}
private fun abrirPaginaWeb(
    context: Context,
    url: String
) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )

        context.startActivity(intent)

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "No se pudo abrir la página",
            Toast.LENGTH_SHORT
        ).show()
    }
}