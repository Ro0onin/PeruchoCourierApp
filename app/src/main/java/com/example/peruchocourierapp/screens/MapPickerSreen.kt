package com.example.peruchocourierapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.theme.ThemeManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

private val limaCenter = LatLng(-12.0464, -77.0428)
private const val userZoom = 16f

private val MapBlue = Color(0xFF1A4FBF)
private val MapRed = Color(0xFFE02020)

private data class MapPickerColors(
    val sheetBg: Color,
    val searchBg: Color,
    val fieldBg: Color,
    val fieldBorder: Color,
    val text: Color,
    val muted: Color,
    val placeholder: Color,
    val handle: Color,
    val locationButtonBg: Color,
    val locationButtonIcon: Color,
    val optionBg: Color,
    val optionSelectedBg: Color,
    val confirmBg: Color,
    val confirmDisabledBg: Color,
    val confirmText: Color,
    val error: Color
)

@Composable
private fun mapPickerColors(): MapPickerColors {
    val dark = ThemeManager.isDarkMode.value

    return if (dark) {
        MapPickerColors(
            sheetBg = Color(0xFF111827),
            searchBg = Color(0xFF111827),
            fieldBg = Color(0xFF1F2937),
            fieldBorder = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            placeholder = Color(0xFF94A3B8),
            handle = Color(0xFF334155),
            locationButtonBg = Color(0xFF111827).copy(alpha = 0.95f),
            locationButtonIcon = Color(0xFFF8FAFC),
            optionBg = Color(0xFF1F2937),
            optionSelectedBg = Color(0xFF172554),
            confirmBg = Color(0xFFF8FAFC),
            confirmDisabledBg = Color(0xFF475569),
            confirmText = Color(0xFF0F172A),
            error = Color(0xFFFFB4B4)
        )
    } else {
        MapPickerColors(
            sheetBg = Color.White,
            searchBg = Color.White,
            fieldBg = Color(0xFFF7F7F7),
            fieldBorder = Color(0xFFE5E5E5),
            text = Color(0xFF1A1A1A),
            muted = Color(0xFF777777),
            placeholder = Color(0xFF777777),
            handle = Color(0xFFE0E0E0),
            locationButtonBg = Color.White.copy(alpha = 0.95f),
            locationButtonIcon = Color(0xFF1A1A1A),
            optionBg = Color(0xFFF7F7F7),
            optionSelectedBg = Color(0xFFEAF1FF),
            confirmBg = Color(0xFF1A1A1A),
            confirmDisabledBg = Color(0xFFBDBDBD),
            confirmText = Color.White,
            error = MapRed
        )
    }
}

private fun estaDentroDePeru(lat: Double, lng: Double): Boolean {
    return lat in -18.50..-0.01 && lng in -81.50..-68.50
}

private fun esPlusCode(texto: String): Boolean {
    val limpio = texto.trim().uppercase()
    val primeraParte = limpio.substringBefore(",").trim()
    return Regex("^[A-Z0-9]{4}\\+[A-Z0-9]{2,4}").containsMatchIn(primeraParte)
}

private fun limpiarDireccion(texto: String?): String {
    val valor = texto?.trim().orEmpty()
    if (valor.isBlank()) return "Ubicación seleccionada"

    return if (esPlusCode(valor)) {
        valor.substringAfter(",", "Ubicación seleccionada").trim()
            .ifBlank { "Ubicación seleccionada" }
    } else {
        valor
    }
}

private fun direccionDesdePlace(
    place: Place,
    prediction: AutocompletePrediction
): String {
    val address = limpiarDireccion(place.address)
    val name = limpiarDireccion(place.name)
    val primary = limpiarDireccion(prediction.getPrimaryText(null).toString())
    val secondary = limpiarDireccion(prediction.getSecondaryText(null).toString())
    val fullText = limpiarDireccion(prediction.getFullText(null).toString())

    return when {
        address != "Ubicación seleccionada" && !esPlusCode(address) -> address
        secondary != "Ubicación seleccionada" && !esPlusCode(secondary) -> secondary
        fullText != "Ubicación seleccionada" && !esPlusCode(fullText) -> fullText
        name != "Ubicación seleccionada" && !esPlusCode(name) -> name
        primary != "Ubicación seleccionada" && !esPlusCode(primary) -> primary
        else -> "Ubicación seleccionada"
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapPickerScreen(
    navController: NavController,
    tipo: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = mapPickerColors()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var locationSelected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isMoving by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var placesReady by remember { mutableStateOf(false) }
    var modoMapa by remember { mutableStateOf(false) }

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
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaCenter, 14f)
    }

    LaunchedEffect(Unit) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )

            val mapsApiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""

            if (mapsApiKey.isNotBlank() && !Places.isInitialized()) {
                Places.initialize(context.applicationContext, mapsApiKey)
            }

            placesReady = Places.isInitialized()
        } catch (_: Exception) {
            placesReady = false
        }

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(searchText, placesReady, modoMapa) {
        if (modoMapa) {
            predictions = emptyList()
            return@LaunchedEffect
        }

        if (!placesReady || searchText.trim().length < 3) {
            predictions = emptyList()
            return@LaunchedEffect
        }

        val placesClient = Places.createClient(context)

        val query = if (
            searchText.contains("Perú", ignoreCase = true) ||
            searchText.contains("Lima", ignoreCase = true)
        ) {
            searchText
        } else {
            "$searchText, Lima, Perú"
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("PE")
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
                errorMessage = ""
            }
            .addOnFailureListener { e ->
                predictions = emptyList()
                errorMessage = e.message ?: "No se pudieron cargar sugerencias"
            }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val location = fusedLocationClient.lastLocation.await()

                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    if (estaDentroDePeru(userLatLng.latitude, userLatLng.longitude)) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(userLatLng, userZoom)
                        )
                    } else {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                        )
                    }
                } else {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                    )
                }

                selectedLocation = null
                selectedAddress = ""
                locationSelected = false
            } catch (_: Exception) {
                selectedLocation = null
                selectedAddress = ""
                locationSelected = false

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                )
            }
        } else {
            selectedLocation = null
            selectedAddress = ""
            locationSelected = false

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
            )
        }
    }

    LaunchedEffect(cameraPositionState.isMoving, modoMapa) {
        if (!modoMapa) return@LaunchedEffect

        if (cameraPositionState.isMoving) {
            isMoving = true
            errorMessage = ""
            return@LaunchedEffect
        }

        if (isMoving) {
            isMoving = false
            isSearching = true

            val center = cameraPositionState.position.target

            if (estaDentroDePeru(center.latitude, center.longitude)) {
                selectedLocation = center
                locationSelected = true

                selectedAddress = obtenerDireccionDesdeCoordenadas(
                    context = context,
                    lat = center.latitude,
                    lng = center.longitude
                )

                searchText = selectedAddress
                errorMessage = ""
            } else {
                errorMessage = "Solo puedes seleccionar ubicaciones dentro del Perú"
            }

            isSearching = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(

            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
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
            if (!modoMapa && locationSelected && selectedLocation != null) {
                Marker(
                    state = MarkerState(position = selectedLocation!!),
                    title = if (tipo == "pickup") "Punto de recojo" else "Punto de entrega",
                    draggable = false,
                    icon = bitmapDescriptorFromDrawableSafe(
                        context,
                        if (tipo == "pickup") R.drawable.ic_pin_recojo else R.drawable.ic_pin_entrega,
                        120,
                        120
                    )
                )
            }
        }

        if (modoMapa) {
            Image(
                painter = painterResource(
                    id = if (tipo == "pickup") R.drawable.ic_pin_recojo else R.drawable.ic_pin_entrega
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(58.dp)
                    .offset(y = (-29).dp)
            )
        }

        SearchBoxArriba(
            tipo = tipo,
            searchText = searchText,
            predictions = predictions,
            isSearching = isSearching,
            colors = colors,
            onTextChange = {
                modoMapa = false
                searchText = it
                errorMessage = ""
            },
            onClear = {
                modoMapa = false
                searchText = ""
                predictions = emptyList()
                selectedLocation = null
                selectedAddress = ""
                locationSelected = false
                errorMessage = ""
            },
            onPredictionClick = { prediction ->
                modoMapa = false
                isSearching = true
                errorMessage = ""

                val request = FetchPlaceRequest.newInstance(
                    prediction.placeId,
                    listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                    )
                )

                Places.createClient(context)
                    .fetchPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        val latLng = place.latLng

                        if (latLng == null) {
                            errorMessage = "No se pudo obtener la ubicación"
                            isSearching = false
                            return@addOnSuccessListener
                        }

                        if (!estaDentroDePeru(latLng.latitude, latLng.longitude)) {
                            errorMessage = "Solo puedes seleccionar ubicaciones dentro del Perú"
                            isSearching = false
                            return@addOnSuccessListener
                        }

                        selectedLocation = latLng
                        selectedAddress = direccionDesdePlace(place, prediction)
                        locationSelected = true
                        searchText = selectedAddress
                        predictions = emptyList()
                        isSearching = false

                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, userZoom)
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        errorMessage = e.message ?: "No se pudo seleccionar el lugar"
                        isSearching = false
                    }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )

        IconButton(
            onClick = {
                if (!hasLocationPermission) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                    return@IconButton
                }

                modoMapa = false
                isSearching = true
                errorMessage = ""

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location == null) {
                            errorMessage = "No se pudo obtener tu ubicación actual"
                            isSearching = false
                            return@addOnSuccessListener
                        }

                        val latLng = LatLng(location.latitude, location.longitude)

                        if (!estaDentroDePeru(latLng.latitude, latLng.longitude)) {
                            errorMessage = "Solo puedes seleccionar ubicaciones dentro del Perú"
                            isSearching = false
                            return@addOnSuccessListener
                        }

                        selectedLocation = latLng
                        locationSelected = true

                        scope.launch {
                            selectedAddress = obtenerDireccionDesdeCoordenadas(
                                context = context,
                                lat = latLng.latitude,
                                lng = latLng.longitude
                            )

                            searchText = selectedAddress
                            isSearching = false

                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, userZoom)
                            )
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = "No se pudo obtener tu ubicación"
                        isSearching = false
                    }
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 86.dp, end = 16.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.locationButtonBg)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = colors.locationButtonIcon
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.sheetBg
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.handle)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = when (tipo) {
                        "pickup" -> "Punto de recojo"
                        "dropoff" -> "Punto de entrega"
                        else -> "Tu ubicación"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = selectedAddress,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación seleccionada") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.fieldBg,
                        unfocusedContainerColor = colors.fieldBg,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = colors.muted,
                        unfocusedLabelColor = colors.muted,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        cursorColor = MapBlue
                    ),
                    leadingIcon = {
                        Image(
                            painter = painterResource(
                                id = if (tipo == "pickup") {
                                    R.drawable.ic_pin_recojo
                                } else {
                                    R.drawable.ic_pin_entrega
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (modoMapa) colors.optionSelectedBg else colors.optionBg
                        )
                        .clickable {
                            modoMapa = true
                            predictions = emptyList()
                            errorMessage = ""
                            isSearching = true

                            val center = cameraPositionState.position.target

                            selectedLocation = center
                            locationSelected = true

                            scope.launch {
                                if (estaDentroDePeru(center.latitude, center.longitude)) {
                                    selectedAddress = obtenerDireccionDesdeCoordenadas(
                                        context = context,
                                        lat = center.latitude,
                                        lng = center.longitude
                                    )

                                    searchText = selectedAddress
                                    errorMessage = ""
                                } else {
                                    selectedAddress = ""
                                    searchText = ""
                                    errorMessage = "Solo puedes seleccionar ubicaciones dentro del Perú"
                                }

                                isSearching = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MapBlue
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Señalar ubicación en el mapa",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }

                if (isSearching) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Buscando dirección...",
                        color = MapBlue,
                        fontSize = 13.sp
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = colors.error,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val location = selectedLocation

                        if (location == null || !locationSelected || selectedAddress.isBlank()) {
                            errorMessage = "Selecciona una dirección primero"
                            return@Button
                        }

                        if (!estaDentroDePeru(location.latitude, location.longitude)) {
                            errorMessage = "Solo puedes confirmar ubicaciones dentro del Perú"
                            return@Button
                        }

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("${tipo}_address", selectedAddress)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("${tipo}_lat", location.latitude)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("${tipo}_lng", location.longitude)

                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isMoving &&
                            !isSearching &&
                            errorMessage.isBlank() &&
                            locationSelected &&
                            selectedLocation != null &&
                            selectedAddress.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.confirmBg,
                        contentColor = colors.confirmText,
                        disabledContainerColor = colors.confirmDisabledBg,
                        disabledContentColor = colors.confirmText.copy(alpha = 0.75f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = when {
                            isMoving -> "Ubicando..."
                            isSearching -> "Buscando..."
                            else -> "Confirmar ubicación"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBoxArriba(
    tipo: String,
    searchText: String,
    predictions: List<AutocompletePrediction>,
    isSearching: Boolean,
    colors: MapPickerColors,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onPredictionClick: (AutocompletePrediction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.searchBg)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = if (tipo == "pickup") {
                            "Busca el punto de recojo"
                        } else {
                            "Busca el punto de entrega"
                        },
                        color = colors.placeholder
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.placeholder
                    )
                },
                trailingIcon = {
                    if (searchText.isNotBlank()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = colors.placeholder
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.fieldBg,
                    unfocusedContainerColor = colors.fieldBg,
                    focusedBorderColor = MapBlue,
                    unfocusedBorderColor = colors.fieldBorder,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedPlaceholderColor = colors.placeholder,
                    unfocusedPlaceholderColor = colors.placeholder,
                    cursorColor = MapBlue
                ),
                shape = RoundedCornerShape(16.dp)
            )

            if (isSearching) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Buscando dirección...",
                    color = MapBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (predictions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                ) {
                    predictions.take(6).forEach { prediction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPredictionClick(prediction) }
                                .padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MapBlue,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    color = colors.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    color = colors.muted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun obtenerDireccionDesdeCoordenadas(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String = withContext(Dispatchers.IO) {
    try {
        if (!estaDentroDePeru(lat, lng)) {
            return@withContext "Ubicación fuera del Perú"
        }

        val lugarCercano = obtenerLugarCercanoConPlaces(context, lat, lng)

        if (!lugarCercano.isNullOrBlank() && !esPlusCode(lugarCercano)) {
            return@withContext lugarCercano
        }

        val geocoder = Geocoder(context, Locale("es", "PE"))
        val addresses = geocoder.getFromLocation(lat, lng, 1)

        if (!addresses.isNullOrEmpty()) {
            val item = addresses[0]

            val nombreLimpio = listOfNotNull(
                item.featureName,
                item.thoroughfare,
                item.subLocality,
                item.locality
            )
                .filter { it.isNotBlank() }
                .filter { !esPlusCode(it) }
                .distinct()
                .joinToString(", ")

            nombreLimpio.ifBlank { "Ubicación seleccionada" }
        } else {
            "Ubicación seleccionada"
        }
    } catch (_: Exception) {
        "Ubicación seleccionada"
    }
}

private fun bitmapDescriptorFromDrawableSafe(
    context: android.content.Context,
    drawableId: Int,
    width: Int,
    height: Int
): com.google.android.gms.maps.model.BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, drawableId)
        ?: return BitmapDescriptorFactory.defaultMarker()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private suspend fun obtenerLugarCercanoConPlaces(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String? = withContext(Dispatchers.IO) {
    try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
        if (apiKey.isBlank()) return@withContext null

        val url = java.net.URL("https://places.googleapis.com/v1/places:searchNearby")
        val connection = url.openConnection() as java.net.HttpURLConnection

        val body = """
            {
              "maxResultCount": 5,
              "rankPreference": "DISTANCE",
              "locationRestriction": {
                "circle": {
                  "center": {
                    "latitude": $lat,
                    "longitude": $lng
                  },
                  "radius": 80.0
                }
              },
              "languageCode": "es"
            }
        """.trimIndent()

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", apiKey)
        connection.setRequestProperty(
            "X-Goog-FieldMask",
            "places.displayName,places.formattedAddress,places.types,places.location"
        )
        connection.doOutput = true

        connection.outputStream.use {
            it.write(body.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode

        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        if (responseCode !in 200..299) {
            android.util.Log.e("PLACES_NEARBY", response)
            return@withContext null
        }

        val json = org.json.JSONObject(response)
        val places = json.optJSONArray("places") ?: return@withContext null

        for (i in 0 until places.length()) {
            val place = places.getJSONObject(i)

            val name = place
                .optJSONObject("displayName")
                ?.optString("text")
                ?.trim()
                .orEmpty()

            if (
                name.isNotBlank() &&
                !esPlusCode(name) &&
                !name.contains("Unnamed", ignoreCase = true)
            ) {
                return@withContext name
            }
        }

        null
    } catch (e: Exception) {
        android.util.Log.e("PLACES_NEARBY", "Error buscando lugar cercano", e)
        null
    }
}