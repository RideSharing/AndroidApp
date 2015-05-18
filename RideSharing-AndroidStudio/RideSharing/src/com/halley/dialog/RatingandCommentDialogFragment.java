package com.halley.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.helper.RoundedImageView;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by enclaveit on 5/12/15.
 */
public class RatingandCommentDialogFragment extends DialogFragment {
    private final String DRIVER_ROLE="driver";
    RatingBar rating;
    Activity activity;
    TextView tvRating, tvComment, tvReview;
    String rating_user;
    Button btnOK;
    SessionManager session;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_rating, null);
        getDialog().setTitle(getResources().getString(R.string.rating)+" & "+getResources().getString(R.string.comment));
        rating=(RatingBar)v.findViewById(R.id.ratingBar1);
        tvRating=(TextView)v.findViewById(R.id.rating);
        tvComment=(TextView)v.findViewById(R.id.comment);
        tvReview=(TextView)v.findViewById(R.id.review);
        tvReview.setText(R.string.review_user);
        btnOK=(Button)v.findViewById(R.id.btnRating);
        btnOK.setText(R.string.ok);
        session=new SessionManager(getActivity());
        RoundedImageView img_avatar = (RoundedImageView) v
                .findViewById(R.id.avatar);
        if (session.getAvatar()!=null) {
            byte[] decodeString = Base64.decode(session.getAvatar(),
                    Base64.DEFAULT);
            Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString, 0,
                    decodeString.length);
            img_avatar.setImageBitmap(decodeByte);
        }
        Bundle bundle=getArguments();
        if(bundle!=null){
            rating_user=bundle.getString("rating_user");

        }
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                tvRating.setText(String.valueOf(rating));
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment=tvComment.getText().toString().trim();
                if(comment.length()==0){
                    Toast.makeText(getActivity(),
                            R.string.no_input, Toast.LENGTH_LONG).show();
                }
                else{
                    ratingUser(String.valueOf(rating.getRating()),comment, rating_user);

                }
            }
        });

        return v;
    }
    public void ratingUser(final String rating,final String comment,final String rating_user_id){
        // Tag used to cancel the request
        String tag_string_req = "req_rating";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_RATING, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Rating Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response
                            .substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        // Error in login. Get the error message
                        commentUser(comment, rating_user_id);
                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getActivity(),
                                message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Rating Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("rating", rating);
                params.put("rating_user_id", rating_user_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void commentUser(final String content,final String comment_user_id){
        // Tag used to cancel the request
        String tag_string_req = "req_comment";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_COMMENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Comment Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response
                            .substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getActivity(),
                                message, Toast.LENGTH_LONG).show();
                        dismiss();
                        getActivity().setResult(-1);
                        getActivity().finish();
                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getActivity(),
                                message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Comment Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("content", content);
                params.put("comment_about_user_id", comment_user_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
