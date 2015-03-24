package com.halley.ridesharing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.halley.helper.DatabaseHandler;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.adapter.TabListItineraryAdapter;
import com.halley.map.GPSLocation.GPSLocation;
import com.halley.model.slidingmenu.NavDrawerItem;
import com.halley.model.slidingmenu.adapter.NavDrawerListAdapter;
import com.halley.registerandlogin.LoginActivity;
import com.halley.registerandlogin.R;
import com.halley.registeritinerary.RegisterItineraryActivity;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements
		SearchView.OnQueryTextListener, ActionBar.TabListener {

	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;
	private SearchView mSearchView;
	private boolean driver = false;
	private Location currentLocation;
	private DatabaseHandler db;
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

	private ViewPager viewPager;
	private TabListItineraryAdapter mAdapter;
	private String[] tabs = { "Bản đồ", "Danh sách" };
	private int mDrawerState;
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gps = new GPSLocation(this);
		// get current Location of user
		currentLocation = gps.getCurrentLocation();

		// enabling action bar app icon and behaving it as toggle button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		View cView = getLayoutInflater().inflate(
				R.layout.switch_role_actionbar, null);
		session = new SessionManager(getApplicationContext());
		/** Set tab navigation mode */

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		/** Getting a reference to ViewPager from the layout */
		viewPager = (ViewPager) findViewById(R.id.pager);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		viewPager.getLayoutParams().height = metrics.heightPixels / 3
				+ metrics.heightPixels / 3;

		mAdapter = new TabListItineraryAdapter(getSupportFragmentManager(),
				this);
		if (currentLocation != null) {
			mAdapter.setFrom_address(currentLocation);
			mAdapter.setTo_address(currentLocation);
			mAdapter.setIsFrom(true);
		} else {
			Log.d("Do not get currentLocation", "null");
		}
		viewPager.setAdapter(mAdapter);
		// Adding Tabs
		for (String tab_name : tabs) {
			getSupportActionBar().addTab(
					getSupportActionBar().newTab().setText(tab_name)
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
				getSupportActionBar().setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
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

	public void searchLocationOnclick(View view) {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.dialog_search_location);
		dialog.setTitle("Tìm địa điểm cần đến");
		final EditText inputSearch;
		Button btnSearch;
		inputSearch = (EditText) dialog.findViewById(R.id.inputSearch);
		btnSearch = (Button) dialog.findViewById(R.id.btnSearch);
		// ArrayList for Listview
		final ArrayList<HashMap<String, String>> locationList;

		final Geocoder geocoder = new Geocoder(dialog.getContext(),
				Locale.getDefault());
		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					List<android.location.Address> list_address = null;
					String search = inputSearch.getText().toString();
					if (!search.equals("")) {
						list_address = geocoder.getFromLocationName(search, 1);
						if (list_address.isEmpty()) {
							Toast.makeText(dialog.getContext(),
									"Không tìm được địa điểm",
									Toast.LENGTH_LONG).show();
						} else {
							Address address = list_address.get(0);
							double latitude = address.getLatitude();
							double longitude = address.getLongitude();
							// Toast.makeText(dialog.getContext(),address.toString()
							// , Toast.LENGTH_LONG).show();
							Intent i = new Intent(MainActivity.this,
									ItineraryActivity.class);
							i.putExtra("toLatitude", latitude);
							i.putExtra("toLongitude", longitude);
							i.putExtra("fromLatitude",
									currentLocation.getLatitude());
							i.putExtra("fromLongitude",
									currentLocation.getLongitude());
							startActivity(i);
							dialog.cancel();
						}
					} else {
						Toast.makeText(dialog.getContext(),
								"Bạn chưa nhập nơi cần đến", Toast.LENGTH_LONG)
								.show();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		dialog.show();

	}

	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	public void logoutUser() {
		session.setLogin(false,null);

		db.deleteUsers();

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

		// adding nav drawer items to array
		if (driver == true) {
			for (int i = 0; i < navDriverMenuTitles.length; i++) {
				navDrawerItems.add(new NavDrawerItem(navDriverMenuTitles[i],
						navMenuIcons.getResourceId(i, -1)));
			}
		} else {
			for (int i = 0; i < navUserMenuTitles.length; i++) {
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
		switch (position) {
		case 0:
			intent = new Intent(this, RegisterItineraryActivity.class);
			if (currentLocation != null) {
				intent.putExtra("fromLatitude", currentLocation.getLatitude());
				intent.putExtra("fromLongitude", currentLocation.getLongitude());
			}
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(this, UserProfileActivity.class);
			// SqLite database handler
			db = new DatabaseHandler(getApplicationContext());
			// Fetching user details from sqlite
			HashMap<String, String> user = db.getUserDetails();
			String name = user.get("name");
			String email = user.get("email");

			break;
		case 5:
			logoutUser();
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
				for (int i = 0; i < menu.size(); i++) {
					// If the drawer is moving / settling or open do not draw
					// the icons
					menu.getItem(i)
							.setVisible(
									mDrawerState != DrawerLayout.STATE_DRAGGING
											&& mDrawerState != DrawerLayout.STATE_SETTLING
											&& !drawerOpen);
				}
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
	public boolean onQueryTextSubmit(String search_location) {
		// Bundle bundle = new Bundle();
		// bundle.putDouble("currentLatitude", currentLocation.getLatitude());
		// bundle.putDouble("currentLongitude", currentLocation.getLongitude());
		// if(search_location!=null){
		// bundle.putString("search_location", search_location);
		// }
		//

		// // fragment.onResume();
		// if (search_location != null) {
		// // mAdapter.setSearch_location(search_location);
		// Fragment fragment = ((TabListItineraryAdapter)
		// viewPager.getAdapter())
		// .getItem(0);
		// mAdapter.notifyDataSetChanged();
		// // FragmentTransaction fragmentTransaction
		// =getSupportFragmentManager().beginTransaction();
		// // fragmentTransaction.remove(fragment).commit();
		// viewPager.setTag(search_location);
		// } else {
		// Toast.makeText(this, "Bạn chưa nhập nơi cần đến",
		// Toast.LENGTH_SHORT).show();
		// }

		return false;
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

}
