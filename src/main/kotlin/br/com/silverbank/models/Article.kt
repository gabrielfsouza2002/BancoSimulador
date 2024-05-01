package br.com.silverbank.models


import org.jetbrains.exposed.sql.*
import java.util.concurrent.atomic.AtomicInteger


/*Um artigo possui três atributos: id, título e corpo. Os atributos de título e corpo podem ser especificados diretamente enquanto um ID exclusivo é gerado automaticamente usando AtomicInteger - uma estrutura de dados thread-safe que garante que dois artigos nunca receberão o mesmo ID. Dentro do Article.kt, vamos criar uma lista mutável para armazenar artigos e adicionar a primeira entrada:*/

/*class Article
private constructor(val id: Int, var title: String, var body: String) {
    companion object {
        private val idCounter = AtomicInteger()

        fun newEntry(title: String, body: String) = Article(idCounter.getAndIncrement(), title, body)
    }
}

val articles = mutableListOf(Article.newEntry(
    "The drive to develop!",
    "...it's what keeps me going."
))*/

data class Article(val id: Int, val title: String, val body: String)

object Articles : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 128)
    val body = varchar("body", 1024)

    override val primaryKey = PrimaryKey(id)
}

