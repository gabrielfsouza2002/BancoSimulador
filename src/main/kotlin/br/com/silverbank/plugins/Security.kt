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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import java.util.*

data class UserSession(val sessionId: String, val name: String, val count: Int) : Principal

@Serializable
data class LoginParameters(
    val username: String,
    val password: String
)

suspend fun checkUserCredentials(username: String, password: String): Boolean {
    return DatabaseSingleton.dbQuery {
        Customers.select { Customers.login eq username and (Customers.senha eq password) }.count() > 0
    }
}
var validUsername: String? = null
var validPassword: String? = null
fun Application.configureSecurity() {

    routing {
        route("/loginDate") {

            post {

                val loginParameters = call.receive<LoginParameters>()
                val username = loginParameters.username
                val password = loginParameters.password

                println("\n\n\n\n\n\n   $username  \n\n\n\n\n\n")
                println("\n\n\n\n\n\n   $password  \n\n\n\n\n\n")
                val teste = checkUserCredentials(username, password)
                println("\n\n\n\n\n\n   $teste \n\n\n\n\n\n")

                if (username != null && password != null) {
                    val responseJson = """
                        {
                            "username": "$username",
                            "password": "$password"
                        }
                    """.trimIndent()
                    validUsername = username
                    validPassword = password
                    call.respondText(responseJson, ContentType.Application.Json, HttpStatusCode.OK)

                } else {
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
                println("\n\n\n\n\n\n TESTE2  $userSessions  \n\n\n\n\n\n")
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

        routing{

            get("/login") {

                call.respondHtml {
                    body {
                        form(
                            action = "/login",
                            encType = FormEncType.applicationXWwwFormUrlEncoded,
                            method = FormMethod.post
                        ) {
                            p {
                                +"Username:"
                                textInput(name = "username")
                            }
                            p {
                                +"Password:"
                                passwordInput(name = "password")
                            }
                            p {
                                submitInput() { value = "Login" }
                            }
                        }
                    }
                }
            }


            authenticate("auth-form") {

                post("/login") {
                    println("\n\n\n\n\n\n TESTE2  $userSessions  \n\n\n\n\n\n")

                    val sessionId = call.principal<UserIdPrincipal>()?.name ?: ""
                    val userSession = userSessions[sessionId]
                    userSession?.let {
                        call.sessions.set(it)
                        call.respondRedirect("/hello/${it.sessionId}")
                    } ?: call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
                }
            }

            authenticate("auth-session", strategy = AuthenticationStrategy.Required) {
                get("/hello/{sessionId}") {
                    val currentSessionId = call.parameters["sessionId"]
                    val userSession = userSessions[currentSessionId]

                    if (userSession != null) {
                        val updatedSession = userSession.copy(count = userSession.count + 1)
                        userSessions[currentSessionId ?: ""] = updatedSession
                        call.sessions.set(updatedSession)
                        call.respondText("Ola, ${userSession.name}! Visit count is ${userSession.count}.")
                    } else {
                        call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
                    }
                }
            }

            get("/logout") { /*Arrumar esse logout, todos os usuarios deslogam quando um usu[ario desloga*/
                val sessionId = call.principal<UserIdPrincipal>()?.name ?: ""
                userSessions.remove(sessionId)
                call.sessions.clear<UserSession>()
                call.respondRedirect("/login")
            }

        }

    println("\n\n\n\n\n\n TESTE1  $userSessions  \n\n\n\n\n\n")

}


