package br.com.silverbank

import br.com.silverbank.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureTemplating()
    configureSockets()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
