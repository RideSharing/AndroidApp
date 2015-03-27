package com.halley.ridesharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.CustomNetworkImageView;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;

public class MapViewOfItineraryFragment extends Fragment implements
		OnMarkerDragListener, OnMarkerClickListener, InfoWindowAdapter {

	// Google Map
	private GoogleMap googleMap;
	Double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private boolean isFrom;
	private SessionManager session;
	private List<Marker> marker_drivers = new ArrayList<Marker>();
	private List<ItineraryItem> itineraryItems;
	private Marker marker_user;
	ImageLoader imageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map_view_of_itinerary,
				container, false);
		session = new SessionManager(this.getActivity());

		if (this.getArguments() != null) {
			fromLatitude = this.getArguments().getDouble("fromLatitude");
			fromLongitude = this.getArguments().getDouble("fromLongitude");
			toLatitude = this.getArguments().getDouble("toLatitude");
			toLongitude = this.getArguments().getDouble("toLongitude");
			isFrom = this.getArguments().getBoolean("isFrom");
			getDriver();
			initilizeMap();

			// adding marker
			marker_user = googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(fromLatitude, fromLongitude))
					.title("Địa chỉ hiện tại của bạn ")
					.snippet("marker_user")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_marker_start))
					.draggable(true));
			marker_user.hideInfoWindow();

		}
		return view;
	}

	private void getDriver() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver";
		itineraryItems = new ArrayList<ItineraryItem>();
		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_GET_ALL_ITINERARY,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d("Login Response: ", response.toString());

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
									itineraryItem
											.setAvatarlUrl("http://trithucsong.com/data/trithucsong_vanhoa/data/van-hoa-alotin-vn-images-van-hoa-nghe-thuat/alotin.vn_1404270588_048bd9c3e598d9a38aad3103a5b642b0.jpg");
									itineraryItem.setRating(4.8);
									itineraryItem.setLeave_date(itinerary
											.getString("leave_date"));
									itineraryItem.setCost(itinerary
											.getString("cost"));
									itineraryItems.add(itineraryItem);
									double latitude = Double.parseDouble(itinerary
											.getString("start_address_lat"));
									double longitude = Double.parseDouble(itinerary
											.getString("start_address_long"));
									Marker marker_driver = googleMap
											.addMarker(new MarkerOptions()
													.position(
															new LatLng(
																	latitude,
																	longitude))
													.title("người lái")
													.snippet(
															"marker_driver_"
																	+ i)
													.icon(BitmapDescriptorFactory
															.fromResource(R.drawable.ic_marker_driver))
													.draggable(false));
									marker_driver.showInfoWindow();
									marker_drivers.add(marker_driver);

								}

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
					.target(new LatLng(toLatitude, toLongitude)).zoom(12)
					.build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			googleMap.setOnMarkerDragListener(this);
			googleMap.setOnMarkerClickListener(this);
			// Setting a custom info window adapter for the google map
			googleMap.setInfoWindowAdapter(this);

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(this.getActivity(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		return false;
	}

	@Override
	public View getInfoContents(Marker marker) {

		
		try {
			int marker_id = Integer.parseInt(marker.getId().substring(1));
			// Getting view from the layout file info_window_layout
			View v = getActivity().getLayoutInflater().inflate(
					R.layout.popup_itinerary, null);
			v.setLayoutParams(new RelativeLayout.LayoutParams(400, 250));

			TextView tvstart_address = (TextView) v
					.findViewById(R.id.start_address);
			TextView tvend_address = (TextView) v.findViewById(R.id.end_address);
			TextView tvleave_date = (TextView) v.findViewById(R.id.leave_date);
			// getting itinerary data for the row
			ItineraryItem m = itineraryItems.get(marker_id - 1);
			// Toast.makeText(getActivity(),
			// m.getStart_address(),Toast.LENGTH_LONG).show();
			// rating

			// start_address
			tvstart_address.setText("Nơi đi: " + m.getStart_address());
			// end_address
			tvend_address.setText("Nơi đến: " + m.getEnd_address());
			// leave_date
			tvleave_date.setText("Ngày đi: " + m.getLeave_date());
			// Toast.makeText(getActivity(),
			// itineraryItems.get(marker_id-1).getStart_address(),
			// Toast.LENGTH_LONG).show();
			return v;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
