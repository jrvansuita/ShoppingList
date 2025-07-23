package br.com.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBaseDAO extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "LISTADECOMPRAS";
	public static final int DATABASE_VERSION = 15;
	private Context context;

	public DataBaseDAO(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE SHOPPINGLIST(_id INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,DATELIST INTEGER);");
		db.execSQL("CREATE TABLE ITEMSHOPPINGLIST(_id INTEGER PRIMARY KEY AUTOINCREMENT, IDSHOPPINGLIST INTEGER, DESCRIPTION TEXT, UNITVALUE FLOAT, QUANTITY FLOAT, CHECKED VARCHAR(1));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			if ((newVersion <= 13)) {
				db.execSQL("ALTER TABLE ITEMSHOPPINGLIST ADD COLUMN QUANTITY FLOAT DEFAULT 0;");
			}

			if ((newVersion == 15)) {
			
				db.beginTransaction();
				db.execSQL("CREATE TABLE SHOPPINGLISTX(_id INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,DATELIST INTEGER);");
				db.execSQL("INSERT INTO SHOPPINGLISTX(_id,NAME,DATELIST) SELECT ID,NAME,DATELIST FROM SHOPPINGLIST;");
				db.execSQL("DROP TABLE IF EXISTS SHOPPINGLIST;");
				
			
				db.execSQL("CREATE TABLE ITEMSHOPPINGLISTX(_id INTEGER PRIMARY KEY AUTOINCREMENT, IDSHOPPINGLIST INTEGER, DESCRIPTION TEXT, UNITVALUE FLOAT, QUANTITY FLOAT, CHECKED VARCHAR(1));");
				db.execSQL("INSERT INTO ITEMSHOPPINGLISTX(_id,IDSHOPPINGLIST,DESCRIPTION,UNITVALUE,QUANTITY,CHECKED) SELECT ID,IDSHOPPINGLIST,DESCRIPTION,UNITVALUE,QUANTITY,CHECKED FROM ITEMSHOPPINGLIST;");				
				db.execSQL("DROP TABLE IF EXISTS ITEMSHOPPINGLIST;");
				
				onCreate(db);
				db.execSQL("INSERT INTO SHOPPINGLIST(_id,NAME,DATELIST) SELECT _id,NAME,DATELIST FROM SHOPPINGLISTX;");
				db.execSQL("INSERT INTO ITEMSHOPPINGLIST(_id,IDSHOPPINGLIST,DESCRIPTION,UNITVALUE,QUANTITY,CHECKED) SELECT _id,IDSHOPPINGLIST,DESCRIPTION,UNITVALUE,QUANTITY,CHECKED FROM ITEMSHOPPINGLISTX;");
				db.setTransactionSuccessful();

			}

		} catch (Exception e) {			
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}finally {
			db.endTransaction();
		}
	}
}
