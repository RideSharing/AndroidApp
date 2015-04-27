package com.halley.app;

import android.content.Intent;

import com.halley.helper.SessionManager;
import com.halley.manageitinerary.ManageItineraryActivity;

import java.util.Locale;

public class AppConfig {
	// Server user login url
	public static String lang=Locale.getDefault().getLanguage();
	public static String URL_LOGIN = "http://192.168.10.132/RESTFul/v1/user/login?lang="+lang;

	// Server user register url
	public static String URL_REGISTER = "http://192.168.10.132/RESTFul/v1/user?lang="+lang;

	// Server driver get all itinerary url
	public static String URL_GET_ALL_ITINERARY = "http://192.168.10.132/RESTFul/v1/itineraries?lang="+lang;

	// Server driver register itinerary url
	public static String URL_REGISTER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itinerary";

	// Server user submit itinerary url
	public static String URL_SUBMIT_ITINERARY = "http://192.168.10.132/RESTFul/v1/customer_accept_itinerary?lang="+lang;

	public static String URL_FEEDBACK = "http://192.168.10.132/RESTFul/v1/feedback?lang="+lang;
	// Server get driver id

	public static String URL_GET_DRIVER = "http://192.168.10.132/RESTFul/v1/driver/user_id?lang="+lang;

	public static String URL_GET_USER = "http://192.168.10.132/RESTFul/v1/user?lang="+lang;

	public static String URL_PASSWORD = "http://192.168.10.132/RESTFul/v1/user/password?lang="+lang;

	public static String URL_AVATAR = "http://192.168.10.132/RESTFul/v1/user/link_avatar?lang="+lang;

	public static String URL_PERSONALID_IMG = "http://192.168.10.132/RESTFul/v1/user/personalID_img?lang="+lang;

	public static String URL_DRIVER = "http://192.168.10.132/RESTFul/v1/driver?lang="+lang;


	// Show list itinerary for user
	public static String URL_DRIVER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itineraries/driver/itinerary_status?lang="+lang;
	
	// Show list itinerary for customer
		public static String URL_CUSTOMER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itineraries/customer/itinerary_status?lang="+lang;
}
