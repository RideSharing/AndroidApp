package com.halley.ridesharing;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.halley.helper.SessionManager;
import com.halley.map.GPSLocation.GPSLocation;
import com.halley.model.slidingmenu.NavDrawerItem;
import com.halley.model.slidingmenu.adapter.NavDrawerListAdapter;
import com.halley.registerandlogin.LoginActivity;
import com.halley.registerandlogin.R;

public class MainActivity extends ActionBarActivity implements
		SearchView.OnQueryTextListener {
	private GoogleMap googleMap;
	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;
	private SearchView mSearchView;
	private boolean driver = false;
	private double currentLatitude;
	private double currentLongitude;
	private final double DEFAULT_LATITUDE=16.054407;
	private final double DEFAULT_LONGITUDE=108.202167;
	public SessionManager session;
	private Fragment fragment;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navUserMenuTitles;
	private String[] navDriverMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	private GPSLocation gps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Loading map
		initilizeMap();
		// check if GPS enabled

		// enabling action bar app icon and behaving it as toggle button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		View cView = getLayoutInflater().inflate(
				R.layout.switch_role_actionbar, null);
		session = new SessionManager(getApplicationContext());

		// Add Navigation Drawer
		this.addNavDrawer(this);
		// if (savedInstanceState == null) {
		// displayView(0);
		// }

		if (!session.isLoggedIn()) {
			logoutUser();
		}

	}

	public void logoutOnclick(View view) {
		logoutUser();
	}

	public void getLocationOnclick(View view) {
		gps = new GPSLocation(MainActivity.this);
		if (gps.canGetLocation()) {
			currentLatitude = gps.getLatitude();
			currentLongitude = gps.getLongitude();
			getCurrentLocation();
		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsGPSAlert();

		}

	}

	public void getCurrentLocation() {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(gps.getLatitude(), gps.getLongitude()))
				.zoom(14).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		// create marker
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(gps.getLatitude(), gps.getLongitude())).title(
				"Địa chỉ hiện tại của bạn ");

		// adding marker
		googleMap.addMarker(marker);

	}

	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	public void logoutUser() {
		session.setLogin(false);

		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
	
	public void showLogoutAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		
		// Setting Dialog Title
		alertDialog.setTitle("Đăng xuất");

		// Setting Dialog Message
		alertDialog
				.setMessage("Bạn muốn đăng xuất khỏi tài khoản?");

		// On pressing Settings button
		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						logoutUser();
					}
				});
		alertDialog.setNegativeButton("Hủy bỏ",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.mapMain)).getMap();
			googleMap.setMyLocationEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);

			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
					.zoom(14).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			// FragmentManager fmanager =
			// getActivity().getSupportFragmentManager();
			// Fragment fragment = fmanager.findFragmentById(R.id.map);
			// Log.d("Fragment ", fragment.toString());
			// SupportMapFragment supportmapfragment =
			// (SupportMapFragment)fragment;
			// GoogleMap supportMap = supportmapfragment.getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(this, "Sorry! unable to create maps",
						Toast.LENGTH_SHORT).show();
			}
		}
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

		// adding nav drawer items to array
		if (driver == true) {
			for (int i = 0; i < 7; i++) {
				navDrawerItems.add(new NavDrawerItem(navDriverMenuTitles[i],
						navMenuIcons.getResourceId(i, -1)));
			}
		} else {
			for (int i = 0; i < 7; i++) {
				navDrawerItems.add(new NavDrawerItem(navUserMenuTitles[i],
						navMenuIcons.getResourceId(i, -1)));
			}

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
		switch (position) {
		case 2:
			intent = new Intent(this, UserProfileActivity.class);

			break;
		 case 6:
			 showLogoutAlert();
		 break;
		default:
			break;
		}

		if (intent != null) {
			startActivity(intent);
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
		// Get the SearchView and set the searchable configuration

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		// SearchView searchView = (SearchView)
		// menu.findItem(R.id.action_search)
		// .getActionView();

		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu
				.findItem(R.id.action_search));

		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(this);

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
		case R.id.action_settings:
			return true;
		case R.id.action_search:
			return true;
		case R.id.action_role:
			if (this.driver == false) {
				item.setTitle("driver");
				item.setIcon(getResources().getDrawable(R.drawable.ic_driver));
				this.driver = true;
			} else {
				item.setTitle("user");
				item.setIcon(getResources().getDrawable(R.drawable.ic_user));
				this.driver = false;
			}
			this.addNavDrawer(this.getApplicationContext());

			return true;
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

		if (!drawerOpen) {
			if (this.driver == false) {
				menu.findItem(R.id.action_role).setTitle("user");
				menu.findItem(R.id.action_role).setIcon(
						getResources().getDrawable(R.drawable.ic_user));
			} else {
				menu.findItem(R.id.action_role).setTitle("driver");
				menu.findItem(R.id.action_role).setIcon(
						getResources().getDrawable(R.drawable.ic_driver));
			}
			this.addNavDrawer(this.getApplicationContext());
		}
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		menu.findItem(R.id.action_role).setVisible(!drawerOpen);
		menu.findItem(R.id.action_search).setVisible(!drawerOpen);
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
	public boolean onQueryTextChange(String location) {
		// Toast.makeText(this,"change "+ query,Toast.LENGTH_SHORT).show();

		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String location) {

		Intent i = new Intent(this, ItineraryActivity.class);
		i.putExtra("location", location);
		i.putExtra("currentLatitude", currentLatitude);
		i.putExtra("currentLongitude", currentLongitude);
		startActivity(i);

		return false;
	}

}
