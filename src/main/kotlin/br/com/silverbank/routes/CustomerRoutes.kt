package br.com.silverbank.routes

import br.com.silverbank.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRouting() {
    route("/customer") {

        /*Observe também como temos duas entradas para get: uma sem parâmetro de caminho e outra com {id?}. Usaremos a primeira entrada para listar todos os clientes e a segunda entrada para exibir um cliente específico.*/
        /*Para listar todos os clientes, podemos retornar a lista customerStorage usando a função call.respond no Ktor, que pode pegar um objeto Kotlin e retorná-lo serializado em um formato especificado. Para o manipulador get, é assim*/
        get {
            if (customerStorage.isNotEmpty()) {
                call.respond(customerStorage)
            } else {
                call.respondText("No customers found", status = HttpStatusCode.OK)
            }
        }

        /*Primeiro, verificamos se o parâmetro id existe na solicitação. Se não existir, respondemos com um código de status 400 Bad Request e uma mensagem de erro e pronto. Se o parâmetro existir, tentamos encontrar o registro correspondente em nosso customerStorage. Se encontrarmos, responderemos com o objeto. Caso contrário, retornaremos um código de status 404 ‘Não encontrado’ com uma mensagem de erro.*/
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            val customer =
                customerStorage.find { it.id == id } ?: return@get call.respondText(
                    "No customer with id $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(customer)
        }

        /*call.receive integra-se ao plugin de negociação de conteúdo configurado. Chamá-lo com o parâmetro genérico Customer desseriaria automaticamente o corpo da solicitação JSON em um objeto Kotlin Customer. Podemos então adicionar o cliente ao nosso armazenamento e responder com um código de status 201 Criado.*/
        post {
            val customer = call.receive<Customer>()
            customerStorage.add(customer)
            call.respondText("Customer stored correctly", status = HttpStatusCode.Created)
        }

        /*Semelhante à definição da nossa solicitação get, garantimos que o id não seja nulo. Se o ID estiver ausente, responderemos com um erro 400 Bad Request. Semelhante à definição da nossa solicitação get, garantimos que o id não seja nulo. Se o id estiver ausente, respondemos com um erro 400 Bad Request.Registre as rotas*/
        delete("{id?}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (customerStorage.removeIf { it.id == id }) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

    }
}

