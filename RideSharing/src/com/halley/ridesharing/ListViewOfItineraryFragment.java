package com.halley.ridesharing;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.halley.app.AppController;
import com.halley.listitinerary.adapter.ItineraryListAdapter;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;

public class ListViewOfItineraryFragment extends Fragment {
	private static final String TAG = MainActivity.class.getSimpleName();
	private ListView listView;
	private ItineraryListAdapter listAdapter;
	private List<ItineraryItem> itineraryItems;
	private static final String url = "http://api.androidhive.info/json/movies.json";
	private ProgressDialog pDialog;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_list_view_of_itinerary, container, false);
        listView = (ListView) rootView.findViewById(R.id.list);
        
        showListView();
        Bundle bundle= this.getArguments();
        Toast.makeText(this.getActivity(), bundle.getString("location"), Toast.LENGTH_LONG).show();
        return rootView;
    }
	
	public void showListView() {
		itineraryItems = new ArrayList<ItineraryItem>();

		listAdapter = new ItineraryListAdapter(this.getActivity(), itineraryItems);
		listView.setAdapter(listAdapter);

		pDialog = new ProgressDialog(this.getActivity());
		// Showing progress dialog before making http request
		pDialog.setMessage("Loading...");
		pDialog.show();

		// Creating volley request obj
		JsonArrayRequest ItineraryReq = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.d(TAG, response.toString());
						hidePDialog();

						if(response.toString()==null){
							
						}
						else{
							// Parsing json
							for (int i = 0; i < response.length(); i++) {
								try {

									JSONObject obj = response.getJSONObject(i);
									ItineraryItem itineraryItem = new ItineraryItem();
									itineraryItem.setTitle(obj.getString("title"));
									itineraryItem.setThumbnailUrl(obj
											.getString("image"));
									itineraryItem.setRating(((Number) obj
											.get("rating")).doubleValue());
									itineraryItem.setYear(obj.getInt("releaseYear"));

									// Genre is json array
									JSONArray genreArry = obj.getJSONArray("genre");
									ArrayList<String> genre = new ArrayList<String>();
									for (int j = 0; j < genreArry.length(); j++) {
										genre.add((String) genreArry.get(j));
									}
									itineraryItem.setGenre(genre);

									// adding movie to movies array
									itineraryItems.add(itineraryItem);

								} catch (JSONException e) {
									e.printStackTrace();
								}

							}
						}

						// notifying list adapter about data changes
						// so that it renders the list view with updated data
						listAdapter.notifyDataSetChanged();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d(TAG, "Error: " + error.getMessage());
						hidePDialog();

					}
				});

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(ItineraryReq);

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
