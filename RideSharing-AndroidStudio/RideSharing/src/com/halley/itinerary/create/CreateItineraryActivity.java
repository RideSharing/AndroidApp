package com.halley.itinerary.create;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.halley.custom_theme.CustomActionBar;
import com.halley.itinerary.list.search.SearchDialogFragment;
import com.halley.itinerary.list.search.SearchDialogFragment.OnDataPass;
import com.halley.helper.GMapV2Direction;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CreateItineraryActivity extends ActionBarActivity implements
		OnMarkerDragListener, OnDataPass {
	private final int REQUEST_EXIT = 1;
	private GoogleMap googleMap;
	private Geocoder geocoder;
	private double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private TextView etStartAddress;
	private TextView etEndAddress;
	private Marker marker_start_address;
	private Marker marker_end_address;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
    private ActionBar actionBar;
	private Context context = this;
	private Button btnAdvance;
	private String distance;
	private String duration;
	// check onclick is From or to
	private boolean isFrom;
	// Direction maps
	private Polyline lineDirection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_itinerary);

        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();

		etStartAddress = (TextView) findViewById(R.id.txtStartAddress);
		etEndAddress = (TextView) findViewById(R.id.txtEndAddress);
		btnAdvance = (Button) findViewById(R.id.btnAdvance);
		btnAdvance.setVisibility(View.INVISIBLE);
		if (this.getIntent().getExtras() != null) {
			fromLatitude = this.getIntent().getExtras()
					.getDouble("fromLatitude");
			fromLongitude = this.getIntent().getExtras()
					.getDouble("fromLongitude");
		}
		initilizeMap();
		// Add current location on Maps
		marker_start_address = addMarkeronMaps(fromLatitude, fromLongitude,
				getResources().getString(R.string.start_addess),
				"marker_start_address", R.drawable.ic_marker_start);
		marker_end_address = addMarkeronMaps(fromLatitude + 0.002,
				fromLongitude + 0.002,
				getResources().getString(R.string.end_addess),
				"marker_end_address", R.drawable.ic_marker_end);
		focusMap(marker_start_address, marker_end_address);
		etStartAddress.setText(getDetailLocation(marker_start_address));
		etEndAddress.setText(getDetailLocation(marker_end_address));
	}

	public void showDialogonClick(View v) {
		switch (v.getId()) {
		case R.id.txtStartAddress:

			isFrom = true;
			break;
		case R.id.txtEndAddress:

			isFrom = false;
			break;
		}
		/** Instantiating TimeDailogFragment, which is a DialogFragment object */
		SearchDialogFragment dialog = new SearchDialogFragment();

		/** Getting FragmentManager object */
		FragmentManager fragmentManager = getFragmentManager();

		/** Starting a FragmentTransaction */
		dialog.show(fragmentManager, "search_location");

	}

	public void AdvanceonClick(View v) {
		pDialog.show();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				pDialog.dismiss();
				/**
				 * Instantiating TimeDailogFragment, which is a DialogFragment
				 * object
				 */
				CreateDetailDialog dialog = new CreateDetailDialog();
				Bundle bundle = new Bundle();
				if (getLocationfromName(etEndAddress.getText().toString()) != null
						&& getLocationfromName(etStartAddress.getText()
								.toString()) != null) {
					bundle.putString("duration", duration);
					bundle.putString("distance", distance);
					bundle.putDouble("start_address_lat",
							marker_start_address.getPosition().latitude);
					bundle.putDouble("start_address_long",
							marker_start_address.getPosition().longitude);
					bundle.putString("start_address",
							getDetailLocation(marker_start_address));
					bundle.putDouble("end_address_lat",
							marker_end_address.getPosition().latitude);
					bundle.putDouble("end_address_long",
							marker_end_address.getPosition().longitude);
					bundle.putString("end_address",
							getDetailLocation(marker_end_address));
				}
				dialog.setArguments(bundle);
				/** Getting FragmentManager object */
				FragmentManager fragmentManager = getFragmentManager();

				/** Starting a FragmentTransaction */
				dialog.show(fragmentManager, "advance_itinerary");

			}
		}, 1000);

	}

	public void submitOnclick(View v) {
		pDialog.show();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				pDialog.dismiss();
				// If inputFromAddress != null
				if (getLocationfromName(etStartAddress.getText().toString()) != null) {
					// remove marker from google map
					marker_start_address.remove();
					// add marker with new lat and long.
					marker_start_address = addMarkeronMaps(
							getLocationfromName(
									etStartAddress.getText().toString())
									.getLatitude(),
							getLocationfromName(
									etStartAddress.getText().toString())
									.getLongitude(),
							getResources()
									.getString(R.string.start_addess),
							"marker_start_address", R.drawable.ic_marker_start);
					// show on googlemap
					onMarkerDragEnd(marker_start_address);
				} else {
					Toast.makeText(context, R.string.not_found_area,
							Toast.LENGTH_SHORT).show();
				}
				if (getLocationfromName(etEndAddress.getText().toString()) != null) {
					// remove marker from google map
					marker_end_address.remove();
					// add marker with new lat and long.
					marker_end_address = addMarkeronMaps(
							getLocationfromName(
									etEndAddress.getText().toString())
									.getLatitude(),
							getLocationfromName(
									etEndAddress.getText().toString())
									.getLongitude(),
							getResources().getString(R.string.end_addess),
							"marker_end_address", R.drawable.ic_marker_end);
					// show on googlemap
					onMarkerDragEnd(marker_end_address);
				} else {
					Toast.makeText(context, R.string.not_found_area,
							Toast.LENGTH_SHORT).show();
				}
				focusMap(marker_start_address, marker_end_address);
				Toast.makeText(
						context,
						R.string.extra_info_itinerary,
						Toast.LENGTH_LONG).show();
				// Getting URL to the Google Directions API
				String url = getDirectionsUrl(
						marker_start_address.getPosition(),
						marker_end_address.getPosition());

				DownloadTask downloadTask = new DownloadTask();

				// Start downloading json data from Google Directions API
				downloadTask.execute(url);
				btnAdvance.setVisibility(View.VISIBLE);

			}
		}, 1000);

	}

	private void focusMap(Marker marker_a, Marker marker_b) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(marker_a.getPosition());
		builder.include(marker_b.getPosition());
		LatLngBounds bounds = builder.build();
		int padding = 0; // offset from edges of the map in pixels
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300, 300,
				padding);
		googleMap.moveCamera(cu);
		googleMap.animateCamera(cu);
	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) this.getFragmentManager()
					.findFragmentById(R.id.mapRegister)).getMap();
			// googleMap.setMyLocationEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(true);
			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);

			googleMap.setOnMarkerDragListener(this);

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(this, R.string.no_load_map,
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	private Marker addMarkeronMaps(double lat, double longi, String title,
			String snippet, int icon) {
		return googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(lat, longi)).title(title)
				.snippet(snippet)
				.icon(BitmapDescriptorFactory.fromResource(icon))
				.draggable(true));
	}

	@Override
	public void onMarkerDrag(Marker marker) {

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// Get location from Marker when user had choosen
		String address = getDetailLocation(marker);
		if (marker.getSnippet().equals("marker_start_address")) {
			etStartAddress.setText(address);
		} else {
			etEndAddress.setText(address);
		}
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	public Address getLocation(LatLng location) {
		Address add = null;
		List<android.location.Address> list_address = null;
		geocoder = new Geocoder(this, Locale.getDefault());

		try {
			list_address = geocoder.getFromLocation(location.latitude,
					location.longitude, 1);
			if (!list_address.isEmpty()) {
				add = list_address.get(0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return add;

	}

	public Address getLocationfromName(String location) {
		Address add = null;
		List<android.location.Address> list_address = null;
		geocoder = new Geocoder(this, Locale.getDefault());

		try {
			list_address = geocoder.getFromLocationName(location, 1);
			if (!list_address.isEmpty()) {
				add = list_address.get(0);
			}
			// Toast.makeText(this, "submit " + locality,
			// Toast.LENGTH_SHORT).show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return add;
	}

	public String getDetailLocation(Marker marker) {
		String address = "";
		Address position = null;
		position = getLocation(marker.getPosition());
		if (position != null) {
			for (int i = 0; i < 4; i++) {

				if (position.getAddressLine(i) != null) {
					address += position.getAddressLine(i) + " ";
				}

			}
		}
		return address;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				GMapV2Direction parser = new GMapV2Direction();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			MarkerOptions markerOptions = new MarkerOptions();

			if (result.size() < 1) {
				Toast.makeText(getBaseContext(), R.string.no_directions,
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();

				lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { // Get distance from the list

						distance = point.get("distance");
						continue;
					} else if (j == 1) { // Get duration from the list
						duration = point.get("duration");

						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(2);
				lineOptions.color(Color.BLUE);

			}
			if (lineDirection != null) {
				Log.d("Remove", "OK");
				lineDirection.remove();

			}

			// Drawing polyline in the Google Map for the i-th route
			lineDirection = googleMap.addPolyline(lineOptions);
		}
	}

	@Override
	public void onDataPass(String address, double latitude, double longtitude) {
		if (isFrom) {
			etStartAddress.setText(address);
		} else {
			etEndAddress.setText(address);
		}

	}

}
