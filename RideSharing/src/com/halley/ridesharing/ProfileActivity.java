package com.halley.ridesharing;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import com.halley.helper.Touch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.RoundedImageView;
import com.halley.helper.SessionManager;
import com.halley.helper.TouchImageView;
import com.halley.registerandlogin.R;

public class ProfileActivity extends Activity {

	private ProgressDialog pDialog;
	private SessionManager session;
	private static final String TAG = ProfileActivity.class.getSimpleName();

	private String key;

	TextView txtfullname, txtemail, txtphone, txtpersonalID;
	private Button upgradeDriver;
	private AlertDialog dialog2, dialog;
	JSONArray profile = null;
	EditText newpass, confirmpass, editfullname, editphone, editpersonalid;
	private ImageView editavatar, personalid_img, edit_personalid_img;
	private RoundedImageView avatar;
	public Bitmap decodeByte2;
	private static final int SELECTED_PICTURE = 1;
	private static final int CAM_REQUEST = 1313;
	private TextView tv1, tv2, tv3, tv4;

	String img_str, img_str_camera;

	String savepass, savefullname, savephone, savepersonalid;

	public String getSavepass() {
		return savepass;
	}

	public void setSavepass(String savepass) {
		this.savepass = savepass;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		txtfullname = (TextView) findViewById(R.id.fullname);
		txtemail = (TextView) findViewById(R.id.email);
		txtphone = (TextView) findViewById(R.id.phone);
		txtpersonalID = (TextView) findViewById(R.id.presionalID);
		avatar = (RoundedImageView) findViewById(R.id.avatar);
		personalid_img = (ImageView) findViewById(R.id.personalid_img);
		edit_personalid_img = (ImageView) findViewById(R.id.edit_personalid_img);
		personalid_img.setOnTouchListener(new Touch());
		upgradeDriver = (Button) findViewById(R.id.btnUpgradeDriver);
		editavatar = (ImageView) findViewById(R.id.editAvatar);
		tv1 = (TextView) findViewById(R.id.tvProfile1);
		tv2 = (TextView) findViewById(R.id.tvProfile2);
		tv3 = (TextView) findViewById(R.id.tvProfile3);
		tv4 = (TextView) findViewById(R.id.tvProfile4);
		Typeface face1 = Typeface.createFromAsset(getAssets(),
				"fonts/DejaVuSerifCondensed-BoldItalic.ttf");
		tv1.setTypeface(face1);
		tv2.setTypeface(face1);
		tv3.setTypeface(face1);
		tv4.setTypeface(face1);
		Typeface face2 = Typeface.createFromAsset(getAssets(),
				"fonts/font_new.ttf");
		txtemail.setTypeface(face2);
		txtphone.setTypeface(face2);
		txtpersonalID.setTypeface(face2);
		Typeface face3 = Typeface.createFromAsset(getAssets(),
				"fonts/NorthernTerritories.ttf");
		txtfullname.setTypeface(face3);

		session = new SessionManager(getApplicationContext());
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Vui lòng chờ...");
		pDialog.setCancelable(false);
		showProfile();
		editavatar.setOnClickListener(new editAvatar());
		TouchImageView iv = new TouchImageView(getApplicationContext());
		upgradeDriver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pDialog.setMessage("Loading ...");
				showDialog();

				Intent i = new Intent(getApplicationContext(),
						UpgradeProfile.class);
				startActivity(i);
				hideDialog();

			}
		});

	}

	class editAvatar implements Button.OnClickListener {

		@Override
		public void onClick(View v) {
			Intent cameraintent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraintent, CAM_REQUEST);

		}

	}

	public void zoomImage(View view) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		MyDialogFragment frag = new MyDialogFragment();
		frag.show(ft, "txn_tag");
	}

	public void uploadavatar(View v) {
		setAvatar();
		Toast.makeText(getApplicationContext(),
				"Thay đổi ảnh đại diện thành công", Toast.LENGTH_LONG).show();
	}

	public void updateProfile(View view) {
		dialog = updateFullname();
		dialog.show();
	}

	public void uploadpersonal_img(View v) {
		setPersonalidImage();
		Toast.makeText(getApplicationContext(), "Thay đổi ảnh CMND thành công",
				Toast.LENGTH_LONG).show();

	}

	public void btnClick(View v) {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, SELECTED_PICTURE);

	}

	public void changepasswordonClick(View v) {
		dialog2 = showEditDialog();
		dialog2.show();

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
			new_image.setImageBitmap(decodeByte2);
			return root;
		}

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
						personalid_img.setImageDrawable(null);
						personalid_img.setBackground(d);

					}
				}, 1000);

			}
			break;
		case CAM_REQUEST:
			if (resultCode == RESULT_OK) {
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream2);
				byte[] image2 = stream2.toByteArray();
				img_str_camera = Base64.encodeToString(image2, 0);
				final Drawable d = new BitmapDrawable(thumbnail);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						avatar.setImageDrawable(null);
						avatar.setBackground(d);

					}
				}, 1000);

			}
			break;
		default:
			break;
		}

	}

	public void setAvatar() {
		String tag_string_req = "req_avatar";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_AVATAR, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Avatar Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();

				params.put("value", img_str_camera);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void setPersonalidImage() {
		String tag_string_req = "req_avatar";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_PERSONALID_IMG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Avatar Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				// Toast.makeText(getApplicationContext(), img_str_camera,
				// Toast.LENGTH_LONG).show();
				params.put("value", img_str);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public AlertDialog updateFullname() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Thay đổi thông tin");
		View view = View.inflate(this, R.layout.dialog_edit_profile, null);
		builder.setView(view);
		editfullname = (EditText) view.findViewById(R.id.edituserfullname);
		editphone = (EditText) view.findViewById(R.id.edituserphone);
		editpersonalid = (EditText) view.findViewById(R.id.edituserpersonalid);
		final String a = txtfullname.getText().toString();
		final String b = txtphone.getText().toString();
		final String c = txtpersonalID.getText().toString();
		editfullname.setText(a);
		editphone.setText(b);
		editpersonalid.setText(c);

		builder.setPositiveButton("Xác nhận", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				savefullname = editfullname.getText().toString();
				savephone = editphone.getText().toString();
				savepersonalid = editpersonalid.getText().toString();
				if (!(savefullname.equals(a)) && (savephone.equals(b))
						&& (savepersonalid.equals(c))) {
					editfullname();
				} else if ((savefullname.equals(a)) && !(savephone.equals(b))
						&& (savepersonalid.equals(c))) {
					editPhone();
				} else if ((savefullname.equals(a)) && (savephone.equals(b))
						&& !(savepersonalid.equals(c))) {
					editPersonalID();
				} else if (!(savefullname.equals(a)) && !(savephone.equals(b))
						&& (savepersonalid.equals(c))) {
					editfullname();
					editPhone();
				} else if (!(savefullname.equals(a)) && (savephone.equals(b))
						&& !(savepersonalid.equals(c))) {
					editfullname();
					editPersonalID();
				} else if ((savefullname.equals(a)) && !(savephone.equals(b))
						&& !(savepersonalid.equals(c))) {
					editPhone();
					editPersonalID();
				} else if (!(savefullname.equals(a)) && !(savephone.equals(b))
						&& !(savepersonalid.equals(c))) {
					editfullname();
					editPhone();
					editPersonalID();
				}
				txtfullname.setText(savefullname);
				txtphone.setText(savephone);
				txtpersonalID.setText(savepersonalid);
			}
		});
		builder.setNeutralButton("Hủy bỏ", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		return dialog;
	}

	public void editfullname() {
		String tag_string_req = "req_fullname";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_FULLNAME, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Edit Fullname Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("value", savefullname);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void editPhone() {
		String tag_string_req = "req_phone";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_PHONE, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Change Pass Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();

				params.put("value", savephone);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void editPersonalID() {
		String tag_string_req = "req_phone";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_PERSONALID, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Change Pass Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();

				params.put("value", savepersonalid);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public AlertDialog showEditDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Thay đổi mật khẩu");
		View view = View.inflate(this, R.layout.dialog_layout, null);
		builder.setView(view);
		newpass = (EditText) view.findViewById(R.id.newpass);
		confirmpass = (EditText) view.findViewById(R.id.confirmpass);
		builder.setPositiveButton("Xác nhận", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if ((newpass.getText().toString()).equals(confirmpass.getText()
						.toString())) {
					setSavepass(newpass.getText().toString());

					changepassword();
				} else {
					Toast.makeText(getApplicationContext(),
							"Two of your pass are not the same",
							Toast.LENGTH_LONG).show();
				}
			}

		});
		builder.setNeutralButton("Hủy bỏ", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		return dialog;
	}

	public void changepassword() {
		String tag_string_req = "req_password";

		pDialog.setMessage("Đang thay đổi ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_PASSWORD, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");

							// Check for error node in json
							if (!error) {

								String message = jObj.getString("message");

							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Change Pass Error: " + error.getMessage());
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

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();

				params.put("value", savepass);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showProfile() {
		String tag_string_req = "req_profile";

		pDialog.setMessage("Đang tải...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_REGISTER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Profile Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								String email = jObj.getString("email");
								String apiKey = jObj.getString("apiKey");
								String fullname = jObj.getString("fullname");
								String phone = jObj.getString("phone");
								String personalid = jObj
										.getString("personalID");
								String personalid_img = jObj
										.getString("personalID_img");
								String link_avatar = jObj
										.getString("link_avatar");
								String created_at = jObj
										.getString("created_at");
								String status = jObj.getString("status");

								// user successfully logged in
								if (!"".equals(link_avatar)) {
									byte[] decodeString = Base64.decode(
											link_avatar, Base64.DEFAULT);
									Bitmap decodeByte = BitmapFactory
											.decodeByteArray(decodeString, 0,
													decodeString.length);

									avatar.setImageBitmap(decodeByte);
									avatar.getLayoutParams().height = 20;
									// avatar.setAnimateImageBitmap(decodeByte,
									// true);
								}
								if (!"".equals(link_avatar)) {
									byte[] decodeString2 = Base64.decode(
											personalid_img, Base64.DEFAULT);
									decodeByte2 = BitmapFactory
											.decodeByteArray(decodeString2, 0,
													decodeString2.length);
									ProfileActivity.this.personalid_img
											.setImageBitmap(decodeByte2);

									TouchImageView iv = new TouchImageView(
											getApplicationContext());
									iv.setImageBitmap(decodeByte2);

									iv.setScaleType(ScaleType.MATRIX);
								}
								txtfullname.setText(fullname);
								txtemail.setText(email);
								txtphone.setText(phone);
								txtpersonalID.setText(personalid);
							} else {
								// Error in login. Get the error message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Profile Error: " + error.getMessage());
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
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
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