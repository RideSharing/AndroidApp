package com.halley.searchitinerary;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.halley.custom_theme.CustomActionBar;
import com.halley.dialog.SearchDialogFragment;
import com.halley.helper.NumberTextWatcher;
import com.halley.registerandlogin.R;
import com.halley.registerandlogin.R.id;
import com.halley.registerandlogin.R.layout;
import com.halley.registerandlogin.R.menu;
import com.halley.ridesharing.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchAdvanceActivity extends ActionBarActivity implements
        GoogleMap.OnMarkerDragListener, SearchDialogFragment.OnDataPass {

    private final int REQUEST_EXIT = 1;
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private double fromLatitude, fromLongitude, toLatitude, toLongitude;
    private TextView etStartAddress;
    private TextView etEndAddress;
    private TextView etLeave_date;
    private EditText etCost;
    private Marker marker_start_address;
    private Marker marker_end_address;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
    private ActionBar actionBar;
    // check onclick is From or to
    private boolean isFrom;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_advance);

        // Progress dialog
        pDialog = new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        pDialog.setCancelable(false);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();

        etStartAddress = (TextView) findViewById(R.id.txtStartAddress);
        etEndAddress = (TextView) findViewById(R.id.txtEndAddress);
        etLeave_date = (TextView) findViewById(R.id.etLeave_date);
        etLeave_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
        etCost=(EditText)findViewById(id.etCost);
        etCost.addTextChangedListener(new NumberTextWatcher(etCost));
        if (this.getIntent().getExtras() != null) {
            fromLatitude = this.getIntent().getExtras()
                    .getDouble("fromLatitude");
            fromLongitude = this.getIntent().getExtras()
                    .getDouble("fromLongitude");
        }
        initilizeMap();
        // Add current location on Maps
        marker_start_address = addMarkeronMaps(fromLatitude, fromLongitude,
                getResources().getString(R.string.start_addess),
                "marker_start_address", R.drawable.ic_marker_start);
        marker_end_address = addMarkeronMaps(fromLatitude + 0.002,
                fromLongitude + 0.002,
                getResources().getString(R.string.end_addess),
                "marker_end_address", R.drawable.ic_marker_end);
        focusMap(marker_start_address, marker_end_address);
        etStartAddress.setText(getDetailLocation(marker_start_address));
        etEndAddress.setText(getDetailLocation(marker_end_address));
	}

    private void showDateTimePicker() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_datetime_picker);
        dialog.setTitle(R.string.leave_date);
        final DatePicker datepicker = (DatePicker) dialog
                .findViewById(R.id.datePicker1);
        final TimePicker timepicker = (TimePicker) dialog
                .findViewById(R.id.timePicker1);
        final Button btnOK = (Button) dialog.findViewById(R.id.OK);

        btnOK.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(getApplicationContext(),
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

    public void showDialogonClick(View v) {
        switch (v.getId()) {
            case R.id.txtStartAddress:

                isFrom = true;
                break;
            case R.id.txtEndAddress:

                isFrom = false;
                break;
        }
        /** Instantiating TimeDailogFragment, which is a DialogFragment object */
        SearchDialogFragment dialog = new SearchDialogFragment();

        /** Getting FragmentManager object */
        FragmentManager fragmentManager = getFragmentManager();

        /** Starting a FragmentTransaction */
        dialog.show(fragmentManager, "search_location");

    }
    public void searchOnclick(View v){

        Handler handler = new Handler();
        final Intent i = new Intent();
        Bundle search_advance=new Bundle();
        LatLng postion_start=marker_start_address.getPosition();
        LatLng postion_end=marker_end_address.getPosition();
        search_advance.putDouble("fromLatitude",postion_start.latitude);
        search_advance.putDouble("fromLongitude",postion_start.longitude);
        search_advance.putString("fromLocation",etStartAddress.toString());
        search_advance.putDouble("toLatitude",postion_end.latitude);
        search_advance.putDouble("toLongitude",postion_end.longitude);
        search_advance.putString("toLocation",etEndAddress.toString());
        String cost = etCost.getText().toString().replace(",", "");
        search_advance.putString("cost",cost);
        String leave_date=etLeave_date.getText().toString().replace(" ","+");
        search_advance.putString("leave_date",leave_date);
        i.putExtra("search_advance",search_advance);
        setResult(RESULT_OK, i);
        finish();

    }

    private void focusMap(Marker marker_a, Marker marker_b) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marker_a.getPosition());
        builder.include(marker_b.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300, 300,
                padding);
        googleMap.moveCamera(cu);
        googleMap.animateCamera(cu);
    }

    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) this.getFragmentManager()
                    .findFragmentById(R.id.mapSearch)).getMap();
            // googleMap.setMyLocationEnabled(true);

            // Enable / Disable Compass icon
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            // Enable / Disable Rotate gesture
            googleMap.getUiSettings().setRotateGesturesEnabled(true);

            // Enable / Disable zooming functionality
            googleMap.getUiSettings().setZoomGesturesEnabled(true);

            googleMap.setOnMarkerDragListener(this);

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(this, R.string.no_load_map,
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Marker addMarkeronMaps(double lat, double longi, String title,
                                   String snippet, int icon) {
        return googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, longi)).title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(icon))
                .draggable(true));
    }

    @Override
    public void onDataPass(String address, double latitude, double longtitude) {
        if (isFrom) {
            etStartAddress.setText(address);
            if (getLocationfromName(etStartAddress.getText().toString()) != null) {
                // remove marker from google map
                marker_start_address.remove();
                // add marker with new lat and long.
                marker_start_address = addMarkeronMaps(
                        getLocationfromName(
                                etStartAddress.getText().toString())
                                .getLatitude(),
                        getLocationfromName(
                                etStartAddress.getText().toString())
                                .getLongitude(),
                        getResources()
                                .getString(R.string.start_addess),
                        "marker_start_address", R.drawable.ic_marker_start);
                // show on googlemap
                onMarkerDragEnd(marker_start_address);
            } else {
                Toast.makeText(this, R.string.not_found_area,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            etEndAddress.setText(address);
            if (getLocationfromName(etEndAddress.getText().toString()) != null) {
                // remove marker from google map
                marker_end_address.remove();
                // add marker with new lat and long.
                marker_end_address = addMarkeronMaps(
                        getLocationfromName(
                                etEndAddress.getText().toString())
                                .getLatitude(),
                        getLocationfromName(
                                etEndAddress.getText().toString())
                                .getLongitude(),
                        getResources().getString(R.string.end_addess),
                        "marker_end_address", R.drawable.ic_marker_end);
                // show on googlemap
                onMarkerDragEnd(marker_end_address);
            } else {
                Toast.makeText(this, R.string.not_found_area,
                        Toast.LENGTH_SHORT).show();
            }
            focusMap(marker_start_address, marker_end_address);
        }

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // Get location from Marker when user had choosen
        String address = getDetailLocation(marker);
        if (marker.getSnippet().equals("marker_start_address")) {
            etStartAddress.setText(address);
        } else {
            etEndAddress.setText(address);
        }
    }
    public Address getLocation(LatLng location) {
        Address add = null;
        List<Address> list_address = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            list_address = geocoder.getFromLocation(location.latitude,
                    location.longitude, 1);
            if (!list_address.isEmpty()) {
                add = list_address.get(0);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return add;

    }

    public Address getLocationfromName(String location) {
        Address add = null;
        List<android.location.Address> list_address = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            list_address = geocoder.getFromLocationName(location, 1);
            if (!list_address.isEmpty()) {
                add = list_address.get(0);
            }
            // Toast.makeText(this, "submit " + locality,
            // Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return add;
    }

    public String getDetailLocation(Marker marker) {
        String address = "";
        Address position = null;
        position = getLocation(marker.getPosition());
        if (position != null) {
            for (int i = 0; i < 4; i++) {

                if (position.getAddressLine(i) != null) {
                    address += position.getAddressLine(i) + " ";
                }

            }
        }
        return address;
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
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
