package com.halley.manageitinerary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

public class ManageItineraryActivity extends ActionBarActivity {
	private static final String TAG = ManageItineraryActivity.class
			.getSimpleName();
	private ProgressDialog pDialog;
	private List<ListManageItem> itineraryList;
	ListView listView;
	private ListManageItineraryAdapter adapter;
	SessionManager session;
	private Activity activity = this;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_itinerary);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.ic_manage_itinerary);
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.list);

		listView.setAdapter(adapter);
		session = new SessionManager(getApplicationContext());
		pDialog = new ProgressDialog(this);
		// Showing progress dialog before making http request
		pDialog.setMessage("Loading...");
		pDialog.show();
		getItinerary();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				ListManageItem m = itineraryList.get(position);
				Intent i = new Intent(activity,
						DetailManageItineraryActivity.class);
				Bundle bundle = new Bundle();

				bundle.putString("fullname", m.getFullname());
				bundle.putString("description", m.getDescription());
				bundle.putString("start_address", m.getStart_address());
				bundle.putString("end_address", m.getEnd_address());
				bundle.putString("duration", m.getDuration());
				bundle.putString("distance", m.getDistance());
				bundle.putString("cost", m.getCost());
				bundle.putString("phone", m.getPhone());
				bundle.putString("leave_date", m.getLeave_date());
				bundle.putString("itinerary_id", m.getItinerary_id());
				bundle.putDouble("start_address_lat",
						Double.parseDouble(m.getStart_address_lat()));
				bundle.putDouble("start_address_long",
						Double.parseDouble(m.getStart_address_long()));
				bundle.putDouble("end_address_lat",
						Double.parseDouble(m.getEnd_address_lat()));
				bundle.putDouble("end_address_long",
						Double.parseDouble(m.getEnd_address_long()));
				i.putExtra("bundle", bundle);
				startActivity(i);

			}
		});
		// changing action bar color
		// getActionBar().setBackgroundDrawable(
		// new ColorDrawable(Color.parseColor("#1b1b1b")));

	}

	private void getItinerary() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver_by_list";
		itineraryList = new ArrayList<ListManageItem>();

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_LIST_ITINERARY, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						// Log.d("Login Response: ", response.toString());
						hidePDialog();
						try {

							JSONObject jObj = new JSONObject(response
									.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json

							if (!error) {
								JSONArray itineraries;
								itineraries = jObj.getJSONArray("itineraries");

								for (int i = 0; i < itineraries.length(); i++) {
									ListManageItem itineraryItem = new ListManageItem();
									JSONObject itinerary = itineraries
											.getJSONObject(i);
									itineraryItem.setDescription(itinerary
											.getString("description"));
									itineraryItem.setStart_address(itinerary
											.getString("start_address"));
									itineraryItem.setEnd_address(itinerary
											.getString("end_address"));
									itineraryItem.setStart_address_lat(itinerary
											.getString("start_address_lat"));
									itineraryItem.setStart_address_long(itinerary
											.getString("start_address_long"));
									itineraryItem.setEnd_address_lat(itinerary
											.getString("end_address_lat"));
									itineraryItem.setEnd_address_long(itinerary
											.getString("end_address_long"));
									itineraryItem.setLeave_date(itinerary
											.getString("leave_date"));
									itineraryItem.setCost(itinerary
											.getString("cost"));
									itineraryItem.setFullname(itinerary
											.getString("fullname"));
									itineraryItem.setDuration(itinerary
											.getString("duration"));
									itineraryItem.setDistance(itinerary
											.getString("distance"));
									itineraryItem.setPhone(itinerary
											.getString("phone"));
									itineraryItem.setItinerary_id(itinerary
											.getString("itinerary_id"));
									itineraryList.add(itineraryItem);
								}
								adapter = new ListManageItineraryAdapter(
										activity, itineraryList);
								listView.setAdapter(adapter);

								// Toast.makeText(getActivity(),
								// itineraries.getJSONObject(0).getString("start_address_lat"),Toast.LENGTH_LONG).show();

							} else {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(activity, message,
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

						Toast.makeText(activity,
								"Không thể kết nối đến server",
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_itinerary, menu);
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
	public void onDestroy() {
		super.onDestroy();
		hidePDialog();
	}

	private void hidePDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
			pDialog = null;
		}
	}
}
