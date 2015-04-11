package com.halley.manageitinerary;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.halley.listitinerary.data.ItineraryItem;
import com.halley.registerandlogin.R;

public class ListManageItineraryAdapter extends BaseExpandableListAdapter {
	private Context context;
	private LayoutInflater inflater;
	public List<ItineraryItem> itineraryItemManage;
	private List<String> listDataHeader; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<ItineraryItem>> listDataChild;

	TextView description, start_address, end_address, duration, cost,
			leave_date;

	public ListManageItineraryAdapter(Context context,
			List<String> listDataHeader,
			HashMap<String, List<ItineraryItem>> listChildData) {
		this.context = context;
		this.listDataHeader = listDataHeader;
		this.listDataChild = listChildData;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listDataChild.get(this.listDataHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final ItineraryItem m = (ItineraryItem) getChild(
				groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_manage_itinerary,
					null);
		}

		description = (TextView) convertView.findViewById(R.id.description);
		start_address = (TextView) convertView.findViewById(R.id.start_address);
		end_address = (TextView) convertView.findViewById(R.id.end_address);
		duration = (TextView) convertView.findViewById(R.id.duration);
		cost = (TextView) convertView.findViewById(R.id.cost);
		leave_date = (TextView) convertView.findViewById(R.id.leave_date);
		description.setText(m.getDescription());
		start_address.setText("Nơi đi: " + m.getStart_address());
		end_address.setText("Nơi đến: " + m.getEnd_address());
		duration.setText("Thời gian dự tính: " + transferDuration(m.getDuration()));
		cost.setText("Giá tiền: " + transferCost(m.getCost()));
		leave_date.setText("Ngày đi: " + m.getLeave_date());
		return convertView;
	}
	
	public String transferDuration(String timeString) {
		int time = Integer.parseInt(timeString);
		int hour = time / 60;
		int min = time % 60;
		return hour + " giờ " + min + " phút";
	}

	public String transferCost(String cost) {
		DecimalFormat formatter = new DecimalFormat("#,###,###");
		return formatter.format(Double.parseDouble(cost)) + " VNĐ";

	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.listDataChild.get(this.listDataHeader.get(groupPosition))
				.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_group_itinerary,
					null);
		}

		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.lblListHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
