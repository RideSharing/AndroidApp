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

    // Server driver get itinerary by id url
    public static String URL_GET_ITINERARY = "http://192.168.10.132/RESTFul/v1/itinerary";

	// Server driver register itinerary url
	public static String URL_REGISTER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itinerary";

	// Server user submit itinerary url
	public static String URL_SUBMIT_ITINERARY = "http://192.168.10.132/RESTFul/v1/customer_accept_itinerary";

    //Driver accept itinerary
    public static String URL_ACCEPT_ITINERARY = "http://192.168.10.132/RESTFul/v1/driver_accept_itinerary";

    // Server customer reject itinerary url
    public static String URL_CUSTOMER_REJECT_ITINERARY = "http://192.168.10.132/RESTFul/v1/customer_reject_itinerary";

    // Server driver reject itinerary url
    public static String URL_DRIVER_REJECT_ITINERARY = "http://192.168.10.132/RESTFul/v1/driver_reject_itinerary";

    public static String URL_FEEDBACK = "http://192.168.10.132/RESTFul/v1/feedback?lang="+lang;

    public static String URL_RATING="http://192.168.10.132/RESTFul/v1/rating";

    public static String URL_RATING_AVERAGE="http://192.168.10.132/RESTFul/v1/average_rating";

    public static String URL_COMMENT="http://192.168.10.132/RESTFul/v1/comment";
	// Server get driver id

	public static String URL_GET_DRIVER = "http://192.168.10.132/RESTFul/v1/driver/user_id?lang="+lang;

	public static String URL_GET_USER = "http://192.168.10.132/RESTFul/v1/user";

    // Get other user profile
    public static String URL_GET_OTHER_PROFILE = "http://192.168.10.132/RESTFul/v1/users";



	public static String URL_PASSWORD = "http://192.168.10.132/RESTFul/v1/user/password?lang="+lang;

	public static String URL_AVATAR = "http://192.168.10.132/RESTFul/v1/user/link_avatar?lang="+lang;

	public static String URL_PERSONALID_IMG = "http://192.168.10.132/RESTFul/v1/user/personalID_img?lang="+lang;

	public static String URL_DRIVER = "http://192.168.10.132/RESTFul/v1/driver";


	// Show list itinerary for user
	public static String URL_DRIVER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itineraries/driver/itinerary_status?lang="+lang;
	
	// Show list itinerary for customer
	public static String URL_CUSTOMER_ITINERARY = "http://192.168.10.132/RESTFul/v1/itineraries/customer/itinerary_status?lang="+lang;

    public static String URL_GET_ALL_VEHICLE = "http://192.168.10.132/RESTFul/v1/vehicles?lang="+lang;

    public static String URL_GET_VEHICLE = "http://192.168.10.132/RESTFul/v1/vehicle";
}
