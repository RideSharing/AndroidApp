package com.halley.ridesharing;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.halley.listitinerary.adapter.TabListItineraryAdapter;
import com.halley.registerandlogin.R;

public class ItineraryActivity extends FragmentActivity implements
ActionBar.TabListener {

	
	private ActionBar actionBar;
	private ViewPager viewPager;
    private TabListItineraryAdapter mAdapter;
    // Tab titles
    private String[] tabs = { "ListView", "MapView"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		
		// Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabListItineraryAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(true);
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
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

		// Enabling Back navigation on Action Bar icon
        getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent=getIntent();
		if(intent.getExtras()!=null){
			mAdapter.setLocation_address(getLocation(intent.getStringExtra("location")));
			
			//Toast.makeText(this, "submit " + intent.getExtras().getString("location"), Toast.LENGTH_SHORT).show();
			
		}
		

		// txtQuery = (TextView) findViewById(R.id.txtQuery);

		//handleIntent(getIntent());

	}
	
	public Address getLocation(String location){
		Geocoder geocoder;
		Address add=null;
		List<android.location.Address> list_address = null;
		geocoder = new Geocoder(this, Locale.getDefault());
		try {
			list_address = geocoder.getFromLocationName(location, 1);
			 add = list_address.get(0);
			
			 
			//Toast.makeText(this, "submit " + locality, Toast.LENGTH_SHORT).show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return add;
		
	}

	

//	@Override
//	protected void onNewIntent(Intent intent) {
//		setIntent(intent);
//		handleIntent(intent);
//	}
//
//	/**
//	 * Handling intent data
//	 */
//	private void handleIntent(Intent intent) {
//		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//			String location = intent.getStringExtra(SearchManager.QUERY);
//
//			/**
//			 * Use this query to display search results like 1. Getting the data
//			 * from SQLite and showing in listview 2. Making webrequest and
//			 * displaying the data For now we just display the query only
//			 */
//			
//
//		}
//
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.itinerary, menu);
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
