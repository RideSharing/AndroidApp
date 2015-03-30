package com.halley.registerandlogin;

import com.halley.map.GPSLocation.GPSLocation;
import com.halley.ridesharing.MainActivity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.DatabaseHandler;
import com.halley.helper.SessionManager;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class LoginActivity extends Activity {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private Button btnLogin;
	private Button btnRegister;
	private Button btnLinkToForgotPassword;
	private Button btnForgotPassword;
	private EditText inputEmail;
	private EditText inputPassword;
	private ProgressDialog pDialog;
	private SessionManager session;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.password);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		// Session manager
		session = new SessionManager(getApplicationContext());

		// Check if user is already logged in or not
		if (session.isLoggedIn()) {
			// User is already logged in. Take him to main activity
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();

				// Check for empty data in the form
				if (email.trim().length() > 0 && password.trim().length() > 0) {
					// login user
					checkLogin(email, password);
				} else {
					// Prompt user to enter credentials
					Toast.makeText(getApplicationContext(), R.string.no_input,
							Toast.LENGTH_LONG).show();
				}
			}

		});
		btnForgotPassword.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						ForgotPasswordActivity.class);
				startActivity(i);
				finish();

			}
		});

		// Link to ForgotPassword Screen
		btnRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				startActivity(i);

			}
		});

	}

	/**
	 * function to verify login details in mysql db
	 * */
	private void checkLogin(final String email, final String password) {
		// Tag used to cancel the request
		String tag_string_req = "req_login";

		pDialog.setMessage("Logging in ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_LOGIN, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Login Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {

								// user successfully logged in
								String apiKey = jObj.getString("apiKey");
								// Create login session
								session.setLogin(true, apiKey);

								// Launch main activity
								Intent intent = new Intent(
										getApplicationContext(),
										MainActivity.class);

								startActivity(intent);

								finish();

							} else {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Login Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("email", email);
				params.put("password", password);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}
