package com.halley.app;

import android.content.Intent;

import com.halley.manageitinerary.ManageItineraryActivity;

public class AppConfig {
	// Server user login url
	// 54.191.137.154
	public static String URL_LOGIN = "http://54.68.126.75/RESTFul/v1/user/login";

	// Server user register url
	public static String URL_REGISTER = "http://54.68.126.75/RESTFul/v1/user";

	// Server driver get all itinerary url
	public static String URL_GET_ALL_ITINERARY = "http://54.68.126.75/RESTFul/v1/itineraries";

	// Server driver register itinerary url
	public static String URL_REGISTER_ITINERARY = "http://54.68.126.75/RESTFul/v1/itinerary";

	// Server user submit itinerary url
	public static String URL_SUBMIT_ITINERARY = "http://54.68.126.75/RESTFul/v1/customer_accept_itinerary";

	public static String URL_FEEDBACK = "http://54.68.126.75/RESTFul/v1/feedback";
	// Server get driver id

	public static String URL_GET_DRIVER = "http://54.68.126.75/RESTFul/v1/driver/user_id";

	public static String URL_GET_USER = "http://54.68.126.75/RESTFul/v1/user";

	public static String URL_PASSWORD = "http://54.68.126.75/RESTFul/v1/user/password";

	public static String URL_AVATAR = "http://54.68.126.75/RESTFul/v1/user/link_avatar";

	public static String URL_FULLNAME = "http://54.68.126.75/RESTFul/v1/user/fullname";

	public static String URL_PHONE = "http://54.68.126.75/RESTFul/v1/user/phone";

	public static String URL_PERSONALID = "http://54.68.126.75/RESTFul/v1/user/personalID";

	public static String URL_PERSONALID_IMG = "http://54.68.126.75/RESTFul/v1/user/personalID_img";

	public static String URL_DRIVER = "http://54.68.126.75/RESTFul/v1/driver";

	public static String URL_DRIVER_LICENSE = "http://54.68.126.75/RESTFul/v1/driver/driver_license";

	public static String URL_DRIVER_LICENSE_IMG = "http://54.68.126.75/RESTFul/v1/driver/driver_license_img";

	// Show list itinerary for user
	public static String URL_LIST_ITINERARY = "http://54.68.126.75/RESTFul/v1/itineraries/driver/itinerary_status";
}
