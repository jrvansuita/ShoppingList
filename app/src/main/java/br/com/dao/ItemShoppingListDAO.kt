package br.com.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.ArrayAdapter
import br.com.activity.R
import br.com.bean.ItemShoppingList
import br.com.vansexception.VansException
import br.com.vansformat.CustomFloatFormat
import br.com.vansprefs.UserPreferences

object ItemShoppingListDAO {
    const val TABLE_NAME: String = "ITEMSHOPPINGLIST"
    const val FIELD_ID: String = "_id"
    const val FIELD_IDSHOPPINGLIST: String = "IDSHOPPINGLIST"
    const val FIELD_DESCRIPTION: String = "DESCRIPTION"
    const val FIELD_UNITVALUE: String = "UNITVALUE"
    const val FIELD_QUANTITY: String = "QUANTITY"
    const val FIELD_CHECKED: String = "CHECKED"

    @JvmStatic
    @Throws(VansException::class)
    fun insert(context: Context, itemShoppingList: ItemShoppingList): ItemShoppingList? {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                val cv = buildContentValues(itemShoppingList)
                db.insertOrThrow(TABLE_NAME, null, cv)
                return selectLast(context, db)
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    private fun buildContentValues(item: ItemShoppingList): ContentValues {
        val cv = ContentValues()
        cv.put(FIELD_IDSHOPPINGLIST, item.idShoppingList)
        cv.put(FIELD_DESCRIPTION, item.description)
        cv.put(FIELD_UNITVALUE, item.unitValue)
        cv.put(FIELD_QUANTITY, item.quantity)
        cv.put(FIELD_CHECKED, item.checked.toString())
        return cv
    }

    @Throws(VansException::class)
    private fun selectLast(context: Context, db: SQLiteDatabase): ItemShoppingList? {
        try {
            db.query(TABLE_NAME, null, null, null, null, null, FIELD_ID + " DESC", "1")
                .use { cursor ->
                    if (cursor.moveToFirst()) {
                        return returnClassInstance(context, cursor)
                    }
                }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
        return null
    }

    @JvmStatic
    @Throws(VansException::class)
    fun checkAllItems(context: Context, idShoppingList: Int, selected: Boolean) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                val cv = ContentValues()
                cv.put(FIELD_CHECKED, selected.toString())
                db.update(
                    TABLE_NAME,
                    cv,
                    FIELD_IDSHOPPINGLIST + " = ?",
                    arrayOf(idShoppingList.toString())
                )
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun delete(context: Context, idItemShoppingList: Int) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                db.delete(TABLE_NAME, FIELD_ID + " = ?", arrayOf(idItemShoppingList.toString()))
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun deleteAllList(context: Context, idShoppingList: Int, onlyChecked: Boolean) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                var whereClause = FIELD_IDSHOPPINGLIST + " = ?"
                var whereArgs = arrayOf(idShoppingList.toString())
                if (onlyChecked) {
                    whereClause += " AND " + FIELD_CHECKED + " = ?"
                    whereArgs = arrayOf(idShoppingList.toString(), true.toString())
                }
                db.delete(TABLE_NAME, whereClause, whereArgs)
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun select(context: Context, idItemShoppingList: Int): ItemShoppingList? {
        try {
            DataBaseDAO(context).readableDatabase.use { db ->
                db.query(
                    TABLE_NAME,
                    null,
                    FIELD_ID + " = ?",
                    arrayOf(idItemShoppingList.toString()),
                    null,
                    null,
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        return returnClassInstance(context, cursor)
                    }
                }
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
        return null
    }

    @JvmStatic
    @Throws(VansException::class)
    fun findLastInserted(context: Context, description: String): ItemShoppingList? {
        try {
            val db = DataBaseDAO(context).readableDatabase

            val cursor = db.query(
                TABLE_NAME,
                arrayOf(
                    FIELD_ID,
                    FIELD_IDSHOPPINGLIST,
                    FIELD_DESCRIPTION,
                    FIELD_UNITVALUE,
                    FIELD_QUANTITY,
                    FIELD_CHECKED
                ),
                FIELD_DESCRIPTION + " = ?",
                arrayOf(description.toString()),
                null,
                null,
                FIELD_ID + " DESC"
            )
            cursor.moveToFirst()
            if (cursor.moveToFirst()) {
                return returnClassInstance(context, cursor)
            }

            cursor.close()
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
        return null
    }

    @JvmStatic
    @Throws(VansException::class)
    fun selectAll(context: Context, filter: String?, idShoppingList: Int): Cursor {
        try {
            val orderBy = buildOrderByClause(context)
            var selection = FIELD_IDSHOPPINGLIST + " = ?"
            val selectionArgsList = ArrayList<String>()
            selectionArgsList.add(idShoppingList.toString())

            if (filter != null && !filter.isEmpty()) {
                selection += " AND " + FIELD_DESCRIPTION + " LIKE ?"
                selectionArgsList.add("%$filter%")
            }

            val db = DataBaseDAO(context).readableDatabase
            return db.query(
                TABLE_NAME,
                null,
                selection,
                selectionArgsList.toTypedArray<String>(),
                null,
                null,
                orderBy
            )
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    private fun buildOrderByClause(context: Context): String {
        val orderBy = StringBuilder()
        if (!UserPreferences.getItemListCheckedOrdenation(context).isEmpty()) {
            orderBy.append(FIELD_CHECKED).append(" ")
                .append(UserPreferences.getItemListCheckedOrdenation(context)).append(", ")
        }
        if (!UserPreferences.getItemListAlphabeticalOrdenation(context).isEmpty()) {
            orderBy.append(FIELD_DESCRIPTION).append(" ")
                .append(UserPreferences.getItemListAlphabeticalOrdenation(context)).append(", ")
        }
        orderBy.append(FIELD_ID).append(" DESC")
        return orderBy.toString()
    }

    @JvmStatic
    fun isAllItemsChecked(context: Context, idShoppingList: Int): Boolean {
        DataBaseDAO(context).readableDatabase.use { db ->
            db.query(
                TABLE_NAME,
                arrayOf(FIELD_CHECKED),
                FIELD_IDSHOPPINGLIST + " = ?",
                arrayOf(idShoppingList.toString()),
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    if (!cursor.getString(cursor.getColumnIndexOrThrow(FIELD_CHECKED))
                            .toBoolean()
                    ) {
                        return false
                    }
                }
                return true
            }
        }
    }

    @JvmStatic
    fun returnClassInstance(context: Context?, cursor: Cursor): ItemShoppingList {
        return ItemShoppingList(
            cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID)),
            cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_IDSHOPPINGLIST)),
            cursor.getString(cursor.getColumnIndexOrThrow(FIELD_DESCRIPTION)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_UNITVALUE)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_QUANTITY)),
            cursor.getString(cursor.getColumnIndexOrThrow(FIELD_CHECKED)).toBoolean()
        )
    }

    @JvmStatic
    @Throws(VansException::class)
    fun selectAutoComplete(
        context: Context,
        descriptionFilters: Array<String?>
    ): ArrayAdapter<String> {
        val list = ArrayList<String>()

        var selection: String? = null
        if (descriptionFilters.size > 0) {
            selection =
                FIELD_DESCRIPTION + " NOT IN (" + numOfParameters(descriptionFilters.size) + ")"
        }

        try {
            DataBaseDAO(context).readableDatabase.use { db ->
                db.query(
                    TABLE_NAME,
                    arrayOf(
                        FIELD_DESCRIPTION
                    ),
                    selection,
                    descriptionFilters,
                    FIELD_DESCRIPTION,
                    null,
                    FIELD_DESCRIPTION + " ASC"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        list.add(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_DESCRIPTION)))
                    }
                }
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }

        return ArrayAdapter(context, android.R.layout.simple_list_item_1, list)
    }

    private fun numOfParameters(num: Int): String {
        val result = StringBuilder()
        for (i in 0..<num) {
            result.append("?,")
        }
        return result.substring(0, result.length - 1)
    }

    @JvmStatic
    @Throws(VansException::class)
    fun update(context: Context, itemShoppingList: ItemShoppingList) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                val cv = buildContentValues(itemShoppingList)
                db.update(
                    TABLE_NAME,
                    cv,
                    FIELD_ID + " = ?",
                    arrayOf(itemShoppingList.id.toString())
                )
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun toString(context: Context, idShoppingList: Int): String {
        val result = StringBuilder()
        var totalValue = 0f

        try {
            selectAll(context, null, idShoppingList).use { c ->
                while (c.moveToNext()) {
                    val item = returnClassInstance(context, c)
                    totalValue += item.unitValue * item.quantity
                    result.append("\n").append(item.description)
                        .append(" - ")
                        .append(CustomFloatFormat.getSimpleFormatedValue(item.quantity.toDouble()))
                        .append(" x ").append(
                            CustomFloatFormat.getMonetaryMaskedValue(
                                context,
                                item.unitValue.toDouble()
                            )
                        )
                }
                if (result.length > 0) {
                    result.append("\n\n").append(context.getString(R.string.total)).append(" : ")
                        .append(
                            CustomFloatFormat.getMonetaryMaskedValue(
                                context,
                                totalValue.toDouble()
                            )
                        )
                }
            }
        } catch (e: Exception) {
            throw VansException(e.message, e)
        }

        return result.toString()
    }
}
