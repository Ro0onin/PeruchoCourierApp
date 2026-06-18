package com.example.peruchocourierapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.peruchocourierapp.ui.theme.*
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.peruchocourierapp.screens.LobbyScreen
import com.example.peruchocourierapp.screens.RoleSelectionScreen
import com.example.peruchocourierapp.screens.ClientLobbyScreen
import com.example.peruchocourierapp.screens.DriverLobbyScreen
import com.example.peruchocourierapp.screens.RegisterScreen
import com.example.peruchocourierapp.screens.MisPedidosScreen
import com.example.peruchocourierapp.screens.PedidosDisponiblesScreen
import com.example.peruchocourierapp.screens.PedidoEnCursoScreen
import com.example.peruchocourierapp.screens.LoginScreen
import com.example.peruchocourierapp.screens.PedidoInternacionalScreen
import com.example.peruchocourierapp.screens.PedidoNacionalScreen
import com.example.peruchocourierapp.screens.MapPickerScreen
import com.example.peruchocourierapp.screens.SeguimientoPedidoScreen
import com.example.peruchocourierapp.screens.PerfilClienteScreen
import com.example.peruchocourierapp.screens.PerfilRepartidorScreen
import com.example.peruchocourierapp.screens.MisEntregasScreen
import android.content.pm.PackageManager
import android.util.Log
import com.example.peruchocourierapp.screens.SeguimientoPedidoClienteScreen
import com.example.peruchocourierapp.screens.SeleccionarPedidoSeguimientoScreen
import com.example.peruchocourierapp.screens.SplashScreen
import com.example.peruchocourierapp.screens.VerifySmsScreen


class MainActivity : ComponentActivity() {

    @Composable
    fun AppNavigation() {

        val navController = rememberNavController()
        val context = LocalContext.current
        val sessionManager = SessionManager(context)

        var isLoggedIn by remember {
            mutableStateOf(sessionManager.isLoggedIn())
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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // 🔹 LOGO
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
            onClick = {navController.navigate("register")},
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
