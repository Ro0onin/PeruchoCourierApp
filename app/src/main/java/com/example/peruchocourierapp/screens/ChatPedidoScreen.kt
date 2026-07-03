package com.example.peruchocourierapp.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.CallContactsResponse
import com.example.peruchocourierapp.models.ChatMessage
import com.example.peruchocourierapp.models.GetChatMessagesResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.math.pow

private val CNegro = Color(0xFF111111)
private val CAzul = Color(0xFF1A4FBF)
private val CAzulOscuro = Color(0xFF0D3280)
private val CRojo = Color(0xFFE02020)
private val CRojoOscuro = Color(0xFFB91C1C)
private val CVerde = Color(0xFF22C55E)
private val CBlancoMsg = Color(0xFFFFFFFF)
private val CGrisFondo = Color(0xFFF5F5F5)
private val CGrisBorde = Color(0xFFF0F0F0)
private val CMuted = Color(0xFF888888)

private enum class TipoBurbuja {
    ENVIADO,
    RECIBIDO
}

private data class ChatMensajeUi(
    val id: Int,
    val tipo: TipoBurbuja,
    val texto: String,
    val hora: String,
    val senderNombre: String,
    val senderInitials: String,
    val leido: Boolean,
    val imagenUrl: String? = null
)

@Composable
fun ChatPedidoScreen(
    navController: NavController,
    orderId: Int,
    receiverEmail: String
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val myEmail = sessionManager.getUserEmail()?.trim().orEmpty()
    val myName = sessionManager.getUserName()?.trim().orEmpty().ifBlank { "Yo" }

    val receiverEmailDecoded = remember(receiverEmail) {
        Uri.decode(receiverEmail).trim()
    }

    val myRole = sessionManager.getUserRole()?.trim()?.lowercase().orEmpty()

    var mensajes by remember { mutableStateOf<List<ChatMensajeUi>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    var chatBloqueado by remember { mutableStateOf(false) }
    var estadoPedido by remember { mutableStateOf("asignado") }
    var driverPhone by remember { mutableStateOf("") }
    var telefonoRemitente by remember { mutableStateOf("") }
    var telefonoDestinatario by remember { mutableStateOf("") }
    var showCallMenu by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }

    fun mapearMensaje(msg: ChatMessage): ChatMensajeUi {
        val isMine = msg.sender_email.trim().equals(myEmail, ignoreCase = true)

        val nombre = if (isMine) {
            myName
        } else {
            msg.sender_email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        val imagenUrl = if (msg.mensaje.startsWith("[imagen]")) {
            val ruta = msg.mensaje.removePrefix("[imagen]").trim()

            when {
                ruta.startsWith("http") -> ruta
                ruta.startsWith("uploads/") -> "https://peruchocourier.com/perucho_api/$ruta"
                else -> "https://peruchocourier.com/perucho_api/uploads/chat/$ruta"
            }
        } else {
            null
        }

        return ChatMensajeUi(
            id = msg.id,
            tipo = if (isMine) TipoBurbuja.ENVIADO else TipoBurbuja.RECIBIDO,
            texto = if (imagenUrl != null) "" else msg.mensaje,
            hora = formatearHoraChat(msg.created_at),
            senderNombre = if (isMine) "Tú" else nombre,
            senderInitials = obtenerInicialesChat(nombre),
            leido = msg.leido == 1,
            imagenUrl = imagenUrl
        )
    }

    fun marcarComoLeido() {
        if (myEmail.isBlank() || orderId <= 0) return

        RetrofitClient.instance.markChatRead(
            orderId = orderId,
            userEmail = myEmail
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {}
            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {}
        })
    }

    fun cargarEstadoPedido() {
        if (orderId <= 0 || myEmail.isBlank()) return

        RetrofitClient.instance.getOrderTracking(
            orderId = orderId,
            userEmail = myEmail
        ).enqueue(object : Callback<ActiveOrderResponse> {
            override fun onResponse(
                call: Call<ActiveOrderResponse>,
                response: Response<ActiveOrderResponse>
            ) {
                val estado = extraerEstadoPedido(response.body())

                if (estado.isNotBlank()) {
                    estadoPedido = estado

                    val estadoNormalizado = estado.lowercase().trim()

                    chatBloqueado =
                        estadoNormalizado == "entregado" ||
                                estadoNormalizado == "completado" ||
                                estadoNormalizado == "finalizado"
                }
            }

            override fun onFailure(call: Call<ActiveOrderResponse>, t: Throwable) {}
        })
    }

    fun cargarMensajes() {
        if (orderId <= 0) {
            isLoading = false
            errorMsg = "Pedido inválido"
            return
        }

        RetrofitClient.instance.getChatMessages(orderId)
            .enqueue(object : Callback<GetChatMessagesResponse> {
                override fun onResponse(
                    call: Call<GetChatMessagesResponse>,
                    response: Response<GetChatMessagesResponse>
                ) {
                    isLoading = false
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        mensajes = result.messages.map { mapearMensaje(it) }
                        errorMsg = ""
                        marcarComoLeido()
                    } else {
                        errorMsg = result?.message ?: "No se pudieron cargar los mensajes"
                    }
                }

                override fun onFailure(call: Call<GetChatMessagesResponse>, t: Throwable) {
                    isLoading = false
                    errorMsg = "Sin conexión"
                }
            })
    }

    fun enviarMensaje() {
        val texto = inputText.trim()

        if (chatBloqueado) {
            errorMsg = "Este chat está cerrado porque el pedido fue entregado"
            return
        }

        if (texto.isBlank() || isSending) return

        if (myEmail.isBlank()) {
            errorMsg = "Sesión inválida"
            return
        }

        if (receiverEmailDecoded.isBlank()) {
            errorMsg = "No se encontró el receptor del chat"
            return
        }

        isSending = true
        inputText = ""
        keyboard?.hide()

        RetrofitClient.instance.sendChatMessage(
            orderId = orderId,
            senderEmail = myEmail,
            receiverEmail = receiverEmailDecoded,
            mensaje = texto
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                isSending = false
                val result = response.body()

                if (response.isSuccessful && result?.success == true) {
                    errorMsg = ""
                    cargarMensajes()
                    cargarEstadoPedido()
                } else {
                    errorMsg = result?.message ?: "No se pudo enviar"
                    cargarEstadoPedido()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                isSending = false
                errorMsg = "No se pudo enviar"
                cargarEstadoPedido()
            }
        })
    }

    fun subirFotoChat(uri: Uri) {
        if (chatBloqueado) return

        try {
            val tempFile = File(
                context.cacheDir,
                "upload_chat_${orderId}_${System.currentTimeMillis()}.jpg"
            )

            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                errorMsg = "No se pudo leer la foto"
                return
            }

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val fotoPart = MultipartBody.Part.createFormData(
                "foto_chat",
                tempFile.name,
                requestFile
            )

            val orderBody = orderId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val senderBody = myEmail.toRequestBody("text/plain".toMediaTypeOrNull())
            val receiverBody = receiverEmailDecoded.toRequestBody("text/plain".toMediaTypeOrNull())

            isSending = true

            RetrofitClient.instance.sendChatPhoto(
                orderBody,
                senderBody,
                receiverBody,
                fotoPart
            ).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    isSending = false

                    if (response.isSuccessful && response.body()?.success == true) {
                        errorMsg = ""
                        cargarMensajes()
                    } else {
                        errorMsg = response.body()?.message ?: "No se pudo subir la foto"
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    isSending = false
                    errorMsg = t.message ?: "No se pudo subir la foto"
                }
            })

        } catch (e: Exception) {
            isSending = false
            errorMsg = e.message ?: "Error al procesar la foto"
        }
    }

    fun crearUriFotoChat(): Uri {
        val file = File.createTempFile(
            "chat_${orderId}_",
            ".jpg",
            context.cacheDir
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            subirFotoChat(photoUri!!)
        } else {
            errorMsg = "No se tomó ninguna foto"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = crearUriFotoChat()
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            errorMsg = "Permiso de cámara denegado"
        }
    }

    fun abrirCamaraChat() {
        if (chatBloqueado) {
            errorMsg = "Este chat está cerrado porque el pedido fue entregado"
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uri = crearUriFotoChat()
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun llamar(numero: String) {
        val numeroLimpio = numero.trim()

        if (numeroLimpio.isBlank()) {
            errorMsg = "Número no disponible"
            return
        }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numeroLimpio")
        }

        context.startActivity(intent)
    }

    fun cargarContactosLlamada() {
        if (orderId <= 0) return

        RetrofitClient.instance.getOrderCallContacts(orderId)
            .enqueue(object : Callback<CallContactsResponse> {
                override fun onResponse(
                    call: Call<CallContactsResponse>,
                    response: Response<CallContactsResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        driverPhone = result.driver_phone.orEmpty()
                        telefonoRemitente = result.telefono_remitente.orEmpty()
                        telefonoDestinatario = result.telefono_destinatario.orEmpty()
                    }
                }

                override fun onFailure(call: Call<CallContactsResponse>, t: Throwable) {}
            })
    }

    LaunchedEffect(orderId) {
        cargarMensajes()
        cargarEstadoPedido()
        cargarContactosLlamada()

        while (true) {
            delay(3000)
            cargarMensajes()
            cargarEstadoPedido()
            cargarContactosLlamada()
        }
    }

    LaunchedEffect(mensajes.size, chatBloqueado) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        ChatTopBar(
            orderInfo = "Pedido #$orderId",
            receiverEmail = receiverEmailDecoded,
            myName = myName,
            showCallButton = true,
            onBack = { navController.popBackStack() },
            onCallClick = { showCallMenu = true }
        )

        ChatBanner(orderInfo = "Pedido #$orderId")

        Box(
            modifier = Modifier
                .weight(1f)
                .background(CGrisFondo)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = CAzul
                    )
                }

                mensajes.isEmpty() -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            EstadoPedidoChatBubble(
                                texto = if (chatBloqueado) "Pedido entregado" else "Repartidor asignado",
                                subtitulo = if (chatBloqueado) "Chat cerrado" else "Pedido en curso"
                            )
                        }

                        if (chatBloqueado) {
                            item { ChatCerradoMensaje() }
                        } else {
                            item {
                                EmptyChatState(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 80.dp)
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            EstadoPedidoChatBubble(
                                texto = if (chatBloqueado) "Pedido entregado" else "Repartidor asignado",
                                subtitulo = if (chatBloqueado) "Chat cerrado" else "Pedido en curso"
                            )
                        }

                        items(
                            items = mensajes,
                            key = { it.id }
                        ) { msg ->
                            when (msg.tipo) {
                                TipoBurbuja.ENVIADO -> MensajeEnviado(
                                    msg = msg,
                                    onImageClick = { imagenSeleccionada = it }
                                )

                                TipoBurbuja.RECIBIDO -> MensajeRecibido(
                                    msg = msg,
                                    onImageClick = { imagenSeleccionada = it }
                                )
                            }
                        }

                        if (chatBloqueado) {
                            item { ChatCerradoMensaje() }
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = errorMsg.isNotBlank(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CRojo,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = errorMsg,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (chatBloqueado) {
            ChatClosedBar()
        } else {
            ChatInputBar(
                value = inputText,
                onChange = { inputText = it },
                isSending = isSending,
                onSend = { enviarMensaje() },
                onCameraClick = { abrirCamaraChat() }
            )
        }
    }

    if (showCallMenu) {
        CallContactsDialog(
            isDriver = myRole == "repartidor",
            driverPhone = driverPhone,
            telefonoRemitente = telefonoRemitente,
            telefonoDestinatario = telefonoDestinatario,
            onDismiss = { showCallMenu = false },
            onCall = { numero ->
                showCallMenu = false
                llamar(numero)
            }
        )
    }

    if (imagenSeleccionada != null) {
        ImagenZoomDialog(
            imageUrl = imagenSeleccionada!!,
            onDismiss = { imagenSeleccionada = null }
        )
    }
}

@Composable
private fun ImagenZoomDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember(imageUrl) { mutableStateOf(1f) }
    var offset by remember(imageUrl) { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen ampliada",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(imageUrl) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 3.2f

                                    val center = Offset(
                                        size.width / 2f,
                                        size.height / 2f
                                    )

                                    offset = (center - tapOffset) * (scale - 1f)
                                }
                            }
                        )
                    }
                    .pointerInput(imageUrl) {
                        detectTransformGestures { _, pan, zoom, _ ->

                            val zoomSuave = zoom.toDouble()
                                .pow(2.2)
                                .toFloat()

                            val newScale = (scale * zoomSuave)
                                .coerceIn(1f, 8f)

                            offset = if (newScale <= 1f) {
                                Offset.Zero
                            } else {
                                offset + (pan * 2.2f)
                            }

                            scale = newScale
                        }
                    },
                contentScale = ContentScale.Fit
            )

            if (scale > 1f) {
                TextButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = "Restablecer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }
    }
}
@Composable
private fun ChatTopBar(
    orderInfo: String,
    receiverEmail: String,
    myName: String,
    showCallButton: Boolean,
    onBack: () -> Unit,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(CGrisFondo),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = CNegro,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chat del pedido",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CNegro
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CVerde)
                )

                Text(
                    text = orderInfo,
                    fontSize = 11.sp,
                    color = CMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showCallButton) {
            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8FFF1))
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Llamar",
                    tint = CVerde,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .width(50.dp)
                .height(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CAzulOscuro, CAzul)))
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = obtenerInicialesChat(receiverEmail.substringBefore("@")),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CRojoOscuro, CRojo))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = obtenerInicialesChat(myName),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }

    HorizontalDivider(
        color = CGrisBorde,
        thickness = 1.dp
    )
}

@Composable
private fun MensajeEnviado(
    msg: ChatMensajeUi,
    onImageClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(Brush.linearGradient(listOf(CRojo, CRojoOscuro)))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .widthIn(max = 240.dp)
            ) {
                if (msg.imagenUrl != null) {
                    AsyncImage(
                        model = msg.imagenUrl,
                        contentDescription = "Imagen del chat",
                        modifier = Modifier
                            .width(210.dp)
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onImageClick(msg.imagenUrl) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = msg.texto,
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(top = 3.dp, end = 2.dp)
            ) {
                Text(text = msg.hora, fontSize = 10.sp, color = CMuted)

                Text(
                    text = if (msg.leido) "✓✓" else "✓",
                    fontSize = 10.sp,
                    color = if (msg.leido) CVerde else CMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(CRojoOscuro, CRojo))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = msg.senderInitials.ifBlank { "YO" },
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MensajeRecibido(
    msg: ChatMensajeUi,
    onImageClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(CAzulOscuro, CAzul))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = msg.senderInitials.ifBlank { "PC" },
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            if (msg.senderNombre.isNotBlank()) {
                Text(
                    text = msg.senderNombre,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CMuted,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = CBlancoMsg,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                if (msg.imagenUrl != null) {
                    AsyncImage(
                        model = msg.imagenUrl,
                        contentDescription = "Imagen del chat",
                        modifier = Modifier
                            .padding(8.dp)
                            .width(210.dp)
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onImageClick(msg.imagenUrl) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = msg.texto,
                        fontSize = 13.sp,
                        color = CNegro,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }

            Text(
                text = msg.hora,
                fontSize = 10.sp,
                color = CMuted,
                modifier = Modifier.padding(top = 3.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun CallContactsDialog(
    isDriver: Boolean,
    driverPhone: String,
    telefonoRemitente: String,
    telefonoDestinatario: String,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = CVerde,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Llamar",
                    color = CNegro,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isDriver) {
                    if (telefonoRemitente.isNotBlank()) {
                        CallContactOption(
                            title = "Remitente",
                            phone = telefonoRemitente,
                            onClick = { onCall(telefonoRemitente) }
                        )
                    }

                    if (telefonoDestinatario.isNotBlank()) {
                        CallContactOption(
                            title = "Destinatario",
                            phone = telefonoDestinatario,
                            onClick = { onCall(telefonoDestinatario) }
                        )
                    }

                    if (telefonoRemitente.isBlank() && telefonoDestinatario.isBlank()) {
                        Text(
                            text = "No hay números disponibles para este pedido.",
                            color = CMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    if (driverPhone.isNotBlank()) {
                        CallContactOption(
                            title = "Repartidor",
                            phone = driverPhone,
                            onClick = { onCall(driverPhone) }
                        )
                    } else {
                        Text(
                            text = "Aún no hay número disponible del repartidor.",
                            color = CMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = CAzul,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun CallContactOption(
    title: String,
    phone: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF6FFF9),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFD5F5DF)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CVerde),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CNegro,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = phone,
                    color = CMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = CVerde,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChatBanner(orderInfo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8EFFE))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            tint = CAzul,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = orderInfo,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CAzul
        )
    }
}

@Composable
private fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💬",
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aún no hay mensajes",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CMuted
        )

        Text(
            text = "Escribe para iniciar la conversación",
            fontSize = 12.sp,
            color = CMuted,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
    onCameraClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EFFE))
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Tomar foto",
                    tint = CAzul,
                    modifier = Modifier.size(22.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CGrisFondo,
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = value,
                    onValueChange = onChange,
                    placeholder = {
                        Text(
                            text = "Escribe un mensaje...",
                            fontSize = 13.sp,
                            color = Color(0xFFBBBBBB)
                        )
                    },
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CGrisFondo,
                        unfocusedContainerColor = CGrisFondo,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = CNegro,
                        unfocusedTextColor = CNegro,
                        cursorColor = CAzul
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val sendButtonBrush = if (value.isBlank() || isSending) {
                Brush.linearGradient(listOf(CGrisFondo, CGrisFondo))
            } else {
                Brush.linearGradient(listOf(CAzul, CAzulOscuro))
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(sendButtonBrush),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CAzul
                    )
                } else {
                    IconButton(
                        onClick = onSend,
                        enabled = value.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (value.isBlank()) CMuted else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatClosedBar() {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFFF1F1))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = CRojo,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Chat cerrado",
                    color = CRojo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "El pedido ya fue entregado. No se pueden enviar más mensajes.",
                    color = CMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ChatCerradoMensaje() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFEFEFEF)
        ) {
            Text(
                text = "Pedido finalizado. Este chat quedó solo para lectura.",
                color = CMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private fun formatearHoraChat(fecha: String): String {
    if (fecha.length >= 16) {
        return fecha.substring(11, 16)
    }

    return fecha
}

private fun obtenerInicialesChat(texto: String): String {
    val limpio = texto.trim()

    if (limpio.isBlank()) return "PC"

    val partes = limpio
        .replace(".", " ")
        .replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }

    return partes
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank {
            limpio.take(2).uppercase()
        }
}

private fun extraerEstadoPedido(response: ActiveOrderResponse?): String {
    if (response == null) return ""

    return try {
        val json = Gson().toJsonTree(response).asJsonObject

        json.getStringSafe("estado")
            ?: json.getStringSafe("status")
            ?: json.getObjectSafe("pedido")?.getStringSafe("estado")
            ?: json.getObjectSafe("order")?.getStringSafe("estado")
            ?: json.getObjectSafe("data")?.getStringSafe("estado")
            ?: json.getObjectSafe("envio")?.getStringSafe("estado")
            ?: ""
    } catch (e: Exception) {
        ""
    }
}

private fun JsonObject.getStringSafe(key: String): String? {
    return try {
        if (has(key) && !get(key).isJsonNull) {
            get(key).asString
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun JsonObject.getObjectSafe(key: String): JsonObject? {
    return try {
        if (has(key) && get(key).isJsonObject) {
            get(key).asJsonObject
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun EstadoPedidoChatBubble(
    texto: String,
    subtitulo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = texto,
                        color = CNegro,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = subtitulo,
                        color = CMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
