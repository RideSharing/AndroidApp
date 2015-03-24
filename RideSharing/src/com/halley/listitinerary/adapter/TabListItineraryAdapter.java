package com.halley.listitinerary.adapter;

import android.app.Activity;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.halley.ridesharing.ListViewOfItineraryFragment;
import com.halley.ridesharing.MapViewOfItineraryFragment;

public class TabListItineraryAdapter extends FragmentPagerAdapter {
	Fragment fragment;
	Bundle bundle;
	Location from_address;
	Location to_address;
	Geocoder geocoder;
	// check from address or to address
	Boolean isFrom;
	ActionBarActivity context;

	

	public Location getFrom_address() {
		return from_address;
	}

	public void setFrom_address(Location from_address) {
		this.from_address = from_address;
	}

	public Location getTo_address() {
		return to_address;
	}

	public void setTo_address(Location to_address) {
		this.to_address = to_address;
	}

	public Boolean getIsFrom() {
		return isFrom;
	}

	public void setIsFrom(Boolean isFrom) {
		this.isFrom = isFrom;
	}



	public TabListItineraryAdapter(FragmentManager fm, ActionBarActivity context) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int index) {
		bundle = new Bundle();
		Location from_address=this.getFrom_address();
		Location to_address=this.getTo_address();
		if (from_address != null&& to_address!=null) {
			bundle.putDouble("fromLatitude", from_address.getLatitude());
			bundle.putDouble("fromLongitude", from_address.getLongitude());
			bundle.putDouble("toLatitude", to_address.getLatitude());
			bundle.putDouble("toLongitude", to_address.getLongitude());
			bundle.putBoolean("isFrom", this.getIsFrom());
			
		} else {
			Log.d("NOt found Location", "");
		}
		
		
		switch (index) {
		case 0:
			if (bundle != null) {

				fragment = new MapViewOfItineraryFragment();
				fragment.setArguments(bundle);
				return fragment;
			}

			// fragment.setArguments(bundle);

		case 1:
			// List fragment activity
			if (bundle != null) {
				fragment = new ListViewOfItineraryFragment();
				fragment.setArguments(bundle);
				return fragment;
			}

		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}

}
