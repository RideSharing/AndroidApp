package com.halley.profile;

import java.io.ByteArrayOutputStream;

import com.halley.registerandlogin.R;
import com.halley.registerandlogin.R.id;
import com.halley.registerandlogin.R.layout;
import com.halley.registerandlogin.R.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class DriverImageActivity extends Activity {
	public String img_str_new_id;
	private static final int SELECTED_PICTURE = 1;
	private static final int CAM_REQUEST = 2;
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.background_change_img);
		dialog = picture();
		dialog.show();
	}
	

	public AlertDialog picture() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_image);
		View view = View.inflate(this, R.layout.picture_picker, null);
		builder.setView(view);
		Button camera = (Button) view.findViewById(R.id.camera);
		Button gallery = (Button) view.findViewById(R.id.gallery);
		final AlertDialog dialog = builder.create();
		camera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cameraintent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraintent, CAM_REQUEST);
				dialog.dismiss();
			}
		});
		gallery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, SELECTED_PICTURE);
				dialog.dismiss();
				
			}
		});
		return dialog;
		
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
				img_str_new_id = Base64.encodeToString(image, 0);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						Intent intent = new Intent(getApplicationContext(),
								UpgradeProfile.class);
						intent.putExtra("driver_img", img_str_new_id);
						startActivity(intent);
//						dialog.dismiss();
						finish();

					}
				}, 500);

			}
			break;
		case CAM_REQUEST:
			if (resultCode == RESULT_OK) {
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream2);
				byte[] image2 = stream2.toByteArray();
				img_str_new_id = Base64.encodeToString(image2, 0);
				final Drawable d = new BitmapDrawable(thumbnail);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(getApplicationContext(),
								UpgradeProfile.class);
						intent.putExtra("driver_img", img_str_new_id);
						startActivity(intent);
//						dialog.dismiss();
						finish();

					}
				}, 500);

			}
			break;
		default:
			break;
		}

	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.driver_image, menu);
//		return true;
//	}

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
}