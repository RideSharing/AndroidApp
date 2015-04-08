package com.halley.manageitinerary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.SessionManager;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;

public class ManageItineraryActivity extends ActionBarActivity {
	private static final String TAG = ManageItineraryActivity.class
			.getSimpleName();
	ListManageItineraryAdapter listAdapter;
	List<String> listDataHeader;
	HashMap<String, List<ItineraryItem>> listDataChild;
	private ProgressDialog pDialog;
	private List<ItineraryItem> itineraryList_completed;
	private List<ItineraryItem> itineraryList_waiting_user;
	private List<ItineraryItem> itineraryList_submit;
	private List<ItineraryItem> itineraryList_ready;
	ExpandableListView listView;
	SessionManager session;
	private Activity activity = this;
	private ActionBar actionBar;
	String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_itinerary);
		customActionBar();
		session = new SessionManager(getApplicationContext());
		pDialog = new ProgressDialog(this);
		listView = (ExpandableListView) findViewById(R.id.list);
		getItinerary();
		listAdapter = new ListManageItineraryAdapter(this, listDataHeader,
				listDataChild);

		// setting list adapter
		listView.setAdapter(listAdapter);

		// Showing progress dialog before making http request
		pDialog.setMessage("Loading...");
		pDialog.show();
		listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				ItineraryItem m = listDataChild.get(
						listDataHeader.get(groupPosition)).get(childPosition);
				Intent i = new Intent(activity,
						DetailManageItineraryActivity.class);
				Bundle bundle = new Bundle();

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
				bundle.putDouble("start_address_lat",
						Double.parseDouble(m.getStart_address_lat()));
				bundle.putDouble("start_address_long",
						Double.parseDouble(m.getStart_address_long()));
				bundle.putDouble("end_address_lat",
						Double.parseDouble(m.getEnd_address_lat()));
				bundle.putDouble("end_address_long",
						Double.parseDouble(m.getEnd_address_long()));
				i.putExtra("bundle", bundle);
				startActivity(i);
				return false;
			}
		});

	}

	private void getItinerary() {
		// Tag used to cancel the request
		String tag_string_req = "req_get_driver_by_list";
		itineraryList_completed = new ArrayList<ItineraryItem>();
		itineraryList_waiting_user = new ArrayList<ItineraryItem>();
		itineraryList_ready = new ArrayList<ItineraryItem>();
		itineraryList_submit = new ArrayList<ItineraryItem>();
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<ItineraryItem>>();
		// Adding child data
		listDataHeader.add("Đang đợi người đi cùng...");
		listDataHeader.add("Đang đợi xác nhận...");
		listDataHeader.add("Chuẩn bị đi..");
		listDataHeader.add("Đã đi");
		StringRequest strReq = new StringRequest(Method.GET,
				AppConfig.URL_LIST_ITINERARY, new Response.Listener<String>() {

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
									ItineraryItem itineraryItem = new ItineraryItem();
									JSONObject itinerary = itineraries
											.getJSONObject(i);
									itineraryItem.setDescription(itinerary
											.getString("description"));
									itineraryItem.setStart_address(itinerary
											.getString("start_address"));
									itineraryItem.setEnd_address(itinerary
											.getString("end_address"));
									itineraryItem.setStart_address_lat(itinerary
											.getString("start_address_lat"));
									itineraryItem.setStart_address_long(itinerary
											.getString("start_address_long"));
									itineraryItem.setEnd_address_lat(itinerary
											.getString("end_address_lat"));
									itineraryItem.setEnd_address_long(itinerary
											.getString("end_address_long"));
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
									itineraryItem.setStatus(itinerary
											.getString("status"));
									// status = itinerary.getString("status");
									itineraryItem.setPhone(itinerary
											.getString("phone"));
									itineraryItem.setItinerary_id(itinerary
											.getString("itinerary_id"));
									if (itineraryItem.getStatus().equals("1")) {
										itineraryList_waiting_user
												.add(itineraryItem);
									} else if (itineraryItem.getStatus()
											.equals("2")) {
										itineraryList_submit.add(itineraryItem);
									} else if (itineraryItem.getStatus()
											.equals("3")) {
										itineraryList_ready.add(itineraryItem);
									} else {
										itineraryList_completed
												.add(itineraryItem);
									}

								}
								listDataChild.put(listDataHeader.get(0),
										itineraryList_waiting_user); // Header,
																		// Child
																		// data
								listDataChild.put(listDataHeader.get(1),
										itineraryList_submit);
								listDataChild.put(listDataHeader.get(2),
										itineraryList_ready);
								listDataChild.put(listDataHeader.get(3),
										itineraryList_completed);

							} else {
								// Error in login. Get the error message
								String message = jObj.getString("message");
								Toast.makeText(activity, message,
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

						Toast.makeText(activity,
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

	public void customActionBar() {
		actionBar = getSupportActionBar();
		actionBar.setElevation(0);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
				.getColor(R.color.bg_login)));
		// Enabling Up / Back navigation
		actionBar.setDisplayHomeAsUpEnabled(true);

		LayoutInflater mInflater = LayoutInflater.from(this);

		View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
		TextView mTitleTextView = (TextView) mCustomView
				.findViewById(R.id.title_text);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ANGEL.otf");
		mTitleTextView.setTypeface(tf);

		ImageButton imageButton = (ImageButton) mCustomView
				.findViewById(R.id.imageButton);
		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Dialog dialog = new Dialog(activity);
				dialog.setContentView(R.layout.dialog_info);
				dialog.setTitle("Thông tin về ứng dụng");
				dialog.show();
			}
		});

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
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
