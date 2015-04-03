package com.halley.manageitinerary;

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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.halley.dialog.SearchDialogFragment.OnDataPass;
import com.halley.map.GPSLocation.GMapV2Direction;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.R.drawable;
import com.halley.registerandlogin.R.id;
import com.halley.registerandlogin.R.layout;
import com.halley.registerandlogin.R.menu;
import com.halley.registerandlogin.R.string;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DetailManageItineraryActivity extends ActionBarActivity implements
		OnMarkerDragListener, OnDataPass {

	private final int REQUEST_EXIT = 1;
	private GoogleMap googleMap;
	private Geocoder geocoder;
	private double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private Marker marker_start_address;
	private Marker marker_end_address;
	private ActionBar actionBar;
	private ProgressDialog pDialog;
	private Context context = this;
	private boolean isFrom;
	private String description, startAddress, endAddress, txtduration,
			txtdistance, leave_date, cost, phone;
	private String duration, distance;
	// Direction maps
	Polyline lineDirection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_manage_itinerary);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		pDialog = new ProgressDialog(this);
		// Showing progress dialog before making http request
		pDialog.setMessage("Đang xử lí dữ liệu...");
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		findViewById(R.id.frame_container_4).getLayoutParams().height = (metrics.heightPixels / 3);
		findViewById(R.id.mainLayoutManage).getLayoutParams().height = (metrics.heightPixels / 3 + metrics.heightPixels / 3);

		Bundle bundle = this.getIntent().getExtras().getBundle("bundle");
		if (bundle != null) {
			fromLatitude = bundle.getDouble("start_address_lat");
			fromLongitude = bundle.getDouble("start_address_long");
			toLatitude = bundle.getDouble("end_address_lat");
			toLongitude = bundle.getDouble("end_address_long");
			description = bundle.getString("description");
			startAddress = bundle.getString("start_address");
			endAddress = bundle.getString("end_address");
			txtduration = bundle.getString("duration");
			txtdistance = bundle.getString("distance");
			cost = bundle.getString("cost");
			phone = bundle.getString("phone");
			leave_date = bundle.getString("leave_date");

		}
		TextView tvdescription = (TextView) findViewById(R.id.description);
		TextView tvstartAddress = (TextView) findViewById(R.id.startAddress);
		TextView tvendAddress = (TextView) findViewById(R.id.endAddress);
		TextView tvduration = (TextView) findViewById(R.id.duration);
		TextView tvdistance = (TextView) findViewById(R.id.distance);
		TextView tvleave_date = (TextView) findViewById(R.id.leave_date);
		TextView tvcost = (TextView) findViewById(R.id.cost);
		TextView tvphone = (TextView) findViewById(R.id.phone);
		tvdescription.setText(description);
		tvstartAddress.setText(startAddress);
		tvendAddress.setText(endAddress);
		tvduration.setText(txtduration);
		tvdistance.setText(txtdistance);
		tvleave_date.setText(leave_date);
		tvcost.setText(cost);
		tvphone.setText(phone);
		initilizeMap();

		// Add current location on Maps

		marker_start_address = addMarkeronMaps(fromLatitude, fromLongitude,
				getResources().getString(R.string.hint_start_addess),
				"marker_start_address", R.drawable.ic_marker_start);
		marker_end_address = addMarkeronMaps(toLatitude, toLongitude,
				getResources().getString(R.string.hint_end_addess),
				"marker_end_address", R.drawable.ic_marker_end);
		focusMap(marker_start_address, marker_end_address);

		// Getting URL to the Google Directions API
		String url = getDirectionsUrl(marker_start_address.getPosition(),
				marker_end_address.getPosition());
		DownloadTask downloadTask = new DownloadTask();

		// Start downloading json data from Google Directions API
		downloadTask.execute(url);

	}

	private Marker addMarkeronMaps(double lati, double longi, String title,
			String snippet, int icon) {
		return googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(lati, longi)).title(title)
				.snippet(snippet)
				.icon(BitmapDescriptorFactory.fromResource(icon))
				.draggable(true));
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

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) this.getFragmentManager()
					.findFragmentById(R.id.mapRegister)).getMap();
			//googleMap.setMyLocationEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(true);
			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);

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
				Toast.makeText(this, "Sorry! unable to create maps",
						Toast.LENGTH_SHORT).show();
			}

		}
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
				Toast.makeText(getBaseContext(), "No Points",
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
		// CameraPosition cameraPosition = new CameraPosition.Builder()
		// .target(new LatLng(fromLatitude, fromLongitude)).zoom(zoom)
		// .build();
		// googleMap.animateCamera(CameraUpdateFactory
		// .newCameraPosition(cameraPosition));
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
					Log.d("De xem", position.getAddressLine(i).toString());
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail_manage_itinerary, menu);
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
	public void onDataPass(String address, double latitude, double longtitude) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

}
