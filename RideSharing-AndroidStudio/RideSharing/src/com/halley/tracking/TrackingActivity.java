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
import android.os.Handler;
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
import com.halley.custom_theme.CustomActionBar;
import com.halley.map.GPSLocation.GMapV2Direction;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

public class TrackingActivity extends ActionBarActivity implements
		LocationListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	// LogCat tag
	private static final String TAG = TrackingActivity.class.getSimpleName();
	private static final long INTERVAL = 1000;
	private static final long FASTEST_INTERVAL = 5000;
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;


	private ActionBar actionBar;
	private GoogleMap googleMap;
	// Direction maps
	private Location mCurrentLocation;
	private Marker marker_user;
	private Marker marker_driver;
	private Double fromLatitude, fromLongitude;
	private Context context = this;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
	Logger logger;
	HubConnection conn;
	HubProxy proxy;
	Handler mHandler = new Handler();

	final MyRunnable mUpdateResults = new MyRunnable() {
		public void run() {
			test(mUpdateResults.data);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isGooglePlayServicesAvailable()) {
			finish();
		}
		Platform.loadPlatformComponent(new AndroidPlatformComponent());

		// Create a new console logger
		logger = new Logger() {

			@Override
			public void log(String message, LogLevel level) {
				// TODO Auto-generated method stub

			}
		};

		// Connect to the server
		conn = new HubConnection("http://ridesharing.tk:8080", "", true, logger);

		// Create the hub proxy
		proxy = conn.createHubProxy("MyHub");

		// Subscribe to the error event
		conn.error(new ErrorCallback() {
			@Override
			public void onError(Throwable error) {
				// error.printStackTrace();
			}
		});

		// Subscribe to the connected event
		conn.connected(new Runnable() {
			@Override
			public void run() {
				System.out.println("CONNECTED");
			}
		});

		// Subscribe to the closed event
		conn.closed(new Runnable() {
			@Override
			public void run() {
				System.out.println("DISCONNECTED");
			}
		});

		// Start the connection
		conn.start().done(new Action<Void>() {
			@Override
			public void run(Void obj) throws Exception {
				System.out.println("Done Connecting!");
			}
		});

		createLocationRequest();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		setContentView(R.layout.activity_tracking);
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
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
				getResources().getString(R.string.start_addess),
				"marker_user", R.drawable.ic_marker_start);
		marker_driver = addMarkeronMaps(fromLatitude + 0.102, fromLongitude,
				getResources().getString(R.string.end_addess),
				"marker_driver", R.drawable.ic_marker_driver);
		focusMap(marker_user, marker_driver);
	}

	public void trackingOnclick(View v) {
        proxy.invoke("connect", "14").done(new Action<Void>() {

            @Override
            public void run(Void obj) throws Exception {
                System.out.println("SENT!");
            }
        });

		proxy.subscribe(new Object() {
			@SuppressWarnings("unused")
			public void getPos(String pos) {
				mUpdateResults.setData(pos);
				mHandler.post(mUpdateResults);
			}
		});
	}

	public void test(String latlng) {
        Toast.makeText(this,latlng,Toast.LENGTH_LONG).show();
		String[] str = latlng.toString().split(",");
		double lat = Double.parseDouble(str[0]);
		double longi = Double.parseDouble(str[1]);
		if (marker_driver != null) {
			marker_driver.remove();
		}
		marker_driver = addMarkeronMaps(lat, longi, "driver", "marker_diver",
				R.drawable.ic_marker_driver);
		focusMap(marker_user, marker_driver);
	}

	private void focusMap(Marker marker_a, Marker marker_b) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(marker_a.getPosition());
		builder.include(marker_b.getPosition());
		LatLngBounds bounds = builder.build();
		int padding = 0; // offset from edges of the map in pixels
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50, 50,
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

		proxy.invoke("sendPos", "11", latlng).done(new Action<Void>() {

			@Override
			public void run(Void obj) throws Exception {
				System.out.println("SENT!");
			}
		});

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
