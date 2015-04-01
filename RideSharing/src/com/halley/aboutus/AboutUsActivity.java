package com.halley.aboutus;

import com.halley.registerandlogin.R;

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

}
