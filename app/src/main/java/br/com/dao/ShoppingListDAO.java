package br.com.dao;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import br.com.bean.ShoppingList;
import br.com.vansexception.VansException;
import br.com.vansschedule.AlarmeNotificationShoppingList;

public class ShoppingListDAO {

	public static final String TABLE_NAME = "SHOPPINGLIST";
	public static final String FIELD_ID = "_id";
	public static final String FIELD_NAME = "NAME";
	public static final String FIELD_DATELIST = "DATELIST";

	public static ShoppingList insert(Context context, SQLiteDatabase db, ShoppingList shoppingList) throws VansException {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, shoppingList.getName());
		cv.put(FIELD_DATELIST, shoppingList.getDate().getTime());
		try {
			db.insertOrThrow(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}

	public static ShoppingList insert(Context context, ShoppingList shoppingList) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, shoppingList.getName());
		cv.put(FIELD_DATELIST, shoppingList.getDate().getTime());
		try {
			db.insertOrThrow(TABLE_NAME, null, cv);
			return selectLast(context, db);
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

	}

	private static ShoppingList selectLast(Context context, SQLiteDatabase db) throws VansException {
		try {
			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_NAME, FIELD_DATELIST }, null, null, null, null, FIELD_ID + " desc");			
			if (cursor.moveToFirst()) {
				return returnClassInstace(context, cursor);
			}

			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return null;
	}

	public static ShoppingList select(Context context, int idShoppingList) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_NAME, FIELD_DATELIST, }, FIELD_ID + " = ?", new String[] { String.valueOf(idShoppingList) }, null, null, null);			
			if (cursor.moveToFirst()) {
				return returnClassInstace(context, cursor);
			}

			cursor.close();

		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

		return null;
	}

	public static void deleteAll(Context context) throws VansException {
		Cursor cursor = null;
		try {			
			cursor = selectAll(context);
			
			while (cursor.moveToNext()){
				delete(context, cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
			}
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}finally{
			cursor.close();
		}		
	}

	public static void delete(Context context, int idShoppingList) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();
			db.delete(TABLE_NAME, FIELD_ID + " = ? ", new String[] { String.valueOf(idShoppingList) });
			db.delete(ItemShoppingListDAO.TABLE_NAME, ItemShoppingListDAO.FIELD_IDSHOPPINGLIST + " = ? ", new String[] { String.valueOf(idShoppingList) });
			AlarmeNotificationShoppingList.cancelAlarme(context, idShoppingList);
			
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}

	}

	public static Cursor selectAll(Context context) throws VansException {
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();
			return db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_NAME, FIELD_DATELIST }, null, null, null, null, FIELD_ID + " desc");			
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}
	
/*	public static ArrayList<ShoppingList> selectAll(Context context) {
		ArrayList<ShoppingList> list = new ArrayList<ShoppingList>();
		try {
			SQLiteDatabase db = new DataBaseDAO(context).getReadableDatabase();

			Cursor cursor = db.query(TABLE_NAME, new String[] { FIELD_ID, FIELD_NAME, FIELD_DATELIST }, null, null, null, null, FIELD_ID + " desc");
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {

				list.add(new ShoppingList(context, cursor.getInt(cursor.getColumnIndex(FIELD_ID)), cursor.getString(cursor.getColumnIndex(FIELD_NAME)), new Date(cursor.getLong(cursor
						.getColumnIndex(FIELD_DATELIST)))));
				cursor.moveToNext();
			}

			cursor.close();

		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}

		return list;
	}*/
	
	public static ShoppingList returnClassInstace(Context context, Cursor cursor) {
		return new ShoppingList(context, cursor.getInt(cursor.getColumnIndex(FIELD_ID)), cursor.getString(cursor.getColumnIndex(FIELD_NAME)), new Date(cursor.getLong(cursor
				.getColumnIndex(FIELD_DATELIST))));
	}

	public static void update(Context context, ShoppingList shoppingList) throws VansException {
		SQLiteDatabase db = new DataBaseDAO(context).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, shoppingList.getName());

		try {
			db.update(TABLE_NAME, cv, FIELD_ID + " = ?", new String[] { String.valueOf(shoppingList.getId()) });
		} catch (Exception e) {
			throw new VansException(e.getMessage(), e);
		}
	}
	

}
