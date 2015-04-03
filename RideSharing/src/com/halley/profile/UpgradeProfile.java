package com.halley.profile;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.helper.Touch;
import com.halley.helper.TouchImageView;
import com.halley.registerandlogin.R;

public class UpgradeProfile extends ActionBarActivity {
	private static final String TAG = ProfileActivity.class.getSimpleName();
	private ProgressDialog pDialog;
	private SessionManager session;
	private String key;
	TextView driverLicense, titleUpgrade;
	String saveLicense, img_str, a;
	private ImageView imageLicense, confirmImage;
	private TextView tvDriver1, tvDriver2, tvDriverLicense;
	private AlertDialog dialog;
	Bitmap decodeByte;
	private static final int SELECTED_PICTURE = 1;
	private ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upgrade_profile);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		driverLicense = (TextView) findViewById(R.id.tvDriverLicense);
		imageLicense = (ImageView) findViewById(R.id.license_img);
		imageLicense.setOnTouchListener(new Touch());
		titleUpgrade = (TextView) findViewById(R.id.titleUpgrade);
		tvDriver1 = (TextView) findViewById(R.id.tvdriver1);
		tvDriver2 = (TextView) findViewById(R.id.tvDriver2);
		tvDriverLicense = (TextView) findViewById(R.id.tvDriverLicense);
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/DejaVuSerifCondensed-BoldItalic.ttf");
		titleUpgrade.setTypeface(face);
		tvDriver1.setTypeface(face);
		tvDriver2.setTypeface(face);
		Typeface face2 = Typeface.createFromAsset(getAssets(),
				"fonts/font_new.ttf");
		tvDriverLicense.setTypeface(face2);
		session = new SessionManager(getApplicationContext());
		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		showLicense();

	}

	public void btnClick(View v) {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, SELECTED_PICTURE);
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case SELECTED_PICTURE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String[] projection = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(uri, projection,
						null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(projection[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				Bitmap bitmap = BitmapFactory.decodeFile(filePath);
				final Drawable d = new BitmapDrawable(bitmap);

				// //Transfer from Base64 String to Image
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				byte[] image = stream.toByteArray();
				img_str = Base64.encodeToString(image, 0);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						imageLicense.setImageDrawable(null);
						imageLicense.setBackground(d);

					}
				}, 1000);

			}

		default:
			break;
		}

	}

	public void submit(View view) {

		if (!session.isDriver()) {
			session.setDriver(true);
			// if ((saveLicense.toString().length() == 0) ||
			// (img_str.toString().length() == 0)) {
			// Toast.makeText(getApplicationContext(), R.string.no_input,
			// Toast.LENGTH_SHORT).show();
			// } else {
			registerDriver();
			Toast.makeText(getApplicationContext(), R.string.confirm_driver,
					Toast.LENGTH_SHORT).show();
			// }

		}
		updateDriverImage();
		updateDriver();
		Toast.makeText(getApplicationContext(), R.string.update_success,
				Toast.LENGTH_SHORT).show();
	}

	public void updateDriverLicense(View v) {
		dialog = updateDriverLicense();
		dialog.show();
	}

	public AlertDialog updateDriverLicense() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Update Here");
		View view = View.inflate(this, R.layout.single_dialog, null);
		builder.setView(view);
		final EditText editDriverLicense = (EditText) view
				.findViewById(R.id.editDriverLicense);
		Button confirmChangeDriver = (Button) view
				.findViewById(R.id.btnChangeDriverLicense);
		Button cancelChangeDriver = (Button) view
				.findViewById(R.id.btnCancelDriverLicense);
		final AlertDialog dialog = builder.create();
		a = driverLicense.getText().toString();
		editDriverLicense.setText(driverLicense.getText().toString());
		confirmChangeDriver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				saveLicense = editDriverLicense.getText().toString().trim();
				if (saveLicense.length() == 0) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.no_input),
							Toast.LENGTH_LONG).show();
				} else if (!saveLicense.equals(a)) {
					driverLicense.setText(saveLicense);

					dialog.dismiss();
					Toast.makeText(getApplicationContext(),
							R.string.submit_change, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.cancel_change, Toast.LENGTH_LONG).show();
				}
			}
		});
		cancelChangeDriver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		return dialog;

	}

	public void updateDriverImage() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Changing ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_DRIVER_LICENSE_IMG,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							if (!error) {

								String message = jObj.getString("message");

							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								// Toast.makeText(getApplicationContext(),
								// errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("value", img_str);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void updateDriver() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Changing ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_DRIVER_LICENSE, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							if (!error) {
								String message = jObj.getString("message");

							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("value", saveLicense);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void showLicense() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Changing ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_DRIVER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response
									.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							if (!error) {
								// String message = jObj.getString("message");
								String driver_license = jObj
										.getString("driver_license");
								String driver_license_img = jObj
										.getString("driver_license_img");

								driverLicense.setText(driver_license);

								byte[] decodeString = Base64.decode(
										driver_license_img, Base64.DEFAULT);
								decodeByte = BitmapFactory.decodeByteArray(
										decodeString, 0, decodeString.length);

								TouchImageView iv = new TouchImageView(
										getApplicationContext());
								iv.setImageBitmap(decodeByte);

								UpgradeProfile.this.imageLicense
										.setImageBitmap(decodeByte);

							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void registerDriver() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Changing ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_DRIVER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Driver Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							if (!error) {

								String message = jObj.getString("message");

							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("driver_license", saveLicense);
				params.put("driver_license_img", img_str);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void zoomImage(View view) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		MyDialogFragment frag = new MyDialogFragment();
		frag.show(ft, "txn_tag");
	}

	public class MyDialogFragment extends DialogFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setStyle(DialogFragment.STYLE_NORMAL, R.style.MY_DIALOG);
		}

		@Override
		public void onStart() {
			super.onStart();
			Dialog d = getDialog();
			if (d != null) {
				int width = ViewGroup.LayoutParams.MATCH_PARENT;
				int height = ViewGroup.LayoutParams.MATCH_PARENT;
				d.getWindow().setLayout(width, height);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.zooming_layout, container,
					false);
			ImageView new_image = (ImageView) root
					.findViewById(R.id.imageView1);
			new_image.setImageBitmap(decodeByte);
			return root;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upgrade_profile, menu);
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}
}
