package com.halley.manageitinerary;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.halley.registerandlogin.R;

/**
 * Created by enclaveit on 4/23/15.
 */
public class ManageItineraryCustomerFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View myFragmentView = inflater.inflate(R.layout.fragment_categories_manage_itinerary_customer, container, false);


        return myFragmentView;
    }

}
