package com.halley.ridesharing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.halley.registerandlogin.R;

public class MapViewOfItineraryFragment extends Fragment implements
		OnMarkerDragListener {

	// Google Map
	private GoogleMap googleMap;
	Double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private boolean isFrom;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map_view_of_itinerary,
				container, false);
		// googleMap = ((MapFragment) getActivity().getFragmentManager()
		// .findFragmentById(R.id.map)).getMap();
		// Toast.makeText(this.getActivity(), container.getTag().toString(),
		// Toast.LENGTH_LONG).show();

		if (this.getArguments() != null) {
			fromLatitude = this.getArguments().getDouble("fromLatitude");
			fromLongitude = this.getArguments().getDouble("fromLongitude");
			toLatitude = this.getArguments().getDouble("toLatitude");
			toLongitude = this.getArguments().getDouble("toLongitude");
			isFrom = this.getArguments().getBoolean("isFrom");
			initilizeMap();
			
				// create marker
				MarkerOptions marker = new MarkerOptions().position(
						new LatLng(fromLatitude, fromLongitude)).title(
						"Địa chỉ hiện tại của bạn ");

				// adding marker
				googleMap.addMarker(marker);
			
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) this.getActivity().getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(true);
			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(toLatitude, toLongitude))
					.zoom(14).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			googleMap.setOnMarkerDragListener(this);
			// FragmentManager fmanager =
			// getActivity().getSupportFragmentManager();
			// Fragment fragment = fmanager.findFragmentById(R.id.map);
			// Log.d("Fragment ", fragment.toString());
			// SupportMapFragment supportmapfragment =
			// (SupportMapFragment)fragment;
			// GoogleMap supportMap = supportmapfragment.getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(this.getActivity(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

}
