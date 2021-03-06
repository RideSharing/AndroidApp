package com.halley.registeritinerary;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

public class RegisterAdvanceDialog extends DialogFragment {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private ActionBar actionBar;
	private EditText etDescription;
	private TextView etLeave_date;
	private TextView etDistance;
	private EditText etDuration;
	private EditText etCost;
	private Button btnRegister;
	private String start_address = "", end_address = "", leave_date = "",
			duration = "", cost = "", description = "", distance = "";
	private Double start_address_lat, start_address_long, end_address_lat,
			end_address_long;
	private SessionManager session;
	private ProgressDialog pDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** Inflating layout for the dialog */

		View v = inflater.inflate(R.layout.dialog_register_advance, null);
		getDialog().setTitle("Đăng kí bổ sung");
		session = new SessionManager(getActivity());

		pDialog = new ProgressDialog(getActivity());
		btnRegister = (Button) v.findViewById(R.id.btnRegister);
		etDescription = (EditText) v.findViewById(R.id.description);
		etLeave_date = (TextView) v.findViewById(R.id.leave_date);
		etDuration = (EditText) v.findViewById(R.id.duration);
		etCost = (EditText) v.findViewById(R.id.cost);
		etDistance = (TextView) v.findViewById(R.id.distance);
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
		etCost.setHint("Gợi ý: "
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

					Toast.makeText(getActivity(), R.string.no_input,
							Toast.LENGTH_SHORT).show();
				} else {
					leave_date = etLeave_date.getText().toString();
					description = etDescription.getText().toString();
					duration = etDuration.getText().toString();
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
		return formatter.format(cost.doubleValue()) + " VNĐ";

	}

	private String transDuration(String duration) {
		String[] str;
		String sumMin = "0";
		try {
			str = duration.split(" ");

			if (str.length == 2) {
				sumMin = Integer.parseInt(str[0]) + " phút";

			} else {
				if (!str[1].equals("day")) {
					sumMin = Integer.parseInt(str[0]) + " giờ "
							+ Integer.parseInt(str[2]) + " phút";

				} else {
					sumMin = Integer.parseInt(str[0]) * 24 + " ngày "
							+ +Integer.parseInt(str[2]) + " giờ";

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
				if (!str[1].equals("ngày")) {
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
		// Toast.makeText(this, "" + sumMin, Toast.LENGTH_LONG).show();
		return String.valueOf(sumMin);
	}

	private void showDateTimePicker() {
		final Dialog dialog = new Dialog(getActivity());

		dialog.setContentView(R.layout.dialog_datetime_picker);
		dialog.setTitle("Ngày đi");
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
							"Không thể chọn thời gian trong quá khứ",
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
		// TODO:
	}
}
