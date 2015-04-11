package com.halley.searchitinerary;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.halley.listitinerary.adapter.TabListItineraryAdapter;
import com.halley.registerandlogin.R;

public class ItineraryActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	private double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private Location fromLocation, toLocation;
	private String address = "";
	private ActionBar actionBar;
	private ViewPager viewPager;
	private TabListItineraryAdapter mAdapter;
	// Tab titles
	private String[] tabs = { "Bản đồ", "Danh sách" };
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		customActionBar();
		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);

		mAdapter = new TabListItineraryAdapter(getSupportFragmentManager(),
				null);
		Intent intent = this.getIntent();
		if (intent.getExtras() != null) {
			fromLocation = new Location("");
			fromLocation.setLatitude(intent.getExtras().getDouble(
					"fromLatitude"));
			fromLocation.setLongitude(intent.getExtras().getDouble(
					"fromLongitude"));

			toLocation = new Location("");
			toLocation.setLatitude(intent.getExtras().getDouble("toLatitude"));
			toLocation
					.setLongitude(intent.getExtras().getDouble("toLongitude"));
			// Toast.makeText(this, location.toString(),
			// Toast.LENGTH_LONG).show();
			address = intent.getExtras().getString("address");
			mAdapter.setFrom_address(fromLocation);
			mAdapter.setTo_address(toLocation);
			mAdapter.setIsFrom(false);
		}
		viewPager.setAdapter(mAdapter);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	public void customActionBar() {
		actionBar = getSupportActionBar();
		actionBar.setElevation(0);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
				.getColor(R.color.bg_login)));

		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.rgb(19,
				143, 215)));
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	@Override
	public void onTabReselected(Tab arg0,
			android.support.v4.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab,
			android.support.v4.app.FragmentTransaction arg1) {
		viewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab arg0,
			android.support.v4.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

}
