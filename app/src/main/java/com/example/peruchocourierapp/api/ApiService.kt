package com.example.peruchocourierapp.api

import com.example.peruchocourierapp.models.ActiveOrderResponse
import com.example.peruchocourierapp.models.BasicResponse
import com.example.peruchocourierapp.models.CreateOrderResponse
import com.example.peruchocourierapp.models.DriverDashboardResponse
import com.example.peruchocourierapp.models.DriverLocationResponse
import com.example.peruchocourierapp.models.GetChatMessagesResponse
import com.example.peruchocourierapp.models.GetDriverHistoryResponse
import com.example.peruchocourierapp.models.GetOrdersResponse
import com.example.peruchocourierapp.models.LoginResponse
import com.example.peruchocourierapp.models.ProfileResponse
import com.example.peruchocourierapp.models.RegisterResponse
import com.example.peruchocourierapp.models.RouteDistanceResponse
import com.example.peruchocourierapp.models.VerificationStatusResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @Multipart
    @POST("register.php")
    fun registerUser(
        @Part("name") name: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part dniFront: MultipartBody.Part,
        @Part dniBack: MultipartBody.Part
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // ===== ENVÍO SIMPLE / CREAR ENVÍO =====
    @FormUrlEncoded
    @POST("create_order.php")
    fun createOrder(
        @Field("user_email") userEmail: String,
        @Field("origen") origen: String,
        @Field("destino") destino: String,
        @Field("descripcion") descripcion: String,
        @Field("peso") peso: String,
        @Field("metodo_pago") metodoPago: String,
        @Field("total") total: String
    ): Call<CreateOrderResponse>

    // ===== PEDIDO NACIONAL CON MAPA =====
    @Multipart
    @POST("create_national_order.php")
    fun createNationalOrder(
        @Part("user_email") userEmail: RequestBody,
        @Part("tipo_envio") tipoEnvio: RequestBody,

        @Part("origen") origen: RequestBody,
        @Part("destino") destino: RequestBody,

        @Part("pickup_address") pickupAddress: RequestBody,
        @Part("pickup_lat") pickupLat: RequestBody,
        @Part("pickup_lng") pickupLng: RequestBody,

        @Part("dropoff_address") dropoffAddress: RequestBody,
        @Part("dropoff_lat") dropoffLat: RequestBody,
        @Part("dropoff_lng") dropoffLng: RequestBody,

        @Part("telefono_remitente") telefonoRemitente: RequestBody,
        @Part("telefono_destinatario") telefonoDestinatario: RequestBody,

        @Part("descripcion") descripcion: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("comentarios_repartidor") comentariosRepartidor: RequestBody,

        @Part("tamano_paquete") tamanoPaquete: RequestBody,
        @Part("peso_kg") pesoKg: RequestBody,
        @Part("tipo_vehiculo") tipoVehiculo: RequestBody,
        @Part("metodo_pago") metodoPago: RequestBody,
        @Part("distancia_km") distanciaKm: RequestBody,
        @Part("total") total: RequestBody,

        @Part fotoPaquete: MultipartBody.Part
    ): Call<BasicResponse>

    @Multipart
    @POST("create_international_order.php")
    fun createInternationalOrder(
        @Part("user_email") userEmail: RequestBody,
        @Part("tipo_envio") tipoEnvio: RequestBody,
        @Part("web_compra") webCompra: RequestBody,
        @Part("productos") productos: RequestBody,
        @Part("precio_compra") precioCompra: RequestBody,
        @Part("tracking") tracking: RequestBody,
        @Part("fecha_llegada") fechaLlegada: RequestBody,
        @Part("peso_estimado") pesoEstimado: RequestBody,
        @Part("metodo_pago") metodoPago: RequestBody,
        @Part("total_estimado") totalEstimado: RequestBody,
        @Part facturaPdf: MultipartBody.Part
    ): Call<BasicResponse>

    @GET("get_orders.php")
    fun getOrders(
        @Query("user_email") userEmail: String
    ): Call<GetOrdersResponse>

    @GET("get_available_orders.php")
    fun getAvailableOrders(): Call<GetOrdersResponse>

    @FormUrlEncoded
    @POST("accept_order.php")
    fun acceptOrder(
        @Field("order_id") orderId: Int,
        @Field("driver_email") driverEmail: String
    ): Call<BasicResponse>

    @GET("get_active_order.php")
    fun getActiveOrder(
        @Query("driver_email") driverEmail: String
    ): Call<ActiveOrderResponse>

    @GET("get_my_active_order.php")
    fun getMyActiveOrder(
        @Query("user_email") userEmail: String
    ): Call<ActiveOrderResponse>

    @FormUrlEncoded
    @POST("update_order_status.php")
    fun updateOrderStatus(
        @Field("order_id") orderId: Int,
        @Field("driver_email") driverEmail: String,
        @Field("estado") estado: String
    ): Call<BasicResponse>

    @FormUrlEncoded
    @POST("update_driver_location.php")
    fun updateDriverLocation(
        @Field("order_id") orderId: Int,
        @Field("driver_email") driverEmail: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String
    ): Call<BasicResponse>

    @GET("get_driver_location.php")
    fun getDriverLocation(
        @Query("order_id") orderId: Int
    ): Call<DriverLocationResponse>

    @FormUrlEncoded
    @POST("get_driver_delivery_history.php")
    fun getDriverDeliveryHistory(
        @Field("driver_email") driverEmail: String
    ): Call<GetDriverHistoryResponse>

    @GET("get_route_distance.php")
    fun getRouteDistance(
        @Query("pickup_lat") pickupLat: String,
        @Query("pickup_lng") pickupLng: String,
        @Query("dropoff_lat") dropoffLat: String,
        @Query("dropoff_lng") dropoffLng: String
    ): Call<RouteDistanceResponse>

    @GET("get_profile.php")
    fun getProfile(
        @Query("email") email: String
    ): Call<ProfileResponse>

    @FormUrlEncoded
    @POST("update_profile_address.php")
    fun updateProfileAddress(

        @Field("email")
        email: String,

        @Field("pais")
        pais: String,

        @Field("direccion")
        direccion: String,

        @Field("apartamento")
        apartamento: String,

        @Field("ciudad")
        ciudad: String,

        @Field("provincia")
        provincia: String,

        @Field("codigo_postal")
        codigoPostal: String

    ): Call<BasicResponse>


    @GET("driver_dashboard_stats.php")
    fun getDriverDashboardStats(
        @Query("driver_email") driverEmail: String
    ): Call<DriverDashboardResponse>

    @GET("get_order_tracking.php")
    fun getOrderTracking(
        @Query("order_id") orderId: Int,
        @Query("user_email") userEmail: String
    ): Call<ActiveOrderResponse>

    @FormUrlEncoded
    @POST("send_sms_code.php")
    fun sendSmsCode(
        @Field("phone") phone: String
    ): Call<BasicResponse>

    @FormUrlEncoded
    @POST("verify_sms_code.php")
    fun verifySmsCode(
        @Field("phone") phone: String,
        @Field("code") code: String
    ): Call<BasicResponse>

    @GET("get_verification_status.php")
    fun getVerificationStatus(
        @Query("email") email: String
    ): Call<VerificationStatusResponse>

    @FormUrlEncoded
    @POST("send_chat_message.php")
    fun sendChatMessage(
        @Field("order_id") orderId: Int,
        @Field("sender_email") senderEmail: String,
        @Field("receiver_email") receiverEmail: String,
        @Field("mensaje") mensaje: String
    ): Call<BasicResponse>

    @GET("get_chat_messages.php")
    fun getChatMessages(
        @Query("order_id") orderId: Int
    ): Call<GetChatMessagesResponse>

    @FormUrlEncoded
    @POST("mark_chat_read.php")
    fun markChatRead(
        @Field("order_id") orderId: Int,
        @Field("user_email") userEmail: String
    ): Call<BasicResponse>

    @FormUrlEncoded
    @POST("save_fcm_token.php")
    fun saveFcmToken(
        @Field("email") email: String,
        @Field("fcm_token") fcmToken: String
    ): Call<BasicResponse>

    @FormUrlEncoded
    @POST("google_login.php")
    fun googleLogin(
        @Field("email") email: String,
        @Field("name") name: String,
        @Field("google_uid") googleUid: String
    ): Call<LoginResponse>

    @Multipart
    @POST("complete_google_profile.php")
    fun completeGoogleProfile(
        @Part("email") email: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("dni_direccion") dniDireccion: RequestBody,
        @Part("dni_provincia") dniProvincia: RequestBody,
        @Part dniFront: MultipartBody.Part,
        @Part dniBack: MultipartBody.Part
    ): Call<BasicResponse>




}
