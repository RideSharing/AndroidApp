package com.halley.listitinerary.data;

import com.halley.helper.CustomNetworkImageView;

public class ItineraryItem {
	private String description, AvatarlUrl;
	private String leave_date;
	private double rating;
	private String start_address;
	private String end_address;
	private String cost;
	private String fullname;
	private String duration;
	private String distance;
	private String phone;
	private String itinerary_id;
	public ItineraryItem() {
	}

	
	public String getItinerary_id() {
		return itinerary_id;
	}


	public void setItinerary_id(String itinerary_id) {
		this.itinerary_id = itinerary_id;
	}


	public String getPhone() {
		return phone;
	}



	public void setPhone(String phone) {
		this.phone = phone;
	}



	public String getDuration() {
		return duration;
	}



	public void setDuration(String duration) {
		this.duration = duration;
	}



	public String getDistance() {
		return distance;
	}



	public void setDistance(String distance) {
		this.distance = distance;
	}



	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAvatarlUrl() {
		return AvatarlUrl;
	}

	public void setAvatarlUrl(String avatarlUrl) {
		AvatarlUrl = avatarlUrl;
	}

	public String getLeave_date() {
		return leave_date;
	}
	

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public void setLeave_date(String leave_date) {
		this.leave_date = leave_date;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
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

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

}
