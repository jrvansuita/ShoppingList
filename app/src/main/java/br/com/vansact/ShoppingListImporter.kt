package br.com.vansact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NavUtils
import br.com.activity.R
import br.com.vansact.AddItemShoppingList
import br.com.vansxmlhandler.ShoppingListXmlImporter
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class ShoppingListImporter : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var doc: Document? = null

        if (intent.scheme == "content" || intent.scheme == "file") {
            try {
                val attachment = contentResolver.openInputStream(
                    intent.data!!
                )
                val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                doc = builder.parse(attachment)
                doc.documentElement.normalize()
                attachment!!.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        try {
            val slImporter = ShoppingListXmlImporter(
                this,
                doc!!
            )
            slImporter.importXml()
            if (slImporter.wasSucessful()) {
                startActivity(
                    Intent(
                        this,
                        AddItemShoppingList::class.java
                    ).putExtra(
                        getString(R.string.id_shopping_list),
                        slImporter.getImportedShoppingList().id
                    )
                )
            }
        } catch (e: DOMException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        } catch (e2: ParseException) {
            Toast.makeText(this, e2.message, Toast.LENGTH_LONG).show()
        }
    }


    override fun onRestart() {
        NavUtils.navigateUpFromSameTask(this)
        super.onRestart()
    }
}
