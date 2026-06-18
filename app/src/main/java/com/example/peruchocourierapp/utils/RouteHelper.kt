package com.example.peruchocourierapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.roundToInt

fun abrirWhatsApp(context: Context, numero: String, mensaje: String = "") {
    val url = "https://wa.me/$numero?text=${Uri.encode(mensaje)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

data class RutaResultado(
    val puntos: List<LatLng> = emptyList(),
    val distanciaKm: Double = 0.0,
    val duracionMin: Int = 0
)

fun obtenerRutaCompleta(
    origin: String,
    destination: String
): RutaResultado {
    return try {
        val apiKey = "AIzaSyDQQ_VqUtUg1xM4F_7g09s6-fbWsxXZtKo"

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${Uri.encode(origin)}" +
                "&destination=${Uri.encode(destination)}" +
                "&mode=driving" +
                "&language=es" +
                "&region=pe" +
                "&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""

        Log.d("ROUTE_DEBUG", "URL: $url")
        Log.d("ROUTE_DEBUG", "HTTP: ${response.code}")
        Log.d("ROUTE_DEBUG", "BODY: $body")

        if (!response.isSuccessful || body.isBlank()) {
            return RutaResultado()
        }

        val json = JSONObject(body)
        val status = json.optString("status")

        if (status != "OK") {
            Log.e("ROUTE_DEBUG", "Google Directions status: $status")
            return RutaResultado()
        }

        val routes = json.optJSONArray("routes") ?: return RutaResultado()
        if (routes.length() == 0) return RutaResultado()

        val route = routes.getJSONObject(0)
        val leg = route.getJSONArray("legs").getJSONObject(0)

        val distanciaKm = leg
            .getJSONObject("distance")
            .optDouble("value", 0.0) / 1000.0

        val duracionMin = (
                leg.getJSONObject("duration")
                    .optDouble("value", 0.0) / 60.0
                ).roundToInt()

        val encodedPolyline = route
            .getJSONObject("overview_polyline")
            .getString("points")

        val puntos = decodePolyline(encodedPolyline)

        RutaResultado(
            puntos = puntos,
            distanciaKm = distanciaKm,
            duracionMin = duracionMin
        )

    } catch (e: Exception) {
        Log.e("ROUTE_DEBUG", "Error obteniendo ruta Google Directions", e)
        RutaResultado()
    }
}
private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val dlat = if ((result and 1) != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }

        lat += dlat

        shift = 0
        result = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val dlng = if ((result and 1) != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }

        lng += dlng

        poly.add(
            LatLng(
                lat / 1E5,
                lng / 1E5
            )
        )
    }

    return poly
}
fun obtenerRuta(
    origin: String,
    destination: String,
    apiKey: String = ""
): List<LatLng> {
    return obtenerRutaCompleta(origin, destination).puntos
}

fun calcularTarifaYango(
    distanciaKm: Double,
    tamanoPaquete: String
): Double {
    val base = when (tamanoPaquete.lowercase()) {
        "pequeño", "pequeno" -> 6.0
        "mediano" -> 9.0
        "grande" -> 14.0
        else -> 8.0
    }

    val precioPorKm = when (tamanoPaquete.lowercase()) {
        "pequeño", "pequeno" -> 1.20
        "mediano" -> 1.60
        "grande" -> 2.30
        else -> 1.50
    }

    val minimo = when (tamanoPaquete.lowercase()) {
        "pequeño", "pequeno" -> 8.0
        "mediano" -> 12.0
        "grande" -> 18.0
        else -> 10.0
    }

    val total = base + (distanciaKm * precioPorKm)

    return maxOf(total, minimo)
}