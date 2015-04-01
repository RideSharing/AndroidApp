package com.halley.manageitinerary;

import java.util.List;

import com.halley.registerandlogin.R;
import com.halley.manageitinerary.ListManageItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListManageItineraryAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<ListManageItem> itineraryItemManage;

	public ListManageItineraryAdapter(Activity activity,
			List<ListManageItem> itineraryItemManage) {
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
		if (convertView == null)
            convertView = inflater.inflate(R.layout.list_manage_itinerary, null);
		TextView description = (TextView) convertView.findViewById(R.id.description);
		TextView start_address = (TextView) convertView.findViewById(R.id.start_address);
		TextView end_address = (TextView) convertView.findViewById(R.id.end_address);
		TextView duration = (TextView) convertView.findViewById(R.id.duration);
		TextView cost = (TextView) convertView.findViewById(R.id.coast);
		TextView leave_date = (TextView) convertView.findViewById(R.id.leave_date);
		
		ListManageItem m = itineraryItemManage.get(position);
		
		description.setText(m.getDescription());
		start_address.setText("Nơi đi: " + m.getStart_address());
		end_address.setText("Nơi đến: " + m.getEnd_address());
		duration.setText("Thời gian dự tính: " + m.getDuration());
		cost.setText("Giá tiền: " + m.getCost());
		leave_date.setText("Ngày đi: " + m.getLeave_date());
		return convertView;
	}
}
