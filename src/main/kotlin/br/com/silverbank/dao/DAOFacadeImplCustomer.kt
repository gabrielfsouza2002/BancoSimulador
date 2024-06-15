package br.com.silverbank.dao

import br.com.silverbank.dao.DatabaseSingleton.dbQuery
import br.com.silverbank.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal
import java.util.*

class DAOFacadeImplCustomer : DAOFacadeCustomer {

    private fun resultRowToCustomer(row: ResultRow) = Customer(
        id = row[Customers.id].toString(),
        nome = row[Customers.nome],
        login = row[Customers.login],
        email = row[Customers.email],
        cpf = row[Customers.cpf],
        senha= row[Customers.senha],
        contaBancaria = row[Customers.contaBancaria],
        saldo = row[Customers.saldo].toFloat()
    )


    override suspend fun allCustomers(): List<Customer> = dbQuery {
        Customers.selectAll().map(::resultRowToCustomer)
    }

    override suspend fun customer(id: Int): Customer? = dbQuery {
        Customers
            .select { Customers.id eq id }
            .map(::resultRowToCustomer)
            .singleOrNull()
    }

    override suspend fun addNewCustomer(nome: String, login: String, email: String, cpf: String, senha: String, contaBancaria: String, saldo: Float): Customer? = dbQuery {

       /* // Gerar um ID único para o cliente
        val customerId = UUID.randomUUID().toString()

        // Gerar uma conta bancária única
        val contaBancaria = generateUniqueAccountNumber()

        // Saldo inicial de 10 mil
        val saldoInicial = 10000.00f*/

        println("\n\n\n\n\n $nome $login $email\n\n\n\n\n")


        val insertStatement = Customers.insert {
            it[Customers.nome] = nome
            it[Customers.login] = login
            it[Customers.email] = email
            it[Customers.cpf] = cpf
            it[Customers.senha] = senha
            it[Customers.contaBancaria] = contaBancaria
            it[Customers.saldo] = BigDecimal.valueOf(saldo.toDouble())

        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCustomer)

    }

}

val daoCustomer: DAOFacadeCustomer = DAOFacadeImplCustomer().apply {

    println("\n\n\n\n\n testeeeeeeeeeeeeeeeeeeeeeeeeee2 \n\n\n\n\n")

    runBlocking {
        println("\n\n\n\n\n testeeeeeeeeeeeeeeeeeeeeeeeeee 3 \n\n\n\n\n")
        //if(allCustomers().isEmpty()) {
            println("\n\n\n\n\n testeeeeeeeeeeeeeeeeeeeeeeeeee 3 \n\n\n\n\n")
            /*addNewCustomer(
                "gabriel",
                "bielzin3",
                "gabrielfsouza.araujo@usp.br",
                "47804811802",
                "paraquedas123",
                "3432-x",
                24.3f
            )*/
        //}
    }
}