package com.example.peruchocourierapp.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.R
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.LoginResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val BlueDark = Color(0xFF0D3280)
private val BluePrimary = Color(0xFF1A4FBF)
private val BlueMid = Color(0xFF2D6BE4)
private val RedPrimary = Color(0xFFE02020)
private val GrayBg = Color(0xFFF4F6FB)
private val GrayBorder = Color(0xFFE8ECF4)
private val GrayText = Color(0xFF6B7A99)
private val GrayPlaceholder = Color(0xFFB0BAD0)
private val DarkText = Color(0xFF1A2340)

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val firebaseAuth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    fun guardarTokenFcm(userEmail: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                RetrofitClient.instance.saveFcmToken(userEmail, token)
                    .enqueue(object : Callback<BasicResponse> {
                        override fun onResponse(
                            call: Call<BasicResponse>,
                            response: Response<BasicResponse>
                        ) {
                            Log.d("FCM", "Token guardado: ${response.body()?.message}")
                        }

                        override fun onFailure(
                            call: Call<BasicResponse>,
                            t: Throwable
                        ) {
                            Log.e("FCM", "Error guardando token", t)
                        }
                    })
            }
    }

    val googleSignInClient = remember(context) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode != Activity.RESULT_OK) {
            isGoogleLoading = false
            errorMessage = "Inicio con Google cancelado o no completado"
            return@rememberLauncherForActivityResult
        }

        isGoogleLoading = true
        errorMessage = ""

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        task.addOnSuccessListener { account ->
            val idToken = account.idToken

            if (idToken.isNullOrEmpty()) {
                isGoogleLoading = false
                errorMessage = "No se obtuvo el token de Google"
                return@addOnSuccessListener
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val user = firebaseAuth.currentUser
                    val userEmail = user?.email ?: ""

                    if (userEmail.isBlank()) {
                        isGoogleLoading = false
                        errorMessage = "No se pudo obtener el correo de Google"
                        return@addOnSuccessListener
                    }

                    RetrofitClient.instance.googleLogin(
                        email = userEmail,
                        name = user?.displayName ?: "",
                        googleUid = user?.uid ?: ""
                    ).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            isGoogleLoading = false
                            val result = response.body()

                            if (response.isSuccessful && result?.success == true) {
                                val phoneDb = result.phone ?: ""
                                val dniDb = result.dni ?: ""
                                val roleDb = result.role ?: "cliente"
                                val nameDb = result.name ?: user?.displayName ?: ""

                                when {
                                    phoneDb.isBlank() || dniDb.isBlank() -> {
                                        navController.navigate(
                                            "completar_perfil_google/${Uri.encode(userEmail)}"
                                        ) {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }

                                    result.phone_verified != 1 -> {
                                        navController.navigate(
                                            "verify_sms/${Uri.encode(phoneDb)}/${Uri.encode(nameDb)}/${Uri.encode(userEmail)}/${Uri.encode(dniDb)}"
                                        ) {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }

                                    else -> {
                                        sessionManager.saveUserSession(
                                            name = nameDb,
                                            email = userEmail,
                                            phone = phoneDb,
                                            role = roleDb,
                                            dni = dniDb
                                        )

                                        guardarTokenFcm(userEmail)

                                        if (roleDb == "repartidor") {
                                            navController.navigate("driver_lobby") {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate("client_lobby") {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            } else {
                                errorMessage = result?.message ?: "No se pudo validar la cuenta Google"
                            }
                        }

                        override fun onFailure(
                            call: Call<LoginResponse>,
                            t: Throwable
                        ) {
                            isGoogleLoading = false
                            errorMessage = "Error conectando con servidor: ${t.message}"
                        }
                    })
                }
                .addOnFailureListener { e ->
                    isGoogleLoading = false
                    errorMessage = "Firebase Auth error: ${e.message}"
                    Log.e("GOOGLE_LOGIN", "Firebase error", e)
                }
        }

        task.addOnFailureListener { e ->
            isGoogleLoading = false
            errorMessage = "Google Sign-In error: ${e.message}"
            Log.e("GOOGLE_LOGIN", "Google error", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(245.dp)
                .background(
                    Brush.linearGradient(
                        listOf(BlueDark, BluePrimary, BlueMid)
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-45).dp, y = 55.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 38.dp, y = (-26).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.055f))
            )

            Image(
                painter = painterResource(id = R.drawable.logo_perucho2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .padding(horizontal = 24.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido!",
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Inicia sesión para continuar",
                color = GrayText,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            PeruchoTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = { email = it },
                placeholder = "correo@ejemplo.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = {
                    Icon(Icons.Outlined.Email, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PeruchoTextField(
                label = "Contraseña",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = GrayPlaceholder)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¿Olvidé mi contraseña?",
                color = BluePrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("forgot_password")
                    },
                textAlign = TextAlign.End
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    color = RedPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()

                    when {
                        cleanEmail.isEmpty() || cleanPassword.isEmpty() -> {
                            errorMessage = "Completa todos los campos"
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> {
                            errorMessage = "Correo electrónico inválido"
                        }

                        else -> {
                            errorMessage = ""
                            isLoading = true

                            RetrofitClient.instance.loginUser(
                                cleanEmail,
                                cleanPassword
                            ).enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(
                                    call: Call<LoginResponse>,
                                    response: Response<LoginResponse>
                                ) {
                                    isLoading = false
                                    val result = response.body()

                                    if (response.isSuccessful && result?.success == true) {
                                        val userEmail = result.email ?: cleanEmail
                                        val roleDb = result.role ?: "cliente"

                                        sessionManager.saveUserSession(
                                            name = result.name ?: "",
                                            email = userEmail,
                                            phone = result.phone ?: "",
                                            role = roleDb,
                                            dni = result.dni ?: ""
                                        )

                                        guardarTokenFcm(userEmail)

                                        if (roleDb == "repartidor") {
                                            navController.navigate("driver_lobby") {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate("client_lobby") {
                                                popUpTo("login") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    } else if (response.isSuccessful && result?.requires_verification == true) {
                                        navController.navigate(
                                            "verify_sms/${Uri.encode(result.phone ?: "")}/${Uri.encode(result.name ?: "")}/${Uri.encode(result.email ?: cleanEmail)}/${Uri.encode(result.dni ?: "")}"
                                        ) {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = result?.message ?: "Credenciales incorrectas"
                                    }
                                }

                                override fun onFailure(
                                    call: Call<LoginResponse>,
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
                    .height(54.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedPrimary,
                    contentColor = Color.White
                ),
                enabled = !isLoading && !isGoogleLoading
            ) {
                Icon(Icons.Outlined.Login, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "Ingresando..." else "Iniciar Sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DividerWithText()

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    isGoogleLoading = true
                    errorMessage = ""

                    firebaseAuth.signOut()

                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, GrayBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF202124)
                ),
                enabled = !isLoading && !isGoogleLoading
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isGoogleLoading)
                        "Conectando con Google..."
                    else
                        "Continuar con Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿No tienes cuenta?",
                color = GrayText,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, BluePrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = BluePrimary
                )
            ) {
                Icon(Icons.Outlined.PersonAdd, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Crear cuenta nueva",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PeruchoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = GrayText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(13.dp),
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = null,
                            tint = GrayPlaceholder
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = GrayPlaceholder,
                    fontSize = 14.sp
                )
            },
            textStyle = TextStyle(
                color = DarkText,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrayBg,
                unfocusedContainerColor = GrayBg,
                disabledContainerColor = GrayBg,
                focusedIndicatorColor = GrayBorder,
                unfocusedIndicatorColor = GrayBorder,
                cursorColor = BluePrimary,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText
            )
        )
    }
}

@Composable
private fun DividerWithText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GrayBorder)
        )

        Text(
            text = "o",
            color = GrayPlaceholder,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GrayBorder)
        )
    }
}