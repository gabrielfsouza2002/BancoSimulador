package br.com.silverbank.routes

import kotlinx.serialization.Serializable
import br.com.silverbank.dao.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

@Serializable
data class CustomerParameters(
    val nome: String,
    val login: String,
    val email: String,
    val cpf: String,
    val senha: String
)

fun generateUniqueAccountNumber(): String {
    val randomNumber = (1000..9999).random()
    return randomNumber.toString()
}

fun Route.customerRouting() {
    route("/newCustomer") {

        post {
            try {
                val customerParameters = call.receive<CustomerParameters>()
                val nome = customerParameters.nome
                val login = customerParameters.login
                val email = customerParameters.email
                val cpf = customerParameters.cpf
                val senha = customerParameters.senha



                // Gerar uma conta bancária única
                val contaBancaria = generateUniqueAccountNumber()

                // Saldo inicial de 10 mil
                val saldoInicial = 10000.00f


                println("\n\n\n\n\n $contaBancaria $login \n\n\n\n\n")

                val teste = daoArticle.addNewArticle("testeC", "testeCC")
                println("\n\n\n\n\n $teste \n\n\n\n\n")

                val customer = daoCustomer.addNewCustomer(nome, login, email, cpf, senha, contaBancaria, saldoInicial)




                if (customer != null) {
                    val responseJson = """
                        {
                            "id": ${customer.id},
                            "nome": "${customer.nome}",
                            "login": "${customer.login}",
                            "email": "${customer.email}",
                            "cpf": "${customer.cpf}",
                            "contaBancaria": "${customer.contaBancaria}",
                            "saldo": ${customer.saldo}
                        }
                    """.trimIndent()
                    println("\n\n\n\n\n $responseJson \n\n\n\n")
                    call.respondText(responseJson, ContentType.Application.Json, HttpStatusCode.Created)
                } else {
                    val errorJson = """
                        {
                            "error": "Erro ao criar cliente"
                        }
                    """.trimIndent()
                    call.respondText(errorJson, ContentType.Application.Json, HttpStatusCode.InternalServerError)
                }
            } catch (e: Exception) {
                val errorJson = """
                    {
                        "error": "Dados inválidos"
                    }
                """.trimIndent()
                call.respondText(errorJson, ContentType.Application.Json, HttpStatusCode.BadRequest)
            }
        }
    }
}