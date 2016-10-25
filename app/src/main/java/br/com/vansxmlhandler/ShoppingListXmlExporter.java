package br.com.vansxmlhandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import br.com.dao.DataBaseDAO;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansexception.VansException;

public class ShoppingListXmlExporter {
	private CustomXmlBuilder customXmlBuilder;
	private SQLiteDatabase db;
	private Context context;
	private int idShoppingList;

	public ShoppingListXmlExporter(Context context) throws IOException {
		this.customXmlBuilder = new CustomXmlBuilder();
		this.customXmlBuilder.start(DataBaseDAO.DATABASE_NAME);

		this.db = new DataBaseDAO(context).getWritableDatabase();
		this.context = context;
	}

	public File export(int idShoppingList) throws VansException {
		String fileName = ShoppingListDAO.select(context, idShoppingList).getName();

		try {
			this.idShoppingList = idShoppingList;

			buildXmlTable(ShoppingListDAO.TABLE_NAME, ShoppingListDAO.FIELD_ID);
			buildXmlTable(ItemShoppingListDAO.TABLE_NAME, ItemShoppingListDAO.FIELD_IDSHOPPINGLIST);

			String xmlString = customXmlBuilder.end();

			return writeToFile(xmlString, fileName);
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}
	}

	private void buildXmlTable(String tableName, String filterFielName) {
		try {
			customXmlBuilder.openTable(tableName);
			String sql = "select * from " + tableName + " where " + filterFielName + " = " + idShoppingList;
			Cursor c = db.rawQuery(sql, new String[0]);
			if (c.moveToFirst()) {
				int cols = c.getColumnCount();
				do {
					customXmlBuilder.openRow();
					for (int i = 0; i < cols; i++) {
						customXmlBuilder.addColumn(c.getColumnName(i), c.getString(i));
					}
					customXmlBuilder.closeRow();
				} while (c.moveToNext());
			}
			c.close();

			customXmlBuilder.closeTable();

		} catch (Exception e) {
			Toast.makeText(context, "Error exporting table " + tableName + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

	
	private File writeToFile(final String xmlString, final String exportFileName) throws IOException {			
		File outputDir = context.getExternalCacheDir(); // context being the Activity pointer
		File file = File.createTempFile(exportFileName +"__" , ".slx", outputDir);
		file.createNewFile();

		ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
		@SuppressWarnings("resource")
		FileChannel channel = new FileOutputStream(file).getChannel();
		try {
			channel.write(buff);
		} finally {
			if (channel != null) {
				channel.close();			
			}
		}
		return file;
	}

	
	

}
