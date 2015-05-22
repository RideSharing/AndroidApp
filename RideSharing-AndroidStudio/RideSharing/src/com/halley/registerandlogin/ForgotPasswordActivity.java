package com.halley.registerandlogin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPasswordActivity extends Activity {
	private static final String TAG = ForgotPasswordActivity.class.getSimpleName();


	private Button btnBacktoLogin;
	EditText edEmail;
	private SweetAlertDialog pDialog;
	private Activity activity= this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		btnBacktoLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
		edEmail = (EditText) findViewById(R.id.forgotEmail);

		// Progress dialog
		pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
		pDialog.setCancelable(false);

		btnBacktoLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(i);
				finish();

			}
		});
	}
	
	public void btnonClick(View view) {
		if(!isValidEmail(edEmail.getText().toString())){
			Toast.makeText(this,getResources().getString(R.string.format_email),Toast.LENGTH_LONG).show();
		}
		else {
			forgotPassword(edEmail.getText().toString());
		}
	}
	public final static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public void forgotPassword(final String email) {
		String URL_FORGOT_NEW = AppConfig.URL_FORGOT_PASSWORD +"/"+email +"?lang="+Locale.getDefault().getLanguage();
		String tag_string_req = "req_forgot_pass";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		showDialog();

		StringRequest strReq = new StringRequest(Method.GET,
				URL_FORGOT_NEW, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Forgot Password Response: " + response.toString());
						hideDialog();
						try {
							JSONObject jObj = new JSONObject(response
									.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							if (!error) {
								String message = jObj.getString("message");
								new SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
										.setTitleText(getResources().getString(R.string.announce))
										.setContentText(message)
										.setConfirmText(getResources().getString(R.string.ok))
										.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(SweetAlertDialog sDialog) {
												sDialog.cancel();
											}
										})
										.show();
							} else {

								// Error occurred in registration. Get the error
								// message
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Forgot Password Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.forgot_password, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
