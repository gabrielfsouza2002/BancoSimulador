package br.com.silverbank.dao

import br.com.silverbank.models.*

interface DAOFacadeCustomer {
    suspend fun addNewCustomer(nome: String, login: String, email: String, cpf: String, senha: String, contaBancaria: String, saldo: Float): Customer?
    suspend fun allCustomers(): List<Customer>
    suspend fun customer(id: Int): Customer?
    suspend fun editCustomer(id: Int, nome: String, login: String, email: String, cpf: String, senha: String): Boolean
    suspend fun deleteCustomer(id: Int): Boolean
}

