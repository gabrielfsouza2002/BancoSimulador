package br.com.silverbank.plugins

import br.com.silverbank.dao.*
import br.com.silverbank.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import br.com.silverbank.routes.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.configureRouting() {
    /*install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }*/

    // Add the line below


    routing {
        customerRouting()
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()

        staticResources("/home", "frontend")

        get("/") {
            call.respondRedirect("articles")
        }
        route("articles") {
            get {
                call.respond(FreeMarkerContent("index.ftl", mapOf("articles" to dao.allArticles())))
            }
            get("new") {
                call.respond(FreeMarkerContent("new.ftl", model = null))
            }
            post {
                val formParameters = call.receiveParameters()
                val title = formParameters.getOrFail("title")
                val body = formParameters.getOrFail("body")
                val article = dao.addNewArticle(title, body)
                call.respondRedirect("/articles/${article?.id}")
            }
            get("{id}") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("show.ftl", mapOf("article" to dao.article(id))))
            }
            get("{id}/edit") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("edit.ftl", mapOf("article" to dao.article(id))))
            }
            post("{id}") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                val formParameters = call.receiveParameters()
                when (formParameters.getOrFail("_action")) {
                    "update" -> {
                        val title = formParameters.getOrFail("title")
                        val body = formParameters.getOrFail("body")
                        dao.editArticle(id, title, body)
                        call.respondRedirect("/articles/$id")
                    }
                    "delete" -> {
                        dao.deleteArticle(id)
                        call.respondRedirect("/articles")
                    }
                }
            }

            get("/test0") {
                call.respondText("Hello World!")
            }

            get("/test1") {
                val text = "<h1>Hello From Ktor</h1>"
                val type = ContentType.parse("text/html")
                call.respondText(text, type)
            }

        }
    }
}