package br.com.silverbank.plugins

import br.com.silverbank.dao.DatabaseSingleton
import br.com.silverbank.models.Customers
import br.com.silverbank.routes.CustomerParameters
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import java.io.File
import java.util.*
import br.com.silverbank.plugins.*

data class UserSession(val sessionId: String, val name: String, val count: Int) : Principal

suspend fun checkUserCredentials(username: String, password: String): Boolean {
    return DatabaseSingleton.dbQuery {
        Customers.select { Customers.login eq username and (Customers.senha eq password) }.count() > 0
    }
}

suspend fun getUserBalance(username: String): Float? {
    return DatabaseSingleton.dbQuery {
        Customers.select { Customers.login eq username }
            .map { it[Customers.saldo].toFloat() }
            .singleOrNull()
    }
}

suspend fun updateUserBalance(username: String, newBalance: Float): Boolean {
    return DatabaseSingleton.dbQuery {
        Customers.update({ Customers.login eq username }) {
            it[saldo] = newBalance.toBigDecimal()
        } > 0
    }
}

suspend fun transferAmount(fromUser: String, toAccount: String, amount: Float): Boolean {
    return DatabaseSingleton.dbQuery {
        val fromUserRow = Customers.select { Customers.login eq fromUser }.singleOrNull()
        val toUserRow = Customers.select { Customers.contaBancaria eq toAccount }.singleOrNull()

        if (fromUserRow != null && toUserRow != null) {
            val fromUserAccountNumber = fromUserRow[Customers.contaBancaria]
            if (fromUserAccountNumber == toAccount) {
                return@dbQuery false // Não pode transferir para a própria conta
            }

            val fromUserBalance = fromUserRow[Customers.saldo].toFloat()
            val toUserBalance = toUserRow[Customers.saldo].toFloat()

            if (fromUserBalance >= amount) {
                Customers.update({ Customers.login eq fromUser }) {
                    it[saldo] = (fromUserBalance - amount).toBigDecimal()
                }
                Customers.update({ Customers.contaBancaria eq toAccount }) {
                    it[saldo] = (toUserBalance + amount).toBigDecimal()
                }
                return@dbQuery true
            }
        }
        return@dbQuery false
    }
}

fun Application.configureSecurity() {
    val userSessions = mutableMapOf<String, UserSession>()
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60
        }
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (checkUserCredentials(credentials.name, credentials.password)) {
                    val sessionId = UUID.randomUUID().toString()
                    userSessions[sessionId] = UserSession(sessionId, credentials.name, 1)
                    UserIdPrincipal(sessionId)
                } else {
                    null
                }
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                userSessions[session.sessionId]
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }

    routing {
        get("/login") {
            val loginFile = File("/home/gabriel/Documents/Projeto/SilverBank/src/main/resources/frontend/html/login.html")
            if (loginFile.exists()) {
                call.respondFile(loginFile)
            } else {
                call.respondText("Arquivo não encontrado", status = HttpStatusCode.NotFound)
            }
        }

        authenticate("auth-form") {
            post("/login") {
                val sessionId = call.principal<UserIdPrincipal>()?.name ?: ""
                val userSession = userSessions[sessionId]
                userSession?.let {
                    call.sessions.set(it)
                    call.respondRedirect("/dashboards/${it.sessionId}")
                } ?: call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
            }
        }

        authenticate("auth-session", strategy = AuthenticationStrategy.Required) {
            get("/dashboards/{sessionId}") {
                val currentSessionId = call.parameters["sessionId"]
                val userSession = userSessions[currentSessionId]

                if (userSession != null) {
                    val balance = getUserBalance(userSession.name) ?: 0.0f
                    val updatedSession = userSession.copy(count = userSession.count + 1)
                    userSessions[currentSessionId ?: ""] = updatedSession
                    call.sessions.set(updatedSession)

                    call.respondHtml {
                        head {
                            title { +"Dashboard - Silver Bank" }
                            meta {
                                charset = "UTF-8"
                                name = "viewport"
                                content = "width=device-width, initial-scale=1.0"
                            }
                            style {
                                unsafe {
                                    +"""
                            :root {
                                --cor-saldo: #007bff;
                                --cor-saldo-bg: #e1f5fe;
                            }
                            body {
                                font-family: Arial, sans-serif;
                                margin: 0;
                                padding: 0;
                                background-color: #f8f9fa;
                            }
                            .navbar {
                                background-color: #004d99;
                                color: white;
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                                padding: 10px 20px;
                            }
                            .navbar h1, .navbar nav a {
                                color: white;
                                text-decoration: none;
                                margin-right: 20px;
                            }
                            .saldo-valor {
                                font-size: 2em;
                                color: var(--cor-saldo);
                                background-color: var(--cor-saldo-bg);
                                padding: 20px;
                                border-radius: 5px;
                                display: inline-block;
                                margin-top: 20px;
                                transition: transform 0.6s ease;
                            }
                            .saldo-valor:hover {
                                transform: scale(1.05);
                            }
                            .card {
                                background-color: #fff;
                                border-radius: 8px;
                                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                                margin: 20px;
                                padding: 20px;
                                text-align: left;
                            }
                            .filtros {
                                margin-bottom: 20px;
                                text-align: center;
                            }
                            .filtro-btn {
                                background-color: #007bff;
                                border: none;
                                border-radius: 5px;
                                color: white;
                                cursor: pointer;
                                margin: 0 5px;
                                padding: 10px 20px;
                            }
                            .filtro-btn:hover {
                                background-color: #0056b3;
                            }
                            .lista-transacoes {
                                list-style: none;
                                padding: 0;
                            }
                            .transacao {
                                align-items: center;
                                display: flex;
                                justify-content: space-between;
                                margin-bottom: 10px;
                                padding: 10px;
                            }
                            .input-group {
                                margin-bottom: 15px;
                            }
                            .input-group label {
                                display: block;
                                margin-bottom: 5px;
                            }
                            .input-group input {
                                width: 100%;
                                padding: 10px;
                                border-radius: 5px;
                                border: 1px solid #ccc;
                            }
                            .btn-transferir {
                                background-color: #007bff;
                                color: white;
                                padding: 10px 15px;
                                border: none;
                                border-radius: 5px;
                                cursor: pointer;
                                width: 100%;
                            }
                            .btn-transferir:hover {
                                background-color: #0056b3;
                            }
                            .receita span {
                                color: #28a745;
                            }
                            .despesa span {
                                color: #dc3545;
                            }
                            .data {
                                color: #6c757d;
                                font-size: 0.9em;
                            }
                            form {
                                display: flex;
                                flex-direction: column;
                            }
                            input, button {
                                margin: 10px 0;
                                padding: 10px;
                                border-radius: 5px;
                                border: 1px solid #ccc;
                            }
                            button {
                                background-color: #007bff;
                                color: white;
                                cursor: pointer;
                            }
                            button:hover {
                                background-color: #0056b3;
                            }
                            """
                                }
                            }
                            script(src = "/home/js/dashboard.js") {}
                            link (rel="shortcut icon" , href="/home/imgs/favicon.ico") {}
                        }
                        body {
                            div("container") {
                                header("navbar") {
                                    h1 { +"Silver Bank" }
                                }
                                main {
                                    section("card") {
                                        id = "saldo"
                                        h2 { +"Saldo Atual" }
                                        div("saldo-valor") {
                                            id = "valorSaldo"
                                            +"R$ $balance"
                                        }
                                    }
                                    section("card") {
                                        id = "transferencia"
                                        h2 { +"Realizar Transferência" }
                                        form {
                                            id = "formTransferencia"
                                            input {
                                                type = InputType.number
                                                id = "valorTransferencia"
                                                placeholder = "Valor"
                                                required = true
                                            }
                                            input {
                                                type = InputType.text
                                                id = "contaDestino"
                                                placeholder = "Conta destino"
                                                required = true
                                            }
                                            button {
                                                type = ButtonType.submit
                                                +"Transferir"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
                }
            }
        }

        post("/transfer") {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, "Usuário não autenticado")
                return@post
            }

            val parameters = call.receiveParameters()
            val valor = parameters["valor"]?.toFloatOrNull()
            val contaDestino = parameters["contaDestino"]
            if (valor == null || contaDestino.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Parâmetros inválidos")
                return@post
            }

            // Verifique se a conta de destino é a mesma que a conta do usuário atual
            val fromUserAccountNumber = DatabaseSingleton.dbQuery {
                Customers.select { Customers.login eq session.name }
                    .map { it[Customers.contaBancaria] }
                    .singleOrNull()
            }

            println("\n\n\n\n\nfromUserAccountNumber: $fromUserAccountNumber, contaDestino: $contaDestino\n\n\n\n\n")
            if (fromUserAccountNumber == contaDestino) {
                call.respond(HttpStatusCode.BadRequest, "Não é possível transferir para a própria conta")
                return@post
            }

            val success = transferAmount(session.name, contaDestino, valor)
            if (success) {
                call.respond(HttpStatusCode.OK, "Transferência realizada com sucesso")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Falha na transferência (Erro de Servidor ou Tentativa de Transação para própria conta.)")
            }
        }

        get("/logout") {
            val sessionId = call.principal<UserIdPrincipal>()?.name ?: ""
            userSessions.remove(sessionId)
            call.sessions.clear<UserSession>()
            call.respondRedirect("/login")
        }
    }
}