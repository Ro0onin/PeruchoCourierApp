package com.example.peruchocourierapp.models

data class UserProfile(

    val name: String?,
    val apellidos: String?,
    val email: String?,
    val phone: String?,
    val dni: String?,

    val pais: String?,
    val direccion: String?,
    val apartamento: String?,
    val ciudad: String?,
    val provincia: String?,
    val codigo_postal: String?
)