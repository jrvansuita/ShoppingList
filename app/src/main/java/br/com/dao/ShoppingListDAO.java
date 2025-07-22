package br.com.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import br.com.bean.ShoppingList;
import br.com.vansexception.VansException;
import br.com.vansschedule.AlarmNotificationShoppingList;

public class ShoppingListDAO {

	public static final String TABLE_NAME = "SHOPPINGLIST";
	public static final String FIELD_ID = "_id";
	public static final String FIELD_NAME = "NAME";
	public static final String FIELD_DATELIST = "DATELIST";

	public static ShoppingList insert(Context context, ShoppingList shoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			return insertInternal(context, db, shoppingList);
		}
	}

	private static ShoppingList insertInternal(Context context, SQLiteDatabase db, ShoppingList shoppingList) throws VansException {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, shoppingList.getName());
		cv.put(FIELD_DATELIST, shoppingList.getDate().getTime());
		try {
			db.insertOrThrow(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException("Error inserting ShoppingList", e);
		}
	}

	private static ShoppingList selectLast(Context context, SQLiteDatabase db) throws VansException {
		try (Cursor cursor = db.query(TABLE_NAME,
				new String[]{FIELD_ID, FIELD_NAME, FIELD_DATELIST},
				null, null, null, null, FIELD_ID + " DESC")) {

			if (cursor.moveToFirst()) {
				return returnClassInstance(context, cursor);
			}
		} catch (Exception e) {
			throw new VansException("Error selecting last ShoppingList", e);
		}
		return null;
	}

	public static ShoppingList select(Context context, int idShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			 Cursor cursor = db.query(TABLE_NAME,
					 new String[]{FIELD_ID, FIELD_NAME, FIELD_DATELIST},
					 FIELD_ID + " = ?",
					 new String[]{String.valueOf(idShoppingList)},
					 null, null, null)) {

			if (cursor.moveToFirst()) {
				return returnClassInstance(context, cursor);
			}
		} catch (Exception e) {
			throw new VansException("Error selecting ShoppingList by ID", e);
		}
		return null;
	}

	public static void deleteAll(Context context) throws VansException {
		try (Cursor cursor = selectAll(context)) {
			while (cursor != null && cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndex(FIELD_ID);
				if (idIndex != -1) {
					delete(context, cursor.getInt(idIndex));
				}
			}
		} catch (Exception e) {
			throw new VansException("Error deleting all ShoppingLists", e);
		}
	}

	public static void delete(Context context, int idShoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			db.delete(TABLE_NAME, FIELD_ID + " = ?", new String[]{String.valueOf(idShoppingList)});
			db.delete(ItemShoppingListDAO.TABLE_NAME, ItemShoppingListDAO.FIELD_IDSHOPPINGLIST + " = ?", new String[]{String.valueOf(idShoppingList)});
			AlarmNotificationShoppingList.cancelAlarm(context, idShoppingList);
		} catch (Exception e) {
			throw new VansException("Error deleting ShoppingList", e);
		}
	}

	public static Cursor selectAll(Context context) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			return db.query(TABLE_NAME,
					new String[]{FIELD_ID, FIELD_NAME, FIELD_DATELIST},
					null, null, null, null, FIELD_ID + " DESC");
		} catch (Exception e) {
			throw new VansException("Error selecting all ShoppingLists", e);
		}
	}

	public static ShoppingList returnClassInstance(Context context, Cursor cursor) {
		int idIndex = cursor.getColumnIndex(FIELD_ID);
		int nameIndex = cursor.getColumnIndex(FIELD_NAME);
		int dateIndex = cursor.getColumnIndex(FIELD_DATELIST);

		if (idIndex == -1 || nameIndex == -1 || dateIndex == -1) {
			return null; // safety fallback
		}

		int id = cursor.getInt(idIndex);
		String name = cursor.getString(nameIndex);
		long dateMillis = cursor.getLong(dateIndex);
		return new ShoppingList(context, id, name, new Date(dateMillis));
	}

	public static void update(Context context, ShoppingList shoppingList) throws VansException {
		try (SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase()) {
			ContentValues cv = new ContentValues();
			cv.put(FIELD_NAME, shoppingList.getName());
			db.update(TABLE_NAME, cv, FIELD_ID + " = ?", new String[]{String.valueOf(shoppingList.getId())});
		} catch (Exception e) {
			throw new VansException("Error updating ShoppingList", e);
		}
	}
}
