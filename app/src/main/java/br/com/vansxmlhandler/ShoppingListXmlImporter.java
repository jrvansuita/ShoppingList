package br.com.vansxmlhandler;

import java.sql.Date;
import java.text.ParseException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import br.com.activity.R;
import br.com.bean.ItemShoppingList;
import br.com.bean.ShoppingList;
import br.com.dao.DataBaseDAO;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansformat.CustomFloatFormat;

public class ShoppingListXmlImporter {
	private Document doc;
	private Context context;
	private ShoppingList shoppingList;
	private ItemShoppingList itemShoppingList;
	private boolean wasSucessful = false;

	public ShoppingListXmlImporter(Context context, Document doc) {
		this.context = context;
		this.shoppingList = new ShoppingList(context);
		this.itemShoppingList = new ItemShoppingList(context);
		this.doc = doc;
	}

	public ShoppingList getImportedShoppingList() {
		return shoppingList;
	}

	public boolean wasSucessful() {
		return wasSucessful;
	}

	public void importXml() throws DOMException, ParseException {
		wasSucessful = false;
		if (isImportableBase()) {
			NodeList tablesList = doc.getDocumentElement().getChildNodes();
			
			SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase(); 
			db.beginTransaction();			
			
			try {				
				
				for (int i = 0; i < tablesList.getLength(); i++) {

					Node tableNode = tablesList.item(i);

					if (isImportableTable(tableNode)) {
						NodeList rowsList = tableNode.getChildNodes();

						for (int j = 0; j < rowsList.getLength(); j++) {

							Node rowNode = rowsList.item(j);

							if (isImportableRow(rowNode)) {
								NodeList colsList = rowNode.getChildNodes();

								for (int k = 0; k < colsList.getLength(); k++) {

									Node colNode = colsList.item(k);

									if (isImportableCol(colNode)) {
										setColAttributesToTableClass(tableNode, colNode);
									} else {
										break;
									}
								}

								if (getTableName(tableNode).equalsIgnoreCase(ShoppingListDAO.TABLE_NAME)) {
									shoppingList = ShoppingListDAO.insert(context, db, shoppingList);
								} else if (getTableName(tableNode).equalsIgnoreCase(ItemShoppingListDAO.TABLE_NAME)) {
									ItemShoppingListDAO.insert(context, db, itemShoppingList);
								}

							} else {
								break;
							}
						}

					} else {
						break;
					}

				}

				db.setTransactionSuccessful();				
				wasSucessful = true;
			} catch (Exception e) {				
				wasSucessful = false;
				doToast(e.getMessage());
			}
			finally {
				db.endTransaction();
				db.close();
			}
		}
	}

	private void setColAttributesToTableClass(Node tableNode, Node colNode) throws DOMException, ParseException {

		if (getTableName(tableNode).equalsIgnoreCase(ShoppingListDAO.TABLE_NAME)) {

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ShoppingListDAO.FIELD_NAME)) {
				shoppingList.setName(context, colNode.getTextContent());
			}

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ShoppingListDAO.FIELD_DATELIST)) {
				shoppingList.setDate(new Date(Long.parseLong(colNode.getTextContent())));
			}
		} else if (getTableName(tableNode).equalsIgnoreCase(ItemShoppingListDAO.TABLE_NAME)) {

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ItemShoppingListDAO.FIELD_IDSHOPPINGLIST)) {
				itemShoppingList.setIdShoppingList(shoppingList.getId());
			}

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ItemShoppingListDAO.FIELD_CHECKED)) {
				itemShoppingList.setChecked(Boolean.valueOf(colNode.getTextContent()));
			}

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ItemShoppingListDAO.FIELD_DESCRIPTION)) {
				itemShoppingList.setDescription(colNode.getTextContent());
			}

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ItemShoppingListDAO.FIELD_UNITVALUE)) {
				itemShoppingList.setUnitValue(CustomFloatFormat.parseFloat(colNode.getTextContent()));
			}

			if (colNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue().equalsIgnoreCase(ItemShoppingListDAO.FIELD_QUANTITY)) {
				itemShoppingList.setQuantity(CustomFloatFormat.parseFloat(colNode.getTextContent()));
			}
		}
	}

	private boolean isImportableBase() {
		try {
			Element dataBaseNode = doc.getDocumentElement();

			return isValidNode((dataBaseNode.getNodeName().equalsIgnoreCase(CustomXmlBuilder.DATA_BASE_NODO_NAME))
					&& (dataBaseNode.getAttribute(CustomXmlBuilder.NAME).equalsIgnoreCase(DataBaseDAO.DATABASE_NAME)) && dataBaseNode.hasChildNodes());
		} catch (Exception e) {
			doToast(e.getMessage());
			return false;
		}
	}

	private boolean isImportableTable(Node tableNode) {
		try {
			return isValidNode(tableNode.getNodeName().equalsIgnoreCase(CustomXmlBuilder.TABLE_NODO_NAME) && tableNode.hasChildNodes() && tableNode.hasAttributes());
		} catch (Exception e) {
			doToast(e.getMessage());
			return false;
		}
	}

	private boolean isImportableRow(Node rowNode) {
		try {
			return isValidNode(rowNode.getNodeName().equalsIgnoreCase(CustomXmlBuilder.ROW_NODO_NAME) && rowNode.hasChildNodes() && (!rowNode.hasAttributes()));
		} catch (Exception e) {
			doToast(e.getMessage());
			return false;
		}
	}

	private boolean isImportableCol(Node colNode) {
		try {
			return isValidNode(colNode.getNodeName().equalsIgnoreCase(CustomXmlBuilder.COL_NODO_NAME) && colNode.hasAttributes());
		} catch (Exception e) {
			doToast(e.getMessage());
			return false;
		}
	}

	private boolean isValidNode(Boolean aValue) {
		if (!aValue) {
			doToast(context.getString(R.string.invalid_importable_xml));
		}
		return aValue;
	}

	private String getTableName(Node tableNode) {
		return tableNode.getAttributes().getNamedItem(CustomXmlBuilder.NAME).getNodeValue();
	}

	private void doToast(String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		((Activity) context).finish();
	}
}
