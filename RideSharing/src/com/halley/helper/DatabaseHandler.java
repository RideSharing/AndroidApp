package com.halley.helper;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String TAG = DatabaseHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "ridesharing";

	// Login table name
	private static final String TABLE_LOGIN = "user";

	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_FULLNAME = "fullname";
	private static final String KEY_APIKEY = "apiKey";
	private static final String KEY_PHONE = "phone";
	private static final String KEY_PERSIONALID = "personalID";
	private static final String KEY_PERSIONALID_IMG = "personalID_img";
	private static final String KEY_LINK_AVATAR = "link_avatar";
	private static final String KEY_CREATED_AT = "created_at";
	private static final String KEY_STATUS = "status";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_FULLNAME + " TEXT,"
				+ KEY_APIKEY + " TEXT UNIQUE," + KEY_PHONE + " TEXT,"
				+ KEY_PERSIONALID + " TEXT UNIQUE," + KEY_PERSIONALID_IMG + " TEXT,"
				+ KEY_LINK_AVATAR + " TEXT," + KEY_STATUS + " TEXT,"
				+ KEY_CREATED_AT + " TEXT" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);

		Log.d(TAG, "Database tables created");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String apiKey,String fullname,String phone,String personalID,
				String personalID_img,String link_avatar,String created_at,String status) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_APIKEY, apiKey); 
		values.put(KEY_FULLNAME, fullname); 
		values.put(KEY_PHONE, phone); 
		values.put(KEY_PERSIONALID, personalID);
		values.put(KEY_PERSIONALID_IMG, personalID_img);
		values.put(KEY_LINK_AVATAR, link_avatar);
		values.put(KEY_STATUS, status); 
		values.put(KEY_CREATED_AT, created_at);

		// Inserting Row
		long id = db.insert(TABLE_LOGIN, null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put(KEY_APIKEY, cursor.getString(1)); 
			user.put(KEY_FULLNAME, cursor.getString(2)); 
			user.put(KEY_PHONE, cursor.getString(3)); 
			user.put(KEY_PERSIONALID, cursor.getString(4));
			user.put(KEY_PERSIONALID_IMG, cursor.getString(5));
			user.put(KEY_LINK_AVATAR, cursor.getString(6));
			user.put(KEY_STATUS, cursor.getString(7)); 
			user.put(KEY_CREATED_AT, cursor.getString(8));
		}
		cursor.close();
		db.close();
		// return user
		Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

		return user;
	}

	/**
	 * Getting user login status return true if rows are there in table
	 * */
	public int getRowCount() {
		String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();

		// return row count
		return rowCount;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

}
