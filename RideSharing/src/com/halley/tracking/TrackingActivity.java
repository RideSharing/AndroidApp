package com.halley.tracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.halley.map.GPSLocation.GMapV2Direction;
import com.halley.registerandlogin.R;

public class TrackingActivity extends ActionBarActivity implements
		LocationListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	// LogCat tag
	private static final String TAG = TrackingActivity.class.getSimpleName();
	private static final long INTERVAL = 1000;
	private static final long FASTEST_INTERVAL = 5000;

	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private Location mCurrentLocation;
	private String mLastUpdateTime;

	private ActionBar actionBar;
	private GoogleMap googleMap;
	private Firebase myFirebaseRef;
	private LocationListener locationListener;
	// Direction maps
	private Polyline lineDirection = null;
	private String distance, duration;
	private Marker marker_user;
	private Marker marker_driver;
	private Double fromLatitude, fromLongitude;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}
		createLocationRequest();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		setContentView(R.layout.activity_tracking);
		customActionBar();
		Firebase.setAndroidContext(this);
		myFirebaseRef = new Firebase("https://ride-sharing.firebaseio.com/");
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		findViewById(R.id.frame_container_4).getLayoutParams().height = (metrics.heightPixels / 3)
				+ (metrics.heightPixels / 3);

		// findViewById(R.id.trackingLayout).getLayoutParams().height =
		// (metrics.heightPixels / 3);
		if (this.getIntent().getExtras() != null) {
			fromLatitude = this.getIntent().getExtras()
					.getDouble("fromLatitude");
			fromLongitude = this.getIntent().getExtras()
					.getDouble("fromLongitude");
		}
		initilizeMap();
		// Add current location on Maps
		marker_user = addMarkeronMaps(fromLatitude, fromLongitude,
				getResources().getString(R.string.hint_start_addess),
				"marker_user", R.drawable.ic_marker_start);
		marker_driver = addMarkeronMaps(fromLatitude + 0.102, fromLongitude,
				getResources().getString(R.string.hint_end_addess),
				"marker_driver", R.drawable.ic_marker_driver);
		focusMap(marker_user, marker_driver);
	}

	public void trackingOnclick(View v) {

		// Map<String, String> user = new HashMap<String, String>();
		// user.put("11", fromLatitude + "," + fromLongitude);
		// myFirebaseRef.setValue(user);
		myFirebaseRef = myFirebaseRef.child("11");

		myFirebaseRef.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot location) {
				// Toast.makeText(getApplicationContext(), location.toString(),
				// Toast.LENGTH_SHORT).show();
				if (location.getValue() != null) {
					String[] str = location.getValue().toString().split(",");
					double lat = Double.parseDouble(str[0]);
					double longi = Double.parseDouble(str[1]);
					if (marker_driver != null) {
						marker_driver.remove();
					}
					marker_driver = addMarkeronMaps(lat, longi, "driver",
							"marker_diver", R.drawable.ic_marker_driver);
					focusMap(marker_user, marker_driver);
				}
			}

			@Override
			public void onCancelled(FirebaseError arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void customActionBar() {
		actionBar = getSupportActionBar();
		actionBar.setElevation(0);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
				.getColor(R.color.bg_login)));
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);

		View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
		TextView mTitleTextView = (TextView) mCustomView
				.findViewById(R.id.title_text);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ANGEL.otf");
		mTitleTextView.setTypeface(tf);

		ImageButton imageButton = (ImageButton) mCustomView
				.findViewById(R.id.imageButton);
		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.dialog_info);
				dialog.setTitle("Thông tin về ứng dụng");
				dialog.show();
			}
		});

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);
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
					.findFragmentById(R.id.mapTracking)).getMap();
			// googleMap.setMyLocationEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(true);
			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);

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
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart fired ..............");
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop fired ..............");
		mGoogleApiClient.disconnect();
		Log.d(TAG,
				"isConnected ...............: "
						+ mGoogleApiClient.isConnected());
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected - isConnected ...............: "
				+ mGoogleApiClient.isConnected());
		if (mGoogleApiClient.isConnected()) {
			startLocationUpdates();
		} else {
			showSettingsGPSAlert();
			
		}
	}

	protected void startLocationUpdates() {
		PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
				.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
						this);
		Log.d(TAG, "Location update started ..............: ");
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "Connection failed: " + connectionResult.toString());
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,
				"Firing onLocationChanged..............................................");
		mCurrentLocation = location;
		// Toast.makeText(getApplicationContext(), location.toString(),
		// Toast.LENGTH_LONG).show();
		// mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		String latlng = String.format("%.6f", location.getLatitude()) + ","
				+ String.format("%.6f", location.getLongitude());

		// myFirebaseRef.setValue(latlng);

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
		Log.d(TAG, "Location update stopped .......................");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mGoogleApiClient.isConnected()) {
			startLocationUpdates();
			Log.d(TAG, "Location update resumed .....................");
		} 
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public void showSettingsGPSAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("Thiết lập GPS");

		// Setting Dialog Message
		alertDialog
				.setMessage("Ứng dụng yêu cầu phải luôn bật GPS khi sử dụng. Hiện tại GPS chưa được bật. Bạn có muốn vào chế độ thiết lập GPS?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Thiết lập",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

}
