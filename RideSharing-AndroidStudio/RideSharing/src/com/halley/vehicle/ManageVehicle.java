package com.halley.vehicle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.github.clans.fab.FloatingActionButton;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request.Method;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ManageVehicle extends ActionBarActivity {
    private final int REFRESH=1;
    private SessionManager session;
    private ListView list;
    private ActionBar actionBar;
    private CustomActionBar custom_actionbar;
    private FloatingActionButton fab;
    private PullRefreshLayout mSwipeRefreshLayout;
    private List<VehicleItem> itemVehicle;
    private SweetAlertDialog pDialog;
    private VehicleListAdapter vehicleAdapter;
    private Activity activity;
    private String vehicle_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_vehicle);
        session = new SessionManager(this);
        list = (ListView) findViewById(R.id.list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        pDialog = new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        getAllVehicle();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VehicleActivity.class);
                startActivityForResult(intent, REFRESH);
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VehicleItem m = itemVehicle.get(position);
                Intent intent = new Intent(getApplicationContext(), DetailVehicleActivity.class);
                intent.putExtra("bundle", m.getVehicle_id());
                startActivityForResult(intent, REFRESH);
            }
        });
    }


    public void getAllVehicle() {
        String tag_string_req = "get_all_vehicle";
        itemVehicle = new ArrayList<VehicleItem>();
        StringRequest strReq = new StringRequest(Method.GET,
                AppConfig.URL_GET_ALL_VEHICLE,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jObj = new JSONObject(response
                                    .substring(response.indexOf("{"),
                                            response.lastIndexOf("}") + 1));
                            boolean error = jObj.getBoolean("error");
                            // Check for error node in json

                            if (!error) {
                                JSONArray vehicles;
                                vehicles = jObj.getJSONArray("vehicles");
                                for (int i = 0; i < vehicles.length(); i++) {
                                    JSONObject vehicleJSON = vehicles.getJSONObject(i);
                                    VehicleItem vehicleItem = new VehicleItem();
                                    vehicleItem.setLicense_plate(vehicleJSON.getString("license_plate"));
                                    vehicleItem.setType(vehicleJSON.getString("type"));
                                    vehicleItem.setVehicle_img(vehicleJSON.getString("vehicle_img"));
                                    vehicleItem.setReg_certificate(vehicleJSON.getString("reg_certificate"));
                                    vehicleItem.setVehicle_id(vehicleJSON.getString("vehicle_id"));
                                    itemVehicle.add(vehicleItem);
                                }
                                vehicleAdapter = new VehicleListAdapter(activity, itemVehicle, getApplicationContext());
                                list.setAdapter(vehicleAdapter);


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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REFRESH) {
            if(resultCode == RESULT_OK){
               getAllVehicle();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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


}
