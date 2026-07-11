package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.RegisterResponse
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.peruchocourierapp.R

private val PNegro = Color(0xFF1A1A1A)
private val PRojo = Color(0xFFE02020)
private val PBlue = Color(0xFF1E4FD8)
private val PGrisF = Color(0xFFF5F5F5)
private val PGrisBorde = Color(0xFFE8E8E8)
private val PTextoSub = Color(0xFF888888)

private data class RegisterColors(
    val screenBg: Color,
    val cardBg: Color,
    val fieldBg: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val placeholder: Color,
    val primaryButton: Color,
    val topBar: Color,
    val successBg: Color,
    val successText: Color,
    val errorBg: Color,
    val errorText: Color,
    val uploadSelectedBg: Color,
    val uploadSelectedIcon: Color,
    val checkboxUnchecked: Color,
    val divider: Color
)

@Composable
private fun registerColors(): RegisterColors {
    val dark = isSystemInDarkTheme()

    return if (dark) {
        RegisterColors(
            screenBg = Color(0xFF0F172A),
            cardBg = Color(0xFF111827),
            fieldBg = Color(0xFF1F2937),
            border = Color(0xFF334155),
            text = Color(0xFFF8FAFC),
            muted = Color(0xFFCBD5E1),
            placeholder = Color(0xFF94A3B8),
            primaryButton = PRojo,
            topBar = PBlue,
            successBg = Color(0xFF14532D),
            successText = Color(0xFFDCFCE7),
            errorBg = Color(0xFF3F1717),
            errorText = Color(0xFFFFB4B4),
            uploadSelectedBg = Color(0xFF14532D),
            uploadSelectedIcon = Color(0xFF86EFAC),
            checkboxUnchecked = Color(0xFF475569),
            divider = Color(0xFF334155)
        )
    } else {
        RegisterColors(
            screenBg = Color.White,
            cardBg = Color.White,
            fieldBg = PGrisF,
            border = PGrisBorde,
            text = PNegro,
            muted = PTextoSub,
            placeholder = PTextoSub,
            primaryButton = PNegro,
            topBar = PBlue,
            successBg = Color(0xFFDCFCE7),
            successText = Color(0xFF16A34A),
            errorBg = Color(0xFFFFF0F0),
            errorText = PRojo,
            uploadSelectedBg = Color(0xFFEAFBF0),
            uploadSelectedIcon = Color(0xFF16A34A),
            checkboxUnchecked = PGrisBorde,
            divider = PGrisBorde
        )
    }
}

@Composable
fun RegisterScreen(navController: NavController) {

    val context = LocalContext.current
    val colors = registerColors()

    var name by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var dniDireccion by remember { mutableStateOf("") }
    var dniProvincia by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showTermsPopup by remember { mutableStateOf(false) }

    var dniFrontUri by remember { mutableStateOf<Uri?>(null) }
    var dniBackUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var cameraTarget by remember { mutableStateOf("") }

    fun crearFotoUri(prefix: String): Uri {
        val file = File.createTempFile(
            "${prefix}_${System.currentTimeMillis()}_",
            ".jpg",
            context.cacheDir
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val dniFrontCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            dniFrontUri = null
            errorMessage = "No se tomó la foto del DNI delantero"
        }
    }

    val dniBackCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            dniBackUri = null
            errorMessage = "No se tomó la foto del DNI posterior"
        }
    }

    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            selfieUri = null
            errorMessage = "No se tomó la selfie de verificación"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (cameraTarget) {
                "dni_front" -> {
                    val uri = crearFotoUri("dni_front")
                    dniFrontUri = uri
                    dniFrontCameraLauncher.launch(uri)
                }

                "dni_back" -> {
                    val uri = crearFotoUri("dni_back")
                    dniBackUri = uri
                    dniBackCameraLauncher.launch(uri)
                }

                "selfie" -> {
                    val uri = crearFotoUri("selfie")
                    selfieUri = uri
                    selfieCameraLauncher.launch(uri)
                }
            }
        } else {
            errorMessage = "Debes permitir el uso de la cámara para continuar"
        }
    }

    fun abrirCamaraRegistro(target: String) {
        cameraTarget = target

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            when (target) {
                "dni_front" -> {
                    val uri = crearFotoUri("dni_front")
                    dniFrontUri = uri
                    dniFrontCameraLauncher.launch(uri)
                }

                "dni_back" -> {
                    val uri = crearFotoUri("dni_back")
                    dniBackUri = uri
                    dniBackCameraLauncher.launch(uri)
                }

                "selfie" -> {
                    val uri = crearFotoUri("selfie")
                    selfieUri = uri
                    selfieCameraLauncher.launch(uri)
                }
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val isSuccess = errorMessage.contains("correctamente", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.topBar)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Image(
                painter = painterResource(R.drawable.logo_perucho2),
                contentDescription = "Perucho Courier",
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(110.dp),   // puedes probar 54, 58 o 62
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                "Crear cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colors.text
            )

            Text(
                "Regístrate para empezar a enviar",
                fontSize = 14.sp,
                color = colors.muted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {

            SectionLabel("Datos personales")
            Spacer(Modifier.height(10.dp))

            RegField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Nombre completo",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text,
                isFirst = true
            )

            RegField(
                value = dni,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 15) dni = it
                },
                placeholder = "DNI / Documento",
                leadingIcon = Icons.Outlined.Badge,
                keyboardType = KeyboardType.Number,
                isLast = true
            )

            Spacer(Modifier.height(18.dp))

            SectionLabel("Datos del DNI")
            Spacer(Modifier.height(10.dp))

            RegField(
                value = dniDireccion,
                onValueChange = { dniDireccion = it },
                placeholder = "Dirección del DNI",
                leadingIcon = Icons.Outlined.Home,
                keyboardType = KeyboardType.Text,
                isFirst = true
            )

            RegField(
                value = dniProvincia,
                onValueChange = { dniProvincia = it },
                placeholder = "Provincia del DNI",
                leadingIcon = Icons.Outlined.LocationCity,
                keyboardType = KeyboardType.Text,
                isLast = true
            )

            Spacer(Modifier.height(18.dp))

            SectionLabel("Contacto")
            Spacer(Modifier.height(10.dp))

            RegField(
                value = email,
                onValueChange = { email = it.trim() },
                placeholder = "Correo electrónico",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                isFirst = true
            )

            RegField(
                value = phone,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 9) phone = it
                },
                placeholder = "+51 9XXXXXXXX",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                isLast = true
            )

            Spacer(Modifier.height(18.dp))

            SectionLabel("Verificación de identidad")
            Spacer(Modifier.height(10.dp))

            DniUploadBox(
                title = "DNI parte delantera",
                subtitle = if (dniFrontUri != null) "Foto tomada correctamente" else "Toma una foto clara del frente del DNI",
                selected = dniFrontUri != null,
                onClick = { abrirCamaraRegistro("dni_front") }
            )

            Spacer(Modifier.height(10.dp))

            DniUploadBox(
                title = "DNI parte posterior",
                subtitle = if (dniBackUri != null) "Foto tomada correctamente" else "Toma una foto clara de la parte posterior",
                selected = dniBackUri != null,
                onClick = { abrirCamaraRegistro("dni_back") }
            )

            Spacer(Modifier.height(10.dp))

            DniUploadBox(
                title = "Selfie de verificación",
                subtitle = if (selfieUri != null) "Selfie tomada correctamente" else "Tómate una selfie clara de tu rostro",
                selected = selfieUri != null,
                onClick = { abrirCamaraRegistro("selfie") }
            )

            Spacer(Modifier.height(18.dp))

            SectionLabel("Contraseña")
            Spacer(Modifier.height(10.dp))

            RegField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña (mín. 6 caracteres)",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                isFirst = true
            )

            RegField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirmar contraseña",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                isLast = true
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.fieldBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        modifier = Modifier.size(22.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.text,
                            uncheckedColor = colors.checkboxUnchecked,
                            checkmarkColor = Color.White
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Aceptar términos y condiciones",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )

                        Text(
                            "Lee y acepta nuestra política de privacidad",
                            fontSize = 11.sp,
                            color = colors.muted
                        )

                        Text(
                            text = "Ver términos y condiciones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PRojo,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { showTermsPopup = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) colors.successBg else colors.errorBg
                ) {
                    Text(
                        text = errorMessage,
                        color = if (isSuccess) colors.successText else colors.errorText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() || dni.isBlank() || email.isBlank() ||
                                phone.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                            errorMessage = "Todos los campos son obligatorios"

                        dniDireccion.isBlank() ->
                            errorMessage = "Ingresa la dirección del DNI"

                        dniProvincia.isBlank() ->
                            errorMessage = "Ingresa la provincia del DNI"

                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                            errorMessage = "Correo electrónico inválido"

                        dni.length < 8 ->
                            errorMessage = "Documento inválido"

                        phone.length != 9 ->
                            errorMessage = "El teléfono debe tener 9 dígitos"

                        dniFrontUri == null ->
                            errorMessage = "Debes tomar la foto del DNI delantero"

                        dniBackUri == null ->
                            errorMessage = "Debes tomar la foto del DNI posterior"

                        selfieUri == null ->
                            errorMessage = "Debes tomarte una selfie para verificar tu identidad"

                        password.length < 6 ->
                            errorMessage = "La contraseña debe tener mínimo 6 caracteres"

                        password != confirmPassword ->
                            errorMessage = "Las contraseñas no coinciden"

                        !acceptedTerms ->
                            errorMessage = "Debes aceptar los términos y condiciones"

                        else -> {
                            isLoading = true
                            errorMessage = ""

                            val nameBody = name.toTextBody()
                            val dniBody = dni.toTextBody()
                            val dniDireccionBody = dniDireccion.toTextBody()
                            val dniProvinciaBody = dniProvincia.toTextBody()
                            val emailBody = email.toTextBody()
                            val phoneBody = phone.toTextBody()
                            val passwordBody = password.toTextBody()

                            val dniFrontPart = uriToMultipart(
                                context = context,
                                uri = dniFrontUri!!,
                                partName = "dni_front",
                                fileName = "dni_front_$dni.jpg"
                            )

                            val dniBackPart = uriToMultipart(
                                context = context,
                                uri = dniBackUri!!,
                                partName = "dni_back",
                                fileName = "dni_back_$dni.jpg"
                            )

                            val selfiePart = uriToMultipart(
                                context = context,
                                uri = selfieUri!!,
                                partName = "selfie",
                                fileName = "selfie_$dni.jpg"
                            )

                            RetrofitClient.instance.registerUser(
                                name = nameBody,
                                dni = dniBody,
                                dniDireccion = dniDireccionBody,
                                dniProvincia = dniProvinciaBody,
                                email = emailBody,
                                phone = phoneBody,
                                password = passwordBody,
                                dniFront = dniFrontPart,
                                dniBack = dniBackPart,
                                selfie = selfiePart
                            ).enqueue(object : Callback<RegisterResponse> {
                                override fun onResponse(
                                    call: Call<RegisterResponse>,
                                    response: Response<RegisterResponse>
                                ) {
                                    isLoading = false

                                    if (response.isSuccessful) {
                                        val result = response.body()

                                        if (result?.success == true) {
                                            val phoneToVerify = result.phone ?: phone

                                            if (result.requires_verification == true) {
                                                navController.navigate(
                                                    "verify_sms/${Uri.encode(phoneToVerify)}/${Uri.encode(name)}/${Uri.encode(email)}/${Uri.encode(dni)}"
                                                ) {
                                                    popUpTo("register") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("login") {
                                                    popUpTo("register") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            errorMessage = result?.message ?: "Error al registrar"
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        errorMessage =
                                            "Servidor ${response.code()}: ${errorBody ?: "Sin detalle"}"
                                    }
                                }

                                override fun onFailure(
                                    call: Call<RegisterResponse>,
                                    t: Throwable
                                ) {
                                    isLoading = false
                                    errorMessage = "Error: ${t.message}"
                                }
                            })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryButton,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Creando cuenta...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Crear cuenta", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta?",
                    fontSize = 13.sp,
                    color = colors.muted
                )

                TextButton(onClick = { navController.navigate("login") }) {
                    Text(
                        "Inicia sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTermsPopup) {
        TermsAndConditionsPopup(
            onAccept = {
                acceptedTerms = true
                showTermsPopup = false
            },
            onDismiss = {
                showTermsPopup = false
            }
        )
    }
}

@Composable
private fun TermsAndConditionsPopup(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = registerColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    text = "Términos y condiciones",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.text
                )

                Text(
                    text = "Perucho Courier Express",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted
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
                TermsItem("01", "Plazos y tiempos de entrega", "Los tiempos de entrega informados por Perucho Courier son estimados y pueden variar por tráfico, condiciones climáticas, restricciones de tránsito, disponibilidad de repartidores, volumen de pedidos u otras situaciones ajenas al control de la empresa. En pedidos internacionales, los plazos pueden verse afectados por procesos aduaneros, aerolíneas, operadores logísticos externos, tiendas de origen o autoridades competentes.")

                TermsItem("02", "Tarifas y pagos", "Las tarifas mostradas en la aplicación o sitio web son calculadas según distancia, tipo de vehículo, peso del paquete, destino, tipo de servicio y demás factores logísticos. Perucho Courier se reserva el derecho de actualizar tarifas, promociones, comisiones o condiciones de pago cuando lo considere necesario. El cliente deberá revisar el monto final antes de confirmar su pedido.")

                TermsItem("03", "Cancelaciones y reprogramaciones", "Si un pedido es cancelado después de haber sido confirmado, asignado a un repartidor o iniciado su proceso logístico, Perucho Courier podrá aplicar cargos operativos por tiempo, traslado, uso de recursos o gestión del pedido. Las reprogramaciones estarán sujetas a disponibilidad de horarios, rutas y repartidores.")

                TermsItem("04", "Tiempo de espera del repartidor", "El repartidor esperará un máximo de 10 minutos en el punto de recojo o entrega. Superado dicho tiempo, Perucho Courier podrá reprogramar el servicio, continuar con la ruta asignada o aplicar cargos adicionales según corresponda.")

                TermsItem("05", "Productos prohibidos", "Está prohibido enviar productos ilegales, peligrosos o restringidos por la legislación peruana: armas, municiones, explosivos, sustancias ilícitas, dinero en efectivo, animales vivos, mercancía robada, falsificada, inflamable, tóxica, corrosiva o peligrosa.")

                TermsItem("06", "Responsabilidad del cliente", "El cliente es responsable de proporcionar información verídica, completa y actualizada: direcciones, datos del remitente y destinatario, descripción real del paquete, peso, tamaño, tracking number y comprobantes requeridos.")

                TermsItem("07", "Pedidos internacionales", "En pedidos internacionales, Perucho Courier actúa como operador logístico e intermediario para facilitar la recepción, gestión y entrega de compras realizadas en tiendas del exterior. Los tiempos internacionales son referenciales.")

                TermsItem("08", "Tracking Number y compras internacionales", "El cliente es responsable de ingresar correctamente el Tracking Number de sus compras internacionales. Si ingresa un número incorrecto, incompleto o perteneciente a otro pedido, Perucho Courier no será responsable por demoras o confusiones.")

                TermsItem("09", "Impuestos, aduanas y gastos adicionales", "Cuando el valor declarado de una compra internacional supere los límites establecidos por SUNAT u otras autoridades, el cliente será responsable del pago de impuestos, tributos, aranceles, almacenaje, aforos u otros gastos aduaneros.")

                TermsItem("10", "Seguimiento y geolocalización", "La aplicación puede mostrar seguimiento en tiempo real mediante GPS. Esta información es referencial y puede presentar variaciones debido a señal del dispositivo, cobertura de red, permisos de ubicación o condiciones técnicas externas.")

                TermsItem("11", "Chat entre cliente y repartidor", "El sistema de mensajería debe utilizarse solo para coordinar aspectos relacionados con el servicio. Está prohibido usar lenguaje ofensivo, amenazas, acoso, solicitar servicios fuera de la plataforma o realizar actividades fraudulentas.")

                TermsItem("12", "Seguro de envío y cobertura", "Perucho Courier podrá ofrecer seguros o coberturas adicionales para determinados envíos. Las condiciones, costos, límites de cobertura, exclusiones y requisitos serán informados al cliente antes de contratar dicho servicio.")

                TermsItem("13", "Pérdidas, daños y embalaje", "El cliente es responsable de entregar los productos correctamente embalados, protegidos y aptos para transporte. Perucho Courier no será responsable por daños derivados de embalaje inadecuado o información incorrecta.")

                TermsItem("14", "Paquetes no reclamados o abandonados", "Los paquetes que permanezcan sin ser reclamados, coordinados o retirados por el cliente durante más de 30 días podrán ser considerados abandonados y generar cargos de almacenaje, gestión u otros costos operativos.")

                TermsItem("15", "Limitación de responsabilidad", "Perucho Courier no será responsable por retrasos, pérdidas, retenciones, daños, imposibilidad de entrega o costos adicionales ocasionados por información incorrecta, decisiones de autoridades, demoras externas, fallas GPS, internet o fuerza mayor.")

                TermsItem("16", "Fuerza mayor", "Perucho Courier no será responsable por incumplimientos o demoras ocasionadas por eventos fuera de su control razonable, tales como desastres naturales, huelgas, disturbios, cierre de vías, restricciones gubernamentales o emergencias.")
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryButton)
            ) {
                Text(
                    text = "Aceptar términos",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = PRojo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun TermsItem(
    number: String,
    title: String,
    body: String
) {
    val colors = registerColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            color = PRojo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(38.dp)
        )

        Column {
            Text(
                text = title,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = body,
                color = colors.muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun DniUploadBox(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = registerColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.uploadSelectedBg else colors.fieldBg
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.UploadFile,
                contentDescription = null,
                tint = if (selected) colors.uploadSelectedIcon else colors.muted,
                modifier = Modifier.size(26.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = colors.muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = registerColors()
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = colors.muted,
        letterSpacing = 0.6.sp
    )
}

@Composable
private fun RegField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val colors = registerColors()
    val topRadius = if (isFirst) 14.dp else 4.dp
    val bottomRadius = if (isLast) 14.dp else 4.dp

    val shape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = shape,
        placeholder = {
            Text(placeholder, color = colors.muted, fontSize = 14.sp)
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = colors.placeholder,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (isPassword && onTogglePasswordVisibility != null) {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Outlined.Visibility
                        else
                            Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = colors.placeholder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.fieldBg,
            unfocusedContainerColor = colors.fieldBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            cursorColor = colors.text
        )
    )

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider)
        )
    }
}

private fun String.toTextBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

private fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String,
    fileName: String
): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)

    val tempFile = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(tempFile)

    inputStream?.copyTo(outputStream)

    inputStream?.close()
    outputStream.close()

    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        partName,
        tempFile.name,
        requestFile
    )
}