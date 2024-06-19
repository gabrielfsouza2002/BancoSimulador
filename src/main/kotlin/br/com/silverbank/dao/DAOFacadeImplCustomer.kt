package br.com.silverbank.dao

import br.com.silverbank.dao.DatabaseSingleton.dbQuery
import br.com.silverbank.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal

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

    override suspend fun editCustomer(id: Int, nome: String, login: String, email: String, cpf: String, senha: String): Boolean = dbQuery {
        Customers.update({ Customers.id eq id }) {
            it[Customers.nome] = nome
            it[Customers.login] = login
            it[Customers.email] = email
            it[Customers.cpf] = cpf
            it[Customers.senha] = senha
        } > 0
    }

    override suspend fun deleteCustomer(id: Int): Boolean = dbQuery {
        Customers.deleteWhere { Customers.id eq id } > 0
    }
}

val daoCustomer: DAOFacadeCustomer = DAOFacadeImplCustomer().apply {


}