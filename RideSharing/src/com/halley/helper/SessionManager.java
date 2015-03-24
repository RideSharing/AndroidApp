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

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void setLogin(boolean isLoggedIn, String value) {

		editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
		editor.putString(API_KEY, value);

		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}

	public String getAPIKey() {
		return pref.getString(API_KEY, null);
	}

	public boolean isLoggedIn() {
		return pref.getBoolean(KEY_IS_LOGGEDIN, false);
	}
}

