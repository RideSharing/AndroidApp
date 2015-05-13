package com.halley.aboutus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.FragmentManager;
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
import com.halley.custom_theme.CustomActionBar;
import com.halley.dialog.RatingandCommentDialogFragment;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.RegisterActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AboutUsActivity extends ActionBarActivity {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	ActionBar actionBar;
	Context context = this;

	private SessionManager session;
	private String email, fullname;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_us);

		// Progress dialog
		pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
		pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
		// Session manager
		session = new SessionManager(getApplicationContext());
		getUser(session.getAPIKey());
	}



	public void feedbackonClick(View v) {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle(R.string.feedback);
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

//        /** Instantiating TimeDailogFragment, which is a DialogFragment object */
//        RatingandCommentDialogFragment dialog = new RatingandCommentDialogFragment();
//
//        Bundle b=new Bundle();
//        b.putString("rating_user","11");
//        dialog.setArguments(b);
//        /** Getting FragmentManager object */
//        FragmentManager fragmentManager = getFragmentManager();
//
//        /** Starting a FragmentTransaction */
//        dialog.show(fragmentManager, "rating");

	}

	private void getUser(final String apiKey) {
		// Tag used to cancel the request
		String tag_string_req = "req_get_user";

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_GET_USER+"?lang="+ Locale.getDefault().getLanguage(), new Response.Listener<String>() {

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

		pDialog.setTitleText(getResources().getString(R.string.process_data));
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
