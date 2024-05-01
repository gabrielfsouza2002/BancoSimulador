package br.com.silverbank.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(val id: String, val firstName: String, val lastName: String, val email: String)

/* Memoria local, mas aqui seria onde colocriamos o banco de dados */

val customerStorage = mutableListOf<Customer>()