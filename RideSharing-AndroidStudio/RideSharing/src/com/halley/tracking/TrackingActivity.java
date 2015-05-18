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
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.custom_theme.CustomActionBar;
import com.halley.dialog.RatingandCommentDialogFragment;
import com.halley.helper.SessionManager;
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
    private static final String URL_TRACKING="http://52.11.206.209:8080";
    private static final String CUSTOMER_ROLE="customer";
    private static final String DRIVER_ROLE="driver";
    private static final int START_ITINERARY=1;
    private static final int START_WAITING=2;
    private static final int END_ITINERARY=3;
	private static  boolean IS_END_ITINERARY=false;
	private static final long INTERVAL = 1000;
	private static final long FASTEST_INTERVAL = 5000;
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;

    String role,customer_id,driver_id, itinerary_id;
	private ActionBar actionBar;
	private GoogleMap googleMap;
	// Direction maps
	private Location mCurrentLocation;
	private Marker marker_user;
	private Marker marker_driver;
    private SessionManager session;
	private Context context = this;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
    private Button btn;
	Logger logger;
	HubConnection conn;
	HubProxy proxy;
	Handler mHandler = new Handler();
	final Activity activity=this;
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
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        setContentView(R.layout.activity_tracking);
        session = new SessionManager(getApplicationContext());

        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            role=bundle.getString("role");
            customer_id=bundle.getString("customer_id");
            driver_id=bundle.getString("driver_id");
			itinerary_id=bundle.getString("itinerary_id");
            Toast.makeText(this,role+" "+customer_id+" "+driver_id,Toast.LENGTH_LONG).show();
        }
        btn = (Button) findViewById(R.id.btnTracking);
        if(role.equals(DRIVER_ROLE)){
            if(session.getWaitingCustomer()==START_ITINERARY){
                btn.setText(getResources().getString(R.string.start_itinerary));
            }
            else if(session.getWaitingCustomer()==START_WAITING){
                btn.setText(getResources().getString(R.string.start_waiting));
            }
            else if(session.getWaitingCustomer()==END_ITINERARY){
                btn.setText(getResources().getString(R.string.end_itinerary));
            }
        }
        else if(role.equals(CUSTOMER_ROLE)) {
			btn.setText(getResources().getString(R.string.end_itinerary));


        }
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(role.equals(DRIVER_ROLE)){
                    if (session.getWaitingCustomer()==START_ITINERARY) {
						btn.setText(getResources().getString(R.string.start_waiting));
                        session.setWaitingCustomer(START_WAITING);

                        // Add current location on Maps
                        proxy.invoke("setItineraryStatus", driver_id,customer_id, "Start itinerary").done(new Action<Void>() {
                            @Override
                            public void run(Void obj) throws Exception {
                                System.out.println("SENT!");
                            }
                        });
                    } else if (session.getWaitingCustomer()==START_WAITING) {
						btn.setText(getResources().getString(R.string.end_itinerary));
                        session.setWaitingCustomer(END_ITINERARY);
                        // Add current location on Maps
                        proxy.invoke("setItineraryStatus", driver_id,customer_id, "Start waiting").done(new Action<Void>() {
                            @Override
                            public void run(Void obj) throws Exception {
                                System.out.println("SENT!");
                            }
                        });
                    } else if(session.getWaitingCustomer()==END_ITINERARY){
                        session.setWaitingCustomer(START_ITINERARY);
						/** Instantiating TimeDailogFragment, which is a DialogFragment object */
						RatingandCommentDialogFragment dialog = new RatingandCommentDialogFragment();
						Bundle b=new Bundle();
						b.putString("rating_user",customer_id);
						dialog.setArguments(b);
						/** Getting FragmentManager object */
						FragmentManager fragmentManager = getFragmentManager();
						/** Starting a FragmentTransaction */
						dialog.show(fragmentManager, "rating");

                        //Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_LONG).show();
                    }
                }
                else if(role.equals(CUSTOMER_ROLE)){
					if(IS_END_ITINERARY){
						endItinerary(itinerary_id);
					}
					else {
						new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
								.setTitleText(getResources().getString(R.string.announce))
								.setContentText(getResources().getString(R.string.not_end_itinerary))
								.setConfirmText(getResources().getString(R.string.ok))
								.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog sDialog) {
										sDialog.cancel();
									}
								})
								.show();
					}
                }
            }
        });
		Platform.loadPlatformComponent(new AndroidPlatformComponent());

		// Create a new console logger
		logger = new Logger() {

			@Override
			public void log(String message, LogLevel level) {
				// TODO Auto-generated method stub

			}
		};

		// Connect to the server
		conn = new HubConnection(URL_TRACKING, "", true, logger);

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
                // Add current location on Maps
                proxy.invoke("connect", (role.equals(DRIVER_ROLE))?driver_id:customer_id).done(new Action<Void>() {

                    @Override
                    public void run(Void obj) throws Exception {
                        System.out.println("SENT!");
                    }
                });

                proxy.subscribe(new Object() {
                    @SuppressWarnings("unused")
                    public void getPos(String driver_id, String pos) {
                        mUpdateResults.setData(pos);
                        mHandler.post(mUpdateResults);
                    }
                });

                if(CUSTOMER_ROLE.equals(role)){
                    proxy.subscribe(new Object() {
                        @SuppressWarnings("unused")
                        public void getItineraryStatus(String driver_id, String pos) {
                            mUpdateResults.setData(pos);
                            mHandler.post(mUpdateResults);
                        }
                    });
                }
			}
		});


		// findViewById(R.id.trackingLayout).getLayoutParams().height =
		// (metrics.heightPixels / 3);

		initilizeMap();


	}



	public void test(String latlng) {
        if ("Start itinerary".equals(latlng)) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getResources().getString(R.string.announce))
                    .setContentText(getResources().getString(R.string.start_itinerary_detail))
                    .setConfirmText(getResources().getString(R.string.ok))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .show();
        } else if ("Start waiting".equals(latlng)) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getResources().getString(R.string.announce))
                    .setContentText(getResources().getString(R.string.start_waiting_detail))
                    .setConfirmText(getResources().getString(R.string.ok))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
							IS_END_ITINERARY=true;
							sDialog.cancel();


                        }
                    })
                    .show();
        } else {
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
	}

	private void focusMap(Marker marker_a, Marker marker_b) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(marker_a.getPosition());
		builder.include(marker_b.getPosition());
		LatLngBounds bounds = builder.build();
		//bounds = boundsWithCenterAndLatLngDistance(centerPointPosition, 12000, 12000);
		int padding = 0; // offset from edges of the map in pixels
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,50);
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
	public void endItinerary(final String itinerary_id) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		StringRequest strReq = new StringRequest(Request.Method.PUT,
				AppConfig.URL_DRIVER_END_ITINERARY + "/" + itinerary_id,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"End itinerary Response: "
										+ response.toString());

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
								/** Instantiating TimeDailogFragment, which is a DialogFragment object */
								RatingandCommentDialogFragment dialog = new RatingandCommentDialogFragment();
								Bundle b=new Bundle();
								b.putString("rating_user",driver_id);
								dialog.setArguments(b);
								/** Getting FragmentManager object */
								FragmentManager fragmentManager = getFragmentManager();
								/** Starting a FragmentTransaction */
								dialog.show(fragmentManager, "rating");
							} else {

								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Registration Error: " + error.getMessage());
				// Toast.makeText(getApplicationContext(),
				// error.getMessage(), Toast.LENGTH_LONG).show();

			}
		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();

				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", session.getAPIKey());

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
        proxy.removeSubscription("getPos");
        proxy.removeSubscription("getItineraryStatus");
        conn.disconnect();
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
        if (marker_user != null) {
            marker_user.remove();
        }
        marker_user = addMarkeronMaps(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                getResources().getString(R.string.start_addess),
                "marker_user", R.drawable.ic_marker_start);

		// Toast.makeText(getApplicationContext(), location.toString(),
		// Toast.LENGTH_LONG).show();
		// mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		String latlng = String.format("%.6f", location.getLatitude()) + ","
				+ String.format("%.6f", location.getLongitude());

		proxy.invoke("sendPos", (role.equals(DRIVER_ROLE))?driver_id:customer_id, (!role.equals(DRIVER_ROLE))?driver_id:customer_id, latlng).done(new Action<Void>() {

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
