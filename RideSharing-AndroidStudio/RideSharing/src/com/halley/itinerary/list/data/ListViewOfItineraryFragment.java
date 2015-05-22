package com.halley.itinerary.list.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;

import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.itinerary.list.adapter.ItineraryListAdapter;
import com.halley.itinerary.list.item.ItineraryItem;
import com.halley.registerandlogin.R;
import com.halley.itinerary.join_in.JoinItineraryActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ListViewOfItineraryFragment extends Fragment {
	private ListView listView;
	private ItineraryListAdapter listAdapter;
	private List<ItineraryItem> itineraryItems;
	private SweetAlertDialog pDialog;
	private double toLatitude, toLongitude, fromLatitude, fromLongitude;
	private boolean isFrom;
    String fromLocation, toLocation,cost="",leave_date="";
	SessionManager session;
    private PullRefreshLayout mSwipeRefreshLayout;
    private MyAsyncTask task;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(
				R.layout.fragment_list_view_of_itinerary, container, false);
		listView = (ListView) rootView.findViewById(R.id.list);
        mSwipeRefreshLayout = (PullRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
		session = new SessionManager(getActivity());
		pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);
		// Get current location from TabListItineraryAdapter
        Bundle bundle=getArguments();
        if (bundle != null) {
            fromLatitude = bundle.getDouble("fromLatitude");
            fromLongitude = bundle.getDouble("fromLongitude");
            toLatitude = bundle.getDouble("toLatitude");
            toLongitude = bundle.getDouble("toLongitude");
            toLocation = bundle.getString("toLocation");
            fromLocation = bundle.getString("fromLocation");
            cost=bundle.getString("cost");
            leave_date=bundle.getString("leave_date");
            isFrom = this.getArguments().getBoolean("isFrom");
		}
		if(!(fromLatitude==0f&&fromLongitude==0f)){
			getItineraries();
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position,
										long arg3) {
					ItineraryItem m = itineraryItems.get(position);
					Intent i = new Intent(getActivity(), JoinItineraryActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("avatar", m.getAvatarlUrl());
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
					bundle.putDouble("rating", m.getRating());
					bundle.putString("vehicle_type", m.getVehicle_type());
					i.putExtra("bundle", bundle);
					startActivity(i);

				}
			});
			mSwipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
//                Toast.makeText(getActivity(),"MainActivity"+ "Refresh triggered at "
//                        ,Toast.LENGTH_LONG).show();
					task = new MyAsyncTask();
					task.execute();
				}
			});

			mSwipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
		}
		return rootView;
	}

	private void getItineraries() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver_by_list";
		itineraryItems = new ArrayList<ItineraryItem>();
        String url="";
        if(isFrom){
            url=AppConfig.URL_GET_ALL_ITINERARY+"&start_address_lat="+fromLatitude+"&start_address_long="+fromLongitude;
        }
        else url=AppConfig.URL_GET_ALL_ITINERARY+"&start_address_lat="+fromLatitude+"&start_address_long="+fromLongitude+"&end_address_lat="+toLatitude+"&end_address_long="+toLongitude+"&cost="+cost+"&leave_date="+leave_date;;
        System.out.println("List: "+url);
        StringRequest strReq = new StringRequest(Method.GET,url
                ,
                new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d("List Response: ", response.toString());
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
									itineraryItem.setAvatarlUrl(itinerary
											.getString("link_avatar"));
									itineraryItem.setRating(Float.parseFloat(itinerary.getString("average_rating")));
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
                                    itineraryItem.setVehicle_type(itinerary
                                            .getString("vehicle_type"));
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
    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        public MyAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

           getItineraries();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SystemClock.sleep(2000);
            publishProgress();
            return null;
        }

        /**
         * update layout in function
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mSwipeRefreshLayout.setRefreshing(false);

        }

        /**
         * after process completed then this function will run
         */
        @Override
        protected void onPostExecute(Void result) {

        }

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
