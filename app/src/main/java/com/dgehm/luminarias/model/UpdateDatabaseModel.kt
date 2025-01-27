package com.dgehm.luminarias.model

data class DataResponse(
    val companias: List<CompaniaResponse>,
    val departamentos: List<DepartamentoResponse>,
    val municipios: List<MunicipioResponse>,
    val distritos: List<DistritoResponse>,
    val potencias: List<PotenciaResponse>,
    val tiposFalla: List<TipoFallaResponse>,
    val tiposLuminaria: List<TipoLuminariaResponse>
)

data class CompaniaResponse(
    val id: Int,
    val nombre: String
)

data class DepartamentoResponse(
    val id: Int,
    val nombre: String
)

data class MunicipioResponse(
    val id: Int,
    val nombre: String
)

data class DistritoResponse(
    val id: Int,
    val nombre: String
)

data class PotenciaResponse(
    val id: Int,
    val tipoLuminariaId: Int,
    val potencia: String,
    val ConsumoPromedio: String,
)

data class TipoFallaResponse(
    val id: Int,
    val nombre: String
)

data class TipoLuminariaResponse(
    val id: Int,
    val nombre: String
)

data class ApiResponse(
    val success: Boolean,
    val data: DataResponse
)