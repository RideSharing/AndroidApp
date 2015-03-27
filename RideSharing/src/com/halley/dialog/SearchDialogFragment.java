package com.halley.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.halley.helper.PlaceJSONParser;
import com.halley.registerandlogin.R;

public class SearchDialogFragment extends DialogFragment {
	private AutoCompleteTextView atvPlaces;
	private static final String API_KEY = "AIzaSyB0vjZDNMCFnBdP1tFXrD346RjEMG46F9Q";
	PlacesTask placesTask;
	ParserTask parserTask;
	Button btnSearch;
	String search;
	double latitude;
	double longitude;

	OnDataPass dataPasser;

	public interface OnDataPass {
		public void onDataPass(String address, double latitude,
				double longtitude);
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		dataPasser = (OnDataPass) a;
	}

	public void passData(String address, double latitude, double longitude) {
		dataPasser.onDataPass(address, latitude, longitude);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** Inflating layout for the dialog */
		View v = inflater.inflate(R.layout.dialog_search_location, null);
		btnSearch = (Button) v.findViewById(R.id.btnSearch);
		atvPlaces = (AutoCompleteTextView) v.findViewById(R.id.atv_places);
		atvPlaces.setThreshold(1);

		atvPlaces.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (placesTask != null) {
					if (placesTask.getStatus() == AsyncTask.Status.PENDING
							|| placesTask.getStatus() == AsyncTask.Status.RUNNING
							|| placesTask.getStatus() == AsyncTask.Status.FINISHED) {
						Log.i("--placesDownloadTask--", "progress_status : "
								+ placesTask.getStatus());
						placesTask.cancel(true);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				placesTask = new PlacesTask();
				placesTask.execute(s.toString());

			}
		});

		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// String search = atvPlaces.getText().toString();
				// Toast.makeText(getActivity(), search,
				// Toast.LENGTH_LONG).show();
				try {
					final Geocoder geocoder = new Geocoder(getActivity(),
							Locale.getDefault());

					List<android.location.Address> list_address = null;
					search = atvPlaces.getText().toString();
					if (search.trim().length() > 0) {
						list_address = geocoder.getFromLocationName(
								search.trim(), 1);
						if (list_address.isEmpty()) {
							Toast.makeText(getActivity(),
									"Không tìm được địa điểm",
									Toast.LENGTH_LONG).show();

						} else {

							Address address = list_address.get(0);
							latitude = address.getLatitude();
							longitude = address.getLongitude();
							passData(getDetailLocation(address), latitude, longitude);
							dismiss();

							// Toast.makeText(dialog.getContext(),address.toString()

						}
					} else {
						Toast.makeText(getActivity(),
								"Bạn chưa nhập nơi cần đến", Toast.LENGTH_LONG)
								.show();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		return v;
	}
	
	public String getDetailLocation(Address position) {
		String address = "";
		if (position != null) {
			for (int i = 0; i < 4; i++) {
				if (position.getAddressLine(i) != null) {
					address += position.getAddressLine(i) + " ";
				}

			}
		}
		return address;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches all places from GooglePlaces AutoComplete Web Service
	private class PlacesTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... place) {
			// For storing data from web service
			String data = "";

			// Obtain browser key from https://code.google.com/apis/console
			String key = "key=" + API_KEY;

			String input = "";

			try {
				input = "input=" + URLEncoder.encode(place[0], "utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			// place type to be searched
			String types = "types=geocode";

			// Sensor enabled
			String sensor = "sensor=false";

			// Building the parameters to the web service
			String parameters = input + "&" + types + "&" + sensor + "&" + key;

			// Output format
			String output = "json";

			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
					+ output + "?" + parameters;

			try {
				// Fetching the data from we service
				data = downloadUrl(url);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			// Creating ParserTask
			parserTask = new ParserTask();

			// Starting Parsing the JSON string returned by Web Service
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;

			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				// Getting the parsed data as a List construct
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {

			String[] from = new String[] { "description" };
			int[] to = new int[] { android.R.id.text1 };

			// Creating a SimpleAdapter for the AutoCompleteTextView
			SimpleAdapter adapter = new SimpleAdapter(getActivity(), result,
					android.R.layout.simple_list_item_1, from, to);

			// Setting the adapter
			atvPlaces.setAdapter(adapter);
		}
	}

}
