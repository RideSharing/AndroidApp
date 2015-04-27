package com.halley.manageitinerary;

import android.app.Fragment;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.adapter.ItineraryListAdapter;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;
import com.halley.ridesharing.MainActivity;
import com.halley.submititinerary.SubmitItineraryActivity;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by enclaveit on 4/23/15.
 */
public class ListItineraryByStatus extends Fragment{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView listView;
    private ItineraryListAdapter listAdapter;
    private List<ItineraryItem> itineraryItems;
    private SweetAlertDialog pDialog;
    private String status;
    private String role;
    SessionManager session;
    private PullRefreshLayout mSwipeRefreshLayout;
    private MyAsyncTask task;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_list_view_of_itinerary, container, false);
        mSwipeRefreshLayout = (PullRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        listView = (ListView) rootView.findViewById(R.id.list);
        session = new SessionManager(getActivity());
        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);

        // Get current location from TabListItineraryAdapter
        Bundle bundle=getArguments();
        if (bundle != null) {
            status=bundle.getString("status");
            role=bundle.getString("role");

            if(role.equals("driver")){
                getItineraries(AppConfig.URL_DRIVER_ITINERARY,status);
            }
            else{
                getItineraries(AppConfig.URL_CUSTOMER_ITINERARY,status);
            }
        }

        mSwipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                Toast.makeText(getActivity(),"MainActivity"+ "Refresh triggered at "
//                        ,Toast.LENGTH_LONG).show();
                task=new MyAsyncTask();
                task.execute();



            }
        });

        mSwipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);

        return rootView;
    }
    private void getItineraries(String url,final String status){
        //AppConfig.URL_CUSTOMER_ITINERARY
        // Tag used to cancel the request
        String tag_string_req = "req_get_driver_by_list";
        itineraryItems = new ArrayList<ItineraryItem>();
        pDialog.show();
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url,
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
                                    JSONObject itinerary = itineraries
                                            .getJSONObject(i);
                                    if(itinerary.getString("status").equals(status)){

                                        ItineraryItem itineraryItem = new ItineraryItem();
                                        itineraryItem.setDescription(itinerary
                                                .getString("description"));
                                        itineraryItem.setStart_address(itinerary
                                                .getString("start_address"));
                                        itineraryItem.setEnd_address(itinerary
                                                .getString("end_address"));
                                        itineraryItem.setAvatarlUrl(itinerary
                                                .getString("link_avatar"));
                                        itineraryItem.setRating(4.8);
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
                                        itineraryItems.add(itineraryItem);
                                    }
                                }
                                //Toast.makeText(getActivity(),itineraryItems.toString(),Toast.LENGTH_LONG).show();
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
    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        public MyAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

           Toast.makeText(getActivity(),"OK", Toast.LENGTH_LONG).show();

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
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
