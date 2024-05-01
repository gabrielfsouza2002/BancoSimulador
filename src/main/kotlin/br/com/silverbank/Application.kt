package br.com.silverbank

import br.com.silverbank.dao.*
import br.com.silverbank.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {DatabaseSingleton.init()
    configureRouting()
    configureTemplating()
    configureSockets()
    configureSecurity()
    configureSerialization()
    /*configureDatabases()*/

}
