package com.halley.ridesharing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.CustomNetworkImageView;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.adapter.ItineraryListAdapter;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;

public class ListViewOfItineraryFragment extends Fragment {
	private static final String TAG = MainActivity.class.getSimpleName();
	private ListView listView;
	private ItineraryListAdapter listAdapter;
	private List<ItineraryItem> itineraryItems;
	private static final String url = "http://192.168.10.74/itinerary.json";
	private ProgressDialog pDialog;
	private Geocoder geocoder;
	private double toLatitude, toLongitude, fromLatitude, fromLongitude;
	private boolean isFrom;
	SessionManager session;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(
				R.layout.fragment_list_view_of_itinerary, container, false);
		listView = (ListView) rootView.findViewById(R.id.list);
		session = new SessionManager(getActivity());
		pDialog = new ProgressDialog(this.getActivity());
		// Showing progress dialog before making http request
		pDialog.setMessage("Đang load dữ liệu...");
		pDialog.show();
		// Get current location from TabListItineraryAdapter
		if (this.getArguments() != null) {
			fromLatitude = this.getArguments().getDouble("fromLatitude");
			fromLongitude = this.getArguments().getDouble("fromLongitude");
			toLatitude = this.getArguments().getDouble("toLatitude");
			toLongitude = this.getArguments().getDouble("toLongitude");
			isFrom = this.getArguments().getBoolean("isFrom");
		}
		getDriver();
		return rootView;
	}

	private void getDriver() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver_by_list";
		itineraryItems = new ArrayList<ItineraryItem>();

		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_GET_ALL_ITINERARY,
				new Response.Listener<String>() {

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
									ItineraryItem itineraryItem = new ItineraryItem();
									JSONObject itinerary = itineraries
											.getJSONObject(i);
									itineraryItem.setDescription(itinerary
											.getString("description"));
									itineraryItem.setStart_address(itinerary
											.getString("start_address"));
									itineraryItem.setEnd_address(itinerary
											.getString("end_address"));
									itineraryItem.setAvatarlUrl(itinerary.getString("link_avatar"));
									itineraryItem.setRating(4.8);
									itineraryItem.setLeave_date(itinerary
											.getString("leave_date"));
									itineraryItem.setCost(itinerary
											.getString("cost"));
									Log.d("OK",
											itineraryItem.getStart_address());
									// Toast.makeText(getActivity(),itinerary.toString(),
									// Toast.LENGTH_LONG).show();
									itineraryItems.add(itineraryItem);
								}
								listAdapter = new ItineraryListAdapter(
										getActivity(), itineraryItems);
								listView.setAdapter(listAdapter);

								// Toast.makeText(getActivity(),
								// itineraries.getJSONObject(0).getString("start_address_lat"),Toast.LENGTH_LONG).show();

							} else {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(getActivity(), message,
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
						
						Toast.makeText(getActivity(),
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

	public void showListView() {

		itineraryItems = new ArrayList<ItineraryItem>();

		listAdapter = new ItineraryListAdapter(this.getActivity(),
				itineraryItems);
		listView.setAdapter(listAdapter);

		// Creating volley request obj
		JsonArrayRequest ItineraryReq = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.d(TAG, response.toString());
						hidePDialog();

						if (response.toString() == null) {

						} else {
							// Parsing json
							for (int i = 0; i < response.length(); i++) {
								try {

									JSONObject obj = response.getJSONObject(i);
									ItineraryItem itineraryItem = new ItineraryItem();
									itineraryItem.setDescription(obj
											.getString("description"));
									itineraryItem.setStart_address(obj
											.getString("start_addess"));
									itineraryItem.setEnd_address(obj
											.getString("end_addess"));
									itineraryItem.setAvatarlUrl(obj
											.getString("image"));
									itineraryItem.setRating(((Number) obj
											.get("rating")).doubleValue());
									itineraryItem.setLeave_date(obj
											.getString("leave_date"));
									itineraryItem
											.setCost(obj.getString("cost"));
									itineraryItems.add(itineraryItem);

								} catch (JSONException e) {
									e.printStackTrace();
								}

							}
						}

						// notifying list adapter about data changes
						// so that it renders the list view with updated data
						listAdapter.notifyDataSetChanged();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d(TAG, "Error: " + error.getMessage());
						hidePDialog();

					}
				});

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(ItineraryReq);

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

	public String getDetailLocation(double latitude, double longitude) {
		Address address = null;
		List<android.location.Address> list_address = null;
		geocoder = new Geocoder(this.getActivity(), Locale.getDefault());
		String detail_address = null;
		try {
			list_address = geocoder.getFromLocation(latitude, longitude, 1);
			address = list_address.get(0);

			// Toast.makeText(this, "submit " + locality,
			// Toast.LENGTH_SHORT).show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (address != null) {
			detail_address = (address.getAddressLine(0) == null ? "" : address
					.getAddressLine(0))
					+ " "
					+ (address.getAddressLine(0) == null ? "" : address
							.getAddressLine(1))
					+ " "
					+ (address.getAddressLine(0) == null ? "" : address
							.getAddressLine(2))
					+ " "
					+ (address.getAddressLine(0) == null ? "" : address
							.getAddressLine(3));
		}
		return detail_address;
	}
}
