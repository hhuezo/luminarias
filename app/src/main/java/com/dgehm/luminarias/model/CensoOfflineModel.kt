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
