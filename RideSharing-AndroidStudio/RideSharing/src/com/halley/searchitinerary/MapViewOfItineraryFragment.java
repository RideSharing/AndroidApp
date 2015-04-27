package com.halley.searchitinerary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
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
import com.halley.helper.SessionManager;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;
import com.halley.submititinerary.SubmitItineraryActivity;

public class MapViewOfItineraryFragment extends Fragment implements
		OnMarkerDragListener, OnMarkerClickListener, InfoWindowAdapter,
		OnInfoWindowClickListener {

	// Google Map
	private GoogleMap googleMap;
	Double fromLatitude, fromLongitude, toLatitude, toLongitude;
	private boolean isFrom;
    private String fromLocation, toLocation;
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
        Bundle bundle=getArguments();
		if (bundle != null) {
			fromLatitude = bundle.getDouble("fromLatitude");
			fromLongitude = bundle.getDouble("fromLongitude");
			toLatitude = bundle.getDouble("toLatitude");
			toLongitude = bundle.getDouble("toLongitude");
			toLocation = bundle.getString("toLocation");
            fromLocation = bundle.getString("fromLocation");
            isFrom = this.getArguments().getBoolean("isFrom");
			getDriver();
			initilizeMap();

			// adding marker
			marker_user = googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(fromLatitude, fromLongitude))
					.title(getResources().getString(R.string.current_address))
					.snippet("marker_user")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_marker_start))
					.draggable(true));
			marker_user.hideInfoWindow();

		}
		return view;
	}
    private String standardlizeString(String toLocation){
        toLocation= toLocation.substring(0,toLocation.length());
        //Toast.makeText(getActivity(),toLocation.replaceAll(" ","+"),Toast.LENGTH_LONG).show();
        return toLocation.replace(" ","+");
    }

	private void getDriver() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver";
		itineraryItems = new ArrayList<ItineraryItem>();
		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_GET_ALL_ITINERARY+"&start_address="+(fromLocation!=null?standardlizeString(fromLocation):"")+"&end_address="+(toLocation!=null?standardlizeString(toLocation):""),
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
                            String a=AppConfig.URL_GET_ALL_ITINERARY+"?start_address="+(fromLocation!=null?standardlizeString(fromLocation):"")+"&end_address="+(toLocation!=null?standardlizeString(toLocation):"");
                            System.out.println(a);
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
									itineraryItem.setDuration(itinerary.getString("duration"));
									itineraryItem.setDistance(itinerary.getString("distance"));
									itineraryItem.setFullname(itinerary.getString("fullname"));
									itineraryItem.setPhone(itinerary.getString("phone"));
									itineraryItem.setItinerary_id(itinerary.getString("itinerary_id"));
									itineraryItems.add(itineraryItem);
									double latitude;
									double longitude;
									if (isFrom) {
										latitude = Double.parseDouble(itinerary
												.getString("start_address_lat"));
										longitude = Double.parseDouble(itinerary
												.getString("start_address_long"));
									} else {
										latitude = Double.parseDouble(itinerary
												.getString("end_address_lat"));
										longitude = Double.parseDouble(itinerary
												.getString("end_address_long"));
									}

									Marker marker_driver = googleMap
											.addMarker(new MarkerOptions()
													.position(
															new LatLng(
																	latitude,
																	longitude))
													.title(getResources().getString(R.id.driver))
													.snippet(
															"marker_driver_"
																	+ i)
													.icon(BitmapDescriptorFactory
															.fromResource(R.drawable.ic_marker_driver))
													.draggable(false));
									//marker_driver.showInfoWindow();
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) this.getActivity().getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			//googleMap.setMyLocationEnabled(true);

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
			googleMap.setOnInfoWindowClickListener(this);

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(this.getActivity(),
						R.string.no_load_map, Toast.LENGTH_SHORT)
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
			TextView tvend_address = (TextView) v
					.findViewById(R.id.end_address);
			TextView tvleave_date = (TextView) v.findViewById(R.id.leave_date);
			// getting itinerary data for the row
			ItineraryItem m = itineraryItems.get(marker_id - 1);
			// Toast.makeText(getActivity(),
			// m.getStart_address(),Toast.LENGTH_LONG).show();
			// rating
			// start_address
			tvstart_address.setText(R.string.start_addess + m.getStart_address());
			// end_address
			tvend_address.setText(R.string.end_addess + m.getEnd_address());
			// leave_date
			tvleave_date.setText(R.string.leave_date + m.getLeave_date());
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

	@Override
	public void onInfoWindowClick(Marker marker) {
		// Toast.makeText(getActivity(),
		// marker.getSnippet().toString(),Toast.LENGTH_LONG).show();
		try {
			int marker_id = Integer.parseInt(marker.getId().substring(1));
			// Getting view from the layout file info_window_layout
			
			// getting itinerary data for the row
			ItineraryItem m = itineraryItems.get(marker_id - 1);
			Intent i=new Intent(getActivity(),SubmitItineraryActivity.class);
			Bundle bundle=new Bundle();
			bundle.putString("avatar", m.getAvatarlUrl());
			bundle.putString("fullname",m.getFullname());
			bundle.putString("description",m.getDescription());
			bundle.putString("start_address", m.getStart_address());
			bundle.putString("end_address", m.getEnd_address());
			bundle.putString("duration", m.getDuration());
			bundle.putString("distance", m.getDistance());
			bundle.putString("cost",m.getCost());
			bundle.putString("phone",m.getPhone());
			bundle.putString("leave_date",m.getLeave_date());
			bundle.putString("itinerary_id", m.getItinerary_id());
			i.putExtra("bundle", bundle);
			startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
