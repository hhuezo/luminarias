package com.dgehm.luminarias.model

data class CensoOfflineList(
    val id: Long,
    val nombreLuminaria: String,
    val fecha: String,
    val potenciaNominal: Int,
    val consumoMensual: Double,
    val nombreDistrito: String,
    val nombreDepartamento: String,
    val direccion: String,
    val observacion: String,
    val nombreFalla: String,
    val condicionLampara: Int,
    val nombreCompania: String
)

data class CensoOffline(
    val id: Long,
    val tipoLuminariaId: Int,
    val fecha: String,
    val potenciaNominal: Int?,
    val potenciaPromedioId: Int?,
    val consumoMensual: Double,
    val distritoId: Int,
    val usuarioIngreso: Int?,
    val latitud: String?,
    val longitud: String?,
    val usuario: Int?,
    val direccion: String,
    val observacion: String?,
    val tipoFallaId: Int?,
    val condicionLampara: Int,
    val companiaId: Int?
)

