package com.halley.listitinerary.adapter;

import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.halley.ridesharing.ListViewOfItineraryFragment;
import com.halley.ridesharing.MapViewOfItineraryFragment;

public class TabListItineraryAdapter extends FragmentPagerAdapter {
	Fragment fragment;
	Bundle bundle;
	Address location_address;
	public Address getLocation_address() {
		return location_address;
	}



	public void setLocation_address(Address location_address) {
		this.location_address = location_address;
	}



	public TabListItineraryAdapter(FragmentManager fm) {
		super(fm);
	}

	

	@Override
	public Fragment getItem(int index) {
		bundle = new Bundle();
		Address address=null;
		switch (index) {
		case 0:
			fragment=new ListViewOfItineraryFragment();
			address= this.getLocation_address();
			bundle.putString("location", address.getLocality());
	        fragment.setArguments(bundle);
			return fragment;
		case 1:
			// Games fragment activity
			fragment= new MapViewOfItineraryFragment();
			address= this.getLocation_address();
			bundle.putDouble("latitude", address.getLatitude());
			bundle.putDouble("longitude", address.getLongitude());
	        fragment.setArguments(bundle);
			return fragment;
	
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}

	

}
