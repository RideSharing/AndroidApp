package com.halley.itinerary.list.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import com.halley.app.AppController;
import com.halley.helper.CustomNetworkImageView;
import com.halley.itinerary.list.item.ItineraryItem;
import com.halley.registerandlogin.R;

public class ItineraryListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<ItineraryItem> itineraryItems;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	public ItineraryListAdapter(Activity activity,
			List<ItineraryItem> itineraryItems) {
		this.activity = activity;
		this.itineraryItems = itineraryItems;
	}

	@Override
	public int getCount() {
		return itineraryItems.size();
	}

	@Override
	public Object getItem(int location) {
		return itineraryItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.itinerary_item, null);

		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();
		CustomNetworkImageView avatar = (CustomNetworkImageView) convertView
				.findViewById(R.id.avatar);
		TextView description = (TextView) convertView
				.findViewById(R.id.description);
		TextView cost = (TextView) convertView.findViewById(R.id.cost);
		TextView start_address = (TextView) convertView
				.findViewById(R.id.start_address);
		TextView end_address = (TextView) convertView
				.findViewById(R.id.end_address);
		TextView leave_date = (TextView) convertView
				.findViewById(R.id.leave_date);
		RatingBar rating= (RatingBar)convertView.findViewById(R.id.ratingBar1);
		rating.setEnabled(false);
		rating.setIsIndicator(true);
		// getting itinerary data for the row
		ItineraryItem m = itineraryItems.get(position);

		// thumbnail image
		byte[] decodeString = Base64.decode(m.getAvatarlUrl(), Base64.DEFAULT);
		Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
				decodeString.length);
		
		avatar.setLocalImageBitmap(decodeByte);

		

		// avatar.setImageUrl(m.getAvatarlUrl(), imageLoader);

		// title
		description.setText(m.getDescription());
		Double rt=m.getRating();
		// rating
		rating.setRating(rt.floatValue());
		cost.setText(activity.getResources().getString(R.string.cost)+ ": " + m.getCost());
		// start_address
		start_address.setText(activity.getResources().getString(R.string.start_addess) + ": " + m.getStart_address());
		// end_address
		end_address.setText(activity.getResources().getString(R.string.end_addess) + ": " +  m.getEnd_address());
		// // genre
		// String genreStr = "";
		// for (String str : m.getGenre()) {
		// genreStr += str + ", ";
		// }
		// genreStr = genreStr.length() > 0 ? genreStr.substring(0,
		// genreStr.length() - 2) : genreStr;
		// genre.setText(genreStr);

		// release year
		leave_date.setText(activity.getResources().getString(R.string.leave_date)+ ": "  + m.getLeave_date());

		return convertView;
	}

}