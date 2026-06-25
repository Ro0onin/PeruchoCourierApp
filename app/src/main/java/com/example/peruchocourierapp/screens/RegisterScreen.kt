    package com.example.peruchocourierapp.screens

    import android.content.Context
    import android.net.Uri
    import android.util.Patterns
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.background
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

    private val PNegro = Color(0xFF1A1A1A)
    private val PRojo = Color(0xFFE02020)
    private val PGrisF = Color(0xFFF5F5F5)
    private val PGrisBorde = Color(0xFFE8E8E8)
    private val PTextoSub = Color(0xFF888888)

    @Composable
    fun RegisterScreen(navController: NavController) {

        val context = LocalContext.current

        var name by remember { mutableStateOf("") }
        var dni by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var acceptedTerms by remember { mutableStateOf(false) }

        var dniFrontUri by remember { mutableStateOf<Uri?>(null) }
        var dniBackUri by remember { mutableStateOf<Uri?>(null) }

        var errorMessage by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        val dniFrontLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            dniFrontUri = uri
        }

        val dniBackLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            dniBackUri = uri
        }

        val isSuccess = errorMessage.contains("correctamente", ignoreCase = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PNegro)
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

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PERUCHO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PRojo
                    )
                    Text(
                        text = "COURIER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    "Crear cuenta",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = PNegro
                )
                Text(
                    "Regístrate para empezar a enviar",
                    fontSize = 14.sp,
                    color = PTextoSub,
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
                    subtitle = if (dniFrontUri != null) "Imagen seleccionada" else "Sube una foto clara del frente del DNI",
                    selected = dniFrontUri != null,
                    onClick = { dniFrontLauncher.launch("image/*") }
                )

                Spacer(Modifier.height(10.dp))

                DniUploadBox(
                    title = "DNI parte posterior",
                    subtitle = if (dniBackUri != null) "Imagen seleccionada" else "Sube una foto clara de la parte posterior",
                    selected = dniBackUri != null,
                    onClick = { dniBackLauncher.launch("image/*") }
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
                    color = PGrisF
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { acceptedTerms = !acceptedTerms }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            modifier = Modifier.size(22.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = PNegro,
                                uncheckedColor = PGrisBorde,
                                checkmarkColor = Color.White
                            )
                        )

                        Column {
                            Text(
                                "Aceptar términos y condiciones",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PNegro
                            )
                            Text(
                                "Al continuar aceptas nuestra política de privacidad",
                                fontSize = 11.sp,
                                color = PTextoSub
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFFF0F0)
                    ) {
                        Text(
                            text = errorMessage,
                            color = if (isSuccess) Color(0xFF16A34A) else PRojo,
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

                            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                errorMessage = "Correo electrónico inválido"

                            dni.length < 8 ->
                                errorMessage = "Documento inválido"

                            phone.length != 9 ->
                                errorMessage = "El teléfono debe tener 9 dígitos"

                            dniFrontUri == null ->
                                errorMessage = "Debes subir la foto del DNI delantero"

                            dniBackUri == null ->
                                errorMessage = "Debes subir la foto del DNI posterior"

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

                                RetrofitClient.instance.registerUser(
                                    name = nameBody,
                                    dni = dniBody,
                                    email = emailBody,
                                    phone = phoneBody,
                                    password = passwordBody,
                                    dniFront = dniFrontPart,
                                    dniBack = dniBackPart
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
                                            errorMessage = "Error del servidor"
                                        }
                                    }

                                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
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
                        containerColor = PNegro,
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
                        color = PTextoSub
                    )
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text(
                            "Inicia sesión",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PNegro
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(14.dp),
            color = if (selected) Color(0xFFEAFBF0) else PGrisF
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.UploadFile,
                    contentDescription = null,
                    tint = if (selected) Color(0xFF16A34A) else PTextoSub,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = PNegro,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = PTextoSub,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PTextoSub,
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
                Text(placeholder, color = PTextoSub, fontSize = 14.sp)
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = PNegro,
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
                    tint = PTextoSub,
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
                            tint = PTextoSub,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = PGrisF,
                unfocusedContainerColor = PGrisF,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = PNegro,
                unfocusedTextColor = PNegro,
                cursorColor = PNegro
            )
        )

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PGrisBorde)
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