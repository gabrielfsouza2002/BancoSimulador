package br.com.silverbank

import br.com.silverbank.dao.DatabaseSingleton
import br.com.silverbank.dao.daoCustomer
import br.com.silverbank.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @BeforeTest
    fun setup() {
        DatabaseSingleton.init()
    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testNewCustomerStructure() = testApplication {
        application {
            configureRouting()
        }

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

    @Test
    fun testLoginStructure() = testApplication {
        application {
            configureSecurity()
        }

        val response = client.post("/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=testuser&password=password")
        }

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/dashboards/", response.headers["Location"]?.substring(0, 12))
    }

    @Test
    fun testLogoutStructure() = testApplication {
        application {
            configureSecurity()
        }

        val response = client.get("/logout")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/", response.headers["Location"])
    }

    @Test
    fun testDashboardStructure() = testApplication {
        application {
            configureSecurity()
        }

        val sessionId = "test-session-id"
        val response = client.get("/dashboards/$sessionId")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Saldo Atual")
        assertContains(responseBody, "Realizar Transferência")
    }

    @Test
    fun testTransferStructure() = testApplication {
        application {
            configureSecurity()
        }

        val response = client.post("/transfer") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("valor=100&contaDestino=1234")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Transferência realizada com sucesso", response.bodyAsText())
    }

    @Test
    fun testDatabaseOperationsStructure() = testApplication {
        application {
            configureRouting()
        }

        // Teste de criação de cliente
        val newCustomer = daoCustomer.addNewCustomer("Test User", "testuser", "testuser@example.com", "12345678901", "password", "1234", 10000.0f)
        assertNotNull(newCustomer)
        assertNotNull(newCustomer?.id)
        assertNotNull(newCustomer?.nome)
        assertNotNull(newCustomer?.login)
        assertNotNull(newCustomer?.email)
        assertNotNull(newCustomer?.cpf)
        assertNotNull(newCustomer?.contaBancaria)
        assertNotNull(newCustomer?.saldo)

        // Teste de leitura de cliente
        val fetchedCustomer = daoCustomer.customer(newCustomer!!.id.toInt())
        assertNotNull(fetchedCustomer)
        assertNotNull(fetchedCustomer?.id)
        assertNotNull(fetchedCustomer?.nome)
        assertNotNull(fetchedCustomer?.login)
        assertNotNull(fetchedCustomer?.email)
        assertNotNull(fetchedCustomer?.cpf)
        assertNotNull(fetchedCustomer?.contaBancaria)
        assertNotNull(fetchedCustomer?.saldo)

        // Teste de atualização de cliente
        val updated = daoCustomer.editCustomer(newCustomer.id.toInt(), "Updated User", "testuser", "testuser@example.com", "12345678901", "newpassword")
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
            configureRouting()
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

    @Test
    fun testSecurityRoutes() = testApplication {
        application {
            configureSecurity()
        }

        // Teste de rota de login
        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=testuser&password=password")
        }

        assertEquals(HttpStatusCode.Found, loginResponse.status)
        assertEquals("/dashboards/", loginResponse.headers["Location"]?.substring(0, 12))

        // Teste de rota de logout
        val logoutResponse = client.get("/logout")
        assertEquals(HttpStatusCode.Found, logoutResponse.status)
        assertEquals("/", logoutResponse.headers["Location"])
    }
}