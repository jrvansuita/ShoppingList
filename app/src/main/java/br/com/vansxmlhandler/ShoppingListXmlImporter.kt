package br.com.vansxmlhandler

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import androidx.core.database.sqlite.transaction
import br.com.activity.R
import br.com.bean.ItemShoppingList
import br.com.bean.ShoppingList
import br.com.dao.DataBaseDAO
import br.com.dao.ItemShoppingListDAO
import br.com.dao.ShoppingListDAO
import br.com.vansformat.CustomFloatFormat
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.sql.Date
import java.text.ParseException

class ShoppingListXmlImporter(
    private val context: Context,
    private val doc: Document
) {
    private var shoppingList: ShoppingList = ShoppingList(
        0,
        context.getString(R.string.untitled),
        Date(System.currentTimeMillis())
    )
    private var itemShoppingList: ItemShoppingList = ItemShoppingList(
        0,
        0,
        context.getString(R.string.no_description),
        0f,
        0f,
        false
    )
    private var wasSucessful: Boolean = false

    fun getImportedShoppingList(): ShoppingList = shoppingList

    fun wasSucessful(): Boolean = wasSucessful

    @Throws(DOMException::class, ParseException::class)
    fun importXml() {
        wasSucessful = false
        if (isImportableBase()) {
            val tablesList = doc.documentElement.childNodes
            val db: SQLiteDatabase = DataBaseDAO(context).writableDatabase
            db.transaction {
                try {
                    for (i in 0 until tablesList.length) {
                        val tableNode = tablesList.item(i)
                        if (isImportableTable(tableNode)) {
                            val rowsList = tableNode.childNodes
                            for (j in 0 until rowsList.length) {
                                val rowNode = rowsList.item(j)
                                if (isImportableRow(rowNode)) {
                                    val colsList = rowNode.childNodes
                                    for (k in 0 until colsList.length) {
                                        val colNode = colsList.item(k)
                                        if (isImportableCol(colNode)) {
                                            setColAttributesToTableClass(tableNode, colNode)
                                        } else {
                                            break
                                        }
                                    }
                                    when (getTableName(tableNode)) {
                                        ShoppingListDAO.TABLE_NAME -> {
                                            shoppingList =
                                                ShoppingListDAO.insert(context, shoppingList)!!
                                        }

                                        ItemShoppingListDAO.TABLE_NAME -> {
                                            ItemShoppingListDAO.insert(context, itemShoppingList)
                                        }
                                    }
                                } else {
                                    break
                                }
                            }
                        } else {
                            break
                        }
                    }
                    wasSucessful = true
                } catch (e: Exception) {
                    wasSucessful = false
                    doToast(e.message)
                } finally {
                }
                db.close()
            }
        }
    }

    @Throws(DOMException::class, ParseException::class)
    private fun setColAttributesToTableClass(tableNode: Node, colNode: Node) {
        when (getTableName(tableNode)) {
            ShoppingListDAO.TABLE_NAME -> {
                when (colNode.attributes.getNamedItem(CustomXmlBuilder.NAME).nodeValue) {
                    ShoppingListDAO.FIELD_NAME -> shoppingList.name = colNode.textContent
                    ShoppingListDAO.FIELD_DATELIST -> shoppingList.date =
                        Date(colNode.textContent.toLong())
                }
            }

            ItemShoppingListDAO.TABLE_NAME -> {
                when (colNode.attributes.getNamedItem(CustomXmlBuilder.NAME).nodeValue) {
                    ItemShoppingListDAO.FIELD_IDSHOPPINGLIST -> itemShoppingList.idShoppingList =
                        shoppingList.id

                    ItemShoppingListDAO.FIELD_CHECKED -> itemShoppingList.checked =
                        colNode.textContent.toBoolean()

                    ItemShoppingListDAO.FIELD_DESCRIPTION -> itemShoppingList.description =
                        colNode.textContent

                    ItemShoppingListDAO.FIELD_UNITVALUE -> itemShoppingList.unitValue =
                        CustomFloatFormat.parseFloat(colNode.textContent)

                    ItemShoppingListDAO.FIELD_QUANTITY -> itemShoppingList.quantity =
                        CustomFloatFormat.parseFloat(colNode.textContent)
                }
            }
        }
    }

    private fun isImportableBase(): Boolean {
        return try {
            val dataBaseNode = doc.documentElement
            isValidNode(
                dataBaseNode.nodeName.equals(
                    CustomXmlBuilder.DATA_BASE_NODO_NAME,
                    ignoreCase = true
                )
                        && dataBaseNode.getAttribute(CustomXmlBuilder.NAME)
                    .equals(DataBaseDAO.DATABASE_NAME, ignoreCase = true)
                        && dataBaseNode.hasChildNodes()
            )
        } catch (e: Exception) {
            doToast(e.message)
            false
        }
    }

    private fun isImportableTable(tableNode: Node): Boolean {
        return try {
            isValidNode(
                tableNode.nodeName.equals(CustomXmlBuilder.TABLE_NODO_NAME, ignoreCase = true)
                        && tableNode.hasChildNodes()
                        && tableNode.hasAttributes()
            )
        } catch (e: Exception) {
            doToast(e.message)
            false
        }
    }

    private fun isImportableRow(rowNode: Node): Boolean {
        return try {
            isValidNode(
                rowNode.nodeName.equals(CustomXmlBuilder.ROW_NODO_NAME, ignoreCase = true)
                        && rowNode.hasChildNodes()
                        && !rowNode.hasAttributes()
            )
        } catch (e: Exception) {
            doToast(e.message)
            false
        }
    }

    private fun isImportableCol(colNode: Node): Boolean {
        return try {
            isValidNode(
                colNode.nodeName.equals(CustomXmlBuilder.COL_NODO_NAME, ignoreCase = true)
                        && colNode.hasAttributes()
            )
        } catch (e: Exception) {
            doToast(e.message)
            false
        }
    }

    private fun isValidNode(aValue: Boolean): Boolean {
        if (!aValue) {
            doToast(context.getString(R.string.invalid_importable_xml))
        }
        return aValue
    }

    private fun getTableName(tableNode: Node): String =
        tableNode.attributes.getNamedItem(CustomXmlBuilder.NAME).nodeValue

    private fun doToast(message: String?) {
        Toast.makeText(context, message ?: "", Toast.LENGTH_LONG).show()
        (context as? Activity)?.finish()
    }
}
