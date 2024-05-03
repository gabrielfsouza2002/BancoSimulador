package br.com.silverbank.routes

import br.com.silverbank.dao.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.articleRouting() {
    route("/articles") {
        get {
            call.respond(FreeMarkerContent("index.ftl", mapOf("articles" to daoArticle.allArticles())))
        }
        get("new") {
            call.respond(FreeMarkerContent("new.ftl", model = null))
        }
        post {
            val formParameters = call.receiveParameters()
            val title = formParameters.getOrFail("title")
            val body = formParameters.getOrFail("body")
            val article = daoArticle.addNewArticle(title, body)
            call.respondRedirect("/articles/${article?.id}") /*No caso do usuario ele teria que ser levado para a tela: registrado com sucesso*/
        }
        get("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            call.respond(FreeMarkerContent("show.ftl", mapOf("article" to daoArticle.article(id))))
        }
        get("{id}/edit") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            call.respond(FreeMarkerContent("edit.ftl", mapOf("article" to daoArticle.article(id))))
        }
        post("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val formParameters = call.receiveParameters()
            when (formParameters.getOrFail("_action")) {
                "update" -> {
                    val title = formParameters.getOrFail("title")
                    val body = formParameters.getOrFail("body")
                    daoArticle.editArticle(id, title, body)
                    call.respondRedirect("/articles/$id")
                }
                "delete" -> {
                    daoArticle.deleteArticle(id)
                    call.respondRedirect("/articles")
                }
            }
        }
    }
}