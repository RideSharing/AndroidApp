package com.halley.profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.halley.helper.Touch;
import com.halley.helper.TouchImageView;
import com.halley.registerandlogin.R;

public class ProfileActivity extends ActionBarActivity {
	int REQUEST_CAMERA = 0, SELECT_FILE = 1;
	private boolean isAvatar;
	private final int REQUEST_EXIT = 1;
	private ActionBar actionBar;
	private ProgressDialog pDialog;
	private SessionManager session;
	private static final String TAG = ProfileActivity.class.getSimpleName();
	private String key;
	TextView txtfullname, txtemail, txtphone, txtpersonalID;
	private Button upgradeDriver;
	private AlertDialog dialog2, dialog;
	JSONArray profile = null;
	EditText newpass, confirmpass, editfullname, editphone, editpersonalid;
	private ImageView personalid_img, edit_personalid_img;
	private ImageView avatar;
	public Bitmap decodeByte2;
	private RoundedImageView editprofile, editavatar;

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
		customActionBar();

		txtfullname = (TextView) findViewById(R.id.fullname);
		txtemail = (TextView) findViewById(R.id.email);
		txtphone = (TextView) findViewById(R.id.phone);
		txtpersonalID = (TextView) findViewById(R.id.presionalID);

		edit_personalid_img = (ImageView) findViewById(R.id.edit_personalid_img);
		upgradeDriver = (Button) findViewById(R.id.btnUpgradeDriver);
		editavatar = (RoundedImageView) findViewById(R.id.editAvatar);
		editprofile = (RoundedImageView) findViewById(R.id.editEmail);
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/NorthernTerritories.ttf");
		txtfullname.setTypeface(face);
		session = new SessionManager(getApplicationContext());
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Vui lòng chờ...");
		pDialog.setCancelable(false);
		showProfile();
		editavatar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				selectImage();
				isAvatar = true;
			}
		});
		avatar = (ImageView) findViewById(R.id.avatar);
		edit_personalid_img.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				selectImage();
				isAvatar = false;
			}
		});
		personalid_img = (ImageView) findViewById(R.id.personalid_img);
		personalid_img.setOnTouchListener(new Touch());
		TouchImageView iv = new TouchImageView(getApplicationContext());
		upgradeDriver.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(getApplicationContext(),
						UpgradeProfile.class);

				startActivityForResult(i, REQUEST_EXIT);

			}
		});

	}

	private void selectImage() {
		final CharSequence[] items = { "Camera", "Thư viện", "Hủy bỏ" };

		AlertDialog.Builder builder = new AlertDialog.Builder(
				ProfileActivity.this);
		builder.setTitle("Thêm ảnh!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Camera")) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File f = new File(android.os.Environment
							.getExternalStorageDirectory(), "temp.jpg");
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, REQUEST_CAMERA);
				} else if (items[item].equals("Thư viện")) {
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent, "Chọn ảnh"),
							SELECT_FILE);
				} else if (items[item].equals("Hủy bỏ")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	public void zoomImage(View view) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		MyDialogFragment frag = new MyDialogFragment();
		frag.show(ft, "txn_tag");
	}

	public void updateProfile(View view) {
		dialog = updateFullname();
		dialog.show();
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

	public void setAvatar(final String avatar) {
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
								session.setAvatar(avatar);
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_SHORT).show();
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

				params.put("value", avatar);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void setPersonalidImage(final String persional_img) {
		String tag_string_req = "req_persional";

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
								String errorMsg = jObj.getString("message");
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
				params.put("value", persional_img);

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
		Button confirmChangeProfile = (Button) view
				.findViewById(R.id.btnConfirmChangeProfile);
		Button cancelChangeProfile = (Button) view
				.findViewById(R.id.btnCancelChangeProfile);
		final String a = txtfullname.getText().toString();
		final String b = txtphone.getText().toString();
		final String c = txtpersonalID.getText().toString();
		editfullname.setText(a);
		editphone.setText(b);
		editpersonalid.setText(c);
		final AlertDialog dialog = builder.create();
		confirmChangeProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				savefullname = editfullname.getText().toString().trim();
				savephone = editphone.getText().toString().trim();
				savepersonalid = editpersonalid.getText().toString().trim();
				if (savefullname.equals(a) && savephone.equals(b)
						&& savepersonalid.equals(c)) {

					Toast.makeText(getApplicationContext(),
							"Thông tin của bạn được giữ nguyên",
							Toast.LENGTH_LONG).show();
				} else if (savefullname.length() == 0
						|| savephone.length() == 0
						|| savepersonalid.length() == 0) {
					Toast.makeText(getApplicationContext(),
							"Vui lòng điền đầy đủ thông tin",
							Toast.LENGTH_SHORT).show();
				} else {
					update(savefullname, savephone, savepersonalid);

					dialog.dismiss();
				}
				txtfullname.setText(savefullname);
				txtphone.setText(savephone);
				txtpersonalID.setText(savepersonalid);

			}
		});
		cancelChangeProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});

		dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		return dialog;
	}

	private void update(final String savefullname, final String savephone,
			final String savepersonalid) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Đang cập nhật ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_REGISTER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {

								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_SHORT).show();
								session.setFullname(savefullname);
							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
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
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("fullname", savefullname);
				params.put("phone", savephone);
				params.put("personalID", savepersonalid);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public AlertDialog showEditDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Thay đổi mật khẩu");
		View view = View.inflate(this, R.layout.dialog_change_password, null);
		builder.setView(view);
		newpass = (EditText) view.findViewById(R.id.newpass);
		confirmpass = (EditText) view.findViewById(R.id.confirmpass);
		Button btnconfirm = (Button) view.findViewById(R.id.btnConfirmChange);
		Button btncancel = (Button) view.findViewById(R.id.btnCancelChange);
		final AlertDialog dialog = builder.create();
		btnconfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (newpass.getText().length() == 0
						|| confirmpass.getText().length() == 0) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.no_input),
							Toast.LENGTH_LONG).show();
				} else if ((newpass.getText().toString()).equals(confirmpass
						.getText().toString())) {
					setSavepass(newpass.getText().toString());

					changepassword();
					dialog.dismiss();
					Toast.makeText(getApplicationContext(),
							"Thay đổi mật khẩu thành công", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Mật khẩu xác nhận không trùng nhau",
							Toast.LENGTH_LONG).show();
				}

			}
		});
		btncancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
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

					@SuppressLint("NewApi")
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
									avatar.setBackground(null);
									avatar.setImageBitmap(decodeByte);

									// avatar.setAnimateImageBitmap(decodeByte,
									// true);
								}
								if (!"".equals(personalid_img)) {
									byte[] decodeString2 = Base64.decode(
											personalid_img, Base64.DEFAULT);
									decodeByte2 = BitmapFactory
											.decodeByteArray(decodeString2, 0,
													decodeString2.length);
									ProfileActivity.this.personalid_img
									.setBackground(null);
									ProfileActivity.this.personalid_img
											.setImageBitmap(decodeByte2);

									
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

	@SuppressLint("NewApi")
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

					if (isAvatar) {
						avatar.setBackground(null);
						avatar.setImageBitmap(bm);
						setAvatar(img_str_new);

					} else {
						personalid_img.setBackground(null);
						personalid_img.setImageBitmap(bm);
						setPersonalidImage(img_str_new);
					}

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

				String tempPath = getPath(selectedImageUri,
						ProfileActivity.this);
				Bitmap bm = getResizedBitmap(tempPath);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				// bm=getResizedBitmap(bm,70,120);
				byte[] image = stream.toByteArray();
				String img_str_new = Base64.encodeToString(image, 0);

				if (isAvatar) {
					avatar.setBackground(null);
					avatar.setImageBitmap(bm);
					setAvatar(img_str_new);

				} else {
					personalid_img.setBackground(null);
					personalid_img.setImageBitmap(bm);
					setPersonalidImage(img_str_new);
				}

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

	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaColumns.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
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
				Dialog dialog = new Dialog(getApplicationContext());
				dialog.setContentView(R.layout.dialog_info);
				dialog.setTitle("Thông tin về ứng dụng");
				dialog.show();
			}
		});

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);
	}

}