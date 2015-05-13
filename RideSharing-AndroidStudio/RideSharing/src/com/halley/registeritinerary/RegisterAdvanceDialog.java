package com.halley.registeritinerary;

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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.NumberTextWatcher;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.RegisterActivity;
import com.halley.ridesharing.MainActivity;
import com.halley.vehicle.VehicleItem;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterAdvanceDialog extends DialogFragment {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private EditText etDescription;
	private TextView etLeave_date;
	private TextView etDistance;
	private EditText etDuration;
	private EditText etCost;
	private Button btnRegister;
	private String start_address = "", end_address = "", leave_date = "",
			duration = "", cost = "", description = "", distance = "", vehicle_id;
	private Double start_address_lat, start_address_long, end_address_lat,
			end_address_long;
	private SessionManager session;
	private SweetAlertDialog pDialog;
    private Spinner spvehicle_type;
    private List<VehicleItem> vehicleItems;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** Inflating layout for the dialog */

		View v = inflater.inflate(R.layout.dialog_register_advance, null);
		getDialog().setTitle(R.string.title_activity_register_advance);
		session = new SessionManager(getActivity());

		pDialog = new SweetAlertDialog(getActivity(),SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);

		btnRegister = (Button) v.findViewById(R.id.btnRegister);
		etDescription = (EditText) v.findViewById(R.id.description);
		etLeave_date = (TextView) v.findViewById(R.id.leave_date);
		etDuration = (EditText) v.findViewById(R.id.duration);
		etCost = (EditText) v.findViewById(R.id.cost);
		etDistance = (TextView) v.findViewById(R.id.distance);
        spvehicle_type= (Spinner) v.findViewById(R.id.vehicle);
        getAllItinerary();
		Bundle bundle = this.getArguments();
		if (bundle != null) {

			start_address = bundle.getString("start_address");
			start_address_lat = bundle.getDouble("start_address_lat");
			start_address_long = bundle.getDouble("start_address_long");
			end_address = bundle.getString("end_address");
			end_address_lat = bundle.getDouble("end_address_lat");
			end_address_long = bundle.getDouble("end_address_long");
			distance = bundle.getString("distance");
			duration = transDuration(bundle.getString("duration"));
		}
		etDistance.setText(distance);
		etDuration.setText(duration);
		etCost.setHint(getResources().getString(R.string.hint)+" "
				+ transferCost(Double
						.parseDouble(getDigitfromDistance(distance)) * 15500 / 40));
		etCost.addTextChangedListener(new NumberTextWatcher(etCost));
		etLeave_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showDateTimePicker();

			}
		});
		btnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (etDescription.getText().toString().trim().length() == 0
						|| etLeave_date.getText().toString().trim().length() == 0
						|| etDuration.getText().toString().trim().length() == 0
						|| etDistance.getText().toString().trim().length() == 0
						|| etCost.getText().toString().trim().length() == 0) {

					Toast.makeText(getActivity(),R.string.no_input,
							Toast.LENGTH_SHORT).show();
				} else {
					leave_date = etLeave_date.getText().toString();
					description = etDescription.getText().toString();
					duration =etDuration.getText().toString();
					cost = etCost.getText().toString().replace(",", "");
					addItinerary(start_address, start_address_lat,
							start_address_long, end_address, end_address_lat,
							end_address_long, duration, distance, cost,
							description, leave_date);

				}

			}
		});
		return v;
	}

	public String transferCost(Double cost) {
		DecimalFormat formatter = new DecimalFormat("#,###,###");
		return formatter.format(cost.doubleValue());

	}

	private String transDuration(String duration) {
		String[] str;
		String sumMin = "0";
		try {
			str = duration.split(" ");

			if (str.length == 2) {
				sumMin = Integer.parseInt(str[0]) + " "+getResources().getString(R.string.min);

			} else {
				if (!str[1].equals("day")) {
					sumMin = Integer.parseInt(str[0]) + " "+getResources().getString(R.string.hour)
							+" "+Integer.parseInt(str[2]) + " "+getResources().getString(R.string.min);

				} else {
					sumMin = Integer.parseInt(str[0]) * 24 + " "+getResources().getString(R.string.day)
							+ +Integer.parseInt(str[2]) + " "+getResources().getString(R.string.hour);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Toast.makeText(this, "" + sumMin, Toast.LENGTH_LONG).show();
		return String.valueOf(sumMin);
	}

	private String getDigitfromDistance(String distance) {
		String[] str = null;
		String sumKm = "0";
		try {
			str = distance.split(" ");
			sumKm = str[0];
			// Toast.makeText(this, str[0], Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sumKm;
	}

	private String getDigitfromDuration(String duration) {
		String[] str;
		int sumMin = 0;
		try {
			str = duration.split(" ");
			if (str.length == 2) {
				sumMin = Integer.parseInt(str[0]);

			} else {
				if (!str[1].equals(R.string.day)) {
					sumMin = Integer.parseInt(str[0]) * 60
							+ Integer.parseInt(str[2]);

				} else {
					sumMin = Integer.parseInt(str[0]) * 60 * 24
							+ Integer.parseInt(str[2]) * 60;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		 Toast.makeText(getActivity(), "" + sumMin, Toast.LENGTH_LONG).show();
		return String.valueOf(sumMin);
	}

	private void showDateTimePicker() {
		final Dialog dialog = new Dialog(getActivity());

		dialog.setContentView(R.layout.dialog_datetime_picker);
		dialog.setTitle(R.string.leave_date);
		final DatePicker datepicker = (DatePicker) dialog
				.findViewById(R.id.datePicker1);
		final TimePicker timepicker = (TimePicker) dialog
				.findViewById(R.id.timePicker1);
		final Button btnOK = (Button) dialog.findViewById(R.id.OK);

		btnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Calendar c = Calendar.getInstance();
				Date currentTime = c.getTime();
				int year = datepicker.getYear();
				int month = datepicker.getMonth();
				int day = datepicker.getDayOfMonth();

				int hour = timepicker.getCurrentHour();
				int min = timepicker.getCurrentMinute();
				c.set(year, month, day, hour, min);
				if (currentTime.after(c.getTime())) {
					Toast.makeText(getActivity(),
							R.string.date_picker,
							Toast.LENGTH_SHORT).show();
				} else {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String date = dateFormat.format(c.getTime());
					etLeave_date.setText(date);
					dialog.dismiss();
				}

			}
		});

		dialog.show();
	}

    private void getAllItinerary() {
        // Tag used to cancel the request
        String tag_string_req = "req_get_itinerary_by_id";
        vehicleItems= new ArrayList<VehicleItem>();
        StringRequest strReq = new StringRequest(Method.GET,
                AppConfig.URL_GET_ALL_VEHICLE,
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
                               JSONArray vehicles=jObj.getJSONArray("vehicles");
                                for(int i=0;i<vehicles.length();i++){
                                    VehicleItem vehicleItem=new VehicleItem();
                                    JSONObject vehicle = vehicles.getJSONObject(i);
                                    vehicleItem.setVehicle_id(vehicle.getString("vehicle_id"));
                                    vehicleItem.setType(vehicle.getString("type"));
                                    vehicleItems.add(vehicleItem);
                                }
                                VehicleAdapter adapter=new VehicleAdapter(vehicleItems,getActivity());
                                spvehicle_type.setAdapter(adapter);
                                spvehicle_type.setOnItemSelectedListener(new MyProcessEvent());

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

	private void addItinerary(final String start_address,
			final Double start_address_lat, final Double start_address_long,
			final String end_address, final Double end_address_lat,
			final Double end_address_long, final String duration,
			final String distance, final String cost, final String description,
			final String leave_date) {
		// Tag used to cancel the request
		String tag_string_req = "req_register_itinerary_2";
		showDialog();
		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_REGISTER_ITINERARY,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG,
								"Register Itinerary Response: "
										+ response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {
								Toast.makeText(getActivity(),
										jObj.getString("message"),
										Toast.LENGTH_LONG).show();
								// Launch main activity
								Intent intent = new Intent(getActivity(),
										MainActivity.class);
								startActivity(intent);

								getActivity().finish();

							} else {
								// Error in register itinerary. Get the error
								// message
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
						Log.e(TAG,
								"Register Itinerary Error: "
										+ error.getMessage());
						Toast.makeText(getActivity(), error.getMessage(),
								Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				// 'start_address','start_address_lat','start_address_long','end_address',
				// 'end_address_lat','end_address_long','leave_date','duration','cost'
				Map<String, String> params = new HashMap<String, String>();
				params.put("start_address", start_address);
				params.put("start_address_lat", start_address_lat.toString());
				params.put("start_address_long", start_address_long.toString());
				params.put("end_address", end_address);
				params.put("end_address_lat", end_address_lat.toString());
				params.put("end_address_long", end_address_long.toString());
				params.put("leave_date", leave_date);
				params.put("duration", getDigitfromDuration(duration));
				params.put("distance", getDigitfromDistance(distance));
				params.put("cost", cost);
				params.put("description", description);

				return params;
			}

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



	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
		Window window = getDialog().getWindow();
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		window.setGravity(Gravity.CENTER);
	}
    private class MyProcessEvent implements
            AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
            Spinner spinner = (Spinner) parent;
            vehicle_id=vehicleItems.get(arg2).getVehicle_id();


        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
}
