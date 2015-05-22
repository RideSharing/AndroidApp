package com.halley.itinerary.list.item;

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
	private String status;
	private String itinerary_id;
	private String start_address_lat, start_address_long, end_address_lat,
			end_address_long;
    private String customer_id;
    private String vehicle_id, vehicle_type;
    private String driver_id;

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
