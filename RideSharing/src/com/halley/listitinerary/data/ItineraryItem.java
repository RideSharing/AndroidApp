package com.halley.listitinerary.data;

import com.halley.helper.CustomNetworkImageView;

public class ItineraryItem {
	private String description, AvatarlUrl;
	private String leave_date;
	private double rating;
	private String start_address;
	private String end_address;
	private String cost;

	public ItineraryItem() {
	}

	public ItineraryItem(String description, String AvatarlUrl,
			String leave_date, double rating, String start_address,
			String end_address, String cost) {
		this.description = description;
		this.AvatarlUrl = AvatarlUrl;
		this.leave_date = leave_date;
		this.rating = rating;
		this.start_address = start_address;
		this.end_address = end_address;
		this.cost = cost;
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
