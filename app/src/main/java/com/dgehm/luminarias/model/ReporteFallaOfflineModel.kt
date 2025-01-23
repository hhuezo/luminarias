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

