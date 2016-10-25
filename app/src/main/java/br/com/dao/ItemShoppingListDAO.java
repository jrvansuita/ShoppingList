package br.com.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import br.com.activity.R;
import br.com.bean.ItemShoppingList;
import br.com.vansexception.VansException;
import br.com.vansformat.CustomFloatFormat;
import br.com.vansprefs.UserPreferences;

public class ItemShoppingListDAO {

	public static final String TABLE_NAME = "ITEMSHOPPINGLIST";
	public static final String FIELD_ID = "_id";
	public static final String FIELD_IDSHOPPINGLIST = "IDSHOPPINGLIST";
	public static final String FIELD_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_UNITVALUE = "UNITVALUE";
	public static final String FIELD_QUANTITY = "QUANTITY";
	public static final String FIELD_CHECKED = "CHECKED";

	public static ItemShoppingList insert(Context context, SQLiteDatabase db, ItemShoppingList itemShoppingList) throws VansException {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_IDSHOPPINGLIST, itemShoppingList.getIdShoppingList());
		cv.put(FIELD_DESCRIPTION, itemShoppingList.getDescription());
		cv.put(FIELD_UNITVALUE, itemShoppingList.getUnitValue());
		cv.put(FIELD_QUANTITY, itemShoppingList.getQuantity());
		cv.put(FIELD_CHECKED, String.valueOf(itemShoppingList.isChecked()));

		try {
			db.insert(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static ItemShoppingList insert(Context context, ItemShoppingList itemShoppingList) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(FIELD_IDSHOPPINGLIST, itemShoppingList.getIdShoppingList());
		cv.put(FIELD_DESCRIPTION, itemShoppingList.getDescription());
		cv.put(FIELD_UNITVALUE, itemShoppingList.getUnitValue());
		cv.put(FIELD_QUANTITY, itemShoppingList.getQuantity());
		cv.put(FIELD_CHECKED, String.valueOf(itemShoppingList.isChecked()));

		try {
			db.insert(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	private static ItemShoppingList selectLast(Context context, SQLiteDatabase db) throws VansException {
		try {
			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_IDSHOPPINGLIST, FIELD_DESCRIPTION, FIELD_UNITVALUE, FIELD_QUANTITY, FIELD_CHECKED }, null, null, null, null, FIELD_ID + " desc");

			if (cursor.moveToNext()) {
				return returnClassInstace(context, cursor);
			}

			cursor.close();
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return null;
	}

	public static void checkAllItens(Context context, int idShoppingList, boolean selected) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(FIELD_CHECKED, String.valueOf(selected));
		try {
			db.update(TABLE_NAME, cv, FIELD_IDSHOPPINGLIST + " = ?", new String[]{String.valueOf(idShoppingList)});
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static void delete(Context context, int idItemShoppingList) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();
			db.delete(TABLE_NAME, FIELD_ID + " = ? ", new String[] { String.valueOf(idItemShoppingList) });
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static void deleteAllLista(Context context, int idShoppingList, boolean onlyCheckeds) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();
			String whereClause = FIELD_IDSHOPPINGLIST + " = ? ";
			String[] whereArgs = new String[] { String.valueOf(idShoppingList) };

			if (onlyCheckeds) {
				whereClause = whereClause + " AND " + FIELD_CHECKED + " = ? ";
				whereArgs = new String[] { String.valueOf(idShoppingList), String.valueOf(onlyCheckeds) };
			}

			db.delete(TABLE_NAME, whereClause, whereArgs);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static ItemShoppingList select(Context context, int idItemShoppingList) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();

			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_IDSHOPPINGLIST, FIELD_DESCRIPTION, FIELD_UNITVALUE, FIELD_QUANTITY, FIELD_CHECKED }, FIELD_ID + " = ?", new String[] { String.valueOf(idItemShoppingList) }, null, null, null);

			if (cursor.moveToNext()) {

				return returnClassInstace(context, cursor);
			}

			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
		return null;
	}

	public static ItemShoppingList findLastInserted(Context context, String description) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();

			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_IDSHOPPINGLIST, FIELD_DESCRIPTION, FIELD_UNITVALUE, FIELD_QUANTITY, FIELD_CHECKED }, FIELD_DESCRIPTION + " = ?", new String[] { String.valueOf(description) }, null, null, FIELD_ID + " DESC");
			cursor.moveToFirst();
			if (cursor.moveToFirst()) {

				return returnClassInstace(context, cursor);
			}

			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
		return null;
	}

	public static Cursor selectAll(Context context, String filter, int idShoppingList) throws VansException {
		try {
			String orderBy = "";

			if (!UserPreferences.getItemListCheckedOrdenation(context).isEmpty()) {
				orderBy = FIELD_CHECKED + " " + UserPreferences.getItemListCheckedOrdenation(context) + ",";
			}

			if (!UserPreferences.getItemListAlphabeticalOrdenation(context).isEmpty()) {
				orderBy += FIELD_DESCRIPTION + " " + UserPreferences.getItemListAlphabeticalOrdenation(context) + ",";
			}

			 orderBy +=  FIELD_ID + " DESC ";

			String where = "";
			if (filter != null){
				where = " AND " + FIELD_DESCRIPTION + " LIKE '%" + filter + "%'";
			}

			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			return db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_IDSHOPPINGLIST, FIELD_DESCRIPTION, FIELD_UNITVALUE, FIELD_QUANTITY, FIELD_CHECKED }, FIELD_IDSHOPPINGLIST + " = ?" + where, new String[] { String.valueOf(idShoppingList) }, null, null, orderBy);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static boolean isAllItemsChecked(Context context, int idShoppingList) {
		boolean is = false;
		SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_IDSHOPPINGLIST, FIELD_CHECKED }, FIELD_IDSHOPPINGLIST + " = ?", new String[] { String.valueOf(idShoppingList) }, null, null, null);

		if (cursor.moveToFirst()) {
			is = true;
			do {
				is = is && Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(FIELD_CHECKED)));
			} while (cursor.moveToNext() && is);

		}
		cursor.close();
		return is;
	}

	public static ItemShoppingList returnClassInstace(Context context, Cursor cursor) {
		return new ItemShoppingList(cursor.getInt(cursor.getColumnIndex(FIELD_ID)), cursor.getInt(cursor.getColumnIndex(FIELD_IDSHOPPINGLIST)), cursor.getString(cursor.getColumnIndex(FIELD_DESCRIPTION)), cursor.getFloat(cursor.getColumnIndex(FIELD_UNITVALUE)), cursor.getFloat(cursor
				.getColumnIndex(FIELD_QUANTITY)), Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(FIELD_CHECKED))));
	}

	public static ArrayAdapter<String> selectAutoComplete(Context context, String[] descriptionFilters) throws VansException {
		ArrayList<String> list = new ArrayList<String>();

		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();

			String selection = null;

			if (descriptionFilters.length > 0) {
				selection = FIELD_DESCRIPTION + " not in (" + numOfParameters(descriptionFilters.length) + ")";
			}

			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_DESCRIPTION }, selection, descriptionFilters, FIELD_DESCRIPTION, null, FIELD_DESCRIPTION + " asc");
			while (cursor.moveToNext()) {
				list.add(cursor.getString(cursor.getColumnIndex(FIELD_DESCRIPTION)));
			}
			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list);
	}

	public static void update(Context context, ItemShoppingList itemShoppingList) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(FIELD_DESCRIPTION, itemShoppingList.getDescription());
		cv.put(FIELD_UNITVALUE, itemShoppingList.getUnitValue());
		cv.put(FIELD_QUANTITY, itemShoppingList.getQuantity());
		cv.put(FIELD_CHECKED, String.valueOf(itemShoppingList.isChecked()));
		try {
			db.update(TABLE_NAME, cv, FIELD_ID + " = ?", new String[] { String.valueOf(itemShoppingList.getId()) });
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static String toString(Context context, int idShoppingList) throws VansException {
		String result = "";
		float totalValue = 0;
		try {
			Cursor c = selectAll(context, null, idShoppingList);
			while (c.moveToNext()) {
				ItemShoppingList item = returnClassInstace(context, c);
				totalValue = totalValue + item.getUnitValue() * item.getQuantity();
				result = result + " \n" + item.getDescription() + " - " + CustomFloatFormat.getSimpleFormatedValue(item.getQuantity()) + " x " + CustomFloatFormat.getMonetaryMaskedValue(context, item.getUnitValue());
			}
			c.close();

			if (!result.isEmpty()) {
				result = result + "\n\n" + context.getString(R.string.total) + " : " + CustomFloatFormat.getMonetaryMaskedValue(context, totalValue);
			}

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return result;
	}

	private static String numOfParameters(int num) {
		String result = "";
		for (int i = 0; i < num; i++) {
			result = result + "?,";
		}
		return result.substring(0, result.length() - 1);

	}

}
