package br.com.silverbank.plugins

import br.com.silverbank.dao.DatabaseSingleton
import br.com.silverbank.models.Customers
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.*
import java.io.File
import java.util.*

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
            challenge {
                call.respondRedirect("/login?error=true")
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

        staticResources("/home", "frontend")

        get("/login") {
            call.respondRedirect("home/html/login.html")
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

                    val dashboardResource = this::class.java.classLoader.getResource("frontend/html/dashboard.html")
                    if (dashboardResource != null) {
                        val htmlContent = dashboardResource.readText().replace("R$ 0.00", "R$ $balance")
                        call.respondText(htmlContent, ContentType.Text.Html)
                    } else {
                        call.respondText("Arquivo não encontrado", status = HttpStatusCode.NotFound)
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
            call.respondRedirect("/")
        }
    }
}