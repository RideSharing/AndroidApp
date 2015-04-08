package com.halley.ridesharing;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.halley.aboutus.AboutUsActivity;
import com.halley.dialog.SearchDialogFragment;
import com.halley.dialog.SearchDialogFragment.OnDataPass;
import com.halley.helper.DatabaseHandler;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.adapter.TabListItineraryAdapter;
import com.halley.manageitinerary.ManageItineraryActivity;
import com.halley.model.slidingmenu.NavDrawerItem;
import com.halley.model.slidingmenu.adapter.NavDrawerListAdapter;
import com.halley.profile.ProfileActivity;
import com.halley.registerandlogin.LoginActivity;
import com.halley.registerandlogin.R;
import com.halley.registeritinerary.RegisterItineraryActivity;
import com.halley.searchitinerary.ItineraryActivity;
import com.halley.tracking.TrackingActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener, OnDataPass, LocationListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final long INTERVAL = 1000 * 10;
	private static final long FASTEST_INTERVAL = 1000 * 5;

	LocationRequest mLocationRequest;
	GoogleApiClient mGoogleApiClient;
	Location mCurrentLocation;
	String mLastUpdateTime;

	private final int REQUEST_REFRESH = 10;
	private boolean driver = false;

	private DatabaseHandler db;
	public SessionManager session;
	private Fragment fragment;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar actionBar;
	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navUserMenuTitles;
	private String[] navDriverMenuTitles;
	private TypedArray navMenuIcons, navMenuIconsdriver;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	private ViewPager viewPager;
	private TabListItineraryAdapter mAdapter;
	private String[] tabs = { "Bản đồ", "Danh sách" };
	private int mDrawerState;
	final Context context = this;
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new SessionManager(getApplicationContext());
		driver = session.isDriver();
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}
		createLocationRequest();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		setContentView(R.layout.activity_main);

		customActionBar();

		// Add Navigation Drawer
		this.addNavDrawer(this);
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage("Khởi tạo giao diện ...");
		showDialog();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				initTab();
				hideDialog();

			}
		}, 3000);

		if (!session.isLoggedIn()) {
			logoutUser();
		}

	}

	public void logoutOnclick(View view) {
		logoutUser();
	}

	public void initTab() {
		View cView = getLayoutInflater().inflate(
				R.layout.switch_role_actionbar, null);

		/** Set tab navigation mode */

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		/** Getting a reference to ViewPager from the layout */
		viewPager = (ViewPager) findViewById(R.id.pager);

		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		viewPager.getLayoutParams().height = metrics.heightPixels / 3
				+ metrics.heightPixels / 3;
		mAdapter = new TabListItineraryAdapter(getSupportFragmentManager(),
				this);
		if (mCurrentLocation != null) {
			mAdapter.setFrom_address(mCurrentLocation);
			mAdapter.setTo_address(mCurrentLocation);
			mAdapter.setIsFrom(true);
		} else {
			Log.d("Do not get currentLocation", "null");
		}
		viewPager.setAdapter(mAdapter);
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

	public void searchLocationOnclick(View view) {
		/** Instantiating TimeDailogFragment, which is a DialogFragment object */
		SearchDialogFragment dialog = new SearchDialogFragment();

		/** Getting FragmentManager object */
		FragmentManager fragmentManager = getFragmentManager();

		/** Starting a FragmentTransaction */
		dialog.show(fragmentManager, "search_location");

	}

	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	public void logoutUser() {
		session.setLogin(false, null, false);

		// db.deleteUsers();
		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void addNavDrawer(Context context) {
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// load slide menu user's items
		navUserMenuTitles = getResources().getStringArray(
				R.array.nav_drawer_items_user);

		// load slide menu driver's items
		navDriverMenuTitles = getResources().getStringArray(
				R.array.nav_drawer_items_driver);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);
		// nav drawer icons from resources
		navMenuIconsdriver = getResources().obtainTypedArray(
				R.array.nav_drawer_icons_driver);

		// adding nav drawer items to array
		if (driver == true) {
			for (int i = navDrawerItems.size(); i < navDriverMenuTitles.length; i++) {
				navDrawerItems.add(new NavDrawerItem(navDriverMenuTitles[i],
						navMenuIconsdriver.getResourceId(i, -1)));
			}

		}
		for (int i = navDrawerItems.size(), k = 0; k < navUserMenuTitles.length; i++, k++) {
			navDrawerItems.add(new NavDrawerItem(navUserMenuTitles[k],
					navMenuIcons.getResourceId(k, -1)));
		}

		// What's hot, We will add a counter here
		// navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons
		// .getResourceId(5, -1), true, "50+"));

		// Recycle the typed array
		navMenuIcons.recycle();
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(context, navDrawerItems);
		mDrawerList.setAdapter(adapter);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.app_menu, // nav drawer open - description for
									// accessibility
				R.string.app_menu // nav drawer close - description for
		// accessibility
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);

				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerStateChanged(int state) {
				mDrawerState = state;
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// display view for selected nav drawer item
			displayView(position);

		}
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Intent intent = null;
		if (driver) {
			switch (position) {
			case 0:

				intent = new Intent(this, RegisterItineraryActivity.class);
				if (mCurrentLocation != null) {
					intent.putExtra("fromLatitude",
							mCurrentLocation.getLatitude());
					intent.putExtra("fromLongitude",
							mCurrentLocation.getLongitude());

				}

				break;
			case 1:

				break;
			case 2:
				intent = new Intent(this, ProfileActivity.class);
				break;
			case 3:
				intent = new Intent(this, ManageItineraryActivity.class);
				break;
			case 4:
				intent = new Intent(this, TrackingActivity.class);
				if (mCurrentLocation != null) {
					intent.putExtra("fromLatitude",
							mCurrentLocation.getLatitude());
					intent.putExtra("fromLongitude",
							mCurrentLocation.getLongitude());

				}
				break;
			case 5:
				intent = new Intent(this, AboutUsActivity.class);
				break;
			case 6:
				logoutUser();
				break;
			default:
				break;
			}
		} else {
			switch (position) {
			case 0:
				break;
			case 1:
				intent = new Intent(this, ProfileActivity.class);
				break;
			case 2:
				intent = new Intent(this, ManageItineraryActivity.class);
				break;
			case 4:
				intent = new Intent(this, AboutUsActivity.class);
				break;
			case 5:
				logoutUser();
				break;
			default:
				break;
			}
		}

		if (intent != null) {
			startActivityForResult(intent, REQUEST_REFRESH);
			// // update selected item and title, then close the drawer
			// mDrawerList.setItemChecked(position, true);
			// mDrawerList.setSelection(position);

			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataPass(String address, double latitude, double longitude) {

		Intent i = new Intent(this, ItineraryActivity.class);
		i.putExtra("fromLatitude", mCurrentLocation.getLatitude());
		i.putExtra("fromLongitude", mCurrentLocation.getLongitude());
		i.putExtra("toLatitude", latitude);
		i.putExtra("toLongitude", longitude);
		i.putExtra("address", address);
		startActivity(i);

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

		final ImageButton imageButton = (ImageButton) mCustomView
				.findViewById(R.id.imageButton);
		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Dialog dialog =new Dialog(context);
				dialog.setContentView(R.layout.dialog_info);
				dialog.setTitle("Thông tin về ứng dụng");
				dialog.show();
			}
		});

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart fired ..............");
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop fired ..............");
		mGoogleApiClient.disconnect();
		Log.d(TAG,
				"isConnected ...............: "
						+ mGoogleApiClient.isConnected());
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected - isConnected ...............: "
				+ mGoogleApiClient.isConnected());
		startLocationUpdates();
	}

	protected void startLocationUpdates() {
		PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
				.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
						this);
		Log.d(TAG, "Location update started ..............: ");
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "Connection failed: " + connectionResult.toString());
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,
				"Firing onLocationChanged..............................................");
		mCurrentLocation = location;
		stopLocationUpdates();
		// mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
		Log.d(TAG, "Location update stopped .......................");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mGoogleApiClient.isConnected()) {
			startLocationUpdates();
			Log.d(TAG, "Location update resumed .....................");
		}
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
