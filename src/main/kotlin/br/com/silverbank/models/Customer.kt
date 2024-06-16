package br.com.silverbank.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import java.util.*


@Serializable
data class Customer(val id: String, val nome: String, val login: String, val email: String, val cpf: String, val senha: String ,val contaBancaria: String, val saldo: Float)

/* Memoria local, mas aqui seria onde colocriamos o banco de dados */

val customerStorage = mutableListOf<Customer>()


object Customers : Table() {
    val id = integer("id").autoIncrement()
    val nome = varchar("nome", 255)
    val login = varchar("login",30)
    val email = varchar("email", 255)
    val cpf = varchar("cpf", 14)
    val senha = varchar("senha",30)
    val contaBancaria = varchar("conta_bancaria", 20)
    val saldo = decimal("saldo", 10, 2)


    override val primaryKey = PrimaryKey(id)
}