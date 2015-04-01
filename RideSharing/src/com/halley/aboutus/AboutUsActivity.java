package com.halley.registerandlogin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AboutUsActivity extends ActionBarActivity {
	ActionBar actionBar;
	Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_us);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	public void feedbackonClick(View v) {
		Dialog dialog = new Dialog(this);
		dialog.setTitle("Phản hồi");
		dialog.setContentView(R.layout.dialog_feedback);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about_us, menu);
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
}
