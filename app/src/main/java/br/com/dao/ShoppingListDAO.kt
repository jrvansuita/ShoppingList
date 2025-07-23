package br.com.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import br.com.bean.ShoppingList
import br.com.vansexception.VansException
import br.com.vansschedule.AlarmNotificationShoppingList
import java.util.Date

object ShoppingListDAO {
    const val TABLE_NAME: String = "SHOPPINGLIST"
    const val FIELD_ID: String = "_id"
    const val FIELD_NAME: String = "NAME"
    const val FIELD_DATELIST: String = "DATELIST"

    @JvmStatic
    @Throws(VansException::class)
    fun insert(context: Context, shoppingList: ShoppingList): ShoppingList? {
        DataBaseDAO(context).writableDatabase.use { db ->
            return insertInternal(context, db, shoppingList)
        }
    }

    @Throws(VansException::class)
    private fun insertInternal(
        context: Context,
        db: SQLiteDatabase,
        shoppingList: ShoppingList
    ): ShoppingList? {
        val cv = ContentValues()
        cv.put(FIELD_NAME, shoppingList.name)
        cv.put(FIELD_DATELIST, shoppingList.date.time)
        try {
            db.insertOrThrow(TABLE_NAME, null, cv)
            return selectLast(context, db)
        } catch (e: Exception) {
            throw VansException("Error inserting ShoppingList", e)
        }
    }

    @Throws(VansException::class)
    private fun selectLast(context: Context, db: SQLiteDatabase): ShoppingList? {
        try {
            db.query(
                TABLE_NAME,
                arrayOf(FIELD_ID, FIELD_NAME, FIELD_DATELIST),
                null, null, null, null, FIELD_ID + " DESC"
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    return returnClassInstance(cursor)
                }
            }
        } catch (e: Exception) {
            throw VansException("Error selecting last ShoppingList", e)
        }
        return null
    }

    @JvmStatic
    @Throws(VansException::class)
    fun select(context: Context, idShoppingList: Int): ShoppingList? {
        try {
            DataBaseDAO(context).readableDatabase.use { db ->
                db.query(
                    TABLE_NAME,
                    arrayOf(FIELD_ID, FIELD_NAME, FIELD_DATELIST),
                    FIELD_ID + " = ?",
                    arrayOf(idShoppingList.toString()),
                    null, null, null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        return returnClassInstance(cursor)
                    }
                }
            }
        } catch (e: Exception) {
            throw VansException("Error selecting ShoppingList by ID", e)
        }
        return null
    }

    @JvmStatic
    @Throws(VansException::class)
    fun deleteAll(context: Context) {
        try {
            selectAll(context).use { cursor ->
                while (cursor.moveToNext()) {
                    val idIndex = cursor.getColumnIndex(FIELD_ID)
                    if (idIndex != -1) {
                        delete(context, cursor.getInt(idIndex))
                    }
                }
            }
        } catch (e: Exception) {
            throw VansException("Error deleting all ShoppingLists", e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun delete(context: Context, idShoppingList: Int) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                db.delete(TABLE_NAME, FIELD_ID + " = ?", arrayOf(idShoppingList.toString()))
                db.delete(
                    ItemShoppingListDAO.TABLE_NAME,
                    ItemShoppingListDAO.FIELD_IDSHOPPINGLIST + " = ?",
                    arrayOf(idShoppingList.toString())
                )
                AlarmNotificationShoppingList.cancelAlarm(context, idShoppingList)
            }
        } catch (e: Exception) {
            throw VansException("Error deleting ShoppingList", e)
        }
    }

    @JvmStatic
    @Throws(VansException::class)
    fun selectAll(context: Context): Cursor {
        try {
            val db = DataBaseDAO(context).readableDatabase
            return db.query(
                TABLE_NAME,
                arrayOf(FIELD_ID, FIELD_NAME, FIELD_DATELIST),
                null, null, null, null, FIELD_ID + " DESC"
            )
        } catch (e: Exception) {
            throw VansException("Error selecting all ShoppingLists", e)
        }
    }

    @JvmStatic
    fun returnClassInstance(cursor: Cursor): ShoppingList? {
        val idIndex = cursor.getColumnIndex(FIELD_ID)
        val nameIndex = cursor.getColumnIndex(FIELD_NAME)
        val dateIndex = cursor.getColumnIndex(FIELD_DATELIST)

        if (idIndex == -1 || nameIndex == -1 || dateIndex == -1) {
            return null // safety fallback
        }

        val id = cursor.getInt(idIndex)
        val name = cursor.getString(nameIndex)
        val dateMillis = cursor.getLong(dateIndex)
        return ShoppingList(id, name, Date(dateMillis))
    }

    @JvmStatic
    @Throws(VansException::class)
    fun update(context: Context, shoppingList: ShoppingList) {
        try {
            DataBaseDAO(context).writableDatabase.use { db ->
                val cv = ContentValues()
                cv.put(FIELD_NAME, shoppingList.name)
                db.update(TABLE_NAME, cv, FIELD_ID + " = ?", arrayOf(shoppingList.id.toString()))
            }
        } catch (e: Exception) {
            throw VansException("Error updating ShoppingList", e)
        }
    }

    @Throws(VansException::class)
    fun count(context: Context): Int {
        return try {
            val db = DataBaseDAO(context).readableDatabase
            val cursor: Cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            throw VansException("Error select count ShoppingList", e)
        }
    }
}
