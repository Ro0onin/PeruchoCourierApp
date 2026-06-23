package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.VerificationStatusResponse
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val BrandRed = Color(0xFFE02020)
private val BrandBlue = Color(0xFF1A4FBF)
private val BrandRedLight = Color(0xFFFFEEEE)
private val BrandBlueLight = Color(0xFFEAF2FF)
private val Dark = Color(0xFF1A1A1A)
private val LightBg = Color(0xFFF5F5F5)
private val Muted = Color(0xFF888888)
private val Green = Color(0xFF22C55E)
private val AirCardBlue = Color(0xFF0E3596)
private val AirCardBlue2 = Color(0xFF1A4DCB)

data class NearbyPlaceItem(
    val title: String,
    val subtitle: String,
    val distanceText: String,
    val lat: Double? = null,
    val lng: Double? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientLobbyScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val isLoggedIn = sessionManager.isLoggedIn()
    val userName = sessionManager.getUserName()?.takeIf { it.isNotBlank() } ?: "Usuario"
    val userEmail = sessionManager.getUserEmail()?.orEmpty() ?: ""
    val initials = getInitialsClient(userName)


    var showMenu by remember { mutableStateOf(false) }
    var pendingOrdersCount by remember { mutableStateOf(0) }
    var pendingOrdersIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    var canCreateOrders by remember { mutableStateOf(false) }
    var verificationMessage by remember {
        mutableStateOf("Debes verificar tu identidad antes de realizar envíos")
    }

    fun bloquearSiNoVerificado(onAllowed: () -> Unit) {
        if (!isLoggedIn) {
            Toast.makeText(context, "Debes iniciar sesión para continuar", Toast.LENGTH_LONG).show()
            navController.navigate("login")
            return
        }

        if (canCreateOrders) {
            onAllowed()
        } else {
            Toast.makeText(context, verificationMessage, Toast.LENGTH_LONG).show()
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
                            val identityOk = result.identity_status.lowercase().trim() == "aprobado"

                            canCreateOrders = smsOk && identityOk

                            verificationMessage = when {
                                !smsOk -> "Debes verificar tu número por SMS antes de realizar envíos"
                                !identityOk -> "Tu identidad aún no fue aprobada por administración"
                                else -> ""
                            }
                        } else {
                            canCreateOrders = false
                            verificationMessage = "No se pudo verificar tu cuenta"
                        }
                    }

                    override fun onFailure(call: Call<VerificationStatusResponse>, t: Throwable) {
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
                            val activeOrders = response.body()?.orders
                                .orEmpty()
                                .filter { order ->
                                    val estado = order.estado
                                        ?.lowercase()
                                        ?.trim()
                                        ?.replace(" ", "_")
                                        ?: ""

                                    estado != "entregado" && estado != "cancelado"
                                }

                            pendingOrdersCount = activeOrders.size
                            pendingOrdersIds = activeOrders.mapNotNull { it.id }
                        }
                    }

                    override fun onFailure(call: Call<GetOrdersResponse>, t: Throwable) {
                        pendingOrdersCount = 0
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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (!granted) {
            isLoadingPlaces = false
            placesMessage = "Activa tu ubicación para ver lugares cercanos"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val appInfo = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )

                val mapsApiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""

                if (mapsApiKey.isNotBlank() && !Places.isInitialized()) {
                    Places.initialize(context.applicationContext, mapsApiKey)
                }

                if (!Places.isInitialized()) {
                    placesMessage = "No se pudo iniciar Places API"
                    return@LaunchedEffect
                }

                val placesClient = Places.createClient(context)
                val placeFields = listOf(
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                val request = FindCurrentPlaceRequest.newInstance(placeFields)

                placesClient.findCurrentPlace(request)
                    .addOnSuccessListener { response ->
                        val places = response.placeLikelihoods
                            .take(5)
                            .mapIndexed { index, item ->
                                val place = item.place
                                NearbyPlaceItem(
                                    title = place.name ?: "Lugar cercano",
                                    subtitle = place.address ?: "Ubicación cercana",
                                    distanceText = "${(index + 1) * 4} min",
                                    lat = place.latLng?.latitude,
                                    lng = place.latLng?.longitude
                                )
                            }

                        nearbyPlaces = places
                        isLoadingPlaces = false
                        placesMessage = if (places.isEmpty()) {
                            "No se encontraron lugares cercanos"
                        } else {
                            ""
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoadingPlaces = false
                        placesMessage = e.message ?: "Error al cargar Places"
                    }

            } catch (e: Exception) {
                isLoadingPlaces = false
                placesMessage = "No se pudieron cargar lugares cercanos"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                onMenuClick = { showMenu = true }
            )

            ServiceGrid(
                onNacionalClick = {
                    bloquearSiNoVerificado {
                        navController.navigate("pedido_nacional")
                    }
                },
                onInternacionalClick = {
                    bloquearSiNoVerificado {
                        navController.navigate("pedido_internacional")
                    }
                },
                onRepartidorClick = {
                    navController.navigate("driver_lobby")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchDeliveryBar(
                onClick = {
                    bloquearSiNoVerificado {
                        navController.navigate("pedido_nacional")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecentPlacesList(
                places = nearbyPlaces,
                message = placesMessage,
                isLoading = isLoadingPlaces,
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

        if (showMenu) {
            ModalBottomSheet(
                onDismissRequest = { showMenu = false },
                containerColor = Color.White,
                dragHandle = null
            ) {
                DrawerMenuContent(
                    canCreateOrders = canCreateOrders,
                    isLoggedIn = isLoggedIn,
                    userName = userName,
                    userEmail = userEmail,
                    initials = initials,
                    onClose = { showMenu = false },
                    onLogin = {
                        showMenu = false
                        navController.navigate("login")
                    },
                    onRegister = {
                        showMenu = false
                        navController.navigate("register")
                    },
                    onProfile = {
                        showMenu = false
                        navController.navigate("perfil_cliente")
                    },
                    onOrders = {
                        showMenu = false
                        navController.navigate("mis_pedidos")
                    },
                    onTracking = {
                        showMenu = false

                        if (pendingOrdersIds.size == 1) {
                            navController.navigate("seguimiento_cliente/${pendingOrdersIds.first()}")
                        } else {
                            navController.navigate("seleccionar_pedido_seguimiento")
                        }
                    },
                    onNewOrder = {
                        showMenu = false
                        bloquearSiNoVerificado {
                            navController.navigate("pedido_nacional")
                        }
                    },
                    onInternational = {
                        showMenu = false
                        bloquearSiNoVerificado {
                            navController.navigate("pedido_internacional")
                        }
                    },
                    pendingOrdersCount = pendingOrdersCount,
                    onSupport = {
                        val numero = "51967929967"
                        val mensaje = Uri.encode("Hola Perucho Courier, necesito soporte.")
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://wa.me/$numero?text=$mensaje")
                        )
                        context.startActivity(intent)
                    },
                    onLogout = {
                        sessionManager.clearSession()
                        showMenu = false
                        navController.navigate("client_lobby") {
                            popUpTo("client_lobby") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    context = context
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 18.dp)
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menú",
                tint = Color.Black,
                modifier = Modifier.size(34.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.logo_perucho2),
            contentDescription = "Perucho Courier",
            modifier = Modifier
                .width(230.dp)
                .height(70.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp, y = (-6).dp)
                .graphicsLayer(
                    scaleX = 1.35f,
                    scaleY = 1.35f
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ServiceGrid(
    onNacionalClick: () -> Unit,
    onInternacionalClick: () -> Unit,
    onRepartidorClick: () -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ServiceCardSmall(
                emoji = "",
                title = "Envío Nacional",
                subtitle = "Lima y provincias",
                bg = AirCardBlue,
                titleColor = Color.White,
                        subtitleColor = Color.White.copy(alpha = 0.8f),
                backgroundImage = R.drawable.caja_nacional,
                onClick = onNacionalClick
            )

            ServiceCardSmall(
                emoji = "",
                title = "Repartidor",
                subtitle = "Próximamente disponible",
                bg = AirCardBlue,
                titleColor = Color.White,
                        subtitleColor = Color.White.copy(alpha = 0.8f),
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
            onClick = onInternacionalClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ServiceCardSmall(
    emoji: String,
    title: String,
    subtitle: String,
    bg: Color,
    titleColor: Color,
    subtitleColor: Color,
    backgroundImage: Int? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = Color(0xFFEAEAEA),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
    ) {
        if (backgroundImage != null) {
            Image(
                painter = painterResource(id = backgroundImage),
                contentDescription = null,
                modifier = Modifier
                    .width(220.dp)
                    .height(160.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 25.dp, y = 12.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.90f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                bg.copy(alpha = 0.98f),
                                bg.copy(alpha = 0.82f),
                                bg.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 12.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            if (emoji.isNotBlank()) {
                Text(
                    text = emoji,
                    fontSize = 26.sp,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = title,
                color = titleColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
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
            .height(250.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AirCardBlue2,
                        AirCardBlue
                    )
                )
            )
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopCenter)
                .offset(y = 8.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.95f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AirCardBlue.copy(alpha = 0.20f),
                            AirCardBlue.copy(alpha = 0.92f)
                        )
                    )
                )
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = price,
                color = BrandRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SearchDeliveryBar(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .border(1.dp, BrandBlue.copy(alpha = 0.18f), RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📍",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "¿A dónde enviamos?",
            color = Muted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BrandBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RecentPlacesList(
    places: List<NearbyPlaceItem>,
    message: String,
    isLoading: Boolean,
    onPlaceClick: (NearbyPlaceItem) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = BrandBlue,
                        strokeWidth = 5.dp,
                        modifier = Modifier.size(58.dp)
                    )
                }
            }

            places.isEmpty() -> {
                RecentPlaceRow(
                    icon = Icons.Outlined.LocationOn,
                    title = message.ifBlank { "No se encontraron lugares cercanos" },
                    subtitle = "Intenta activar tu ubicación o vuelve a intentarlo",
                    time = "",
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    onClick = {}
                )
            }

            else -> {
                places.forEachIndexed { index, place ->
                    RecentPlaceRow(
                        icon = Icons.Outlined.LocationOn,
                        title = place.title,
                        subtitle = place.subtitle,
                        time = place.distanceText,
                        iconBg = Color(0xFFF0F0F0),
                        iconTint = Color.Black,
                        onClick = { onPlaceClick(place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPlaceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    time: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Dark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = Color(0xFF999999),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerMenuContent(
    canCreateOrders: Boolean,
    isLoggedIn: Boolean,
    userName: String,
    userEmail: String,
    initials: String,
    onClose: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onProfile: () -> Unit,
    onOrders: () -> Unit,
    onTracking: () -> Unit,
    onNewOrder: () -> Unit,
    onInternational: () -> Unit,
    pendingOrdersCount: Int,
    onSupport: () -> Unit,
    onLogout: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(BrandRed, BrandBlue)
                    )
                )
                .padding(horizontal = 18.dp, vertical = 9.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (isLoggedIn) {
                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = userEmail,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Bienvenido",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Login,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = BrandRed
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                "Iniciar sesión",
                                color = BrandRed,
                                fontWeight = FontWeight.Black
                            )
                        }

                        OutlinedButton(
                            onClick = onRegister,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("Crear cuenta")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (isLoggedIn) {
                MenuSectionTitle("Mi cuenta")

                MenuItem(
                    icon = Icons.Outlined.AccountCircle,
                    iconBg = LightBg,
                    iconTint = Dark,
                    title = "Mi perfil",
                    subtitle = null,
                    onClick = onProfile
                )

                MenuItem(
                    icon = Icons.Outlined.ListAlt,
                    iconBg = BrandRedLight,
                    iconTint = BrandRed,
                    title = "Mis pedidos",
                    subtitle = "Ver historial completo",
                    badge = if (pendingOrdersCount > 0) pendingOrdersCount.toString() else null,
                    onClick = onOrders
                )
            }

            MenuSectionTitle("Servicios")

            MenuItem(
                icon = Icons.Outlined.Inventory2,
                iconBg = BrandRedLight,
                iconTint = BrandRed,
                title = "Nuevo pedido",
                subtitle = "Realiza tu pedido nacional",
                enabled = canCreateOrders,
                lockedText = "Pendiente de verificación",
                onClick = onNewOrder
            )

            MenuItem(
                icon = Icons.Outlined.Public,
                iconBg = BrandBlueLight,
                iconTint = BrandBlue,
                title = "Pedido Internacional",
                subtitle = "USA / China → Perú · $8.5/kg",
                enabled = canCreateOrders,
                lockedText = "Pendiente de verificación",
                onClick = onInternational
            )

            MenuItem(
                icon = Icons.Outlined.LocationOn,
                iconBg = Color(0xFFE8FAF0),
                iconTint = Green,
                title = "Rastrear pedido",
                subtitle = "Seguimiento en tiempo real",
                onClick = onTracking
            )

            MenuSectionTitle("Información")

            MenuItem(
                icon = Icons.Outlined.Description,
                iconBg = LightBg,
                iconTint = Dark,
                title = "Términos y Condiciones",
                subtitle = "Condiciones de uso",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://peruchocourier.com/terminos.php")
                    )
                    context.startActivity(intent)
                }
            )

            MenuItem(
                icon = Icons.Outlined.PrivacyTip,
                iconBg = LightBg,
                iconTint = Dark,
                title = "Política de Privacidad",
                subtitle = "Protección de datos",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://peruchocourier.com/privacidad.php")
                    )
                    context.startActivity(intent)
                }
            )

            MenuItem(
                icon = Icons.Outlined.HeadsetMic,
                iconBg = LightBg,
                iconTint = Dark,
                title = "Soporte",
                subtitle = "WhatsApp Perucho Courier",
                onClick = onSupport
            )

            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Cerrar sesión",
                        color = BrandRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "Perucho Courier v2.1.0",
                color = Color(0xFFBBBBBB),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MenuSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFFBBBBBB),
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
    )
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    badge: String? = null,
    enabled: Boolean = true,
    lockedText: String? = null,
    onClick: () -> Unit
) {
    val alphaValue = if (enabled) 1f else 0.35f
    val finalSubtitle = if (!enabled && lockedText != null) lockedText else subtitle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg.copy(alpha = alphaValue)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = alphaValue),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Dark.copy(alpha = alphaValue),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!finalSubtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = finalSubtitle,
                    color = if (!enabled) BrandRed.copy(alpha = 0.75f) else Color(0xFF999999),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.width(34.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            when {
                !enabled -> {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFFBBBBBB),
                        modifier = Modifier.size(18.dp)
                    )
                }

                !badge.isNullOrBlank() -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(BrandRed)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFFDDDDDD),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getInitialsClient(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "US"
    }
}