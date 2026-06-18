package com.example.peruchocourierapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

private val limaCenter = LatLng(-12.0464, -77.0428)
private const val userZoom = 16f

private fun estaDentroDePeru(lat: Double, lng: Double): Boolean {
    return lat in -18.50..-0.01 && lng in -81.50..-68.50
}

@SuppressLint("MissingPermission")
@Composable
fun MapPickerScreen(
    navController: NavController,
    tipo: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var selectedLocation by remember { mutableStateOf(limaCenter) }
    var selectedAddress by remember { mutableStateOf("Moviendo mapa...") }
    var errorMessage by remember { mutableStateOf("") }
    var isMoving by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var placesReady by remember { mutableStateOf(false) }

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

    LaunchedEffect(searchText, placesReady) {
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
                        selectedLocation = userLatLng

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(userLatLng, userZoom)
                        )

                        selectedAddress = obtenerDireccionDesdeCoordenadas(
                            context = context,
                            lat = userLatLng.latitude,
                            lng = userLatLng.longitude
                        )
                    } else {
                        selectedLocation = limaCenter
                        selectedAddress = "Lima, Perú"

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                        )
                    }
                } else {
                    selectedLocation = limaCenter
                    selectedAddress = "Lima, Perú"

                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                    )
                }
            } catch (_: Exception) {
                selectedLocation = limaCenter
                selectedAddress = "Lima, Perú"

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
                )
            }
        } else {
            selectedLocation = limaCenter
            selectedAddress = "Lima, Perú"

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(limaCenter, 14f)
            )
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            isMoving = true
            errorMessage = ""
            selectedAddress = "Moviendo mapa..."
        }

        if (!cameraPositionState.isMoving && isMoving) {
            isMoving = false

            val center = cameraPositionState.position.target

            if (!estaDentroDePeru(center.latitude, center.longitude)) {
                errorMessage = "Solo puedes seleccionar ubicaciones dentro del Perú"
                selectedAddress = "Ubicación fuera del Perú"
                return@LaunchedEffect
            }

            selectedLocation = center
            errorMessage = ""

            selectedAddress = obtenerDireccionDesdeCoordenadas(
                context = context,
                lat = center.latitude,
                lng = center.longitude
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-30).dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (tipo == "pickup") {
                        R.drawable.ic_pin_recojo
                    } else {
                        R.drawable.ic_pin_entrega
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }

        IconButton(
            onClick = {
                if (selectedLocation.latitude != 0.0 && selectedLocation.longitude != 0.0) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(selectedLocation, userZoom)
                    )
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = Color(0xFF1A1A1A)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
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
                        .background(Color(0xFFE0E0E0))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = when (tipo) {
                        "pickup" -> "Punto de recojo"
                        "dropoff" -> "Punto de entrega"
                        else -> "Tu ubicación"
                    },
                    fontSize = 24.sp,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        errorMessage = ""
                    },
                    placeholder = {
                        Text(
                            text = if (tipo == "pickup") {
                                "Escribe la calle o lugar de recojo"
                            } else {
                                "Escribe la calle o lugar de entrega"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF777777)
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    searchText = ""
                                    predictions = emptyList()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = Color(0xFF777777)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1A4FBF),
                        unfocusedBorderColor = Color(0xFFE5E5E5),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                if (predictions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 210.dp)
                        ) {
                            predictions.take(5).forEach { prediction ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isSearching = true
                                            errorMessage = ""

                                            val placeId = prediction.placeId
                                            val fields = listOf(
                                                Place.Field.ID,
                                                Place.Field.NAME,
                                                Place.Field.ADDRESS,
                                                Place.Field.LAT_LNG
                                            )

                                            val request = FetchPlaceRequest.newInstance(placeId, fields)

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
                                                    selectedAddress = place.address ?: place.name ?: prediction.getFullText(null).toString()
                                                    searchText = selectedAddress
                                                    predictions = emptyList()
                                                    isSearching = false

                                                    scope.launch {
                                                        cameraPositionState.animate(
                                                            CameraUpdateFactory.newLatLngZoom(
                                                                latLng,
                                                                userZoom
                                                            )
                                                        )
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = e.message ?: "No se pudo seleccionar el lugar"
                                                    isSearching = false
                                                }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF1A4FBF),
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prediction.getPrimaryText(null).toString(),
                                            color = Color(0xFF1A1A1A),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = prediction.getSecondaryText(null).toString(),
                                            color = Color(0xFF777777),
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

                if (isSearching) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Buscando dirección...",
                        color = Color(0xFF1A4FBF),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = selectedAddress,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación seleccionada") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7F7F7),
                        unfocusedContainerColor = Color(0xFFF7F7F7),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = Color(0xFF777777),
                        unfocusedLabelColor = Color(0xFF777777),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A)
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

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFE02020),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!estaDentroDePeru(
                                selectedLocation.latitude,
                                selectedLocation.longitude
                            )
                        ) {
                            errorMessage = "Solo puedes confirmar ubicaciones dentro del Perú"
                            return@Button
                        }

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "${tipo}_address",
                                if (selectedAddress.isBlank() || selectedAddress == "Moviendo mapa...") {
                                    "Ubicación seleccionada"
                                } else {
                                    selectedAddress
                                }
                            )

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("${tipo}_lat", selectedLocation.latitude)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("${tipo}_lng", selectedLocation.longitude)

                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isMoving && !isSearching && errorMessage.isBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBDBDBD),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = when {
                            isMoving -> "Ubicando..."
                            isSearching -> "Buscando..."
                            else -> "Confirmar ubicación"
                        },
                        fontSize = 17.sp
                    )
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

        val geocoder = Geocoder(context, Locale("es", "PE"))
        val addresses = geocoder.getFromLocation(lat, lng, 1)

        if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0) ?: "Ubicación seleccionada"
        } else {
            "Ubicación seleccionada"
        }
    } catch (e: Exception) {
        "Ubicación seleccionada"
    }
}
