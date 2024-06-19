package br.com.silverbank.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import br.com.silverbank.routes.*
import io.ktor.server.http.content.*


fun Application.configureRouting() {

    routing {

        staticResources("/home", "frontend")

        customerRouting()


        /*customerEstRouting()
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()*/


        get("/") {
            call.respondRedirect("home/html/index.html")
        }

        get("/favicon.ico") {
            call.respond(HttpStatusCode.NoContent)
        }

    }
}