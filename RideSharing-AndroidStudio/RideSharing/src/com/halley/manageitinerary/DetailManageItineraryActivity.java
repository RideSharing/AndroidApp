package com.halley.manageitinerary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import com.halley.dialog.SearchDialogFragment.OnDataPass;
import com.halley.helper.NumberTextWatcher;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.adapter.ItineraryListAdapter;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.map.GPSLocation.GMapV2Direction;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailManageItineraryActivity extends ActionBarActivity implements
		OnDataPass,View.OnClickListener {
	private static final String TAG = DetailManageItineraryActivity.class
			.getSimpleName();
    private static final String DRIVER_ROLE="driver";
    private static final String ITINERARY_STATUS_CREATED ="1";
    private static final String ITINERARY_STATUS_CUSTOMER_ACCEPTED ="2";
    private static final String ITINERARY_STATUS_DRIVER_ACCEPTED ="3";
    private static final String ITINERARY_STATUS_FINISH ="4";
	private GoogleMap googleMap;
	private Marker marker_start_address;
	private Marker marker_end_address;
	private ActionBar actionBar;
    private Button btnEditItinerary, btnDeleteItinerary,btnOngoing;
	private SweetAlertDialog pDialog;
	private Context context = this;
	private TextView tvdescription, tvstartAddress, tvendAddress, tvduration,
			tvdistance, tvleave_date, tvcost, tvvehicle_type,tvphone;
	EditText edDescription, edDuration, edCost;
	TextView edLeaveDate;
	private String duration, distance, key, itinerary_id, role, status;
	private SessionManager session;
	private ItineraryItem itineraryItem;
	String getDescription, getLeaveDate, getDuration, getCost;
	// Direction maps
	Polyline lineDirection = null;
	private Activity activity = this;
    private CustomActionBar custom_actionbar;
    private LinearLayout controlLayout, onGoingLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_manage_itinerary);

		session = new SessionManager(getApplicationContext());
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        controlLayout= (LinearLayout) findViewById(R.id.controlLayout);
        onGoingLayout= (LinearLayout) findViewById(R.id.onGoingLayout);
        tvdescription = (TextView) findViewById(R.id.description);
        tvstartAddress = (TextView) findViewById(R.id.startAddress);
        tvendAddress = (TextView) findViewById(R.id.endAddress);
        tvduration = (TextView) findViewById(R.id.duration);
        tvdistance = (TextView) findViewById(R.id.distance);
        tvleave_date = (TextView) findViewById(R.id.leave_date);
        tvcost = (TextView) findViewById(R.id.cost);
        tvphone = (TextView) findViewById(R.id.phone);
        tvvehicle_type = (TextView) findViewById(R.id.vehicle_type);
        btnEditItinerary = (Button) findViewById(R.id.btnEditItinerary);
        btnDeleteItinerary = (Button) findViewById(R.id.btnDeleteItinerary);

        btnOngoing = (Button) findViewById(R.id.btnOngoing);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
		Bundle bundle = this.getIntent().getExtras().getBundle("bundle");
		if (bundle != null) {
			itinerary_id = bundle.getString("itinerary_id");
            role= bundle.getString("role");
            status= bundle.getString("status");
            // if driver created itinerary, set hide button ON GOING
            if(status.equals(ITINERARY_STATUS_CREATED)){
                controlLayout.setVisibility(View.VISIBLE);
                onGoingLayout.setVisibility(View.GONE);
            }
            else if(status.equals(ITINERARY_STATUS_DRIVER_ACCEPTED)){
                controlLayout.setVisibility(View.GONE);
                onGoingLayout.setVisibility(View.VISIBLE);
            }
            else if(status.equals(ITINERARY_STATUS_FINISH)||status.equals(ITINERARY_STATUS_CUSTOMER_ACCEPTED)){
                controlLayout.setVisibility(View.GONE);
                onGoingLayout.setVisibility(View.GONE);
            }

            if(role.equals(DRIVER_ROLE)){}
            else{}
            getItinerary(itinerary_id);

		}
        btnOngoing.setOnClickListener(this);
		btnEditItinerary.setOnClickListener(this);
		btnDeleteItinerary.setOnClickListener(this);
	}
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnDeleteItinerary){
            confirmDelelte();
        } else if(v.getId()==R.id.btnEditItinerary){
            updateItinerary();
        } else if(v.getId()==R.id.btnOngoing){
            checkStatusItinerary(itinerary_id);
        }
    }
    public void checkStatusItinerary(String status){
        if(!role.equals(DRIVER_ROLE)) btnOngoing.setEnabled(false);
        //Ready for going
        if(status.equals(ITINERARY_STATUS_DRIVER_ACCEPTED)){
            //change status from ready to on going
            btnOngoing.setText(R.string.on_going);
        }
//        else if(status.equals(ITINERARY_STATUS_ON_GOING)){
//            //change status from on going to finish
//            btnOngoing.setText(R.string.end_addess);
//        }

    }


	public void confirmDelelte() {

    SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);

            dialog.setTitleText(getResources().getString(R.string.delete_itinerary))
            .setContentText(getResources().getString(R.string.you_sure))
            .setCancelText(getResources().getString(R.string.cancel))
            .setConfirmText(getResources().getString(R.string.ok))
            .showCancelButton(true)
            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.cancel();
                }
            })
            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    deleteItinerary();

                    finish();
                }
            }).show();

	}

	public void updateItinerary() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.update_itinerary);
		View view = View.inflate(this, R.layout.dialog_detail_itinerary, null);
		builder.setView(view);
		final AlertDialog dialog = builder.create();
		edDescription = (EditText) view.findViewById(R.id.etDescription);
		edLeaveDate = (TextView) view.findViewById(R.id.etDateStart);
		edDuration = (EditText) view.findViewById(R.id.etDuration);
		edCost = (EditText) view.findViewById(R.id.etCost);
		String a = tvdescription.getText().toString();
		String b = tvleave_date.getText().toString();
		String c = tvduration.getText().toString();
		String d = tvcost.getText().toString();
		edDescription.setText(a);
		edLeaveDate.setText(b);
		edDuration.setText(c);
		edCost.setText(d);
		edCost.addTextChangedListener(new NumberTextWatcher(edCost));
		edLeaveDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePicker();

			}
		});
		Button confirmChangeItinerary = (Button) view
				.findViewById(R.id.btnChangeItinerary);
		Button cancelChangeItinerary = (Button) view
				.findViewById(R.id.btnCancelItinerary);
		confirmChangeItinerary.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getDescription = edDescription.getText().toString();
				getLeaveDate = edLeaveDate.getText().toString();
				getDuration = edDuration.getText().toString();
				getCost = edCost.getText().toString().replace(",", "");
				if (edDescription.length() == 0 || edLeaveDate.length() == 0
						|| edDuration.length() == 0 || edCost.length() == 0) {
					Toast.makeText(getApplicationContext(),
							R.string.no_input,
							Toast.LENGTH_SHORT).show();
				} else {
					editItinerary();
					dialog.dismiss();

				}


			}
		});
		cancelChangeItinerary.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	public void editItinerary() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_REGISTER_ITINERARY + "/" + itinerary_id+"?lang="+Locale.getDefault().getLanguage(),
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
                                tvdescription.setText(getDescription);
                                tvleave_date.setText(getLeaveDate);
                                tvduration.setText(getDuration);
                                tvcost.setText(getCost);
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
							} else {

								// Error occurred in registration. Get the error
								// message
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
						hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("description", getDescription);
				params.put("leave_date", getLeaveDate);
				params.put("duration", getDuration);
				params.put("cost", getCost);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}
    private void getItinerary(final String id) {
        // Tag used to cancel the request
        String tag_string_req = "req_get_itinerary_by_id";
        Toast.makeText(this,id,Toast.LENGTH_LONG).show();
        StringRequest strReq = new StringRequest(Method.GET,
                AppConfig.URL_GET_ITINERARY+"/"+id+"?lang="+Locale.getDefault().getLanguage(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Log.d("Login Response: ", response.toString());
                        try {

                            JSONObject jObj = new JSONObject(response
                                    .substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");
                            // Check for error node in json

                            if (!error) {
                                itineraryItem = new ItineraryItem();
                                    itineraryItem.setStart_address_lat(jObj
                                            .getString("start_address_lat"));
                                    itineraryItem.setStart_address_long(jObj
                                            .getString("start_address_long"));
                                    itineraryItem.setEnd_address_lat(jObj
                                            .getString("end_address_lat"));
                                    itineraryItem.setEnd_address_long(jObj
                                            .getString("end_address_long"));
                                    itineraryItem.setDescription(jObj
                                            .getString("description"));
                                    itineraryItem.setStart_address(jObj
                                            .getString("start_address"));
                                    itineraryItem.setEnd_address(jObj
                                            .getString("end_address"));
                                    itineraryItem.setRating(4.8);
                                    itineraryItem.setLeave_date(jObj
                                            .getString("leave_date"));
                                    itineraryItem.setCost(jObj
                                            .getString("cost"));
                                    itineraryItem.setDuration(jObj
                                            .getString("duration"));
                                    itineraryItem.setDistance(jObj
                                            .getString("distance"));
                                itineraryItem.setPhone(jObj
                                        .getString("phone"));
                                itineraryItem.setVehicle_id(jObj.getString("vehicle_id"));
                                itineraryItem.setVehicle_type(jObj.getString("vehicle_type"));
                                tvdescription.setText(itineraryItem.getDescription());
                                tvstartAddress.setText(itineraryItem.getStart_address());
                                tvendAddress.setText(itineraryItem.getEnd_address());
                                tvduration.setText(transferDuration(itineraryItem.getDuration()));
                                tvdistance.setText(itineraryItem.getDistance());
                                tvleave_date.setText(itineraryItem.getLeave_date());
                                tvcost.setText(transferCost(itineraryItem.getCost()));
                                tvvehicle_type.setText(itineraryItem.getVehicle_type());
                                initilizeMap();
                                // Add current location on Maps
                                marker_start_address = addMarkeronMaps(Double.parseDouble(itineraryItem.getStart_address_lat()), Double.parseDouble(itineraryItem.getStart_address_long()),
                                        getResources().getString(R.string.start_addess), " ",
                                        R.drawable.ic_marker_start);
                                marker_end_address = addMarkeronMaps(Double.parseDouble(itineraryItem.getEnd_address_lat()),Double.parseDouble(itineraryItem.getEnd_address_long()),
                                        getResources().getString(R.string.end_addess), " ",
                                        R.drawable.ic_marker_end);
                                focusMap(marker_start_address, marker_end_address);

                                // Getting URL to the Google Directions API
                                String url = getDirectionsUrl(marker_start_address.getPosition(),
                                        marker_end_address.getPosition());
                                DownloadTask downloadTask = new DownloadTask();

                                // Start downloading json data from Google Directions API
                                downloadTask.execute(url);

                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getApplicationContext(), message,
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(),
                        R.string.not_connect,
                        Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

	public void deleteItinerary() {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		showDialog();

		StringRequest strReq = new StringRequest(Method.DELETE,
				AppConfig.URL_REGISTER_ITINERARY + "/" + itinerary_id,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Driver License Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {

								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
                                finish();

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
						hideDialog();
					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				key = session.getAPIKey();
				// Toast.makeText(getApplicationContext(), "Go Go "+ key,
				// Toast.LENGTH_LONG).show();
				params.put("Authorization", key);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void DateTimePicker() {
		final Dialog dialog = new Dialog(context);
		dialog.setTitle(R.string.leave_date);
		dialog.setContentView(R.layout.dialog_datetime_picker);

		final DatePicker datepicker = (DatePicker) dialog
				.findViewById(R.id.datePicker1);
		final TimePicker timepicker = (TimePicker) dialog
				.findViewById(R.id.timePicker1);
		final Button btnOK = (Button) dialog.findViewById(R.id.OK);
		btnOK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				Date currentTime = c.getTime();
				int year = datepicker.getYear();
				int month = datepicker.getMonth();
				int day = datepicker.getDayOfMonth();

				int hour = timepicker.getCurrentHour();
				int minute = timepicker.getCurrentMinute();
				c.set(year, month, day, hour, minute);
				if (currentTime.after(c.getTime())) {
					Toast.makeText(context,
							R.string.date_picker,
							Toast.LENGTH_SHORT).show();
				} else {
					SimpleDateFormat simple = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String date = simple.format(c.getTime());
					edLeaveDate.setText(date);
					dialog.dismiss();
				}

			}
		});
		dialog.show();
	}

	public String transferDuration(String timeString) {
		int time = Integer.parseInt(timeString);
		int hour = time / 60;
		int min = time % 60;
		return hour + " "+getResources().getString(R.string.hour)+" " + min + " "+getResources().getString(R.string.min);
	}

	public String transferCost(String cost) {
		DecimalFormat formatter = new DecimalFormat("#,###,###");
		return formatter.format(Double.parseDouble(cost));

	}

	private Marker addMarkeronMaps(double lati, double longi, String title,
			String snippet, int icon) {
		return googleMap.addMarker(new MarkerOptions()
				.position(new LatLng(lati, longi)).title(title)
				.snippet(snippet)
				.icon(BitmapDescriptorFactory.fromResource(icon))
				.draggable(false));
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
				Toast.makeText(this,R.string.no_load_map,
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

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}


}
