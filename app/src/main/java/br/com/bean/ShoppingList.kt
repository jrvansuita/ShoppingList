package br.com.bean

import android.content.Context
import br.com.activity.R
import java.util.Date

data class ShoppingList(
    var id: Int,
    var name: String,
    var date: Date
) {
    constructor(context: Context) : this(
        0,
        context.getString(R.string.untitled),
        Date()
    )

    constructor(context: Context, id: Int, name: String, date: Date) : this(
        id,
        if (name.isEmpty()) context.getString(R.string.untitled) else name,
        date
    )
}

