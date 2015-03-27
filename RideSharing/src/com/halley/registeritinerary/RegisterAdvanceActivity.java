package com.halley.registeritinerary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.RegisterActivity;
import com.halley.ridesharing.MainActivity;

public class RegisterAdvanceActivity extends ActionBarActivity {
	// LogCat tag
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private ActionBar actionBar;
	private EditText etDescription;
	private EditText etStartAddress;
	private EditText etEndAddress;
	private TextView etLeave_date;
	private TextView etDistance;
	private EditText etDuration;
	private EditText etCost;
	private Button btnRegister;
	private Context context = this;
	private String start_address = "", end_address = "", leave_date = "",
			duration = "", cost = "", description = "", distance = "";
	private Double start_address_lat, start_address_long, end_address_lat,
			end_address_long;
	private SessionManager session;
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_advance);
		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);
		session = new SessionManager(context);
		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Đang xử lý dữ liệu...");
		etDescription = (EditText) findViewById(R.id.description);
		etStartAddress = (EditText) findViewById(R.id.startAddress);
		etEndAddress = (EditText) findViewById(R.id.endAddress);
		etLeave_date = (TextView) findViewById(R.id.leave_date);
		etDuration = (EditText) findViewById(R.id.duration);
		etCost = (EditText) findViewById(R.id.cost);
		etDistance = (TextView) findViewById(R.id.distance);
		Bundle bundle = this.getIntent().getExtras();

		if (bundle != null) {
			start_address = bundle.getString("start_address");
			start_address_lat = bundle.getDouble("start_address_lat");
			start_address_long = bundle.getDouble("start_address_long");
			end_address = bundle.getString("end_address");
			end_address_lat = bundle.getDouble("end_address_lat");
			end_address_long = bundle.getDouble("end_address_long");
			distance = bundle.getString("distance");
			duration = bundle.getString("duration");
		}
		etStartAddress.setText(start_address);
		etEndAddress.setText(end_address);
		etDistance.setText(distance);
		etDuration.setText(duration);

		etLeave_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showDateTimePicker();

			}
		});

	}

	private String getDigitfromDistance(String distance) {
		String[] str;
		str = distance.split(" ");
		// Toast.makeText(this, str[0], Toast.LENGTH_LONG).show();
		return str[0];
	}

	private String getDigitfromDuration(String duration) {
		String[] str;
		str = duration.split(" ");
		int sumMin = 0;
		if (str.length == 2) {
			sumMin = Integer.parseInt(str[0]);

		} else {
			if (!str[1].equals("day")) {
				sumMin = Integer.parseInt(str[0]) * 60
						+ Integer.parseInt(str[2]);

			} else {
				sumMin = Integer.parseInt(str[0]) * 60 * 24
						+ Integer.parseInt(str[2]) * 60;

			}
		}
		// Toast.makeText(this, "" + sumMin, Toast.LENGTH_LONG).show();
		return String.valueOf(sumMin);
	}

	private void showDateTimePicker() {
		final Dialog dialog = new Dialog(context);

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
				int year = datepicker.getYear();
				int month = datepicker.getMonth();
				int day = datepicker.getDayOfMonth();

				int hour = timepicker.getCurrentHour();
				int min = timepicker.getCurrentMinute();
				c.set(year, month, day, hour, min);
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String date = dateFormat.format(c.getTime());
				etLeave_date.setText(date);
				dialog.dismiss();

			}
		});

		dialog.show();
	}

	public void registerOnclick(View v) {
		if (etDescription.getText().toString().trim().length() == 0
				|| etStartAddress.length() == 0
				|| etEndAddress.getText().toString().trim().length() == 0
				|| etLeave_date.getText().toString().trim().length() == 0
				|| etDuration.getText().toString().trim().length() == 0
				|| etDistance.getText().toString().trim().length() == 0
				|| etCost.getText().toString().trim().length() == 0) {

			Toast.makeText(context,
					this.getResources().getString(R.string.no_input),
					Toast.LENGTH_SHORT).show();
		} else {
			leave_date = etLeave_date.getText().toString();
			description = etDescription.getText().toString();
			duration = etDuration.getText().toString();
			cost = etCost.getText().toString();
			addItinerary(start_address, start_address_lat, start_address_long,
					end_address, end_address_lat, end_address_long, duration,
					distance, cost, description, leave_date);

		}
	}

	private void addItinerary(final String start_address,
			final Double start_address_lat, final Double start_address_long,
			final String end_address, final Double end_address_lat,
			final Double end_address_long, final String duration,
			final String distance, final String cost, final String description,
			final String leave_date) {
		// Tag used to cancel the request
		String tag_string_req = "req_register_itinerary";
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
							JSONObject jObj = new JSONObject(
									response.substring(response.indexOf("{"),
											response.lastIndexOf("}") + 1));
							boolean error = jObj.getBoolean("error");
							// Check for error node in json
							if (!error) {
								Toast.makeText(context,
										jObj.getString("message"),
										Toast.LENGTH_LONG).show();
								// Launch main activity
								Intent intent = new Intent(
										getApplicationContext(),
										MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								startActivity(intent);
								setResult(RESULT_OK, null);
								finish();
								

							} else {
								// Error in register itinerary. Get the error
								// message
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
						Log.e(TAG,
								"Register Itinerary Error: "
										+ error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_advance, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
