package com.halley.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "RideSharing";

	private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

	private static final String API_KEY = "apiKey";
	
	private static final String KEY_IS_DRIVER = "isDriver";
	
	private static final String AVATAR="link_avatar";
	
	private static final String FULL_NAME="fullname";

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	

	public void setLogin(boolean isLoggedIn, String api_key,boolean isDriver,String avatar,String fullname) {

		editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
		editor.putString(API_KEY, api_key);
		editor.putBoolean(KEY_IS_DRIVER, isDriver);
		editor.putString(AVATAR, avatar);
		editor.putString(FULL_NAME, fullname);
		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}
	
	public void setDriver(boolean isDriver) {
		editor.putBoolean(KEY_IS_DRIVER, isDriver);
		editor.commit();	
	}
	public void setAvatar(String avatar) {
		editor.putString(AVATAR, avatar);
		editor.commit();
	}
	public void setFullname(String fullname) {
		editor.putString(FULL_NAME, fullname);
		editor.commit();
	}

	public String getAPIKey() {
		return pref.getString(API_KEY, null);
	}
	public String getAvatar() {
		return pref.getString(AVATAR, null);
	}
	public String getFullname() {
		return pref.getString(FULL_NAME, null);
	}

	public boolean isLoggedIn() {
		return pref.getBoolean(KEY_IS_LOGGEDIN, false);
	}
	
	public boolean isDriver() {
		return pref.getBoolean(KEY_IS_DRIVER, false);
	}
}

