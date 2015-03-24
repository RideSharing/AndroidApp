package com.halley.ridesharing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.halley.registerandlogin.R;

public class MapViewOfItineraryFragment extends Fragment {

	// Google Map
	private GoogleMap googleMap;
	Double latitude;
	Double longitude;
	Double currentlatitude;
	Double currentlongitude;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(
				R.layout.fragment_map_view_of_itinerary, container, false);

		initilizeMap();
		if (this.getArguments() != null) {
			latitude = this.getArguments().getDouble("latitude");
			longitude = this.getArguments().getDouble("longitude");
			currentlatitude = this.getArguments().getDouble("currentLatitude");
			currentlongitude = this.getArguments().getDouble("currentLongitude");

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(14).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			// create marker
			MarkerOptions marker = new MarkerOptions().position(new LatLng(currentlatitude, currentlongitude)).title("Địa chỉ hiện tại của bạn ");
			 
			// adding marker
			googleMap.addMarker(marker);
		}
		return rootView;
	}

	private void initilizeMap() {
		
		if (googleMap == null) {
			googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true);
			
			
			
			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);

			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);
//			FragmentManager fmanager = getActivity().getSupportFragmentManager();
//            Fragment fragment = fmanager.findFragmentById(R.id.map);
//            Log.d("Fragment ", fragment.toString());
//            SupportMapFragment supportmapfragment = (SupportMapFragment)fragment;
//            GoogleMap supportMap = supportmapfragment.getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(this.getActivity(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        } 
    }

	@Override
	public void onResume() {
		super.onResume();
		initilizeMap();
	}
}
