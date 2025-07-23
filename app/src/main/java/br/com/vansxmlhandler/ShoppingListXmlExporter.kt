package br.com.vansxmlhandler

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import br.com.dao.DataBaseDAO
import br.com.dao.ItemShoppingListDAO
import br.com.dao.ShoppingListDAO
import br.com.vansexception.VansException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class ShoppingListXmlExporter(private val context: Context) {
    private val customXmlBuilder = CustomXmlBuilder().apply {
        start(DataBaseDAO.DATABASE_NAME)
    }
    private val db: SQLiteDatabase = DataBaseDAO(context).writableDatabase
    private var idShoppingList: Int = 0

    @Throws(VansException::class)
    fun export(idShoppingList: Int): File? {
        val fileName = ShoppingListDAO.select(context, idShoppingList)?.name ?: "null"
        return try {
            this.idShoppingList = idShoppingList
            buildXmlTable(ShoppingListDAO.TABLE_NAME, ShoppingListDAO.FIELD_ID)
            buildXmlTable(ItemShoppingListDAO.TABLE_NAME, ItemShoppingListDAO.FIELD_IDSHOPPINGLIST)
            val xmlString = customXmlBuilder.end()
            writeToFile(xmlString, fileName)
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun buildXmlTable(tableName: String, filterFieldName: String) {
        try {
            customXmlBuilder.openTable(tableName)
            val sql = "select * from $tableName where $filterFieldName = $idShoppingList"
            val c: Cursor = db.rawQuery(sql, emptyArray())
            if (c.moveToFirst()) {
                val cols = c.columnCount
                do {
                    customXmlBuilder.openRow()
                    for (i in 0 until cols) {
                        customXmlBuilder.addColumn(c.getColumnName(i), c.getString(i))
                    }
                    customXmlBuilder.closeRow()
                } while (c.moveToNext())
            }
            c.close()
            customXmlBuilder.closeTable()
        } catch (e: Exception) {
            Toast.makeText(
                context, "Error exporting table $tableName - ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(xmlString: String, exportFileName: String): File {
        val outputDir = context.externalCacheDir
        val file = File.createTempFile("${exportFileName}__", ".slx", outputDir)
        file.createNewFile()
        val buff = ByteBuffer.wrap(xmlString.toByteArray())
        FileOutputStream(file).channel.use { channel ->
            channel.write(buff)
        }
        return file
    }
}


