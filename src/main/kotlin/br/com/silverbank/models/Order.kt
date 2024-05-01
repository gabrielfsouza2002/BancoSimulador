package br.com.silverbank.models

import kotlinx.serialization.Serializable

/*Os pedidos que desejamos armazenar em nosso sistema devem ser identificáveis ​​por um número de pedido (que pode conter travessões) e devem conter uma lista de itens do pedido. Esses itens do pedido devem ter uma descrição textual, o número da frequência com que esse item aparece no pedido,*/
@Serializable
data class Order(val number: String, val contents: List<OrderItem>)

@Serializable
data class OrderItem(val item: String, val amount: Int, val price: Double)

val orderStorage = listOf(

    Order("2020-04-06-01", listOf(
        OrderItem("Ham Sandwich", 2, 5.50),
        OrderItem("Water", 1, 1.50),
        OrderItem("Beer", 3, 2.30),
        OrderItem("Cheesecake", 1, 3.75)
    )),
    Order("2020-04-03-01", listOf(
        OrderItem("Cheeseburger", 1, 8.50),
        OrderItem("Water", 2, 1.50),
        OrderItem("Coke", 2, 1.76),
        OrderItem("Ice Cream", 1, 2.35)
    ))
)

