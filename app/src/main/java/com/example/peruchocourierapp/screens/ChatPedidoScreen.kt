package com.example.peruchocourierapp.screens

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.peruchocourierapp.SessionManager
import com.example.peruchocourierapp.api.RetrofitClient
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.ChatMessage
import com.example.peruchocourierapp.models.GetChatMessagesResponse
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    val leido: Boolean
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

    var mensajes by remember { mutableStateOf<List<ChatMensajeUi>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }

    fun mapearMensaje(msg: ChatMessage): ChatMensajeUi {
        val isMine = msg.sender_email.trim().equals(myEmail, ignoreCase = true)
        val nombre = if (isMine) {
            myName
        } else {
            msg.sender_email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        return ChatMensajeUi(
            id = msg.id,
            tipo = if (isMine) TipoBurbuja.ENVIADO else TipoBurbuja.RECIBIDO,
            texto = msg.mensaje,
            hora = formatearHoraChat(msg.created_at),
            senderNombre = if (isMine) "Tú" else nombre,
            senderInitials = obtenerInicialesChat(nombre),
            leido = msg.leido == 1
        )
    }

    fun marcarComoLeido() {
        if (myEmail.isBlank() || orderId <= 0) return

        RetrofitClient.instance.markChatRead(
            orderId = orderId,
            userEmail = myEmail
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {}

            override fun onFailure(
                call: Call<BasicResponse>,
                t: Throwable
            ) {}
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

                override fun onFailure(
                    call: Call<GetChatMessagesResponse>,
                    t: Throwable
                ) {
                    isLoading = false
                    errorMsg = "Sin conexión"
                }
            })
    }

    fun enviarMensaje() {
        val texto = inputText.trim()

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
                } else {
                    errorMsg = result?.message ?: "No se pudo enviar"
                }
            }

            override fun onFailure(
                call: Call<BasicResponse>,
                t: Throwable
            ) {
                isSending = false
                errorMsg = "No se pudo enviar"
            }
        })
    }

    LaunchedEffect(orderId) {
        cargarMensajes()

        while (true) {
            delay(3000)
            cargarMensajes()
        }
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.lastIndex)
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
            onBack = { navController.popBackStack() }
        )

        ChatBanner(orderInfo = "Pedido #$orderId")
        EstadoPedidoChatBubble(
            texto = "Repartidor asignado",
            subtitulo = "Pedido en curso"
        )

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
                    EmptyChatState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = mensajes,
                            key = { it.id }
                        ) { msg ->
                            when (msg.tipo) {
                                TipoBurbuja.ENVIADO -> MensajeEnviado(msg)
                                TipoBurbuja.RECIBIDO -> MensajeRecibido(msg)
                            }
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
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }
        }

        ChatInputBar(
            value = inputText,
            onChange = { inputText = it },
            isSending = isSending,
            onSend = { enviarMensaje() }
        )
    }
}

@Composable
private fun ChatTopBar(
    orderInfo: String,
    receiverEmail: String,
    myName: String,
    onBack: () -> Unit
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
private fun MensajeEnviado(msg: ChatMensajeUi) {
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
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .widthIn(max = 230.dp)
            ) {
                Text(
                    text = msg.texto,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(top = 3.dp, end = 2.dp)
            ) {
                Text(
                    text = msg.hora,
                    fontSize = 10.sp,
                    color = CMuted
                )

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
private fun MensajeRecibido(msg: ChatMensajeUi) {
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
                modifier = Modifier.widthIn(max = 230.dp)
            ) {
                Text(
                    text = msg.texto,
                    fontSize = 13.sp,
                    color = CNegro,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
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
private fun ChatInputBar(
    value: String,
    onChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit
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

@Composable
private fun EstadoPedidoChatBubble(
    texto: String,
    subtitulo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CGrisFondo)
            .padding(top = 16.dp),
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