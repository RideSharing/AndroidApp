package com.halley.ridesharing;

import java.util.HashMap;

import com.halley.helper.DatabaseHandler;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HomeFragment extends Fragment{
	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;
	
	private DatabaseHandler db;
	private SessionManager session;
	public HomeFragment(){}
	    
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle bundle) {
	  
	        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
	        txtName = (TextView) rootView.findViewById(R.id.name);  
	        txtEmail = (TextView) rootView.findViewById(R.id.email);  
	        btnLogout = (Button) rootView.findViewById(R.id.btnLogout);  


			// Displaying the user details on the screen
			txtName.setText(getArguments().getString("name"));
			txtEmail.setText(getArguments().getString("email"));

			// Logout button click event
//			btnLogout.setOnClickListener(new View.OnClickListener() {
	//
//				@Override
//				public void onClick(View v) {
//					logoutUser();
//				}
//			});
	        
	        return rootView;
	    }
	    

}
