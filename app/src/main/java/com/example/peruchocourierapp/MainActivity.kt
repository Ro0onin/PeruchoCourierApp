package com.example.peruchocourierapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.peruchocourierapp.screens.*
import com.example.peruchocourierapp.ui.theme.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    companion object {
        var pendingOpenChat by mutableStateOf(false)
        var pendingOrderId by mutableStateOf(0)
        var pendingReceiverEmail by mutableStateOf("")
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("open_chat", false) == true) {
            pendingOpenChat = true
            pendingOrderId = intent.getStringExtra("order_id")?.toIntOrNull() ?: 0
            pendingReceiverEmail = intent.getStringExtra("receiver_email") ?: ""

            Log.d(
                "NOTIFICATION_CLICK",
                "open_chat=true orderId=$pendingOrderId receiver=$pendingReceiverEmail"
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()
        val context = LocalContext.current
        val sessionManager = SessionManager(context)

        LaunchedEffect(
            pendingOpenChat,
            pendingOrderId,
            pendingReceiverEmail
        ) {
            if (
                pendingOpenChat &&
                pendingOrderId > 0 &&
                pendingReceiverEmail.isNotBlank()
            ) {
                navController.navigate(
                    "chat_pedido/$pendingOrderId/$pendingReceiverEmail"
                ) {
                    launchSingleTop = true
                }

                pendingOpenChat = false
            }
        }

        val startDestination = "splash"

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("login") {
                LoginScreen(navController)
            }

            composable("role_selection") {
                RoleSelectionScreen(navController)
            }

            composable("client_lobby") {
                ClientLobbyScreen(navController)
            }

            composable("driver_lobby") {
                DriverLobbyScreen(navController)
            }

            composable("crear_pedido") {
                PedidoNacionalScreen(navController)
            }

            composable("seguimiento_pedido") {
                SeguimientoPedidoScreen(navController)
            }

            composable("pedido_internacional") {
                PedidoInternacionalScreen(navController)
            }

            composable("pedido_nacional") {
                PedidoNacionalScreen(navController)
            }

            composable("mis_pedidos") {
                MisPedidosScreen(navController)
            }

            composable("pedido_en_curso/{driverEmail}") { backStackEntry ->
                val driverEmail = backStackEntry.arguments?.getString("driverEmail") ?: ""
                PedidoEnCursoScreen(
                    navController = navController,
                    driverEmailParam = driverEmail
                )
            }

            composable("perfil_cliente") {
                PerfilClienteScreen(navController)
            }

            composable("pedidos_disponibles") {
                PedidosDisponiblesScreen(navController)
            }

            composable("map_picker/{tipo}") { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                MapPickerScreen(
                    navController = navController,
                    tipo = tipo
                )
            }

            composable("mis_entregas") {
                MisEntregasScreen(navController)
            }

            composable("perfil_repartidor") {
                PerfilRepartidorScreen(navController)
            }

            composable("register") {
                RegisterScreen(navController)
            }

            composable("lobby") {
                LobbyScreen()
            }

            composable("seleccionar_pedido_seguimiento") {
                SeleccionarPedidoSeguimientoScreen(navController)
            }

            composable("seguimiento_cliente/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments
                    ?.getString("orderId")
                    ?.toIntOrNull() ?: 0

                SeguimientoPedidoClienteScreen(
                    navController = navController,
                    orderIdParam = orderId
                )
            }

            composable("verify_sms/{phone}/{name}/{email}/{dni}") { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val dni = backStackEntry.arguments?.getString("dni") ?: ""

                VerifySmsScreen(
                    navController = navController,
                    phoneParam = phone,
                    nameParam = name,
                    emailParam = email,
                    dniParam = dni
                )
            }

            composable("splash") {
                SplashScreen(navController)
            }

            composable("chat_pedido/{orderId}/{receiverEmail}") { backStackEntry ->
                val orderId = backStackEntry.arguments
                    ?.getString("orderId")
                    ?.toIntOrNull() ?: 0

                val receiverEmail = backStackEntry.arguments
                    ?.getString("receiverEmail") ?: ""

                ChatPedidoScreen(
                    navController = navController,
                    orderId = orderId,
                    receiverEmail = receiverEmail
                )
            }
            composable("completar_perfil_google/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""

                CompletarPerfilGoogleScreen(
                    navController = navController,
                    emailParam = email
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_TOKEN", "Error obteniendo token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM_TOKEN", token)
        }

        try {
            val appInfo = packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )

            val apiKey = appInfo.metaData
                ?.getString("com.google.android.geo.API_KEY")
                ?: "NO SE ENCONTRÓ"

            Log.d("MAPS_API_KEY", "API Key: $apiKey")

        } catch (e: Exception) {
            Log.e("MAPS_API_KEY", "Error obteniendo API Key", e)
        }

        setContent {
            PeruchoCourierAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_perucho2),
            contentDescription = "Logo Perucho Courier",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryRed
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }
}

@Composable
fun BaseScreen(titulo: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

