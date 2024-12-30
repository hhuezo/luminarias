package com.dgehm.luminarias.model

import com.google.gson.annotations.SerializedName

data class ResponseReporteFallaCreate(
    val departamentos: List<Departamento>?,
    val tipos: List<TipoFalla>?
)


data class Departamento(
    val id: Int,
    val nombre: String,
    val codigo: String? = null
)




data class TipoFalla(
    val id: Int,
    val nombre: String,
    val activo: Int? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null,
    @SerializedName("fecha_modificacion") val fechaModificacion: String? = null,
    @SerializedName("usuario_creacion") val usuarioCreacion: String? = null,
    @SerializedName("usuario_modificacion") val usuarioModificacion: String? = null
)


data class ResponseMunicipio(
    val municipios: List<Municipio>
)

data class Municipio(
    val id: Int,
    val nombre: String,
    val departamento_id: Int? = null,
    val convenio: Int? = null,
    val nombre_responsable: String? = null,
    val correo_responsable: String? = null,
    val telefono_responsable: String? = null,
    val direccion_responsable: String? = null,
    val puesto_responsable: String? = null
)


data class ResponseDistrito(
    val distritos: List<Distrito>
)

data class Distrito(
    val id: Int,
    val nombre: String,
    val municipio_id: Int? = null,
    val codigo: String? = null,
    val extension_territorial: String? = null,
    val poblacion: String? = null
)
