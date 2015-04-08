package com.halley.manageitinerary;

import java.util.List;

import com.halley.registerandlogin.R;
import com.halley.listitinerary.data.ItineraryItem;
import com.halley.manageitinerary.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListManageItineraryAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	public List<ItineraryItem> itineraryItemManage;
	public String statusAdapter;
	private ManageItineraryActivity manage;
	TextView description, start_address, end_address, duration, cost,
			leave_date;

	public ListManageItineraryAdapter(Activity activity,
			List<ItineraryItem> itineraryItemManage) {
		this.activity = activity;
		this.itineraryItemManage = itineraryItemManage;

	}

	@Override
	public int getCount() {
		return itineraryItemManage.size();
	}

	@Override
	public Object getItem(int location) {
		return itineraryItemManage.get(location);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null) {		
			convertView = inflater.inflate(R.layout.list_manage_itinerary,
					null);
			
			ItineraryItem m = itineraryItemManage.get(position);
			if(m.getStatus().equals("1")){
				convertView.setBackgroundDrawable(new ColorDrawable(Color.BLUE));
			}
			else{
				convertView.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
			}
			convertView = inflater.inflate(R.layout.list_manage_itinerary,
					null);			
			description = (TextView) convertView.findViewById(R.id.description);
			start_address = (TextView) convertView
					.findViewById(R.id.start_address);
			end_address = (TextView) convertView.findViewById(R.id.end_address);
			duration = (TextView) convertView.findViewById(R.id.duration);
			cost = (TextView) convertView.findViewById(R.id.cost);
			leave_date = (TextView) convertView.findViewById(R.id.leave_date);
			description.setText(m.getDescription());
			start_address.setText("Nơi đi: " + m.getStart_address());
			end_address.setText("Nơi đến: " + m.getEnd_address());
			duration.setText("Thời gian dự tính: " + m.getDuration());
			cost.setText("Giá tiền: " + m.getCost() + " VND");
			leave_date.setText("Ngày đi: " + m.getLeave_date());

		}
		return convertView;

	}
}
