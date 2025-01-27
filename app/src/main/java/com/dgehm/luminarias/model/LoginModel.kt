package com.dgehm.luminarias.model

data class ResponseLogin(
    val value: Int,
    val mensaje: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String
)


