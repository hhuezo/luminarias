package com.dgehm.luminarias.model

data class ReporteFallaOffline(
    val id: Long,
    val fechaCreacion: String,
    val distritoId: Int,
    val tipoFallaId: Int,
    val descripcion: String,
    val latitud: String,
    val longitud: String,
    val telefonoContacto: String,
    val nombreContacto: String,
    val correoContacto: String,
    val usuarioCreacion: Int,
    val urlFoto: String?
)


data class ReporteFallaOfflineList(
    val id: Long,
    val fechaCreacion: String,
    val descripcion: String,
    val tipoFalla: String, // Nombre del tipo de falla
    val distrito: String, // Nombre del distrito
    val departamento: String, // Nombre del departamento
    val nombreContacto: String,
    val telefonoContacto: String
)


