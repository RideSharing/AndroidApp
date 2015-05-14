package com.halley.profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.helper.Touch;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UpgradeProfile extends ActionBarActivity {
	private static final String TAG = ProfileActivity.class.getSimpleName();
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
	private SessionManager session;
	int REQUEST_CAMERA = 0, SELECT_FILE = 1;
	private String key,img_str="";
	TextView driverLicense;

	private ImageView imageLicense;

	private AlertDialog dialog;
	Bitmap decodeByte;
	private ActionBar actionBar;
	Button btnUpgradeDriver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade_profile);
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
		driverLicense = (TextView) findViewById(R.id.tvDriverLicense);
		imageLicense = (ImageView) findViewById(R.id.license_img);
		imageLicense.setOnTouchListener(new Touch());
		btnUpgradeDriver = (Button) findViewById(R.id.btnUpgradeDriver);
		session = new SessionManager(getApplicationContext());
		if (session.isDriver()) {
			btnUpgradeDriver.setText(R.string.change_info);
		}



		showLicense();
		
	}

	public void btnClick(View v) {
		selectImage();
	}

	public void submit(View view) {
		
		if (!session.isDriver()) {

			registerDriver(driverLicense.getText().toString().trim(),
					img_str);

		} else {
			updateDriver(driverLicense.getText().toString().trim(), img_str);
		}
	}

	public void updateDriverLicense(View v) {
		dialog = updateDriverLicense();
		dialog.show();
	}

	public AlertDialog updateDriverLicense() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.change_info);
		View view = View.inflate(this, R.layout.dialog_edit_driver_license, null);
		builder.setView(view);
		final EditText editDriverLicense = (EditText) view
				.findViewById(R.id.editDriverLicense);

		Button confirmChangeDriver = (Button) view
				.findViewById(R.id.btnChangeDriverLicense);
		Button cancelChangeDriver = (Button) view
				.findViewById(R.id.btnCancelDriverLicense);
		final AlertDialog dialog = builder.create();
		editDriverLicense.setText(driverLicense.getText().toString());
		confirmChangeDriver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String driveLicense_dialog = editDriverLicense.getText()
						.toString().trim();
				if (driveLicense_dialog.length() == 0) {
					Toast.makeText(getApplicationContext(),
							R.string.no_input,
							Toast.LENGTH_LONG).show();
				} else {
					driverLicense.setText(driveLicense_dialog);
					dialog.dismiss();
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

	public void showLicense() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
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

								driverLicense.setText(jObj.getString(
										"driver_license").equals("null") ? ""
										: jObj.getString("driver_license"));
								String driver_license_img = jObj
										.getString("driver_license_img");
								
								if (!driver_license_img.equals("null")) {
									byte[] decodeString = Base64.decode(
											driver_license_img, Base64.DEFAULT);
									decodeByte = BitmapFactory.decodeByteArray(
											decodeString, 0,
											decodeString.length);
									UpgradeProfile.this.imageLicense
											.setImageBitmap(decodeByte);
									img_str=driver_license_img;
								}

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
				params.put("Authorization", key);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void registerDriver(final String driver_license,
			final String driver_license_img) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
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

								Toast.makeText(getApplicationContext(),
										R.string.confirm_driver,
										Toast.LENGTH_SHORT).show();
								session.setDriver(true);
								btnUpgradeDriver.setText(R.string.change_info);
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
						Log.e(TAG, "Registration Error: " + error.getMessage());
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				if (!driver_license.equals("")) {
					params.put("driver_license", driver_license);
				}
				if (!driver_license_img.equals("")) {
					params.put("driver_license_img", driver_license_img);
				}
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void updateDriver(final String driver_license,
			final String driver_license_img) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_DRIVER+"?lang="+Locale.getDefault().getLanguage(), new Response.Listener<String>() {

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
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_SHORT).show();
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
						Log.e(TAG, "Registration Error: " + error.getMessage());
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("driver_license", driver_license);
				params.put("driver_license_img", driver_license_img);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

    private void selectImage() {
        final CharSequence[] items = { "Camera",getResources().getString(R.string.gallery), getResources().getString(R.string.cancel) };

        AlertDialog.Builder builder = new AlertDialog.Builder(
                UpgradeProfile.this);
        builder.setTitle(getResources().getString(R.string.add_image));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(getResources().getString(R.string.gallery))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.select_image)),
                            SELECT_FILE);
                } else if (items[item].equals(getResources().getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CAMERA) {
				File f = new File(Environment.getExternalStorageDirectory()
						.toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {
					Bitmap bm = getResizedBitmap(f.getAbsolutePath());
					Matrix matrix = new Matrix();
					matrix.postRotate(90);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
							bm.getHeight(), matrix, true);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();

					bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);

					byte[] image = stream.toByteArray();
					String img_str_new = Base64.encodeToString(image, 0);
					imageLicense.setImageBitmap(bm);
					img_str=img_str_new;
					String path = android.os.Environment
							.getExternalStorageDirectory()
							+ File.separator
							+ "Phoenix" + File.separator + "default";
					f.delete();
					OutputStream fOut = null;
					File file = new File(path, String.valueOf(System
							.currentTimeMillis()) + ".jpg");
					try {
						fOut = new FileOutputStream(file);
						bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();
						fOut.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == SELECT_FILE) {
				Uri selectedImageUri = data.getData();

				String tempPath = getPath(selectedImageUri, UpgradeProfile.this);
				Bitmap bm = getResizedBitmap(tempPath);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				// bm=getResizedBitmap(bm,70,120);
				byte[] image = stream.toByteArray();
				String img_str_new = Base64.encodeToString(image, 0);
				img_str=img_str_new;
				imageLicense.setImageBitmap(bm);

			}
		}
	}

	public Bitmap getResizedBitmap(String tempPath) {
		Bitmap bm;
		// Decode image size
		BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
		btmapOptions.inJustDecodeBounds = true;

		bm = BitmapFactory.decodeFile(tempPath, btmapOptions);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 120;

		// Find the correct scale value. It should be the power of 2.
		int scale = 1;
		while (btmapOptions.outWidth / scale / 2 >= REQUIRED_SIZE
				&& btmapOptions.outHeight / scale / 2 >= REQUIRED_SIZE)
			scale *= 2;

		// Decode with inSampleSize
		BitmapFactory.Options btmapOptions2 = new BitmapFactory.Options();
		btmapOptions2.inSampleSize = scale;
		return BitmapFactory.decodeFile(tempPath, btmapOptions2);

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
            Drawable d = imageLicense.getDrawable();
			new_image.setImageDrawable(d);
			return root;
		}

	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}

	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaColumns.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}
}
