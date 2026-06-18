package com.example.peruchocourierapp.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun DriverHomeScreen(navController: NavController) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var acceptingOrderId by remember { mutableStateOf<Int?>(null) }

    fun cargarPedidosDisponibles() {
        isLoading = true
        errorMessage = ""

        RetrofitClient.instance.getAvailableOrders()
            .enqueue(object : Callback<GetOrdersResponse> {
                override fun onResponse(
                    call: Call<GetOrdersResponse>,
                    response: Response<GetOrdersResponse>
                ) {
                    isLoading = false

                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == true) {
                            orders = result.orders.filter {
                                it.tipo_envio == "nacional" || it.tipo_envio.isNullOrEmpty()
                            }
                        } else {
                            orders = emptyList()
                            errorMessage = "No se pudieron cargar los pedidos"
                        }
                    } else {
                        errorMessage = "Error del servidor: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<GetOrdersResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    fun aceptarPedido(order: Order) {
        val driverEmail = sessionManager.getUserEmail()
        val orderId = order.id

        if (driverEmail.isNullOrBlank()) {
            errorMessage = "No se encontró la sesión del repartidor"
            return
        }

        if (orderId == null) {
            errorMessage = "El pedido no tiene ID válido"
            return
        }

        acceptingOrderId = orderId
        errorMessage = ""

        RetrofitClient.instance.acceptOrder(orderId, driverEmail)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    acceptingOrderId = null

                    if (response.isSuccessful) {
                        val result = response.body()

                        if (result?.success == true) {
                            navController.navigate("pedido_en_curso")
                        } else {
                            errorMessage = result?.message ?: "No se pudo aceptar el pedido"
                        }
                    } else {
                        errorMessage = "Error del servidor: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    acceptingOrderId = null
                    errorMessage = "Error de conexión: ${t.message}"
                }
            })
    }

    LaunchedEffect(Unit) {
        cargarPedidosDisponibles()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pedidos disponibles",
                fontSize = 28.sp
            )
        }

        when {
            isLoading -> {
                item {
                    Text("Cargando pedidos...")
                }
            }

            errorMessage.isNotEmpty() -> {
                item {
                    Text(
                        text = errorMessage,
                        color = Color.Red
                    )
                }
            }

            orders.isEmpty() -> {
                item {
                    Text("No hay pedidos nacionales disponibles")
                }
            }

            else -> {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Pedido #${order.id ?: 0}",
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Cliente: ${order.user_email ?: "-"}")
                            Text("Recojo: ${order.pickup_address ?: order.origen ?: "-"}")
                            Text("Entrega: ${order.dropoff_address ?: order.destino ?: "-"}")
                            Text("Descripción: ${order.descripcion ?: "-"}")
                            Text("Distancia: ${order.distancia_km ?: "-"} km")
                            Text("Tamaño: ${order.tamano_paquete ?: "-"}")
                            Text("Método de pago: ${order.metodo_pago ?: "-"}")
                            Text("Total: S/ ${order.total ?: "-"}")
                            Text("Estado: ${order.estado ?: "-"}")

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { aceptarPedido(order) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = acceptingOrderId != order.id
                            ) {
                                Text(
                                    if (acceptingOrderId == order.id) {
                                        "Aceptando..."
                                    } else {
                                        "Aceptar pedido"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { cargarPedidosDisponibles() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar lista")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}