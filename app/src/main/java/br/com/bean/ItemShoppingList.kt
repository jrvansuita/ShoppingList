package br.com.bean

data class ItemShoppingList(
    var id: Int,
    var idShoppingList: Int,
    var description: String,
    var unitValue: Float,
    var quantity: Float,
    var checked: Boolean
) {
    val total: Float
        get() = quantity * unitValue
}

