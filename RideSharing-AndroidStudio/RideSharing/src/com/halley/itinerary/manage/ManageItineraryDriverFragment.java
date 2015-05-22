package com.halley.itinerary.manage;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.halley.registerandlogin.R;

/**
 * Created by enclaveit on 4/23/15.
 */
public class ManageItineraryDriverFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View myFragmentView = inflater.inflate(R.layout.fragment_categories_manage_itinerary_driver, container, false);

        return myFragmentView;
    }
}
