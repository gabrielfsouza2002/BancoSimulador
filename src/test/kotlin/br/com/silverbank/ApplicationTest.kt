package br.com.silverbank

import br.com.silverbank.dao.DatabaseSingleton
import br.com.silverbank.dao.daoCustomer
import br.com.silverbank.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.application.*


class ApplicationTest {

    @BeforeTest
    fun setup() {
        DatabaseSingleton.init()
    }

    private fun Application.ensureSecurityConfigured() {
        try {
            configureSecurity()
        } catch (e: DuplicatePluginException) {
            // Plugin já configurado, ignorar exceção
        }
    }

    private fun Application.ensureRoutingConfigured() {
        try {
            configureRouting()
        } catch (e: DuplicatePluginException) {
            // Plugin já configurado, ignorar exceção
        }
    }

    @Test
    fun testRoot() = testApplication {
        application {
            ensureRoutingConfigured()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testNewCustomerStructure() = testApplication {
        application {
            ensureRoutingConfigured()
        }

        val response = client.post("/newCustomer") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "nome": "Test User",
                "login": "testuser1",
                "email": "testuser@example.com",
                "cpf": "12345678901",
                "senha": "password"
            }""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "\"nome\":")
        assertContains(responseBody, "\"login\":")
        assertContains(responseBody, "\"email\":")
        assertContains(responseBody, "\"cpf\":")
        assertContains(responseBody, "\"contaBancaria\":")
        assertContains(responseBody, "\"saldo\":")
    }

    @Test
    fun testDatabaseOperationsStructure() = testApplication {
        application {
            ensureRoutingConfigured()
        }

        // Teste de criação de cliente
        val newCustomer = daoCustomer.addNewCustomer("Test User", "testuser3", "testuser@example.com", "12345678901", "password", "1234", 10000.0f)
        assertNotNull(newCustomer)
        assertNotNull(newCustomer?.id)
        assertNotNull(newCustomer?.nome)
        assertNotNull(newCustomer?.login)
        assertNotNull(newCustomer?.email)
        assertNotNull(newCustomer?.cpf)
        assertNotNull(newCustomer?.contaBancaria)
        assertNotNull(newCustomer?.saldo)

        // Teste de leitura de cliente
        val fetchedCustomer = daoCustomer.customer(newCustomer.id.toInt())
        assertNotNull(fetchedCustomer)
        assertNotNull(fetchedCustomer?.id)
        assertNotNull(fetchedCustomer?.nome)
        assertNotNull(fetchedCustomer?.login)
        assertNotNull(fetchedCustomer?.email)
        assertNotNull(fetchedCustomer?.cpf)
        assertNotNull(fetchedCustomer?.contaBancaria)
        assertNotNull(fetchedCustomer?.saldo)

        // Teste de atualização de cliente
        val updated = daoCustomer.editCustomer(newCustomer.id.toInt(), "Updated User", "testuser2", "testuser@example.com", "12345678901", "newpassword")
        assertTrue(updated)

        val updatedCustomer = daoCustomer.customer(newCustomer.id.toInt())
        assertNotNull(updatedCustomer)
        assertNotNull(updatedCustomer?.id)
        assertNotNull(updatedCustomer?.nome)
        assertNotNull(updatedCustomer?.login)
        assertNotNull(updatedCustomer?.email)
        assertNotNull(updatedCustomer?.cpf)
        assertNotNull(updatedCustomer?.contaBancaria)
        assertNotNull(updatedCustomer?.saldo)

        // Teste de exclusão de cliente
        val deleted = daoCustomer.deleteCustomer(newCustomer.id.toInt())
        assertTrue(deleted)

        val deletedCustomer = daoCustomer.customer(newCustomer.id.toInt())
        assertNull(deletedCustomer)
    }

    @Test
    fun testCustomerRoutes() = testApplication {
        application {
            ensureRoutingConfigured()
        }

        // Teste de rota de criação de cliente
        val response = client.post("/newCustomer") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "nome": "Test User",
                "login": "testuser",
                "email": "testuser@example.com",
                "cpf": "12345678901",
                "senha": "password"
            }""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "\"nome\":")
        assertContains(responseBody, "\"login\":")
        assertContains(responseBody, "\"email\":")
        assertContains(responseBody, "\"cpf\":")
        assertContains(responseBody, "\"contaBancaria\":")
        assertContains(responseBody, "\"saldo\":")
    }
}