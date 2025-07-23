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

	public static ItemShoppingList insert(Context context, ItemShoppingList itemShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			ContentValues cv = buildContentValues(itemShoppingList);
			db.insertOrThrow(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	private static ContentValues buildContentValues(ItemShoppingList item) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_IDSHOPPINGLIST, item.getIdShoppingList());
		cv.put(FIELD_DESCRIPTION, item.getDescription());
		cv.put(FIELD_UNITVALUE, item.getUnitValue());
		cv.put(FIELD_QUANTITY, item.getQuantity());
		cv.put(FIELD_CHECKED, String.valueOf(item.isChecked()));
		return cv;
	}

	private static ItemShoppingList selectLast(Context context, SQLiteDatabase db) throws VansException {
		try (Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, FIELD_ID + " DESC", "1")) {
			if (cursor.moveToFirst()) {
				return returnClassInstance(context, cursor);
			}
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
		return null;
	}

	public static void checkAllItems(Context context, int idShoppingList, boolean selected) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			ContentValues cv = new ContentValues();
			cv.put(FIELD_CHECKED, String.valueOf(selected));
			db.update(TABLE_NAME, cv, FIELD_IDSHOPPINGLIST + " = ?", new String[]{String.valueOf(idShoppingList)});
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static void delete(Context context, int idItemShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			db.delete(TABLE_NAME, FIELD_ID + " = ?", new String[]{String.valueOf(idItemShoppingList)});
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static void deleteAllList(Context context, int idShoppingList, boolean onlyChecked) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			String whereClause = FIELD_IDSHOPPINGLIST + " = ?";
			String[] whereArgs = new String[]{String.valueOf(idShoppingList)};
			if (onlyChecked) {
				whereClause += " AND " + FIELD_CHECKED + " = ?";
				whereArgs = new String[]{String.valueOf(idShoppingList), String.valueOf(true)};
			}
			db.delete(TABLE_NAME, whereClause, whereArgs);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static ItemShoppingList select(Context context, int idItemShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			 Cursor cursor = db.query(TABLE_NAME, null, FIELD_ID + " = ?", new String[]{String.valueOf(idItemShoppingList)}, null, null, null)) {

			if (cursor.moveToFirst()) {
				return returnClassInstance(context, cursor);
			}
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

				return returnClassInstance(context, cursor);
			}

			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
		return null;
	}

	public static Cursor selectAll(Context context, String filter, int idShoppingList) throws VansException {
		try {
			String orderBy = buildOrderByClause(context);
			String selection = FIELD_IDSHOPPINGLIST + " = ?";
			ArrayList<String> selectionArgsList = new ArrayList<>();
			selectionArgsList.add(String.valueOf(idShoppingList));

			if (filter != null && !filter.isEmpty()) {
				selection += " AND " + FIELD_DESCRIPTION + " LIKE ?";
				selectionArgsList.add("%" + filter + "%");
			}

			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			return db.query(TABLE_NAME, null, selection, selectionArgsList.toArray(new String[0]), null, null, orderBy);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	private static String buildOrderByClause(Context context) {
		StringBuilder orderBy = new StringBuilder();
		if (!UserPreferences.getItemListCheckedOrdenation(context).isEmpty()) {
			orderBy.append(FIELD_CHECKED).append(" ").append(UserPreferences.getItemListCheckedOrdenation(context)).append(", ");
		}
		if (!UserPreferences.getItemListAlphabeticalOrdenation(context).isEmpty()) {
			orderBy.append(FIELD_DESCRIPTION).append(" ").append(UserPreferences.getItemListAlphabeticalOrdenation(context)).append(", ");
		}
		orderBy.append(FIELD_ID).append(" DESC");
		return orderBy.toString();
	}

	public static boolean isAllItemsChecked(Context context, int idShoppingList) {
		try (SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			 Cursor cursor = db.query(TABLE_NAME, new String[]{FIELD_CHECKED}, FIELD_IDSHOPPINGLIST + " = ?", new String[]{String.valueOf(idShoppingList)}, null, null, null)) {

			while (cursor.moveToNext()) {
				if (!Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_CHECKED)))) {
					return false;
				}
			}
			return true;
		}
	}

	public static ItemShoppingList returnClassInstance(Context context, Cursor cursor) {
		return new ItemShoppingList(
				cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID)),
				cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_IDSHOPPINGLIST)),
				cursor.getString(cursor.getColumnIndexOrThrow(FIELD_DESCRIPTION)),
				cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_UNITVALUE)),
				cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_QUANTITY)),
				Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_CHECKED)))
		);
	}

	public static ArrayAdapter<String> selectAutoComplete(Context context, String[] descriptionFilters) throws VansException {
		ArrayList<String> list = new ArrayList<>();

		String selection = null;
		if (descriptionFilters.length > 0) {
			selection = FIELD_DESCRIPTION + " NOT IN (" + numOfParameters(descriptionFilters.length) + ")";
		}

		try (SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			 Cursor cursor = db.query(TABLE_NAME, new String[]{FIELD_DESCRIPTION}, selection, descriptionFilters, FIELD_DESCRIPTION, null, FIELD_DESCRIPTION + " ASC")) {

			while (cursor.moveToNext()) {
				list.add(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_DESCRIPTION)));
			}
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
	}

	private static String numOfParameters(int num) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < num; i++) {
			result.append("?,");
		}
		return result.substring(0, result.length() - 1);
	}

	public static void update(Context context, ItemShoppingList itemShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			ContentValues cv = buildContentValues(itemShoppingList);
			db.update(TABLE_NAME, cv, FIELD_ID + " = ?", new String[]{String.valueOf(itemShoppingList.getId())});
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static String toString(Context context, int idShoppingList) throws VansException {
		StringBuilder result = new StringBuilder();
		float totalValue = 0f;

		try (Cursor c = selectAll(context, null, idShoppingList)) {
			while (c.moveToNext()) {
				ItemShoppingList item = returnClassInstance(context, c);
				totalValue += item.getUnitValue() * item.getQuantity();
				result.append("\n").append(item.getDescription())
						.append(" - ").append(CustomFloatFormat.getSimpleFormatedValue(item.getQuantity()))
						.append(" x ").append(CustomFloatFormat.getMonetaryMaskedValue(context, item.getUnitValue()));
			}

			if (result.length() > 0) {
				result.append("\n\n").append(context.getString(R.string.total)).append(" : ")
						.append(CustomFloatFormat.getMonetaryMaskedValue(context, totalValue));
			}

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return result.toString();
	}
}
