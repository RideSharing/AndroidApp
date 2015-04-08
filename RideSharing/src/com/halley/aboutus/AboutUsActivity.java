package com.halley.aboutus;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.RegisterActivity;

public class AboutUsActivity extends ActionBarActivity {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	ActionBar actionBar;
	Context context = this;
	private ProgressDialog pDialog;
	private SessionManager session;
	private String email, fullname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_us);
		customActionBar();
		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		// Session manager
		session = new SessionManager(getApplicationContext());
		getUser(session.getAPIKey());
	}

	public void customActionBar() {
		actionBar = getSupportActionBar();
		actionBar.setElevation(0);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
				.getColor(R.color.bg_login)));
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);

		View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
		TextView mTitleTextView = (TextView) mCustomView
				.findViewById(R.id.title_text);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ANGEL.otf");
		mTitleTextView.setTypeface(tf);

		ImageButton imageButton = (ImageButton) mCustomView
				.findViewById(R.id.imageButton);
		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.dialog_info);
				dialog.setTitle("Thông tin về ứng dụng");
				dialog.show();
			}
		});

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);
	}

	public void feedbackonClick(View v) {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Phản hồi");
		dialog.setContentView(R.layout.dialog_feedback);
		final EditText etName = (EditText) dialog.findViewById(R.id.name);
		final EditText etEmail = (EditText) dialog.findViewById(R.id.email);
		final EditText etFeedback = (EditText) dialog
				.findViewById(R.id.feedback);
		Button btnFeedback = (Button) dialog.findViewById(R.id.btnfeedback);
		etName.setText(fullname);
		etEmail.setText(email);
		btnFeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (etName.getText().toString().trim().length() == 0
						|| etEmail.getText().toString().trim().length() == 0
						|| etFeedback.getText().toString().trim().length() == 0) {
					Toast.makeText(dialog.getContext(), R.string.no_input,
							Toast.LENGTH_SHORT).show();
				} else {
					feedback(etEmail.getText().toString().trim(), etName
							.getText().toString().trim(), etFeedback.getText()
							.toString().trim());
					dialog.dismiss();
				}

			}
		});
		dialog.show();

	}

	private void getUser(final String apiKey) {
		// Tag used to cancel the request
		String tag_string_req = "req_get_user";

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_GET_USER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "User Response: " + response.toString());

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {
								email = jObj.getString("email");
								fullname = jObj.getString("fullname");
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
						Log.e(TAG, "User Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();

					}
				}) {

			@Override
			public Map<String, String> getHeaders() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("Authorization", apiKey);
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void feedback(final String email, final String fullname,
			final String message) {
		// Tag used to cancel the request
		String tag_string_req = "req_feedback";

		pDialog.setMessage("Đang xử lý dữ liệu ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_FEEDBACK, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Feedback Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response
									.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
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
						Log.e(TAG, "Feedback Error: " + error.getMessage());
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
				params.put("name", fullname);
				params.put("content", message);
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
