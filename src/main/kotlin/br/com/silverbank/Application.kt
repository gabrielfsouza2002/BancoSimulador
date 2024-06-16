package br.com.silverbank

import br.com.silverbank.dao.*
import br.com.silverbank.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)


}

fun Application.module() {DatabaseSingleton.init()
    configureSecurity()
    configureRouting()
    configureTemplating()
    configureSockets()
    configureSerialization()
    /*configureDatabases()*/

}
