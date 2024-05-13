package br.com.silverbank.routes

import br.com.silverbank.dao.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

data class CustomerParameters(
    val nome: String,
    val login: String,
    val email: String,
    val cpf: String,
    val senha: String,
    val contaBancaria: String,
    val saldo: Float
)

fun generateUniqueAccountNumber(): String {
    val randomNumber = (1000..9999).random()
    return randomNumber.toString()
}

fun Route.customerRouting() {
    route("/newCustomer") {
        post {
            val customerParameters = call.receive<CustomerParameters>()
            val nome = customerParameters.nome
            val login = customerParameters.login
            val email = customerParameters.email
            val cpf = customerParameters.cpf
            val senha = customerParameters.senha

            println("\n \n \n" + senha)

             // Gerar uma conta bancária única
             val contaBancaria = generateUniqueAccountNumber()

             // Saldo inicial de 10 mil
             val saldoInicial = 10000.00f

                val customer = daoCustomer.addNewCustomer(nome, login, email, cpf, senha, contaBancaria, saldoInicial)
                if (customer != null) {
                    call.respond(HttpStatusCode.Created, customer)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }

        }
    }
}