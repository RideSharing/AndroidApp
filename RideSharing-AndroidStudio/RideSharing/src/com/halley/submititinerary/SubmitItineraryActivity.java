package com.halley.submititinerary;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.RoundedImageView;
import com.halley.helper.SessionManager;
import com.halley.manageitinerary.ManageItineraryActivity;
import com.halley.profile.ProfileActivity;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SubmitItineraryActivity extends ActionBarActivity {
    private final String STATUS_USER_VERIFY_COMPLETED="4";
	private ActionBar actionBar;
	private RoundedImageView imavatar;
	private TextView tvfullname, tvdescription, tvstart_address, tvend_address,
			tvduration, tvdistance, tvcost, tvphone, tvleave_date,tvvehicle_type;
	private String avatar = "", fullname = "", description = "", start_address = "",
			end_address = "", duration = "", distance = "", cost = "", vehicle_type="",
			phone = "", leave_date, itinerary_id = "";
    private Double rating;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;

    private SessionManager session;
	private Context context=this;
	private RatingBar ratingbar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_itinerary);
        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
		// Session manager
		session = new SessionManager(getApplicationContext());
		imavatar = (RoundedImageView) findViewById(R.id.avatar);
		tvfullname = (TextView) findViewById(R.id.fullname);
		tvdescription = (TextView) findViewById(R.id.description);
		tvstart_address = (TextView) findViewById(R.id.startAddress);
		tvend_address = (TextView) findViewById(R.id.endAddress);
		tvduration = (TextView) findViewById(R.id.duration);
		tvdistance = (TextView) findViewById(R.id.distance);
		tvcost = (TextView) findViewById(R.id.cost);
		tvphone = (TextView) findViewById(R.id.phone);
		tvleave_date = (TextView) findViewById(R.id.leave_date);
		ratingbar=(RatingBar)findViewById(R.id.ratingBarDriver);
        tvvehicle_type=(TextView)findViewById(R.id.vehicle_type);
		Bundle bundle = this.getIntent().getExtras().getBundle("bundle");
		if (bundle != null) {
			avatar = bundle.getString("avatar");
			fullname = bundle.getString("fullname");
			description = bundle.getString("description");
			start_address = bundle.getString("start_address");
			end_address = bundle.getString("end_address");
			duration = bundle.getString("duration");
			distance = bundle.getString("distance");
			cost = bundle.getString("cost");
			phone = bundle.getString("phone");
			leave_date = bundle.getString("leave_date");
			itinerary_id = bundle.getString("itinerary_id");
            rating = bundle.getDouble("rating");
            vehicle_type= bundle.getString("vehicle_type");
		}
		if (!"".equals(avatar)) {
			byte[] decodeString = Base64.decode(avatar, Base64.DEFAULT);
			Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
					decodeString.length);

			imavatar.setImageBitmap(decodeByte);
		}
		tvfullname.setText(fullname.toUpperCase());
		tvdescription.setText(description);
		tvstart_address.setText(start_address);
		tvend_address.setText(end_address);
		tvduration.setText(transferDuration(duration));
		tvdistance.setText(distance);
		tvleave_date.setText(leave_date);
		tvcost.setText(transferCost(cost));
		tvphone.setText(phone);
        ratingbar.setEnabled(false);
        ratingbar.setRating(rating.floatValue());
        tvvehicle_type.setText(vehicle_type);
	}

	public void submitItineraryonClick(View v) {
        checkVerifyUser();
	}

    public String transferDuration(String timeString) {
        if(!timeString.equals("null")) {
            int time = Integer.parseInt(timeString);
            int hour = time / 60;
            int min = time % 60;
            return hour + " "+getResources().getString(R.string.hour)+" " + min + " "+getResources().getString(R.string.min);
        }
        return  "0 "+getResources().getString(R.string.hour)+"  0 "+getResources().getString(R.string.min);

    }

    public String transferCost(String cost) {
        if(!cost.equals("null")) {
            DecimalFormat formatter = new DecimalFormat("#,###,###");
            return formatter.format(Double.parseDouble(cost));
        }
        return "0";

    }

    public void checkVerifyUser() {
        String tag_string_req = "req_profile";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_USER+"/status"+"?lang="+Locale.getDefault().getLanguage(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Verify User Response: " + response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String status=jObj.getString("status");
                        if(status.equals(STATUS_USER_VERIFY_COMPLETED)) {
                            submitItinerary(itinerary_id);
                        }
                        else{
                            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText(getResources().getString(R.string.announce))
                                    .setContentText(getResources().getString(R.string.not_verify_user))
                                    .setConfirmText(getResources().getString(R.string.ok))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.cancel();
                                        }
                                    })
                                    .show();
                        }

                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
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
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

	private void submitItinerary(final String itinerary_id) {
		// Tag used to cancel the request
		String tag_string_req = "req_submit_itinerary";

		pDialog.setTitleText(getResources().getString(R.string.process_data));
		showDialog();

		StringRequest strReq = new StringRequest(Method.PUT,
				AppConfig.URL_SUBMIT_ITINERARY + "/" + itinerary_id+"?lang="+ Locale.getDefault().getLanguage(),
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d("Submit Itinerary Response: ",
								response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
								Intent i = new Intent(getApplicationContext(),
										ManageItineraryActivity.class);
								startActivity(i);
								finish();

							} else {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(getApplicationContext(),
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
						Log.e("Login Error: ", "");
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
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
