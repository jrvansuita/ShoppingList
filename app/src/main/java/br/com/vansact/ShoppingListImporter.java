package br.com.vansact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import br.com.activity.R;
import br.com.vansxmlhandler.ShoppingListXmlImporter;

public class ShoppingListImporter extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Document doc = null;

		if (getIntent().getScheme().equals("content") || getIntent().getScheme().equals("file")) {
			try {
				InputStream attachment = getContentResolver().openInputStream(getIntent().getData());
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				doc = builder.parse(attachment);
				doc.getDocumentElement().normalize();
				attachment.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			
			
			ShoppingListXmlImporter slImporter = new ShoppingListXmlImporter(this, doc);
			slImporter.importXml();
			if (slImporter.wasSucessful()) {
				
				startActivity(new Intent(this, AddItemShoppingList.class).putExtra(getString(R.string.id_shopping_list), slImporter.getImportedShoppingList().getId()));
			}
			
		} catch (DOMException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		} catch ( ParseException e2){
            Toast.makeText(this, e2.getMessage(), Toast.LENGTH_LONG).show();
        }
	}
	
	
	@Override
	protected void onRestart() {
		android.support.v4.app.NavUtils.navigateUpFromSameTask(this);
		super.onRestart();
	}
	
}
