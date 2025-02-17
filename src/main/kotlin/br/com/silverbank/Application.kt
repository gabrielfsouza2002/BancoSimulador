package br.com.silverbank

import br.com.silverbank.dao.*
import br.com.silverbank.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {DatabaseSingleton.init()
    configureRouting()
    configureSecurity()
    configureTemplating()
    configureSockets()
    configureSerialization()
}
