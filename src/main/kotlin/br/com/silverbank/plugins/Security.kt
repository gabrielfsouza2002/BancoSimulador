package br.com.silverbank.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import java.util.*



data class UserSession(val sessionId: String, val name: String, val count: Int) : Principal

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
                when {
                    (credentials.name == "jetbrains" && credentials.password == "foobar") -> {
                        val sessionId = UUID.randomUUID().toString()
                        userSessions[sessionId] = UserSession(sessionId, credentials.name, 1)
                        UserIdPrincipal(sessionId)
                    }
                    (credentials.name == "jetbrains2" && credentials.password == "foobar2") -> {
                        val sessionId = UUID.randomUUID().toString()
                        userSessions[sessionId] = UserSession(sessionId, credentials.name, 1)
                        UserIdPrincipal(sessionId)
                    }
                    else -> null
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
        basic("auth-basic") {
            realm = "Access to the '/admin' path"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "password") {
                    UserIdPrincipal("admin")
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/login") {
            call.respondHtml {
                body {
                    form(action = "/login", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
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
}
