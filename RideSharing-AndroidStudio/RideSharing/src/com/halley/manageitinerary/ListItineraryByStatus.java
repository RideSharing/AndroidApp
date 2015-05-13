package com.halley.manageitinerary;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String DRIVER_ROLE="driver";
    private static final String CUSTOMER_ROLE="customer";
    private static final String ITINERARY_STATUS_CREATED ="1";
    private static final String ITINERARY_STATUS_CUSTOMER_ACCEPTED ="2";
    private static final String ITINERARY_STATUS_DRIVER_ACCEPTED ="3";
    private static final String ITINERARY_STATUS_FINISH ="4";
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

            if(role.equals(DRIVER_ROLE)){
                showDialog();
                getItineraries(AppConfig.URL_DRIVER_ITINERARY,status);
            }
            else{
                showDialog();
                getItineraries(AppConfig.URL_CUSTOMER_ITINERARY,status);
            }
        }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                        long arg3) {
                    final ItineraryItem m = itineraryItems.get(position);

                    if(m.getStatus().equals(ITINERARY_STATUS_CREATED)||m.getStatus().equals(ITINERARY_STATUS_DRIVER_ACCEPTED)||m.getStatus().equals(ITINERARY_STATUS_FINISH)){
                        Intent i=new Intent(getActivity(),DetailManageItineraryActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("itinerary_id", m.getItinerary_id());
                        bundle.putString("role",role);
                        bundle.putString("status",m.getStatus());
                        i.putExtra("bundle", bundle);

                        startActivity(i);
                    }
                    else if(m.getStatus().equals(ITINERARY_STATUS_CUSTOMER_ACCEPTED)){
                        if(role.equals(DRIVER_ROLE)) {
                            //Creating the instance of PopupMenu
                            PopupMenu popup = new PopupMenu(getActivity(), arg1);
                            //Inflating the Popup using xml file
                            popup.getMenuInflater().inflate(R.menu.pop_up_menu_itinerary_driver, popup.getMenu());

                            //registering popup with OnMenuItemClickListener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId()==R.id.see_itinerary){
                                        Intent i=new Intent(getActivity(),DetailManageItineraryActivity.class);
                                        Bundle bundle=new Bundle();
                                        bundle.putString("itinerary_id", m.getItinerary_id());
                                        bundle.putString("role",role);
                                        bundle.putString("status",m.getStatus());
                                        i.putExtra("bundle", bundle);

                                        startActivity(i);
                                    }
                                    else if(item.getItemId()==R.id.see_customer){
                                        getCustomerProfile(m.getCustomer_id(),m.getItinerary_id(),m.getStatus());
                                    }
                                    return true;
                                }
                            });
                            popup.show();//showing popup menu

                        }
                        else{
                            //Creating the instance of PopupMenu
                            PopupMenu popup = new PopupMenu(getActivity(), arg1);
                            //Inflating the Popup using xml file
                            popup.getMenuInflater().inflate(R.menu.pop_up_menu_itinerary_customer, popup.getMenu());

                            //registering popup with OnMenuItemClickListener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId()==R.id.see_itinerary){
                                        Intent i=new Intent(getActivity(),DetailManageItineraryActivity.class);
                                        Bundle bundle=new Bundle();
                                        bundle.putString("itinerary_id", m.getItinerary_id());
                                        bundle.putString("role",role);
                                        bundle.putString("status",m.getStatus());
                                        i.putExtra("bundle", bundle);

                                        startActivity(i);
                                    }
                                    else if(item.getItemId()==R.id.reject_itinerary){
                                        new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                                                .setTitleText(getResources().getString(R.string.you_sure))
                                                .setContentText(getResources().getString(R.string.customer_reject_itinerary))
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
                                                        rejectItinerarybyCustomer(m.getItinerary_id(),role,status);
                                                        sweetAlertDialog.cancel();

                                                    }
                                                })
                                                .show();
                                    }
                                    return true;
                                }
                            });
                            popup.show();//showing popup menu
                        }
                    }

                }
            });



        mSwipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                task=new MyAsyncTask();
                task.execute();



            }
        });

        mSwipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);

        return rootView;
    }

    private void rejectItinerarybyCustomer(final String itinerary_id,final String role,final String status){
        // Tag used to cancel the request
        String tag_string_req = "req_reject_itinerary";
        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CUSTOMER_REJECT_ITINERARY + "/" + itinerary_id+"?lang="+ Locale.getDefault().getLanguage(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Reject Itinerary Response: ",
                                response.toString());
                        hidePDialog();

                        try {
                            JSONObject jObj = new JSONObject(
                                    response.substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");
                            // Check for error node in json
                            if (!error) {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();
                                getItineraries(AppConfig.URL_CUSTOMER_ITINERARY,status);

                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Reject Error: ", "");
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hidePDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    private void getItineraries(String url,final String status){
        //AppConfig.URL_CUSTOMER_ITINERARY
        // Tag used to cancel the request
        String tag_string_req = "req_get_driver_by_list";
        itineraryItems = new ArrayList<ItineraryItem>();

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
                                        itineraryItem.setStatus(itinerary
                                                .getString("status"));
                                        itineraryItem.setCustomer_id(itinerary
                                                .getString("customer_id"));
                                        itineraryItem.setPhone(itinerary
                                                .getString("phone"));
                                        itineraryItem.setVehicle_id(itinerary.getString("vehicle_id"));
                                        itineraryItem.setVehicle_type(itinerary.getString("vehicle_type"));
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
    public void showDriverAcceptDialog(final String fullname,final String link_avatar,final String itinerary_id,final String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = View.inflate(getActivity(),
                R.layout.dialog_driver_accept_customer, null);
        builder.setView(view);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(getResources().getString(R.string.customer_profile));
        builder.setMessage(fullname + " "+getResources().getString(R.string.join_in_itinerary));
        final Button show = (Button) view.findViewById(R.id.show);
        final LinearLayout checkProfile = (LinearLayout) view.findViewById(R.id.checkProfile);
        checkProfile.setVisibility(View.GONE);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show.setVisibility(View.GONE);
                checkProfile.setVisibility(View.VISIBLE);
            }
        });
        TextView tvfullname = (TextView) view.findViewById(R.id.fullnameOther);
        RatingBar rating = (RatingBar) view.findViewById(R.id.ratingBarDriverOther);
        //TextView tvcomment = (TextView) view.findViewById(R.id.commentOther);
        ImageView avatarCustomer = (ImageView) view
                .findViewById(R.id.avatarOther);
        tvfullname.setText(fullname);
        //tvcomment.setText("He's crazy guys. Keep away from him!!!");
        rating.setRating(4.5f);
        byte[] decodeString = Base64.decode(link_avatar, Base64.DEFAULT);
        Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
                decodeString.length);
        avatarCustomer.setImageBitmap(decodeByte);
        builder.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                acceptCustomer(itinerary_id,status);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.deny), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
               rejectCustomer(itinerary_id,status);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void acceptCustomer(final String itinerary_id,final String status) {
        String tag_string_req = "req_persional";

        // pDialog.setMessage("Loading....");
        // showDialog();

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_ACCEPT_ITINERARY + "/"+itinerary_id+"lang="+Locale.getDefault().getLanguage(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("", "Driver Accept Response: " + response.toString());
                        // hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(
                                    response.substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {

                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();
                                getItineraries(AppConfig.URL_DRIVER_ITINERARY,status);

                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Accept User", ": " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                // hideDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void rejectCustomer(final String itinerary_id,final String status) {
        String tag_string_req = "reject_itinerary";


        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_DRIVER_REJECT_ITINERARY + "/"+itinerary_id+"?lang="+Locale.getDefault().getLanguage(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("", "Reject Itinerary Response: " + response.toString());


                        try {
                            JSONObject jObj = new JSONObject(
                                    response.substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {

                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();
                                getItineraries(AppConfig.URL_DRIVER_ITINERARY,status);


                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Reject Customer Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                // hideDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void getCustomerProfile(final String user_id,final String itinerary_id,final String status) {
        String tag_string_req = "req_profile";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_OTHER_PROFILE + "/"+user_id+"?lang="+Locale.getDefault().getLanguage(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("", "Profile Response: " + response.toString());

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {

                                String fullname = jObj.getString("fullname");
                                String link_avatar = jObj.getString("link_avatar");

                                showDriverAcceptDialog(fullname,link_avatar,itinerary_id,status);
                            } else {
                                // Error in login. Get the error message
                                String message = jObj.getString("message");
                                Toast.makeText(getActivity(),
                                        message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Profile Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hidePDialog();
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
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
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
            if(role.equals(DRIVER_ROLE)){
                getItineraries(AppConfig.URL_DRIVER_ITINERARY,status);
            }
            else{
                getItineraries(AppConfig.URL_CUSTOMER_ITINERARY,status);
            }

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


}
