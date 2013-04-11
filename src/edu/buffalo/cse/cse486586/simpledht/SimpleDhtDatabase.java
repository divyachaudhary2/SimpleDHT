package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SimpleDhtDatabase extends SQLiteOpenHelper{
	public static final String debug="DB";
	static final String dbName="DhtDB";
	public static final String Dht_Table="Dht_Table";
	public static final String key="key";
	public static final String value="value";
	static int version=1;
	public SimpleDhtDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, dbName, null,33); 
	}

	public void onCreate(SQLiteDatabase db) {
		try{
			Log.d(debug, "creating database");
		db.execSQL("CREATE TABLE "+Dht_Table+" ("+key+ " TEXT PRIMARY KEY , "+
				value+ " TEXT)");
		}catch(Exception ex){
			Log.d(debug,"exception in creating db");
			ex.printStackTrace();
		}
		
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+Dht_Table);
		onCreate(db);
	}
}
