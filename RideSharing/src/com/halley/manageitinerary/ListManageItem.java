package com.halley.manageitinerary;

public class ListManageItem {
	private String description, phone, itinerary_id, start_address,
			end_address, duration, cost, leave_date, fullname, distance,
			start_address_lat, start_address_long, end_address_lat,
			end_address_long;

	public ListManageItem() {
	}

	public ListManageItem(String description, String start_address,
			String end_address, String duration, String cost,
			String leave_date, String fullname, String distance,
			String itinerary_id, String phone) {
		this.description = description;
		this.start_address = start_address;
		this.end_address = end_address;
		this.duration = duration;
		this.cost = cost;
		this.leave_date = leave_date;
		this.fullname = fullname;
		this.distance = distance;
		this.phone = phone;
		this.itinerary_id = itinerary_id;
	}

	public String getStart_address_lat() {
		return start_address_lat;
	}

	public void setStart_address_lat(String start_address_lat) {
		this.start_address_lat = start_address_lat;
	}

	public String getStart_address_long() {
		return start_address_long;
	}

	public void setStart_address_long(String start_address_long) {
		this.start_address_long = start_address_long;
	}

	public String getEnd_address_lat() {
		return end_address_lat;
	}

	public void setEnd_address_lat(String end_address_lat) {
		this.end_address_lat = end_address_lat;
	}

	public String getEnd_address_long() {
		return end_address_long;
	}

	public void setEnd_address_long(String end_address_long) {
		this.end_address_long = end_address_long;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getItinerary_id() {
		return itinerary_id;
	}

	public void setItinerary_id(String itinerary_id) {
		this.itinerary_id = itinerary_id;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStart_address() {
		return start_address;
	}

	public void setStart_address(String start_address) {
		this.start_address = start_address;
	}

	public String getEnd_address() {
		return end_address;
	}

	public void setEnd_address(String end_address) {
		this.end_address = end_address;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getLeave_date() {
		return leave_date;
	}

	public void setLeave_date(String leave_date) {
		this.leave_date = leave_date;
	}

}
