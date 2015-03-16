package com.halley.map.GPSLocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GPSLocation extends Service implements LocationListener {

	protected LocationListener locationListener;

	// Declaring a Location Manager

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	private Activity context;
	private Location currentLocation;
	private LocationManager locationManager;


	public GPSLocation(Activity context) {
		super();
		this.context = context;
		// First we need to check availability of play services

		// Toast.makeText(this, "Lat "+GPSLocation.getLatitude()+ " Long "+
		// GPSLocation.getLongitude(), Toast.LENGTH_LONG).show();
		locationManager = (LocationManager) this.context
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Do what you need if enabled...
			// Register the listener with the Location Manager to receive location
			// updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, this);
			// Define a listener that responds to location updates
			// Location location =
			// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			// if (location != null) {
			// Toast.makeText(this.context,
			// "Last Location is found..",
			// Toast.LENGTH_LONG).show();
			// } else {
			//
			// Toast.makeText(this.context,
			// "Last Location is not found..",
			// Toast.LENGTH_LONG).show();
			// }
		} else {
			showSettingsGPSAlert();
		}
		

	}

	public void showSettingsGPSAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("Thiết lập GPS");

		// Setting Dialog Message
		alertDialog
				.setMessage("Ứng dụng yêu cầu phải luôn bật GPS khi sử dụng. Hiện tại GPS chưa được bật. Bạn có muốn vào chế độ thiết lập GPS?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Thiết lập",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
					}
				});

		

		// Showing Alert Message
		alertDialog.show();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this.context, "Gps đã bật", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this.context, "Gps đã tắt, vui lòng luôn bật GPS trong khi sử dụng ứng dụng", Toast.LENGTH_LONG).show();

	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

}
