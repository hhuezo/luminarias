package com.dgehm.luminarias.model

data class ResponseCensoCreate(
    val success: Boolean,
    val data: Data
)

data class Data(
    val departamentos: List<Departamento>,
    val municipios: List<Municipio>,
    val distritos: List<Distrito>,
    val tipos: List<Tipo>,
    val tipos_falla: List<TipoFalla>,
    val puntosCercanos: Int,
    val id_distrito_valido: Boolean,
    val companias: List<Compania>
)


data class Tipo(
    val id: Int,
    val nombre: String,
)

data class Compania(
    val id: Int,
    val nombre: String,
    //val activo: String,
    //val pivot: Pivot?
)

data class Pivot(
    val distrito_id: Int,
    val compania_id: Int
)


data class PotenciaPromedioResponse(
    val value: Int,
    val potencia_promedio: List<PotenciaPromedio>
)

data class PotenciaPromedio(
    val id: Int,
    val potencia: String,
    val consumo_promedio: String?
)

