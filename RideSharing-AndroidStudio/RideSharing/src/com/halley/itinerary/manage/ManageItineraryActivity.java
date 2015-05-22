package com.halley.itinerary.manage;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.view.View;

import android.widget.CompoundButton;

import android.widget.LinearLayout;
import android.widget.Switch;


import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.itinerary.list.data.ListItineraryByStatus;
import com.halley.registerandlogin.R;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ManageItineraryActivity extends ActionBarActivity {
    private static final int REQUEST_OK= 1;

    private Switch switchrole;
    private ActionBar actionBar;
    private CustomActionBar custom_actionbar;
    private SweetAlertDialog pDialog;
    private SessionManager session;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    LinearLayout switchLayout;
    private static final String ITINERARY_STATUS_CREATED ="1";
    private static final String ITINERARY_STATUS_CUSTOMER_ACCEPTED ="2";
    private static final String ITINERARY_STATUS_DRIVER_ACCEPTED ="3";
    private static final String ITINERARY_STATUS_FINISH ="4";
    private static final String CUSTOMER ="customer";//true is customer
    private static final String DRIVER ="driver";// false is driver
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_itinerary);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        session=new SessionManager(getApplicationContext());
        switchrole = (Switch) findViewById(R.id.switchrole);
        switchLayout=(LinearLayout)findViewById(R.id.switchLayout);
        if(!session.isDriver()){
            switchLayout.setVisibility(View.GONE);
            switchrole.setChecked(true);
        }
        else {

            switchrole.setChecked(false);
            switchrole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        fragmentManager = getFragmentManager();

                        fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentManager.popBackStack();

                        ManageItineraryCustomerFragment fragmentS1 = new ManageItineraryCustomerFragment();
                        fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
                        fragmentTransaction.commit();

                    } else {

                        fragmentManager = getFragmentManager();
                        fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentManager.popBackStack();

                        ManageItineraryDriverFragment fragmentS1 = new ManageItineraryDriverFragment();
                        fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
                        fragmentTransaction.commit();
                    }

                }
            });
        }
        if(switchrole.isChecked()){

            //check the current state before we display the screen
            fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentManager.popBackStack();

            ManageItineraryCustomerFragment fragmentS1 = new ManageItineraryCustomerFragment();
            fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
            fragmentTransaction.commit();
        }
        else{

            //check the current state before we display the screen
            fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentManager.popBackStack();

            ManageItineraryDriverFragment fragmentS1 = new ManageItineraryDriverFragment();
            fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
            fragmentTransaction.commit();
        }

    }
    //customer
    public void acceptOnclick(View v) {
        displaylistItineraryFragment(CUSTOMER, ITINERARY_STATUS_DRIVER_ACCEPTED);

    }

    public void waitingOnclick(View v) {
        displaylistItineraryFragment(CUSTOMER, ITINERARY_STATUS_CUSTOMER_ACCEPTED);
    }
    //driver
    public void createdOnclick(View v) {
        displaylistItineraryFragment(DRIVER, ITINERARY_STATUS_CREATED);
    }
    public void waitingUserOnclick(View v) {
        displaylistItineraryFragment(DRIVER, ITINERARY_STATUS_CUSTOMER_ACCEPTED);
    }
    public void acceptedUserOnclick(View v) {
        displaylistItineraryFragment(DRIVER, ITINERARY_STATUS_DRIVER_ACCEPTED);
    }
    public void finishCustomerOnclick(View v){
        displaylistItineraryFragment(CUSTOMER, ITINERARY_STATUS_FINISH);
    }
    public void finishDriverOnclick(View v){
        displaylistItineraryFragment(DRIVER, ITINERARY_STATUS_FINISH);
    }



    public void displaylistItineraryFragment(String role, String status) {
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.addToBackStack(null);
        ListItineraryByStatus list_itinerary = new ListItineraryByStatus();
        Bundle bundle = new Bundle();
        bundle.putString("status", status);
        bundle.putString("role", role);
        list_itinerary.setArguments(bundle);
        fragmentTransaction.replace(R.id.enabled_view_foo, list_itinerary);
        fragmentTransaction.commit();

    }


    @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {

            if(switchrole.isChecked()){
                //check the current state before we display the screen
                fragmentManager = getFragmentManager();
                fragmentTransaction = fragmentManager
                        .beginTransaction();
                fragmentManager.popBackStack();
                ManageItineraryCustomerFragment fragmentS1 = new ManageItineraryCustomerFragment();
                fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
                fragmentTransaction.commit();
            }
            else{
                //check the current state before we display the screen
                fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                fragmentTransaction = fragmentManager
                        .beginTransaction();

                ManageItineraryDriverFragment fragmentS1 = new ManageItineraryDriverFragment();
                fragmentTransaction.replace(R.id.enabled_view_foo, fragmentS1);
                fragmentTransaction.commit();
            }
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_OK) {
            if(resultCode == RESULT_OK){
                startActivity(getIntent());
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

}

