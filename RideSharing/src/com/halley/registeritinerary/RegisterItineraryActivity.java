package com.halley.registeritinerary;

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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.halley.dialog.SearchDialogFragment;
import com.halley.dialog.SearchDialogFragment.OnDataPass;
import com.halley.map.GPSLocation.GMapV2Direction;
import com.halley.registerandlogin.R;

public class RegisterItineraryActivity extends ActionBarActivity implements
		OnMarkerDragListener, OnDataPass {
	private final int REQUEST_EXIT=1;
	private GoogleMap googleMap;
	private Geocoder geocoder;
	private double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private TextView etStartAddress;
	private TextView etEndAddress;
	private Marker marker_start_address;
	private Marker marker_end_address;
	private ActionBar actionBar;
	private ProgressDialog pDialog;
	private Context context = this;
	Button btnAdvance;
	private String distance;
	private String duration;
	// check onclick is From or to
	private boolean isFrom;
	// Direction maps
	Polyline lineDirection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_itinerary);
		
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		pDialog = new ProgressDialog(this);
		// Showing progress dialog before making http request
		pDialog.setMessage("Đang xử lí dữ liệu...");

		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);
		etStartAddress = (TextView) findViewById(R.id.txtStartAddress);
		etEndAddress = (TextView) findViewById(R.id.txtEndAddress);
		btnAdvance = (Button) findViewById(R.id.btnAdvance);
		btnAdvance.setVisibility(View.INVISIBLE);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		findViewById(R.id.frame_container_3).getLayoutParams().height = (metrics.heightPixels / 2);
		findViewById(R.id.mainLayout).getLayoutParams().height = metrics.heightPixels / 2;
		if (this.getIntent().getExtras() != null) {
			fromLatitude = this.getIntent().getExtras()
					.getDouble("fromLatitude");
			fromLongitude = this.getIntent().getExtras()
					.getDouble("fromLongitude");
		}
		initilizeMap();
		focusMap(fromLatitude, fromLongitude, 9);
		// Add current location on Maps
		marker_start_address = addMarkeronMaps(fromLatitude, fromLongitude,
				getResources().getString(R.string.hint_start_addess),
				"marker_start_address", R.drawable.ic_marker_start);
		marker_end_address = addMarkeronMaps(fromLatitude + 0.002,
				fromLongitude + 0.002,
				getResources().getString(R.string.hint_end_addess),
				"marker_end_address", R.drawable.ic_marker_end);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_itinerary, menu);
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
				Intent i = new Intent(context, RegisterAdvanceActivity.class);
				// Check location assist
				if (getLocationfromName(etEndAddress.getText().toString()) != null
						&& getLocationfromName(etStartAddress.getText()
								.toString()) != null) {
					i.putExtra("start_address_lat",
							marker_start_address.getPosition().latitude);
					i.putExtra("start_address_long",
							marker_start_address.getPosition().longitude);
					i.putExtra("start_address",
							getDetailLocation(marker_start_address));
					i.putExtra("end_address_lat",
							marker_end_address.getPosition().latitude);
					i.putExtra("end_address_long",
							marker_end_address.getPosition().longitude);
					i.putExtra("end_address",
							getDetailLocation(marker_end_address));
					i.putExtra("duration", duration);
					i.putExtra("distance", distance);
					startActivityForResult(i, REQUEST_EXIT);
				} else {
					Toast.makeText(
							context,
							"Thông tin điểm đi hoặc điểm đến chưa được xác định, Vui lòng kiểm tra lại",
							Toast.LENGTH_LONG).show();
				}

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
									.getString(R.string.hint_start_addess),
							"marker_start_address", R.drawable.ic_marker_start);
					// show on googlemap
					onMarkerDragEnd(marker_start_address);
				} else {
					Toast.makeText(context, "Không thể xác định được nơi đi",
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
							getResources().getString(R.string.hint_end_addess),
							"marker_end_address", R.drawable.ic_marker_end);
					// show on googlemap
					onMarkerDragEnd(marker_end_address);
				} else {
					Toast.makeText(context, "Không thể xác định được nơi đến",
							Toast.LENGTH_SHORT).show();
				}
				focusMap(marker_start_address.getPosition().latitude,
						marker_start_address.getPosition().longitude, 7);
				Toast.makeText(
						context,
						"Kiểm tra thông tin trên bản đồ, sau đó nhấn Nâng cao ",
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

	private void focusMap(double fromLatitude, double fromLongitude, int zoom) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(fromLatitude, fromLongitude)).zoom(zoom)
				.build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) this.getFragmentManager()
					.findFragmentById(R.id.mapRegister)).getMap();
			googleMap.setMyLocationEnabled(true);

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

	private Marker addMarkeronMaps(double lati, double longi, String title,
			String snippet, int icon) {
		return googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(lati, longi)).title(title)
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

	@Override
	public void onDataPass(String address, double latitude, double longtitude) {
		if (isFrom) {
			etStartAddress.setText(address);
		} else {
			etEndAddress.setText(address);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	    if (requestCode == REQUEST_EXIT) {
	         if (resultCode == RESULT_OK) {
	            this.finish();

	         }
	     }
	}
}
